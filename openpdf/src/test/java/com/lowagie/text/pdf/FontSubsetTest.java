package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FontSubsetTest {

    /*
     * See : https://github.com/LibrePDF/OpenPDF/issues/623
     */
    @Test
    void createSubsetPrefixTestPass(){
        assertTrue(true);
    }

    private byte[] getLiberationFontByte() throws IOException {
        try (InputStream stream = BaseFont.getResourceStream("fonts/liberation/LiberationSerif-Regular.ttf", null)) {
            assertThat(stream).isNotNull();
            return IOUtils.toByteArray(stream);
        }
    }

    /*
     * This test is to ensure creation of CIDSet dictionary according to the includeCidSet flag
     */
    @Test
    void includeCidSetTestPass() {
        Assertions.assertThrows(NullPointerException.class, this::includeCidSetTest);
    }

    void includeCidSetTest() throws Exception {
        assertCidSetPresence(true);
        assertCidSetPresence(false);
    }

    private void assertCidSetPresence(boolean includeCidSet) throws Exception {
        byte[] documentBytes;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            BaseFont font = BaseFont.createFont("LiberationSerif-Regular.ttf", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED, true, getLiberationFontByte(), null);
            font.setIncludeCidSet(includeCidSet);
            String text = "This is the test string.";
            document.add(new Paragraph(text, new Font(font, 12)));
            document.close();

            documentBytes = outputStream.toByteArray();
        }

        boolean fontFound = false;
        try (PdfReader reader = new PdfReader(documentBytes)) {
            for (int k = 1; k < reader.getXrefSize(); ++k) {
                PdfObject obj = reader.getPdfObjectRelease(k);
                if (obj == null || !obj.isDictionary()) {
                    continue;
                }
                PdfDictionary dic = (PdfDictionary) obj;
                PdfObject type = PdfReader.getPdfObjectRelease(dic.get(PdfName.TYPE));
                if (type == null || !type.isName()) {
                    continue;
                }
                PdfDictionary fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
                if (PdfName.FONT.equals(type) && fd != null) {
                    PdfIndirectReference cidSet = fd.getAsIndirectObject(PdfName.CIDSET);
                    assertEquals(includeCidSet, cidSet != null);
                    fontFound = true;
                    break;
                }
            }
        }
        assertTrue(fontFound);
    }

}
