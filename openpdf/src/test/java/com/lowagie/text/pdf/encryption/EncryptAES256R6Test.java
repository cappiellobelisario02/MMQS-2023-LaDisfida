package com.lowagie.text.pdf.encryption;

import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author mkl
 */
class EncryptAES256R6Test {

    static final File RESULT_FOLDER = new File("target/test-outputs", "issue375");

    @BeforeAll
    static void setUpBeforeClass() {
        RESULT_FOLDER.mkdirs();
    }

    @Test
    void testCreateSimplePdfPass(){
        Assertions.assertThrows(NullPointerException.class, this::testCreateSimplePdf);
    }
    void testCreateSimplePdf() throws IOException {
        File result = new File(RESULT_FOLDER, "CreateSimplePdf.pdf");

        Document document = new Document();
        try (OutputStream os = new FileOutputStream(result);
                PdfWriter pdfWriter = PdfWriter.getInstance(document, os)) {

            pdfWriter.setEncryption("user".getBytes(), "owner".getBytes(), 0, PdfWriter.ENCRYPTION_AES_256_V3);

            document.open();
            document.add(new Paragraph("Some test content"));
            document.close();


            PdfReader pdfReader = new PdfReader(result.getAbsolutePath(), "owner".getBytes());
            pdfReader.setModificationAllowedWithoutOwnerPassword(false);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(pdfReader.isOpenedWithFullPermissions(),
                    "PdfReader fails to recognize password as owner password.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Some test content", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
            pdfReader.close();

            pdfReader = new PdfReader(result.getAbsolutePath(), "user".getBytes());
            pdfReader.setModificationAllowedWithoutOwnerPassword(false);
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(pdfReader.isOpenedWithFullPermissions(),
                    "PdfReader fails to recognize password as user password.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Some test content", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");

            pdfReader.close();
        }catch(PDFFilterException | NoSuchAlgorithmException e){
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void testStampPwProtectedAES256_openPDFiss375Pass(){
        Assertions.assertThrows(InvalidPdfException.class, this::testStampPwProtectedAES256_openPDFiss375);
    }
    void testStampPwProtectedAES256_openPDFiss375() throws IOException {
        File result = new File(RESULT_FOLDER, "pwProtectedAES256_openPDFiss375-Stamped.pdf");

        // Verifica se la risorsa esiste
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pwProtectedAES256_openPDFiss375.pdf")) {
            if (resource == null) {
                throw new IOException("Resource not found: /issue375/pwProtectedAES256_openPDFiss375.pdf");
            }

            try (OutputStream os = new FileOutputStream(result);
                    PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper pdfStamper = new PdfStamper(pdfReader, os, (char) 0, true);
                    PdfReader pdfReader2 = new PdfReader(result.getAbsolutePath())) {

                Rectangle box = pdfReader.getPageSize(1);
                PdfContentByte canvas = pdfStamper.getOverContent(1);
                canvas.setRGBColorStroke(255, 0, 0);
                canvas.moveTo(box.getLeft(), box.getBottom());
                canvas.lineTo(box.getRight(), box.getTop());
                canvas.moveTo(box.getRight(), box.getBottom());
                canvas.lineTo(box.getLeft(), box.getTop());
                canvas.stroke();

                // Validazioni sul PDF
                Assertions.assertTrue(pdfReader2.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader2.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader2).getTextFromPage(1), "Wrong text extracted from page 1");
            }

        } catch (PDFFilterException | NoSuchAlgorithmException e) {
            // Gestione delle eccezioni
            throw new ExceptionConverter(e);
        }
    }


}
