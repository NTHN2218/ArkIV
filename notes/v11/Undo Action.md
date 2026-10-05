# ArkIV — Structural Undo (`actionUndo`) Design Document

Branch: `10.1.1` (lineage `main → v10 → 10.1.1`) Feature name: **actionUndo** — structural/action-level undo, distinct from any future **textUndo** (per-textbox typing undo, shelved, not committed to)

---

## 1. Core Decisions

- No redo — undo only. Dropping redo significantly reduces logging complexity (no forward-stack bookkeeping).
- Action-level granularity — logs major actions only, not keystrokes.
- Unlimited history for the current session — the action log is discarded only when the program closes.
- Applies across all registers, not scoped to just the one currently open.
- Runs entirely in RAM — deliberately not persisted to disk. JSON/disk persistence is reserved for `saveTasks()`/`RegisterManager`, which already covers durability; undo history is explicitly session-only.

---

## 2. Logged Actions

### Entry-Level

- _Create_ main/sub Entry
- _Delete_ main/sub Entry (main-entry delete cascades to its sub-Entries)
- _Edit_ main/sub Entry text
- _Move Up/Down_ main/sub Entry

### Register-Level

- _Create_ register
- _Rename_ register
- _Delete_ register
- _Reorder_ register
- _Set Default_ register
- _Recognize_ (an unrecognized register file)

### Explicitly NOT Logged

- _Collapse/Expand_ (single entry or Collapse All / Expand All) — considered unnecessary; easily reversible by hand, not destructive. (A bundled `CollapseExpandAll` action type was designed at one point to batch-log Collapse All / Expand All as one entry, but was later deliberately removed/ditched during implementation to reduce scope.)
- _Undo itself_ — never logged. Logging an undo would create an entry whose own reversal is itself, risking infinite loops.

---

## 3. Logging Philosophy

- **Log the reversal instruction, not a description of the action.** When an action is logged, the exact steps required to _reverse_ it are computed and stored at that moment — not a record of what happened. Undo therefore executes exactly what's logged, with zero interpretation/processing needed at undo-time.
- **IDs are stable for the whole session.** Task IDs and register IDs only ever increment (`taskCounter`, `RegisterManager.nextId()`); they are never reused or reassigned, even after deletes. This makes anchoring reversal data to IDs safe.
- **Anchor stability is guaranteed for free by strict LIFO ordering with no redo.** By the time any log entry is reached for undo, everything logged after it has already been undone (in order). This guarantees the world looks exactly as it did the instant that entry was created — no drift, no stale references, no need to re-derive context at undo-time.
- **Consumption:** each logged action is only ever undone once. Once visited during undo, it's popped/consumed, preventing any possibility of repeat/loop reversal of the same action.

---

## 4. Handling Delete (and the fan-out problem)

A naive approach would log one entry per _affected task_, which blows up badly given ArkIV's limits (999 sub-Entries per main Entry, 999 main Entries per register — i.e. up to ~998,001 tasks in the worst case per register).

**Resolution:** log one entry per _action_, not per affected task.

- A main-Entry delete bundles its own data **plus all its sub-Entries' data** as one snapshot (`TaskSnapshot` for the entry + `List<TaskSnapshot>` for children) inside a single `DeleteEntry` log entry.
- A register delete bundles the **entire register file content** (the full JSON blob, read via `getRegisterFileContent()`) plus its header metadata (id, name, filename, order, was-it-default) as one `DeleteRegister` log entry.

This collapses the 999×999 fan-out problem entirely — one register-delete is always exactly one log entry, however large.

---

## 5. Stack Structure

