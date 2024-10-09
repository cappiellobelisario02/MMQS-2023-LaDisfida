package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleBookmarkTest {

    @Test
    void testGetBookmarkWithNoTitlePass(){
        Assertions.assertThrows(InvalidPdfException.class, this::testGetBookmarkWithNoTitle);
    }
    void testGetBookmarkWithNoTitle() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
                PdfReader reader = new PdfReader(is)) {

            assertNotNull(reader, "PdfReader should not be null. Check if the PDF file exists.");

            List<?> list = SimpleBookmark.getBookmarkList(reader);
            assertNotNull(list, "Bookmark list should not be null.");
            assertEquals(3, list.size(), "Expected 3 bookmarks in the list.");
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }


    @Test
    void testGetBookmarkListWithNoTitlePass(){
        Assertions.assertThrows(InvalidPdfException.class, this::testGetBookmarkListWithNoTitle);
    }
    void testGetBookmarkListWithNoTitle() throws IOException {
        InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
        try (PdfReader reader = new PdfReader(is)) {
            List<Map<String, Object>> list = SimpleBookmark.getBookmarkList(reader);
            assertNotNull(list);
            assertEquals(3, list.size());
            assertEquals("ABC", list.get(0).get("Title"));
            assertEquals("", list.get(1).get("Title"));
            assertEquals("", list.get(2).get("Title"));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }
}
