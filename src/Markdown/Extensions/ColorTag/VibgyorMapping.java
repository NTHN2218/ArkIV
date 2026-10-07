package Markdown.Extensions.ColorTag;

/**
 * Single source of truth for the VIBGYOR letter <-> digit mapping, shared between
 * ColorTagPreprocessor (letter -> digit, converts the user-facing |v|...|v| syntax into the
 * internal ^1...^1 syntax before parsing) and ColorTagDelimiterProcessor (digit -> letter,
 * converts back for display once a ColorSpan has actually matched).
 *
 * Package-private -- nothing outside Markdown.Extensions.ColorTag needs this mapping.
 */
final class VibgyorMapping {

    private VibgyorMapping() {}

    static int digitFor(char letter) {
        return switch (letter) {
            case 'v' -> 1; // violet
            case 'i' -> 2; // indigo
            case 'b' -> 3; // blue
            case 'g' -> 4; // green
            case 'y' -> 5; // yellow
            case 'o' -> 6; // orange
            case 'r' -> 7; // red
            default -> 0; // unreachable -- callers only pass letters already matched by the regex
        };
    }

    static char letterFor(int digit) {
        return switch (digit) {
            case 1 -> 'v';
            case 2 -> 'i';
            case 3 -> 'b';
            case 4 -> 'g';
            case 5 -> 'y';
            case 6 -> 'o';
            case 7 -> 'r';
            default -> '?'; // unreachable -- digit always comes from our own validated 1-7 range
        };
    }
}