- **One global stack** — holds all register-level actions (create/rename/delete/reorder/set-default/recognize register).
- **Per-register local stacks** — holds entry-level actions, keyed _externally_ by register ID in a `Map<Integer, Deque<UndoAction>>` inside the undo manager itself (never attached to the register object). This is essential: it lets a register's local stack **survive its own register being deleted**, so deleting a register can later be undone along with its full action history intact.
- **Global sequence number.** Every logged action (global or local) receives a shared, ever-incrementing `sequence` value, stamped at `log()` time. This is the single source of truth for true chronological order across both tiers — it is what lets the two physically separate stacks be compared for "which happened more recently" without merging them into one structure.
- **Ctrl+Z resolution rule:** while viewing a given register, compare only the _global_ stack's top entry vs. that _specific register's_ local stack top, by sequence number, and undo whichever is higher (more recent). **Never** reaches into another (non-current) register's local stack — by design, not an oversight.
- **Nothing is discarded mid-session** — including orphaned local stacks belonging to registers that were deleted and never had that deletion undone. Everything persists until program close.

### Worked example of sequence-number resolution

```
seq=0  Create Register A        → GLOBAL
seq=1  Create Entry 1 in A      → LOCAL(A)
seq=2  Create Entry 2 in A      → LOCAL(A)
seq=3  Switch to Register B     (not loggable)
seq=4  Create Entry 1 in B      → LOCAL(B)
seq=5  Switch back to A
seq=6  Rename Register A        → GLOBAL
```

Viewing A, first Ctrl+Z: global top = seq 6, local(A) top = seq 2 → 6 wins (rename undone). Second Ctrl+Z: global top = seq 0 (create register), local(A) top = seq 2 → 2 wins (Entry 2 creation undone), _not_ the older global entry — proving the comparison is genuinely chronological, not global-first.

---

## 6. UX Behavior

- If the winning undo is a **global** action, the result is reflected **only in the sidebar** — the user is never force-navigated away from whichever register they're currently viewing.
- Every undone action produces a **toast** confirming the restoration/reversal (see §9 for exact wording).
- If a reversal restores something inside a **collapsed** main Entry, that Entry **auto-expands** so the change is visible.
- If a reversal restores something off-screen, the view **auto-scrolls** to that location.

---

## 7. Data Structure

- `sealed interface UndoAction` (Java 17+ sealed types; project runs on **Java 25**, confirmed LTS, pattern-matching switch fully supported) with `sequence()` and `registerId()` as common accessors.
- One `record` per action type, **each in its own file** (Java requires any `public` top-level type to live in a file named after it — this was hit as a real compile error early on and corrected):
    - `CreateEntry(sequence, registerId, taskId)`
    - `DeleteEntry(sequence, registerId, deletedTask: TaskSnapshot, deletedChildren: List<TaskSnapshot>, anchorAfterTaskId)`
    - `EditEntry(sequence, registerId, taskId, oldText)`
    - `MoveEntry(sequence, registerId, taskIdA, taskIdB)`
    - `CreateRegister(sequence, registerId, newRegisterId)`
    - `DeleteRegister(sequence, registerId, oldRegisterId, name, filename, oldOrder, wasDefault, fullFileContentJson)`
    - `RenameRegister(sequence, registerId, oldName)`
    - `ReorderRegister(sequence, registerId, registerIdA, registerIdB)`
    - `SetDefaultRegister(sequence, registerId, previousDefaultId)`
    - `RecognizeRegister(sequence, registerId, newRegisterId)`
- `permits` lists every one of the above on `UndoAction` — this means the compiler **forces every exhaustive switch to handle all types**; adding an 11th type later without a matching case anywhere fails to _compile_, not silently misbehave. This was the explicit reason `sealed` was chosen over a plain abstract class.
- `TaskSnapshot(id, parentId, text, isDone, isSub, isCollapsed)` — the reusable per-task data record used inside `DeleteEntry`.
- Records are immutable, which is correct for something meant to be a frozen snapshot of the past — but it means the real `sequence` value isn't known at construction time at the call site (only `ActionUndoManager` knows the running counter). Callers construct with a placeholder `sequence=0`; `log()` rebuilds the record with the true sequence stamped in via a `withSequence()` switch before pushing it onto a stack.

