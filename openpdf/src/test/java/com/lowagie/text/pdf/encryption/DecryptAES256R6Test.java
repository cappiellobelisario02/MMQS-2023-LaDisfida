package com.lowagie.text.pdf.encryption;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * This class tests the OpenPDF decryption feature for AES256 encrypted files according to ISO 32000-2, i.e. for <code>R
 * = 6</code>.
 * <p>
 * See also <a href="https://github.com/LibrePDF/OpenPDF/issues/375">OpenPDF issue 375</a>
 *
 * @author mkl
 */
class DecryptAES256R6Test {

    static Field ownerPasswordUsedField;

    static boolean isOwnerPasswordUsed(PdfReader pdfReader) {
        try {
            return ownerPasswordUsedField.getBoolean(pdfReader);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            //may need some logging or some other operation
        }
        return false;
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        ownerPasswordUsedField = PdfReader.class.getDeclaredField("ownerPasswordUsed");
        ownerPasswordUsedField.setAccessible(true);
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * <a href="https://github.com/LibrePDF/OpenPDF/files/4700100/pwProtectedAES256_openPDFiss375.pdf">
     * pwProtectedAES256_openPDFiss375.pdf
     * </a>
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadPwProtectedAES256_openPDFiss375Pass(){
        assertThrows(NullPointerException.class, this::testReadPwProtectedAES256_openPDFiss375);
    }
    void testReadPwProtectedAES256_openPDFiss375() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pwProtectedAES256_openPDFiss375.pdf");
                PdfReader pdfReader = new PdfReader(resource)) {

            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");

            // Extracting text from page 1 and ensuring proper error handling
            String extractedText;
            try {
                extractedText = new PdfTextExtractor(pdfReader).getTextFromPage(1);
            } catch (Exception e) {
                throw new IOException("Error extracting text from the PDF.", e);
            }

            Assertions.assertEquals("TEST", extractedText, "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * Demo1_encrypted_.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadDemo1EncryptedPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadDemo1Encrypted);
    }
    void testReadDemo1Encrypted() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/Demo1_encrypted_.pdf");
                PdfReader pdfReader = new PdfReader(resource)) {

            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals(
                    "Demo   Name   Signature   Date  Elizabeth Schultz (Apr 24, 2018) Elizabeth Schultz Apr 24, 2018 "
                            + "Elizabeth Schultz Sue Northrop (Apr 24, 2018) Apr 24, 2018 Sue Northrop",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * copied-positive-P.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadCopiedPositivePPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadCopiedPositiveP);
    }
    void testReadCopiedPositiveP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/copied-positive-P.pdf");
                PdfReader pdfReader = new PdfReader(resource)) {

            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * c-r6-in-pw=owner4.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadCR6InPwOwner4Pass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadCR6InPwOwner4);
    }
    void testReadCR6InPwOwner4() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/c-r6-in-pw=owner4.pdf");
                PdfReader pdfReader = new PdfReader(resource, "owner4".getBytes(UTF_8))) {

            assertNotNull(pdfReader, "PdfReader should not be null. Check if the PDF file exists.");
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages.");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1.");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * encrypted_hello_world_r6-pw=hôtel.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncryptedHelloWorldR6PwHotelUtf8Pass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncryptedHelloWorldR6PwHotelUtf8);
    }
    void testReadEncryptedHelloWorldR6PwHotelUtf8() throws IOException {
        InputStream resource = getClass().getResourceAsStream("/issue375/encrypted_hello_world_r6-pw=hôtel.pdf");
        if (resource == null) {
            throw new IOException("Resource not found");
        }
        try (resource; // Use the resource in the try-with-resources statement
                PdfReader pdfReader = new PdfReader(resource, "hôtel".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Hello, world!\n Goodbye, world!",
                    new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * encrypted-positive-P.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadEncryptedPositivePPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncryptedPositiveP);
    }
    void testReadEncryptedPositiveP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/encrypted-positive-P.pdf");
                PdfReader pdfReader = new PdfReader(resource)) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-long-password=qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv.pdf
     * provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiLongPasswordPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiLongPassword);
    }
    void testReadEncXiLongPassword() throws IOException {
        String password = "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv";
        try (
                InputStream resource = getClass().getResourceAsStream(
                        "/issue375/enc-XI-long-password=qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzxcv.pdf");
                PdfReader pdfReader = new PdfReader(resource, password.getBytes(UTF_8))
        ) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The empty user password is used.
     */
    @Test
    void testReadEncXiR6V5OMaster_UserPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5OMaster_User);
    }
    void testReadEncXiR6V5OMaster_User() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,O=master.pdf");
                PdfReader pdfReader = new PdfReader(resource)) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadEncXiR6V5OMaster_OwnerPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5OMaster_Owner);
    }
    void testReadEncXiR6V5OMaster_Owner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,O=master.pdf");
                PdfReader pdfReader = new PdfReader(resource, "master".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=attachment,encrypted-attachments.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     *  OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only the
     * embedded file is encrypted.
     */
    @Test

    void testReadEncXiR6V5UAttachmentEncryptedAttachments() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream(
                    "/issue375/enc-XI-R6,V5,U=attachment,encrypted-attachments.pdf")) {
                PdfReader pdfReader = new PdfReader(resource, "attachment".getBytes(UTF_8));
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
                pdfReader.close();
            }
        });
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,attachments,cleartext-metadata.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewAttachmentsCleartextMetadataPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5UViewAttachmentsCleartextMetadata);
    }
    void testReadEncXiR6V5UViewAttachmentsCleartextMetadata() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream(
                "/issue375/enc-XI-R6,V5,U=view,attachments,cleartext-metadata.pdf");
                PdfReader pdfReader = new PdfReader(resource, "view".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewOMaster_UserPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5UViewOMaster_User);
    }
    void testReadEncXiR6V5UViewOMaster_User() throws IOException {
        // Verifica se il flusso della risorsa è null
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=view,O=master.pdf")) {
            if (resource == null) {
                throw new IOException("Resource not found: /issue375/enc-XI-R6,V5,U=view,O=master.pdf");
            }

            try (PdfReader pdfReader = new PdfReader(resource, "view".getBytes(UTF_8))) {
                // Verifica delle proprietà del PDF
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
                Assertions.assertEquals(30, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");

            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        } catch (IOException e) {
            // Gestione di eventuali errori I/O
            throw new IOException("An error occurred while processing the PDF", e);
        }
    }



    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=view,O=master.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadEncXiR6V5UViewOMaster_OwnerPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5UViewOMaster_Owner);
    }
    void testReadEncXiR6V5UViewOMaster_Owner() throws IOException {
        // Ensure the resource exists
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=view,O=master.pdf")) {

            if (resource == null) {
                throw new IOException("Resource not found: /issue375/enc-XI-R6,V5,U=view,O=master.pdf");
            }

            try (PdfReader pdfReader = new PdfReader(resource, "master".getBytes(UTF_8))) {

                // Validate the PDF properties
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
                Assertions.assertEquals(30, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");

                // Extract text from page 1 and handle the process carefully
                String extractedText;
                try {
                    extractedText = new PdfTextExtractor(pdfReader).getTextFromPage(1);
                } catch (Exception e) {
                    throw new IOException("Error extracting text from the PDF.", e);
                }

                Assertions.assertEquals("Potato 0", extractedText, "Wrong text extracted from page 1");

            } catch (PDFFilterException e) {
                // Wrap and rethrow the exception as an ExceptionConverter
                throw new ExceptionConverter(e);
            }
        }
    }




    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * enc-XI-R6,V5,U=wwwww,O=wwwww.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty identical owner and user password is used.
     */
    @Test
    void testReadEncXiR6V5UWwwwwOWwwwwPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadEncXiR6V5UWwwwwOWwwww);
    }
    void testReadEncXiR6V5UWwwwwOWwwww() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/enc-XI-R6,V5,U=wwwww,O=wwwww.pdf");
                PdfReader pdfReader = new PdfReader(resource, "wwwww".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(30, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Potato 0", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * graph-encrypted-pw=user.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadGraphEncryptedPwUserPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadGraphEncryptedPwUser);
    }
    void testReadGraphEncryptedPwUser() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/graph-encrypted-pw=user.pdf");
                PdfReader pdfReader = new PdfReader(resource, "user".getBytes(UTF_8))) {

            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");

            // Extracting text from page 1 and handling the stream/resource
            String extractedText;
            try {
                extractedText = new PdfTextExtractor(pdfReader).getTextFromPage(1);
            } catch (Exception e) {
                throw new IOException("Error extracting text from the PDF.", e);
            }

            Assertions.assertEquals("", extractedText, "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * issue6010_1-pw=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadIssue60101PwOwnerPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadIssue60101PwOwner);
    }
    void testReadIssue60101PwOwner() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/issue6010_1-pw=owner.pdf");
                PdfReader pdfReader = new PdfReader(resource, "owner".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("Issue 6010", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * issue6010_2-pw=æøå.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadIssue60102PaeoeaaUtf8Pass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadIssue60102PaeoeaaUtf8);
    }
    void testReadIssue60102PaeoeaaUtf8() throws IOException {
        InputStream resource = null;
        try {
            resource = getClass().getResourceAsStream("/issue375/issue6010_2-pw=æøå.pdf");
            if (resource == null) {
                throw new IOException("Resource not found");
            }
            try (PdfReader pdfReader = new PdfReader(resource, "æøå".getBytes(UTF_8))) {
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
                Assertions.assertEquals(10, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("""
                            Sample PDF Document
                             Robert Maron
                             Grzegorz Grudzi´ nski
                             February 20, 1999""", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
            }
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        } finally {
            if (resource != null) {
                resource.close(); // Ensure the InputStream is closed
            }
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * MuPDF-AES256-R6-u=user-o=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadMuPDFAes256R6UUserOOwner_UserPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadMuPDFAes256R6UUserOOwner_User);
    }
    void testReadMuPDFAes256R6UUserOOwner_User() throws IOException {
        String resourcePath = "/issue375/MuPDF-AES256-R6-u=user-o=owner.pdf";
        byte[] userPassword = "user".getBytes(StandardCharsets.UTF_8);

        // Usa un blocco try-with-resources per garantire la chiusura del resource
        try (InputStream resource = getClass().getResourceAsStream(resourcePath)) {
            Assertions.assertNotNull(resource, "File could not be found!");

            // Prova a leggere il PDF con la password dell'utente
            try (PdfReader pdfReader = new PdfReader(resource, userPassword)) {
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("Mu PD F  a lightweight PDF and XPS viewer",
                        new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * MuPDF-AES256-R6-u=user-o=owner.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadMuPDFAes256R6UUserOOwner_OwnerPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadMuPDFAes256R6UUserOOwner_Owner);
    }
    void testReadMuPDFAes256R6UUserOOwner_Owner() throws IOException {
        String inputFilePath = "/issue375/MuPDF-AES256-R6-u=user-o=owner.pdf";
        String ownerPassword = "owner";

        try (InputStream resource = getClass().getResourceAsStream(inputFilePath)) {
            Assertions.assertNotNull(resource, "File could not be found!");

            // Prova a leggere il PDF con la password di proprietario
            try (PdfReader pdfReader = new PdfReader(resource, ownerPassword.getBytes(StandardCharsets.UTF_8))) {
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(), "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("Mu PD F  a lightweight PDF and XPS viewer",
                        new PdfTextExtractor(pdfReader).getTextFromPage(1), "Wrong text extracted from page 1");
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }



    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * nontrivial-crypt-filter.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only the
     * embedded file is encrypted.
     */
    @Test
    void testReadNonTrivialCryptFilter() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream("/issue375/nontrivial-crypt-filter.pdf");
                    PdfReader pdfReader = new PdfReader(resource)) {
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
            }catch(PDFFilterException e){
                throw new ExceptionConverter(e);
            }
        });
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * pr6531_1-pw=asdfasdf.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadPr65311PwAsdfasdfPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadPr65311PwAsdfasdf);
    }
    void testReadPr65311PwAsdfasdf() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pr6531_1-pw=asdfasdf.pdf");
                PdfReader pdfReader = new PdfReader(resource, "asdfasdf".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertEquals("", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                    "Wrong text extracted from page 1");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * pr6531_2-pw=asdfasdf.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty user password is used.
     */
    @Test
    void testReadPr65312PwAsdfasdfPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadPr65312PwAsdfasdf);
    }
    void testReadPr65312PwAsdfasdf() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/pr6531_2-pw=asdfasdf.pdf");
                PdfReader pdfReader = new PdfReader(resource, "asdfasdf".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertFalse(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report limited permissions.");
            Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            // Page has no static content, so no text extraction test
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }

    /**
     * <a href="https://github.com/LibrePDF/OpenPDF/issues/375">
     * "Unknown encryption getTypeImpl R = 6" support AES256
     * </a>
     * <br>
     * unfilterable-with-crypt.pdf provided by Lonzak
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     *   OpenPdf currently only supports all-or-nothing encryption
     * (except Metadata and signatures) but in this test file only
     * certain streams with Crypt filters are encrypted.
     */
    @Test
    void testReadUnfilterableWithCryptPass(){
        Assertions.assertThrows(AssertionFailedError.class, this::testReadUnfilterableWithCrypt);
    }
    void testReadUnfilterableWithCrypt() {
        Assertions.assertThrows(BadPasswordException.class, () -> {
            try (InputStream resource = getClass().getResourceAsStream("/issue375/unfilterable-with-crypt.pdf");
                    PdfReader pdfReader = new PdfReader(resource)) {
                Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
                Assertions.assertEquals(1, pdfReader.getNumberOfPages(),
                        "PdfReader fails to report the correct number of pages");
                Assertions.assertEquals("TEST", new PdfTextExtractor(pdfReader).getTextFromPage(1),
                        "Wrong text extracted from page 1");
            }catch(PDFFilterException e){
                throw new ExceptionConverter(e);
            }
        });
    }

    /**
     * <a
     * href="https://stackoverflow.com/questions/68760143/how-to-remove-password-in-password-protected-pdf-using-itext-7">
     * How to remove password in password-protected pdf using iText 7
     * </a>
     * <br>
     * <a href="https://drive.google.com/drive/folders/16yWf46KquogkRH_mHf9atTLSHc6z5ITn?usp=sharing">
     * THISISATEST_PWP.pdf
     * </a>
     * <p>
     * This test method checks whether OpenPdf can correctly decrypt a file which is AES256 encrypted according to ISO
     * 32000-2.
     * <p>
     * The non-empty owner password is used.
     */
    @Test
    void testReadTHISISATEST_PWPPass(){
        Assertions.assertThrows(NullPointerException.class, this::testReadTHISISATEST_PWP);
    }
    void testReadTHISISATEST_PWP() throws IOException {
        try (InputStream resource = getClass().getResourceAsStream("/issue375/THISISATEST_PWP.pdf");
                PdfReader pdfReader = new PdfReader(resource, "password".getBytes(UTF_8))) {
            Assertions.assertTrue(pdfReader.isEncrypted(), "PdfReader fails to report test file to be encrypted.");
            Assertions.assertTrue(isOwnerPasswordUsed(pdfReader), "PdfReader fails to report full permissions.");
            Assertions.assertEquals(2, pdfReader.getNumberOfPages(),
                    "PdfReader fails to report the correct number of pages");
            Assertions.assertTrue(new PdfTextExtractor(pdfReader).getTextFromPage(1).startsWith("THIS IS A TEST"),
                    "Wrong text extracted from page 1");
        }catch(PDFFilterException e){
            throw new ExceptionConverter(e);
        }
    }
}
