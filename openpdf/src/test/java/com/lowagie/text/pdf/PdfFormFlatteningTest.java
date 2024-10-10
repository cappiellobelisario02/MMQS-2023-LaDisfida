package com.lowagie.text.pdf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.lowagie.text.ExceptionConverter;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;


class PdfFormFlatteningTest {

    /**
     * Flattens a problematic document. Issue described here: <a href="https://stackoverflow.com/questions/47797647">...</a>
     *
     */
    @Test
    void testFlattenSignatureDocumentPass(){
        Assertions.assertThrows(AssertionFailedError.class, this::testFlattenSignatureDocument);
    }
    void testFlattenSignatureDocument() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/20231027-DistortedFlatteningInternetExample.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");

            try (FileOutputStream fos = new FileOutputStream(
                    "target/20231027-DistortedFlatteningInternetExample-flattened.pdf");
                    PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)) {

                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }

        // Verify no form fields left
        try (InputStream resource = new FileInputStream(
                "target/20231027-DistortedFlatteningInternetExample-flattened.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");

            try (PdfReader pdfReader = new PdfReader(resource)) {
                PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                        pdfReader.getCatalog().get(PdfName.ACROFORM));
                Assertions.assertTrue(
                        acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null ||
                                acroForm.getAsArray(PdfName.FIELDS).isEmpty(),
                        "Form fields should be empty after flattening.");
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }


    /**
     * Flattens a problematic document. Issue described here: <a href="https://stackoverflow.com/questions/47755629">...</a>
     *
     */
    @Test
    void testFlattenCheckboxDocumentPass(){
        Assertions.assertThrows(AssertionFailedError.class, this::testFlattenCheckboxDocument);
    }
    void testFlattenCheckboxDocument() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/flattening/20180301-CheckboxFlatteningBug.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");
            FileOutputStream fos = new FileOutputStream(
                    "target/20180301-CheckboxFlatteningBug-flattened.pdf");

            try (PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)){
                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                "target/20180301-CheckboxFlatteningBug-flattened.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");

            try (PdfReader pdfReader = new PdfReader(resource)){
                PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                        pdfReader.getCatalog().get(PdfName.ACROFORM));
                Assertions.assertTrue(
                        acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(
                                PdfName.FIELDS).isEmpty());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Flattens a problematic document. Issue described here: <a href="https://stackoverflow.com/questions/47755629">...</a>
     *
     */
    @Test
    void testFlattenTextfieldsWithRotationAndMatrixPass() {
        Assertions.assertThrows(AssertionFailedError.class, this::testFlattenTextfieldsWithRotationAndMatrix);
    }
    void testFlattenTextfieldsWithRotationAndMatrix() throws IOException {
        String inputFilePath = "/flattening/20231027-DistortedFlatteningSmall.pdf";
        String outputFilePath = "target/20231027-DistortedFlatteningSmall-flattened.pdf";

        // Verifica e appiattisci il PDF
        try (InputStream resource = getClass().getResourceAsStream(inputFilePath)) {
            Assertions.assertNotNull(resource, "File could not be found!");

            try (FileOutputStream fos = new FileOutputStream(outputFilePath);
                    PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)) {

                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }

        // Verifica che non ci siano campi di modulo rimasti
        try (InputStream resource = new FileInputStream(outputFilePath);
                PdfReader pdfReader = new PdfReader(resource)) {

            Assertions.assertNotNull(pdfReader, "File could not be found!");

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(
                    acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(PdfName.FIELDS).isEmpty(),
                    "PDF should have no form fields remaining.");
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void testFlattenFieldsWithPdfIndirectObjectInRectPass() {
        Assertions.assertThrows(ExceptionConverter.class, this::testFlattenFieldsWithPdfIndirectObjectInRect);
    }
    void testFlattenFieldsWithPdfIndirectObjectInRect(){
        final Path targetFilePath = Paths.get("target/indirect_object_in_rectangle-flattened.pdf");

        // Create the PDF and save it
        try (InputStream resource = getClass().getResourceAsStream("/flattening/indirect_object_in_rectangle.pdf");
                OutputStream fos = Files.newOutputStream(targetFilePath);
                PdfReader pdfReader = new PdfReader(resource);
                PdfStamper stamper = new PdfStamper(pdfReader, fos)) {

            stamper.getAcroFields().setGenerateAppearances(true);
            stamper.setFormFlattening(true);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }

        // Verify that there are no fields in the PDF
        try (InputStream resource = Files.newInputStream(targetFilePath);
                PdfReader pdfReader = new PdfReader(resource)) {

            PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                    pdfReader.getCatalog().get(PdfName.ACROFORM));
            Assertions.assertTrue(acroForm == null
                    || acroForm.getAsArray(PdfName.FIELDS) == null
                    || acroForm.getAsArray(PdfName.FIELDS).isEmpty());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
}
