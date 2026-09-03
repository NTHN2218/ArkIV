package Undo;

import java.util.*;

/**
 * Owns all undo history for the session. Two-tier storage:
 *  - globalStack: register-level actions (create/delete/rename/reorder register, etc.)
 *  - localStacks: per-register entry-level actions, keyed by register id
 *    (kept external to the register object so a local stack survives its
 *    own register being deleted -- see DeleteRegister reversal in Phase 4).
 *
 * Every logged action gets a shared, ever-incrementing sequence number --
 * the single source of truth for true chronological order across both tiers.
 * Ctrl+Z compares only globalStack's top vs. the CURRENT register's local
 * top by sequence, and undoes whichever is higher. Never touches other
 * registers' local stacks.
 *
 * Nothing is ever discarded mid-session, including orphaned local stacks
 * from deleted-and-never-undone registers -- everything persists until
 * program close (session-only, never written to disk).
 */
public class ActionUndoManager {

    private final Deque<UndoAction> globalStack = new ArrayDeque<>();
    private final Map<Integer, Deque<UndoAction>> localStacks = new HashMap<>();

    private long sequenceCounter = 0;

    private final UndoCallbacks callbacks;

    public ActionUndoManager(UndoCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * Logs a completed action's precomputed reversal instruction.
     * Routes to globalStack or the correct per-register localStack
     * based on the action's type, and stamps it with the next
     * sequence number.
     *
     * IMPORTANT: never call this from inside a reversal handler --
     * doing so would log the undo itself, risking infinite loops.
     */
    public void log(UndoAction action) {
        long seq = sequenceCounter++;

        // Records are immutable, so "stamping" the sequence means
        // constructing a fresh copy with the sequence filled in.
        // withSequence() is a small per-type helper -- see below.
        UndoAction stamped = withSequence(action, seq);

        if (isGlobalAction(stamped)) {
            globalStack.push(stamped);
            UndoDebug.summary("Logged GLOBAL action seq=" + seq + " type=" + stamped.getClass().getSimpleName());
        } else {
            int regId = stamped.registerId();
            localStacks.computeIfAbsent(regId, k -> new ArrayDeque<>()).push(stamped);
            UndoDebug.summary("Logged LOCAL action seq=" + seq + " registerId=" + regId
                    + " type=" + stamped.getClass().getSimpleName());
        }

        UndoDebug.verbose(stamped.toString());
    }

    /**
     * True for register-level action types -> globalStack.
     * False for entry-level action types -> that register's localStack.
     */
    private boolean isGlobalAction(UndoAction action) {
        return switch (action) {
            case CreateRegister a -> true;
            case DeleteRegister a -> true;
            case RenameRegister a -> true;
            case ReorderRegister a -> true;
            case SetDefaultRegister a -> true;
            case RecognizeRegister a -> true;

            case CreateEntry a -> false;
            case DeleteEntry a -> false;
            case EditEntry a -> false;
            case MoveEntry a -> false;
        };
    }

    /**
     * Records are immutable -- since callers construct an action with
     * sequence=0 as a placeholder (they don't know the real sequence
     * number until log() assigns it), this rebuilds the same record
     * with the real sequence filled in.
     */
    private UndoAction withSequence(UndoAction action, long seq) {
        return switch (action) {
            case CreateEntry a -> new CreateEntry(seq, a.registerId(), a.taskId());
            case DeleteEntry a -> new DeleteEntry(seq, a.registerId(), a.deletedTask(), a.deletedChildren(), a.anchorAfterTaskId());
            case EditEntry a -> new EditEntry(seq, a.registerId(), a.taskId(), a.oldText());
            case MoveEntry a -> new MoveEntry(seq, a.registerId(), a.taskIdA(), a.taskIdB());

            case CreateRegister a -> new CreateRegister(seq, a.registerId(), a.newRegisterId());
            case DeleteRegister a -> new DeleteRegister(seq, a.registerId(), a.oldRegisterId(), a.name(),
                    a.filename(), a.oldOrder(), a.wasDefault(), a.fullFileContentJson());
            case RenameRegister a -> new RenameRegister(seq, a.registerId(), a.oldName());
            case ReorderRegister a -> new ReorderRegister(seq, a.registerId(), a.registerIdA(), a.registerIdB());
            case SetDefaultRegister a -> new SetDefaultRegister(seq, a.registerId(), a.previousDefaultId());
            case RecognizeRegister a -> new RecognizeRegister(seq, a.registerId(), a.newRegisterId());
        };
    }

    // ── Temporary Phase 2 inspection helpers (safe to keep -- useful for debugging later too) ──

    public boolean hasGlobalAction() {
        return !globalStack.isEmpty();
    }

    public boolean hasLocalAction(int registerId) {
        Deque<UndoAction> stack = localStacks.get(registerId);
        return stack != null && !stack.isEmpty();
    }

    public UndoAction peekGlobal() {
        return globalStack.peek();
    }

    public UndoAction peekLocal(int registerId) {
        Deque<UndoAction> stack = localStacks.get(registerId);
        return stack == null ? null : stack.peek();
    }

    /**
     * Compares globalStack's top vs. the CURRENT register's local top by
     * sequence number, pops whichever is more recent, and dispatches its
     * reversal. Never touches other registers' local stacks.
     *
     * Returns true if something was undone, false if both stacks (global +
     * current-local) were empty -- lets the caller decide whether to show
     * "nothing to undo" feedback.
     */
    public UndoAction undo(int currentRegisterId) {
        UndoAction globalTop = globalStack.peek();
        Deque<UndoAction> localStack = localStacks.get(currentRegisterId);
        UndoAction localTop = (localStack == null) ? null : localStack.peek();

//        if (globalTop == null && localTop == null) {
//            UndoDebug.summary("Undo requested but nothing to undo (registerId=" + currentRegisterId + ")");
//            return false;
//        }

        boolean useGlobal = (localTop == null) ||
                (globalTop != null && globalTop.sequence() > localTop.sequence());

        UndoAction winner = useGlobal ? globalStack.pop() : localStack.pop();

        UndoDebug.summary("Undoing seq=" + winner.sequence() + " type=" + winner.getClass().getSimpleName()
                + " (" + (useGlobal ? "GLOBAL" : "LOCAL") + ")");
        UndoDebug.verbose(winner.toString());

        dispatch(winner);

        // ── Phase 7: UX feedback ──
        callbacks.showToast.accept(toastMessageFor(winner));

        if (useGlobal) {
            callbacks.refreshRegisterSidebar.run();
        } else {
            Integer targetTaskId = affectedTaskId(winner);
            if (targetTaskId != null) {
                callbacks.expandIfCollapsed.accept(targetTaskId);
                callbacks.scrollToTask.accept(targetTaskId);
            }
        }

        return winner;
    }

    public long lastLoggedSequence() {
        return sequenceCounter - 1;
    }

    /**
     * Maps each action type to its toast message. Kept generic/categorical --
     * never embeds entry text, since entries can hold arbitrarily large content.
     */
    private String toastMessageFor(UndoAction action) {
        return switch (action) {
            case CreateEntry a -> "Entry creation undone";
            case DeleteEntry a -> a.deletedTask().isSub()
                    ? "Sub-entry restored"
                    : (a.deletedChildren().isEmpty() ? "Entry restored" : "Entry and sub-entries restored");
            case EditEntry a -> "Edit undone";
            case MoveEntry a -> "Move undone";

            case CreateRegister a -> "Register creation undone";
            case DeleteRegister a -> "Register restored";
            case RenameRegister a -> "Register name reverted";
            case ReorderRegister a -> "Register order undone";
            case SetDefaultRegister a -> "Default register reverted";
            case RecognizeRegister a -> "Register unrecognized again";
        };
    }

    /**
     * Returns the task id a LOCAL undo should expand-if-collapsed + scroll to,
     * or null if there's nothing meaningful to focus (e.g. CreateEntry's
     * reversal deletes the task -- nothing left on screen to point at).
     */
    private Integer affectedTaskId(UndoAction action) {
        return switch (action) {
            case DeleteEntry a -> a.deletedTask().id();
            case EditEntry a -> a.taskId();
            case MoveEntry a -> a.taskIdA();
            case CreateEntry a -> null;

            case CreateRegister a -> null;
            case DeleteRegister a -> null;
            case RenameRegister a -> null;
            case ReorderRegister a -> null;
            case SetDefaultRegister a -> null;
            case RecognizeRegister a -> null;
        };
    }

    /**
     * Routes each action type to its private reversal handler. sealed +
     * exhaustive switch means a new UndoAction type added later without a
     * matching case here fails to COMPILE, not silently misbehave.
     */
    private void dispatch(UndoAction action) {
        switch (action) {
            case CreateEntry a -> reverseCreateEntry(a);
            case DeleteEntry a -> reverseDeleteEntry(a);
            case EditEntry a -> reverseEditEntry(a);
            case MoveEntry a -> reverseMoveEntry(a);

            case CreateRegister a -> reverseCreateRegister(a);
            case DeleteRegister a -> reverseDeleteRegister(a);
            case RenameRegister a -> reverseRenameRegister(a);
            case ReorderRegister a -> reverseReorderRegister(a);
            case SetDefaultRegister a -> reverseSetDefaultRegister(a);
            case RecognizeRegister a -> reverseRecognizeRegister(a);
        }
    }

// ── Entry-level reversal handlers ───────────────────────────────────

    private void reverseCreateEntry(CreateEntry a) {
        // Reversing a create = delete the task that was created.
        callbacks.deleteTaskById.accept(a.taskId());
    }

    private void reverseDeleteEntry(DeleteEntry a) {
        // Reversing a delete = recreate it (+children) right after its old anchor.
        callbacks.reinsertTask.reinsert(a.deletedTask(), a.deletedChildren(), a.anchorAfterTaskId());
    }

    private void reverseEditEntry(EditEntry a) {
        // Reversing an edit = restore the old text.
        callbacks.editTaskText.accept(a.taskId(), a.oldText());
    }

    private void reverseMoveEntry(MoveEntry a) {
        // Reversing a move = swap the two tasks back.
        callbacks.swapTasksById.accept(a.taskIdA(), a.taskIdB());
    }

// ── Register-level reversal handlers ────────────────────────────────

    private void reverseCreateRegister(CreateRegister a) {
        // Reversing a create = delete the register that was created.
        callbacks.deleteRegisterById.accept(a.newRegisterId());
    }

    private void reverseDeleteRegister(DeleteRegister a) {
        // Reversing a delete = restore the full register from its snapshot.
        callbacks.restoreRegister.restore(
                a.oldRegisterId(), a.name(), a.filename(),
                a.oldOrder(), a.wasDefault(), a.fullFileContentJson()
        );
    }

    private void reverseRenameRegister(RenameRegister a) {
        callbacks.renameRegisterById.accept(a.registerId(), a.oldName());
    }

    private void reverseReorderRegister(ReorderRegister a) {
        // Swapping order back is symmetric with the original reorder.
        callbacks.reorderRegistersById.accept(a.registerIdA(), a.registerIdB());
    }

    private void reverseSetDefaultRegister(SetDefaultRegister a) {
        callbacks.setDefaultRegisterById.accept(a.previousDefaultId());
    }

    private void reverseRecognizeRegister(RecognizeRegister a) {
        // Reversing a recognize = un-recognize it again (back to unrecognized).
        callbacks.unrecognizeRegisterById.accept(a.newRegisterId());
    }
}