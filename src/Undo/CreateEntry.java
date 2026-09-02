package Undo;

public record CreateEntry(long sequence, int registerId, int taskId) implements UndoAction { }