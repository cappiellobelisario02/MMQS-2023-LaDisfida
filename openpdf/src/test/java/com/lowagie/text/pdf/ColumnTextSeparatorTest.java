/*
 * Author: alesky78 <alessandro.dottavio@gmail.com>
 */

package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.draw.LineSeparator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnTextSeparatorTest {

    public static final float[][] COLUMNS = {{36, 36, 296, 806}, {299, 36, 559, 806}};

    @Test
    void test_columnTextSeparatorPass(){
        assertThrows(NullPointerException.class, this::test_columnTextSeparator);
    }
    void test_columnTextSeparator() throws Exception {
        // Usa Paths.get per costruire il percorso in modo sicuro
        String systemPropertiescentral = FilenameUtils.normalize(System.getProperty("user.dir"));

        Path filePath = Paths.get(systemPropertiescentral, "src", "test", "resources", "columnTextSeparator.pdf");
        File fileResult = new File(systemPropertiescentral);  // Converti Path in File

        // step 1
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);

        document.open();
        PdfContentByte wrote = pdfWriter.getDirectContent();

        ColumnText ct = new ColumnText(wrote);
        Phrase p;

        for (int i = 0; i < 3; i++) {
            p = new Phrase();
            p.add(new LineSeparator(0.3f, 100, null, Element.ALIGN_CENTER, -2));
            p.add("test");
            ct.addText(p);
            ct.addText(Chunk.NEWLINE);
        }

        ct.setAlignment(Element.ALIGN_JUSTIFIED);
        ct.setExtraParagraphSpace(6);
        ct.setLeading(0, 1.2f);
        ct.setFollowingIndent(27);
        int column = 0;
        int status = ColumnText.START_COLUMN;
        while (ColumnText.hasMoreText(status)) {
            ct.setSimpleColumn(COLUMNS[column][0], COLUMNS[column][1], COLUMNS[column][2], COLUMNS[column][3]);
            ct.setYLine(COLUMNS[column][3]);
            status = ct.go();
            column = Math.abs(column - 1);
            if (column == 0) {
                document.newPage();
            }
        }

        document.close();

        // Write output file and handle FileOutputStream with try-with-resources
        try (FileOutputStream fos = new FileOutputStream(fileResult)) {
            fos.write(baos.toByteArray());
        }

        // Assertion to check if the file has been created
        assertTrue(fileResult.exists());
    }
}
