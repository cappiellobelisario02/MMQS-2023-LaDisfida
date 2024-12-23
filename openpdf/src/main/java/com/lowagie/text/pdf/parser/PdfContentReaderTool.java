/*
 * Copyright 2008 by Kevin Day.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import org.apache.commons.io.FilenameUtils;
import org.apache.fop.pdf.PDFFilterException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Tool that parses the content of a PDF document.
 *
 * @since 2.1.4
 */
@SuppressWarnings("WeakerAccess")
public class PdfContentReaderTool {

    public static final String STDOUT = "stdout";
    static Logger logger = Logger.getLogger(PdfContentReaderTool.class.getName());

    /**
     * Shows the detail of a dictionary. This is similar to the PdfLister functionality.
     *
     * @param dic the dictionary of which you want the detail
     * @return a String representation of the dictionary
     */
    public static String getDictionaryDetail(PdfDictionary dic) {
        return getDictionaryDetail(dic, 0);
    }

    /**
     * Shows the detail of a dictionary.
     *
     * @param dic   the dictionary of which you want the detail
     * @param depth the depth of the current dictionary (for nested dictionaries)
     * @return a String representation of the dictionary
     */
    public static String getDictionaryDetail(PdfDictionary dic, int depth) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        List<PdfName> subDictionaries = new ArrayList<>();
        for (PdfName key : dic.getKeys()) {
            PdfObject val = dic.getDirectObject(key);
            if (val.isDictionary()) {
                subDictionaries.add(key);
            }
            builder.append(key);
            builder.append('=');
            builder.append(val);
            builder.append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(')');
        PdfName pdfSubDictionaryName;
        for (PdfName subDictionary : subDictionaries) {
            pdfSubDictionaryName = subDictionary;
            builder.append('\n');
            builder.append("\t".repeat(Math.max(0, depth + 1)));
            builder.append("Subdictionary ");
            builder.append(pdfSubDictionaryName);
            builder.append(" = ");
            builder.append(getDictionaryDetail(
                    dic.getAsDict(pdfSubDictionaryName), depth + 1));
        }
        return builder.toString();
    }

    /**
     * Writes information about a specific page from PdfReader to the specified output stream.
     *
     * @param reader  the PdfReader to read the page content from
     * @param pageNum the page number to read
     * @param out     the output stream to send the content to
     * @throws IOException thrown when an I/O operation goes wrong
     * @since 2.1.5
     */
    public static void listContentStreamForPage(PdfReader reader, int pageNum, PrintWriter out)
            throws IOException, PDFFilterException {
        out.println("==============Page " + pageNum + "====================");
        out.println("- - - - - Dictionary - - - - - -");
        PdfDictionary pageDictionary = reader.getPageN(pageNum);
        out.println(getDictionaryDetail(pageDictionary));
        out.println("- - - - - Content Stream - - - - - -");
        RandomAccessFileOrArray f = reader.getSafeFile();

        byte[] contentBytes = reader.getPageContent(pageNum, f);
        f.close();

        InputStream is = new ByteArrayInputStream(contentBytes);
        int ch;
        while ((ch = is.read()) != -1) {
            out.print((char) ch);
        }

        out.println("\n- - - - - Text Extraction - - - - - -");
        PdfTextExtractor extractor = new PdfTextExtractor(reader,
                new MarkedUpTextAssembler(reader));
        String extractedText = extractor.getTextFromPage(pageNum);
        if (!extractedText.isEmpty()) {
            out.println(extractedText);
        } else {
            out.println("No text found on page " + pageNum);
        }

        out.println();

    }

    /**
     * Writes information about each page in a PDF file to the specified output stream.
     *
     * @param pdfFile a File instance referring to a PDF file
     * @param out     the output stream to send the content to
     * @throws IOException thrown when an I/O operation goes wrong
     * @since 2.1.5
     */
    public static void listContentStream(File pdfFile, PrintWriter out) throws IOException, PDFFilterException {
        try(PdfReader reader = new PdfReader(pdfFile.getCanonicalPath())){

            int maxPageNum = reader.getNumberOfPages();

            for (int pageNum = 1; pageNum <= maxPageNum; pageNum++) {
                listContentStreamForPage(reader, pageNum, out);
            }
        } catch (PDFFilterException e) {
            throw new PDFFilterException();
        }
    }

