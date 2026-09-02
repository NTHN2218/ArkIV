package Undo;

public record EditEntry(long sequence, int registerId, int taskId, String oldText) implements UndoAction { }