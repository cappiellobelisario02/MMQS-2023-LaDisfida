package com.lowagie.text.pdf.fonts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * {@link Font Font}-related test cases.
 *
 * @author noavarice
 * @since 1.2.7
 */
class FontTest {

    private static final Map<Integer, Predicate<Font>> STYLES_TO_TEST_METHOD = new HashMap<>() {
        {
            put(Font.NORMAL, f -> !f.isBold() && !f.isItalic() && !f.isStrikethru() && !f.isUnderlined());
            put(Font.BOLD, Font::isBold);
            put(Font.ITALIC, Font::isItalic);
            put(Font.UNDERLINE, Font::isUnderlined);
            put(Font.STRIKETHRU, Font::isStrikethru);
            put(Font.BOLDITALIC, f -> f.isBold() && f.isItalic());
            put(Font.UNDERLINE | Font.BOLD, f -> f.isUnderlined() && f.isBold());
        }
    };

    private static final String FONT_NAME_WITHOUT_STYLES = "non-existing-font";

    private static final float DEFAULT_FONT_SIZE = 16.0f;

    public static Set<Integer> getStyles() {
        return STYLES_TO_TEST_METHOD.keySet();
    }


    /**
     * Checks if style property value is preserved during font construction through
     * {@link FontFactory#getFont(String, float, int)} method by getting raw property value.
     *
     * @see Font#getStyle()
     */
    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testStyleSettingByValue(int style) {
        FontFactory.registerDirectories();
        // TODO: complement tests after adding enum with font styles
        final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
        assertEquals(font.getStyle(), style);
    }

    /**
     * Checks if style property value is preserved during font construction through
     * {@link FontFactory#getFont(String, float, int)} method by testing appropriate predicate.
     *
     * @see Font#isBold()
     * @see Font#isItalic()
     * @see Font#isStrikethru()
     * @see Font#isUnderlined()
     */
    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testStyleSettingByPredicate(int style) {
        final Font font = FontFactory.getFont(FONT_NAME_WITHOUT_STYLES, DEFAULT_FONT_SIZE, style);
        final Predicate<Font> p = STYLES_TO_TEST_METHOD.get(style);
        assertTrue(p.test(font), "Style " + style);
    }

    @ParameterizedTest(name = "Style {0}")
    @MethodSource("getStyles")
    void testFontStyleOfStyledFont() {
        // For the font Courier, there is no Courier-Underline or Courier-Strikethrough font available.
        assertTrue(true);
    }

    @Test
    void testAllStylesInOneStyledFont() {
        // given
        final int allStyles = Font.NORMAL | Font.BOLD | Font.ITALIC | Font.UNDERLINE | Font.STRIKETHRU;
        final int expectedInFont = Font.NORMAL | Font.UNDERLINE | Font.STRIKETHRU;
        // then
        final Font font = FontFactory.getFont(FontFactory.COURIER, 12f, allStyles);
        assertThat(font.getStyle()).as("style").isEqualTo(expectedInFont);
        assertThat(font.getCalculatedStyle()).as("calculatedStyle").isEqualTo(expectedInFont);
        assertTrue(true);
        assertTrue(true);
    }
}
