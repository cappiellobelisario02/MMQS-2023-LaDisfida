/*
 * $Id: PdfDocument.java 4098 2009-11-16 13:27:45Z blowagie $
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

package com.lowagie.text.pdf;

import static com.lowagie.text.pdf.PdfAnnotation.FLAGS_PRINT;
import static java.awt.Font.LAYOUT_RIGHT_TO_LEFT;

import com.lowagie.text.Anchor;
import com.lowagie.text.Annotation;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocListener;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
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
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.ActionException;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.draw.DrawInterface;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.internal.PdfAnnotationsImp;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;


/**
 * <CODE>PdfDocument</CODE> is the class that is used by <CODE>PdfWriter</CODE>
 * to translate a <CODE>Document</CODE> into a PDF with different pages.
 * <p>
 * A <CODE>PdfDocument</CODE> always listens to a <CODE>Document</CODE> and adds the Pdf representation of every
 * <CODE>Element</CODE> that is added to the <CODE>Document</CODE>.
 *
 * @see Document
 * @see DocListener
 * @see PdfWriter
 * @since 2.0.8 (class was package-private before)
 */

public class PdfDocument extends Document {

    static Logger logger = Logger.getLogger(PdfDocument.class.getName());

    protected static final DecimalFormat SIXTEEN_DIGITS = new DecimalFormat("0000000000000000");
    /**
     * The characters to be applied the hanging punctuation.
     */
    static final String HANGING_PUNCTUATION = ".,;:'";

// CONSTRUCTING A PdfDocument/PdfWriter INSTANCE
    /**
     * The <CODE>PdfWriter</CODE>.
     */
    protected PdfWriter writer;
    /**
     * This is the PdfContentByte object, containing the text.
     */
    protected PdfContentByte text;
    /**
     * This is the PdfContentByte object, containing the borders and other Graphics.
     */
    protected PdfContentByte graphics;

// LISTENER METHODS START

//    [L0] ElementListener interface
    /**
     * This represents the leading of the lines.
     */
    protected float leading = 0;
    /**
     * This represents the current alignment of the PDF Elements.
     */
    protected int alignment = Element.ALIGN_LEFT;
    /**
     * This is the current height of the document.
     */
    protected float currentHeight = 0;
    /**
     * Signals that onParagraph is valid (to avoid that a Chapter/Section title is treated as a Paragraph).
     *
     * @since 2.1.2
     */
    protected boolean isSectionTitle = false;
    /**
     * Signals that the current leading has to be subtracted from a YMark object when positive.
     *
     * @since 2.1.2
     */
    protected int leadingCount = 0;
    /**
     * The current active <CODE>PdfAction</CODE> when processing an <CODE>Anchor</CODE>.
     */
    protected PdfAction anchorAction = null;
    //    [L3] DocListener interface
    protected int textEmptySize;
    /**
     * XMP Metadata for the page.
     */
    protected byte[] xmpMetadata = null;
    /**
     * margin in x direction starting from the left. Will be valid in the next page
     */
    protected float nextMarginLeft;
    /**
     * margin in x direction starting from the right. Will be valid in the next page
     */
    protected float nextMarginRight;
    /**
     * margin in y direction starting from the top. Will be valid in the next page
     */
    protected float nextMarginTop;
    /**
     * margin in y direction starting from the bottom. Will be valid in the next page
     */
    protected float nextMarginBottom;

//    [L1] DocListener interface
    /**
     * Signals that OnOpenDocument should be called.
     */
    protected boolean firstPageEvent = true;

//    [L2] DocListener interface
    /**
     * The line that is currently being written.
     */
    protected PdfLine line = null;
    /**
     * The lines that are written until now.
     */
    protected java.util.List<PdfLine> lines = new ArrayList<>();

    // [C9] Metadata for the page
    /**
     * Holds the type of the last element, that has been added to the document.
     */
    protected int lastElementType = -1;
    protected Indentation indentation = new Indentation();
    /**
     * some meta information about the Document.
     */
    protected PdfInfo info = new PdfInfo();

//    [L4] DocListener interface
    /**
     * This is the root outline of the document.
     */
    protected PdfOutline rootOutline;

//    [L5] DocListener interface
    /**
     * This is the current <CODE>PdfOutline</CODE> in the hierarchy of outlines.
     */
    protected PdfOutline currentOutline;
    /**
     * Contains the Viewer preferences of this PDF document.
     */
    protected PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
    protected PdfPageLabels pageLabels;
    /**
     * Stores the destinations keyed by name. Value is
     * <CODE>Object[]{PdfAction,PdfIndirectReference,PdfDestintion}</CODE>.
     */
    protected TreeMap<String, Object[]> localDestinations = new TreeMap<>();
    protected HashMap<String, PdfIndirectReference> documentLevelJS = new HashMap<>();

    //    [L6] DocListener interface
    protected HashMap<String, PdfIndirectReference> documentFileAttachment = new HashMap<>();
    protected String openActionName;

    //    [L7] DocListener interface
    protected PdfAction openActionAction;

    //    [L8] DocListener interface
    protected PdfDictionary additionalActions;

    //    [L9] DocListener interface
    protected PdfCollection collection;

    //    [L10] DocListener interface
    protected int markPoint;

//    [L11] DocListener interface
    /**
     * This is the size of the next page.
     */
    protected Rectangle nextPageSize = null;

//    [L12] DocListener interface
    /**
     * This is the size of the several boxes of the current Page.
     */
    protected HashMap<String, PdfRectangle> thisBoxSize = new HashMap<>();

// DOCLISTENER METHODS END
    /**
     * This is the size of the several boxes that will be used in the next page.
     */
    protected HashMap<String, PdfRectangle> boxSize = new HashMap<>();
    /**
     * The duration of the page
     */
    protected int duration = -1; // negative values will indicate no duration
    /**
     * The page transition
     */
    protected PdfTransition transition = null;
    protected PdfDictionary pageAA = null;
    protected PdfIndirectReference thumb;
    /**
     * This are the page resources of the current Page.
     */
    protected PageResources pageResources;
    /**
     * Holds value of property strictImageSequence.
     */
    protected boolean strictImageSequence = false;
    /**
     * This is the position where the image ends.
     */
    protected float imageEnd = -1;
    /**
     * This is the image that could not be shown on a previous page.
     */
    protected Image imageWait = null;
    /**
     * Stores a list of document level JavaScript actions.
     */
    int jsCounter;
    PdfAnnotationsImp annotationsImp;
    /**
     * This checks if the page is empty.
     */
    private boolean pageEmpty = true;
    /**
     * This is the flag meaning whether document is creating footer.
     */
    private boolean isDoFooter = false;

    /**
     * Constructs a new PDF document.
     */
    public PdfDocument() {
        super();
        addProducer();
        addCreationDate();
    }

