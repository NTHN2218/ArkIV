package Undo;

public record MoveEntry(long sequence, int registerId, int taskIdA, int taskIdB) implements UndoAction { }