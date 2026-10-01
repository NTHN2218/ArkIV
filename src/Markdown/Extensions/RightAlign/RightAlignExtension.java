package Markdown.Extensions.RightAlign;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

/**
 * Registers the ::text:: right-align syntax with a CommonMark Parser.Builder.
 * Wire in via MarkdownRenderer's PARSER: .extensions(List.of(..., RightAlignExtension.create()))
 */
public class RightAlignExtension implements Parser.ParserExtension {

    private RightAlignExtension() {}

    public static Extension create() {
        return new RightAlignExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new RightAlignDelimiterProcessor());
    }
}
