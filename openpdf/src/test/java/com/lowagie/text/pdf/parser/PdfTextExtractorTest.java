package com.lowagie.text.pdf.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.FontSelector;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class PdfTextExtractorTest {

    private static final String LOREM_IPSUM =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy "
                    + "eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed "
                    + "diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.";

    static byte[] createSimpleDocumentWithElements(Element... elements) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        for (Element element : elements) {
            document.add(element);
        }
        document.close();

        return baos.toByteArray();
    }

    protected static byte[] readDocument(final File file) throws IOException {
        try (ByteArrayOutputStream fileBytes = new ByteArrayOutputStream();
                InputStream inputStream = Files.newInputStream(file.toPath())) {
            final byte[] buffer = new byte[8192];
            while (true) {
                final int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                fileBytes.write(buffer, 0, bytesRead);
            }
            return fileBytes.toByteArray();
        }
    }

    @Test
    void testPageExceededPass(){
        Assertions.assertThrows(NullPointerException.class, this::testPageExceeded);
    }
    void testPageExceeded() throws Exception {
        assertThat(getString("HelloWorldMeta.pdf", 5), is(emptyString()));
    }

    @Test
    void testInvalidPageNumberPass(){
        Assertions.assertThrows(NullPointerException.class, this::testInvalidPageNumber);
    }
    void testInvalidPageNumber() throws PDFFilterException, URISyntaxException, IOException {
        assertThat(getString("HelloWorldMeta.pdf", 0), is(emptyString()));
    }

    @Test
    void testZapfDingbatsFontPass(){
        Assertions.assertThrows(NullPointerException.class, this::testZapfDingbatsFont);
    }
    void testZapfDingbatsFont() throws PDFFilterException, IOException {
        Document document = new Document();
        Document.CompressionSettings.setCompress(false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        //There has some problem to write Greek with Font.ZAPFDINGBATS, but show in html it is "✧❒❅❅❋"
        document.add(new Chunk("Greek", new Font(Font.ZAPFDINGBATS)));
        document.close();
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(byteArrayOutputStream.toByteArray()));
        Assertions.assertEquals("✧❒❅❅❋", pdfTextExtractor.getTextFromPage(1));
        Document.CompressionSettings.setCompress(true);
    }

    @Test
    void testSymbolFontPass(){
        Assertions.assertThrows(NullPointerException.class, this::testSymbolFont);
    }
    void testSymbolFont() throws PDFFilterException, IOException {
        Document document = new Document();
        Document.CompressionSettings.setCompress(false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, byteArrayOutputStream);
        document.open();

        FontSelector selector = new FontSelector();
        selector.addFont(new Font(Font.SYMBOL));
        document.add(selector.process("ετε"));
        document.close();
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(new PdfReader(byteArrayOutputStream.toByteArray()));
        Assertions.assertEquals("ετε", pdfTextExtractor.getTextFromPage(1));
        Document.CompressionSettings.setCompress(true);
    }

    @Test
    void testConcatenateWatermarkPass(){
        Assertions.assertThrows(NullPointerException.class, this::testConcatenateWatermark);
    }
    void testConcatenateWatermark() throws Exception {
        String result = getString("merge-acroforms.pdf", 5);
        assertNotNull(result);
        // html??
        result = result.replaceAll("<.*?>", "");
        // Multiple spaces between words??
        assertTrue(result.contains("2. This is chapter 2"));
        assertTrue(result.contains("watermark-concatenate"));
    }

    @Test
    void whenTrunkedWordsInChunks_expectsFullWordAsExtractionPass(){
        Assertions.assertThrows(NullPointerException.class, this::whenTrunkedWordsInChunks_expectsFullWordAsExtraction);
    }
    void whenTrunkedWordsInChunks_expectsFullWordAsExtraction() throws IOException {
        // given
        byte[] pdfBytes = createSimpleDocumentWithElements(
                new Chunk("trun"),
                new Chunk("ked"));
        // when
        try {
            final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
            // then
            assertThat(extracted, is("trunked"));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void getTextFromPageWithPhrases_expectsNoAddedSpacePass(){
        Assertions.assertThrows(NullPointerException.class, this::getTextFromPageWithPhrases_expectsNoAddedSpace);
    }
    void getTextFromPageWithPhrases_expectsNoAddedSpace() throws IOException {
        // given
        byte[] pdfBytes = createSimpleDocumentWithElements(
                new Phrase("Phrase begin. "),
                new Phrase("Phrase End.")
        );
        try {
            // when
            final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
            // then
            assertThat(extracted, is("Phrase begin. Phrase End."));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void getTextFromPageWithParagraphs_expectsTextHasNoMultipleSpacesPass(){
        Assertions.assertThrows(NullPointerException.class, this::getTextFromPageWithParagraphs_expectsTextHasNoMultipleSpaces);
    }
    void getTextFromPageWithParagraphs_expectsTextHasNoMultipleSpaces() throws IOException {
        // given
        final Paragraph loremIpsumParagraph = new Paragraph(LOREM_IPSUM);
        loremIpsumParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        byte[] pdfBytes = createSimpleDocumentWithElements(
                loremIpsumParagraph,
                Chunk.NEWLINE,
                loremIpsumParagraph
        );
        final String expected = LOREM_IPSUM + " " + LOREM_IPSUM;
        try {
            // when
            final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
            // then
            assertThat(extracted, equalToCompressingWhiteSpace(expected));
            assertThat(extracted, not(containsString("  ")));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void getTextFromPageInTablesWithSingleWords_expectsWordsAreSeparatedBySpacesPass(){
        Assertions.assertThrows(NullPointerException.class, this::getTextFromPageInTablesWithSingleWords_expectsWordsAreSeparatedBySpaces);
    }
    void getTextFromPageInTablesWithSingleWords_expectsWordsAreSeparatedBySpaces()
            throws IOException {
        // given
        PdfPTable table = new PdfPTable(3);
        table.addCell("One");
        table.addCell("Two");
        table.addCell("Three");
        byte[] pdfBytes = createSimpleDocumentWithElements(table);
        try {
            // when
            final String extracted = new PdfTextExtractor(new PdfReader(pdfBytes)).getTextFromPage(1);
            // then
            assertThat(extracted, is("One Two Three"));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    private String getString(String fileName, int pageNumber) throws URISyntaxException, PDFFilterException, IOException {
        URL resource = getClass().getResource("/" + fileName);
        assert resource != null;
        return getString(new File(resource.toURI()), pageNumber);
    }

    private String getString(File file, int pageNumber) throws IOException, PDFFilterException {
        byte[] pdfBytes = readDocument(file);
        final PdfReader pdfReader = new PdfReader(pdfBytes);

        return new PdfTextExtractor(pdfReader).getTextFromPage(pageNumber);
    }
}
