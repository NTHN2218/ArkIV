package Undo;

/** Restore a fully deleted register from its saved snapshot. */
@FunctionalInterface
public interface RegisterRestorer {
    void restore(int id, String name, String filename, int order, boolean wasDefault, String fileContentJson);
}