/*
 * $Id: FullFontNames.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts;

import com.lowagie.text.pdf.BaseFont;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Retrieving the full font getName
 */
public class FullFontNames {

    private static final Logger logger = Logger.getLogger(FullFontNames.class.getName());

    /**
     * Retrieving the full font getName
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("fullfontname_arialbi.txt"))) {
            BaseFont bf = BaseFont.createFont("c:\\windows\\fonts\\arialbi.ttf", "winansi", BaseFont.NOT_EMBEDDED);
            out.write("postscriptname: " + bf.getPostscriptFontName());
            out.write("\r\n\r\n");
            String[][] names = bf.getFullFontName();
            out.write("\n\nListing the full font getName:\n\n");
            for (String[] name : names) {
                // Microsoft encoding
                if (name[0].equals("3") && name[1].equals("1")) {
                    out.write(name[3] + "\r\n");
                }
            }
            out.flush(); // Not necessary, since we're using try-with-resources
        } catch (IOException e) {
            // Consider using a logging framework or System.err for error logging
            // Simple error logging for demonstration
            String msg = "Exception: " + e;
            logger.severe(msg);
        }
    }

}
