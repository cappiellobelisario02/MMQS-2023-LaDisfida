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
        // Assicurati di avere almeno un argomento
        if (args.length == 0) {
            System.err.println("No PDF files provided.");
            return;
        }

        // Usa try-with-resources per BufferedWriter
        try (BufferedWriter out = new BufferedWriter(new FileWriter("info.txt"))) {
            for (String arg : args) {
                // Utilizza un try-with-resources per PdfReader
                try (PdfReader r = new PdfReader(arg)) {
                    // Scrive il nome del file PDF e le sue informazioni
                    out.write(arg);
                    out.write("\r\n------------------------------------\r\n");
                    out.write(r.getInfo().toString());
                    out.write("\r\n------------------------------------\r\n");
                } catch (IOException | PDFFilterException e) {
                    // Messaggio di errore specifico per ogni file
                    System.err.println("Error reading PDF file: " + arg + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Gestione degli errori di scrittura su file
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

}
