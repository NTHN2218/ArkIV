package Undo;

/**
 * Immutable snapshot of a single task's data at the moment it was deleted.
 * Used inside DeleteEntry to allow exact reconstruction on undo.
 */
public record TaskSnapshot(
        int id,
        int parentId,
        String text,
        boolean isDone,
        boolean isSub,
        boolean isCollapsed
) { }