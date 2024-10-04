package com.lowagie.text.pdf;

import java.io.File;
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


class PdfFormFlatteningTest {


    /**
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47797647
     *
     * @throws IOException exception that may be thrown in the body of this test method
     */
    @Test
    void testFlattenSignatureDocument() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/20231027-DistortedFlatteningInternetExample.pdf")) {

            Assertions.assertNotNull(resource, "File could not be found!");

            FileOutputStream fos = new FileOutputStream(
                    new File("target/20231027-DistortedFlatteningInternetExample-flattened.pdf"));

            try (PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)){
                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                new File("target/20231027-DistortedFlatteningInternetExample-flattened.pdf"))) {

            Assertions.assertNotNull(resource, "File could not be found!");
            try (PdfReader pdfReader = new PdfReader(resource)){
                PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectRelease(
                        pdfReader.getCatalog().get(PdfName.ACROFORM));
                Assertions.assertTrue(
                        acroForm == null || acroForm.getAsArray(PdfName.FIELDS) == null || acroForm.getAsArray(
                                PdfName.FIELDS).isEmpty());
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47755629
     *
     * @throws IOException
     */
    @Test
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
                new File("target/20180301-CheckboxFlatteningBug-flattened.pdf"))) {

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
     * Flattens a problematic document. Issue described here: https://stackoverflow.com/questions/47755629
     *
     * @throws IOException exception thrown within the body
     */
    @Test
    void testFlattenTextfieldsWithRotationAndMatrix() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/20231027-DistortedFlatteningSmall.pdf")) {
            Assertions.assertNotNull(resource, "File could not be found!");
            FileOutputStream fos = new FileOutputStream(
                    "target/20231027-DistortedFlatteningSmall-flattened.pdf");

            try (PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)){
                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = new FileInputStream(
                new File("target/20231027-DistortedFlatteningSmall-flattened.pdf"))) {

            Assertions.assertNotNull(resource, "File could not be found!");

            try(PdfReader pdfReader = new PdfReader(resource)) {
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

    @Test
    void testFlattenFieldsWithPdfIndirectObjectInRect() throws IOException {
        final Path targetFilePath = Paths.get("target/indirect_object_in_rectangle-flattened.pdf");
        try (InputStream resource = getClass().getResourceAsStream(
                "/flattening/indirect_object_in_rectangle.pdf")) {
            Assertions.assertNotNull(resource, "File could not be found!");
            OutputStream fos = Files.newOutputStream(targetFilePath);

            try (PdfReader pdfReader = new PdfReader(resource);
                    PdfStamper stamper = new PdfStamper(pdfReader, fos)){
                stamper.getAcroFields().setGenerateAppearances(true);
                stamper.setFormFlattening(true);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
        //Verify no form fields left (the correct shape is difficult to verify...)
        try (InputStream resource = Files.newInputStream(targetFilePath)) {

            Assertions.assertNotNull(resource, "File could not be found!");

            try(PdfReader pdfReader = new PdfReader(resource)) {
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

}
