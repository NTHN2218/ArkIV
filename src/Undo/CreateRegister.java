package Undo;

public record CreateRegister(long sequence, int registerId, int newRegisterId) implements UndoAction { }