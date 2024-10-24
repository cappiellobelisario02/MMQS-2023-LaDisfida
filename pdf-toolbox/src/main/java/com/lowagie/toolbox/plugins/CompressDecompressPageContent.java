/*
 * $Id: CompressDecompressPageContent.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie
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
 * This class was originally published under the MPL by Bruno Lowagie.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.rups.io.filters.PdfFilter;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class CompressDecompressPageContent extends AbstractTool {

    static {
        addVersion("$Id: CompressDecompressPageContent.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Burst object.
     */
    private static final String SRCFILE_ARG = "srcfile";
    private static final String DEST = "destfile";
    private static final String COMP = "compress";

    private static final Logger logger = Logger.getLogger(CompressDecompressPageContent.class.getName());

    public CompressDecompressPageContent() {
        FileArgument f = new FileArgument(this, SRCFILE_ARG, "The file you want to compress/decompress", false,
                new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
        arguments.add(new FileArgument(this, DEST,
                "The file to which the compressed/decompressed PDF has to be written", true, new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, COMP, COMP);
        oa.addOption("Compress page content", "true");
        oa.addOption("Decompress page content", "false");
        arguments.add(oa);
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Compress/Decompress", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Compress/Decompress OPENED ===");
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
        return (File) getValue(DEST);
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try (PdfReader reader = new PdfReader(((File) getValue(SRCFILE_ARG)).getAbsolutePath())){
            if (getValue(SRCFILE_ARG) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            if (getValue(DEST) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            boolean compress = "true".equals(getValue(COMP));
            synchronized (arguments) {
                Document.compress = compress;
                int total = reader.getNumberOfPages() + 1;
                for (int i = 1; i < total; i++) {
                    reader.setPageContent(i, reader.getPageContent(i));
                }
                Document.compress = true;
            }
        } catch (IOException | PDFFilterException | InstantiationException e) {
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            logger.severe(e.getMessage());
        }
    }
}
