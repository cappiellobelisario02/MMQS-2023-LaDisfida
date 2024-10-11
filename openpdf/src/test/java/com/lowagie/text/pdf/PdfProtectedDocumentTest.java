package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lowagie.text.DocWriter;
import com.lowagie.text.Utilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfProtectedDocumentTest {

    @Test
    void signPasswordProtectedPass(){
        assertThrows(InvalidPdfException.class, this::signPasswordProtected);
    }
    void signPasswordProtected() throws Exception {
        Calendar signDate = Calendar.getInstance();

        byte[] documentBytes;
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        byte[] originalDocId;
        byte[] changingId = null;

        // Sign and compare the generated range
        for (int i = 0; i < 10; i++) {
            byte[] documentId; // Move this out of try to avoid scope issues
            try (InputStream is = getClass().getResourceAsStream("/open_protected.pdf");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(is, new byte[]{' '});
                    PdfStamper stp = PdfStamper.createSignature(reader, baos, '\0', null, true)) {

                originalDocId = reader.getDocumentId();

                stp.setEnforcedModificationDate(signDate);

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

                // Ensure range stream is closed after use
                byte[] result;
                try (InputStream rangeStream = sap.getRangeStream()) {
                    result = Utilities.toByteArray(rangeStream);
                }

                byte[] sha256 = getSHA256(result);
                if (expectedDigestPreClose == null) {
                    expectedDigestPreClose = sha256;
                } else {
                    assertFalse(Arrays.equals(expectedDigestPreClose, sha256));
                }

                PdfDictionary update = new PdfDictionary();
                update.put(PdfName.CONTENTS, new PdfString("aaaa").setHexWriting(true));
                sap.close(update);

                byte[] resultClose;
                try (InputStream rangeStreamClose = sap.getRangeStream()) {
                    resultClose = Utilities.toByteArray(rangeStreamClose);
                }

                assertArrayEquals(result, resultClose);

                byte[] sha256Close = getSHA256(resultClose);
                if (expectedDigestClose == null) {
                    expectedDigestClose = sha256Close;
                } else {
                    assertFalse(Arrays.equals(expectedDigestClose, sha256Close));
                }

                documentBytes = baos.toByteArray();
            }

            // Ensure document is readable
            try (InputStream is = new ByteArrayInputStream(documentBytes);
                    PdfReader reader = new PdfReader(is, new byte[]{' '})) {

                assertNotNull(reader);

                documentId = reader.getDocumentId(); // Get document ID after closing previous try
                assertNotNull(documentId);
                assertArrayEquals(originalDocId, documentId);

                PdfArray idArray = reader.getTrailer().getAsArray(PdfName.ID);
                assertEquals(2, idArray.size());
                assertArrayEquals(documentId,
                        com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));

                byte[] currentChangingId = DocWriter.getISOBytes(idArray.getPdfObject(1).toString());
                if (changingId != null) {
                    assertFalse(Arrays.equals(changingId, currentChangingId));
                }
                changingId = currentChangingId;
            }
        }
    }



    @Test
    void signPasswordProtectedOverrideFileIdPass(){
        Assertions.assertThrows(InvalidPdfException.class, this::signPasswordProtectedOverrideFileId);
    }
    void signPasswordProtectedOverrideFileId() throws Exception {
        Calendar signDate = Calendar.getInstance();

        byte[] originalDocId;
        PdfObject overrideFileId = new PdfLiteral("<123><123>".getBytes());

        byte[] documentBytes;
        byte[] expectedDigestPreClose = null;
        byte[] expectedDigestClose = null;

        for (int i = 0; i < 10; i++) {
            try (InputStream is = getClass().getResourceAsStream("/open_protected.pdf")) {
                if (is == null) {
                    throw new IOException("Resource not found: /open_protected.pdf");
                }

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        PdfReader reader = new PdfReader(is, new byte[]{' '})) {

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

                    // Gestione di resultClose per evitare riferimenti
                    byte[] resultClose;
                    try (ByteArrayOutputStream tempBaos = new ByteArrayOutputStream()) {
                        tempBaos.write(result); // Scrivi il risultato originale
                        resultClose = Utilities.toByteArray(sap.getRangeStream());
                    }

                    assertArrayEquals(result, resultClose);

                    byte[] sha256Close = getSHA256(resultClose);
                    if (expectedDigestClose == null) {
                        expectedDigestClose = sha256Close;
                    } else {
                        assertArrayEquals(expectedDigestClose, sha256Close);
                    }

                    documentBytes = baos.toByteArray();
                }

                // Verifica che il documento sia leggibile
                try (InputStream is2 = new ByteArrayInputStream(documentBytes);
                        PdfReader reader2 = new PdfReader(is2, new byte[]{' '})) {

                    assertNotNull(reader2);

                    byte[] documentId = reader2.getDocumentId();
                    assertNotNull(documentId);
                    assertArrayEquals(originalDocId, documentId);

                    PdfArray idArray = reader2.getTrailer().getAsArray(PdfName.ID);
                    assertEquals(2, idArray.size());
                    assertArrayEquals(documentId,
                            com.lowagie.text.DocWriter.getISOBytes(idArray.getPdfObject(0).toString()));
                    assertEquals("123", idArray.getPdfObject(1).toString());
                }
            }
        }
    }


    private byte[] getSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(bytes);
    }

}
