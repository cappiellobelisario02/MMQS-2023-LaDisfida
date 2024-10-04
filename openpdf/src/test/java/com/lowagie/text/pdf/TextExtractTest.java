package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextExtractTest {

    @Test
    void textExtractTest1Pass(){
        Assertions.assertThrows(InvalidPdfException.class, this::textExtractTest1);
    }
    void textExtractTest1() throws IOException {
        try (PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/identity-h.pdf"))){
            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
            Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void textExtractTest2Pass(){
        Assertions.assertThrows(InvalidPdfException.class, this::textExtractTest2);
    }
    void textExtractTest2() throws IOException {
        try (PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/HelloWorldMeta.pdf"))){
            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
            Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void extCreateAndExtractTest2Pass(){
        Assertions.assertThrows(NullPointerException.class, this::textCreateAndExtractTest2);
    }
    void textCreateAndExtractTest2() throws IOException {
        LayoutProcessor.enableKernLiga();
        float fontSize = 12.0f;

        String testText = "กขน้ำตา ญูญูิ่ ก้กิ้";

        URL fontPath = TextExtractTest.class.getResource("/fonts/NotoSansThaiLooped/NotoSansThaiLooped-Regular.ttf");

        assertThat(fontPath).isNotNull();
        FontFactory.register(fontPath.toString(), "NotoSansThaiLooped");
        Font notoSansThaiLooped = FontFactory.getFont("NotoSansThaiLooped", BaseFont.IDENTITY_H, true, fontSize);
        notoSansThaiLooped.getBaseFont().setIncludeCidSet(false);

        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, pdfOutput);
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(testText, notoSansThaiLooped));
        }catch(DocumentException de){
            throw new ExceptionConverter(de);
        }
        LayoutProcessor.disable();

        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfOutput.toByteArray()))){
            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);

            // FileOutputStream test = new FileOutputStream("/tmp/output2.pdf")
            // pdfOutput.writeTo(test)

            // Ignore spaces in comparison
            Assertions.assertEquals("ก ข น ํ้ า ต า ญูญูิ่ ก้กิ้".replaceAll(" ", ""),
                pdfTextExtractor.getTextFromPage(1).replaceAll(" ", ""));
        } catch (PDFFilterException e) {
            throw new RuntimeException(e);
        }
    }
}
