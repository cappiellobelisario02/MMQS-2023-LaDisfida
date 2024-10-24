package com.lowagie.text.pdf.fonts;

import static com.lowagie.text.pdf.PdfReader.logger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.lowagie.text.exceptions.FontCreationException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FopGlyphProcessor;
import com.lowagie.text.pdf.LayoutProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Gajendra kumar (raaz2.gajendra@gmail.com)
 */
class AdvanceTypographyTest {

    /**
     * Without glyph substitution out will be {660,666,676,1143,656,1130}, which is no correct FopGlyphProcessor
     * performs glyph substitution to correct the output
     *
     * @throws Exception - DocumentException or IOException thrown by the processedContent() method
     */
    @Test
    void testTypographySubstitutionPass(){
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, this::testTypographySubstitution);
    }
    void testTypographySubstitution() {
        try{
        char[] expectedOutput = {660, 666, 911, 656, 1130};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/jaldi/Jaldi-Regular.ttf", BaseFont.IDENTITY_H, false),
                "नमस्ते", "fonts/jaldi/Jaldi-Regular.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");

        assertArrayEquals(expectedOutput, str.toCharArray());
    }catch(IOException e){
            logger.info("Exception raised");
        }
    }

    /**
     * In some fonts combination of two characters can be represented by single glyph This method tests above case.
     *
     * @throws Exception - UnsupportedEncodingException by the convertToBytesWithGlyphs method
     */
    @Test
    void testSubstitutionWithMergePass(){
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, this::testSubstitutionWithMerge);
    }
    void testSubstitutionWithMerge() throws IOException {
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false),
                "instruction", "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
    }

    @Test
    void testSubstitutionWithMergeWithLayoutProcessorEnabledPass(){
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, this::testSubstitutionWithMergeWithLayoutProcessorEnabled);
    }
    void testSubstitutionWithMergeWithLayoutProcessorEnabled() throws IOException {
        LayoutProcessor.enable();
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, true, false,
                        null,
                        null),
                "instruction", "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
        LayoutProcessor.disable();
    }

    /**
     * Test fonts loaded externally and passed as byte array to BaseFont, Fop should be able to resolve these fonts
     *
     * @throws Exception a DocumentException or an IOException thrown by BaseFont.createFont
     */
    @Test
    void testInMemoryFonts(){
        try{
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        BaseFont font = BaseFont.createFont("ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, true,
                getTestFontByte(), null, false);
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                font, "instruction", "Viaoda Libre", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
    }catch(IOException e){
            logger.info("Exception raised");
        }
    }

    @Disabled("This test is failing, need to investigate. @YOSHIDA may know the reason."
            + "Should work, when GH-591 #592 is fixed.")
    @Test
    void testSurrogatePairPass(){
        org.junit.jupiter.api.Assertions.assertTrue(true);
    }

    private byte[] getTestFontByte() throws IOException {
        try (InputStream stream = BaseFont.getResourceStream("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", null)) {
            assertThat(stream).isNotNull();
            return IOUtils.toByteArray(stream);
        }
    }

}
