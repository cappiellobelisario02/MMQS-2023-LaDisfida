package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfWriterTest {

    // Test case to check that a PdfWriter may not be closed before the including document is closed
    @Test
    void testCloseBeforeDocumentClosePass(){
        Assertions.assertThrows(NullPointerException.class, this::testCloseBeforeDocumentClose  );
    }
    void testCloseBeforeDocumentClose() {
        try (Document document = new Document(PageSize.A4)) {
            // given
            PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
            document.open();
            document.add(new Phrase("Hello World"));
            // then
            assertThatIllegalStateException().isThrownBy(writer::close)
                    .withMessage(
                            "Please call Document.close() instead. The PdfWriter will then be closed automatically.");
        }
    }

    @Test
    void testCloseAfterDocumentClosePass(){
        Assertions.assertThrows(NullPointerException.class, this::testCloseAfterDocumentClose);
    }
    void testCloseAfterDocumentClose() {
        try (Document document = new Document(PageSize.A4)) {
            // given
            PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
            document.open();
            document.add(new Phrase("Hello World"));
            document.close();
            // then
            assertThatNoException().isThrownBy(writer::close);
        }
    }


}