    /**
     * Integrate a paragraph into a table, so it can be a whole.
     * <p>Note: This is not a table with square, it's just like the paragraph, but
     * it cannot be separated.
     *
     * @param paragraph the {@code Paragraph} incoming paragraphs to be consolidated
     * @return {@code PdfPTable} the whole which will be used later
     */
    static PdfPTable createInOneCell(Paragraph paragraph) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);

        PdfPCell cell = createCellFromParagraph(paragraph);
        table.addCell(cell);

        return table;
    }

    private static PdfPCell createCellFromParagraph(Paragraph paragraph) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);

        int i = 0;
        while (i < paragraph.size()) {
            if (paragraph.get(i) instanceof Chunk) {
                i = processChunks(paragraph, i, cell);
            } else {
                cell.addElement(paragraph.get(i));
            }
            i++;
        }

        return cell;
    }

    private static int processChunks(Paragraph paragraph, int startIndex, PdfPCell cell) {
        Paragraph subParagraph = new Paragraph();
        boolean hasNewLine = false;
        int i = startIndex;

        while (i < paragraph.size() && paragraph.get(i) instanceof Chunk chunk) {
            if (chunk.getContent().equals("\n")) {
                hasNewLine = true;
                break;
            } else {
                subParagraph.add(chunk);
            }
            i++;
        }

        subParagraph.setLeading(paragraph.getLeading());
        cell.addElement(subParagraph);

        if (hasNewLine) {
            cell.addElement(new Chunk("\n"));
        }

        return i - 1; // Adjust index to account for the outer loop increment
    }


    /**
     * Adds a <CODE>PdfWriter</CODE> to the <CODE>PdfDocument</CODE>.
     *
     * @param writer the <CODE>PdfWriter</CODE> that writes everything what is added to this document to an
     *               outputstream.
     * @throws DocumentException on error
     */
    public void addWriter(PdfWriter writer) throws DocumentException {
        if (this.writer == null) {
            this.writer = writer;
            annotationsImp = new PdfAnnotationsImp(writer);
            return;
        }
        throw new DocumentException(
                MessageLocalization.getComposedMessage("you.can.only.add.a.writer.to.a.pdfdocument.once"));
    }

    /**
     * Getter for the current leading.
     *
     * @return the current leading
     * @since 2.1.2
     */
    public float getLeading() {
        return leading;
    }

    /**
     * Setter for the current leading.
     *
     * @param leading the current leading
     * @since 2.1.6
     */
    void setLeading(float leading) {
        this.leading = leading;
    }

    /**
     * Signals that an <CODE>Element</CODE> was added to the <CODE>Document</CODE>.
     *
     * @param element the element to add
     * @return <CODE>true</CODE> if the element was added, <CODE>false</CODE> if not.
     * @throws DocumentException when a document isn't open yet, or has been closed
     */
    @Override
    public boolean add(Element element) throws DocumentException {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        try {
            boolean result = switch (element.type()) {
                case Element.HEADER, Element.TITLE, Element.SUBJECT, Element.KEYWORDS, Element.AUTHOR, Element.CREATOR,
                     Element.PRODUCER, Element.CREATIONDATE, Element.MODIFICATIONDATE -> handleMetaElement(element);
                case Element.CHUNK -> handleChunkElement((Chunk) element);
                case Element.ANCHOR -> handleAnchorElement((Anchor) element);
                case Element.ANNOTATION -> handleAnnotationElement((Annotation) element);
                case Element.PHRASE -> handlePhraseElement((Phrase) element);
                case Element.PARAGRAPH -> handleParagraphElement((Paragraph) element);
                case Element.SECTION, Element.CHAPTER -> handleSectionElement((Section) element);
                case Element.LIST -> handleListElement((List) element);
                case Element.LISTITEM -> handleListItemElement((ListItem) element, element);
                case Element.RECTANGLE_CONST -> handleRectangleElement((Rectangle) element);
                case Element.PTABLE -> handlePTableElement((PdfPTable) element);
                case Element.MULTI_COLUMN_TEXT -> handleMultiColumnTextElement((MultiColumnText) element);
                case Element.TABLE -> handleTableElement((Table) element);
                case Element.JPEG, Element.JPEG2000, Element.JBIG2, Element.IMGRAW, Element.IMGTEMPLATE ->
                        handleImageElement((Image) element);
                case Element.YMARK -> handleYmarkElement((DrawInterface) element);
                case Element.MARKED -> handleMarkedElement((MarkedObject) element);
                default -> false;
            };
            lastElementType = element.type();
            return result;
        } catch (Exception e) {
            throw new DocumentException(e);
        }
    }

    private boolean handleMetaElement(Element element) {
        Meta meta = (Meta) element;
        switch (element.type()) {
            case Element.HEADER:
                info.addkey(meta.getName(), meta.getContent());
                break;
            case Element.TITLE:
                info.addTitle(meta.getContent());
                break;
            case Element.SUBJECT:
                info.addSubject(meta.getContent());
                break;
            case Element.KEYWORDS:
                info.addKeywords(meta.getContent());
                break;
            case Element.AUTHOR:
                info.addAuthor(meta.getContent());
                break;
            case Element.CREATOR:
                info.addCreator(meta.getContent());
                break;
            case Element.PRODUCER:
                info.addProducer(meta.getContent());
                break;
            case Element.CREATIONDATE:
                info.addCreationDate();
                break;
            case Element.MODIFICATIONDATE:
                PdfDate date = new PdfDate(meta.getContent());
                info.addModificationDate(date);
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean handleChunkElement(Chunk chunk) {
        if (line == null) {
            carriageReturn();
        }
        PdfChunk pdfChunk = new PdfChunk(chunk, anchorAction);
        if (!pdfChunk.isTab()) {
            PdfChunk overflow;
            while ((overflow = line.add(pdfChunk)) != null) {
                carriageReturn();
                pdfChunk = overflow;
                pdfChunk.trimFirstSpace();
            }
        } else {
            line.add(pdfChunk);
        }
        pageEmpty = false;
        if (pdfChunk.isAttribute(Chunk.NEWPAGE)) {
            newPage();
        }
        return true;
    }

// Other element handling methods follow a similar pattern:

    private boolean handleAnchorElement(Anchor anchor) {
        leadingCount++;
        String url = anchor.getReference();
        leading = anchor.getLeading();
        if (url != null) {
            anchorAction = new PdfAction(url);
        }
        anchor.process(this);
        anchorAction = null;
        leadingCount--;
        return true;
    }

    private boolean handleAnnotationElement(Annotation annot) throws IOException {
        if (line == null) {
            carriageReturn();
        }
        Rectangle rect = new Rectangle(0, 0);
        if (line != null) {
            rect = new Rectangle(annot.llx(indentRight() - line.widthLeft()),
                    annot.uryMethod(indentTop() - currentHeight - 20),
                    annot.urxMethod(indentRight() - line.widthLeft() + 20),
                    annot.llyMethod(indentTop() - currentHeight));
        }
        PdfAnnotation an = PdfAnnotationsImp.convertAnnotation(writer, annot, rect);
        annotationsImp.addPlainAnnotation(an);
        pageEmpty = false;
        return true;
    }

    private boolean handlePhraseElement(Phrase phrase) {
        leadingCount++;
        leading = phrase.getLeading();
        phrase.process(this);
        leadingCount--;
        return true;
    }

    private boolean handleParagraphElement(Paragraph paragraph) {
        leadingCount++;
        leading = paragraph.getLeading();
        paragraph.process(this);
        leadingCount--;
        return true;
    }

    private boolean handleSectionElement(Section section) {
        if (section.isTriggerNewPage()) {
            newPage();
        }
        leadingCount++;
        section.process(this);
        leadingCount--;
        return true;
    }

    private boolean handleListElement(List list) {
        leadingCount++;
        leading = list.getTotalLeading();
        list.process(this);
        leadingCount--;
        return true;
    }

    private boolean handleListItemElement(ListItem listItem, Element element) {
        leadingCount++;
        leading = listItem.getLeading();
        element.process(this);
        leadingCount--;
        return true;
    }

    private boolean handleRectangleElement(Rectangle rectangle) {
        graphics.rectangle(rectangle.getLeft(), rectangle.getBottom(), rectangle.getWidth(), rectangle.getHeight());
        if (rectangle.getBackgroundColor() != null) {
            graphics.setColorFill(rectangle.getBackgroundColor());
        }
        graphics.fill();
        pageEmpty = false;
        return true;
    }

    private boolean handlePTableElement(PdfPTable table) {
        try {
            table.writeSelectedRows(0, -1, indentLeft(), indentTop() - currentHeight, writer.getDirectContent());
            currentHeight += table.getTotalHeight();
        } catch (DocumentException e) {
            return false;
        }
        pageEmpty = false;
        return true;
    }

    private boolean handleMultiColumnTextElement(MultiColumnText multiColumnText) {
        try {
            currentHeight += multiColumnText.write(writer.getDirectContent(), writer.getPdfDocument(), indentTop() - currentHeight);
        } catch (DocumentException e) {
            return false;
        }
        pageEmpty = false;
        return true;
    }

    private boolean handleImageElement(Image image) {
        if (isDoFooter) {
            addDelay(image);
        } else {
            add(image);
        }
        return true;
    }

    private boolean handleYmarkElement(DrawInterface zh) {
        zh.draw(graphics, indentLeft(), indentBottom(), indentRight(), indentTop(),
                indentTop() - currentHeight - (leadingCount > 0 ? leading : 0));
        pageEmpty = false;
        return true;
    }

    private boolean handleMarkedElement(MarkedObject mo) {
        MarkedObject title = ((MarkedSection) mo).getTitle();
        if (title != null) {
            title.process(this);
        }
        mo.process(this);
        return true;
    }

// Handle additional cases or default behavior as needed


    private boolean handleTableElement(Table element) throws BadElementException, DocumentException {
        PdfPTable ptable = element.createPdfPTable();
        if (ptable.size() <= ptable.getHeaderRows()) {
            return false; //nothing to do
        }
        // before every table, we add a new line and flush all lines
        ensureNewLine();
        flushLines();
        addPTable(ptable);
        pageEmpty = false;
        return true;
    }

//    Info Dictionary and Catalog

    /**
     * Opens the document.
     * <p>
     * You have to open the document before you can begin to add content to the body of the document.
     */
    @Override
    public void open() {
        if (!open) {
            super.open();
            writer.open();
            rootOutline = new PdfOutline(writer);
            currentOutline = rootOutline;
        }
        try {
            initPage();
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    /**
     * Closes the document.
     * <p>
     * Once all the content has been written in the body, you have to close the body. After that nothing can be written
     * to the body anymore.
     * </p>
     */
    @Override
    public void close() {
        if (close) {
            return;
        }
        try {
            boolean wasImage = (imageWait != null);
            newPage();
            if (imageWait != null || wasImage) {
                newPage();
            }
            if (annotationsImp.hasUnusedAnnotations()) {
                throw new DocumentException(MessageLocalization.getComposedMessage(
                        "not.all.annotations.could.be.added.to.the.document.the.document.doesn.t.have.enough.pages"));
            }
            PdfPageEvent pageEvent = writer.getPageEvent();
            if (pageEvent != null) {
                pageEvent.onCloseDocument(writer, this);
            }
            super.close();

            writer.addLocalDestinations((TreeMap<String, Object>) localDestinations);
            calculateOutlineCount();
            writeOutlines();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }

        writer.close();
    }

    /**
     * Use this method to set the XMP Metadata.
     *
     * @param xmpMetadata The xmpMetadata to set.
     */
    public void setXmpMetadata(byte[] xmpMetadata) {
        this.xmpMetadata = xmpMetadata;
    }

//    [C1] outlines

    /**
     * Makes a new page and sends it to the <CODE>PdfWriter</CODE>.
     *
     * @return a <CODE>boolean</CODE>
     */
    @Override
    public boolean newPage() {
        lastElementType = -1;
        if (isPageEmpty()) {
            setNewPageSizeAndMargins();
            return false;
        }

        validatePageState();

        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null) {
            pageEvent.onEndPage(writer, this);
        }

        // Added to inform any listeners that we are moving to a new page (added by David Freels)
        super.newPage();

        // Initialize page indentation (added by Pelikan Stephan)
        resetIndentation();

        try {
            flushLines();
            PdfPage page = createPage();
            processPageMetadata(page);
            addPageContent(page);
            initPage();
        } catch (DocumentException | IOException de) {
            throw new ExceptionConverter(de);
        }
        return true;
    }

    private void validatePageState() {
        if (!open || close) {
            throw new DocumentException(MessageLocalization.getComposedMessage("the.document.is.not.open"));
        }
    }

    private void resetIndentation() {
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
    }

    private PdfPage createPage() {
        PdfDictionary resources = preparePageResources();
        PdfPage page = new PdfPage(new PdfRectangle(pageSize, pageSize.getRotationPdfPCell()), thisBoxSize, resources, pageSize.getRotationPdfPCell());
        page.put(PdfName.TABS, writer.getTabs());
        return page;
    }

    private PdfDictionary preparePageResources() {
        PdfDictionary resources = pageResources.getResources();
        pageResources.addDefaultColorDiff(writer.getDefaultColorspace());
        if (writer.isRgbTransparencyBlending()) {
            PdfDictionary dcs = new PdfDictionary();
            dcs.put(PdfName.CS, PdfName.DEVICERGB);
            pageResources.addDefaultColorDiff(dcs);
        }
        return resources;
    }

    private void processPageMetadata(PdfPage page) throws IOException {
        if (xmpMetadata != null) {
            addMetadataToPage(page);
        }
        addPageTransitions(page);
        addPageActions(page);
        addThumbnail(page);
        addUserUnit(page);
        addAnnotations(page);
        addTagInfo(page);
    }

    private void addMetadataToPage(PdfPage page) throws IOException {
        PdfStream xmp = new PdfStream(xmpMetadata);
        xmp.put(PdfName.TYPE_CONST, PdfName.METADATA);
        xmp.put(PdfName.SUBTYPE, PdfName.XML);
        PdfEncryption crypto = writer.getEncryption();
        if (crypto != null && !crypto.isMetadataEncrypted()) {
            PdfArray ar = new PdfArray();
            ar.add(PdfName.CRYPT);
            xmp.put(PdfName.FILTER, ar);
        }
        page.put(PdfName.METADATA, writer.addToBody(xmp).getIndirectReference());
    }

    private void addPageTransitions(PdfPage page) {
        if (this.transition != null) {
            page.put(PdfName.TRANS, this.transition.getTransitionDictionary());
            transition = null;
        }
        if (this.duration > 0) {
            page.put(PdfName.DUR, new PdfNumber(this.duration));
            duration = 0;
        }
    }

    private void addPageActions(PdfPage page) throws IOException {
        if (pageAA != null) {
            page.put(PdfName.AA, writer.addToBody(pageAA).getIndirectReference());
            pageAA = null;
        }
    }

    private void addThumbnail(PdfPage page) {
        if (thumb != null) {
            page.put(PdfName.THUMB, thumb);
            thumb = null;
        }
    }

    private void addUserUnit(PdfPage page) {
        if (writer.getUserunit() > 0f) {
            page.put(PdfName.USERUNIT, new PdfNumber(writer.getUserunit()));
        }
    }

    private void addAnnotations(PdfPage page) {
        if (annotationsImp.hasUnusedAnnotations()) {
            PdfArray array = annotationsImp.rotateAnnotations(writer, pageSize);
            if (!array.isEmpty()) {
                page.put(PdfName.ANNOTS, array);
            }
        }
    }

    private void addTagInfo(PdfPage page) {
        if (writer.isTagged()) {
            int pageIdValue = writer.getStructureTreeRoot()
                    .getOrCreatePageKey(writer.getCurrentPageNumber() - 1);
            page.put(PdfName.STRUCTPARENTS, new PdfNumber(pageIdValue));
        }
    }

    private void addPageContent(PdfPage page) {
        if (text.size() > textEmptySize) {
            text.endText();
        } else {
            text = null;
        }
        writer.add(page, new PdfContents(writer.getDirectContentUnder(), graphics, text, writer.getDirectContent(), pageSize));
    }


    /**
     * Sets the pagesize.
     *
     * @param pageSize the new pagesize
     * @return <CODE>true</CODE> if the page size was set
     */
    @Override
    public boolean setPageSize(Rectangle pageSize) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextPageSize = new Rectangle(pageSize);
        return true;
    }

    /**
     * Sets the margins.
     *
     * @param marginLeft   the margin on the left
     * @param marginRight  the margin on the right
     * @param marginTop    the margin on the top
     * @param marginBottom the margin on the bottom
     * @return a <CODE>boolean</CODE>
     */
    @Override
    public boolean setMargins(float marginLeft, float marginRight, float marginTop, float marginBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        nextMarginLeft = marginLeft;
        nextMarginRight = marginRight;
        nextMarginTop = marginTop;
        nextMarginBottom = marginBottom;
        return true;
    }

    /**
     * @see DocListener#setMarginMirroring(boolean)
     */
    @Override
    public boolean setMarginMirroring(boolean marginMirroring) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroring(marginMirroring);
    }

    /**
     * @see DocListener#setMarginMirroring(boolean)
     * @since 2.1.6
     */
    @Override
    public boolean setMarginMirroringTopBottom(boolean marginMirroringTopBottom) {
        if (writer != null && writer.isPaused()) {
            return false;
        }
        return super.setMarginMirroringTopBottom(marginMirroringTopBottom);
    }

    /**
     * Sets the page number.
     *
     * @param pageN the new page number
     */
    @Override
    public void setPageCount(int pageN) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setPageCount(pageN);
    }

    /**
     * Sets the page number to 0.
     */
    @Override
    public void resetPageCount() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetPageCount();
    }

    /**
     * Changes the header of this document.
     *
     * @param header the new header
     */
    @Override
    public void setHeader(HeaderFooter header) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setHeader(header);
    }

