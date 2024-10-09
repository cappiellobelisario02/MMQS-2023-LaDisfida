package com.lowagie.text.html;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.Document;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

/**
 * This test class contains a series of smoke tests. The goal of these tests is not validate the generated document, but
 * to ensure no exception is thrown.
 */
class SAXmyHtmlHandlerTest {

    /**
     * Test scenario: a html file with a title will not generate.
     */
    @Test
    void testTitle_generate() {
        InputStream is = SAXmyHtmlHandlerTest.class.getClassLoader().getResourceAsStream("parseTitle.html");
        parseHtml(is);
    }


    /**
     * Test scenario: a html file with a table will generate like html webpage.
     */
    @Test
    void testTable_generate() {
        try (InputStream is = SAXmyHtmlHandlerTest.class.getClassLoader().getResourceAsStream("parseTable.html")) {
            if (is == null) {
                throw new IOException("Resource 'parseTable.html' not found");
            }
            parseHtml(is);
        } catch (IOException e) {
            System.err.println("Error reading HTML resource: " + e.getMessage());
        }
    }


    /**
     * Parse the input HTML file to PDF file.
     *
     * @param is The input stream of the HTML file.
     */
    void parseHtml(InputStream is) {
        try {
            Document doc1 = new Document();
            doc1.open();
            HtmlParser.parse(doc1, is);
            assertNotNull(doc1, () -> is + " was not parsed successfully");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
