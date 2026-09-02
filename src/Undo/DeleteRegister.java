package Undo;

public record DeleteRegister(long sequence, int registerId,
                             int oldRegisterId, String name, String filename,
                             int oldOrder, boolean wasDefault,
                             String fullFileContentJson) implements UndoAction { }