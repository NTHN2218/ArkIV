package Markdown.Extensions.RightAlign;

import Markdown.Extensions.ColorTag.ColorTagExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;

import java.util.List;

/**
 * SCRATCH -- Phase 1 verification only. Run main(), compare each printed tree to the EXPECT line.
 * Delete once Phase 1 is signed off.
 */
public class RightAlignAstTest {

    private static final Parser PARSER = Parser.builder()
            .extensions(List.of(TaskListItemsExtension.create(), ColorTagExtension.create(), RightAlignExtension.create()))
            .build();

    public static void main(String[] args) {
        run("1. basic, left text",            "Buy milk ::due Fri::",          "Text, RightAlign[Text]");
        run("2. whole line",                  "::due Fri::",                   "RightAlign[Text]");
        run("3. text after closer",           "::a:: more",                    "all literal Text, no RightAlign");
        run("4. two spans one line",          "::a:: ::b::",                   "first literal, second RightAlign (VERIFY)");
        run("5. nested bold",                 "x ::**bold**::",                "RightAlign[StrongEmphasis]");
        run("6. nested color",                "x ::^1hi^1::",                  "RightAlign[ColorSpan]");
        run("7. inside bold",                 "**::x::**",                     "literal (documented limitation)");
        run("8. intraword",                   "std::vec::",                    "Text, RightAlign[Text] (accepted false positive)");
        run("9. escaped",                     "\\::a\\::",                     "literal, no RightAlign");
        run("10. triple colon",               ":::a:::",                       "literal, no RightAlign");
        run("11. soft break, 2 lines",        "L1 ::x::\nL2 ::y::",            "RightAlign on both lines");
        run("12. hard break",                 "L1 ::x::  \nL2",                "RightAlign, HardLineBreak, Text");
        run("13. whitespace inside",          ":: x ::",                       "literal (not flanking)");
        run("14. unclosed",                   "::abc",                         "literal");
        run("15. in list item",               "- item ::r::",                  "BulletList > ListItem > Paragraph > RightAlign");
    }

    private static void run(String label, String input, String expect) {
        System.out.println("── " + label + "  |  input: " + input.replace("\n", "\\n"));
        System.out.println("   EXPECT: " + expect);
        print(PARSER.parse(input), 1);
        System.out.println();
    }

    private static void print(Node node, int depth) {
        StringBuilder sb = new StringBuilder("   ");
        sb.append("  ".repeat(depth)).append(node.getClass().getSimpleName());
        if (node instanceof Text t) sb.append(" \"").append(t.getLiteral().replace("\n", "\\n")).append("\"");
        System.out.println(sb);
        for (Node c = node.getFirstChild(); c != null; c = c.getNext()) print(c, depth + 1);
    }
}
