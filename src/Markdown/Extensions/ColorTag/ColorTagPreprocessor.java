package Markdown.Extensions.ColorTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites the user-facing |v|...|v| .. |r|...|r| color-tag syntax into the internal
 * ^1...^1 .. ^7...^7 syntax that ColorTagDelimiterProcessor actually understands, before the
 * raw text is ever handed to the CommonMark parser.
 *
 * Why this exists: "|v|" is two real delimiter characters per marker, and CommonMark's own
 * delimiter-stack algorithm pairs individual characters by proximity, not pre-validated
 * multi-char tokens -- registering "|" with it directly risks the two pipes WITHIN one marker
 * pairing with each other, or across two unrelated markers, before the real cross-marker pair
 * is ever tried (confirmed by tracing the actual matching algorithm against this exact syntax).
 * Resolving "|v| ... |v|" into one token ourselves, via a simple left-to-right scan instead of
 * CommonMark's candidate-stack search, sidesteps that ambiguity entirely.
 *
 * "|" is never registered with CommonMark as a delimiter character at all -- only the already
 * fully-tested "^" is. So any stray, unmatched "|" in ordinary text (a table-like aside, a
 * Unix path, whatever) is always completely inert here, same as any other plain character.
 *
 * Deliberately single-line only (no DOTALL): a |v| that never finds its matching |v| on the
 * same line falls straight through as literal, untouched text -- never leaking the internal
 * ^-based syntax into what the user sees. Spanning a color tag across a line break isn't
 * supported; revisit if that turns out to matter in practice.
 */
public final class ColorTagPreprocessor {

    private ColorTagPreprocessor() {}

    // Captures the letter so the closing tag is required to repeat the SAME letter via the
    // \1 backreference -- "|v| ... |i|" (mismatched) simply never matches as a pair.
    private static final Pattern PATTERN = Pattern.compile("\\|([vibgyor])\\|(.*?)\\|\\1\\|");

    /**
     * Returns rawText with every well-formed |v|...|v| .. |r|...|r| span (same line only)
     * rewritten to its internal ^1...^1 .. ^7...^7 equivalent. Does not mutate rawText --
     * callers still hold the original string untouched; this result is only ever fed to the
     * parser, never persisted and never what reopens in the edit dialog.
     */
    public static String preprocess(String rawText) {
        if (rawText == null || rawText.indexOf('|') == -1) {
            return rawText; // fast path -- no pipes at all, nothing to do
        }

        Matcher matcher = PATTERN.matcher(rawText);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(rawText, lastEnd, matcher.start());

            char letter = matcher.group(1).charAt(0);
            String content = matcher.group(2);
            int digit = VibgyorMapping.digitFor(letter);

            result.append('^').append(digit).append(content).append('^').append(digit);

            lastEnd = matcher.end();
        }
        result.append(rawText, lastEnd, rawText.length());

        return result.toString();
    }
}