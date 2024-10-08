package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import java.io.IOException;
import com.lowagie.text.ExceptionConverter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcSetTest {

    @Test
    void procSetTest1Pass(){
        Assertions.assertThrows(NullPointerException.class, this::procSetTest1);
    }
    void procSetTest1() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, stream);
        document.open();
        document.add(Chunk.NEWLINE);
        document.close();
        try (PdfReader reader = new PdfReader(stream.toByteArray())){
            Assertions.assertNull(reader.getPageN(1).getAsDict(PdfName.RESOURCES).get(PdfName.PROCSET));
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }
}
