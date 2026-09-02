package Undo;

public sealed interface UndoAction
        permits CreateEntry, DeleteEntry, EditEntry, MoveEntry,
        CreateRegister, DeleteRegister, RenameRegister,
        ReorderRegister, SetDefaultRegister, RecognizeRegister {

    long sequence();
    int registerId();
}