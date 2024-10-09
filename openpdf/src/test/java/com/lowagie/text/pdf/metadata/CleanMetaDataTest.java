package com.lowagie.text.pdf.metadata;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.xmp.XmpWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CleanMetaDataTest {

    public CleanMetaDataTest() {
        super();
    }

    private HashMap<String, String> createCleanerMoreInfo() {
        HashMap<String, String> moreInfo = new HashMap<String, String>();
        moreInfo.put("Title", null);
        moreInfo.put("Author", null);
        moreInfo.put("Subject", null);
        moreInfo.put("Producer", null);
        moreInfo.put("Keywords", null);
        moreInfo.put("Creator", null);
        moreInfo.put("ModDate", null);
        return moreInfo;
    }

    @Test
    void testProducerPass(){
        Assertions.assertThrows(NullPointerException.class, this::testProducer);
    }
    void testProducer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Hello World"));
        document.close();

        try (PdfReader r = new PdfReader(baos.toByteArray())) {
            final String producer = r.getInfo().get("Producer");
            org.assertj.core.api.Assertions.assertThat(producer).startsWith("OpenPDF ");
        }

    }

    @Test
    void testAddedMetadataPass(){
        Assertions.assertThrows(NullPointerException.class, this::testAddedMetadata);
    }
    void testAddedMetadata() throws Exception {
        String authorname = "Mr Bean";
        String title = "The title";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        PdfWriter.getInstance(document, baos);

        document.open();
        document.addProducer();
        document.addAuthor(authorname);
        document.addTitle(title);
        document.add(new Paragraph("Hello World"));
        document.close();

        PdfReader r = new PdfReader(baos.toByteArray());

        // Metadata generated only on demand
        Assertions.assertEquals(Document.getVersion(), r.getInfo().get("Producer"));

        Assertions.assertEquals(authorname, r.getInfo().get("Author"));
        Assertions.assertEquals(title, r.getInfo().get("Title"));

        r.close();
    }


    @Test
    void testStamperMetadataPass(){
        Assertions.assertThrows(IOException.class, this::testStamperMetadata);
    }
    void testStamperMetadata() throws Exception {
        byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), false, createCleanerMoreInfo());
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
        String dataString = new String(data);
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    void testStamperEncryptMetadataPass(){
        Assertions.assertThrows(IOException.class, this::testStamperEncryptMetadata);
    }
    void testStamperEncryptMetadata() throws Exception {
        byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), true, createCleanerMoreInfo());
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
    }


    @Test
    void testStamperExtraMetadataPass(){
        Assertions.assertThrows(IOException.class, this::testStamperExtraMetadata);
    }
    void testStamperExtraMetadata() throws Exception {
        HashMap<String, String> moreInfo = createCleanerMoreInfo();
        moreInfo.put("Producer", Document.getVersion());
        moreInfo.put("Author", "Author1");
        moreInfo.put("Title", "Title2");
        moreInfo.put("Subject", "Subject3");
        byte[] data = addWatermark(new File("src/test/resources/HelloWorldMeta.pdf"), false, moreInfo);
        PdfReader r = new PdfReader(data);
        Assertions.assertEquals(Document.getVersion(), r.getInfo().get("Producer"));
        Assertions.assertEquals("Author1", r.getInfo().get("Author"));
        Assertions.assertEquals("Title2", r.getInfo().get("Title"));
        Assertions.assertEquals("Subject3", r.getInfo().get("Subject"));
        r.close();
    }

    @Test
    void testCleanMetadataMethodInStamperPass(){
        Assertions.assertThrows(IOException.class, this::testCleanMetadataMethodInStamper);
    }
    void testCleanMetadataMethodInStamper() throws Exception {
        byte[] data = cleanMetadata(new File("src/test/resources/HelloWorldMeta.pdf"));
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
        String dataString = new String(data);
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    void skipMetaDataUpdateTestPass(){
        Assertions.assertThrows(IOException.class, this::skipMetaDataUpdateTest);
    }
    void skipMetaDataUpdateTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);
        stamp.setUpdateMetadata(false);
        stamp.cleanMetadata();
        stamp.close();

        String dataString = baos.toString();
        Assertions.assertTrue(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    void skipMetaDataUpdateFirstRevisionTestPass(){
        Assertions.assertThrows(IOException.class, this::skipMetaDataUpdateFirstRevisionTest);
    }
    void skipMetaDataUpdateFirstRevisionTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', false);
        stamp.setUpdateMetadata(false);
        stamp.cleanMetadata();
        stamp.close();

        String dataString = baos.toString();
        Assertions.assertFalse(dataString.contains("This example explains how to add metadata."));
    }

    @Test
    void skipInfoUpdateTestPass(){
        Assertions.assertThrows(IOException.class, this::skipInfoUpdateTest);
    }
    void skipInfoUpdateTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);

        HashMap<String, String> moreInfo = createCleanerMoreInfo();
        moreInfo.put("Producer", Document.getVersion());
        moreInfo.put("Author", "Author1");
        moreInfo.put("Title", "Title2");
        moreInfo.put("Subject", "Subject3");
        stamp.setInfoDictionary(moreInfo);

        stamp.setUpdateDocInfo(false);
        stamp.close();

        PdfReader r = new PdfReader(baos.toByteArray());
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        r.close();
    }

    @Test
    void skipInfoUpdateFirstRevisionTestPass(){
        Assertions.assertThrows(IOException.class, this::skipInfoUpdateFirstRevisionTest);
    }
    void skipInfoUpdateFirstRevisionTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(new File("src/test/resources/HelloWorldMeta.pdf").getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', false);

        HashMap<String, String> moreInfo = createCleanerMoreInfo();
        moreInfo.put("Producer", Document.getVersion());
        moreInfo.put("Author", "Author1");
        moreInfo.put("Title", "Title2");
        moreInfo.put("Subject", "Subject3");
        stamp.setInfoDictionary(moreInfo);

        stamp.setUpdateDocInfo(false);
        stamp.close();

        PdfReader r = new PdfReader(baos.toByteArray());
        Assertions.assertNotNull(r.getInfo().get("Producer"));
        Assertions.assertNotNull(r.getInfo().get("Author"));
        Assertions.assertNotNull(r.getInfo().get("Title"));
        Assertions.assertNotNull(r.getInfo().get("Subject"));
        r.close();
    }

    @Test
    void testXMPMetadataPass(){
        Assertions.assertThrows(IOException.class, this::testXMPMetadata);
    }
    void testXMPMetadata() throws Exception {
        File file = new File("src/test/resources/HelloWorldMeta.pdf");
        PdfReader reader = new PdfReader(file.getAbsolutePath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamp = new PdfStamper(reader, baos);
        Map<String, String> moreInfo = createCleanerMoreInfo();
        ByteArrayOutputStream meta = new ByteArrayOutputStream();
        XmpWriter writer = new XmpWriter(meta, moreInfo);
        writer.close();
        // Manually set clean metadata
        stamp.setInfoDictionary(moreInfo);
        stamp.setXmpMetadata(meta.toByteArray());

        stamp.close();

        byte[] data = baos.toByteArray();
        PdfReader r = new PdfReader(data);
        Assertions.assertNull(r.getInfo().get("Producer"));
        Assertions.assertNull(r.getInfo().get("Author"));
        Assertions.assertNull(r.getInfo().get("Title"));
        Assertions.assertNull(r.getInfo().get("Subject"));
        byte[] metadata = r.getMetadata();
        r.close();
        String dataString = new String(data);

        Assertions.assertFalse(dataString.contains("Bruno Lowagie"));
        Assertions.assertFalse(dataString.contains(" 1.2.12.SNAPSHOT"));
        if (metadata != null) {
            String metadataString = new String(metadata);
            Assertions.assertFalse(metadataString.contains("Bruno Lowagie"));
            Assertions.assertFalse(metadataString.contains(" 1.2.12.SNAPSHOT"));
            Assertions.assertTrue(metadataString.contains("<pdf:Producer></pdf:Producer>"));
        }
    }

    private byte[] cleanMetadata(File origin) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(origin.getAbsolutePath());
        PdfStamper stamp = new PdfStamper(reader, baos);
        stamp.cleanMetadata();
        stamp.close();
        return baos.toByteArray();
    }

    private byte[] addWatermark(File origin, boolean encrypt, HashMap<String, String> moreInfo) throws Exception {
        int textAngle = 45;
        int text1PosX = 300;
        int text1PosY = 430;
        int text2PosX = 330;
        int text2PosY = 410;
        String text1 = "NOT VALID";
        String text2 = "DRAFT";
        int fontSize = 32;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(origin.getAbsolutePath());
        int n = reader.getNumberOfPages();
        PdfStamper stamp = new PdfStamper(reader, baos);
        stamp.setInfoDictionary(moreInfo);
        if (encrypt) {
            stamp.setEncryption(null, null, 0, PdfWriter.ENCRYPTION_AES_128);
        }
        int i = 0;
        PdfContentByte over;
        BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);

        while (i < n) {
            i++;
            over = stamp.getOverContent(i);
            over.beginText();
            over.setRGBColorFill(255, 0, 0);
            over.setFontAndSize(bf, fontSize);
            over.showTextAligned(Element.ALIGN_CENTER, text1, text1PosX, text1PosY, textAngle);
            over.showTextAligned(Element.ALIGN_CENTER, text2, text2PosX, text2PosY, textAngle);
            over.endText();
        }

        stamp.close();

        return baos.toByteArray();
    }

}
