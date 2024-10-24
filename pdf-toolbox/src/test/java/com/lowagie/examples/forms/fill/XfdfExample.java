/*
 * $Id: XfdfExample.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.forms.fill;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.XfdfReader;
import org.apache.fop.pdf.PDFFilterException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * How to merge an XFDF file with a PDF form.
 */
public class XfdfExample {

    private static final Logger logger = Logger.getLogger(XfdfExample.class.getName());

    /**
     * Merges an XFDF file with a PDF form.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            // merging the FDF file
            PdfReader pdfreader = new PdfReader("SimpleRegistrationForm.pdf");
            PdfStamper stamp = new PdfStamper(pdfreader, new FileOutputStream("registered_xfdf.pdf"));
            XfdfReader fdfreader = new XfdfReader("register.xfdf");
            AcroFields form = stamp.getAcroFields();
            form.setFields(fdfreader);
            stamp.close();
        } catch (IOException | PDFFilterException | NoSuchAlgorithmException e) {
            logger.severe("Exception occured");
        }

    }
}
