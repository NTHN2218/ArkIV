package Markdown.Extensions.ColorTag;

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

/**
 * Registers the ^1..^7 VIBGYOR color-tag syntax with a CommonMark Parser.Builder.
 * Wire in via MarkdownRenderer's PARSER: .extensions(List.of(..., ColorTagExtension.create()))
 */
public class ColorTagExtension implements Parser.ParserExtension {

    private ColorTagExtension() {}

    public static Extension create() {
        return new ColorTagExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new ColorTagDelimiterProcessor());
    }
}