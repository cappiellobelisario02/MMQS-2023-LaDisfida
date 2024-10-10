/*
 * $Id: HandoutPdf.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2002 by Bruno Lowagie
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.tools;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;

/**
 * Takes an existing PDF file and makes handouts.
 *
 * @since 2.1.1 (renamed to follow Java naming conventions)
 */
public class HandoutPdf {

    public static final Logger logger = Logger.getLogger(HandoutPdf.class.getName());

    /**
     * Makes handouts based on an existing PDF file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            logger.severe("arguments: srcfile destfile pages");
            return;
        }

        try {
            int pages = validatePages(args[2]);

            float[] xCoordinates = {30f, 280f, 320f, 565f};
            float[][] yCoordinates = calculateYCoordinates(pages);

            processDocument(args[0], args[1], pages, xCoordinates, yCoordinates);
        } catch (Exception e) {
            logger.severe(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static int validatePages(String pagesArg) throws DocumentException {
        int pages = Integer.parseInt(pagesArg);
        if (pages < 2 || pages > 8) {
            throw new DocumentException(MessageLocalization.getComposedMessage(
                    "you.can.t.have.1.pages.on.one.page.minimum.2.maximum.8", pages));
        }
        return pages;
    }

    private static float[][] calculateYCoordinates(int pages) {
        float height = (778f - (20f * (pages - 1))) / pages;
        float[] y1 = new float[pages];
        float[] y2 = new float[pages];

        y1[0] = 812f;
        y2[0] = 812f - height;

        for (int i = 1; i < pages; i++) {
            y1[i] = y2[i - 1] - 20f;
            y2[i] = y1[i] - height;
        }

        return new float[][] {y1, y2};
    }

    private static void processDocument(String srcFile, String destFile, int pages,
            float[] xCoordinates, float[][] yCoordinates)
            throws DocumentException {
        // Validate and sanitize the destination file path
        File destFilePath = new File(destFile);

        // Check if the destination file is a valid path and inside a safe directory
        if (!destFilePath.getAbsolutePath().startsWith(new File("safe_directory").getAbsolutePath())) {
            throw new IllegalArgumentException("Destination path is not allowed.");
        }

        // Ensure the parent directory exists
        File parentDir = destFilePath.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs(); // Create the directory if it does not exist
        }

        // Use try-with-resources for automatic resource management
        try (PdfReader reader = new PdfReader(srcFile);
                Document document = new Document(PageSize.A4);
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destFilePath))) {

            int totalPages = reader.getNumberOfPages();
            String stringToLog = "There are " + totalPages + " pages in the original file.";
            logger.info(stringToLog);
            document.open();
            PdfContentByte cb = writer.getDirectContent();

            int p = 0;
            for (int i = 1; i <= totalPages; i++) {
                processPage(reader, writer, cb, i, p, xCoordinates, yCoordinates);
                p++;
                if (p == pages) {
                    p = 0;
                    document.newPage();
                }
            }
        } catch (Exception e) {
            logger.severe("Error processing document: " + e.getMessage());
            // Handle exception as needed
        }
    }

    private static void processPage(PdfReader reader, PdfWriter writer, PdfContentByte cb,
            int pageIndex, int p, float[] xCoordinates, float[][] yCoordinates) throws DocumentException {
        Rectangle rect = reader.getPageSizeWithRotation(pageIndex);
        float factor = calculateScaleFactor(rect, xCoordinates, yCoordinates, p);
        float dx = calculateDx(rect, xCoordinates, factor);
        float dy = calculateDy(rect, yCoordinates, p, factor);

        PdfImportedPage page = writer.getImportedPage(reader, pageIndex);
        int rotation = reader.getPageRotation(pageIndex);

        float[] rotationAndFactor = {rotation, factor};
        float[] coordinates = {dx, dy, xCoordinates[0], xCoordinates[2]}; // includes xCoordinates[0] and xCoordinates[2]

        placePage(cb, page, rotationAndFactor, coordinates, yCoordinates, p);
        drawRectangles(cb, coordinates, yCoordinates, p, rect, new float[]{factor, dx, dy});

        String stringToLog = "Processed page " + pageIndex;
        logger.info(stringToLog);
    }


    private static float calculateScaleFactor(Rectangle rect, float[] xCoordinates, float[][] yCoordinates, int p) {
        float factorx = (xCoordinates[1] - xCoordinates[0]) / rect.getWidth();
        float factory = (yCoordinates[0][p] - yCoordinates[1][p]) / rect.getHeight();
        return Math.min(factorx, factory);
    }

    private static float calculateDx(Rectangle rect, float[] xCoordinates, float factor) {
        return (xCoordinates[1] - xCoordinates[0] - rect.getWidth() * factor) / 2f;
    }

    private static float calculateDy(Rectangle rect, float[][] yCoordinates, int p, float factor) {
        return (yCoordinates[0][p] - yCoordinates[1][p] - rect.getHeight() * factor) / 2f;
    }

    private static void placePage(PdfContentByte cb, PdfImportedPage page, float[] rotationAndFactor,
            float[] coordinates, float[][] yCoordinates, int p) {
        float rotation = rotationAndFactor[0];
        float factor = rotationAndFactor[1];
        float dx = coordinates[0];
        float dy = coordinates[1];

        if (rotation == 90 || rotation == 270) {
            cb.addTemplate(page, 0, -factor, factor, 0, coordinates[2] + dx, yCoordinates[1][p] + dy + page.getWidth() * factor);
        } else {
            cb.addTemplate(page, factor, 0, 0, factor, coordinates[2] + dx, yCoordinates[1][p] + dy);
        }
    }

    private static void drawRectangles(PdfContentByte cb, float[] coordinates,
            float[][] yCoordinates, int p, Rectangle rect, float[] factorAndOffset) {
        float factor = factorAndOffset[0];
        float dx = factorAndOffset[1];
        float dy = factorAndOffset[2];

        cb.setRGBColorStroke(0xC0, 0xC0, 0xC0);
        cb.rectangle(coordinates[2] - 5f, yCoordinates[1][p] - 5f, coordinates[3] - coordinates[2] + 10f, yCoordinates[0][p] - yCoordinates[1][p] + 10f);
        for (float l = yCoordinates[0][p] - 19; l > yCoordinates[1][p]; l -= 16) {
            cb.moveTo(coordinates[2], l);
            cb.lineTo(coordinates[3], l);
        }
        cb.rectangle(coordinates[0] + dx, yCoordinates[1][p] + dy, rect.getWidth() * factor, rect.getHeight() * factor);
        cb.stroke();
    }

}
