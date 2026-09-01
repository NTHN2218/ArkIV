1. **Core Decisions** 
	- No redo - reduces complexity of logging performed actions
	- Action level granularity - logs major actions only
	- Unlimited history of logged action for current session - Action log is discarded only on when program is closed
	- Applies across all registers
	
2. **Logged Actions**
	- Entry-Level
		- *create* main/sub entry
		- *delete* main/sub entry
		- *edit* main/sub entry
		- *move up/down* main/sub entry
	 - Register-Level
		 - *create* register
		 - *rename* register
		 - *delete* register
		 - *re-order* register
		 - *set default* register
		 - *recognize/remove* register
	- Actions Not Logged
		- *collapse/expand* main/sub entry - unnecessary
		- *undo* - can lead to infinite loops
	
3. **Logging Philosophy**
	- Log the reversal instruction - When an action is logged, instead of logging the description of the performed action , the exact steps required to reverse that action is logged. Therefore, to undo the program simply executes exactly what is logged, thus zero processing is required at undo time.
	- IDs (task ids, register ids) are stable for the whole session — they only ever increment, never get reused/reassigned — so anchoring reversal data to IDs is safe.
	- Anchor stability is guaranteed for free by strict **LIFO** ordering with no redo: by the time any log entry is reached for undo, everything logged after it has already been undone, so the world is guaranteed to look exactly like it did the instant that entry was created. This ensures no drift, no stale references.
	- Since every action gets logged, to undo it simply needs to walk back through the queue to reverse an action. Once a logged has been visited during undoing that log gets consumed to ensure no repeating loops of same action.
	
4. **Handling Delete**
	- A delete logs one entry per _action_, not one per affected task — a main entry's delete bundles its own data plus all its sub-entries' data as one snapshot. A register delete bundles the entire register file content as one snapshot. This avoids the 999×999 fan-out problem entirely.
	
5. **Stack Structure**
	- One **global stack**: register-level actions.
	- **Per-register local stacks**, keyed externally by register id (in the undo manager, not attached to the register object) — this lets a local stack survive its own register being deleted, so deleting a register can still be undone along with its full history.
	- Every logged action (global or local) gets a shared, ever-incrementing **sequence number** — the single source of truth for true chronological order across both tiers.
	- Ctrl+Z while inside a register: compares only the _global_ stack's top entry vs. that _specific register's_ local stack top, by sequence number, and undoes whichever is higher (more recent). Never looks at other registers' local stacks.
	- No stacks are ever discarded mid-session, including orphaned local stacks from deleted-and-never-undone registers — everything persists until program close. 
6. **UX**
	- If the winning undo is a global action, it's reflected only in the sidebar — no forced navigation away from whatever register the user is currently viewing.
	- Every undone action gets a toast to visually indicate to restoration, reversal of that action.
	-  When a reversal restores something inside a collapsed main entry, that entry auto-expands so the change is visible.
	- When a reversal restores something scrollbar moves to that location.
	
7. **Data Structure** 
	- `sealed interface UndoAction` with one `record` per action type (Java 17), e.g. `CreateEntry`, `DeleteEntry`, `EditEntry`, `MoveEntry`, `CreateRegister`, `DeleteRegister`, `RenameRegister`, `ReorderRegister`, `SetDefaultRegister`, `RecognizeRegister`.
	- `sealed` means the compiler forces every switch/dispatch to handle all action types — adding a new type later can't be silently forgotten.
	- Records are immutable and boilerplate-free — a good fit for something meant to be a frozen snapshot.
	- Delete-type records bundle a `TaskSnapshot` (id, parentId, text, isDone, isSub, isCollapsed) for the deleted task and, if applicable, a list of them for cascaded children.
	
8. **Memory**
	-  `UndoAction` — the sealed interface + record definitions (pure data).
	- `UndoManager` — owns `Deque<UndoAction> globalStack`, `Map<Integer, Deque<UndoAction>> localStacks`, the sequence counter, `log()`, and `undo(currentRegisterId)` (the global-vs-local sequence comparison + pop + dispatch).
	- Reversal handlers — private methods inside `UndoManager`, one per action type, invoked via a pattern-matching switch over the sealed type.
	- Access to main-class internals is solved via the same **callback pattern** already used for `EditMenu`/`FileMenu` (`Runnable`/`Consumer`/`BiConsumer` passed in at construction) — `UndoManager` never reaches into private state directly, it's handed small execution primitives (reinsert task, delete by id, swap by id, restore register file, etc.) from the main class.
	- `log()` gets called manually at the point of each accounted-for action (no central interception/event bus — matches ArkIV's existing direct-call style).
	
 9. **Code Architecture (dedicated Undo package)**
	- `UndoAction` — the sealed interface + record definitions (pure data).
	- `UndoManager` — owns `Deque<UndoAction> globalStack`, `Map<Integer, Deque<UndoAction>> localStacks`, the sequence counter, `log()`, and `undo(currentRegisterId)` (the global-vs-local sequence comparison + pop + dispatch).
	- Reversal handlers — private methods inside `UndoManager`, one per action type, invoked via a pattern-matching switch over the sealed type.
	- Access to main-class internals is solved via the same **callback pattern** already used for `EditMenu`/`FileMenu` (`Runnable`/`Consumer`/`BiConsumer` passed in at construction) — `UndoManager` never reaches into private state directly, it's handed small execution primitives (reinsert task, delete by id, swap by id, restore register file, etc.) from the main class.
	- `log()` gets called manually at the point of each accounted-for action (no central interception/event bus — matches ArkIV's existing direct-call style).
10. **Toast messages**
	 **Entry-level**
	 
	- **Undo Create** (main/sub) → `"Entry creation undone"`
	- **Undo Delete** (main) → `"Entry restored"` (or, if it had children: `"Entry and sub-entries restored"`)
	- **Undo Delete** (sub) → `"Sub-entry restored"`
	- **Undo Edit/Rename text** → `"Edit undone"`
	- **Undo Move** (up or down) → `"Move undone"`
	
	**Register-level**
	
	- **Undo Create Register** → `"Register creation undone"`
	- **Undo Delete Register** → `"Register restored"`
	- **Undo Rename Register** → `"Register name reverted"`
	- **Undo Reorder Register** → `"Register order undone"`
	- **Undo Set Default** → `"Default register reverted"`
	- **Undo Recognize** → `"Register unrecognized again"`
	- 