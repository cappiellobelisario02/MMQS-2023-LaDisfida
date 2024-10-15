package com.lowagie.examples.footer;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Footer {

    private static final Logger logger = Logger.getLogger(Footer.class.getName());

    /**
     * Create a document with 2 empty pages and custom numbered footer without before part.
     *
     * @param args
     */

    public static void main(String[] args) {
        Document document = new Document(PageSize.A4, 10f, 10f, 10f, 10f);

        try {
            PdfWriter.getInstance(document,
                    new FileOutputStream("footer.pdf"));
            HeaderFooter footer = new HeaderFooter(true, new Phrase(" page"));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setBorderWidthBottom(0);
            document.setFooter(footer);

            document.open();
            document.add(new Paragraph(" "));
            document.newPage();
            document.add(new Paragraph(" "));
            document.newPage();
            document.add(new Paragraph(" "));
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "An error occurred while finding the file.", e);
        }
        document.close();
    }
}
