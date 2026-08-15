package Markdown;

import utilities.UniversalThemes;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.Color;

/**
 * Central lookup for markdown element -> AttributeSet, sourced from UniversalThemes.
 * Static-utility class -- never instantiated.
 *
 * Design: base-attribute getters (getPlainAttributes, getHeadingAttributes) return a
 * fresh SimpleAttributeSet each call. Toggle helpers (applyBold, applyItalic) MUTATE a
 * given set in place, so the Visitor (Phase 3) can layer them on top of a base -- this
 * is what lets "bold inside a heading" work without a new combination for every pairing.
 *
 * New elements in later tiers (strikethrough, highlight, etc.) are added as NEW methods
 * here -- existing methods are never touched.
 */
public class MarkdownStyles {

    private MarkdownStyles() {
        // static-utility class -- never instantiated
    }

    // Family pulled from an already-loaded theme font, so we never hardcode a font name.
    private static final String FONT_FAMILY = UniversalThemes.FONT_R_14.getFamily();

    // Matches the body text size already used for entry content (see TaskItem / inputArea).
    private static final int BASE_FONT_SIZE = 17;

    // add alongside BASE_FONT_SIZE / HEADING_SIZES
    private static final int MARKER_FONT_SIZE = 1; // deliberately small
    // smudge rather than cleanly vanishing, varies by OS/font renderer

    // Heading sizes, capped within the same 10-20pt range UniversalThemes already defines.
    private static final int[] HEADING_SIZES = {
            0,  // unused index
            27, // level 1
            22, // level 2  =  17 + 6
            19, // level 3  =  17 + 3
            15, // level 4
            14, // level 5
            13  // level 6
    };

    // add near the top, alongside FONT_FAMILY/BASE_FONT_SIZE
    private static final Color HEADING_ACCENT = deriveHeadingColor(UniversalThemes.ACCENT_COLOR);

    private static Color deriveHeadingColor(Color base) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        // same hue, slightly desaturated, noticeably lighter -- distinct from raw ACCENT_COLOR
        // used elsewhere for buttons/selection/tree-accent-bar
        float hue = hsb[0];
        float saturation = Math.max(0f, hsb[1] * 0.75f);
        float brightness = Math.min(1f, hsb[2] * 1.25f);
        Color derived = Color.getHSBColor(hue, saturation, brightness);

        return derived;
    }

    // ── Base attribute sets ─────────────────────────────────────────────

    public static SimpleAttributeSet getPlainAttributes() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, BASE_FONT_SIZE);
        StyleConstants.setBold(attrs, false);
        StyleConstants.setItalic(attrs, false);
        StyleConstants.setForeground(attrs, UniversalThemes.TXT_PRIMARY);

        return attrs;
    }

    public static SimpleAttributeSet getHeadingAttributes(int level) {
        int clampedLevel = Math.max(1, Math.min(level, 6));
        int size = HEADING_SIZES[clampedLevel];

        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, size);
        StyleConstants.setBold(attrs, true);

        int lvl1 = HEADING_SIZES[1];
        Color headingColor = (size==lvl1) ? UniversalThemes.MD_COLOR_HEADING : UniversalThemes.TXT_PRIMARY;
        StyleConstants.setForeground(attrs, headingColor);

        return attrs;
    }

    // ── Toggle helpers (mutate a given set -- used for merging onto a base context) ────

    public static void applyBold(MutableAttributeSet attrs) {
        StyleConstants.setBold(attrs, true);
        StyleConstants.setForeground(attrs, UniversalThemes.MD_COLOR_BOLD);

    }

    public static void applyItalic(MutableAttributeSet attrs) {
        StyleConstants.setItalic(attrs, true);
    }

    // ── Utility: safe mutable copy of any attribute set (used for the Visitor's push/pop stack) ──

    public static SimpleAttributeSet copyOf(javax.swing.text.AttributeSet base) {
        SimpleAttributeSet copy = new SimpleAttributeSet(base);
        return copy;
    }

    // ── Checkbox attributes (for tasklist items: - [ ] / - [x]) ──────────

    public static SimpleAttributeSet getCheckboxBracketAttributes() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, BASE_FONT_SIZE);
        StyleConstants.setBold(attrs, false);
        StyleConstants.setItalic(attrs, false);
        StyleConstants.setForeground(attrs, UniversalThemes.TXT_SECONDARY);

        return attrs;
    }

    public static SimpleAttributeSet getCheckboxCheckedMarkAttributes() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, BASE_FONT_SIZE);
        StyleConstants.setBold(attrs, true);
        StyleConstants.setItalic(attrs, false);
        StyleConstants.setForeground(attrs, UniversalThemes.ACCENT_COLOR);

        return attrs;
    }

    public static SimpleAttributeSet getCheckedTaskTextAttributes() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, BASE_FONT_SIZE);
        StyleConstants.setBold(attrs, false);
        StyleConstants.setItalic(attrs, false);
        StyleConstants.setForeground(attrs, UniversalThemes.DISABLED_TEXT);

        return attrs;
    }

    // ── Muted attributes (for de-emphasized syntax markers: **, *, #, etc.) ──

    public static SimpleAttributeSet getMutedAttributes() {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setFontSize(attrs, MARKER_FONT_SIZE);
        StyleConstants.setBold(attrs, false);
        StyleConstants.setItalic(attrs, false);
        StyleConstants.setForeground(attrs, UniversalThemes.DIMMED_TEXT);

        return attrs;
    }
}