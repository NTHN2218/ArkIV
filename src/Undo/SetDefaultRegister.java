package Undo;

public record SetDefaultRegister(long sequence, int registerId, int previousDefaultId) implements UndoAction { }