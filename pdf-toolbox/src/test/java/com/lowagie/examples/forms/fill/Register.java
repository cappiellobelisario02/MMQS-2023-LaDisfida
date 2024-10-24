/*
 * $Id: Register.java 3373 2008-05-12 16:21:24Z xlv $
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
import org.apache.fop.pdf.PDFFilterException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

/**
 * Fill in a simple registration form.
 */
public class Register {

    private static final Logger logger = Logger.getLogger(Register.class.getName());

    /**
     * Reads a form and fills in the fields.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Filling in a form");
        try {
            // we create a reader for a certain document
            PdfStamper stamp2 = getPdfStamper();
            AcroFields form2 = stamp2.getAcroFields();
            form2.setField("name", "Bruno Lowagie");
            form2.setField("address", "Baeyensstraat 121, Sint-Amandsberg");
            form2.setField("postal_code", "BE-9040");
            form2.setField("email", "bruno@lowagie.com");
            stamp2.setFormFlattening(true);
            stamp2.close();
        } catch (IOException | PDFFilterException | NoSuchAlgorithmException de) {
            logger.severe("Exception occured");
        }
    }

    private static PdfStamper getPdfStamper() throws IOException, PDFFilterException, NoSuchAlgorithmException {
        PdfReader reader = new PdfReader("SimpleRegistrationForm.pdf");
        // filling in the form
        PdfStamper stamp1 = new PdfStamper(reader, new FileOutputStream("registered.pdf"));
        AcroFields form1 = stamp1.getAcroFields();
        form1.setField("name", "Bruno Lowagie");
        form1.setField("address", "Baeyensstraat 121, Sint-Amandsberg");
        form1.setField("postal_code", "BE-9040");
        form1.setField("email", "bruno@lowagie.com");
        stamp1.close();
        // filling in the form and flatten
        reader = new PdfReader("SimpleRegistrationForm.pdf");
        return new PdfStamper(reader, new FileOutputStream("registered_flat.pdf"));
    }
}
