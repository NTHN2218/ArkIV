package Undo;

public record RenameRegister(long sequence, int registerId, String oldName) implements UndoAction { }