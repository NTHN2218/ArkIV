package Markdown.Extensions.RightAlign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites the user-facing ">> text >>" right-align syntax into the internal "::text::" syntax
 * that RightAlignDelimiterProcessor understands, before the raw text reaches the CommonMark parser.
 *
 * Two deliberate differences from a naive ">>" -> "::" swap:
 *  - Inner whitespace is trimmed (">> text >>" -> "::text::"). The delimiter processor relies on
 *    CommonMark flanking rules, so ":: text ::" (spaces inside) would never match and would show
 *    up as literal colons.
 *  - Only rewritten when the closing ">>" is the last thing on its line (trailing spaces allowed),
 *    mirroring the processor's own end-of-line rule. Anything that wouldn't survive the processor
 *    is left untouched, so the internal "::" form never leaks into what the user sees.
 *
 * Single line only. ">>>" runs are ignored. Content that starts or ends with ':' is skipped
 * (it would merge with the "::" markers into a ":::" run, which the processor rejects).
 *
 * Does not mutate its input -- the result is only ever fed to the parser.
 */
public final class RightAlignPreprocessor {

    private RightAlignPreprocessor() {}

    private static final Pattern PATTERN = Pattern.compile(
            "(?<!>)>>(?!>)[ \\t]*(\\S(?:.*?\\S)?)[ \\t]*(?<!>)>>[ \\t]*$",
            Pattern.MULTILINE);

    public static String preprocess(String rawText) {
        if (rawText == null || rawText.indexOf(">>") == -1) {
            return rawText; // fast path
        }

        Matcher matcher = PATTERN.matcher(rawText);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            result.append(rawText, lastEnd, matcher.start());

            String content = matcher.group(1);
            if (content.charAt(0) == ':' || content.charAt(content.length() - 1) == ':') {
                result.append(matcher.group()); // would form ":::" -- leave as typed
            } else {
                result.append("::").append(content).append("::");
                // keep any trailing spaces the user left after the closer
                String whole = matcher.group();
                int trailing = 0;
                while (trailing < whole.length()
                        && (whole.charAt(whole.length() - 1 - trailing) == ' '
                        || whole.charAt(whole.length() - 1 - trailing) == '\t')) {
                    trailing++;
                }
                result.append(whole, whole.length() - trailing, whole.length());
            }
            lastEnd = matcher.end();
        }
        result.append(rawText, lastEnd, rawText.length());

        return result.toString();
    }
}