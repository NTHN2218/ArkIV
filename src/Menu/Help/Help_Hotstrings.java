package Menu.Help;

import Menu.Help.Help_Markdown.Group;
import Menu.Help.Help_Markdown.Kind;
import Menu.Help.Help_Markdown.Row;
import Menu.Help.Help_Markdown.Table;

import java.util.List;

/**
 * Help content for the Hot Strings tab. Mirrors the rules in AutoHotkey.Hotstring --
 * update both when a rule is added.
 */
public final class Help_Hotstrings {

    private Help_Hotstrings() {}

    private static Row row(String... cells) { return new Row(cells); }

    public static final Table TABLE = new Table(
            new String[]{"Type", "Trigger", "Result"},
            new Kind[]{Kind.TEXT, Kind.CODE, Kind.TEXT},
            new double[]{0.40, 0.30, 0.30},
            List.of(
                    new Group("",
                            row("Check-list",  "-[",   "- [ ]"),
                            row("Right arrow", "-->",  "—⟶"),
                            row("Left arrow",  "<--",  "⟵—"),
                            row("Not Equal",   "=/= ", "≠"))
            ));
}