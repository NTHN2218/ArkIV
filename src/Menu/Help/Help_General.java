package Menu.Help;

import Menu.Help.Help_Markdown.Group;
import Menu.Help.Help_Markdown.Kind;
import Menu.Help.Help_Markdown.Row;
import Menu.Help.Help_Markdown.Table;

import java.util.List;

/**
 * Help content for the General Use tab: topic -> explanation, grouped.
 * Plain text only -- keep key names in sync with HelpKeybinds.
 */
public final class Help_General {

    private Help_General() {}

    private static Row row(String... cells) { return new Row(cells); }

    public static final Table GLOSSARY = new Table(
            new String[]{"Term", "Menaing"},
            new Kind[]{Kind.TEXT, Kind.CODE},
            new double[]{0.20, 0.80},
            List.of(
                    new Group("",
                            row("Register",
                                    "- 1st level of hierarchy in App\n"+
                                    "- It is saved as its own file"
                            ),
                            row("Entry",
                                    "- 2nd level of hierarchy in App\n"+
                                    "- Highest level of hierarchy in a Register"
                            ),
                            row("sub-Entry",
                                    "- 3rd level of hierarchy in App\n"+
                                    "- Lowest level of hierarchy in a Register"
                            ),
                            row("task-item",
                                    "- Collective term for Entry and sub-Entry"
                            ),

                            row("","")
                    )

            )

    );

    public static final Table TABLE = new Table(
            new String[]{"Topic", "Details"},
            new Kind[]{Kind.TEXT, Kind.TEXT},
            new double[]{0.20, 0.80},
            List.of(
                    new Group("Getting started",
                            row("What is ArkIV",
                                    "A personal organizer. Notes and to-dos are Entries, optionally with sub-Entries under them, " +
                                            "kept in Registers (separate lists)."),
                            row("Add an Entry",
                                    "Press Ctrl+N (or File > New Entry), type, and press Enter. Shift+Enter adds a new line."),
                            row("Add a sub-Entry",
                                    "Click the + button on an Entry, or select the Entry and press Ctrl+N. "
                                            + "Sub-Entries sit under their Entry, one level deep."),
                            row("Select a task-item",
                                    "Double-click it or tick its checkbox. The border blinks while it is selected. "
                                            + "The checkbox only selects; it does not mark the Entry as done."),
                            row("Edit, delete, move",
                                    "With an Entry selected: Ctrl+E edits, Ctrl+D deletes (its sub-Entries go too), "
                                            + "Ctrl+Up / Ctrl+Down moves it. Esc deselects."),
                            row("Collapse / expand",
                                    "Right-click an Entry to hide or show its sub-Entries. With nothing selected, "
                                            + "Ctrl+Up collapses all and Ctrl+Down expands all."),
                            row("Find",
                                    "Press Ctrl+F, type, and press Enter. Enter again jumps to the next match. "
                                            + "Matches inside collapsed Entries open automatically."),
                            row("Formatting",
                                    "Entries support Markdown, color tags and hot strings. See the Mark Down and Hot Strings tabs.")
                    ),

                    new Group("Registers",
                            row("What they are",
                                    "Each register is its own list, saved as its own file. Use them to keep areas of your life or work apart."),
                            row("Create one",
                                    "Press Ctrl+Shift+N, or right-click the Registers heading in the sidebar, then type a name."),
                            row("Switch",
                                    "Click a register in the sidebar, or press Ctrl+Tab / Ctrl+Shift+Tab to cycle. "
                                            + "ArkIV reopens the register you used last."),
                            row("Manage",
                                    "Right-click a register to rename it, reorder it, copy its file path, show it in the file explorer, "
                                            + "or remove it. The last remaining register cannot be removed."),
                            row("Unrecognized registers",
                                    "Data files ArkIV doesn't know about show up under Unrecognized Registers (read-only). "
                                            + "Right-click one to recognize it as a register, or delete it.")
                    ),

                    new Group("Ways to use it",
                            row("One register per area",
                                    "For example Work, Home, Ideas, Study. Flip between them with Ctrl+Tab."),
                            row("Topic and steps",
                                    "Make each project or topic an Entry and put its tasks or notes underneath as sub-Entries. "
                                            + "Collapse it when you aren't working on it."),
                            row("Quick to-dos",
                                    "Type -[ then Space for a checklist item. When it's done, change [ ] to [x] and it greys out."),
                            row("Color with meaning",
                                    "Pick a purpose per color and stick to it, e.g. red for urgent, green for done. "
                                            + "Select text in the edit box and press Ctrl+Alt+1 to 7."),
                            row("Deadlines",
                                    "Right-align pushes a due date to the end of the line. Select it and press Ctrl+Alt+R."),
                            row("Finished Entries",
                                    "Collapse them, or move them to the bottom with Ctrl+Down.")
                    ),

                    new Group("Good to know",
                            row("Backup",
                                    "Each register is a file in ArkIV's assets folder. Use git and GitHub or similar software to Backup your data."),
                            row("Limits",
                                    "Up to 999 Entries per register and 999 sub-Entries per Entry."),
                            row("Deleting",
                                    "Deleting is permanent and takes the Entry's sub-Entries with it. A confirmation appears first."),
                            row("Help",
                                    "Press F1 any time to reopen this window.")
                    )
            ));
}