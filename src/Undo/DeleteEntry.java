package Undo;

import java.util.List;

public record DeleteEntry(long sequence, int registerId,
                          TaskSnapshot deletedTask,
                          List<TaskSnapshot> deletedChildren,
                          int anchorAfterTaskId) implements UndoAction { }