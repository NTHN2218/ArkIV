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
}