/*
 * $Id: Bookmarks2XML.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Hans-Werner Hilse, Bruno Lowagie
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
 * and Hans-Werner Hilse.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
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
public class Bookmarks2XML extends AbstractTool {

    private static final Logger logger = Logger.getLogger(Bookmarks2XML.class.getName());
    public static final String PDFFILE = "pdffile";
    public static final String XMLFILE = "xmlfile";

    static {
        addVersion("$Id: Bookmarks2XML.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs an Bookmarks2XML object.
     */
    public Bookmarks2XML() {
        arguments.add(new FileArgument(this, PDFFILE, "the PDF from which you want to extract bookmarks", false,
                new PdfFilter()));
        arguments.add(new FileArgument(this, XMLFILE, "the resulting bookmarks file in XML", true));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Bookmarks2XML", true, true, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Bookmarks2XML OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try (PdfReader reader = new PdfReader(((File) getValue(PDFFILE)).getAbsolutePath())){
            if (getValue(XMLFILE) == null) {
                throw new InstantiationException("You need to choose an xml file");
            }
            if (getValue(PDFFILE) == null) {
                throw new InstantiationException("You need to choose a source PDF file");
            }
            reader.consolidateNamedDestinations();
            List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);
            // save them in XML format
            FileOutputStream bmWriter = new FileOutputStream((File) getValue(XMLFILE));
            SimpleBookmark.exportToXML(bookmarks, bmWriter, "UTF-8", false);
            bmWriter.close();
        } catch (Exception e) {
            //da vedere come effettuare il log
            JOptionPane.showMessageDialog(internalFrame,
                    e.getMessage(),
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            logger.severe(e.getMessage());
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
