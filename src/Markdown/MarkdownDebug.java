package Markdown;

public class MarkdownDebug {

    // Flip to true only when actively tracing a bug -- keeps day-to-day logs short.
    public static boolean VERBOSE = false;

    private MarkdownDebug() {}

    public static void log(String msg) {
        if (VERBOSE) System.out.println(msg);
    }
}