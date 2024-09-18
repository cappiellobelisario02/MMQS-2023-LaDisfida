/*
 * $Id: HtmlWriter.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU LIBRARY GENERAL PUBLIC LICENSE for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html;

import static com.lowagie.text.error_messages.MessageLocalization.getComposedMessage;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Header;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.MarkedObject;
import com.lowagie.text.MarkedSection;
import com.lowagie.text.Meta;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Row;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

/**
 * A <CODE>DocWriter</CODE> class for HTML.
 * <p>
 * An <CODE>HtmlWriter</CODE> can be added as a <CODE>DocListener</CODE> to a certain <CODE>Document</CODE> by getting
 * an instance. Every <CODE>Element</CODE> added to the original <CODE>Document</CODE> will be written to the
 * <CODE>OutputStream</CODE> of this <CODE>HtmlWriter</CODE>.
 * <p>
 * Example:
 * <BLOCKQUOTE><PRE>
 * // creation of the document with a certain size and certain margins Document document = new Document(PageSize.A4, 50,
 * 50, 50, 50); try { // this will write HTML to the Standard OutputStream
 * <STRONG>HtmlWriter.getInstance(document, System.out);</STRONG>
 * // this will write HTML to a file called text.html
 * <STRONG>HtmlWriter.getInstance(document, new FileOutputStream("text.html"));</STRONG>
 * // this will write HTML to for instance the OutputStream of a HttpServletResponse-object
 * <STRONG>HtmlWriter.getInstance(document, response.getOutputStream());</STRONG>
 * } catch(DocumentException de) { System.err.println(de.getMessage()); } // this will close the document and all the
 * OutputStreams listening to it
 * <STRONG>document.close();</STRONG>
 * </PRE></BLOCKQUOTE>
 */

public class HtmlWriter extends DocWriter {

    // static membervariables (tags)

    /**
     * This is a possible HTML-tag.
     */
    public static final byte[] BEGINCOMMENT = getISOBytes("<!-- ");

    /**
     * This is a possible HTML-tag.
     */
    public static final byte[] ENDCOMMENT = getISOBytes(" -->");

    /**
     * This is a possible HTML-tag.
     */
    public static final String NBSP = "&nbsp;";

    // membervariables

    /**
     * This is the current font of the HTML.
     */
    protected Stack<Font> currentfont = new Stack<>();

    /**
     * This is the standard font of the HTML.
     */
    protected Font standardfont = new Font();

    /**
     * This is a path for images.
     */
    protected String imagepath = null;

    /**
     * Stores the page number.
     */
    protected int pageN = 0;

    /**
     * This is the textual part of a header
     */
    protected Header header = null;

    /**
     * This is the textual part of the footer
     */
    protected HeaderFooter footer = null;

    /**
     * Store the markup properties of a MarkedObject.
     */
    protected Properties markup = new Properties();

    // constructor

    /**
     * Constructs a <CODE>HtmlWriter</CODE>.
     *
     * @param doc The <CODE>Document</CODE> that has to be written as HTML
     * @param os  The <CODE>OutputStream</CODE> the writer has to write to.
     */

