/*
 * $Id: SAXiTextHandler.java 4070 2009-09-19 18:21:12Z psoares33 $
 *
 * Copyright 2001, 2002 by Bruno Lowagie.
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

package com.lowagie.text.xml;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocListener;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Meta;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.factories.ElementFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.xml.simpleparser.EntitiesToSymbol;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import static com.lowagie.text.pdf.PdfReader.logger;

/**
 * This class is a Handler that controls the iText XML to PDF conversion. Subclass it, if you want to change the way
 * iText translates XML to PDF.
 */

public class SAXiTextHandler<T extends XmlPeer> extends DefaultHandler {

    /**
     * This is the resulting document.
     */
    protected DocListener document;

    /**
     * This is a <CODE>Stack</CODE> of objects, waiting to be added to the document.
     */
    protected Deque<Element> stack;

    /**
     * Counts the number of chapters in this document.
     */
    protected int chapters = 0;

    /**
     * This is the current chunk to which characters can be added.
     */
    protected Chunk currentChunk = null;

    /**
     * This is the current chunk to which characters can be added.
     */
    protected boolean ignore = false;
    /**
     * This hashmap contains all the custom keys and peers.
     */
    protected Map<String, T> myTags;
    /**
     * This is a flag that can be set, if you want to open and close the Document-object yourself.
     */
    private boolean controlOpenClose = true;
    /**
     * current margin of a page.
     */
    private float topMargin = 36;
    /**
     * current margin of a page.
     */
    private float rightMargin = 36;
    /**
     * current margin of a page.
     */
    private float leftMargin = 36;
    /**
     * current margin of a page.
     */
    private float bottomMargin = 36;
    private BaseFont bf = null;

    /**
     * @param document the DocListener
     */
    public SAXiTextHandler(DocListener document) {
        this.document = document;
        stack = new ArrayDeque<>();
    }

    /**
     * @param document the DocListener
     * @param myTags   a Map of the tags
     * @param bf       the base class for the supported fonts
     */
    public SAXiTextHandler(DocListener document, Map<String, T> myTags, BaseFont bf) {
        this(document, myTags);
        this.bf = bf;
    }

    /**
     * @param document the DocListener
     * @param myTags   a Map of the tags
     */
    public SAXiTextHandler(DocListener document, Map<String, T> myTags) {
        this(document);
        this.myTags = myTags;
    }

    /**
     * Sets the parameter that allows you to enable/disable the control over the Document.open() and Document.close()
     * method.
     * <p>
     * If you set this parameter to true (= default), the parser will open the Document object when the start-root-tag
     * is encountered and close it when the end-root-tag is met. If you set it to false, you have to open and close the
     * Document object yourself.
     *
     * @param controlOpenClose set this to false if you plan to open/close the Document yourself
     */

    public void setControlOpenClose(boolean controlOpenClose) {
        this.controlOpenClose = controlOpenClose;
    }

    /**
     * This method gets called when a start tag is encountered.
     *
     * @param uri        the Uniform Resource Identifier
     * @param localName  the local getName (without prefix), or the empty string if Namespace processing is not being
     *                   performed.
     * @param name       the getName of the tag that is encountered
     * @param attributes the list of attributes
     */

