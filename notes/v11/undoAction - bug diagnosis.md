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