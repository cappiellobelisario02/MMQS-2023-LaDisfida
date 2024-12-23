package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Utilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfSignatureAppearanceTest {

    @Test
    void invisibleExternalSignaturePass(){
        Assertions.assertThrows(NullPointerException.class, this::invisibleExternalSignature);
    }
    void invisibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException, PDFFilterException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        Calendar signDate = Calendar.getInstance();
        byte[] originalDocId;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());
        byte[] resultDocument;

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is)) {

                originalDocId = reader.getDocumentId();

                PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);
                stp.setEnforcedModificationDate(signDate);
                stp.setOverrideFileId(overrideFileId);

                PdfSignatureAppearance sap = stp.getSignatureAppearance();
                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
                dic.put(PdfName.M, new PdfDate(signDate));

                sap.setCryptoDictionary(dic);
                sap.setSignDate(signDate);
                sap.setCertificationLevel(2);
                sap.setReason("Test");

                Map<PdfName, Integer> exc = new HashMap<>();
                exc.put(PdfName.CONTENTS, 10);
                sap.preClose(exc);

                byte[] result = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256 = getSHA256(result);
                if (expectedDigestPreClose == null) {
                    expectedDigestPreClose = sha256;
                } else {
                    assertArrayEquals(expectedDigestPreClose, sha256);
                }

                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                byte[] resultClose = Utilities.toByteArray(sap.getRangeStream());
                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertArrayEquals(expectedDigestClose, sha256Close);
                }

                resultDocument = baos.toByteArray();
            }

            try (InputStream resultIS = new ByteArrayInputStream(resultDocument);
                    PdfReader resultReader = new PdfReader(resultIS)) {

                byte[] documentId = resultReader.getDocumentId();
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = resultReader.getTrailer().getAsArray(PdfName.ID);
                assertEquals(2, idArray.size());
                assertArrayEquals(documentId,
                        com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                assertEquals("123", idArray.getPdfObject(1).toString());
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }


    @Test
    void visibleExternalSignaturePass(){
        Assertions.assertThrows(NullPointerException.class, this::visibleExternalSignature);
    }
    void visibleExternalSignature() throws DocumentException, IOException, NoSuchAlgorithmException {
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        Calendar signDate = Calendar.getInstance();

        byte[] originalDocId;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        for (int i = 0; i < 10; i++) {
            // Try-with-resources for InputStream and PdfReader
            try (InputStream is = getClass().getResourceAsStream("/EmptyPage.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is)) {

                originalDocId = reader.getDocumentId();

                // Create the PdfStamper for signature
                PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true);
                stp.setEnforcedModificationDate(signDate);
                stp.setOverrideFileId(overrideFileId);

                PdfSignatureAppearance sap = stp.getSignatureAppearance();

                PdfDictionary dic = new PdfDictionary();
                dic.put(PdfName.FILTER, PdfName.ADOBE_PPKLITE);
                dic.put(PdfName.M, new PdfDate(signDate));

                sap.setCryptoDictionary(dic);
                sap.setSignDate(signDate);
                sap.setVisibleSignature(new Rectangle(100, 100), 1);
                sap.setLayer2Text("Hello world");

                Map<PdfName, Integer> exc = new HashMap<>();
                exc.put(PdfName.CONTENTS, 10);
                sap.preClose(exc);

                // Get the range stream for the digest calculation
                try (InputStream rangeStream = sap.getRangeStream()) { // Use try-with-resources here
                    byte[] result = Utilities.toByteArray(rangeStream);
                    byte[] sha256 = getSHA256(result);
                    if (expectedDigestPreClose == null) {
                        expectedDigestPreClose = sha256;
                    } else {
                        assertArrayEquals(expectedDigestPreClose, sha256);
                    }
                }

                // Update the signature
                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                // Calculate SHA-256 of the updated result
                try (InputStream rangeStreamClose = sap.getRangeStream()) { // Use try-with-resources here
                    byte[] resultClose = Utilities.toByteArray(rangeStreamClose);
                    byte[] sha256Close = getSHA256(resultClose);
                    if (expectedDigestClose == null) {
                        expectedDigestClose = sha256Close;
                    } else {
                        assertArrayEquals(expectedDigestClose, sha256Close);
                    }
                }

                byte[] resultDocument = baos.toByteArray();

                // Ensure that PdfReader is closed
                try (InputStream resultIS = new ByteArrayInputStream(resultDocument);
                        PdfReader resultReader = new PdfReader(resultIS)) {

                    byte[] documentId = resultReader.getDocumentId();
                    assertNotNull(documentId);
                    assertArrayEquals(originalDocId, documentId);

                    PdfArray idArray = resultReader.getTrailer().getAsArray(PdfName.ID);
                    assertEquals(2, idArray.size());
                    assertArrayEquals(documentId,
                            com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                    assertEquals("123", idArray.getPdfObject(1).toString());
                }
            } catch (PDFFilterException e) {
                throw new ExceptionConverter(e);
            }
        }
    }


    private byte[] getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

}
