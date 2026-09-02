package Undo;

import java.util.List;
import java.util.function.*;

/**
 * Bundle of small execution primitives handed to ActionUndoManager from
 * ArkIVv9's constructor -- same callback pattern already used for
 * EditMenu/FileMenu. ActionUndoManager never reaches into ArkIVv9's
 * private state directly; it only ever calls what's handed to it here.
 */
public class UndoCallbacks {

    // Entry-level primitives
    public final TaskReinserter reinsertTask;
    public final Consumer<Integer> deleteTaskById;
    public final BiConsumer<Integer, String> editTaskText;
    public final BiConsumer<Integer, Integer> swapTasksById;

    // Register-level primitives
    public final Consumer<Integer> deleteRegisterById;
    public final RegisterRestorer restoreRegister;
    public final BiConsumer<Integer, String> renameRegisterById;
    public final BiConsumer<Integer, Integer> reorderRegistersById;
    public final Consumer<Integer> setDefaultRegisterById;
    public final Consumer<Integer> unrecognizeRegisterById;

    // UX primitives
    public final Consumer<String> showToast;
    public final Consumer<Integer> expandIfCollapsed;
    public final Consumer<Integer> scrollToTask;
    public final Runnable refreshRegisterSidebar;

    public UndoCallbacks(
            TaskReinserter reinsertTask,
            Consumer<Integer> deleteTaskById,
            BiConsumer<Integer, String> editTaskText,
            BiConsumer<Integer, Integer> swapTasksById,
            Consumer<Integer> deleteRegisterById,
            RegisterRestorer restoreRegister,
            BiConsumer<Integer, String> renameRegisterById,
            BiConsumer<Integer, Integer> reorderRegistersById,
            Consumer<Integer> setDefaultRegisterById,
            Consumer<Integer> unrecognizeRegisterById,
            Consumer<String> showToast,
            Consumer<Integer> expandIfCollapsed,
            Consumer<Integer> scrollToTask,
            Runnable refreshRegisterSidebar
    ) {
        this.reinsertTask = reinsertTask;
        this.deleteTaskById = deleteTaskById;
        this.editTaskText = editTaskText;
        this.swapTasksById = swapTasksById;
        this.deleteRegisterById = deleteRegisterById;
        this.restoreRegister = restoreRegister;
        this.renameRegisterById = renameRegisterById;
        this.reorderRegistersById = reorderRegistersById;
        this.setDefaultRegisterById = setDefaultRegisterById;
        this.unrecognizeRegisterById = unrecognizeRegisterById;
        this.showToast = showToast;
        this.expandIfCollapsed = expandIfCollapsed;
        this.scrollToTask = scrollToTask;
        this.refreshRegisterSidebar = refreshRegisterSidebar;
    }
}