//  [C3] PdfViewerPreferences interface

    /**
     * Resets the header of this document.
     */
    @Override
    public void resetHeader() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetHeader();
    }

    /**
     * Changes the footer of this document.
     *
     * @param footer the new footer
     */
    @Override
    public void setFooter(HeaderFooter footer) {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.setFooter(footer);
    }

    /**
     * Resets the footer of this document.
     */
    @Override
    public void resetFooter() {
        if (writer != null && writer.isPaused()) {
            return;
        }
        super.resetFooter();
    }

//    [C4] Page labels

    /**
     * Initializes a page.
     * <p>
     * If the footer/header is set, it is printed.
     *
     * @throws DocumentException on error
     */
    protected void initPage() throws DocumentException {
        // the pagenumber is incremented
        pageN++;

        // initialization of some page objects
        annotationsImp.resetAnnotations();
        pageResources = new PageResources();

        writer.resetContent();
        graphics = new PdfContentByte(writer);
        text = new PdfContentByte(writer);
        text.reset();
        text.beginText();
        textEmptySize = text.size();

        markPoint = 0;
        setNewPageSizeAndMargins();
        imageEnd = -1;
        indentation.imageIndentRight = 0;
        indentation.imageIndentLeft = 0;
        indentation.indentBottom = 0;
        indentation.indentTop = 0;
        currentHeight = 0;

        // backgroundcolors, etc...
        thisBoxSize = new HashMap<>(boxSize);
        if (pageSize.getBackgroundColor() != null
                || pageSize.hasBorders()
                || pageSize.getBorderColor() != null) {
            add(pageSize);
        }

        float oldleading = leading;
        int oldAlignment = alignment;
        // if there is a footer, the footer is added
        doFooter();
        // we move to the left/top position of the page
        text.moveText(left(), top());
        doHeader();
        pageEmpty = true;
        // if there is an image waiting to be drawn, draw it
        try {
            if (imageWait != null) {
                add(imageWait);
                imageWait = null;
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        leading = oldleading;
        alignment = oldAlignment;
        carriageReturn();

        PdfPageEvent pageEvent = writer.getPageEvent();
        if (pageEvent != null) {
            if (firstPageEvent) {
                pageEvent.onOpenDocument(writer, this);
            }
            pageEvent.onStartPage(writer, this);
        }
        firstPageEvent = false;
    }

    /**
     * Adds the current line to the list of lines and also adds an empty line.
     *
     * @throws DocumentException on error
     */
    protected void newLine() throws DocumentException {
        lastElementType = -1;
        carriageReturn();
        if (lines != null && !lines.isEmpty()) {
            lines.add(line);
            currentHeight += line.height();
        }
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

//    [C5] named objects: local destinations, javascript, embedded files

    /**
     * If the current line is not empty or null, it is added to the arraylist of lines and a new empty line is added.
     */
    protected void carriageReturn() {
        // the arraylist with lines may not be null
        if (lines == null) {
            lines = new ArrayList<>();
        }
        // If the current line is not null
        if (line != null) {
            // we check if the end of the page is reached (bugfix by Francois Gravel)
            if (currentHeight + line.height() + leading < indentTop() - indentBottom()) {
                // if so nonempty lines are added and the height is augmented
                if (line.size() > 0) {
                    currentHeight += line.height();
                    lines.add(line);
                    pageEmpty = false;
                }
            } else {
                // if the end of the line is reached, we start a new page
                newPage();
            }
        }
        if (imageEnd > -1 && currentHeight > imageEnd) {
            imageEnd = -1;
            indentation.imageIndentRight = 0;
            indentation.imageIndentLeft = 0;
        }
        // a new current line is constructed
        line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
    }

    /**
     * Gets the current vertical page position.
     *
     * @param ensureNewLine Tells whether a new line shall be enforced. This may cause side effects for elements that do
     *                      not terminate the lines they've started because those lines will get terminated.
     * @return The current vertical page position.
     */
    public float getVerticalPosition(boolean ensureNewLine) {
        // ensuring that a new line has been started.
        if (ensureNewLine) {
            ensureNewLine();
        }
        return top() - currentHeight - indentation.indentTop;
    }

    /**
     * Ensures that a new line has been started.
     */
    protected void ensureNewLine() {
        try {
            if ((lastElementType == Element.PHRASE) ||
                    (lastElementType == Element.CHUNK)) {
                newLine();
                flushLines();
            }
        } catch (DocumentException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    /**
     * Writes all the lines to the text-object.
     *
     * @return the displacement that was caused
     * @throws DocumentException on error
     */
    protected float flushLines() throws DocumentException {
        // checks if the ArrayList with the lines is not null
        if (lines == null) {
            return 0;
        }
        // checks if a new Line has to be made.
        if (line != null && line.size() > 0) {
            lines.add(line);
            line = new PdfLine(indentLeft(), indentRight(), alignment, leading);
        }

        // checks if the ArrayList with the lines is empty
        if (lines.isEmpty()) {
            return 0;
        }

        // initialization of some parameters
        Object[] currentValues = new Object[2];
        PdfFont currentFont = null;
        float displacement = 0;
        PdfLine l;
        currentValues[1] = 0.0F;
        // looping over all the lines
        for (PdfLine line1 : lines) {

            // this is a line in the loop
            l = line1;

            float moveTextX = l.indentLeft() - indentLeft() + indentation.indentLeft + indentation.listIndentLeft
                    + indentation.sectionIndentLeft;
            text.moveText(moveTextX, -l.height());
            // is the line preceded by a symbol?
            if (l.listSymbol() != null) {
                ColumnText.showTextAligned(graphics, Element.ALIGN_LEFT, new Phrase(l.listSymbol()),
                        text.getXTLM() - l.listIndent(), text.getYTLM(), 0);
            }

            currentValues[0] = currentFont;

            writeLineToContent(l, text, graphics, currentValues, writer.getSpaceCharRatio());

            currentFont = (PdfFont) currentValues[0];
            displacement += l.height();
            text.moveText(-moveTextX, 0);

        }
        lines = new ArrayList<>();
        return displacement;
    }

    /**
     * Writes a text line to the document. It takes care of all the attributes.
     * <p>
     * Before entering the line position must have been established and the
     * <CODE>text</CODE> argument must be in text object scope (<CODE>beginText()</CODE>).
     *
     * @param line          the line to be written
     * @param text          the <CODE>PdfContentByte</CODE> where the text will be written to
     * @param graphics      the <CODE>PdfContentByte</CODE> where the graphics will be written to
     * @param currentValues the current font and extra spacing values
     * @param ratio the
     * @throws DocumentException on error
     */
    void writeLineToContent(PdfLine line, PdfContentByte text, PdfContentByte graphics, Object[] currentValues, float ratio) throws DocumentException {
        PdfFont currentFont = (PdfFont) (currentValues[0]);
        float lastBaseFactor = (Float) (currentValues[1]);
        PdfChunk chunk;

        LineLayoutParams lineLayoutParams = initializeLineLayoutParams();
        ChunkAttributes attributes = initializeChunkAttributes();

        lastBaseFactor = adjustLayoutParams(line, lastBaseFactor, ratio, lineLayoutParams);

        boolean adjustMatrix = false;

        for (Iterator<PdfChunk> j = line.iterator(); j.hasNext(); ) {
            chunk = j.next();
            attributes.setColor(chunk.color());
            lineLayoutParams.setHScale(1);

            handleChunkAttributes(chunk, text, graphics, lastBaseFactor, line, lineLayoutParams, attributes);

            setFontIfDifferent(chunk, text, currentFont);
            renderChunkText(chunk, text, attributes, lineLayoutParams);

            if (chunk.isImage() || chunk.isAttribute(Chunk.SKEW) || chunk.isAttribute(Chunk.HSCALE)) {
                adjustMatrix = true;
            }
        }

        currentValues[0] = finalizeContent(text, lineLayoutParams, adjustMatrix, line, currentFont, lastBaseFactor,
                attributes)[0];
        currentValues[1] = finalizeContent(text, lineLayoutParams, adjustMatrix, line, currentFont, lastBaseFactor,
                attributes)[1];
    }

    private LineLayoutParams initializeLineLayoutParams() {
        // Inizializza e restituisce i parametri del layout della linea
        return new LineLayoutParams(false, 0, 0, 0, 0, 0, 0);
    }

    private ChunkAttributes initializeChunkAttributes() {
        // Inizializza e restituisce gli attributi del chunk
        return new ChunkAttributes(0, 0, 0, 0, 0, null, 0);
    }

    private void setFontIfDifferent(PdfChunk chunk, PdfContentByte text, PdfFont currentFont) {
        // Imposta il font se diverso da quello corrente
        if (!chunk.font().equals(currentFont)) {
            currentFont = chunk.font();
            text.setFontAndSize(currentFont.getFont(), currentFont.size());
        }
    }

    private void renderChunkText(PdfChunk chunk, PdfContentByte text, ChunkAttributes attributes, LineLayoutParams params) throws DocumentException {
        setRenderingMode(chunk, text, attributes);
        setRiseAndColor(chunk, text, attributes);

        if (chunk.isImage()) {
            // Logica per il rendering dell'immagine, se necessario
        } else if (chunk.isVerticalSeparator()) {
            // Gestione del separatore verticale
        } else if (chunk.isHorizontalSeparator()) {
            handleHorizontalSeparator(chunk, text, params);
        } else if (chunk.isTab()) {
            handleTabChunk(chunk, text, attributes, params);
        } else if (params.getJustified() && params.getNumberOfSpaces() > 0 && chunk.isSpecialEncoding()) {
            handleSpecialEncoding(chunk, text, params);
        } else {
            handleStandardText(chunk, text, params);
        }

        resetTextAttributes(text, attributes, params);
    }

    private void setRenderingMode(PdfChunk chunk, PdfContentByte text, ChunkAttributes attributes) {
        // Imposta il modo di rendering in base agli attributi del chunk
        Object[] textRender = (Object[]) chunk.getAttribute(Chunk.TEXTRENDERMODE);
        if (textRender != null) {
            int tr = (Integer) textRender[0] & 3;
            if (tr != PdfContentByte.TEXT_RENDER_MODE_FILL) {
                text.setTextRenderingMode(tr);
            }
            if (tr == PdfContentByte.TEXT_RENDER_MODE_STROKE || tr == PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE) {
                float strokeWidth = (Float) textRender[1];
                if (strokeWidth != 1) {
                    text.setLineWidth(strokeWidth);
                }
                Color strokeColor = (Color) textRender[2];
                if (strokeColor == null) {
                    strokeColor = attributes.getColor();
                }
                if (strokeColor != null) {
                    text.setColorStroke(strokeColor);
                }
            }
        }
    }

    private void setRiseAndColor(PdfChunk chunk, PdfContentByte text, ChunkAttributes attributes) {
        // Imposta il rise del testo e il colore
        Float rise = (Float) chunk.getAttribute(Chunk.SUBSUPSCRIPT);
        if (rise != null) {
            text.setTextRise(rise);
        }
        if (attributes.getColor() != null) {
            text.setColorFill(attributes.getColor());
        }
    }

    private void handleHorizontalSeparator(PdfChunk chunk, PdfContentByte text, LineLayoutParams params) {
        // Gestisce il rendering dei separatori orizzontali
        PdfTextArray array = new PdfTextArray();
        array.add(-params.getGlueWidth() * 1000f / chunk.font().size() / params.getHScale());
        text.showText(array);
    }

    private void handleTabChunk(PdfChunk chunk, PdfContentByte text, ChunkAttributes attributes, LineLayoutParams params) {
        // Gestisce il rendering dei chunk di tipo tabulazione
        PdfTextArray array = new PdfTextArray();
        array.add((attributes.getTabPosition() - attributes.getxMarker()) * 1000f / chunk.font().size() / params.getHScale());
        text.showText(array);
    }

    private void handleSpecialEncoding(PdfChunk chunk, PdfContentByte text, LineLayoutParams params) throws DocumentException {
        // Gestisce il caso di encoding speciale e giustificazione del testo
        if (params.getHScale() != params.getLastHScale()) {
            params.setLastHScale(params.getHScale());
            text.setWordSpacing(params.getBaseWordSpacing() / params.getHScale());
            text.setCharacterSpacing(params.getBaseCharacterSpacing() / params.getHScale() + text.getCharacterSpacing());
        }

        String s = chunk.toString();
        int idx = s.indexOf(' ');
        if (idx < 0) {
            text.showText(s);
        } else {
            float spaceCorrection = -params.getBaseWordSpacing() * 1000f / chunk.font().size() / params.getHScale();
            PdfTextArray textArray = new PdfTextArray();
            textArray.add(s.substring(0, idx));
            int lastIdx = idx;
            while ((idx = s.indexOf(' ', lastIdx + 1)) >= 0) {
                textArray.add(spaceCorrection);
                textArray.add(s.substring(lastIdx, idx));
                lastIdx = idx;
            }
            textArray.add(spaceCorrection);
            textArray.add(s.substring(lastIdx));
            text.showText(textArray);
        }
    }

    private void handleStandardText(PdfChunk chunk, PdfContentByte text, LineLayoutParams params) {
        // Gestisce il rendering standard del testo
        if (params.getJustified() && params.getHScale() != params.getLastHScale()) {
            params.setLastHScale(params.getHScale());
            text.setWordSpacing(params.getBaseWordSpacing() / params.getHScale());
            text.setCharacterSpacing(params.getBaseCharacterSpacing() / params.getHScale() + text.getCharacterSpacing());
        }
        text.showText(chunk.toString());
    }

    private void resetTextAttributes(PdfContentByte text, ChunkAttributes attributes, LineLayoutParams params) {
        // Reimposta le proprietà di rendering di default alla fine di ciascun chunk
        text.setTextRise(0);
        text.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
        if (attributes.getColor() != null) {
            text.resetRGBColorFill();
        }
        if (attributes.getColor() != null) {
            text.resetRGBColorStroke();
        }
        if (params.getGlueWidth() != 1) {
            text.setLineWidth(1);
        }
    }

    private Object[] finalizeContent(PdfContentByte text, LineLayoutParams params, boolean adjustMatrix,
            PdfLine line, PdfFont currentFont, float lastBaseFactor, ChunkAttributes attributes) {
        // Finalizza il contenuto del testo, resetta spaziature e altre impostazioni
        if (params.getJustified()) {
            text.setWordSpacing(0);
            text.setCharacterSpacing(0);
            if (line.isNewlineSplit()) {
                lastBaseFactor = 0;
            }
        }
        if (adjustMatrix) {
            text.moveText(attributes.getBaseXMarker() - text.getXTLM(), 0);
        }

        return new Object[] {currentFont.getFont(), lastBaseFactor};
    }

    private float adjustLayoutParams(PdfLine line, float lastBaseFactor, float ratio, LineLayoutParams params) {
        int separatorCount = line.getSeparatorCount();
        int lineLen = line.getLineLengthUtf32();
        params.setNumberOfSpaces(line.numberOfSpaces());
        params.setJustified(shouldJustify(line, params.getNumberOfSpaces(), lineLen));

        if (separatorCount > 0) {
            adjustGlueWidth(line, params, separatorCount);
        } else if (params.getJustified()) {
            lastBaseFactor = handleJustifiedLine(line, lastBaseFactor, ratio, params, lineLen);
        }

        return lastBaseFactor;
    }

    private boolean shouldJustify(PdfLine line, int numberOfSpaces, int lineLen) {
        return line.hasToBeJustified() && (numberOfSpaces != 0 || lineLen > 1);
    }

    private void adjustGlueWidth(PdfLine line, LineLayoutParams params, int separatorCount) {
        params.setGlueWidth(line.widthLeft() / separatorCount);
    }

    private float handleJustifiedLine(PdfLine line, float lastBaseFactor, float ratio, LineLayoutParams params, int lineLen) {
        if (line.isNewlineSplit() && line.widthLeft() >= (lastBaseFactor * (ratio * params.getNumberOfSpaces() + lineLen - 1))) {
            handleNewlineSplit(line, lastBaseFactor, ratio, params, lineLen);
        } else {
            handleNonNewlineSplit(line, ratio, params, lineLen);
        }
        return lastBaseFactor;
    }

    private void handleNewlineSplit(PdfLine line, float lastBaseFactor, float ratio, LineLayoutParams params, int lineLen) {
        if (line.isRTL() || (LayoutProcessor.isEnabled() && LayoutProcessor.isSet(LAYOUT_RIGHT_TO_LEFT))) {
            text.moveText(line.widthLeft() - lastBaseFactor * (ratio * params.getNumberOfSpaces() + lineLen - 1), 0);
        }
        params.setBaseWordSpacing(ratio * lastBaseFactor);
        params.setBaseCharacterSpacing(lastBaseFactor);
    }

    private void handleNonNewlineSplit(PdfLine line, float ratio, LineLayoutParams params, int lineLen) {
        float width = line.widthLeft();
        applyHangingCorrection(line, params, width);
        float baseFactor = calculateBaseFactor(ratio, params, lineLen, width);
        params.setBaseWordSpacing(ratio * baseFactor);
        params.setBaseCharacterSpacing(baseFactor);
    }

    private void applyHangingCorrection(PdfLine line, LineLayoutParams params, float width) {
        PdfChunk last = line.getChunk(line.size() - 1);
        if (last != null) {
            String s = last.toString();
            char c = s.charAt(s.length() - 1);
            if (!s.isEmpty() && HANGING_PUNCTUATION.indexOf(c) >= 0) {
                float oldWidth = width;
                width += last.font().width(c) * 0.4f;
                params.setHangingCorrection(width - oldWidth);
            }
        }
    }

    private float calculateBaseFactor(float ratio, LineLayoutParams params, int lineLen, float width) {
        return (params.getNumberOfSpaces() == 0 && ratio == PdfWriter.NO_SPACE_CHAR_RATIO) ? 0f
                : width / (ratio * params.getNumberOfSpaces() + lineLen - 1);
    }

    private void handleChunkAttributes(PdfChunk chunk, PdfContentByte text, PdfContentByte graphics, float lastBaseFactor,
            PdfLine line, LineLayoutParams params, ChunkAttributes attributes) {
        int chunkStrokeIdx = 0;
        attributes.setLastChunkStroke(line.getLastStrokeChunk());
        if (chunkStrokeIdx <= attributes.getLastChunkStroke()) {
            initializeMarkers(text, attributes);
            float width = calculateWidth(chunk, params);

            if (chunk.isStroked()) {
                handleStrokedChunk(chunk, text, graphics, lastBaseFactor, params, attributes, line);
            }

            updateMarkerAndIndex(attributes, width);
        }
    }

    private void initializeMarkers(PdfContentByte text, ChunkAttributes attributes) {
        attributes.setxMarker(text.getXTLM());
        attributes.setBaseXMarker(attributes.getxMarker());
        attributes.setyMarker(text.getYTLM());
    }

    private float calculateWidth(PdfChunk chunk, LineLayoutParams params) {
        return params.getJustified()
                ? chunk.getWidthCorrected(params.getBaseCharacterSpacing(), params.getBaseWordSpacing())
                : chunk.width();
    }

    private void handleStrokedChunk(PdfChunk chunk, PdfContentByte text, PdfContentByte graphics, float lastBaseFactor,
            LineLayoutParams params, ChunkAttributes attributes, PdfLine line) {
        PdfChunk nextChunk = line.getChunk(attributes.getChunkStrokeIdx() + 1);
        float width = calculateWidth(chunk, params);
        if (chunk.isSeparator()) {
            drawSeparator(chunk, graphics, attributes, line, params);
        }
        if (chunk.isTab()) {
            drawTab(chunk, graphics, attributes);
        }
        handleBackground(chunk, graphics, lastBaseFactor, params, attributes, nextChunk, width);
        if (chunk.isAttribute(Chunk.UNDERLINE)) {
            handleUnderline(chunk, graphics, lastBaseFactor, params, attributes, nextChunk, width);
        }
        handleAction(chunk, text, lastBaseFactor, attributes, nextChunk, width, params);
        handleRemoteGoto(chunk, attributes, width, lastBaseFactor, params);
        handleLocalGoto(chunk, attributes, width, lastBaseFactor, params);
        handleLocalDestination(chunk, attributes);
        handleGenericTag(chunk, attributes, width, lastBaseFactor, params);
        handleAnnotation(chunk, text, attributes, width, lastBaseFactor, params);
        handleTextMatrixAndScaling(chunk, text, params, attributes);
        handleImage(chunk, graphics, text, attributes, lastBaseFactor);
    }

    private void drawTab(PdfChunk chunk, PdfContentByte graphics, ChunkAttributes attributes) {
        if (chunk.isTab()) {
            Object[] tab = (Object[]) chunk.getAttribute(Chunk.TAB);
            DrawInterface di = (DrawInterface) tab[0];
            attributes.setTabPosition((Float) tab[1] + (Float) tab[3]);
            float fontSize = chunk.font().size();
            float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
            float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);

            // Verifica se la posizione del tab è maggiore della posizione corrente
            if (attributes.getTabPosition() > attributes.getxMarker()) {
                di.draw(graphics, attributes.getxMarker(), attributes.getyMarker() + descender,
                        attributes.getTabPosition(), ascender - descender, attributes.getyMarker());
            }

            // Aggiornamento dei marcatori di posizione
            float tmp = attributes.getxMarker();
            attributes.setxMarker(attributes.getTabPosition());
            attributes.setTabPosition(tmp);
        }
    }

    private void drawSeparator(PdfChunk chunk, PdfContentByte graphics, ChunkAttributes attributes, PdfLine line,
            LineLayoutParams params) {
        float width = params.getGlueWidth();
        Object[] sep = (Object[]) chunk.getAttribute(Chunk.SEPARATOR);
        DrawInterface di = (DrawInterface) sep[0];
        boolean vertical = (boolean) sep[1];
        float fontSize = chunk.font().size();
        float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
        float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
        if (vertical) {
            di.draw(graphics, attributes.getBaseXMarker(), attributes.getyMarker() + descender,
                    attributes.getBaseXMarker() + line.getOriginalWidth(), ascender - descender, attributes.getyMarker());
        } else {
            di.draw(graphics, attributes.getxMarker(), attributes.getyMarker() + descender,
                    attributes.getxMarker() + width, ascender - descender, attributes.getyMarker());
        }
    }

    private void handleBackground(PdfChunk chunk, PdfContentByte graphics, float lastBaseFactor,
            LineLayoutParams params, ChunkAttributes attributes, PdfChunk nextChunk, float width) {
        if (chunk.isAttribute(Chunk.BACKGROUND)) {
            graphics.saveState();
            float subtract = calculateSubtract(nextChunk, lastBaseFactor, params);
            float fontSize = chunk.font().size();
            float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
            float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
            Object[] bgr = (Object[]) chunk.getAttribute(Chunk.BACKGROUND);

            graphics.setColorFill((Color) bgr[0]);
            float[] extra = (float[]) bgr[1];
            graphics.rectangle(attributes.getxMarker() - extra[0],
                    attributes.getyMarker() + descender - extra[1] + chunk.getTextRise(),
                    width - subtract + extra[0] + extra[2], ascender - descender + extra[1] + extra[3]);
            graphics.fill();
            graphics.setGrayFill(0);
            graphics.restoreState();
        }
    }

    private float calculateSubtract(PdfChunk nextChunk, float lastBaseFactor, LineLayoutParams params) {
        float subtract = lastBaseFactor;
        if (nextChunk != null && nextChunk.isAttribute(Chunk.BACKGROUND)) {
            subtract = 0;
        }
        if (nextChunk == null) {
            subtract += params.getHangingCorrection();
        }
        return subtract;
    }

    private void updateMarkerAndIndex(ChunkAttributes attributes, float width) {
        attributes.setxMarker(attributes.getxMarker() + width);
        attributes.setChunkStrokeIdx(attributes.getChunkStrokeIdx() + 1);
    }

    private void handleUnderline(PdfChunk chunk, PdfContentByte graphics, float lastBaseFactor,
            LineLayoutParams params, ChunkAttributes attributes, PdfChunk nextChunk, float width) {
        float subtract = calculateSubtract(nextChunk, lastBaseFactor, params);
        Object[][] unders = (Object[][]) chunk.getAttribute(Chunk.UNDERLINE);
        Color scolor;
        for (Object[] obj : unders) {
            scolor = (Color) obj[0];
            float[] ps = (float[]) obj[1];
            if (scolor == null) {
                scolor = attributes.getColor();
            }
            else {
                graphics.setColorStroke(scolor);
            }
            float fsize = chunk.font().size();
            graphics.setLineWidth(ps[0] + fsize * ps[1]);
            float shift = ps[2] + fsize * ps[3];
            int cap2 = (int) ps[4];
            if (cap2 != 0) {
                graphics.setLineCap(cap2);
            }
            graphics.moveTo(attributes.getxMarker(), attributes.getyMarker() + shift);
            graphics.lineTo(attributes.getxMarker() + width - subtract, attributes.getyMarker() + shift);
            graphics.stroke();
            if (scolor != null) {
                graphics.resetGrayStroke();
            }
            if (cap2 != 0) {
                graphics.setLineCap(0);
            }
        }
        graphics.setLineWidth(1);
    }

    private void handleAction(PdfChunk chunk, PdfContentByte text, float lastBaseFactor,
            ChunkAttributes attributes, PdfChunk nextChunk, float width, LineLayoutParams params) {
        if (chunk.isAttribute(Chunk.ACTION)) {
            float subtract = calculateSubtract(nextChunk, lastBaseFactor, params);
            PdfAnnotation annotation = new PdfAnnotation(writer, attributes.getxMarker(),
                    attributes.getyMarker(), attributes.getxMarker() + width - subtract,
                    attributes.getyMarker() + chunk.font().size(),
                    (PdfAction) chunk.getAttribute(Chunk.ACTION));
            annotation.setFlags(FLAGS_PRINT);
            text.addAnnotation(annotation);
        }
    }

    private void handleRemoteGoto(PdfChunk chunk, ChunkAttributes attributes, float width, float lastBaseFactor,
            LineLayoutParams params) {
        if (chunk.isAttribute(Chunk.REMOTEGOTO)) {
            float subtract = calculateSubtract(null, lastBaseFactor, params);
            Object[] obj = (Object[]) chunk.getAttribute(Chunk.REMOTEGOTO);
            String filename = (String) obj[0];
            if (obj[1] instanceof String string) {
                remoteGoto(filename, string, attributes.getxMarker(), attributes.getyMarker(),
                        attributes.getxMarker() + width - subtract, attributes.getyMarker() + chunk.font().size());
            } else {
                remoteGoto(filename, (Integer) obj[1], attributes.getxMarker(), attributes.getyMarker(),
                        attributes.getxMarker() + width - subtract, attributes.getyMarker() + chunk.font().size());
            }
        }
    }

    private void handleLocalGoto(PdfChunk chunk, ChunkAttributes attributes, float width, float lastBaseFactor,
            LineLayoutParams params) {
        if (chunk.isAttribute(Chunk.LOCALGOTO)) {
            float subtract = calculateSubtract(null, lastBaseFactor, params);
            localGoto((String) chunk.getAttribute(Chunk.LOCALGOTO), attributes.getxMarker(),
                    attributes.getyMarker(), attributes.getxMarker() + width - subtract,
                    attributes.getyMarker() + chunk.font().size());
        }
    }

    private void handleLocalDestination(PdfChunk chunk, ChunkAttributes attributes) {
        if (chunk.isAttribute(Chunk.LOCALDESTINATION)) {
            localDestination((String) chunk.getAttribute(Chunk.LOCALDESTINATION),
                    new PdfDestination(PdfDestination.XYZ, attributes.getxMarker(),
                            attributes.getyMarker() + chunk.font().size(), 0));
        }
    }

    private void handleGenericTag(PdfChunk chunk, ChunkAttributes attributes, float width, float lastBaseFactor,
            LineLayoutParams params) {
        if (chunk.isAttribute(Chunk.GENERICTAG)) {
            float subtract = calculateSubtract(null, lastBaseFactor, params);
            Rectangle rect = new Rectangle(attributes.getxMarker(), attributes.getyMarker(),
                    attributes.getxMarker() + width - subtract, attributes.getyMarker() + chunk.font().size());
            PdfPageEvent pev = writer.getPageEvent();
            if (pev != null) {
                pev.onGenericTag(writer, this, rect, (String) chunk.getAttribute(Chunk.GENERICTAG));
            }
        }
    }

    private void handleAnnotation(PdfChunk chunk, PdfContentByte text, ChunkAttributes attributes, float width,
            float lastBaseFactor, LineLayoutParams params) {
        if (chunk.isAttribute(Chunk.PDFANNOTATION)) {
            float subtract = calculateSubtract(null, lastBaseFactor, params);
            float fontSize = chunk.font().size();
            float ascender = chunk.font().getFont().getFontDescriptor(BaseFont.ASCENT, fontSize);
            float descender = chunk.font().getFont().getFontDescriptor(BaseFont.DESCENT, fontSize);
            PdfAnnotation annot = PdfFormField.shallowDuplicate((PdfAnnotation) chunk.getAttribute(Chunk.PDFANNOTATION));
            annot.put(PdfName.RECT, new PdfRectangle(attributes.getxMarker(), attributes.getyMarker() + descender,
                    attributes.getxMarker() + width - subtract, attributes.getyMarker() + ascender));
            text.addAnnotation(annot);
        }
    }

    private void handleTextMatrixAndScaling(PdfChunk chunk, PdfContentByte text, LineLayoutParams params,
            ChunkAttributes attributes) {
        float[] parameters = (float[]) chunk.getAttribute(Chunk.SKEW);
        Float hs = (Float) chunk.getAttribute(Chunk.HSCALE);
        if (parameters != null || hs != null) {
            float b = 0;
            float c = 0;
            if (parameters != null) {
                b = parameters[0];
                c = parameters[1];
            }
            if (hs != null) {
                params.setHScale(hs);
            }
            text.setTextMatrix(params.getHScale(), b, c, 1, attributes.getxMarker(), attributes.getyMarker());
        }
        if (chunk.isAttribute(Chunk.CHAR_SPACING)) {
            Float cs = (Float) chunk.getAttribute(Chunk.CHAR_SPACING);
            text.setCharacterSpacing(cs);
        }
    }

    private void handleImage(PdfChunk chunk, PdfContentByte graphics, PdfContentByte text,
            ChunkAttributes attributes, float lastBaseFactor) {
        if (chunk.isImage()) {
            Image image = chunk.getImage();
            float[] matrix = image.matrix();
            matrix[Image.CX] = attributes.getxMarker() + chunk.getImageOffsetX() - matrix[Image.CX];
            matrix[Image.CY] = attributes.getyMarker() + chunk.getImageOffsetY() - matrix[Image.CY];
            graphics.addImage(image, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
            text.moveText(attributes.getxMarker() + lastBaseFactor + image.getScaledWidth() - text.getXTLM(), 0);
        }
    }

    /**
     * Gets the indentation on the left side.
     *
     * @return a margin
     */

    protected float indentLeft() {
        return left(indentation.indentLeft + indentation.listIndentLeft + indentation.imageIndentLeft
                + indentation.sectionIndentLeft);
    }

    /**
     * Gets the indentation on the right side.
     *
     * @return a margin
     */

    protected float indentRight() {
        return right(indentation.indentRight + indentation.sectionIndentRight + indentation.imageIndentRight);
    }

    /**
     * Gets the indentation on the top side.
     *
     * @return a margin
     */

    protected float indentTop() {
        return top(indentation.indentTop);
    }

    /**
     * Gets the indentation on the bottom side.
     *
     * @return a margin
     */

    float indentBottom() {
        return bottom(indentation.indentBottom);
    }

    /**
     * Adds extra space. This method should probably be rewritten.
     *
     * @param extraspace extra space
     * @param oldleading old leading
     * @param f          font
     */
    protected void addSpacing(float extraspace, float oldleading, Font f) {
        if (extraspace == 0) {
            return;
        }
        if (pageEmpty) {
            return;
        }
        if (currentHeight + line.height() + leading > indentTop() - indentBottom()) {
            return;
        }
        leading = extraspace;
        carriageReturn();
        if (f.isUnderlined() || f.isStrikethru()) {
            f = new Font(f);
            int style = f.getStyle();
            style &= ~Font.UNDERLINE;
            style &= ~Font.STRIKETHRU;
            f.setStyle(style);
        }
        Chunk space = new Chunk(" ", f);
        space.process(this);
        carriageReturn();
        leading = oldleading;
    }

    /**
     * Gets the <CODE>PdfInfo</CODE>-object.
     *
     * @return <CODE>PdfInfo</CODE>
     */

    protected PdfInfo getInfo() {
        return info;
    }

    /**
     * Gets the <CODE>PdfCatalog</CODE>-object.
     *
     * @param pages an indirect reference to this document pages
     * @return <CODE>PdfCatalog</CODE>
     */

    PdfCatalog getCatalog(PdfIndirectReference pages) {
        PdfCatalog catalog = new PdfCatalog(pages, writer);

        // [C1] outlines
        if (rootOutline.getKids().isEmpty()) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES);
            catalog.put(PdfName.OUTLINES, rootOutline.indirectReference());
        }

        // [C2] version
        writer.getPdfVersion().addToCatalog(catalog);

        // [C3] preferences
        viewerPreferences.addToCatalog(catalog);

        // [C4] pagelabels
        if (pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pageLabels.getDictionary(writer));
        }

        // [C5] named objects
        catalog.addNames(localDestinations, getDocumentLevelJS(), documentFileAttachment, writer);

        // [C6] actions
        if (openActionName != null) {
            PdfAction action = getLocalGotoAction(openActionName);
            catalog.setOpenAction(action);
        } else if (openActionAction != null) {
            catalog.setOpenAction(openActionAction);
        }
        if (additionalActions != null) {
            catalog.setAdditionalActions(additionalActions);
        }

        // [C7] portable collections
        if (collection != null) {
            catalog.put(PdfName.COLLECTION, collection);
        }

        // [C8] AcroForm
        if (annotationsImp.hasValidAcroForm()) {
            try {
                catalog.put(PdfName.ACROFORM, writer.addToBody(annotationsImp.getAcroForm()).getIndirectReference());
            } catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }

        return catalog;
    }

    /**
     * Adds a named outline to the document .
     *
     * @param outline the outline to be added
     * @param name    the name of this local destination
     */
    void addOutline(PdfOutline outline, String name) {
        localDestination(name, outline.getPdfDestination());
    }

    /**
     * Gets the root outline. All the outlines must be created with a parent. The first level is created with this
     * outline.
     *
     * @return the root outline
     */
    public PdfOutline getRootOutline() {
        return rootOutline;
    }

    /**
     * Updates the count in the outlines.
     */
    void calculateOutlineCount() {
        if (rootOutline.getKids().isEmpty()) {
            return;
        }
        traverseOutlineCount(rootOutline);
    }

    /**
     * Recursive method to update the count in the outlines.
     */
    void traverseOutlineCount(PdfOutline outline) {
        java.util.List<PdfOutline> kids = outline.getKids();
        PdfOutline parent = outline.parent();
        if (kids.isEmpty()) {
            if (parent != null) {
                parent.setCount(parent.getCount() + 1);
            }
        } else {
            for (PdfOutline kid : kids) {
                traverseOutlineCount(kid);
            }
            if (parent != null) {
                if (outline.isOpen()) {
                    parent.setCount(outline.getCount() + parent.getCount() + 1);
                } else {
                    parent.setCount(parent.getCount() + 1);
                    outline.setCount(-outline.getCount());
                }
            }
        }
    }

//    [C6] document level actions

    /**
     * Writes the outline tree to the body of the PDF document.
     */
    void writeOutlines() throws IOException {
        if (rootOutline.getKids().isEmpty()) {
            return;
        }
        outlineTree(rootOutline);
        writer.addToBody(rootOutline, rootOutline.indirectReference());
    }

    /**
     * Recursive method used to write outlines.
     */
    void outlineTree(PdfOutline outline) throws IOException {
        outline.setIndirectReference(writer.getPdfIndirectReference());
        if (outline.parent() != null) {
            outline.put(PdfName.PARENT, outline.parent().indirectReference());
        }
        java.util.List<PdfOutline> kids = outline.getKids();
        int size = kids.size();
        for (PdfOutline kid1 : kids) {
            outlineTree(kid1);
        }
        for (int k = 0; k < size; ++k) {
            if (k > 0) {
                kids.get(k).put(PdfName.PREV, kids.get(k - 1).indirectReference());
            }
            if (k < size - 1) {
                kids.get(k).put(PdfName.NEXT, kids.get(k + 1).indirectReference());
            }
        }
        if (size > 0) {
            outline.put(PdfName.FIRST, kids.get(0).indirectReference());
            outline.put(PdfName.LAST, kids.get(size - 1).indirectReference());
        }
        for (PdfOutline kid : kids) {
            writer.addToBody(kid, kid.indirectReference());
        }
    }

    /**
     * @see PdfViewerPreferences#setViewerPreferences(int)
     */
    void setViewerPreferences(int preferences) {
        this.viewerPreferences.setViewerPreferences(preferences);
    }

    /**
     * @see PdfViewerPreferences#addViewerPreference(PdfName,
     * PdfObject)
     */
    void addViewerPreference(PdfName key, PdfObject value) {
        this.viewerPreferences.addViewerPreference(key, value);
    }

    /**
     * Sets the page labels
     *
     * @param pageLabels the page labels
     */
    void setPageLabels(PdfPageLabels pageLabels) {
        this.pageLabels = pageLabels;
    }

    /**
     * Implements a link to other part of the document. The jump will be made to a local destination with the same name,
     * that must exist.
     *
     * @param name the name for this link
     * @param llx  the lower left x corner of the activation area
     * @param lly  the lower left y corner of the activation area
     * @param urx  the upper right x corner of the activation area
     * @param ury  the upper right y corner of the activation area
     */
    void localGoto(String name, float llx, float lly, float urx, float ury) {
        PdfAction action = getLocalGotoAction(name);
        PdfAnnotation pdfAnnotation = new PdfAnnotation(writer, llx, lly, urx, ury, action);
        pdfAnnotation.setFlags(FLAGS_PRINT);
        annotationsImp.addPlainAnnotation(pdfAnnotation);
    }

//    [C7] portable collections

    /**
     * Implements a link to another document.
     *
     * @param filename the filename for the remote document
     * @param name     the name to jump to
     * @param llx      the lower left x corner of the activation area
     * @param lly      the lower left y corner of the activation area
     * @param urx      the upper right x corner of the activation area
     * @param ury      the upper right y corner of the activation area
     */
    void remoteGoto(String filename, String name, float llx, float lly, float urx, float ury) {
        annotationsImp.addPlainAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, name)));
    }

    /**
     * Implements a link to another document.
     *
     * @param filename the filename for the remote document
     * @param page     the page to jump to
     * @param llx      the lower left x corner of the activation area
     * @param lly      the lower left y corner of the activation area
     * @param urx      the upper right x corner of the activation area
     * @param ury      the upper right y corner of the activation area
     */
    void remoteGoto(String filename, int page, float llx, float lly, float urx, float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, new PdfAction(filename, page)));
    }

