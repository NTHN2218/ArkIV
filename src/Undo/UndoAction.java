package Undo;

import java.util.List;

/**
 * Sealed interface representing a single reversible action.
 * Every record IS a precomputed reversal instruction, not a description
 * of the forward action — undo() executes it literally, no interpretation.
 *
 * sealed + permits forces every switch over UndoAction to handle all
 * cases at compile time -- a new action type added later without a
 * matching case anywhere will fail to compile, not fail silently.
 */
public sealed interface UndoAction
        permits CreateEntry, DeleteEntry, EditEntry, MoveEntry,
        CreateRegister, DeleteRegister, RenameRegister,
        ReorderRegister, SetDefaultRegister, RecognizeRegister,
        CollapseExpandAll {

    long sequence();
    int registerId();
}

// ── Entry-level actions ─────────────────────────────────────────────

/*
 *  Reversal of create: delete the task by id.
 */
record CreateEntry(long sequence, int registerId, int taskId) implements UndoAction { }

/*
 * Reversal of delete: recreate the task (+children if any) after anchorAfterTaskId.
 */
record DeleteEntry(long sequence, int registerId,
                   TaskSnapshot deletedTask,
                   List<TaskSnapshot> deletedChildren,
                   int anchorAfterTaskId) implements UndoAction { }

/*
 *  Reversal of an edit: restore the old text.
 */
record EditEntry(long sequence, int registerId, int taskId, String oldText) implements UndoAction { }

/*
 *  Reversal of a move: swap the two task ids back.
 */
record MoveEntry(long sequence, int registerId, int taskIdA, int taskIdB) implements UndoAction { }

/*
 * Snapshot of one main entry's collapsed state before a bulk collapse/expand-all action.
 */
record CollapseState(int taskId, boolean wasCollapsed) { }

/*
 * Reversal of a Collapse All / Expand All: restore every main entry's
 * previous isCollapsed state exactly as it was before the bulk action.
 * Same record shape handles both directions -- undo just replays whatever
 * state each entry was in beforehand.
 */
record CollapseExpandAll(long sequence, int registerId,
                         List<CollapseState> previousStates) implements UndoAction { }

// ── Register-level actions ──────────────────────────────────────────

/*
 * Reversal of a register create: delete the register by id.
 */
record CreateRegister(long sequence, int registerId, int newRegisterId) implements UndoAction { }

/*
 *  Reversal of a register delete: restore the register's full file content + header info.
 */
record DeleteRegister(long sequence, int registerId,
                      int oldRegisterId, String name, String filename,
                      int oldOrder, boolean wasDefault,
                      String fullFileContentJson) implements UndoAction { }

/*
 * Reversal of a register rename: restore the old name.
 */
record RenameRegister(long sequence, int registerId, String oldName) implements UndoAction { }

/*
 * Reversal of a reorder: swap the two register ids' order back.
 */
record ReorderRegister(long sequence, int registerId, int registerIdA, int registerIdB) implements UndoAction { }

/*
 * Reversal of set-default: restore the previous default register id.
 */
record SetDefaultRegister(long sequence, int registerId, int previousDefaultId) implements UndoAction { }

/*
 * Reversal of a recognize: un-recognize it again (remove from header).
 */
record RecognizeRegister(long sequence, int registerId, int newRegisterId) implements UndoAction { }