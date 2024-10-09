/*
 * $Id: Info.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.general.read;

import com.lowagie.text.pdf.PdfReader;
import org.apache.fop.pdf.PDFFilterException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Getting information from a PDF file.
 */
public class Info {

    /**
     * Getting information from a PDF file
     *
     * @param args the names of paths to PDF files.
     */
    public static void main(String[] args) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("info.txt"))) {
            for (String arg : args) {
                try (PdfReader r = new PdfReader(arg)) {
                    out.write(arg);
                    out.write("\r\n------------------------------------\r\n");
                    out.write(r.getInfo().toString());
                    out.write("\r\n------------------------------------\r\n");
                } catch (IOException | PDFFilterException e) {
                    System.err.println("Error reading PDF file: " + arg + " - " + e.getMessage());
                }
            }
            out.flush();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }


}
