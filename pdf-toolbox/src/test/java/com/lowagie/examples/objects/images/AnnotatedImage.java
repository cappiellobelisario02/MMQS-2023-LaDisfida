/*
 * $Id: AnnotatedImage.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.images;

import com.lowagie.text.Annotation;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Images with an annotation.
 */
public class AnnotatedImage {

    private static final Logger logger = Logger.getLogger(AnnotatedImage.class.getName());

    /**
     * Adds some annotated images to a PDF file.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("images and annotations");

        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document, new FileOutputStream("annotated_images.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we add some content
            Image jpeg = Image.getInstance("otsoe.jpg");
            jpeg.setAnnotation(new Annotation("picture", "This is my dog", 0, 0, 0, 0));
            jpeg.setAbsolutePosition(100f, 550f);
            document.add(jpeg);
            Image wmf = Image.getInstance("iText.wmf");
            wmf.setAnnotation(new Annotation(0, 0, 0, 0, "http://www.lowagie.com/iText"));
            wmf.setAbsolutePosition(100f, 200f);
            document.add(wmf);
        } catch (IOException | DocumentException de) {
            logger.severe("Exception occured");
        }

        // step 5: we close the document
        document.close();
    }

}
