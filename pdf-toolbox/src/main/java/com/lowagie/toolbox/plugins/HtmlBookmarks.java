/*
 * $Id: HtmlBookmarks.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 by Bruno Lowagie.
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

import com.lowagie.text.Anchor;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Header;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.tools.Executable;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Allows you to generate an index file in HTML containing Bookmarks to an existing PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class HtmlBookmarks extends AbstractTool {

    public static final String SRCFILE = "srcfile";
    public static final String OWNERPASSWORD = "ownerpassword";
    public static final String TITLE = "Title";

    static {
        addVersion("$Id: HtmlBookmarks.java 3373 2008-05-12 16:21:24Z xlv $");
    }

    static Logger logger = Logger.getLogger(HtmlBookmarks.class.getName());

    /**
     * Constructs an HtmlBookmarks object.
     */
    public HtmlBookmarks() {
        arguments.add(new FileArgument(this, SRCFILE, "The file you want to inspect", false, new PdfFilter()));
        arguments.add(new StringArgument(this, OWNERPASSWORD, "The owner password if the file is encrypt"));
        arguments.add(new StringArgument(this, "css", "The path to a CSS file"));
    }

    /**
     * Recursive method to write Bookmark titles to the System.out.
     *
     * @param pdf      the path to the PDF file
     * @param section  the section to which the bookmarks should be added
     * @param bookmark a Map containing a Bookmark (and possible kids)
     */
    private static void addBookmark(String pdf, Section section, Map<String, Object> bookmark) {
        Section s = createBookmark(pdf, section, bookmark);
        // As long as the Definition of mapList and kids is recursive, we have to work with unchecked casts
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> kids = (List<Map<String, Object>>) bookmark.get("Kids");
        if (kids == null) {
            return;
        }
        for (Map<String, Object> kid : kids) {
            addBookmark(pdf, s, kid);
        }
    }

    /**
     * Adds a line with the title and an anchor.
     *
     * @param pdf      the link to the PDF file
     * @param section  the section that gets the line
     * @param bookmark the bookmark that has the data for the line
     * @return a subsection of section
     */
    private static Section createBookmark(String pdf, Section section, Map<String, Object> bookmark) {
        Section s;
        Paragraph title = new Paragraph((String) bookmark.get(TITLE));
        logger.info(bookmark.get(TITLE).toString());
        String action = (String) bookmark.get("Action");
        if ("GoTo".equals(action)) {
            if (bookmark.get("Page") != null) {
                String page = (String) bookmark.get("Page");
                StringTokenizer tokens = new StringTokenizer(page);
                String token = tokens.nextToken();
                Anchor anchor = new Anchor(" page" + token);
                anchor.setReference(pdf + "#page=" + token);
                title.add(anchor);
            }
        } else if ("URI".equals(action)) {
            String url = (String) bookmark.get("URI");
            Anchor anchor = new Anchor(" Goto URL");
            anchor.setReference(url);
            title.add(anchor);
        } else if ("GoToR".equals(action)) {
            String remote = (String) bookmark.get("File");
            Anchor anchor = new Anchor(" goto " + remote);
            if (bookmark.get("Named") != null) {
                String named = (String) bookmark.get("Named");
                remote = remote + "#nameddest=" + named;
            } else if (bookmark.get("Page") != null) {
                String page = (String) bookmark.get("Page");
                StringTokenizer tokens = new StringTokenizer(page);
                String token = tokens.nextToken();
                anchor.add(new Chunk(" page " + token));
                remote = remote + "#page=" + token;
            }
            anchor.setReference(remote);
            title.add(anchor);
        }
        if (section == null) {
            s = new Chapter(title, 0);
        } else {
            s = section.addSection(title);
        }
        s.setNumberDepth(0);
        return s;
    }

    /**
     * Allows you to generate an index file in HTML containing Bookmarks to an existing PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        com.lowagie.toolbox.plugins.HtmlBookmarks tool = new com.lowagie.toolbox.plugins.HtmlBookmarks();
        if (args.length < 1) {
            logger.info(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Html Bookmarks", true, true, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Html Bookmarks OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        try (Document document = new Document()) {
            validateSourceFile();
            File src = (File) getValue(SRCFILE);
            reader = initializePdfReader(src);
            File html = prepareHtmlFile(src);
            HtmlWriter.getInstance(document, new FileOutputStream(html));
            addStylesheet(document);
            addDocumentMetadata(document, reader, src);
            document.open();
            addContent(document, reader, src);
            Executable.launchBrowser(html.getAbsolutePath());
        } catch (Exception e) {
            handleException(e);
        } finally {
            closeReader(reader);
        }
    }

    private void validateSourceFile() throws InstantiationException {
        if (getValue(SRCFILE) == null) {
            throw new InstantiationException("You need to choose a sourcefile");
        }
    }

    private PdfReader initializePdfReader(File src)
            throws InstantiationException, IOException, PDFFilterException {
        PdfReader result;
        if (getValue(OWNERPASSWORD) == null) {
            result = new PdfReader(src.getAbsolutePath());
        } else {
            result = new PdfReader(src.getAbsolutePath(), ((String) getValue(OWNERPASSWORD)).getBytes());
        }
        return result;
    }

    private File prepareHtmlFile(File src) {
        File directory = src.getParentFile();
        String name = src.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return new File(directory, name + "_index.html");
    }

    private void addStylesheet(Document document) throws InstantiationException {
        Object css = getValue("css");
        if (css != null) {
            document.add(new Header(HtmlTags.STYLESHEET, css.toString()));
        }
    }

    private void addDocumentMetadata(Document document, PdfReader reader, File src) {
        String title = reader.getInfo().get(TITLE);
        if (title == null) {
            document.addTitle("Index for " + src.getName());
        } else {
            document.addKeywords("Index for '" + title + "'");
        }
        String keywords = reader.getInfo().get("Keywords");
        if (keywords != null) {
            document.addKeywords(keywords);
        }
        String description = reader.getInfo().get("Subject");
        if (description != null) {
            document.addSubject(description);
        }
    }

    private void addContent(Document document, PdfReader reader, File src) {
        String title = reader.getInfo().get(TITLE);
        Paragraph titleParagraph = new Paragraph(title == null ? "Index for " + src.getName() : "Index for '" + title + "'");
        document.add(titleParagraph);

        String description = reader.getInfo().get("Subject");
        if (description != null) {
            document.add(new Paragraph(description));
        }

        List<Map<String, Object>> mapList = SimpleBookmark.getBookmarkList(reader);
        if (mapList == null) {
            document.add(new Paragraph("This document has no bookmarks."));
        } else {
            for (Map<String, Object> map : mapList) {
                Chapter chapter = (Chapter) createBookmark(src.getName(), null, map);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> kids = (List<Map<String, Object>>) map.get("Kids");
                if (kids != null) {
                    for (Map<String, Object> kid : kids) {
                        addBookmark(src.getName(), chapter, kid);
                    }
                }
                document.add(chapter);
            }
        }
    }

    private void handleException(Exception e) {
        JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        logger.info(e.getMessage());
    }

    private void closeReader(PdfReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                //da vedere come effettuare il log
            }
        }
    }


    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // if the internal frame is null, the tool was called from the command line
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
