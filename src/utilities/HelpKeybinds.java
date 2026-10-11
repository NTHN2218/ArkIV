package utilities;

import utilities.HelpContent.Group;
import utilities.HelpContent.Kind;
import utilities.HelpContent.Row;
import utilities.HelpContent.Table;

import java.util.List;

/**
 * Help content for the Key Binds tab. Two tables:
 *  - BY_SELECTION: keys whose meaning depends on whether an Entry is selected (3 columns)
 *  - MAIN: everything else, grouped by where it works (2 columns)
 * Data only -- layout is HelpTable's job.
 */
public final class HelpKeybinds {

    private HelpKeybinds() {}

    private static Row row(String... cells) { return new Row(cells); }


    public static final Table MAIN = new Table(
            new String[]{"Action", "Key-Bind"},
            new Kind[]{Kind.CODE, Kind.TEXT},
            new double[]{0.40, 0.60},
            List.of(
                    new Group("Mouse Actions",
                            row("Select/Deselect task-item", "Double-click on task-item"),
                            row("Collapse/Expand Entry",     "Right-click on Entry"),
                            row("Autoscroll",                "Middle-click")
                    ),

                    new Group("Global",
                            row("Help", "F1"),
                            row("Find", "Ctrl + F"),
                            row("Next Tab", "Ctrl + Tab"),
                            row("Prev Tab", "Ctrl + Shift + Tab"),
                            row("New Entry","Ctrl + N"),
                            row("New register", "Ctrl + Shift + N"),
                            row("Collapse all", "Ctrl + Up"),
                            row("Expand all", "Ctrl + Down"),
                            row("Scroll to top", "Ctrl + Shift + Up"),
                            row("Scroll to bottom", "Ctrl + Shift + Down")
                    ),

                    new Group("Dialogs",
                            row("Enter", "Submit (Create / Save)"),
                            row("Shift+Enter", "New line in the text box"),
                            row("Ctrl+W", "Close the open dialog")
                    ),

                    new Group("task-item selected",
                            row("Create sub-Entry",    "Ctrl + N"),
                            row("Edit task-item",      "Ctrl + E"),
                            row("Delete task-item",    "Ctrl + D"),
                            row("Move task-item Up",   "Ctrl + Up"),
                            row("Move task-item Down", "Ctrl + Down"),
                            row("Deselect", "Esc")
                    ),

                    new Group("Editing a task-item - Caret Control",
                            row("Jump to start of word", "Alt + Left"),
                            row("Jump to end of word",   "Alt + Right"),
                            row("Jump to start of line", "Ctrl + Alt + Left"),
                            row("Jump to end of line",   "Ctrl + Alt + Right"),
                            row("",""),
                            row("Select to start of word", "Shift + Alt + Left"),
                            row("Select to end of word",   "Shift + Alt + Right"),
                            row("Select to start of line", "Ctrl + Shift + Alt + Left"),
                            row("Select to end of line",   "Ctrl + Shift + Alt + Right")

                    ),

                    new Group("Editing a task-item - Mark Down Wrapper",
                            row("Bold",               "Ctrl + Alt + B"),
                            row("Italic",             "Ctrl + Alt + I"),
                            row("Inline Code",        "Ctrl + Alt + `"),
                            row("Right Align",        "Ctrl + Alt + R"),
                            row("Color Tag - violet", "Ctrl + Alt + 1"),
                            row("Color Tag - indigo", "Ctrl + Alt + 2"),
                            row("Color Tag - blue",   "Ctrl + Alt + 3"),
                            row("Color Tag - green",  "Ctrl + Alt + 4"),
                            row("Color Tag - yellow", "Ctrl + Alt + 5"),
                            row("Color Tag - orange", "Ctrl + Alt + 6"),
                            row("Color Tag - red",    "Ctrl + Alt + 7")

                    )

            ));
}