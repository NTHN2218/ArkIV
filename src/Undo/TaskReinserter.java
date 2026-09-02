package Undo;

import java.util.List;

/** Reinsert a deleted task (and its children, if any) after a given anchor task id. */
@FunctionalInterface
public interface TaskReinserter {
    void reinsert(TaskSnapshot task, List<TaskSnapshot> children, int anchorAfterTaskId);
}