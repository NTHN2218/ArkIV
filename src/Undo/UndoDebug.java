package Undo;

/**
 * Two-boolean debug gate for the Undo package, mirroring Markdown/MarkdownDebug.
 * ACTIVE = master on/off switch. VERBOSE = extra detail when ACTIVE is true.
 * Flip both to false before final pre-commit cleanup (comment calls out, don't delete).
 */
public class UndoDebug {

    public static boolean ACTIVE = true;
    public static boolean VERBOSE = true;

    public static void summary(String message) {
        if (ACTIVE) {
            System.out.println("[Undo] " + message);
        }
    }

    public static void verbose(String message) {
        if (ACTIVE && VERBOSE) {
            System.out.println("[Undo][verbose] " + message);
        }
    }
}