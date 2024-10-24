/*
 * $Id: ExtractAttachments.java 3712 2009-02-20 20:11:31Z xlv $
 * Copyright (c) 2005-2007 Paulo Soares, Bruno Lowagie, Carsten Hammer
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
 * Paulo Soares and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNameTree;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you extract the attachments of a PDF.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ExtractAttachments extends AbstractTool {

    public static final String EXCEPTION_OCCURED = "Exception occured";

    static {
        addVersion("$Id: ExtractAttachments.java 3712 2009-02-20 20:11:31Z xlv $");
    }

    static Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.ExtractAttachments.class.getName());

    /**
     * Constructs a ExtractAttachements object.
     */
    private static final String SRCFILE_ARGUMENT_NAME = "srcfile";
    public ExtractAttachments() {
        FileArgument f = new FileArgument(this, SRCFILE_ARGUMENT_NAME,
                "The file you want to operate on", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
    }

    /**
     * Unpacks a file attachment.
     *
     * @param filespec The dictionary containing the file specifications
     * @param outPath  The path where the attachment has to be written
     * @throws IOException on error
     */
    public static void unpackFile(PdfDictionary filespec,
            String outPath) throws IOException, PDFFilterException {
        if (filespec == null) {
            return;
        }
        PdfName type = filespec.getAsName(PdfName.TYPE_CONST);
        if (!PdfName.F.equals(type) && !PdfName.FILESPEC.equals(type)) {
            return;
        }
        PdfDictionary ef = filespec.getAsDict(PdfName.EF);
        if (ef == null) {
            return;
        }
        PdfString fn = filespec.getAsString(PdfName.F);
        String stringToLog = "Unpacking file '" + fn + "' to " + outPath;
        logger.info(stringToLog);
        if (fn == null) {
            return;
        }
        File fLast = new File(fn.toUnicodeString());
        File fullPath = new File(outPath, fLast.getName());
        if (fullPath.exists()) {
            return;
        }
        PRStream prs = (PRStream) PdfReader.getPdfObject(ef.get(PdfName.F));
        if (prs == null) {
            return;
        }
        byte[] b = PdfReader.getStreamBytes(prs);
        try (FileOutputStream fout = new FileOutputStream(fullPath)) {
            fout.write(b);
        } catch (IOException e) {
            logger.severe(EXCEPTION_OCCURED);
        }
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("ExtractAttachments", true, false,
                true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== ExtractAttachments OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            validateSourceFile();
            File src = (File) getValue(SRCFILE_ARGUMENT_NAME);
            String outPath = getOutputPath(src);

            extractedTryCatch(src, outPath);
        } catch (InstantiationException e) {
            logger.severe(EXCEPTION_OCCURED);
        }
    }

    private void extractedTryCatch(File src, String outPath) {
        try (PdfReader reader = new PdfReader(src.getAbsolutePath())) {
            processEmbeddedFiles(reader, outPath);
            processAnnotations(reader, outPath);
        } catch (PDFFilterException | IOException e) {
            logger.severe(EXCEPTION_OCCURED);
        }
    }


    private void validateSourceFile() throws InstantiationException {
        if (getValue(SRCFILE_ARGUMENT_NAME) == null) {
            throw new InstantiationException("You need to choose a sourcefile");
        }
    }

    private String getOutputPath(File src) {
        File parentFile = src.getParentFile();
        return (parentFile != null) ? parentFile.getAbsolutePath() : "";
    }

    private void processEmbeddedFiles(PdfReader reader, String outPath) throws IOException, PDFFilterException {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
        if (names != null) {
            PdfDictionary embFiles = names.getAsDict(new PdfName("EmbeddedFiles"));
            if (embFiles != null) {
                HashMap<String, PdfObject> embMap = PdfNameTree.readTree(embFiles);
                for (PdfObject pdfObject : embMap.values()) {
                    PdfDictionary filespec = (PdfDictionary) PdfReader.getPdfObject(pdfObject);
                    unpackFile(filespec, outPath);
                }
            }
        }
    }

    private void processAnnotations(PdfReader reader, String outPath) throws IOException, PDFFilterException {
        for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
            PdfArray annots = reader.getPageN(k).getAsArray(PdfName.ANNOTS);
            if (annots == null) {
                continue;
            }
            for (PdfObject pdfObject : annots.getElements()) {
                PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject(pdfObject);
                if (PdfName.FILEATTACHMENT.equals(annot.getAsName(PdfName.SUBTYPE))) {
                    PdfDictionary filespec = annot.getAsDict(PdfName.FS);
                    unpackFile(filespec, outPath);
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
        throw new InstantiationException("There is more than one destfile.");
    }

}