    protected HtmlWriter(Document doc, OutputStream os) {
        super(doc, os);

        document.addDocListener(this);
        this.pageN = document.getPageNumber();
        try {
            os.write(LT);
            os.write(getISOBytes(HtmlTags.HTML));
            os.write(GT);
            os.write(NEWLINE);
            os.write(TAB);
            os.write(LT);
            os.write(getISOBytes(HtmlTags.HEAD));
            os.write(GT);
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    // get an instance of the HtmlWriter

    /**
     * Gets an instance of the <CODE>HtmlWriter</CODE>.
     *
     * @param document The <CODE>Document</CODE> that has to be written
     * @param os       The <CODE>OutputStream</CODE> the writer has to write to.
     * @return a new <CODE>HtmlWriter</CODE>
     */
    public static HtmlWriter getInstance(Document document, OutputStream os) {
        return new HtmlWriter(document, os);
    }

    // implementation of the DocListener methods

    /**
     * Signals that an new page has to be started.
     *
     * @return <CODE>true</CODE> if this action succeeded, <CODE>false</CODE> if not.
     */
    @Override
    public boolean newPage() {
        try {
            writeStart(HtmlTags.DIV);
            write(" ");
            write(HtmlTags.STYLE);
            write("=\"");
            writeCssProperty(Markup.CSS_KEY_PAGE_BREAK_BEFORE, Markup.CSS_VALUE_ALWAYS);
            write("\" /");
            os.write(GT);
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
        return true;
    }

    /**
     * Signals that an <CODE>Element</CODE> was added to the <CODE>Document</CODE>.
     *
     * @param element a high level object that has to be translated to HTML
     * @return <CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
     * @throws DocumentException when a document isn't open yet, or has been closed
     */
    @Override
    public boolean add(Element element) throws DocumentException {
        if (pause) {
            return false;
        }
        if (open && !element.isContent()) {
            throw new DocumentException(
                    getComposedMessage("the.document.is.open.you.can.only.add.elements.with.content"));
        }
        try {
            switch (element.type()) {
                case Element.HEADER:
                    try {
                        Header header = (Header) element;
                        if (HtmlTags.STYLESHEET.equals(header.getName())) {
                            writeLink(header);
                        } else if (HtmlTags.JAVASCRIPT.equals(header.getName())) {
                            writeJavaScript(header);
                        } else {
                            writeHeader(header);
                        }
                    } catch (IOException ioe) {
                        throw new IOException(ioe);
                    }
                    return true;
                case Element.SUBJECT:
                case Element.KEYWORDS:
                case Element.AUTHOR:
                    Meta meta = (Meta) element;
                    writeHeader(meta);
                    return true;
                case Element.TITLE:
                    addTabs(2);
                    writeStart(HtmlTags.TITLE);
                    os.write(GT);
                    addTabs(3);
                    write(HtmlEncoder.encode(((Meta) element).getContent()));
                    addTabs(2);
                    writeEnd(HtmlTags.TITLE);
                    return true;
                case Element.CREATOR:
                    writeComment("Creator: " + HtmlEncoder.encode(((Meta) element).getContent()));
                    return true;
                case Element.PRODUCER:
                    writeComment("Producer: " + HtmlEncoder.encode(((Meta) element).getContent()));
                    return true;
                case Element.CREATIONDATE:
                    writeComment("Creationdate: " + HtmlEncoder.encode(((Meta) element).getContent()));
                    return true;
                case Element.MARKED:
                    if (element instanceof MarkedSection) {
                        MarkedSection ms = (MarkedSection) element;
                        addTabs(1);
                        writeStart(HtmlTags.DIV);
                        writeMarkupAttributes(ms.getMarkupAttributes());
                        os.write(GT);
                        MarkedObject mo = ((MarkedSection) element).getTitle();
                        if (mo != null) {
                            markup = mo.getMarkupAttributes();
                            mo.process(this);
                        }
                        ms.process(this);
                        writeEnd(HtmlTags.DIV);
                        return true;
                    } else {
                        MarkedObject mo = (MarkedObject) element;
                        markup = mo.getMarkupAttributes();
                        return mo.process(this);
                    }
                default:
                    write(element, 2);
                    return true;
            }
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    /**
     * Signals that the <CODE>Document</CODE> has been opened and that
     * <CODE>Elements</CODE> can be added.
     * <p>
     * The <CODE>HEAD</CODE>-section of the HTML-document is written.
     */
    @Override
    public void open() {
        super.open();
        try {
            writeComment(Document.getVersion());
            writeComment("CreationDate: " + new Date().toString());
            addTabs(1);
            writeEnd(HtmlTags.HEAD);
            addTabs(1);
            writeStart(HtmlTags.BODY);
            if (document.leftMargin() > 0) {
                write(HtmlTags.LEFTMARGIN, String.valueOf(document.leftMargin()));
            }
            if (document.rightMargin() > 0) {
                write(HtmlTags.RIGHTMARGIN, String.valueOf(document.rightMargin()));
            }
            if (document.topMargin() > 0) {
                write(HtmlTags.TOPMARGIN, String.valueOf(document.topMargin()));
            }
            if (document.bottomMargin() > 0) {
                write(HtmlTags.BOTTOMMARGIN, String.valueOf(document.bottomMargin()));
            }
            if (pageSize.getBackgroundColor() != null) {
                write(HtmlTags.BACKGROUNDCOLOR, HtmlEncoder.encode(pageSize.getBackgroundColor()));
            }
            if (document.getJavaScript_onLoad() != null) {
                write(HtmlTags.JAVASCRIPT_ONLOAD, HtmlEncoder.encode(document.getJavaScript_onLoad()));
            }
            if (document.getJavaScript_onUnLoad() != null) {
                write(HtmlTags.JAVASCRIPT_ONUNLOAD, HtmlEncoder.encode(document.getJavaScript_onUnLoad()));
            }
            if (document.getHtmlStyleClass() != null) {
                write(Markup.HTML_ATTR_CSS_CLASS, document.getHtmlStyleClass());
            }
            os.write(GT);
            initHeader(); // line added by David Freels
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    /**
     * Signals that the <CODE>Document</CODE> was closed and that no other
     * <CODE>Elements</CODE> will be added.
     */
    @Override
    public void close() {
        try {
            initFooter(); // line added by David Freels
            addTabs(1);
            writeEnd(HtmlTags.BODY);
            os.write(NEWLINE);
            writeEnd(HtmlTags.HTML);
            super.close();
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    // some protected methods

    /**
     * Adds the header to the top of the <CODE>Document</CODE>
     */
    protected void initHeader() {
        if (header != null) {
            try {
                add(header.paragraph());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Adds the header to the top of the <CODE>Document</CODE>
     */
    protected void initFooter() {
        if (footer != null) {
            try {
                // Set the page number. HTML has no notion of a page, so it should always
                // add up to 1
                footer.setPageNumber(pageN + 1);
                add(footer.paragraph());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Writes a Metatag in the header.
     *
     * @param meta the element that has to be written
     * @throws IOException on error
     */
    protected void writeHeader(Meta meta) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.META);
        switch (meta.type()) {
            case Element.HEADER:
                write(HtmlTags.NAME, meta.getName());
                break;
            case Element.SUBJECT:
                write(HtmlTags.NAME, HtmlTags.SUBJECT);
                break;
            case Element.KEYWORDS:
                write(HtmlTags.NAME, HtmlTags.KEYWORDS);
                break;
            case Element.AUTHOR:
                write(HtmlTags.NAME, HtmlTags.AUTHOR);
                break;
            default:
                break;
        }
        write(HtmlTags.CONTENT, HtmlEncoder.encode(meta.getContent()));
        writeEnd();
    }

    /**
     * Writes a link in the header.
     *
     * @param header the element that has to be written
     * @throws IOException on error
     */
    protected void writeLink(Header header) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.LINK);
        write(HtmlTags.REL, header.getName());
        write(HtmlTags.TYPE, HtmlTags.TEXT_CSS);
        write(HtmlTags.REFERENCE, header.getContent());
        writeEnd();
    }

    /**
     * Writes a JavaScript section or, if the markup attribute HtmlTags.URL is set, a JavaScript reference in the
     * header.
     *
     * @param header the element that has to be written
     * @throws IOException on error
     */
    protected void writeJavaScript(Header header) throws IOException {
        addTabs(2);
        writeStart(HtmlTags.SCRIPT);
        write(HtmlTags.LANGUAGE, HtmlTags.JAVASCRIPT);
        if (markup.size() > 0) {
            /* JavaScript reference example:
             *
             * <script language="JavaScript" src="/myPath/MyFunctions.js"/>
             */
            writeMarkupAttributes(markup);
            os.write(GT);
            writeEnd(HtmlTags.SCRIPT);
        } else {
            /* JavaScript coding convention:
             *
             * <script language="JavaScript" type="text/javascript">
             * <!--
             * // ... JavaScript methods ...
             * //-->
             * </script>
             */
            write(HtmlTags.TYPE, Markup.HTML_VALUE_JAVASCRIPT);
            os.write(GT);
            addTabs(2);
            write(new String(BEGINCOMMENT) + "\n");
            write(header.getContent());
            addTabs(2);
            write("//" + new String(ENDCOMMENT));
            addTabs(2);
            writeEnd(HtmlTags.SCRIPT);
        }
    }

    /**
     * Writes some comment.
     * <p>
     * This method writes some comment.
     *
     * @param comment the comment that has to be written
     * @throws IOException on error
     */
    protected void writeComment(String comment) throws IOException {
        addTabs(2);
        os.write(BEGINCOMMENT);
        write(comment);
        os.write(ENDCOMMENT);
    }

    // public methods

    /**
     * Changes the standardfont.
     *
     * @param standardfont The font
     */
    public void setStandardFont(Font standardfont) {
        this.standardfont = standardfont;
    }

    /**
     * Checks if a given font is the same as the font that was last used.
     *
     * @param font the font of an object
     * @return true if the font differs
     */
    public boolean isOtherFont(Font font) {
        try {
            Font cFont = currentfont.peek();
            return cFont.compareTo(font) != 0;
        } catch (EmptyStackException ese) {
            return standardfont.compareTo(font) != 0;
        }
    }

    /**
     * Sets the basepath for images.
     * <p>
     * This is especially useful if you add images using a file, rather than an URL. In PDF there is no problem, since
     * the images are added inline, but in HTML it is sometimes necessary to use a relative path or a special path to
     * some images directory.
     *
     * @param imagepath the new imagepath
     */
    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    /**
     * Resets the imagepath.
     */
    public void resetImagepath() {
        imagepath = null;
    }

    /**
     * Changes the header of this document.
     *
     * @param header the new header
     */
    @Override
    public void setHeader(Header header) {
        this.header = header;
    }

    /**
     * Changes the footer of this document.
     *
     * @param footer the new footer
     */
    @Override
    public void setFooter(HeaderFooter footer) {
        this.footer = footer;
    }

    /**
     * Signals that a <CODE>String</CODE> was added to the <CODE>Document</CODE>.
     *
     * @param string a String to add to the HTML
     * @return <CODE>true</CODE> if the string was added, <CODE>false</CODE> if not.
     */
    public boolean add(String string) {
        if (pause) {
            return false;
        }
        try {
            write(string);
            return true;
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    /**
     * Writes the HTML representation of an element.
     *
     * @param element the element
     * @param indent  the indentation
     * @throws IOException thrown when an I/O operation fails
     */
    protected void write(Element element, int indent) throws IOException {
        switch (element.type()) {
            case Element.MARKED:
                handleMarked(element);
                break;
            case Element.CHUNK:
                handleChunk((Chunk) element, indent);
                break;
            case Element.PHRASE:
                handlePhrase((Phrase) element, indent);
                break;
            case Element.ANCHOR:
                handleAnchor((Anchor) element, indent);
                break;
            case Element.PARAGRAPH:
                handleParagraph((Paragraph) element, indent);
                break;
            case Element.SECTION:
            case Element.CHAPTER:
                handleSection((Section) element, indent);
                break;
            case Element.LIST:
                handleList((List) element, indent);
                break;
            case Element.LISTITEM:
                handleListItem((ListItem) element, indent);
                break;
            case Element.CELL:
                handleCell((Cell) element, indent);
                break;
            case Element.ROW:
                handleRow((Row) element, indent);
                break;
            case Element.TABLE:
                handleTable((Table) element, indent);
                break;
            case Element.ANNOTATION:
                handleAnnotation((Annotation) element);
                break;
            case Element.IMGRAW:
            case Element.JPEG:
            case Element.JPEG2000:
            case Element.IMGTEMPLATE:
                handleImage((Image) element, indent);
                break;
            default:
                break;
        }
    }

    private void handleMarked(Element element) {
        try {
            add(element);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void handleChunk(Chunk chunk, int indent) throws IOException {
        Image image = chunk.getImage();
        if (image != null) {
            write(image, indent);
            return;
        }

        if (chunk.isEmpty()) {
            return;
        }

        boolean tag = isOtherFont(chunk.getFont()) || markup.size() > 0;
        if (tag) {
            writeStartTag(HtmlTags.SPAN, chunk.getFont(), indent);
        }

        writeSubSupScript(chunk);

        write(HtmlEncoder.encode(chunk.getContent()));

        writeClosingSubSupScript(chunk);

        if (tag) {
            writeEnd(Markup.HTML_TAG_SPAN);
        }
    }

    private void writeSubSupScript(Chunk chunk) throws IOException {
        Map<String, Object> attributes = chunk.getChunkAttributes();
        if (attributes != null && attributes.get(Chunk.SUBSUPSCRIPT) != null) {
            if ((Float) attributes.get(Chunk.SUBSUPSCRIPT) > 0) {
                writeStart(HtmlTags.SUP);
            } else {
                writeStart(HtmlTags.SUB);
            }
            os.write(GT);
        }
    }

    private void writeClosingSubSupScript(Chunk chunk) throws IOException {
        Map<String, Object> attributes = chunk.getChunkAttributes();
        if (attributes != null && attributes.get(Chunk.SUBSUPSCRIPT) != null) {
            os.write(LT);
            os.write(FORWARD);
            if ((Float) attributes.get(Chunk.SUBSUPSCRIPT) > 0) {
                write(HtmlTags.SUP);
            } else {
                write(HtmlTags.SUB);
            }
            os.write(GT);
        }
    }

    private void handlePhrase(Phrase phrase, int indent) throws IOException {
        writeStartTag(Markup.HTML_TAG_SPAN, phrase.getFont(), indent);
        currentfont.push(phrase.getFont());
        for (Object o : phrase) {
            write((Element) o, indent + 1);
        }
        writeEnd(Markup.HTML_TAG_SPAN);
        currentfont.pop();
    }

    private void handleAnchor(Anchor anchor, int indent) throws IOException {
        writeStartTag(HtmlTags.ANCHOR, anchor.getFont(), indent);
        currentfont.push(anchor.getFont());
        for (Object o : anchor) {
            write((Element) o, indent + 1);
        }
        writeEnd(HtmlTags.ANCHOR);
        currentfont.pop();
    }

    private void handleParagraph(Paragraph paragraph, int indent) throws IOException {
        writeStartTag(HtmlTags.DIV, paragraph.getFont(), indent);
        currentfont.push(paragraph.getFont());
        for (Object o : paragraph) {
            write((Element) o, indent + 1);
        }
        writeEnd(HtmlTags.DIV);
        currentfont.pop();
    }

    private void handleSection(Section section, int indent) throws IOException {
        writeSection(section, indent);
    }

    private void handleList(List list, int indent) throws IOException {
        addTabs(indent);
        if (list.isNumbered()) {
            writeStart(HtmlTags.ORDEREDLIST);
        } else {
            writeStart(HtmlTags.UNORDEREDLIST);
        }
        writeMarkupAttributes(markup);
        os.write(GT);
        for (Object o : list.getItems()) {
            write((Element) o, indent + 1);
        }
        addTabs(indent);
        if (list.isNumbered()) {
            writeEnd(HtmlTags.ORDEREDLIST);
        } else {
            writeEnd(HtmlTags.UNORDEREDLIST);
        }
    }

    private void handleListItem(ListItem listItem, int indent) throws IOException {
        writeStartTag(HtmlTags.LISTITEM, listItem.getFont(), indent);
        currentfont.push(listItem.getFont());
        for (Object o : listItem) {
            write((Element) o, indent + 1);
        }
        writeEnd(HtmlTags.LISTITEM);
        currentfont.pop();
    }

    private void handleCell(Cell cell, int indent) throws IOException {
        writeStartTag(cell.isHeader() ? HtmlTags.HEADERCELL : HtmlTags.CELL, cell.getFont(), indent);
        if (cell.isEmpty()) {
            write(NBSP);
        } else {
            for (Iterator<Element> i = cell.getElements(); i.hasNext(); ) {
                write((Element) i.next(), indent + 1);
            }
        }
        writeEnd(cell.isHeader() ? HtmlTags.HEADERCELL : HtmlTags.CELL);
    }

    private void handleRow(Row row, int indent) throws IOException {
        addTabs(indent);
        writeStart(HtmlTags.ROW);
        writeMarkupAttributes(markup);
        os.write(GT);
        Element cell;
        for (int i = 0; i < row.getColumns(); i++) {
            if ((cell = row.getCell(i)) != null) {
                write(cell, indent + 1);
            }
        }
        writeEnd(HtmlTags.ROW);
    }

    private void handleTable(Table table, int indent) throws IOException {
        table.complete();
        addTabs(indent);
        writeStart(HtmlTags.TABLE);
        writeMarkupAttributes(markup);
        os.write(SPACE);
        write(HtmlTags.WIDTH);
        os.write(EQUALS);
        os.write(QUOTE);
        write(String.valueOf(table.getWidth()));
        if (!table.isLocked()) {
            write("%");
        }
        os.write(QUOTE);
        String alignment = HtmlEncoder.getAlignment(table.getAlignment());
        if (!"".equals(alignment)) {
            write(HtmlTags.ALIGN, alignment);
        }
        write(HtmlTags.CELLPADDING, String.valueOf(table.getPadding()));
        write(HtmlTags.CELLSPACING, String.valueOf(table.getSpacing()));
        if (table.getBorderWidth() != Rectangle.UNDEFINED) {
            write(HtmlTags.BORDERWIDTH, String.valueOf(table.getBorderWidth()));
        }
        if (table.getBorderColor() != null) {
            write(HtmlTags.BORDERCOLOR, HtmlEncoder.encode(table.getBorderColor()));
        }
        if (table.getBackgroundColor() != null) {
            write(HtmlTags.BACKGROUNDCOLOR, HtmlEncoder.encode(table.getBackgroundColor()));
        }
        os.write(GT);
        for (Iterator<String> iterator = table.iterator(); iterator.hasNext(); ) {
            write((Row) iterator.next(), indent + 1);
        }
        writeEnd(HtmlTags.TABLE);
    }

    private void handleAnnotation(Annotation annotation) throws IOException {
        writeComment(annotation.titleMethod() + ": " + annotation.contentMethod());
    }

    private void handleImage(Image image, int indent) throws IOException {
        if (image.getUrl() == null) {
            return;
        }
        addTabs(indent);
        writeStart(HtmlTags.IMAGE);
        String path = image.getUrl().toString();
        if (imagepath != null) {
            path = imagepath + path.substring(path.lastIndexOf('/') + 1);
        }
        write(HtmlTags.URL, path);
        if ((image.getAlignment() & Image.RIGHT) > 0) {
            write(HtmlTags.ALIGN, HtmlTags.ALIGN_RIGHT);
        } else if ((image.getAlignment() & Image.MIDDLE) > 0) {
            write(HtmlTags.ALIGN, HtmlTags.ALIGN_MIDDLE);
        } else {
            write(HtmlTags.ALIGN, HtmlTags.ALIGN_LEFT);
        }
        if (image.getAlt() != null) {
            write(HtmlTags.ALT, image.getAlt());
        }
        write(HtmlTags.PLAINWIDTH, String.valueOf(image.getScaledWidth()));
        write(HtmlTags.PLAINHEIGHT, String.valueOf(image.getScaledHeight()));
        writeMarkupAttributes(markup);
        writeEnd();
    }

    private void writeStartTag(String tag, Font font, int indent) throws IOException {
        addTabs(indent);
        writeStart(tag);
        writeMarkupAttributes(markup);
        if (font != null) {
            write(font, null);
        }
        os.write(GT);
    }


    /**
     * Writes the HTML representation of a section.
     *
     * @param section the section to write
     * @param indent  the indentation
     * @throws IOException thrown when an I/O operation fails
     */
    protected void writeSection(Section section, int indent) throws IOException {
        if (section.getTitle() != null) {
            int depth = section.getDepth() - 1;
            if (depth > 5) {
                depth = 5;
            }
            Properties styleAttributes = new Properties();
            if (section.getTitle().hasLeading()) {
                styleAttributes.setProperty(Markup.CSS_KEY_LINEHEIGHT, section.getTitle().getTotalLeading() + "pt");
            }
            // start tag
            addTabs(indent);
            writeStart(HtmlTags.H[depth]);
            write(section.getTitle().getFont(), styleAttributes);
            String alignment = HtmlEncoder.getAlignment(section.getTitle().getAlignment());
            if (!"".equals(alignment)) {
                write(HtmlTags.ALIGN, alignment);
            }
            writeMarkupAttributes(markup);
            os.write(GT);
            currentfont.push(section.getTitle().getFont());
            // contents
            for (Object o : section.getTitle()) {
                write((Element) o, indent + 1);
            }
            // end tag
            addTabs(indent);
            writeEnd(HtmlTags.H[depth]);
            currentfont.pop();
        }
        for (Object o : section) {
            write((Element) o, indent);
        }
    }

    /**
     * Writes the representation of a <CODE>Font</CODE>.
     *
     * @param font            a <CODE>Font</CODE>
     * @param styleAttributes the style of the font
     * @throws IOException thrown when an I/O operation fails
     */
    protected void write(Font font, Properties styleAttributes) throws IOException {
        if (font == null || !isOtherFont(font)) {
            return;
        }

        write(" ");
        write(HtmlTags.STYLE);
        write("=\"");

        if (styleAttributes != null) {
            writeCssProperties(styleAttributes);
        }

        writeFontProperties(font);

        write("\"");
    }

    private void writeCssProperties(Properties styleAttributes) throws IOException {
        if (styleAttributes == null) {
            return;
        }

        for (Enumeration<String> e = styleAttributes.propertyNames(); e.hasMoreElements();) {
            String key = e.nextElement();
            writeCssProperty(key, styleAttributes.getProperty(key));
        }
    }

    private void writeFontProperties(Font font) throws IOException {
        if (!isOtherFont(font)) {
            return;
        }

        writeCssProperty(Markup.CSS_KEY_FONTFAMILY, font.getFamilyname());

        if (font.getSize() != Font.UNDEFINED) {
            writeCssProperty(Markup.CSS_KEY_FONTSIZE, font.getSize() + "pt");
        }

        if (font.getColor() != null) {
            writeCssProperty(Markup.CSS_KEY_COLOR, HtmlEncoder.encode(font.getColor()));
        }

        writeFontStyle(font);
    }

    private void writeFontStyle(Font font) throws IOException {
        BaseFont bf = font.getBaseFont();
        if (bf == null) {
            return;
        }

        int fontstyle = getFontStyleFromBaseFont(bf, font);
        if (fontstyle == Font.UNDEFINED || fontstyle == Font.NORMAL) {
            return;
        }

        writeBoldItalicProperties(fontstyle);
        writeUnderlineStrikethrough(fontstyle);
    }

    private int getFontStyleFromBaseFont(BaseFont bf, Font font) {
        int fontstyle = font.getStyle();
        String ps = bf.getPostscriptFontName().toLowerCase();
        if (ps.contains("bold")) {
            if (fontstyle == Font.UNDEFINED) {
                fontstyle = 0;
            }
            fontstyle |= Font.BOLD;
        }
        if (ps.contains("italic") || ps.contains("oblique")) {
            if (fontstyle == Font.UNDEFINED) {
                fontstyle = 0;
            }
            fontstyle |= Font.ITALIC;
        }
        return fontstyle;
    }

    private void writeBoldItalicProperties(int fontstyle) throws IOException {
        if ((fontstyle & Font.BOLDITALIC) == Font.BOLD) {
            writeCssProperty(Markup.CSS_KEY_FONTWEIGHT, Markup.CSS_VALUE_BOLD);
        }
        if ((fontstyle & Font.BOLDITALIC) == Font.ITALIC) {
            writeCssProperty(Markup.CSS_KEY_FONTSTYLE, Markup.CSS_VALUE_ITALIC);
        }
        if (fontstyle == Font.BOLDITALIC) {
            writeCssProperty(Markup.CSS_KEY_FONTWEIGHT, Markup.CSS_VALUE_BOLD);
            writeCssProperty(Markup.CSS_KEY_FONTSTYLE, Markup.CSS_VALUE_ITALIC);
        }
    }

    private void writeUnderlineStrikethrough(int fontstyle) throws IOException {
        if ((fontstyle & Font.UNDERLINE) > 0) {
            writeCssProperty(Markup.CSS_KEY_TEXTDECORATION, Markup.CSS_VALUE_UNDERLINE);
        }
        if ((fontstyle & Font.STRIKETHRU) > 0) {
            writeCssProperty(Markup.CSS_KEY_TEXTDECORATION, Markup.CSS_VALUE_LINETHROUGH);
        }
    }


    /**
     * Writes out a CSS property.
     *
     * @param prop  a CSS property
     * @param value the value of the CSS property
     * @throws IOException thrown when an I/O operation fails
     */
    protected void writeCssProperty(String prop, String value) throws IOException {
        write(prop + ": " + value + "; ");
    }
}
