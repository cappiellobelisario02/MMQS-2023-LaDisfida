/*
 * Copyright 2004 Paulo Soares
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
 * Contributions by:
 * Lubos Strapko
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html.simpleparser;

import static com.lowagie.text.html.Markup.parseLength;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocListener;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.FontProvider;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class HTMLWorker implements SimpleXMLDocHandler, DocListener {

    private static final String TABLE_KEY = "table";

    public static final String TAGS_SUPPORTED_STRING =
            "ol ul li a pre font span br p div body table td th tr i b u sub sup em strong s strike"
                    + " h1 h2 h3 h4 h5 h6 img hr";
    protected static final Map<String, Object> tagsSupported = new HashMap<>();

    static {
        StringTokenizer tok = new StringTokenizer(TAGS_SUPPORTED_STRING);
        while (tok.hasMoreTokens()) {
            tagsSupported.put(tok.nextToken(), null);
        }
    }

    protected ArrayList<Element> objectList;
    protected DocListener document;
    private Paragraph currentParagraph;
    private ChainedProperties cprops = new ChainedProperties();
    private Deque<Object> stack = new ArrayDeque<>();
    private boolean pendingTR = false;
    private boolean pendingTD = false;
    private boolean pendingLI = false;
    private StyleSheet style = new StyleSheet();
    private boolean isPRE = false;
    private Deque<Object> tableState = new ArrayDeque<>();
    private boolean skipText = false;
    private Map<String, Object> interfaceProps;
    private FactoryProperties factoryProperties = new FactoryProperties();


    public void push(Object item) {
        stack.addFirst(item);
    }

    // Method to pop an element from the stack
    public Object pop() {
        return stack.removeFirst();
    }

    // Method to peek the top element from the stack
    public Object peek() {
        return stack.peekFirst();
    }

    // Method to check if the stack is empty
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    // Method to push an element onto the stack
    public void pushState(Object state) {
        tableState.addFirst(state);
    }

    // Method to pop an element from the stack
    public Object popState() {
        return tableState.removeFirst();
    }

    // Method to peek the top element from the stack
    public Object peekState() {
        return tableState.peekFirst();
    }

    // Method to check if the stack is empty
    public boolean isTableStateEmpty() {
        return tableState.isEmpty();
    }

    /**
     * Creates a new instance of HTMLWorker
     *
     * @param document A class that implements <CODE>DocListener</CODE>
     */
    public HTMLWorker(DocListener document) {
        this.document = document;
    }

    public static ArrayList<Element> parseToList(Reader reader, StyleSheet style) throws IOException {
        Map<String, Object> interfaceProps = null;
        return parseToList(reader, style, interfaceProps);
    }

    public static ArrayList<Element> parseToList(Reader reader, StyleSheet style, Map<String, Object> interfaceProps)
            throws IOException {
        com.lowagie.text.html.simpleparser.HTMLWorker worker = new com.lowagie.text.html.simpleparser.HTMLWorker(null);
        if (style != null) {
            worker.style = style;
        }
        worker.document = worker;
        worker.setInterfaceProps(interfaceProps);
        worker.objectList = new ArrayList<>();
        worker.parse(reader);
        return worker.objectList;
    }

    public StyleSheet getStyleSheet() {
        return style;
    }

    public void setStyleSheet(StyleSheet style) {
        this.style = style;
    }

    public Map<String, Object> getInterfaceProps() {
        return interfaceProps;
    }

    public void setInterfaceProps(Map<String, Object> interfaceProps) {
        this.interfaceProps = interfaceProps;
        FontProvider ff = null;
        if (interfaceProps != null) {
            ff = (FontProvider) interfaceProps.get("font_factory");
        }
        if (ff != null) {
            factoryProperties.setFontImp(ff);
        }
    }

    public void parse(Reader reader) throws IOException {
        SimpleXMLParser.parse(this, null, reader, true);
    }

    public void endDocument() {
        try {
            stack.forEach(o -> document.add((Element) o));
            if (currentParagraph != null) {
                document.add(currentParagraph);
            }
            currentParagraph = null;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public void startDocument() {
        Map<String, String> h = new HashMap<>();
        style.applyStyle("body", h);
        cprops.addToChain("body", h);
    }

    public void startElement(String tag, Map<String, String> style) {
        if (!tagsSupported.containsKey(tag)) {
            return;
        }

        try {
            this.style.applyStyle(tag, style);
            handleFollowTag(tag);
            if (handleSpecialTags(tag, style)) {
                return;
            }

            endElement("p");
            handleGeneralTags(tag, style);

        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void handleFollowTag(String tag) {
        String follow = FactoryProperties.followTags.get(tag);
        if (follow != null) {
            Map<String, String> prop = new HashMap<>();
            prop.put(follow, null);
            cprops.addToChain(follow, prop);
        }
    }

    private boolean handleSpecialTags(String tag, Map<String, String> style) throws IOException {
        switch (tag) {
            case HtmlTags.ANCHOR:
                handleAnchorTag(style);
                return true;
            case HtmlTags.NEWLINE:
                handleNewlineTag();
                return true;
            case HtmlTags.HORIZONTALRULE:
                handleHorizontalRuleTag(style);
                return true;
            case HtmlTags.CHUNK:
            case HtmlTags.SPAN:
                cprops.addToChain(tag, style);
                return true;
            case HtmlTags.IMAGE:
                handleImageTag(style);
                return true;
            default:
                return false;
        }
    }

    private void handleAnchorTag(Map<String, String> style) {
        cprops.addToChain(HtmlTags.ANCHOR, style);
        if (currentParagraph == null) {
            currentParagraph = new Paragraph();
        }
        stack.push(currentParagraph);
        currentParagraph = new Paragraph();
    }

    private void handleNewlineTag() {
        if (currentParagraph == null) {
            currentParagraph = new Paragraph();
        }
        Chunk chunk = factoryProperties.createChunk("\n", cprops);
        currentParagraph.add(chunk);
    }

    private void handleHorizontalRuleTag(Map<String, String> style) {
        boolean addLeadingBreak = shouldAddLeadingBreak();
        String align = style.get("align");
        int hrAlign = determineHorizontalAlignment(align);
        float hrWidth = determineHorizontalRuleWidth(style);
        float hrSize = determineHorizontalRuleSize(style);

        if (addLeadingBreak) {
            currentParagraph.add(Chunk.NEWLINE);
        }
        currentParagraph.add(new LineSeparator(hrSize, hrWidth, null, hrAlign, currentParagraph.getLeading() / 2));
        currentParagraph.add(Chunk.NEWLINE);
    }

    private boolean shouldAddLeadingBreak() {
        if (currentParagraph == null) {
            currentParagraph = new Paragraph();
            return false;
        }
        int numChunks = currentParagraph.getChunks().size();
        return numChunks > 0 &&
                !((Chunk) (currentParagraph.getChunks().get(numChunks - 1))).getContent().endsWith("\n");
    }

    private int determineHorizontalAlignment(String align) {
        if (align != null) {
            if (align.equalsIgnoreCase("left")) {
                return Element.ALIGN_LEFT;
            }
            if (align.equalsIgnoreCase("right")) {
                return Element.ALIGN_RIGHT;
            }
        }
        return Element.ALIGN_CENTER;
    }

    private float determineHorizontalRuleWidth(Map<String, String> style) {
        String width = style.get("width");
        if (width != null) {
            float tmpWidth = parseLength(width, Markup.DEFAULT_FONT_SIZE);
            if (tmpWidth > 0) {
                return tmpWidth;
            }
            if (!width.endsWith("%")) {
                return 100; // Treat a pixel width as 100% for now.
            }
        }
        return 1;
    }

    private float determineHorizontalRuleSize(Map<String, String> style) {
        String size = style.get("size");
        if (size != null) {
            float tmpSize = parseLength(size, Markup.DEFAULT_FONT_SIZE);
            if (tmpSize > 0) {
                return tmpSize;
            }
        }
        return 1;
    }

    private void handleImageTag(Map<String, String> style) throws IOException {
        String src = style.get(ElementTags.SRC);
        if (src == null) {
            return;
        }
        cprops.addToChain(HtmlTags.IMAGE, style);
        Image img = resolveImage(src, style);
        if (img == null) {
            return;
        }
        adjustImageProperties(img, style);
        addImageToDocumentOrParagraph(img, style);
    }

    private Image resolveImage(String src, Map<String, String> style) throws IOException {
        Image img = null;
        if (interfaceProps != null) {
            img = tryResolveImageFromProvider(src, style);
            if (img == null) {
                img = tryResolveImageFromStaticMap(src);
            }
        }
        if (img == null) {
            img = tryResolveImageFromSource(src);
        }
        return img;
    }

    private Image tryResolveImageFromProvider(String src, Map<String, String> style) {
        ImageProvider ip = (ImageProvider) interfaceProps.get("img_provider");
        if (ip != null) {
            return ip.getImage(src, (HashMap) style, cprops, document);
        }
        return null;
    }

    private Image tryResolveImageFromStaticMap(String src) throws IOException {
        Map<String, String> images = (HashMap<String, String>) interfaceProps.get("img_static");
        if (images != null) {
            Image tim = (Image) images.get(src);
            if (tim != null) {
                return Image.getInstance(tim);
            }
        } else {
            if (!src.startsWith("http")) {
                String baseUrl = (String) interfaceProps.get("img_baseurl");
                if (baseUrl != null) {
                    src = baseUrl + src;
                    return Image.getInstance(src);
                }
            }
        }
        return null;
    }

    private Image tryResolveImageFromSource(String src) throws IOException {
        if (src.startsWith("data:image/")) {
            int pos = src.indexOf("base64,");
            return Image.getInstance(Base64.getDecoder().decode(src.substring(pos + 7)));
        } else {
            if (!src.startsWith("http")) {
                String path = cprops.getOrDefault("image_path", "");
                src = new File(path, src).getPath();
            }
            return Image.getInstance(src);
        }
    }

    private void adjustImageProperties(Image img, Map<String, String> style) {
        String width = style.get("width");
        String height = style.get("height");
        float actualFontSize = parseLength(cprops.getProperty(ElementTags.SIZE), Markup.DEFAULT_FONT_SIZE);
        if (actualFontSize <= 0f) {
            actualFontSize = Markup.DEFAULT_FONT_SIZE;
        }
        float widthInPoints = parseLength(width, actualFontSize);
        float heightInPoints = parseLength(height, actualFontSize);
        if (widthInPoints > 0 && heightInPoints > 0) {
            img.scaleAbsolute(widthInPoints, heightInPoints);
        } else if (widthInPoints > 0) {
            heightInPoints = img.getHeight() * widthInPoints / img.getWidth();
            img.scaleAbsolute(widthInPoints, heightInPoints);
        } else if (heightInPoints > 0) {
            widthInPoints = img.getWidth() * heightInPoints / img.getHeight();
            img.scaleAbsolute(widthInPoints, heightInPoints);
        }
        img.setWidthPercentage(0);
    }

    private void addImageToDocumentOrParagraph(Image img, Map<String, String> style) {
        String align = style.get("align");
        if (align != null) {
            endElement("p");
            int ralign = Image.MIDDLE;
            if (align.equalsIgnoreCase("left")) {
                ralign = Image.LEFT;
            } else if (align.equalsIgnoreCase("right")) {
                ralign = Image.RIGHT;
            }
            img.setAlignment(ralign);
            Img i = null;
            boolean skip = false;
            if (interfaceProps != null) {
                i = (Img) interfaceProps.get("img_interface");
                if (i != null) {
                    skip = i.process(img, (HashMap) style, cprops, document);
                }
            }
            if (!skip) {
                document.add(img);
            }
            cprops.removeChain(HtmlTags.IMAGE);
        } else {
            cprops.removeChain(HtmlTags.IMAGE);
            if (currentParagraph == null) {
                currentParagraph = FactoryProperties.createParagraph(cprops);
            }
            currentParagraph.add(new Chunk(img, 0, 0));
        }
    }

    private void handleGeneralTags(String tag, Map<String, String> style) {
        switch (tag) {
            case "h1":
            case "h2":
            case "h3":
            case "h4":
            case "h5":
            case "h6":
                handleHeadingTag(tag, style);
                break;
            case HtmlTags.UNORDEREDLIST:
            case HtmlTags.ORDEREDLIST:
                handleListTag(tag, style);
                break;
            case HtmlTags.LISTITEM:
                handleListItemTag(style);
                break;
            case HtmlTags.DIV:
            case HtmlTags.BODY:
            case "p":
                cprops.addToChain(tag, style);
                break;
            case HtmlTags.PRE:
                handlePreformattedTag(style);
                break;
            case "tr":
                handleTableRowTag(style);
                break;
            case "td":
            case "th":
                handleTableCellTag(tag);
                break;
            case TABLE_KEY:
                handleTableTag(style);
                break;
            default:
                break;
        }
    }

    private void handleHeadingTag(String tag, Map<String, String> style) {
        if (!style.containsKey(ElementTags.SIZE)) {
            int v = 7 - Integer.parseInt(tag.substring(1));
            style.put(ElementTags.SIZE, Integer.toString(v));
        }
        cprops.addToChain(tag, style);
    }

    private void handleListTag(String tag, Map<String, String> style) {
        if (pendingLI) {
            endElement(HtmlTags.LISTITEM);
        }
        skipText = true;
        cprops.addToChain(tag, style);
        com.lowagie.text.List list = new com.lowagie.text.List(tag.equals(HtmlTags.ORDEREDLIST));
        setIndentationFromProperties(list, cprops);
        stack.push(list);
    }

    private void handleListItemTag(Map<String, String> style) {
        if (pendingLI) {
            endElement(HtmlTags.LISTITEM);
        }
        skipText = false;
        pendingLI = true;
        cprops.addToChain(HtmlTags.LISTITEM, style);
        ListItem item = FactoryProperties.createListItem(cprops);
        stack.push(item);
    }

    private void handlePreformattedTag(Map<String, String> style) {
        if (!style.containsKey(ElementTags.FACE)) {
            style.put(ElementTags.FACE, "Courier");
        }
        cprops.addToChain(HtmlTags.PRE, style);
        isPRE = true;
    }

    private void handleTableRowTag(Map<String, String> style) {
        if (pendingTR) {
            endElement("tr");
        }
        skipText = true;
        pendingTR = true;
        cprops.addToChain("tr", style);
    }

    private void handleTableCellTag(String tag) {
        if (pendingTD) {
            endElement(tag);
        }
        skipText = false;
        pendingTD = true;
        cprops.addToChain(tag, style);
        stack.push(new IncCell(tag, cprops));
    }

    private void handleTableTag(Map<String, String> style) {
        cprops.addToChain(TABLE_KEY, style);
        IncTable table = new IncTable(style);
        stack.push(table);
        tableState.push(new boolean[]{pendingTR, pendingTD});
        pendingTR = pendingTD = false;
        skipText = true;
    }

    private void setIndentationFromProperties(com.lowagie.text.List list, ChainedProperties cprops) {
        try {
            float indentation = Float.parseFloat(cprops.getProperty("indent"));
            list.setIndentationLeft(indentation);
        } catch (NumberFormatException e) {
            // Handle the case where the property is not a valid float
            list.setAutoindent(true);
        }
    }

    public void endElement(String tag) {
        if (!tagsSupported.containsKey(tag)) {
            return;
        }
        try {
            handleFollowAndFontTags(tag);
            handleAnchorTag(tag);
            handleParagraphAndStack(tag);

            if (tag.equals(HtmlTags.UNORDEREDLIST) || tag.equals(HtmlTags.ORDEREDLIST)) {
                handleListTags(tag);
                return;
            }
            if (tag.equals(HtmlTags.LISTITEM)) {
                handleListItemTag();
                return;
            }
            handleSimpleTags(tag);

            if (tag.equals(TABLE_KEY)) {
                handleTableTag();
                return;
            }
            if (tag.equals("tr")) {
                handleTableRowTag();
                return;
            }
            if (tag.equals("td") || tag.equals("th")) {
                handleTableCellTag(tag);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void handleFollowAndFontTags(String tag) {
        String follow = FactoryProperties.followTags.get(tag);
        if (follow != null || tag.equals("font") || tag.equals("span")) {
            cprops.removeChain(follow != null ? follow : tag);
        }
    }

    private void handleAnchorTag(String tag) throws DocumentException {
        if (!tag.equals("a")) {
            return;
        }

        if (currentParagraph == null) {
            currentParagraph = new Paragraph();
        }

        boolean skip = false;
        if (interfaceProps != null) {
            ALink i = (ALink) interfaceProps.get("alink_interface");
            if (i != null) {
                skip = i.process(currentParagraph, cprops);
            }
        }

        if (!skip) {
            cprops.findProperty("href").ifPresent(href -> {
                for (Element chunk : currentParagraph.getChunks()) {
                    ((Chunk) chunk).setAnchor(href);
                }
            });
        }

        Paragraph tmp = (Paragraph) stack.pop();
        tmp.add(new Phrase(currentParagraph));
        currentParagraph = tmp;
        cprops.removeChain("a");
    }

    private void handleParagraphAndStack(String tag) throws DocumentException {
        if (tag.equals("br") || currentParagraph == null) {
            return;
        }

        if (stack.isEmpty()) {
            document.add(currentParagraph);
        } else {
            Object obj = stack.pop();
            if (obj instanceof TextElementArray) {
                ((TextElementArray) obj).add(currentParagraph);
            }
            stack.push(obj);
        }
        currentParagraph = null;
    }

    private void handleListTags(String tag) throws DocumentException {
        if (pendingLI) {
            endElement(HtmlTags.LISTITEM);
        }
        skipText = false;
        cprops.removeChain(tag);
        if (stack.isEmpty()) {
            return;
        }

        Object obj = stack.pop();
        if (!(obj instanceof com.lowagie.text.List)) {
            stack.push(obj);
            return;
        }

        if (stack.isEmpty()) {
            document.add((Element) obj);
        } else {
            addElementToStackOrDocument((Element) obj);
        }
    }

    private void handleListItemTag() throws DocumentException {
        pendingLI = false;
        skipText = true;
        cprops.removeChain(HtmlTags.LISTITEM);
        if (stack.isEmpty()) {
            return;
        }

        Object obj = stack.pop();
        if (!(obj instanceof ListItem)) {
            stack.push(obj);
            return;
        }

        if (stack.isEmpty()) {
            document.add((Element) obj);
            return;
        }

        Object list = stack.pop();
        if (list instanceof com.lowagie.text.List) {
            ((com.lowagie.text.List) list).add((Element) obj);
            adjustListSymbol((ListItem) obj);
        }
        stack.push(list);
    }

    private void adjustListSymbol(ListItem item) {
        List<Element> cks = item.getChunks();
        if (!cks.isEmpty()) {
            item.getListSymbol().setFont(((Chunk) cks.get(0)).getFont());
        }
    }

    private void handleSimpleTags(String tag) {
        if (tag.equals("div") || tag.equals("body") || tag.equals("p")
                || tag.equals(HtmlTags.PRE) || tag.matches("h[1-6]")) {
            cprops.removeChain(tag);
            if (tag.equals(HtmlTags.PRE)) {
                isPRE = false;
            }
        }
    }

    private void handleTableTag() throws DocumentException {
        if (pendingTR) {
            endElement("tr");
        }
        cprops.removeChain(TABLE_KEY);
        IncTable table = (IncTable) stack.pop();
        PdfPTable tb = table.buildTable();
        tb.setSplitRows(true);
        addElementToStackOrDocument(tb);

        boolean[] state = (boolean[]) tableState.pop();
        pendingTR = state[0];
        pendingTD = state[1];
        skipText = false;
    }

    private void handleTableRowTag() throws DocumentException {
        if (pendingTD) {
            endElement("td");
        }
        pendingTR = false;
        cprops.removeChain("tr");

        List<PdfPCell> cells = new ArrayList<>();
        IncTable table = null;

        while (!stack.isEmpty()) {
            Object obj = stack.pop();
            if (obj instanceof IncCell) {
                cells.add(((IncCell) obj).getCell());
            }
            if (obj instanceof IncTable) {
                table = (IncTable) obj;
                break;
            }
        }

        if (table != null) {
            table.addCols(cells);
            table.endRow();
            stack.push(table);
        }
        skipText = true;
    }


    private void addElementToStackOrDocument(Element element) throws DocumentException {
        if (stack.isEmpty()) {
            document.add(element);
        } else {
            Object peek = stack.peek();
            if (peek instanceof com.lowagie.text.List) {
                ((com.lowagie.text.List) peek).add((com.lowagie.text.List) element);
            } else {
                ((TextElementArray) peek).add(element);
            }
        }
    }


    public void text(String str) {
        if (skipText) {
            return;
        }

        String content = preprocessText(str);
        if (content.isEmpty()) {
            return;
        }

        if (isPRE) {
            addContentToCurrentParagraph(content);
            return;
        }

        currentParagraph = currentParagraph != null ? currentParagraph : FactoryProperties.createParagraph(cprops);
        Chunk chunk = factoryProperties.createChunk(content, cprops);
        currentParagraph.add(chunk);
    }

    private String preprocessText(String str) {
        if (str.trim().length() == 0 || str.indexOf(' ') < 0) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        boolean newline = false;
        for (char character : str.toCharArray()) {
            switch (character) {
                case ' ':
                    if (!newline) {
                        buf.append(character);
                    }
                    break;
                case '\n':
                    if (buf.length() > 0) {
                        newline = true;
                        buf.append(' ');
                    }
                    break;
                case '\r':
                case '\t':
                    break;
                default:
                    newline = false;
                    buf.append(character);
            }
        }
        return buf.toString();
    }

    private void addContentToCurrentParagraph(String content) {
        if (currentParagraph == null) {
            currentParagraph = FactoryProperties.createParagraph(cprops);
        }
        Chunk chunk = factoryProperties.createChunk(content, cprops);
        currentParagraph.add(chunk);
    }

    public boolean add(Element element) throws DocumentException {
        objectList.add(element);
        return true;
    }

    public void clearTextWrap() throws DocumentException {
        throw new UnsupportedOperationException("Clearing not supported yet.");
    }

    public void close() {
        throw new UnsupportedOperationException("Closing failed by unsupported operation");
    }

    public boolean newPage() {
        return true;
    }

    public void open() {
        throw new UnsupportedOperationException("Opening failed by unsupported operation");
    }

    public void resetFooter() {
        throw new UnsupportedOperationException("Reset footer failed by unsupported operation");
    }

    public void resetHeader() {
        throw new UnsupportedOperationException("Reset header failed by unsupported operation");
    }

    public void resetPageCount() {
        throw new UnsupportedOperationException("Reset page count failed by unsupported operation");
    }

    public void setFooter(HeaderFooter footer) {
        throw new UnsupportedOperationException("Setting footer failed by unsupported operation");
    }

    public void setHeader(HeaderFooter header) {
        throw new UnsupportedOperationException("Setting header failed by unsupported operation");
    }

    public boolean setMarginMirroring(boolean marginMirroring) {
        return false;
    }

    /**
     * @see com.lowagie.text.DocListener#setMarginMirroring(boolean)
     * @since 2.1.6
     */
    public boolean setMarginMirroringTopBottom(boolean marginMirroring) {
        return false;
    }

    public boolean setMargins(float marginLeft, float marginRight,
            float marginTop, float marginBottom) {
        return true;
    }

    public void setPageCount(int pageN) {
        throw new UnsupportedOperationException("Setting page count failed by unsupported operation");
    }

    public boolean setPageSize(Rectangle pageSize) {
        return true;
    }

}