//    [C8] AcroForm

    /**
     * Implements an action in an area.
     *
     * @param action the <CODE>PdfAction</CODE>
     * @param llx    the lower left x corner of the activation area
     * @param lly    the lower left y corner of the activation area
     * @param urx    the upper right x corner of the activation area
     * @param ury    the upper right y corner of the activation area
     */
    void setAction(PdfAction action, float llx, float lly, float urx, float ury) {
        addAnnotation(new PdfAnnotation(writer, llx, lly, urx, ury, action));
    }

    PdfAction getLocalGotoAction(String name) {
        PdfAction action;
        Object[] obj = localDestinations.get(name);
        if (obj == null) {
            obj = new Object[3];
        }
        if (obj[0] == null) {
            if (obj[1] == null) {
                obj[1] = writer.getPdfIndirectReference();
            }
            action = new PdfAction((PdfIndirectReference) obj[1]);
            obj[0] = action;
            localDestinations.put(name, obj);
        } else {
            action = (PdfAction) obj[0];
        }
        return action;
    }

    /**
     * The local destination to where a local goto with the same name will jump to.
     *
     * @param name        the name of this local destination
     * @param destination the <CODE>PdfDestination</CODE> with the jump coordinates
     * @return <CODE>true</CODE> if the local destination was added,
     * <CODE>false</CODE> if a local destination with the same name
     * already existed
     */
    boolean localDestination(String name, PdfDestination destination) {
        Object[] obj = localDestinations.get(name);
        if (obj == null) {
            obj = new Object[3];
        }
        if (obj[2] != null) {
            return false;
        }
        obj[2] = destination;
        localDestinations.put(name, obj);
        if (!destination.hasPage()) {
            destination.addPage(writer.getCurrentPage());
        }
        return true;
    }

    void addJavaScript(PdfAction js) {
        if (js.get(PdfName.JS) == null) {
            throw new ActionException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        }
        try {
            documentLevelJS.put(SIXTEEN_DIGITS.format(jsCounter++), writer.addToBody(js).getIndirectReference());
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    void addJavaScript(String name, PdfAction js) {
        if (js.get(PdfName.JS) == null) {
            throw new ActionException(MessageLocalization.getComposedMessage("only.javascript.actions.are.allowed"));
        }
        try {
            documentLevelJS.put(name, writer.addToBody(js).getIndirectReference());
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

//    [F12] tagged PDF

    HashMap<String, PdfIndirectReference> getDocumentLevelJS() {
        return documentLevelJS;
    }

    void addFileAttachment(String description, PdfFileSpecification fs) throws IOException {
        if (description == null) {
            PdfString desc = (PdfString) fs.get(PdfName.DESC);
            if (desc == null) {
                description = "";
            } else {
                description = PdfEncodings.convertToString(desc.getBytes(), null);
            }
        }
        fs.addDescription(description, true);
        if (description.length() == 0) {
            description = "Unnamed";
        }
        String fn = PdfEncodings.convertToString(new PdfString(description, PdfObject.TEXT_UNICODE).getBytes(), null);
        int k = 0;
        while (documentFileAttachment.containsKey(fn)) {
            ++k;
            fn = PdfEncodings.convertToString(new PdfString(description + " " + k, PdfObject.TEXT_UNICODE).getBytes(),
                    null);
        }
        documentFileAttachment.put(fn, fs.getReference());
    }

    HashMap<String, PdfIndirectReference> getDocumentFileAttachment() {
        return documentFileAttachment;
    }

//    [U1] page sizes

    void setOpenAction(String name) {
        openActionName = name;
        openActionAction = null;
    }

    void setOpenAction(PdfAction action) {
        openActionAction = action;
        openActionName = null;
    }

    void addAdditionalAction(PdfName actionType, PdfAction action) {
        if (additionalActions == null) {
            additionalActions = new PdfDictionary();
        }
        if (action == null) {
            additionalActions.remove(actionType);
        } else {
            additionalActions.put(actionType, action);
        }
        if (additionalActions.size() == 0) {
            additionalActions = null;
        }
    }

    /**
     * Sets the collection dictionary.
     *
     * @param collection a dictionary of type PdfCollection
     */
    public void setCollection(PdfCollection collection) {
        this.collection = collection;
    }

    /**
     * Gets the AcroForm object.
     *
     * @return the PdfAcroform object of the PdfDocument
     */
    PdfAcroForm getAcroForm() {
        return annotationsImp.getAcroForm();
    }

    void setSigFlags(int f) {
        annotationsImp.setSigFlags(f);
    }

    void addCalculationOrder(PdfFormField formField) {
        annotationsImp.addCalculationOrder(formField);
    }

//    [U2] empty pages

    void addAnnotation(PdfAnnotation annot) {
        pageEmpty = false;
        annotationsImp.addAnnotation(annot);
    }

    int getMarkPoint() {
        return markPoint;
    }

    void incMarkPoint() {
        ++markPoint;
    }

//    [U3] page actions

    void setCropBoxSize(Rectangle crop) {
        setBoxSize("crop", crop);
    }

    void setBoxSize(String boxName, Rectangle size) {
        if (size == null) {
            boxSize.remove(boxName);
        } else {
            boxSize.put(boxName, new PdfRectangle(size));
        }
    }

    protected void setNewPageSizeAndMargins() {
        pageSize = nextPageSize;
        if (marginMirroring && (getPageNumber() & 1) == 0) {
            marginRight = nextMarginLeft;
            marginLeft = nextMarginRight;
        } else {
            marginLeft = nextMarginLeft;
            marginRight = nextMarginRight;
        }
        if (marginMirroringTopBottom && (getPageNumber() & 1) == 0) {
            marginTop = nextMarginBottom;
            marginBottom = nextMarginTop;
        } else {
            marginTop = nextMarginTop;
            marginBottom = nextMarginBottom;
        }
    }

    /**
     * Gives the size of a trim, art, crop or bleed box, or null if not defined.
     *
     * @param boxName crop, trim, art or bleed
     * @return Rectangle
     */
    Rectangle getBoxSize(String boxName) {
        PdfRectangle r = thisBoxSize.get(boxName);
        if (r != null) {
            return r.getRectangle();
        }
        return null;
    }

    boolean isPageEmpty() {
        return writer == null || (writer.getDirectContent().size() == 0 && writer.getDirectContentUnder().size() == 0
                && (pageEmpty || writer.isPaused()));
    }

    void setPageEmpty(boolean pageEmpty) {
        this.pageEmpty = pageEmpty;
    }

//    [U8] thumbnail images

    /**
     * Sets the display duration for the page (for presentations)
     *
     * @param seconds the number of seconds to display the page
     */
    void setDuration(int seconds) {
        if (seconds > 0) {
            this.duration = seconds;
        } else {
            this.duration = -1;
        }
    }

    /**
     * Sets the transition for the page
     *
     * @param transition the PdfTransition object
     */
    void setTransition(PdfTransition transition) {
        this.transition = transition;
    }

//    [M0] Page resources contain references to fonts, extgstate, images,...

    void setPageAction(PdfName actionType, PdfAction action) {
        if (pageAA == null) {
            pageAA = new PdfDictionary();
        }
        pageAA.put(actionType, action);
    }

    void setThumbnail(Image image) throws DocumentException {
        thumb = writer.getImageReference(writer.addDirectImageSimple(image));
    }

//    [M3] Images

    PageResources getPageResources() {
        return pageResources;
    }

    /**
     * Getter for property strictImageSequence.
     *
     * @return Value of property strictImageSequence.
     */
    boolean isStrictImageSequence() {
        return this.strictImageSequence;
    }

    /**
     * Setter for property strictImageSequence.
     *
     * @param strictImageSequence New value of property strictImageSequence.
     */
    void setStrictImageSequence(boolean strictImageSequence) {
        this.strictImageSequence = strictImageSequence;
    }

    /**
     * Method added by Pelikan Stephan
     */
    public void clearTextWrap() {
        float tmpHeight = imageEnd - currentHeight;
        if (line != null) {
            tmpHeight += line.height();
        }
        if ((imageEnd > -1) && (tmpHeight > 0)) {
            carriageReturn();
            currentHeight += tmpHeight;
        }
    }

    /**
     * Adds an image to the document.
     *
     * @param image the <CODE>Image</CODE> to add
     * @throws PdfException      on error
     * @throws DocumentException on error
     */

    protected void add(Image image) throws DocumentException {
        if (image.hasAbsoluteY()) {
            addImageToGraphics(image);
            pageEmpty = false;
            return;
        }

        if (needsNewPage(image)) {
            handleImageWait(image);
            return;
        }

        pageEmpty = false;
        handleImageWaitCycle(image);

        float diff = calculateDiff(image);
        float startPosition = calculateStartPosition(image);
        float lowerleft = calculateLowerLeft(image, diff);

        addImageToGraphics(image, startPosition, lowerleft);
        if (!(isTextWrapped(image) || isUnderlying(image))) {
            updateCurrentHeight(image, diff);
            flushLines();
            text.moveText(0, -(image.getScaledHeight() + diff));
            newLine();
        }
    }

    private void addImageToGraphics(Image image) {
        graphics.addImage(image);
    }

    private boolean needsNewPage(Image image) {
        return currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom();
    }

    private void handleImageWait(Image image) {
        if (!strictImageSequence && imageWait == null) {
            imageWait = image;
            return;
        }
        newPage();
        if (currentHeight != 0 && indentTop() - currentHeight - image.getScaledHeight() < indentBottom()) {
            imageWait = image;
        }
    }

    private void handleImageWaitCycle(Image image) {
        if (image == imageWait) {
            imageWait = null;
        }
    }

    private float calculateDiff(Image image) {
        boolean textwrap = isTextWrapped(image);
        return textwrap ? leading + leading / 2 : leading / 2;
    }

    private float calculateStartPosition(Image image) {
        float[] mt = image.matrix();
        float startPosition = indentLeft() - mt[4];
        int mAlignment = image.getAlignment();

        if ((mAlignment & Image.RIGHT) == Image.RIGHT) {
            startPosition = indentRight() - image.getScaledWidth() - mt[4];
        } else if ((mAlignment & Image.MIDDLE) == Image.MIDDLE) {
            startPosition = indentLeft() + ((indentRight() - indentLeft() - image.getScaledWidth()) / 2) - mt[4];
        }

        if (image.hasAbsoluteX()) {
            startPosition = image.getAbsoluteX();
        }

        adjustIndentation(image, mAlignment, startPosition);
        return startPosition;
    }

    private float calculateLowerLeft(Image image, float diff) {
        return indentTop() - currentHeight - image.getScaledHeight() - diff;
    }

    private void addImageToGraphics(Image image, float startPosition, float lowerleft) {
        float[] mt = image.matrix();
        graphics.addImage(image, mt[0], mt[1], mt[2], mt[3], startPosition, lowerleft - mt[5]);
    }

    private boolean isTextWrapped(Image image) {
        return (image.getAlignment() & Image.TEXTWRAP) == Image.TEXTWRAP && ((image.getAlignment() & Image.MIDDLE) != Image.MIDDLE);
    }

    private boolean isUnderlying(Image image) {
        return (image.getAlignment() & Image.UNDERLYING) == Image.UNDERLYING;
    }

    private void adjustIndentation(Image image, int alignment, float startPosition) {
        if (isTextWrapped(image)) {
            if (imageEnd < 0 || imageEnd < currentHeight + image.getScaledHeight() + calculateDiff(image)) {
                imageEnd = currentHeight + image.getScaledHeight() + calculateDiff(image);
            }
            if ((alignment & Image.RIGHT) == Image.RIGHT) {
                indentation.imageIndentRight += image.getScaledWidth() + image.getIndentationLeft();
            } else {
                indentation.imageIndentLeft += image.getScaledWidth() + image.getIndentationRight();
            }
        } else {
            adjustIndentationForNonTextWrapped(image, alignment, startPosition);
        }
    }

    private void adjustIndentationForNonTextWrapped(Image image, int alignment, float startPosition) {
        if ((alignment & Image.RIGHT) == Image.RIGHT) {
            startPosition -= image.getIndentationRight();
        } else if ((alignment & Image.MIDDLE) == Image.MIDDLE) {
            startPosition += image.getIndentationLeft() - image.getIndentationRight();
        } else {
            startPosition += image.getIndentationLeft();
        }
    }

    private void updateCurrentHeight(Image image, float diff) {
        currentHeight += image.getScaledHeight() + diff;
    }


    /**
     * write non-text <CODE>Element</CODE> into document
     */

    protected void flushSpecial() {
        if (footer.getSpecialContent() == null) {
            return;
        }

        for (Element element : footer.getSpecialContent()) {
            switch (element.type()) {
                case Element.JPEG,
                     Element.JPEG2000,
                     Element.JBIG2,
                     Element.IMGRAW,
                     Element.IMGTEMPLATE:
                    processImage((Image) element);
                    break;
                case Element.PTABLE:
                    processTable((PdfPTable) element);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected type: " + element.type());
            }
        }

        footer.setPadding(0);
    }

    private void processImage(Image image) {
        boolean textwrap = (image.getAlignment() & Image.TEXTWRAP) == Image.TEXTWRAP
                && ((image.getAlignment() & Image.MIDDLE) != Image.MIDDLE);
        float diff = leading / 2;
        if (textwrap) {
            diff += leading;
        }

        float lowerleft = footer.getTop() - image.getRelativeTop() - image.getScaledHeight() - diff;
        float[] mt = image.matrix();
        float startPosition = calculateStartPosition(image, mt);

        graphics.addImage(image, mt[0], mt[1], mt[2], mt[3], startPosition, lowerleft - mt[5]);
    }

    private float calculateStartPosition(Image image, float[] mt) {
        float startPosition = indentLeft() - mt[4];

        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
            startPosition = indentRight() - image.getScaledWidth() - mt[4];
        } else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) {
            startPosition = indentLeft() + ((indentRight() - indentLeft() - image.getScaledWidth()) / 2) - mt[4];
        }

        if (image.hasAbsoluteX()) {
            startPosition = image.getAbsoluteX();
        }

        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
            startPosition -= image.getIndentationRight();
        } else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) {
            startPosition += image.getIndentationLeft() - image.getIndentationRight();
        } else {
            startPosition += image.getIndentationLeft();
        }

        return startPosition;
    }

    private void processTable(PdfPTable ptable) {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        ct.addElement(ptable);
        ct.setSimpleColumn(indentLeft(), footer.getBottom(), indentRight(), footer.getTop());
        ct.go();
    }


    /**
     * Occupies space for <CODE>Image</CODE> that will be added later instead of now
     *
     * @param image the new <CODE>Image</CODE>
     */

    protected void addDelay(Image image) {
        if (image.hasAbsoluteY()) {
            logger.info("Warning: absoluteY of image is invalid in footer");
        }

        image.setRelativeTop(currentHeight); // set the offset relative to the top
        image.setAlignment(image.getAlignment() | footer.alignment());
        footer.addSpecialContent(image);

        // add indentation for text
        boolean textwrap = (image.getAlignment() & Image.TEXTWRAP) == Image.TEXTWRAP
                && ((image.getAlignment() & Image.MIDDLE) != Image.MIDDLE);
        boolean underlying = (image.getAlignment() & Image.UNDERLYING) == Image.UNDERLYING;
        float diff = leading / 2;
        if (textwrap) {
            if (imageEnd < 0 || imageEnd < currentHeight + image.getScaledHeight() + diff) {
                imageEnd = currentHeight + image.getScaledHeight() + diff;
            }
            if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
                // indentation suggested by Pelikan Stephan
                indentation.imageIndentRight += image.getScaledWidth() + image.getIndentationLeft();
            } else {
                // indentation suggested by Pelikan Stephan
                indentation.imageIndentLeft += image.getScaledWidth() + image.getIndentationRight();
            }
        }
        // move text
        if (!(textwrap || underlying)) {
            currentHeight += image.getScaledHeight() + diff;
            flushLines();
            text.moveText(0, -(image.getScaledHeight() + diff));
            newLine();
        } else {
            footer.addPadding(image.getScaledHeight() + diff);
        }
    }

    /**
     * Occupies space for <CODE>PdfPTable</CODE> that will be added later instead of now
     *
     * @param table the new <CODE>PdfPTable</CODE>
     */
    protected void delayTableAddition(PdfPTable table) {
        setTableWidth(table);
        final float footerPadding = table.getTotalHeight() - (0.75f * leading);

        footer.addSpecialContent(table);
        footer.addPadding(footerPadding);
    }

    /**
     * Adds a <CODE>PdfPTable</CODE> to the document.
     *
     * @param ptable the <CODE>PdfPTable</CODE> to be added to the document.
     * @throws DocumentException on error
     */
    void addPTable(PdfPTable ptable) throws DocumentException {
        ColumnText ct = new ColumnText(writer.getDirectContent());
        // if the table prefers to be on a single page, and it wouldn't
        //fit on the current page, start a new page.
        if (ptable.getKeepTogether() && !fitsPage(ptable, 0f) && currentHeight > 0) {
            newPage();
        }
        // add dummy paragraph if we aren't at the top of a page, so that
        // spacingBefore will be taken into account by ColumnText
        if (currentHeight > 0 || ptable.isSkipFirstHeader()) {
            Paragraph p = new Paragraph();
            p.setLeading(0);
            ct.addElement(p);
        }
        ct.addElement(ptable);
        boolean he = ptable.isHeadersInEvent();
        ptable.setHeadersInEvent(true);
        int loop = 0;
        while (true) {
            ct.setSimpleColumn(indentLeft(), indentBottom(), indentRight(), indentTop() - currentHeight);
            int status = ct.go();
            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                text.moveText(0, ct.getYLine() - indentTop() + currentHeight);
                currentHeight = indentTop() - ct.getYLine();
                break;
            }
            if (indentTop() - currentHeight == ct.getYLine()) {
                ++loop;
            } else {
                loop = 0;
            }
            if (loop == 3) {
                add(new Paragraph("ERROR: Infinite table loop"));
                break;
            }
            newPage();
        }
        ptable.setHeadersInEvent(he);
    }

    /**
     * Checks if a <CODE>PdfPTable</CODE> fits the current page of the <CODE>PdfDocument</CODE>.
     *
     * @param table  the table that has to be checked
     * @param margin a certain margin
     * @return <CODE>true</CODE> if the <CODE>PdfPTable</CODE> fits the page, <CODE>false</CODE> otherwise.
     */

    boolean fitsPage(PdfPTable table, float margin) {
        setTableWidth(table);
        // ensuring that a new line has been started.
        ensureNewLine();
        return table.getTotalHeight() + ((currentHeight > 0) ? table.spacingBefore() : 0f)
                <= indentTop() - currentHeight - indentBottom() - margin;
    }

    //    [M4] Adding a PdfPTable

    private void setTableWidth(final PdfPTable table) {
        if (!table.isLockedWidth()) {
            float totalWidth = (indentRight() - indentLeft()) * table.getWidthPercentage() / 100;
            table.setTotalWidth(totalWidth);
        }
    }

    /**
     * Returns the bottomvalue of a <CODE>Table</CODE> if it were added to this document.
     *
     * @param table the table that may or may not be added to this document
     * @return a bottom value
     */
    float bottom(Table table) {
        // constructing a PdfTable
        PdfTable tmp = new PdfTable(table, indentLeft(), indentRight(), indentTop() - currentHeight);
        return tmp.getBottom();
    }

    protected void doFooter() throws DocumentException {
        if (footer == null) {
            return;
        }
        isDoFooter = true;
        // Begin added by Edgar Leonardo Prieto Perilla
        // Avoid footer indentation
        float tmpIndentLeft = indentation.indentLeft;
        float tmpIndentRight = indentation.indentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        float tmpListIndentLeft = indentation.listIndentLeft;
        float tmpImageIndentLeft = indentation.imageIndentLeft;
        float tmpImageIndentRight = indentation.imageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29

        indentation.indentLeft = indentation.indentRight = 0;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = 0;
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
        // End added: Bonf (Marc Schneider) 2003-07-29
        // End Added by Edgar Leonardo Prieto Perilla
        footer.setPageNumber(pageN);
        leading = footer.paragraph().getTotalLeading();
        add(footer.paragraph());
        // adding the footer limits the height
        indentation.indentBottom = currentHeight;
        text.moveText(left(), indentBottom());
        flushLines();
        text.moveText(-left(), -bottom());
        footer.setTop(bottom(Math.max(footer.getPadding(), currentHeight)));
        footer.setBottom(bottom() - (0.75f * leading));
        footer.setLeft(left());
        footer.setRight(right());
        graphics.rectangle(footer.llx(), footer.urx(), footer.getWidth(), footer.getHeight());
        flushSpecial();
        indentation.indentBottom = currentHeight + leading * 2;
        currentHeight = 0;
        // Begin added by Edgar Leonardo Prieto Perilla
        indentation.indentLeft = tmpIndentLeft;
        indentation.indentRight = tmpIndentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = tmpListIndentLeft;
        indentation.imageIndentLeft = tmpImageIndentLeft;
        indentation.imageIndentRight = tmpImageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
        // End added by Edgar Leonardo Prieto Perilla
        isDoFooter = false;
    }

    protected void doHeader() throws DocumentException {
        // if there is a header, the header = added
        if (header == null) {
            return;
        }
        // Begin added by Edgar Leonardo Prieto Perilla
        // Avoid header indentation
        float tmpIndentLeft = indentation.indentLeft;
        float tmpIndentRight = indentation.indentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        float tmpListIndentLeft = indentation.listIndentLeft;
        float tmpImageIndentLeft = indentation.imageIndentLeft;
        float tmpImageIndentRight = indentation.imageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
        indentation.indentLeft = indentation.indentRight = 0;
        //  Added: Bonf
        indentation.listIndentLeft = 0;
        indentation.imageIndentLeft = 0;
        indentation.imageIndentRight = 0;
        // End added: Bonf
        // Begin added by Edgar Leonardo Prieto Perilla
        header.setPageNumber(pageN);
        leading = header.paragraph().getTotalLeading();
        text.moveText(0, leading);
        add(header.paragraph());
        newLine();
        indentation.indentTop = currentHeight - leading;
        header.setTop(top() + leading);
        header.setBottom(indentTop() + leading * 2 / 3);
        header.setLeft(left());
        header.setRight(right());
        graphics.rectangle(header.llx(), header.urx(), header.getWidth(), header.getHeight());
        flushLines();
        currentHeight = 0;
        // Begin added by Edgar Leonardo Prieto Perilla
        // Restore indentation
        indentation.indentLeft = tmpIndentLeft;
        indentation.indentRight = tmpIndentRight;
        // Begin added: Bonf (Marc Schneider) 2003-07-29
        indentation.listIndentLeft = tmpListIndentLeft;
        indentation.imageIndentLeft = tmpImageIndentLeft;
        indentation.imageIndentRight = tmpImageIndentRight;
        // End added: Bonf (Marc Schneider) 2003-07-29
        // End Added by Edgar Leonardo Prieto Perilla
    }

    /**
     * <CODE>PdfInfo</CODE> is the PDF InfoDictionary.
     * <p>
     * A document's trailer may contain a reference to an Info dictionary that provides information about the document.
     * This optional dictionary may contain one or more keys, whose values should be strings.<BR> This object is
     * described in the 'Portable Document Format Reference Manual version 1.3' section 6.10 (page 120-121)
     *
     * @since 2.0.8 (PdfDocument was package-private before)
     */

    public static class PdfInfo extends PdfDictionary {

        /**
         * Construct a <CODE>PdfInfo</CODE>-object.
         */

        PdfInfo() {
            super();
        }

        /**
         * Constructs a <CODE>PdfInfo</CODE>-object.
         *
         * @param author  name of the author of the document
         * @param title   title of the document
         * @param subject subject of the document
         */

        PdfInfo(String author, String title, String subject) {
            this();
            addProducer();
            addCreationDate();
            addTitle(title);
            addSubject(subject);
            addAuthor(author);
        }

        /**
         * Adds the title of the document.
         *
         * @param title the title of the document
         */

        void addTitle(String title) {
            put(PdfName.TITLE, new PdfString(title, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the subject to the document.
         *
         * @param subject the subject of the document
         */

        void addSubject(String subject) {
            put(PdfName.SUBJECT, new PdfString(subject, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds some keywords to the document.
         *
         * @param keywords the keywords of the document
         */

        void addKeywords(String keywords) {
            put(PdfName.KEYWORDS, new PdfString(keywords, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the author to the document.
         *
         * @param author the name of the author
         */

        void addAuthor(String author) {
            put(PdfName.AUTHOR, new PdfString(author, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the creator to the document.
         *
         * @param creator the name of the creator
         */

        void addCreator(String creator) {
            put(PdfName.CREATOR, new PdfString(creator, PdfObject.TEXT_UNICODE));
        }

        /**
         * Adds the name of the producer to the document.
         */
        void addProducer() {
            addProducer(getVersion());
        }

        /**
         * Adds the name of the producer to the document.
         *
         * @param producer name of the producer
         */
        void addProducer(final String producer) {
            put(PdfName.PRODUCER, new PdfString(producer));
        }

        /**
         * Adds the date of creation to the document.
         */

        void addCreationDate() {
            PdfString date = new PdfDate();
            put(PdfName.CREATIONDATE, date);
        }

        /**
         * Adds the modification date (=current date and time) to the document.
         */
        void addModificationDate() {
            PdfString date = new PdfDate();
            put(PdfName.MODDATE, date);
        }

        /**
         * Adds the modification date to the document.
         */
        void addModificationDate(PdfDate date) {
            put(PdfName.MODDATE, date);
        }

        void addkey(String key, String value) {
            if (key.equals("Producer") || key.equals("CreationDate")) {
                return;
            }
            put(new PdfName(key), new PdfString(value, PdfObject.TEXT_UNICODE));
        }
    }

//    [M5] header/footer

    /**
     * <CODE>PdfCatalog</CODE> is the PDF Catalog-object.
     * <p>
     * The Catalog is a dictionary that is the root node of the document. It contains a reference to the tree of pages
     * contained in the document, a reference to the tree of objects representing the document's outline, a reference to
     * the document's article threads, and the list of named destinations. In addition, the Catalog indicates whether
     * the document's outline or thumbnail page images should be displayed automatically when the document is viewed and
     * whether some location other than the first page should be shown when the document is opened.<BR> In this class
     * however, only the reference to the tree of pages is implemented.<BR> This object is described in the 'Portable
     * Document Format Reference Manual version 1.3' section 6.2 (page 67-71)
     */

    static class PdfCatalog extends PdfDictionary {

        /**
         * The writer writing the PDF for which we are creating this catalog object.
         */
        PdfWriter writer;

        /**
         * Constructs a <CODE>PdfCatalog</CODE>.
         *
         * @param pages  an indirect reference to the root of the document's Pages tree.
         * @param writer the writer the catalog applies to
         */

        PdfCatalog(PdfIndirectReference pages, PdfWriter writer) {
            super(CATALOG);
            this.writer = writer;
            put(PdfName.PAGES, pages);
        }

        /**
         * Adds the names of the named destinations to the catalog.
         *
         * @param localDestinations      the local destinations
         * @param documentLevelJS        the javascript used in the document
         * @param documentFileAttachment the attached files
         * @param writer                 the writer the catalog applies to
         */
        void addNames(TreeMap<String, Object[]> localDestinations, Map<String, PdfIndirectReference> documentLevelJS,
                Map<String, PdfIndirectReference> documentFileAttachment, PdfWriter writer) {
            if (localDestinations.isEmpty() && documentLevelJS.isEmpty() && documentFileAttachment.isEmpty()) {
                return;
            }
            try {
                PdfDictionary names = new PdfDictionary();
                if (!localDestinations.isEmpty()) {
                    PdfArray ar = new PdfArray();
                    for (Entry<String, Object[]> entry : localDestinations.entrySet()) {
                        String name = entry.getKey();
                        Object[] obj = entry.getValue();
                        if (obj[2] == null) {
                            //no destination
                            continue;
                        }
                        PdfIndirectReference ref = (PdfIndirectReference) obj[1];
                        ar.add(new PdfString(name, null));
                        ar.add(ref);
                    }
                    if (ar.size() > 0) {
                        PdfDictionary dests = new PdfDictionary();
                        dests.put(PdfName.NAMES, ar);
                        names.put(PdfName.DESTS, writer.addToBody(dests).getIndirectReference());
                    }
                }
                if (!documentLevelJS.isEmpty()) {
                    PdfDictionary tree = PdfNameTree.writeTree(documentLevelJS, writer);
                    names.put(PdfName.JAVASCRIPT, writer.addToBody(tree).getIndirectReference());
                }
                if (!documentFileAttachment.isEmpty()) {
                    names.put(PdfName.EMBEDDEDFILES,
                            writer.addToBody(PdfNameTree.writeTree(documentFileAttachment, writer))
                                    .getIndirectReference());
                }
                if (names.size() > 0) {
                    put(PdfName.NAMES, writer.addToBody(names).getIndirectReference());
                }
            } catch (IOException e) {
                throw new ExceptionConverter(e);
            }
        }

        /**
         * Adds an open action to the catalog.
         *
         * @param action the action that will be triggered upon opening the document
         */
        void setOpenAction(PdfAction action) {
            put(PdfName.OPENACTION, action);
        }


        /**
         * Sets the document level additional actions.
         *
         * @param actions dictionary of actions
         */
        void setAdditionalActions(PdfDictionary actions) {
            try {
                put(PdfName.AA, writer.addToBody(actions).getIndirectReference());
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * @since 2.0.8 (PdfDocument was package-private before)
     */
    public static class Indentation {

        /**
         * This represents the current indentation of the PDF Elements on the left side.
         */
        float indentLeft = 0;

        /**
         * Indentation to the left caused by a section.
         */
        float sectionIndentLeft = 0;

        /**
         * This represents the current indentation of the PDF Elements on the left side.
         */
        float listIndentLeft = 0;

        /**
         * This is the indentation caused by an image on the left.
         */
        float imageIndentLeft = 0;

        /**
         * This represents the current indentation of the PDF Elements on the right side.
         */
        float indentRight = 0;

        /**
         * Indentation to the right caused by a section.
         */
        float sectionIndentRight = 0;

        /**
         * This is the indentation caused by an image on the right.
         */
        float imageIndentRight = 0;

        /**
         * This represents the current indentation of the PDF Elements on the top side.
         */
        float indentTop = 0;

        /**
         * This represents the current indentation of the PDF Elements on the bottom side.
         */
        float indentBottom = 0;
    }

    /**
     * This is a helper class for adding a Table to a document.
     *
     * @since 2.0.8 (PdfDocument was package-private before)
     */
    protected static class RenderingContext {

        /**
         * A PdfPTable
         */
        public static final PdfTable table = null;
        float pagetop = -1;
        float oldHeight = -1;
        PdfContentByte cellGraphics = null;
        float lostTableBottom;
        float maxCellBottom;
        float maxCellHeight;
        Map<PdfCell, Integer> rowspanMap = new HashMap<>();
        // Possible keys and values are Set or Integer. Really?
        Map<PdfCell, Integer> pageMap = new HashMap<>();
        Map<Integer, Set<PdfCell>> pageCellSetMap = new HashMap<>();

        /**
         * Consumes the rowspan
         *
         * @param c pdf cell
         * @return a rowspan.
         */
        public int consumeRowspan(PdfCell c) {
            if (c.rowspan() == 1) {
                return 1;
            }

            Integer i = rowspanMap.get(c);
            if (i == null) {
                i = c.rowspan();
            }

            i = i - 1;
            rowspanMap.put(c, i);

            if (i < 1) {
                return 1;
            }
            return i;
        }

        /**
         * Looks at the current rowspan.
         *
         * @param c cell
         * @return the current rowspan
         */
        public int currentRowspan(PdfCell c) {
            Integer i = rowspanMap.get(c);
            if (i == null) {
                return c.rowspan();
            } else {
                return i;
            }
        }

        public int cellRendered(PdfCell cell, int pageNumber) {
            Integer i = pageMap.get(cell);
            if (i == null) {
                i = 1;
            } else {
                i = i + 1;
            }
            pageMap.put(cell, i);

            Integer pageInteger = pageNumber;
            Set<PdfCell> set = pageCellSetMap.computeIfAbsent(pageInteger, k -> new HashSet<>());
            set.add(cell);

            return i;
        }

        public int numCellRendered(PdfCell cell) {
            Integer i = pageMap.get(cell);
            if (i == null) {
                i = 0;
            }
            return i;
        }

        public boolean isCellRenderedOnPage(PdfCell cell, int pageNumber) {
            Integer pageInteger = pageNumber;
            Set<PdfCell> set = pageCellSetMap.get(pageInteger);

            if (set != null) {
                return set.contains(cell);
            }

            return false;
        }
    }
}
