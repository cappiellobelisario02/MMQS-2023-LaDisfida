package org.librepdf.openpdf.examples.fonts.languages;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Japanese {
        public void generatePdf() throws IOException {
            // step 0: prepare font with Chinese symbols
            BaseFont baseFont = BaseFont.createFont(
                    Objects.requireNonNull(Japanese.class.getClassLoader().getResource("fonts/GenShinGothic-Normal.ttf")).getFile(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 12, Font.NORMAL);

            // step 1: Prepare document for Japanese text
            Document document = new Document();
            ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, pdfOutput);
            document.open();

            // step 2: Add content to the document
            document.add(new Chunk("\uD842\uDFB7", font));

            // step 3: Close the document
            document.close();

            // Write the PDF file to the file system
            Files.write(Paths.get(Japanese.class.getSimpleName() + ".pdf"), pdfOutput.toByteArray());
        }
}
