/*
 * $Id: InspectPDF.java 3826 2009-03-31 17:46:18Z blowagie $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class was originally published under the MPL by Bruno Lowagie
 * and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to inspect an existing PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class InspectPDF extends AbstractTool {

    private static final Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.InspectPDF.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String OWNERPASSWORD = "ownerpassword";

    static {
        addVersion("$Id: InspectPDF.java 3826 2009-03-31 17:46:18Z blowagie $");
    }

    /**
     * Constructs an InpectPDF object.
     */
    public InspectPDF() {
        arguments.add(new FileArgument(this, SRCFILE, "The file you want to inspect", false, new PdfFilter()));
        arguments.add(new StringArgument(this, OWNERPASSWORD, "The owner password if the file is encrypt"));
    }

    /**
     * Inspects an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            logger.severe("Invalid arguments provided. Please refer to the usage guidelines.");
            System.exit(1); // Exit the program to avoid unintended behavior
        }

        com.lowagie.toolbox.plugins.InspectPDF tool = new com.lowagie.toolbox.plugins.InspectPDF();
        tool.setMainArguments(args);
        tool.execute();
    }



    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Pdf Information", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Pdf Information OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        String stringToLog;
        try {
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            if (getValue(OWNERPASSWORD) == null) {
                reader = new PdfReader(((File) getValue(SRCFILE)).getAbsolutePath());
            } else {
                reader = new PdfReader(((File) getValue(SRCFILE)).getAbsolutePath(),
                        ((String) getValue(OWNERPASSWORD)).getBytes());
            }
            // Some general document information and page size
            logger.info("=== Document Information ===");
            logger.info("PDF Version: " + reader.getPdfVersion());
            logger.info("Number of pages: " + reader.getNumberOfPages());
            logger.info("Number of PDF objects: " + reader.getXrefSize());
            logger.info("File length: " + reader.getFileLength());
            logger.info("Encrypted? " + reader.isEncrypted());
            if (reader.isEncrypted()) {
                stringToLog = "Permissions: " + PdfEncryptor.getPermissionsVerbose(reader.getPermissions());
                logger.info(stringToLog);
                logger.info("128 bit? " + reader.is128Key());
            }
            logger.info("Rebuilt? " + reader.isRebuilt());
            // Some metadata
            logger.info("=== Metadata ===");
            Map<String, String> info = reader.getInfo();
            String key;
            String value;
            for (Map.Entry<String, String> entry : info.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                stringToLog = key + ": " + value;
                logger.info(stringToLog);
            }
            if (reader.getMetadata() == null) {
                logger.info("There is no XML Metadata in the file");
            } else {
                stringToLog = "XML Metadata: " + new String(reader.getMetadata());
                logger.info(stringToLog);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "An unexpected error occurred during execution.", e);
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (Exception e) {
                    //da vedere come effettuare il log
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
        throw new InstantiationException("There is no file to show.");
    }

}