---

## 8. Code Architecture (`Undo` package)

- **`UndoAction`** — the sealed interface (pure data contract).
- **One file per record type** (see §7).
- **`UndoDebug`** — two-boolean debug gate mirroring the existing `MarkdownDebug` pattern: `ACTIVE` (master switch) and `VERBOSE` (density). `summary()` always prints when active; `verbose()` only when both are true. (`VERBOSE` was later set to `false` by NTHN mid-session — too much non-essential data during stress-test runs.)
- **`ActionUndoManager`** — owns:
    - `Deque<UndoAction> globalStack`
    - `Map<Integer, Deque<UndoAction>> localStacks`
    - `long sequenceCounter`
    - `log(UndoAction action)` — routes to global or local stack based on action type, stamps sequence
    - `undo(int currentRegisterId)` — the global-vs-local sequence comparison, pop, dispatch, UX side-effects (toast/sidebar/expand/scroll)
    - `dispatch(UndoAction action)` — exhaustive pattern-matching switch routing to one private `reverseXxx()` handler per type
    - Ten private `reverseXxx()` methods, each calling into the handed-in callbacks to literally execute the stored reversal
- **Access to `ArkIV`'s internals** is solved via the same **callback pattern** already used elsewhere in the codebase for `EditMenu`/`FileMenu` (`Runnable`/`Consumer`/`BiConsumer`/custom functional interfaces passed in at construction). `ActionUndoManager` never reaches into `ArkIV`'s private state directly.
    - `UndoCallbacks` — bundle of primitives: `reinsertTask`, `deleteTaskById`, `editTaskText`, `swapTasksById`, `deleteRegisterById`, `restoreRegister`, `renameRegisterById`, `reorderRegistersById`, `setDefaultRegisterById`, `unrecognizeRegisterById`, `showToast`, `expandIfCollapsed`, `scrollToTask`, `refreshRegisterSidebar`.
    - Two small custom functional interfaces, each in its own file (same public-type-per-file rule): `TaskReinserter`, `RegisterRestorer`.