    public void startElement(String uri, String localName, String name, Attributes attributes) {

        Properties properties = new Properties();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String attribute = attributes.getQName(i);
                properties.setProperty(attribute, attributes.getValue(i));
            }
        }
        handleStartingTags(name, properties);
    }

    /**
     * This method deals with the starting tags.
     *
     * @param name       the getName of the tag
     * @param attributes the list of attributes
     */

    public void handleStartingTags(String name, Properties attributes) {
        if (shouldIgnore(name)) {
            ignore = true;
            return;
        }

        processCurrentChunk();

        switch (name) {
            case ElementTags.CHUNK:
                handleChunk(attributes);
                break;
            case ElementTags.ENTITY:
                handleEntity(attributes);
                break;
            case ElementTags.PHRASE,
                 ElementTags.ANCHOR,
                 ElementTags.PARAGRAPH,
                 ElementTags.TITLE,
                 ElementTags.LIST,
                 ElementTags.LISTITEM,
                 ElementTags.CELL,
                 ElementTags.SECTION,
                 ElementTags.CHAPTER:
                handleTextElement(attributes);
                break;
            case ElementTags.TABLE:
                handleTable(attributes);
                break;
            case ElementTags.IMAGE:
                handleImage(attributes);
                break;
            case ElementTags.ANNOTATION:
                handleAnnotation(attributes);
                break;
            default:
                handleSpecialTags(name, attributes);
        }
    }

    private boolean shouldIgnore(String name) {
        return ignore || ElementTags.IGNORE.equals(name);
    }

    private void processCurrentChunk() {
        if (currentChunk != null && isNotBlank(currentChunk.getContent())) {
            TextElementArray current = getCurrentElementFromStack();
            current.add(currentChunk);
            stack.push(current);
            currentChunk = null;
        }
    }

    private TextElementArray getCurrentElementFromStack() {
        try {
            return (TextElementArray) stack.pop();
        } catch (EmptyStackException ese) {
            return new Paragraph("", bf == null ? new Font() : new Font(this.bf));
        }
    }

    private void handleChunk(Properties attributes) {
        currentChunk = ElementFactory.getChunk(attributes);
        if (bf != null) {
            currentChunk.setFont(new Font(this.bf));
        }
    }

    private void handleEntity(Properties attributes) {
        Font font = (currentChunk != null) ? currentChunk.getFont() : new Font();
        if (currentChunk != null) {
            handleEndingTags(ElementTags.CHUNK);
        }
        currentChunk = EntitiesToSymbol.get(attributes.getProperty(ElementTags.ID), font);
    }

    private void handleTextElement(Properties attributes) {
        Element element = ElementFactory.getListItem(attributes);
        stack.push(element);  // Abstracted element creation logic
    }

    private void handleTable(Properties attributes) {
        Table table = ElementFactory.getTable(attributes);
        setTableWidths(table);
        stack.push(table);
    }

    private void setTableWidths(Table table) {
        float[] widths = table.getProportionalWidths();
        for (int i = 0; i < widths.length; i++) {
            if (widths[i] == 0) {
                widths[i] = 100.0f / widths.length;
            }
        }
        try {
            table.setWidths(widths);
        } catch (BadElementException bee) {
            throw new ExceptionConverter(bee);
        }
    }

    private void handleImage(Properties attributes) {
        try {
            Image img = ElementFactory.getImage(attributes);
            addingImage(img);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    private void handleAnnotation(Properties attributes) {
        Annotation annotation = ElementFactory.getAnnotation(attributes);
        try {
            modifyTextElementArrayIntoStack(annotation);
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    private void handleSpecialTags(String name, Properties attributes) {
        if (isNewline(name)) {
            handleNewline();
        } else if (isNewpage(name)) {
            handleNewPage();
        } else if (ElementTags.HORIZONTALRULE.equals(name)) {
            handleHorizontalRule();
        } else if (isDocumentRoot(name)) {
            handleDocumentRoot(attributes);
        }
    }

    private void handleNewline() {
        TextElementArray current = getCurrentElementFromStack();
        current.add(Chunk.NEWLINE);
        stack.push(current);
    }
    private static final String ORIENTATION_LANDSCAPE = "landscape";
    private void handleNewPage() {
        TextElementArray current = getCurrentElementFromStack();
        Chunk newPage = new Chunk("");
        newPage.setNewPage();
        if (bf != null) {
            newPage.setFont(new Font(this.bf));
        }
        current.add(newPage);
        stack.push(current);
    }

    private void handleHorizontalRule() {
        LineSeparator hr = new LineSeparator(1.0f, 100.0f, null, Element.ALIGN_CENTER, 0);
        try {
            TextElementArray current = (TextElementArray) stack.pop();
            current.add(hr);
            stack.push(current);
        } catch (EmptyStackException ese) {
            try {
                document.add(hr);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }

    private void handleDocumentRoot(Properties attributes) {
        processDocumentAttributes(attributes);
        if (controlOpenClose) {
            document.open();
        }
    }

    private void processDocumentAttributes(Properties attributes) {
        Rectangle pageSize = null;
        String orientation = null;

        for (Object o : attributes.keySet()) {
            String key = (String) o;
            String value = attributes.getProperty(key);

            if (processMargins(key, value)) continue;

            switch (key) {
                case ElementTags.PAGE_SIZE:
                    pageSize = getPageSize(value);
                    break;
                case ElementTags.ORIENTATION:
                    orientation = getOrientation(value);
                    break;
                default:
                    addMeta(key, value);
            }
        }

        if (pageSize != null) {
            if (ORIENTATION_LANDSCAPE.equals(orientation)) {
                pageSize = pageSize.rotate();
            }
            document.setPageSize(pageSize);
        }
        document.setMargins(leftMargin, rightMargin, topMargin, bottomMargin);
    }

    private boolean processMargins(String key, String value) {
        try {
            switch (key) {
                case ElementTags.LEFT:
                    leftMargin = Float.parseFloat(value + "f");
                    return true;
                case ElementTags.RIGHT:
                    rightMargin = Float.parseFloat(value + "f");
                    return true;
                case ElementTags.TOP:
                    topMargin = Float.parseFloat(value + "f");
                    return true;
                case ElementTags.BOTTOM:
                    bottomMargin = Float.parseFloat(value + "f");
                    return true;
                default:
                    break;
            }
        } catch (NumberFormatException ex) {
            throw new ExceptionConverter(ex);
        }
        return false;
    }

    private Rectangle getPageSize(String value) {
        try {
            Field pageSizeField = PageSize.class.getField(value);
            return (Rectangle) pageSizeField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    private String getOrientation(String value) {
        return ORIENTATION_LANDSCAPE.equals(value) ? ORIENTATION_LANDSCAPE : null;
    }

    private void addMeta(String key, String value) {
        try {
            document.add(new Meta(key, value));
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    private void addingImage (Image img) {
        try {
            addImage(img);
        } catch (EmptyStackException ese) {
            // if there is no element on the stack, the Image is added
            // to the document
            try {
                document.add(img);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }
    }

    private void modifyTextElementArrayIntoStack(Annotation annotation) {
        try {
            TextElementArray current = (TextElementArray) stack.pop();
            addAnnotationToTextElementArray(current, annotation);
            stack.push(current);
        } catch (EmptyStackException ese) {
            document.add(annotation);
        }
    }

    private void addAnnotationToTextElementArray(TextElementArray current, Annotation annotation){
        if(current != null){
            current.add(annotation);
        }else{
            document.add(annotation);
        }
    }

    /**
     * This method gets called when ignorable white space encountered.
     *
     * @param ch     an array of characters
     * @param start  the start position in the array
     * @param length the number of characters to read from the array
     */

    public void ignorableWhitespace(char[] ch, int start, int length) {
        characters(ch, start, length);
    }

    /**
     * This method gets called when characters are encountered.
     *
     * @param ch     an array of characters
     * @param start  the start position in the array
     * @param length the number of characters to read from the array
     */

    public void characters(char[] ch, int start, int length) {
        if (ignore) {
            return;
        }

        String content = new String(ch, start, length).trim();
        if (content.isEmpty()) {
            return;
        }

        String processedContent = processContent(content);

        if (currentChunk == null) {
            currentChunk = createChunk(processedContent);
        } else {
            currentChunk.append(processedContent);
        }
    }

    private String processContent(String content) {
        StringBuilder buf = new StringBuilder();
        boolean newline = false;

        for (char character : content.toCharArray()) {
            newline = handleCharacter(buf, character, newline);
        }

        return buf.toString();
    }

    private boolean handleCharacter(StringBuilder buf, char character, boolean newline) {
        switch (character) {
            case ' ':
                if (!newline) {
                    buf.append(character);
                }
                break;
            case '\n':
                buf.append(' ');
                return true;
            case '\r',
                 '\t':
                break;
            default:
                buf.append(character);
                newline = false;
        }
        return newline;
    }

    private Chunk createChunk(String content) {
        if (bf == null) {
            return new Chunk(content);
        }
        return new Chunk(content, new Font(this.bf));
    }


    /**
     * Sets the font that has to be used.
     *
     * @param bf the base class for the supported fonts
     */
    public void setBaseFont(BaseFont bf) {
        this.bf = bf;
    }

    /**
     * This method gets called when an end tag is encountered.
     *
     * @param uri   the Uniform Resource Identifier
     * @param lname the local getName (without prefix), or the empty string if Namespace processing is not being
     *              performed.
     * @param name  the getName of the tag that ends
     */

    public void endElement(String uri, String lname, String name) {
        handleEndingTags(name);
    }

    /**
     * This method deals with the starting tags.
     *
     * @param name the getName of the tag
     */

    public void handleEndingTags(String name) {
        try {
            if (shouldIgnoreTag(name)) return;
            if (isSelfClosingTag(name)) return;

            handleTitleTag(name);
            handleEndChunk();
            handleEndTagWithTextElement(name);
            handleListItem(name);
            handleTable(name);
            handleSection(name);
            handleChapter(name);
            handleDocumentRoot(name);

        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    // 1. Helper method to check if the tag should be ignored
    private boolean shouldIgnoreTag(String name) {
        if (ElementTags.IGNORE.equals(name)) {
            ignore = false;
            return true;
        }
        return ignore;
    }

    // 2. Handle self-closing tags (tags that don’t have content)
    private boolean isSelfClosingTag(String name) {
        return isNewpage(name) || ElementTags.ANNOTATION.equals(name) || ElementTags.IMAGE.equals(name) || isNewline(name);
    }

    // 3. Handle title tag
    private void handleTitleTag(String name) throws DocumentException {
        if (ElementTags.TITLE.equals(name)) {
            Paragraph current = (Paragraph) stack.pop();
            if (currentChunk != null) {
                current.add(currentChunk);
                currentChunk = null;
            }
            Section previous = (Section) stack.pop();
            previous.setTitle(current);
            stack.push(previous);
        }
    }

    // 4. Handle end chunk
    private void handleEndChunk() {
        if (currentChunk != null) {
            TextElementArray current = updateTextElementArray();
            current.add(currentChunk);
            stack.push(current);
            currentChunk = null;
        }
    }

    // 5. Handle end tag with text elements (e.g., paragraphs, anchors, lists, etc.)
    private void handleEndTagWithTextElement(String name) throws DocumentException {
        if (ElementTags.PHRASE.equals(name) || ElementTags.ANCHOR.equals(name) || ElementTags.LIST.equals(name)
                || ElementTags.PARAGRAPH.equals(name)) {
            Element current = stack.pop();
            addTextElementArrayWithElementIntoStack(current);
        }
    }

    // 6. Handle list items
    private void handleListItem(String name) {
        if (ElementTags.LISTITEM.equals(name)) {
            ListItem listItem = (ListItem) stack.pop();
            List list = (List) stack.pop();
            list.add(listItem);
            stack.push(list);
        }
    }

    // 7. Handle table tags
    private void handleTable(String name) throws DocumentException {
        if (ElementTags.TABLE.equals(name)) {
            Table table = (Table) stack.pop();
            addTextElementArrayWithTableIntoStack(table);
        } else if (ElementTags.ROW.equals(name)) {
            handleTableRow();
        }
    }

    // Handle table rows separately
    private void handleTableRow() throws DocumentException {
        java.util.List<Cell> cells = new ArrayList<>();
        int columns = 0;
        Table table;
        Cell cell;

        while (true) {
            Element element = stack.pop();
            if (element.getTypeImpl() == Element.CELL) {
                cell = (Cell) element;
                columns += cell.getColspan();
                cells.add(cell);
            } else {
                table = (Table) element;
                break;
            }
        }

        if (table.getColumns() < columns) {
            table.addColumns(columns - table.getColumns());
        }

        setCellWidthsAndAddToTable(cells, columns, table);
        stack.push(table);
    }

    private void setCellWidthsAndAddToTable(java.util.List<Cell> cells, int columns, Table table) throws DocumentException {
        Collections.reverse(cells);
        float[] cellWidths = new float[columns];
        boolean[] cellNulls = new boolean[columns];
        Arrays.fill(cellNulls, true);

        float total = 0.0f;
        int j = 0;

        for (Cell cell : cells) {
            String width = cell.getWidthAsString();
            if (cell.getWidth() == 0 && cell.getColspan() == 1 && cellWidths[j] == 0) {
                total = calculateTotalCellWidth(total, cellWidths, j, columns);
            } else if (cell.getColspan() == 1 && width.endsWith("%")) {
                calculateTotalAndUpdateCellNulls(width, cellWidths, cellNulls, j);
            }
            j += cell.getColspan();
            try {
                table.addCell(cell);
            } catch (IOException e) {
                String msg = "Problem adding cell " + cell;
                logger.severe(msg);
            }
        }

        updateTableColumnWidths(table, cellWidths, cellNulls, columns, total);
    }

    private void updateTableColumnWidths(Table table, float[] cellWidths, boolean[] cellNulls, int columns, float total) throws DocumentException {
        float[] widths = table.getProportionalWidths();
        if (widths.length == columns) {
            float left = calculateLeftWidth(widths, cellNulls, cellWidths);
            if (100.0 >= total) {
                updateCellWidths(cellWidths, widths, left, total);
            }
            table.setWidths(cellWidths);
        }
    }

    // 1. Helper method to calculate the left width
    private float calculateLeftWidth(float[] widths, boolean[] cellNulls, float[] cellWidths) {
        float left = 0.0f;
        for (int i = 0; i < widths.length; i++) {
            if (cellNulls[i] && widths[i] != 0) {
                left += widths[i];
                cellWidths[i] = widths[i];
            }
        }
        return left;
    }

    // 2. Helper method to update cell widths
    private void updateCellWidths(float[] cellWidths, float[] widths, float left, float total) {
        for (int i = 0; i < widths.length; i++) {
            if (cellWidths[i] == 0 && widths[i] != 0) {
                cellWidths[i] = (widths[i] / left) * (100.0f - total);
            }
        }
    }

    // 8. Handle section tag
    private void handleSection(String name) {
        if (ElementTags.SECTION.equals(name)) {
            stack.pop();
        }
    }

    // 9. Handle chapter tag
    private void handleChapter(String name) throws DocumentException {
        if (ElementTags.CHAPTER.equals(name)) {
            document.add(stack.pop());
        }
    }

    // 10. Handle document root
    private void handleDocumentRoot(String name) throws DocumentException {
        if (isDocumentRoot(name)) {
            updateStackWithTextElementArraysAndElements();
            if (controlOpenClose) {
                document.close();
            }
        }
    }


    private TextElementArray updateTextElementArray(){
        try {
            return (TextElementArray) stack.pop();
        } catch (EmptyStackException ese) {
            return new Paragraph();
        }
    }

    private void addTextElementArrayWithElementIntoStack(Element current){
        try {
            TextElementArray previous = (TextElementArray) stack.pop();
            previous.add(current);
            stack.push(previous);
        } catch (EmptyStackException ese) {
            document.add(current);
        }
    }

    private void addTextElementArrayWithTableIntoStack(Table table){
        try {
            TextElementArray previous = (TextElementArray) stack.pop();
            previous.add(table);
            stack.push(previous);
        } catch (EmptyStackException ese) {
            document.add(table);
        }
    }

    private float calculateTotalCellWidth(float total, float[] cellWidths, int j, int columns){
        cellWidths[j] = 100.0f / columns;
        total += cellWidths[j];
        return total;
    }

    private void calculateTotalAndUpdateCellNulls(String width, float[] cellWidths, boolean[] cellNulls, int j) {
        try {
            // Attempt to parse the width and update cell widths
            cellWidths[j] = Float.parseFloat(width.substring(0, width.length() - 1) + "f");
            cellNulls[j] = false;
        } catch (NumberFormatException nfe) {
            // Log specific error for invalid float format
            String msg = "Failed to parse cell width: " + width;
            logger.log(Level.WARNING, msg, nfe);
        } catch (IndexOutOfBoundsException ioe) {
            // Log specific error for index out of bounds
            String msg = "Index out of bounds when updating cell: " + j;
            logger.log(Level.WARNING, msg, ioe);
        }
    }


    private void updateStackWithTextElementArraysAndElements(){
        try {
            Element element = stack.pop();
            addTextElementArrayWithElementIntoStack(element);
        } catch (EmptyStackException ese) {
            logger.severe("Empty stack when updating text element");
        }
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    protected void addImage(Image img) throws EmptyStackException {
        // if there is an element on the stack...
        Element current = stack.pop();
        // ...and it's a Chapter or a Section, the Image can be
        // added directly
        if (current instanceof Section || current instanceof Cell) {
            ((TextElementArray) current).add(img);
            stack.push(current);
        } else if (current instanceof Phrase) {
            // ... if it is a Phrase, we have to wrap the Image in a new Chunk
            ((TextElementArray) current).add(new Chunk(img, 0, 0));
            stack.push(current);
        } else {
            // ...if not, we need to a lot of stuff
            Deque<Element> newStack = new ArrayDeque<>();
            while (!(current instanceof Section || current instanceof Cell)) {
                newStack.push(current);
                if (current instanceof Anchor anchorCurrent) {
                    img.setAnnotation(new Annotation(0, 0, 0,
                            0, anchorCurrent.getReference()));
                }
                current = stack.pop();
            }
            ((TextElementArray) current).add(img);
            stack.push(current);
            while (!newStack.isEmpty()) {
                stack.push(newStack.pop());
            }
        }
    }

    /**
     * Checks if a certain tag corresponds with the newpage-tag.
     *
     * @param tag a presumed tagname
     * @return <CODE>true</CODE> or <CODE>false</CODE>
     */

    private boolean isNewpage(String tag) {
        return ElementTags.NEWPAGE.equals(tag);
    }

    /**
     * Checks if a certain tag corresponds with the newpage-tag.
     *
     * @param tag a presumed tagname
     * @return <CODE>true</CODE> or <CODE>false</CODE>
     */

    private boolean isNewline(String tag) {
        return ElementTags.NEWLINE.equals(tag);
    }

    /**
     * Checks if a certain tag corresponds with the roottag.
     *
     * @param tag a presumed tagname
     * @return <CODE>true</CODE> if <VAR>tag </VAR> equals <CODE>itext
     * </CODE>,<CODE>false</CODE> otherwise.
     */

    protected boolean isDocumentRoot(String tag) {
        return ElementTags.ITEXT.equals(tag);
    }
}
