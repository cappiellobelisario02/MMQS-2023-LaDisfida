/*
 * $Id: XML2Bookmarks.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 by Hans-Werner Hilse.
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.toolbox.plugins;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to add bookmarks to an existing PDF file
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class XML2Bookmarks extends AbstractTool {

    public static final Logger logger = Logger.getLogger(XML2Bookmarks.class.getName());
    public static final String PDFFILE = "pdffile";
    public static final String XMLFILE = "xmlfile";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: XML2Bookmarks.java 3373 2008-05-12 16:21:24Z xlv $");
    }

    /**
     * Constructs an XML2Bookmarks object.
     */
    public XML2Bookmarks() {
        arguments.add(new FileArgument(this, XMLFILE, "the bookmarks in XML", false));
        arguments.add(new FileArgument(this, PDFFILE, "the PDF to which you want to add bookmarks", false,
                new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE, "the resulting PDF", true, new PdfFilter()));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("XML + PDF = PDF", true, true, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== XML2Bookmarks OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        FileOutputStream fouts = null;
        PdfStamper stamper = null;
        try (FileInputStream bmReader = new FileInputStream((File) getValue(XMLFILE));
                PdfReader reader = new PdfReader(((File) getValue(PDFFILE)).getAbsolutePath())) {

            if (getValue(XMLFILE) == null) {
                throw new InstantiationException("You need to choose an xml file");
            }
            if (getValue(PDFFILE) == null) {
                throw new InstantiationException("You need to choose a source PDF file");
            }
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination PDF file");
            }

            List<Map<String, Object>> bookmarks = SimpleBookmark.importFromXML(bmReader);
            reader.consolidateNamedDestinations();

            fouts = new FileOutputStream((File) getValue(DESTFILE));
            stamper = new PdfStamper(reader, fouts);
            stamper.setOutlines(bookmarks);
            stamper.setViewerPreferences(reader.getSimpleViewerPreferences() | PdfWriter.PAGE_MODE_USE_OUTLINES);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(internalFrame, "File not found: " + e.getMessage(),
                    e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.severe("File not found: " + e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(internalFrame, "I/O error: " + e.getMessage(),
                    e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.severe("I/O error: " + e.getMessage());
        } catch (DocumentException e) {
            JOptionPane.showMessageDialog(internalFrame, "Document error: " + e.getMessage(),
                    e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.severe("Document error: " + e.getMessage());
        } catch (InstantiationException e) {
            JOptionPane.showMessageDialog(internalFrame, "Instantiation error: " + e.getMessage(),
                    e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.severe("Instantiation error: " + e.getMessage());
        } catch (PDFFilterException e) {
            JOptionPane.showMessageDialog(internalFrame,
                    "Unexpected error: " + e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            logger.severe("Unexpected error: " + e.getMessage());
        } finally {
            if (fouts != null) {
                try {
                    fouts.close();
                } catch (IOException e) {
                    logger.severe("Failed to close FileOutputStream: " + e.getMessage());
                }
            }
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (IOException | NoSuchAlgorithmException e) {
                    logger.severe("Failed to close PdfStamper: " + e.getMessage());
                }
            }
        }
    }



    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DESTFILE);
    }

}
