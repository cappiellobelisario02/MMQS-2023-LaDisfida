/*
 * $Id: TrueTypeCollections.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.fonts.getting;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Using True Type Collections.
 */
public class TrueTypeCollections {

    /**
     * Using true type collections.
     *
     * @param args no arguments needed
     */
    private static final Logger logger = Logger.getLogger(TrueTypeCollections.class.getName());

    public static void main(String[] args) {
        System.out.println("True Type Collections");

        try (Document document = new Document(); BufferedWriter out = new BufferedWriter(
                new FileWriter("msgothic.txt"))) {
            String[] names = BaseFont.enumerateTTCNames("c:\\windows\\fonts\\msgothic.ttc");
            for (int i = 0; i < names.length; i++) {
                out.write("font " + i + ": " + names[i]);
                out.write(System.lineSeparator());
            }

            // Step 2: creation of the writer
            PdfWriter.getInstance(document, new FileOutputStream("truetypecollections.pdf"));

            // Step 3: we open the document
            document.open();

            // Step 4: we add content to the document
            BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\msgothic.ttc,1", BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            System.out.println("postscriptname: " + bf.getPostscriptFontName());
            Font font = new Font(bf, 16);
            String text1 = "\u5951\u7d04\u8005\u4f4f\u6240\u30e9\u30a4\u30f3\uff11";
            String text2 = "\u5951\u7d04\u8005\u96fb\u8a71\u756a\u53f7";
            document.add(new Paragraph(text1, font));
            document.add(new Paragraph(text2, font));
        } catch (DocumentException | IOException de) {
            // Log l'errore invece di stamparlo direttamente
            logger.log(Level.SEVERE, "An error occurred while processing the document.", de);
        }
    }
}
