package Undo;

public record ReorderRegister(long sequence, int registerId, int registerIdA, int registerIdB) implements UndoAction { }