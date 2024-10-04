package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.lowagie.text.ExceptionConverter;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Test;

class SimpleBookmarkTest {

    @Test
    void testGetBookmarkWithNoTitle() throws IOException {
        InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
        try(PdfReader reader = new PdfReader(is)) {
            List<?> list = SimpleBookmark.getBookmarkList(reader);
            assertNotNull(list);
            assertEquals(3, list.size());
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void testGetBookmarkListWithNoTitle() throws IOException {
        InputStream is = getClass().getResourceAsStream("/OutlineUriActionWithNoTitle.pdf");
        try (PdfReader reader = new PdfReader(is)){
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
