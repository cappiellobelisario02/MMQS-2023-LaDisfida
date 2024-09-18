/*
 * $Id: Decrypt.java 3271 2008-04-18 20:39:42Z xlv $
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

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to decrypt an existing PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Decrypt extends AbstractTool {

    static Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.Decrypt.class.getName());

    static {
        addVersion("$Id: Decrypt.java 3271 2008-04-18 20:39:42Z xlv $");
    }


    /**
     * Constructs a Decrypt object.
     */
    private static final String SRCFILE_ARG = "srcfile";
    private static final String DEST = "destfile";
    private static final String OP = "ownerpassword";
    public Decrypt() {
        arguments.add(new FileArgument(this, SRCFILE_ARG, "The file you want to decrypt", false, new PdfFilter()));
        arguments.add(new FileArgument(this, DEST, "The file to which the decrypted PDF has to be written", true,
                new PdfFilter()));
        arguments.add(new StringArgument(this, OP, "The ownerpassword you want to add to the PDF file"));
    }

    /**
     * Decrypts an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        com.lowagie.toolbox.plugins.Decrypt tool = new com.lowagie.toolbox.plugins.Decrypt();
        if (args.length < 2) {
            logger.info(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Decrypt", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Decrypt OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        FileOutputStream fos = null;
        try {
            if (getValue(SRCFILE_ARG) == null) {
                throw new InstantiationException("You need to choose a source file");
            }
            if (getValue(DEST) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            byte[] ownerpassword = null;
            if (getValue(OP) != null) {
                ownerpassword = ((String) getValue(OP)).getBytes();
            }

            // Extracted the PdfReader initialization logic to a new method
            reader = initializePdfReader((File) getValue(SRCFILE_ARG), ownerpassword);
            fos = createFileOutputStream((File) getValue(DEST));
            PdfStamper stamper = new PdfStamper(reader, fos);
            stamper.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            logger.info(e.getMessage());
        } finally {
            if (reader != null && fos != null) {
                try {
                    reader.close();
                    fos.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    // New method to initialize PdfReader
    private PdfReader initializePdfReader(File sourceFile, byte[] ownerpassword) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(sourceFile.getAbsolutePath(), ownerpassword);
            // You can add more processing logic here if needed...
        } catch (Exception e) {
            //e.printStackTrace();  // Handle the exception or log it as necessary
        }
        return reader;
    }
    private FileOutputStream createFileOutputStream(File destFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(destFile);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return fos;
    }


    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the command line
            return;
        }
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DEST);
    }

}