    /**
     * Writes information about the specified page in a PDF file to the specified output stream.
     *
     * @param pdfFile a File instance referring to a PDF file
     * @param pageNum the page number to read
     * @param out     the output stream to send the content to
     * @throws IOException thrown when an I/O operation goes wrong
     * @since 2.1.5
     */
    public static void listContentStream(File pdfFile, int pageNum,
            PrintWriter out) throws IOException, PDFFilterException {
        PdfReader reader = new PdfReader(pdfFile.getCanonicalPath());

        listContentStreamForPage(reader, pageNum, out);
    }

    /**
     * Writes information about each page in a PDF file to the specified file, or System.out.
     *
     */
    public void processPdf(String pdfFilePath, String outputFilePath, Integer pageNum) throws IOException {
        try (PrintWriter writer = new PrintWriter(System.out)) {
            // Validate input
            if (pdfFilePath == null || pdfFilePath.isEmpty()) {
                logger.info("Invalid PDF file path");
                return;
            }

            handleContentStreaming(pdfFilePath);  // Assuming you have a method for this

            // Validate the PDF file path to prevent path manipulation
            File pdfFile = validatePath(pdfFilePath);

            // Check if specific page content needs to be processed
            if (pageNum == null) {
                listContentStream(pdfFile, writer);
            } else {
                listContentStream(pdfFile, pageNum, writer);
            }
            writer.flush();

            if (outputFilePath != null && !outputFilePath.isEmpty()) {
                String stringToLog = "Finished writing content to " + outputFilePath;
                logger.info(stringToLog);
            }
        } catch (PDFFilterException e) {
            logger.severe("An error occurred");
        }
    }

    // Helper method to validate and sanitize file paths
    private static File validatePath(String path) throws IOException {
        // Rimuovi eventuali spazi bianchi
        path = path.trim();

        // Usa Paths per convertire in un percorso sicuro
        Path filePath = Paths.get(path).normalize();

        // Controlla se il percorso è assoluto o contiene ".."
        if (filePath.isAbsolute() || filePath.toString().contains("..")) {
            throw new SecurityException("Invalid path: " + path);
        }

        // Ottieni il percorso canonico del file e quello della directory base
        File file = filePath.toFile();
        String canonicalPath = file.getCanonicalPath();
        String basePath = new File(".").getCanonicalPath();

        // Controlla se il percorso canonico inizia con il percorso di base
        if (!canonicalPath.startsWith(basePath)) {
            throw new SecurityException("Path manipulation attempt detected: " + path);
        }

        // Controlla se è un file e non una directory
        if (Files.isDirectory(filePath)) {
            throw new SecurityException("Path points to a directory, not a file: " + path);
        }

        // Controlla l'estensione del file
        if (!canonicalPath.endsWith(".pdf")) {
            throw new SecurityException("Invalid file extension: " + path);
        }

        return file;
    }


    private static void handleContentStreaming(String args) {

        String args1StringCured = FilenameUtils.normalize(args);


        try (PrintWriter writer = !args1StringCured.equalsIgnoreCase(STDOUT)
                ? new PrintWriter(new FileOutputStream(args1StringCured))
                : new PrintWriter(System.out)) {

            if (!args.equalsIgnoreCase(STDOUT)) {
                String stringToLog = "Writing PDF content to " + args;
                logger.info(stringToLog);
            }

            int pageNum = -1;
            if (args.length() >= 3) {
                pageNum = Integer.parseInt(args);
            }

            String argsToFile = FilenameUtils.normalize(args);
            File fileInput = new File(argsToFile);

            if (pageNum == -1) {
                listContentStream(fileInput, writer);
            } else {
                listContentStream(fileInput, pageNum, writer);
            }
            writer.flush();

            if (!args.equalsIgnoreCase(STDOUT)) {
                String stringToLog = "Finished writing content to " + args;
                logger.info(stringToLog);
            }
        } catch (PDFFilterException | IOException e) {
            logger.info(e.getMessage());
        }
    }
}
