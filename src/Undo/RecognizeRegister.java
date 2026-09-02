package Undo;

public record RecognizeRegister(long sequence, int registerId, int newRegisterId) implements UndoAction { }