- `log()` is called **manually** at the point of each accounted-for action, inside the existing action methods (`addTaskFromInput`, `createSubEntry`, `confirmDeleteTask`, `editEntry`'s submit, `moveTaskUp`/`moveTaskDown`, `handleCreateRegister`, `commitInlineRename`, `onSetDefault`, `onMoveUp`/`onMoveDown`, `handleDeleteRegister`, `onRecognize`). No central interception/event bus — matches ArkIV's existing direct-call style.

### Naming collision note

Java's own `javax.swing.undo.UndoManager` exists in a different package. No actual conflict, but the custom class was deliberately named `ActionUndoManager` (not bare `UndoManager`) to avoid import confusion — this also nicely lines up with the `actionUndo`/`textUndo` naming split (see §12).

---

## 9. Toast Messages

Kept deliberately generic/categorical — never embeds entry text, since entries can hold arbitrarily large content (up to full "textbook"-scale pasted text was explicitly discussed as a worst case).

**Entry-level**

|Action|Toast|
|---|---|
|Undo Create (main/sub)|"Entry creation undone"|
|Undo Delete (main, no children)|"Entry restored"|
|Undo Delete (main, with children)|"Entry and sub-entries restored"|
|Undo Delete (sub)|"Sub-entry restored"|
|Undo Edit/Rename text|"Edit undone"|
|Undo Move (up or down)|"Move undone"|

**Register-level**

|Action|Toast|
|---|---|
|Undo Create Register|"Register creation undone"|
|Undo Delete Register|"Register restored"|
|Undo Rename Register|"Register name reverted"|
|Undo Reorder Register|"Register order undone"|
|Undo Set Default|"Default register reverted"|
|Undo Recognize|"Register unrecognized again"|

(Collapse/Expand-All toast — "Entry states restored" — was designed during the brief window that batch-logging was considered, then became moot once that logging was removed.)

---

## 10. Keybinding

- **Ctrl+Z** → triggers structural undo (`performUndo()` → `actionUndoManager.undo(currentRegisterId)`), bound at the **root level** (`JRootPane` `InputMap`/`ActionMap`, `WHEN_IN_FOCUSED_WINDOW`), matching the existing pattern used for Ctrl+Tab register cycling. Fires unconditionally — no focus-context branching.
- **Ctrl+Shift+Z** was considered as a reserved "force structural undo regardless of focus" escape hatch (relevant only if textUndo were ever built, so a textbox's local undo wouldn't shadow structural undo) — **not actually assigned**, since textUndo was shelved.
- `EditMenu`'s previously-dead `undo()` stub was rewired to call the same `performUndo()` callback, so the Edit menu item and the keybind trigger identical behavior.

---

## 11. Memory Footprint

- Deleted/changed text is **not duplicated** — Java `String`s are immutable, so a log entry just holds a reference to the same String object that would otherwise have become garbage. The cost is "data that would have been garbage-collected stays alive," not "data gets copied."
- Realistic everyday sessions: low single-digit MB total undo-log footprint.
- Deliberately pathological worst case (repeated huge-text deletes): estimated around tens of MB — still trivial against typical system RAM (estimated <1% of 16GB).
- A "spill large entries to a temp JSON directory on disk, keep only a path reference in the stack" idea was discussed in detail and **deliberately shelved** as over-engineering for a rare worst case that the numbers don't justify — explicitly logged as a **possible future-version escape hatch**, not part of this feature's scope. Simpler alternatives (cap by entry count or cumulative memory budget) were also discussed as lower-effort fallbacks if this ever becomes a real problem.

---

## 12. Branching

```
main
 └── v10                      (merged to main only once all of v10 is complete)
      └── 10.1.1               (actionUndo — this feature)
```

- `textUndo` (per-textbox typing undo, via Swing's built-in `javax.swing.undo` mechanism) was originally planned as a sibling branch (`10.1.2`), but was explicitly shelved/deprioritized — current branch naming simplified to just `10.1.1` directly under `v10` rather than nesting `10.1.1`/`10.1.2` under a `10.1` parent, since there's no sibling feature in active development.
- `v10` only merges to `main` once the **entire** v10 version (not just undo) is complete.

---

### Design principle

Rather than hand-writing a second, independent "what the correct result should be" model (which risks encoding the same assumptions/bugs as the real code), the tester leans on the system's own designed invariant: **undoing action N must restore the world to exactly how it looked the instant before action N happened.** This is directly checkable by snapshotting real state before each action and diffing against real state after undoing back to that point — no hand-authored "expected" logic needed.

### Components

- **`WorldSnapshot`** — full ground truth at one instant: every register's header info (id, name, file, order) + every register's task contents (`TaskFields`: id, parentId, text, isDone, isSub, isCollapsed), read directly from disk/memory (register files are always authoritative immediately after an action since every action already calls `saveTasks()`/`saveHeader()`).
- **`StressCallbacks`** — bundle of headless (no-dialog) action triggers (`stressCreateMainEntry`, `stressCreateSubEntry`, `stressDeleteRandomEntry`, `stressEditRandomEntry`, `stressMoveRandomEntry`, `stressCreateRegister`, `stressDeleteRandomRegister`, `stressRenameRandomRegister`, `stressReorderRandomRegisters`, `stressSetRandomDefault`, `stressSwitchToRandomRegister`), plus `captureWorld` and `performUndoOn`(registerId). Headless variants were required because real action methods are wrapped in confirm dialogs the tester can't click through.
- **`UndoStressTester`** — holds a `Map<Long, WorldSnapshot>` keyed by **sequence number** (not stack position — chosen specifically because register-level actions mean draining isn't always one clean linear pop order across registers) of pre-action snapshots.
    - `runRandomActions(int count)` — randomly fires actions from the pool, snapshotting world state immediately before each one actually gets logged.
    - `undoEverythingAndVerify(List<Integer> allRegisterIds)` — loops across all registers' current-undo-availability, popping/undoing, then diffing actual post-undo state against the recorded pre-action snapshot for that sequence number; logs `❌ MISMATCH` with both full states on any discrepancy.
- Triggered via a temporary root-level keybind, **Ctrl+Shift+T**, wired the same way as Ctrl+Z, calling `runRandomActions(100)` then `undoEverythingAndVerify(allIds)`.
- A later refinement replaced the raw full-dump mismatch printer with a targeted **diff printer** (`printDiff`) that reports only the specific fields/ids that differ (missing ids, extra ids, reordered-but-same-set ids, `defaultRegisterId` mismatch, register-order mismatch) rather than dumping both entire world states — the original dumps were functionally unreadable at scale.

---

## 14. Real Bug Found & Diagnosed via the Stress Tester

### Symptom

Running the stress tester surfaced a large, sustained run of mismatches — the same task IDs kept appearing as "extra"/"missing" across dozens of consecutive undos without ever resolving, rather than isolated one-off discrepancies.

### Root cause

`ActionUndoManager.undo(registerId)` only used `registerId` to select _which stack_ to pop from — it never actually switched the in-memory application state (`allTasks`, `idToTaskMap`, `taskPanel`, `currentRegisterId`) to match that register before dispatching the reversal. All the real reversal callbacks (`deleteTaskById`, `editTaskText`, `swapTasksById`, `reinsertTask`) operate on whatever register happens to be currently loaded in memory — not necessarily the register the undo logically belongs to.

In real usage this was invisible: Ctrl+Z always calls `undo(currentRegisterId)` for whatever's already on screen, so loaded-register and target-register were always identical by construction. It only surfaced once the stress tester (correctly, per the local-stack design) exercised undoing a register that wasn't the one currently displayed — a legitimate scenario the design always allowed for but that no code path had actually been built to handle.

The specific test harness function responsible, `performUndoOnRegister`, even had a comment stating the _intended_ behavior ("Temporarily undo AS IF the given register were current, without navigating away") that the code underneath never actually implemented.

### Fix

`performUndoOnRegister` was updated to actually call `switchToRegister(target)` (the existing, already-correct load/save method used for normal register switching) before invoking `actionUndoManager.undo(registerId)`, whenever the target register differs from `currentRegisterId`. A `null` guard was added for the edge case where the target register doesn't currently exist (i.e. its own `DeleteRegister` hasn't been undone yet, so there's no file to load) — returning `null` from the tester in that case rather than crashing, since that register's local-stack contents genuinely cannot be reversed until the register itself is restored first (which will happen naturally since `DeleteRegister` lives on the global stack).

### Secondary fix (crash hardening)

A separate `NoSuchElementException` crash was found in `undo()`'s `pop()` calls (a narrow peek/pop race). Wrapped in try/catch treating it as "nothing to undo" rather than crashing, to keep the stress tester's long runs resilient without masking the real underlying bug above.

### Process lesson (explicitly identified)

The local-stack design established early on that reversal instructions are register-scoped and self-contained — but the implication that the _callbacks executing_ those instructions also need to operate on that specific register's data (not whatever's currently in memory) was never explicitly stated or enforced as a design requirement. It was a gap in the Phase 3/4 callback contract, not something that got broken later — it should have been an explicit design question ("does a local undo require switching registers first, or do callbacks need to be register-parameterized?") asked at stack-design time, and wasn't.

---

## 15. Explicitly Deferred / Out of Scope

- `textUndo` — per-textbox typing undo via Swing's built-in mechanism (shelved indefinitely, no committed branch).
- Spill-large-entries-to-disk memory optimization (shelved as unnecessary given realistic memory footprint numbers).
- Collapse/Expand (including Collapse All / Expand All) logging — explicitly decided not to log, after briefly being designed then removed.