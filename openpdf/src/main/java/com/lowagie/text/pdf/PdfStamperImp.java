/*
 * Copyright 2003 by Paulo Soares.
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.AcroFields.Item;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import com.lowagie.text.xml.xmp.XmpReader;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sun.jdi.InternalException;
import org.xml.sax.SAXException;


class PdfStamperImp extends PdfWriter {

    public static final Logger logger = Logger.getLogger(PdfStamperImp.class.getName());
    public static final String ACTIONTYPE_PDFACTION_ACTION_INT_PAGE = "use.setpageaction.pdfname.actiontype.pdfaction.action.int.page";
    public static final String NS_ADOBE_COM_XAP_1_0 = "http://ns.adobe.com/xap/1.0/";
    public static final String PRODUCER = "Producer";

    protected AcroFields acroFields;
    protected boolean flat = false;
    protected boolean flatFreeText = false;
    protected int[] namePtr = {0};
    protected Set<String> partialFlattening = new HashSet<>();
    protected boolean useVp = false;
    protected PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
    protected Map<PdfTemplate, Object> fieldTemplates = new HashMap<>();
    protected boolean fieldsAdded = false;
    protected int sigFlags = 0;
    protected boolean append;
    protected IntHashtable marked;
    protected int initialXrefSize;
    protected PdfAction openAction;
    HashMap<PdfReader, IntHashtable> readers2intrefs = new HashMap<>();
    HashMap<PdfReader, RandomAccessFileOrArray> readers2file = new HashMap<>();
    RandomAccessFileOrArray file;
    PdfReader reader;
    IntHashtable myXref = new IntHashtable();
    /**
     * Integer(page number) -> PageStamp
     */
    HashMap<PdfDictionary, PageStamp> pagesToContent = new HashMap<>();
    boolean closed = false;
    /**
     * Holds value of property rotateContents.
     */
    private boolean rotateContents = true;
    private boolean includeFileID = true;
    private PdfObject overrideFileId = null;
    private Calendar modificationDate = null;
    private boolean updateMetadata = true;
    private boolean updateDocInfo = true;

    /**
     * Creates new PdfStamperImp.
     *
     * @param reader     the read PDF
     * @param os         the output destination
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original document
     * @throws DocumentException on error
     */
    PdfStamperImp(PdfReader reader, OutputStream os, char pdfVersion, boolean append)
            throws DocumentException, IOException {
        super(new PdfDocument(), os);
        validateReader(reader);
        this.reader = reader;
        file = reader.getSafeFile();
        this.append = append;

        if (append) {
            handleAppendMode(reader);
        } else {
            handleRegularMode(reader, pdfVersion);
        }

        super.open();
        pdf.addWriter(this);

        if (append) {
            configureForAppendMode(reader);
        }

        initialXrefSize = reader.getXrefSize();
    }

    private void validateReader(PdfReader reader) throws DocumentException, BadPasswordException {
        if (!reader.isOpenedWithFullPermissions()) {
            throw new BadPasswordException();
        }
        if (reader.isTampered()) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("the.original.document.was.reused.read.it.again.from.file"));
        }
        reader.setTampered(true);
    }

    private void handleAppendMode(PdfReader reader) throws IOException, DocumentException {
        if (reader.isRebuilt()) {
            throw new DocumentException(MessageLocalization.getComposedMessage(
                    "append.mode.requires.a.document.without.errors.even.if.recovery.was.possible"));
        }
        if (reader.isEncrypted()) {
            crypto = new PdfEncryption(reader.getDecrypt());
        }
        pdfVersion.setAppendmode(true);
        file.reOpen();
        transferFileContentToOutputStream(file, os);
        file.close();
        prevxref = reader.getLastXref();
        reader.setAppendable(true);
    }

    private void transferFileContentToOutputStream(RandomAccessFileOrArray file, OutputStream os) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = file.read(buf)) > 0) {
            os.write(buf, 0, n);
        }
    }

    private void handleRegularMode(PdfReader reader, char pdfVersion) {
        if (pdfVersion == 0) {
            super.setPdfVersion(reader.getPdfVersion());
        } else {
            super.setPdfVersion(pdfVersion);
        }
    }

    private void configureForAppendMode(PdfReader reader) {
        body.setRefnum(reader.getXrefSize());
        marked = new IntHashtable();
        if (reader.isNewXrefType()) {
            fullCompression = true;
        }
        if (reader.isHybridXref()) {
            fullCompression = false;
        }
    }


    static void findAllObjects(PdfReader reader, PdfObject obj, IntHashtable hits) {
        if (obj == null) {
            return;
        }
        switch (obj.type()) {
            case PdfObject.INDIRECT:
                PRIndirectReference iref = (PRIndirectReference) obj;
                if (reader != iref.getReader()) {
                    return;
                }
                if (hits.containsKey(iref.getNumber())) {
                    return;
                }
                hits.put(iref.getNumber(), 1);
                findAllObjects(reader, PdfReader.getPdfObject(obj), hits);
                return;
            case PdfObject.ARRAY:
                PdfArray a = (PdfArray) obj;
                for (int k = 0; k < a.size(); ++k) {
                    findAllObjects(reader, a.getPdfObject(k), hits);
                }
                return;
            case PdfObject.DICTIONARY, PdfObject.STREAM:
                PdfDictionary dic = (PdfDictionary) obj;
                for (PdfName name : dic.getKeys()) {
                    findAllObjects(reader, dic.get(name), hits);
                }
                break;
            default:
                break;
        }
    }

    private static void moveRectangle(PdfDictionary dic2, PdfReader r, int pageImported, PdfName key, String name) {
        Rectangle m = r.getBoxSize(pageImported, name);
        if (m == null) {
            dic2.remove(key);
        } else {
            dic2.put(key, new PdfRectangle(m));
        }
    }

    void close(Map<String, String> moreInfo) throws IOException, NoSuchAlgorithmException {
        if (closed) {
            return;
        }

        handleViewerPreferences();
        handleFields();
        handleCatalogAndAcroForm();
        handleMetadata(moreInfo);
        writeDocument();

        closed = true;
    }

    private void handleViewerPreferences() {
        if (useVp) {
            reader.setViewerPreferences(viewerPreferences);
            markUsed(reader.getTrailer().get(PdfName.ROOT));
        }
    }

    private void handleFields() throws IOException {
        if (flat) {
            flatFields();
        }
        if (flatFreeText) {
            flatFreeTextFields();
        }
        addFieldResources();
    }

    private void handleCatalogAndAcroForm() throws IOException {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary pages = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.PAGES));
        markUsed(pages);

        PdfObject acroFormObject = PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), reader.getCatalog());
        if (acroFormObject instanceof PdfDictionary acroformDict) {
            handleAcroForm(acroformDict);
        }

        addSharedObjectsToBody();
        setOutlines();
        setJavaScript();
        addFileAttachments();
        updateCatalog(catalog);
    }

    private void handleAcroForm(PdfDictionary acroForm) throws IOException {
        if (acroFields != null && acroFields.getXfa().isChanged()) {
            markUsed(acroForm);
            if (!flat) {
                acroFields.getXfa().setXfa(this);
            }
        }
        if (sigFlags != 0) {
            acroForm.put(PdfName.SIGFLAGS, new PdfNumber(sigFlags));
            markUsed(acroForm);
            markUsed(reader.getCatalog());
        }
    }

    private void updateCatalog(PdfDictionary catalog) {
        if (openAction != null) {
            catalog.put(PdfName.OPENACTION, openAction);
        }
        if (pdf.pageLabels != null) {
            catalog.put(PdfName.PAGELABELS, pdf.pageLabels.getDictionary(this));
        }
        updateOCProperties(catalog);
    }

    private void updateOCProperties(PdfDictionary catalog) {
        if (!documentOCG.isEmpty()) {
            fillOCProperties(false);
            PdfDictionary ocdict = catalog.getAsDict(PdfName.OCPROPERTIES);
            if (ocdict == null) {
                ocdict = ocProperties;
                catalog.put(PdfName.OCPROPERTIES, ocdict);
            } else {
                updateOCDict(ocdict);
            }
        }
    }

    private void updateOCDict(PdfDictionary ocdict) {
        ocdict.put(PdfName.OCGS, ocProperties.get(PdfName.OCGS));
        PdfDictionary ddict = ocdict.getAsDict(PdfName.D);
        if (ddict == null) {
            ddict = new PdfDictionary();
            ocdict.put(PdfName.D, ddict);
        }
        ddict.put(PdfName.ORDER, ocProperties.getAsDict(PdfName.D).get(PdfName.ORDER));
        ddict.put(PdfName.RBGROUPS, ocProperties.getAsDict(PdfName.D).get(PdfName.RBGROUPS));
        ddict.put(PdfName.OFF, ocProperties.getAsDict(PdfName.D).get(PdfName.OFF));
        ddict.put(PdfName.AS, ocProperties.getAsDict(PdfName.D).get(PdfName.AS));
    }

    private void handleMetadata(Map<String, String> moreInfo) throws IOException {
        String producer = getProducer(moreInfo);
        PdfStream xmp = prepareXMP(producer);

        if (xmp != null) {
            if (append && xmpMetadata != null) {
                addXMPToBody(xmp);
            } else {
                addXMPToCatalog(xmp);
            }
        }
    }

    private PdfDictionary getOldInfo() {
        PRIndirectReference iInfo = (PRIndirectReference) reader.getTrailer().get(PdfName.INFO);
        return (PdfDictionary) PdfReader.getPdfObject(iInfo);
    }

    private String getProducer(Map<String, String> moreInfo) {
        String producer = null;
        PRIndirectReference iInfo = (PRIndirectReference) reader.getTrailer().get(PdfName.INFO);
        PdfDictionary oldInfo = getOldInfo();
        if (iInfo != null && oldInfo != null && oldInfo.get(PdfName.PRODUCER) != null) {
            producer = oldInfo.getAsString(PdfName.PRODUCER).toUnicodeString();
        }
        if (producer == null) {
            producer = Document.getVersion();
        } else if (!producer.contains(Document.getProduct())) {
            producer = producer + "; modified using " + Document.getVersion();
        }
        if (moreInfo != null && moreInfo.containsKey(PRODUCER)) {
            producer = moreInfo.get(PRODUCER);
        }
        return producer;
    }

    private PdfStream prepareXMP(String producer) throws IOException {
        PdfObject xmpo = PdfReader.getPdfObject(reader.getCatalog().get(PdfName.METADATA));
        byte[] altMetadata = (xmpo != null && xmpo.isStream()) ? PdfReader.getStreamBytesRaw((PRStream) xmpo) : null;
        if (xmpMetadata != null) {
            altMetadata = xmpMetadata;
        }
        PdfDate date = modificationDate == null ? new PdfDate() : new PdfDate(modificationDate);

        if (altMetadata != null && (!append || updateMetadata)) {
            return createXMPStream(altMetadata, producer, date);
        }
        return null;
    }

    private PdfStream createXMPStream(byte[] altMetadata, String producer, PdfDate date) throws IOException {
        PdfStream xmp;
        try {
            XmpReader xmpr = new XmpReader(altMetadata);
            if (!xmpr.replace("http://ns.adobe.com/pdf/1.3/", PRODUCER, producer)) {
                xmpr.add("rdf:Description", "http://ns.adobe.com/pdf/1.3/", "pdf:Producer", producer);
            }
            if (!xmpr.replace(NS_ADOBE_COM_XAP_1_0, "ModifyDate", date.getW3CDate())) {
                xmpr.add("rdf:Description", NS_ADOBE_COM_XAP_1_0, "xmp:ModifyDate", date.getW3CDate());
            }
            xmpr.replace(NS_ADOBE_COM_XAP_1_0, "MetadataDate", date.getW3CDate());
            xmp = new PdfStream(xmpr.serializeDoc());
        } catch (SAXException e) {
            xmp = new PdfStream(altMetadata);
        }
        xmp.put(PdfName.TYPE_CONST, PdfName.METADATA);
        xmp.put(PdfName.SUBTYPE, PdfName.XML);
        if (crypto != null && !crypto.isMetadataEncrypted()) {
            PdfArray ar = new PdfArray();
            ar.add(PdfName.CRYPT);
            xmp.put(PdfName.FILTER, ar);
        }
        return xmp;
    }

    private void addXMPToBody(PdfStream xmp) throws IOException {
        PdfObject xmpo = PdfReader.getPdfObject(reader.getCatalog().get(PdfName.METADATA));
        body.add(xmp, xmpo.getIndRef());
    }

    private void addXMPToCatalog(PdfStream xmp) throws IOException {
        reader.getCatalog().put(PdfName.METADATA, body.add(xmp).getIndirectReference());
        markUsed(reader.getCatalog());
    }

    private void writeDocument() throws IOException, NoSuchAlgorithmException {
        try {
            file.reOpen();
            alterContents();
            writeObjects();
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e);
        } finally {
            closeFile();
        }
    }

    private void writeObjects() throws IOException, NoSuchAlgorithmException {
        int rootN = ((PRIndirectReference) reader.trailer.get(PdfName.ROOT)).getNumber();
        if (append) {
            writeAppendObjects(rootN);
        } else {
            writeNonAppendObjects(rootN);
        }
        writeCrossReferenceTable();
    }

    private void writeAppendObjects(int rootN) throws IOException {
        for (int j : marked.getKeys()) {
            PdfObject obj = reader.getPdfObjectRelease(j);
            if (obj != null && j < initialXrefSize) {
                addToBody(obj, j, j != rootN);
            }
        }
        for (int k = initialXrefSize; k < reader.getXrefSize(); ++k) {
            PdfObject obj = reader.getPdfObject(k);
            if (obj != null) {
                addToBody(obj, getNewObjectNumber(reader, k));
            }
        }
    }

    private void writeNonAppendObjects(int rootN) throws IOException {
        for (int k = 1; k < reader.getXrefSize(); ++k) {
            PdfObject obj = reader.getPdfObjectRelease(k);
            if (obj != null) {
                addToBody(obj, getNewObjectNumber(reader, k), k != rootN);
            }
        }
    }

    private void writeCrossReferenceTable() throws IOException, NoSuchAlgorithmException {
        PdfIndirectReference root = new PdfIndirectReference(0, getNewObjectNumber(reader, ((PRIndirectReference) reader.trailer.get(PdfName.ROOT)).getNumber()),
                0);
        PdfDictionary info = getInfoDictionary(getOldInfo(), new PdfDate(modificationDate), getProducer(null), null);
        PdfIndirectReference infoRef = addToBody(info, false).getIndirectReference();
        body.writeCrossReferenceTable(os, root, infoRef, getEncryptionRef(), getFileIDPSI(), prevxref);
        os.write(getISOBytes("startxref\n"));
        os.write(getISOBytes(String.valueOf(body.offset())));
        os.write(getISOBytes("\n%%EOF\n"));
        os.flush();
        if (isCloseStream()) {
            os.close();
        }
        reader.close();
    }

    private void closeFile() {
        try {
            file.close();
        } catch (Exception e) {
            // empty on purpose
        }
    }

    private PdfIndirectReference getEncryptionRef() throws IOException, NoSuchAlgorithmException {
        if (crypto != null) {
            return append ? reader.getCryptoRef() : addToBody(crypto.getEncryptionDictionary(), false).getIndirectReference();
        }
        return null;
    }

    private PdfObject getFileIDPSI() throws NoSuchAlgorithmException {
        if (crypto != null && includeFileID) {
            byte[] fileIDPartTwo = overrideFileId != null ? PdfEncryption.getFileIdChangingPart(overrideFileId) : PdfEncryption.createDocumentId();
            return PdfEncryption.createInfoId(crypto.documentID, fileIDPartTwo);
        }
        if (includeFileID) {
            byte[] documentId = reader.getDocumentId();
            if (overrideFileId != null) {
                return documentId != null ? PdfEncryption.createInfoId(documentId, PdfEncryption.getFileIdChangingPart(overrideFileId)) : overrideFileId;
            }
            return documentId != null ? PdfEncryption.createInfoId(documentId) : PdfEncryption.createInfoId(PdfEncryption.createDocumentId());
        }
        return null;
    }


    PdfDictionary getInfoDictionary(PdfDictionary oldInfo, PdfDate modificationDate, String producer,
            Map<String, String> moreInfo) {
        PdfDictionary newInfo = new PdfDictionary();
        if (oldInfo != null) {
            for (PdfName key : oldInfo.getKeys()) {
                PdfObject value = PdfReader.getPdfObject(oldInfo.get(key));
                newInfo.put(key, value);
            }
        }

        newInfo.put(PdfName.MODDATE, modificationDate);
        if (producer != null) {
            newInfo.put(PdfName.PRODUCER, new PdfString(producer));
        }

        if (moreInfo != null) {
            for (Map.Entry<String, String> entry : moreInfo.entrySet()) {
                String key = entry.getKey();
                PdfName keyName = new PdfName(key);
                String value = entry.getValue();
                if (value == null) {
                    newInfo.remove(keyName);
                } else {
                    newInfo.put(keyName, new PdfString(value, PdfObject.TEXT_UNICODE));
                }
            }
        }
        return newInfo;
    }

    void applyRotation(PdfDictionary pageN, ByteBuffer out) {
        if (!rotateContents) {
            return;
        }
        Rectangle page = reader.getPageSizeWithRotation(pageN);
        int rotation = page.getRotationPdfPCell();
        switch (rotation) {
            case 90:
                out.append(PdfContents.ROTATE90);
                out.append(page.getTop());
                out.append(' ').append('0').append(PdfContents.ROTATEFINAL);
                break;
            case 180:
                out.append(PdfContents.ROTATE180);
                out.append(page.getRight());
                out.append(' ');
                out.append(page.getTop());
                out.append(PdfContents.ROTATEFINAL);
                break;
            case 270:
                out.append(PdfContents.ROTATE270);
                out.append('0').append(' ');
                out.append(page.getRight());
                out.append(PdfContents.ROTATEFINAL);
                break;
            default:
                break;
        }
    }

    void alterContents() throws IOException {
        for (PageStamp ps : pagesToContent.values()) {
            PdfDictionary pageN = ps.pageN;
            markUsed(pageN);

            PdfArray ar = processPageContent(pageN);

            ByteBuffer out = new ByteBuffer();
            try {
                handleUnderContent(ps, pageN, out);
                handleOverContent(ps, pageN, ar, out);
            } catch (Exception e) {
                logger.info("ERROR ByteBuffer >> " + e.getMessage());
            }

            alterResources(ps);
        }
    }

    private PdfArray processPageContent(PdfDictionary pageN) {
        PdfArray ar;
        PdfObject content = PdfReader.getPdfObject(pageN.get(PdfName.CONTENTS), pageN);
        if (content != null) {
            if (content.isArray()) {
                ar = (PdfArray) content;
                markUsed(ar);
            } else if (content.isStream()) {
                ar = new PdfArray();
                ar.add(pageN.get(PdfName.CONTENTS));
                pageN.put(PdfName.CONTENTS, ar);
            } else {
                ar = new PdfArray();
                pageN.put(PdfName.CONTENTS, ar);
            }
        } else {
            ar = new PdfArray();
            pageN.put(PdfName.CONTENTS, ar);
        }
        return ar;
    }

    private void handleUnderContent(PageStamp ps, PdfDictionary pageN, ByteBuffer out) throws IOException {
        if (ps.under != null) {
            out.append(PdfContents.SAVESTATE);
            applyRotation(pageN, out);
            out.append(ps.under.getInternalBuffer());
            out.append(PdfContents.RESTORESTATE);

            PdfStream stream = new PdfStream(out.toByteArray());
            stream.flateCompress(compressionLevel);
            addToBody(stream).getIndirectReference();
            out.reset();
        }
    }

    private void handleOverContent(PageStamp ps, PdfDictionary pageN, PdfArray ar, ByteBuffer out) throws IOException {
        if (ps.over != null) {
            out.append(PdfContents.SAVESTATE);

            PdfStream stream = new PdfStream(out.toByteArray());
            stream.flateCompress(compressionLevel);
            ar.addFirst(addToBody(stream).getIndirectReference());
            out.reset();

            out.append(' ');
            out.append(PdfContents.RESTORESTATE);
            ByteBuffer buf = ps.over.getInternalBuffer();
            out.append(buf.getBuffer(), 0, ps.replacePoint);
            out.append(PdfContents.SAVESTATE);
            applyRotation(pageN, out);
            out.append(buf.getBuffer(), ps.replacePoint, buf.size() - ps.replacePoint);
            out.append(PdfContents.RESTORESTATE);

            stream = new PdfStream(out.toByteArray());
            stream.flateCompress(compressionLevel);
            ar.add(addToBody(stream).getIndirectReference());
        }
    }


    void alterResources(PageStamp ps) {
        ps.pageN.put(PdfName.RESOURCES, ps.pageResources.getResources());
    }

    @Override
    protected int getNewObjectNumber(PdfReader reader, int number) {
        IntHashtable ref = readers2intrefs.get(reader);
        if (ref != null) {
            int n = ref.get(number);
            if (n == 0) {
                n = getIndirectReferenceNumber();
                ref.put(number, n);
            }
            return n;
        }
        if (currentPdfReaderInstance == null) {
            if (append && number < initialXrefSize) {
                return number;
            }
            int n = myXref.get(number);
            if (n == 0) {
                n = getIndirectReferenceNumber();
                myXref.put(number, n);
            }
            return n;
        } else {
            return currentPdfReaderInstance.getNewObjectNumber(number);
        }
    }

    RandomAccessFileOrArray getReaderFile(PdfReader reader) {
        if (readers2intrefs.containsKey(reader)) {
            RandomAccessFileOrArray raf = readers2file.get(reader);
            if (raf != null) {
                return raf;
            }
            return reader.getSafeFile();
        }
        if (currentPdfReaderInstance == null) {
            return file;
        } else {
            return currentPdfReaderInstance.getReaderFile();
        }
    }

    /**
     * Removes the encryption from the document (and also inherently the permissions)
     *
     * @throws DocumentException
     */
    public void removeEncryption() throws DocumentException, NoSuchAlgorithmException {
        super.setEncryption(null, null, 0, ENCRYPTION_NONE);
        this.reader.setPermissions(0);
    }

    public void registerReader(PdfReader reader, boolean openFile) throws IOException {
        if (readers2intrefs.containsKey(reader)) {
            return;
        }
        readers2intrefs.put(reader, new IntHashtable());
        if (openFile) {
            RandomAccessFileOrArray raf = reader.getSafeFile();
            readers2file.put(reader, raf);
            raf.reOpen();
        }
    }

    public void unRegisterReader(PdfReader reader) {
        if (!readers2intrefs.containsKey(reader)) {
            return;
        }
        readers2intrefs.remove(reader);
        RandomAccessFileOrArray raf = readers2file.get(reader);
        if (raf == null) {
            return;
        }
        readers2file.remove(reader);
        try {
            raf.close();
        } catch (Exception ignored) {
//da vedere come effettuare il log
        }
    }

    public void addComments(FdfReader fdf) throws IOException {
        if (readers2intrefs.containsKey(fdf)) {
            return;
        }

        PdfDictionary catalog = getFdfCatalog(fdf);
        if (catalog == null) {
            return;
        }

        PdfArray annots = catalog.getAsArray(PdfName.ANNOTS);
        if (annots == null || annots.isEmpty()) {
            return;
        }

        registerReader(fdf, false);
        IntHashtable hits = new IntHashtable();
        Map<String, PdfObject> irt = new HashMap<>();
        List<PdfObject> annotObjects = processAnnotations(fdf, annots, hits, irt);

        updateIndirectReferences(fdf, hits, irt);
        addAnnotationsToPages(annotObjects);
    }

    private PdfDictionary getFdfCatalog(FdfReader fdf) {
        PdfDictionary catalog = fdf.getCatalog();
        return catalog != null ? catalog.getAsDict(PdfName.FDF) : null;
    }

    private List<PdfObject> processAnnotations(FdfReader fdf, PdfArray annots, IntHashtable hits, Map<String, PdfObject> irt) {
        List<PdfObject> annotObjects = new ArrayList<>();
        for (int i = 0; i < annots.size(); ++i) {
            PdfObject obj = annots.getPdfObject(i);
            PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject(obj);
            PdfNumber page = annot.getAsNumber(PdfName.PAGE);
            if (page != null && page.intValue() < reader.getNumberOfPages()) {
                findAllObjects(fdf, obj, hits);
                annotObjects.add(obj);
                addIndirectReferenceIfNecessary(annot, obj, irt);
            }
        }
        return annotObjects;
    }

    private void addIndirectReferenceIfNecessary(PdfDictionary annot, PdfObject obj, Map<String, PdfObject> irt) {
        if (obj.type() == PdfObject.INDIRECT) {
            PdfObject nm = PdfReader.getPdfObject(annot.get(PdfName.NM));
            if (nm != null && nm.type() == PdfObject.STRING) {
                irt.put(nm.toString(), obj);
            }
        }
    }

    private void updateIndirectReferences(FdfReader fdf, IntHashtable hits, Map<String, PdfObject> irt) throws IOException {
        int[] arhits = hits.getKeys();
        for (int n : arhits) {
            PdfObject obj = fdf.getPdfObject(n);
            if (obj.type() == PdfObject.DICTIONARY) {
                PdfObject str = PdfReader.getPdfObject(((PdfDictionary) obj).get(PdfName.IRT));
                if (str != null && str.type() == PdfObject.STRING) {
                    PdfObject i = irt.get(str.toString());
                    if (i != null) {
                        PdfDictionary updatedDict = new PdfDictionary();
                        updatedDict.merge((PdfDictionary) obj);
                        updatedDict.put(PdfName.IRT, i);
                        obj = updatedDict;
                    }
                }
            }
            addToBody(obj, getNewObjectNumber(fdf, n));
        }
    }

    private void addAnnotationsToPages(List<PdfObject> annotObjects) {
        for (PdfObject obj : annotObjects) {
            PdfDictionary annot = (PdfDictionary) PdfReader.getPdfObject(obj);
            PdfNumber page = annot.getAsNumber(PdfName.PAGE);
            if (page != null) {
                PdfDictionary pageDict = reader.getPageN(page.intValue() + 1);
                PdfArray annotArray = (PdfArray) PdfReader.getPdfObject(pageDict.get(PdfName.ANNOTS), pageDict);
                if (annotArray == null) {
                    annotArray = new PdfArray();
                    pageDict.put(PdfName.ANNOTS, annotArray);
                    markUsed(pageDict);
                }
                markUsed(annotArray);
                annotArray.add(obj);
            }
        }
    }


    PageStamp getPageStamp(int pageNum) {
        PdfDictionary pageN = reader.getPageN(pageNum);
        return pagesToContent.computeIfAbsent(pageN, key -> new PageStamp(this, key));
    }


    PdfContentByte getUnderContent(int pageNum) {
        if (pageNum < 1 || pageNum > reader.getNumberOfPages()) {
            return null;
        }
        PageStamp ps = getPageStamp(pageNum);
        if (ps.under == null) {
            ps.under = new StampContent(this, ps);
        }
        return ps.under;
    }

    PdfContentByte getOverContent(int pageNum) {
        if (pageNum < 1 || pageNum > reader.getNumberOfPages()) {
            return null;
        }
        PageStamp ps = getPageStamp(pageNum);
        if (ps.over == null) {
            ps.over = new StampContent(this, ps);
        }
        return ps.over;
    }

    void correctAcroFieldPages(int page) {
        if (acroFields == null) {
            return;
        }
        if (page > reader.getNumberOfPages()) {
            return;
        }
        Map<String, Item> fields = acroFields.getAllFields();
        for (Item item : fields.values()) {
            for (int k = 0; k < item.size(); ++k) {
                int p = item.getPage(k);
                if (p >= page) {
                    item.forcePage(k, p + 1);
                }
            }
        }
    }

    void replacePage(PdfReader r, int pageImported, int pageReplaced) {
        PdfDictionary pageN = reader.getPageN(pageReplaced);
        if (pagesToContent.containsKey(pageN)) {
            throw new IllegalStateException(MessageLocalization.getComposedMessage(
                    "this.page.cannot.be.replaced.new.content.was.already.added"));
        }
        PdfImportedPage p = getImportedPage(r, pageImported);
        PdfDictionary dic2 = reader.getPageNRelease(pageReplaced);
        dic2.remove(PdfName.RESOURCES);
        dic2.remove(PdfName.CONTENTS);
        moveRectangle(dic2, r, pageImported, PdfName.MEDIABOX, "media");
        moveRectangle(dic2, r, pageImported, PdfName.CROPBOX, "crop");
        moveRectangle(dic2, r, pageImported, PdfName.TRIMBOX, "trim");
        moveRectangle(dic2, r, pageImported, PdfName.ARTBOX, "art");
        moveRectangle(dic2, r, pageImported, PdfName.BLEEDBOX, "bleed");
        dic2.put(PdfName.ROTATE, new PdfNumber(r.getPageRotation(pageImported)));
        PdfContentByte cb = getOverContent(pageReplaced);
        cb.addTemplate(p, 0, 0);
        PageStamp ps = pagesToContent.get(pageN);
        ps.replacePoint = ps.over.getInternalBuffer().size();
    }

    void insertPage(int pageNumber, Rectangle mediabox) {
        Rectangle media = new Rectangle(mediabox);
        int rotation = media.getRotationPdfPCell() % 360;
        PdfDictionary page = new PdfDictionary(PdfName.PAGE);
        PdfDictionary resources = new PdfDictionary();
        PdfArray procset = new PdfArray();
        procset.add(PdfName.PDF);
        procset.add(PdfName.TEXT);
        procset.add(PdfName.IMAGEB);
        procset.add(PdfName.IMAGEC);
        procset.add(PdfName.IMAGEI);
        resources.put(PdfName.PROCSET, procset);
        page.put(PdfName.RESOURCES, resources);
        page.put(PdfName.ROTATE, new PdfNumber(rotation));
        page.put(PdfName.MEDIABOX, new PdfRectangle(media, rotation));
        PRIndirectReference pref = reader.addPdfObject(page);
        PdfDictionary parent;
        PRIndirectReference parentRef;
        if (pageNumber > reader.getNumberOfPages()) {
            PdfDictionary lastPage = reader.getPageNRelease(reader.getNumberOfPages());
            parentRef = (PRIndirectReference) lastPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary) PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray) PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            kids.add(pref);
            markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
        } else {
            if (pageNumber < 1) {
                pageNumber = 1;
            }
            PdfDictionary firstPage = reader.getPageN(pageNumber);
            PRIndirectReference firstPageRef = reader.getPageOrigRef(pageNumber);
            reader.releasePage(pageNumber);
            parentRef = (PRIndirectReference) firstPage.get(PdfName.PARENT);
            parentRef = new PRIndirectReference(reader, parentRef.getNumber());
            parent = (PdfDictionary) PdfReader.getPdfObject(parentRef);
            PdfArray kids = (PdfArray) PdfReader.getPdfObject(parent.get(PdfName.KIDS), parent);
            int len = kids.size();
            int num = firstPageRef.getNumber();
            for (int k = 0; k < len; ++k) {
                PRIndirectReference cur = (PRIndirectReference) kids.getPdfObject(k);
                if (num == cur.getNumber()) {
                    kids.add(k, pref);
                    break;
                }
            }
            if (len == kids.size()) {
                throw new InternalException(MessageLocalization.getComposedMessage("internal.inconsistence"));
            }
            markUsed(kids);
            reader.pageRefs.insertPage(pageNumber, pref);
            correctAcroFieldPages(pageNumber);
        }
        page.put(PdfName.PARENT, parentRef);
        while (parent != null) {
            markUsed(parent);
            PdfNumber count = (PdfNumber) PdfReader.getPdfObjectRelease(parent.get(PdfName.COUNT));
            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }
    }

    /**
     * Getter for property rotateContents.
     *
     * @return Value of property rotateContents.
     */
    boolean isRotateContents() {
        return this.rotateContents;
    }

    /**
     * Setter for property rotateContents.
     *
     * @param rotateContents New value of property rotateContents.
     */
    void setRotateContents(boolean rotateContents) {
        this.rotateContents = rotateContents;
    }

    boolean isContentWritten() {
        return body.size() > 1;
    }

    AcroFields getAcroFields() {
        if (acroFields == null) {
            acroFields = new AcroFields(reader, this);
        }
        return acroFields;
    }

    void setFormFlattening(boolean flat) {
        this.flat = flat;
    }

    void setFreeTextFlattening(boolean flat) {
        this.flatFreeText = flat;
    }

    boolean partialFormFlattening(String name) {
        getAcroFields();
        if (acroFields.getXfa().isXfaPresent()) {
            throw new UnsupportedOperationException(
                    MessageLocalization.getComposedMessage("partial.form.flattening.is.not.supported.with.xfa.forms"));
        }
        if (!acroFields.getAllFields().containsKey(name)) {
            return false;
        }
        partialFlattening.add(name);
        return true;
    }

    void flatFields() {
        checkAppendMode();

        getAcroFields();
        Map<String, Item> fields = acroFields.getAllFields();

        handlePartialFlattening(fields);

        PdfDictionary acroForm = reader.getCatalog().getAsDict(PdfName.ACROFORM);
        PdfArray acroFds = null;
        PdfBoolean needAppearance = null;
        if (acroForm != null) {
            acroFds = (PdfArray) PdfReader.getPdfObject(acroForm.get(PdfName.FIELDS), acroForm);
            needAppearance = (PdfBoolean) acroForm.get(PdfName.NEEDAPPEARANCES);
        }

        for (Map.Entry<String, Item> entry : fields.entrySet()) {
            processField(entry, acroFds, needAppearance);
        }

        if (!fieldsAdded && partialFlattening.isEmpty()) {
            removeUnusedAnnotations();
            eliminateAcroformObjects();
        }
    }

    private void checkAppendMode() {
        if (append) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("field.flattening.is.not.supported.in.append.mode"));
        }
    }

    private void handlePartialFlattening(Map<String, Item> fields) {
        if (fieldsAdded && partialFlattening.isEmpty()) {
            partialFlattening.addAll(fields.keySet());
        }
    }

    private void processField(Map.Entry<String, Item> entry, PdfArray acroFds, PdfBoolean needAppearance) {
        String name = entry.getKey();
        if (!partialFlattening.isEmpty() && !partialFlattening.contains(name)) {
            return;
        }

        Item item = entry.getValue();
        for (int k = 0; k < item.size(); ++k) {
            PdfDictionary merged = item.getMerged(k);
            if (merged == null) continue; // Safeguard against null

            PdfNumber ff = merged.getAsNumber(PdfName.F);
            int flags = (ff != null) ? ff.intValue() : 0;
            int page = item.getPage(k);
            if (page == -1) continue;

            processAppearance(merged, name, needAppearance, flags, page);
            if (!partialFlattening.isEmpty()) {
                updateAnnotations(entry, acroFds, page, k);
            }
        }
    }

    private void processAppearance(PdfDictionary merged, String name, PdfBoolean needAppearance, int flags, int page) {
        PdfDictionary appDic = merged.getAsDict(PdfName.AP);
        PdfStream appStream = (appDic != null) ? appDic.getAsStream(PdfName.N) : null;

        if (needAppearance != null && needAppearance.booleanValue()) {
            regenerateFieldIfNeeded(name, appDic);
        }

        boolean transformNeeded = checkTransformationNeeded(merged, appStream);

        if (appDic != null && (flags & PdfAnnotation.FLAGS_PRINT) != 0 && (flags & PdfAnnotation.FLAGS_HIDDEN) == 0) {
            processNormalAppearance(appDic, merged, page, transformNeeded);
        }
    }

    private void regenerateFieldIfNeeded(String name, PdfDictionary appDic) {
        boolean regenerate = false;
        int type = this.acroFields.getFieldType(name);

        if (type != AcroFields.FIELD_TYPE_SIGNATURE && (appDic == null || !(appDic.getDirectObject(PdfName.N) instanceof PdfIndirectReference))) {
                regenerate = true;
            }

        if (regenerate) {
            try {
                this.acroFields.regenerateField(name);
            } catch (Exception e) {
                // ignore any exception
            }
        }
    }

    private boolean checkTransformationNeeded(PdfDictionary merged, PdfStream appStream) {
        if (this.acroFields.isGenerateAppearances() && appStream != null) {
            PdfArray bboxRaw = appStream.getAsArray(PdfName.BBOX);
            PdfArray rectRaw = merged.getAsArray(PdfName.RECT);
            if (bboxRaw != null && rectRaw != null) {
                applyTransformation(appStream, bboxRaw, rectRaw, merged);
                return true;
            }
        }
        return false;
    }

    private void applyTransformation(PdfStream appStream, PdfArray bboxRaw, PdfArray rectRaw, PdfDictionary merged) {
        PdfRectangle bbox = new PdfRectangle(bboxRaw);
        PdfRectangle rect = new PdfRectangle(rectRaw);

        float rectWidth = rect.width();
        float rectHeight = rect.height();
        double rotation = getRotation(merged);

        if (Math.abs(rotation) > 0 && rotation % 180 != 0 && rotation % 90 == 0) {
            rectWidth = rect.height();
            rectHeight = rect.width();
        }

        float scaleFactorWidth = Math.abs(bbox.width() != 0 ? rectWidth / bbox.width() : 1.0f);
        float scaleFactorHeight = Math.abs(bbox.height() != 0 ? rectHeight / bbox.height() : 1.0f);

        PdfArray array = new PdfArray(new float[]{scaleFactorWidth, 0, 0, scaleFactorHeight, 0, 0});
        appStream.put(PdfName.MATRIX, array);
        markUsed(appStream);
    }

    private double getRotation(PdfDictionary merged) {
        PdfDictionary mkDic = merged.getAsDict(PdfName.MK);
        if (mkDic != null && mkDic.get(PdfName.R) != null) {
            return mkDic.getAsNumber(PdfName.R).floatValue();
        }
        return 0;
    }

    private void processNormalAppearance(PdfDictionary appDic, PdfDictionary merged, int page, boolean transformNeeded) {
        PdfObject normalAppearanceObj = appDic.get(PdfName.N);
        PdfAppearance app = createAppearance(normalAppearanceObj, merged);

        if (app != null) {
            Rectangle box = PdfReader.getNormalizedRectangle(merged.getAsArray(PdfName.RECT));
            PdfContentByte cb = getOverContent(page);
            cb.setLiteral("Q ");

            if (transformNeeded) {
                applyTransformations(app, box, cb, merged);
            } else {
                addTemplateWithoutTransformation(app, box, cb, normalAppearanceObj);
            }

            cb.setLiteral("q ");
        }
    }

    private PdfAppearance createAppearance(PdfObject normalAppearanceObj, PdfDictionary merged) {
        PdfObject objReal = PdfReader.getPdfObject(normalAppearanceObj);
        if (normalAppearanceObj instanceof PdfIndirectReference pdfIndirectReference && !pdfIndirectReference.isIndirect()) {
            return new PdfAppearance(pdfIndirectReference);
        } else if (objReal instanceof PdfStream pdfStream) {
            pdfStream.put(PdfName.SUBTYPE, PdfName.FORM);
            return new PdfAppearance((PdfIndirectReference) normalAppearanceObj);
        } else if (objReal != null && objReal.isDictionary()) {
            PdfName as = merged.getAsName(PdfName.AS);
            if (as != null) {
                PdfIndirectReference iref = (PdfIndirectReference) ((PdfDictionary) objReal).get(as);
                if (iref != null) {
                    PdfAppearance app = new PdfAppearance(iref);
                    if (iref.isIndirect()) {
                        objReal = PdfReader.getPdfObject(iref);
                        ((PdfDictionary) objReal).put(PdfName.SUBTYPE, PdfName.FORM);
                    }
                    return app;
                }
            }
        }
        return null;
    }

    private void applyTransformations(PdfAppearance app, Rectangle box, PdfContentByte cb, PdfDictionary merged) {
        AffineTransform transform = new AffineTransform();
        double x = box.getLeft();
        double y = box.getBottom();

        double rotation = getRotation(merged);
        rotation = rotation % 360;
        if (rotation == 90 || rotation == 180) {
            x += box.getWidth();
        }
        if (rotation == 180 || rotation == 270) {
            y += box.getHeight();
        }

        transform.translate(x, y);
        transform.rotate(Math.toRadians(rotation));

        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        cb.addTemplate(app, matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
    }

    private void addTemplateWithoutTransformation(PdfAppearance app, Rectangle box, PdfContentByte cb, PdfObject normalAppearanceObj) {
        if (!(normalAppearanceObj instanceof PdfIndirectReference)) {
            PdfArray bBoxCoordinates = ((PdfDictionary) PdfReader.getPdfObject(normalAppearanceObj)).getAsArray(PdfName.BBOX);
            if (bBoxCoordinates != null && bBoxCoordinates.size() >= 4) {
                Rectangle bBox = PdfReader.getNormalizedRectangle(bBoxCoordinates);
                cb.addTemplate(app, (box.getWidth() / bBox.getWidth()), 0, 0,
                        (box.getHeight() / bBox.getHeight()), box.getLeft(), box.getBottom());
            } else {
                throw new DocumentException("The required BBox attribute of the field is missing. The PDF is not PDF spec compliant!");
            }
        } else {
            cb.addTemplate(app, box.getLeft(), box.getBottom());
        }
    }

    private void updateAnnotations(Map.Entry<String, Item> entry, PdfArray acroFds, int page, int k) {
        PdfDictionary pageDic = reader.getPageN(page);
        PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
        if (annots == null) return;

        List<PdfObject> annotsList = new ArrayList<>();
        for (int idx = 0; idx < annots.size(); ++idx) {
            PdfObject ran = annots.getPdfObject(idx);
            if (!ran.isIndirect()) {
                annotsList.add(ran);
            } else {
                processAnnotation(entry, acroFds, annotsList, ran, k);
            }
        }
        annots.removeAll(annotsList);
        if (annots.isEmpty()) {
            PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS));
            pageDic.remove(PdfName.ANNOTS);
        }
    }

    private void processAnnotation(Map.Entry<String, Item> entry, PdfArray acroFds, List<PdfObject> annotsList, PdfObject ran, int k) {
        PdfObject ran2 = entry.getValue().getWidgetRef(k);

        if (areReferencesEqual(ran, ran2)) {
            annotsList.add(ran);
            handleWidgetReferences(ran2, acroFds);
        }
    }

    private boolean areReferencesEqual(PdfObject ran, PdfObject ran2) {
        return ran.isIndirect() && ran2.isIndirect() &&
                ((PRIndirectReference) ran).getNumber() == ((PRIndirectReference) ran2).getNumber();
    }

    private void handleWidgetReferences(PdfObject ran2, PdfArray acroFds) {
        PRIndirectReference wdref = (PRIndirectReference) ran2;

        while (true) {
            PdfDictionary wd = (PdfDictionary) PdfReader.getPdfObject(wdref);
            PRIndirectReference parentRef = (PRIndirectReference) wd.get(PdfName.PARENT);
            PdfReader.killIndirect(wdref);

            if (parentRef == null) { // reached AcroForm
                removeFromArray(acroFds, wdref);
                break;
            }

            PdfDictionary parent = (PdfDictionary) PdfReader.getPdfObject(parentRef);
            PdfArray kids = parent.getAsArray(PdfName.KIDS);
            removeFromArray(kids, wdref);

            if (!kids.isEmpty()) {
                break;
            }

            wdref = parentRef;
        }
    }

    private void removeFromArray(PdfArray kids, PRIndirectReference ref) {
        List<PdfObject> toRemove = new ArrayList<>();
        for (int i = 0; i < kids.size(); ++i) {
            PdfObject obj = kids.getPdfObject(i);
            if (obj.isIndirect() && ((PRIndirectReference) obj).getNumber() == ref.getNumber()) {
                toRemove.add(obj);
            }
        }
        kids.removeAll(toRemove);
    }


    private void removeUnusedAnnotations() {
        for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
            PdfDictionary pageDic = reader.getPageN(page);
            PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
            if (annots == null) continue;

            List<PdfObject> annotsList = new ArrayList<>();
            for (int idx = 0; idx < annots.size(); ++idx) {
                PdfObject annoto = annots.getDirectObject(idx);
                if ((annoto instanceof PdfIndirectReference) && !annoto.isIndirect()) {
                    continue;
                }
                if (!annoto.isDictionary() || PdfName.WIDGET.equals(((PdfDictionary) annoto).get(PdfName.SUBTYPE))) {
                    annotsList.add(annoto);
                }
            }
            annots.removeAll(annotsList);
            if (annots.isEmpty()) {
                PdfReader.killIndirect(pageDic.get(PdfName.ANNOTS));
                pageDic.remove(PdfName.ANNOTS);
            }
        }
    }



    void eliminateAcroformObjects() {
        PdfObject acro = reader.getCatalog().get(PdfName.ACROFORM);
        if (acro == null) {
            return;
        }
        PdfDictionary acrodic = (PdfDictionary) PdfReader.getPdfObject(acro);
        reader.killXref(acrodic.get(PdfName.XFA));
        acrodic.remove(PdfName.XFA);
        PdfObject iFields = acrodic.get(PdfName.FIELDS);
        if (iFields != null) {
            PdfDictionary kids = new PdfDictionary();
            kids.put(PdfName.KIDS, iFields);
            sweepKids(kids);
            PdfReader.killIndirect(iFields);
            acrodic.put(PdfName.FIELDS, new PdfArray());
        }
        acrodic.remove(PdfName.SIGFLAGS);
    }

    void sweepKids(PdfObject obj) {
        PdfObject oo = PdfReader.killIndirect(obj);
        if (oo == null || !oo.isDictionary()) {
            return;
        }
        PdfDictionary dic = (PdfDictionary) oo;
        PdfArray kids = (PdfArray) PdfReader.killIndirect(dic.get(PdfName.KIDS));
        if (kids == null) {
            return;
        }
        for (int k = 0; k < kids.size(); ++k) {
            sweepKids(kids.getPdfObject(k));
        }
    }

    private void flatFreeTextFields() {
        validateAppendMode();

        for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
            PdfDictionary pageDic = reader.getPageN(page);
            PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
            if (annots != null) {
                List<Integer> toRemove = processAnnotations(annots, page);
                removeAnnotations(annots, toRemove);
                cleanUpPageAnnotations(pageDic, annots);
            }
        }
    }

    private void validateAppendMode() {
        if (append) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("freetext.flattening.is.not.supported.in.append.mode"));
        }
    }

    private List<Integer> processAnnotations(PdfArray annots, int page) {
        List<Integer> toRemove = new ArrayList<>();
        for (int idx = 0; idx < annots.size(); ++idx) {
            PdfObject annoto = annots.getDirectObject(idx);
            if (isValidAnnotation(annoto) && isFreeTextAnnotation((PdfDictionary) annoto)) {
                handleFreeTextAnnotation((PdfDictionary) annoto, page, toRemove, idx);
            }
        }
        return toRemove;
    }

    private boolean isValidAnnotation(PdfObject annoto) {
        return annoto instanceof PdfDictionary annotoDict && annotoDict.get(PdfName.SUBTYPE) != null;
    }

    private boolean isFreeTextAnnotation(PdfDictionary annDic) {
        return PdfName.FREETEXT.equals(annDic.get(PdfName.SUBTYPE));
    }

    private void handleFreeTextAnnotation(PdfDictionary annDic, int page, List<Integer> toRemove, int idx) {
        PdfNumber ff = annDic.getAsNumber(PdfName.F);
        int flags = (ff != null) ? ff.intValue() : 0;

        if ((flags & PdfAnnotation.FLAGS_PRINT) != 0 && (flags & PdfAnnotation.FLAGS_HIDDEN) == 0) {
            PdfAppearance app = getAppearance(annDic);
            if (app != null) {
                drawAppearanceOnPage(app, annDic, page);
                toRemove.add(idx);
            }
        }
    }

    private PdfAppearance getAppearance(PdfDictionary annDic) {
        PdfObject obj1 = annDic.get(PdfName.AP);
        if (obj1 == null) {
            return null;
        }
        PdfDictionary appDic = (obj1 instanceof PdfIndirectReference) ?
                (PdfDictionary) PdfReader.getPdfObject(obj1) : (PdfDictionary) obj1;
        PdfObject obj = appDic.get(PdfName.N);
        return extractAppearance(obj, appDic);
    }

    private PdfAppearance extractAppearance(PdfObject obj, PdfDictionary appDic) {
        PdfObject objReal = PdfReader.getPdfObject(obj);

        if (obj instanceof PdfIndirectReference reference) {
            return handleIndirectReference(reference);
        }

        if (objReal instanceof PdfStream) {
            return handlePdfStream(objReal, obj);
        }

        if (objReal != null && objReal.isDictionary()) {
            return handleDictionary(objReal, appDic);
        }

        return null;
    }

    private PdfAppearance handleIndirectReference(PdfIndirectReference reference) {
        // Questo codice viene eseguito se il riferimento è indiretto.
        if (reference.isIndirect()) {
            return new PdfAppearance(reference);
        }
        return null; // Restituisce null se non è un riferimento indiretto.
    }

    private PdfAppearance handlePdfStream(PdfObject objReal, PdfObject obj) {
        // Questo codice gestisce il caso in cui il PdfObject è un PdfStream.
        PdfDictionary dict = (PdfDictionary) objReal;
        dict.put(PdfName.SUBTYPE, PdfName.FORM);
        assert obj instanceof PdfIndirectReference;
        return new PdfAppearance((PdfIndirectReference) obj);
    }

    private PdfAppearance handleDictionary(PdfObject objReal, PdfDictionary appDic) {
        PdfDictionary dict = (PdfDictionary) objReal;
        PdfName asP = appDic.getAsName(PdfName.AS);

        if (asP == null) {
            return null;
        }

        PdfIndirectReference iref = (PdfIndirectReference) dict.get(asP);
        if (iref == null) {
            return null;
        }

        PdfAppearance app = new PdfAppearance(iref);
        if (iref.isIndirect()) {
            PdfObject newObjReal = PdfReader.getPdfObject(iref);
            if (newObjReal instanceof PdfDictionary newDict) {
                newDict.put(PdfName.SUBTYPE, PdfName.FORM);
            }
        }

        return app;
    }


    private void drawAppearanceOnPage(PdfAppearance app, PdfDictionary annDic, int page) {
        Rectangle box = PdfReader.getNormalizedRectangle(annDic.getAsArray(PdfName.RECT));
        PdfContentByte cb = getOverContent(page);
        cb.setLiteral("Q ");
        cb.addTemplate(app, box.getLeft(), box.getBottom());
        cb.setLiteral("q ");
    }

    private void removeAnnotations(PdfArray annots, List<Integer> toRemove) {
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int idx = toRemove.get(i);
            annots.remove(idx);
        }
    }

    private void cleanUpPageAnnotations(PdfDictionary pageDic, PdfArray annots) {
        if (annots.isEmpty()) {
            pageDic.remove(PdfName.ANNOTS);
        }
    }



    /**
     * @see com.lowagie.text.pdf.PdfWriter#getPageReference(int)
     */
    @Override
    public PdfIndirectReference getPageReference(int page) {
        PdfIndirectReference ref = reader.getPageOrigRef(page);
        if (ref == null) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.page.number.1", page));
        }
        return ref;
    }

    void addDocumentField(PdfIndirectReference ref) {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObjectNullConverting(catalog.get(PdfName.ACROFORM),
                catalog);
        if (acroForm == null) {
            acroForm = new PdfDictionary();
            catalog.put(PdfName.ACROFORM, acroForm);
            markUsed(catalog);
        }
        PdfArray fields = (PdfArray) PdfReader.getPdfObjectNullConverting(acroForm.get(PdfName.FIELDS), acroForm);
        if (fields == null) {
            fields = new PdfArray();
            acroForm.put(PdfName.FIELDS, fields);
            markUsed(acroForm);
        }
        if (!acroForm.contains(PdfName.DA)) {
            acroForm.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
            markUsed(acroForm);
        }
        fields.add(ref);
        markUsed(fields);
    }

    void addFieldResources() throws IOException {
        if (fieldTemplates.isEmpty()) {
            return;
        }
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acroForm = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog);
        if (acroForm == null) {
            acroForm = new PdfDictionary();
            catalog.put(PdfName.ACROFORM, acroForm);
            markUsed(catalog);
        }
        PdfDictionary dr = (PdfDictionary) PdfReader.getPdfObject(acroForm.get(PdfName.DR), acroForm);
        if (dr == null) {
            dr = new PdfDictionary();
            acroForm.put(PdfName.DR, dr);
            markUsed(acroForm);
        }
        markUsed(dr);
        for (PdfTemplate template : fieldTemplates.keySet()) {
            PdfFormField.mergeResources(dr, (PdfDictionary) template.getResources(), this);
        }
        
        PdfDictionary fonts = dr.getAsDict(PdfName.FONT);
        if (fonts == null) {
            fonts = new PdfDictionary();
            dr.put(PdfName.FONT, fonts);
        }
        if (!fonts.contains(PdfName.HELV)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.HELVETICA);
            dic.put(PdfName.ENCODING, PdfName.WIN_ANSI_ENCODING);
            dic.put(PdfName.NAME, PdfName.HELV);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.HELV, addToBody(dic).getIndirectReference());
        }
        if (!fonts.contains(PdfName.ZADB)) {
            PdfDictionary dic = new PdfDictionary(PdfName.FONT);
            dic.put(PdfName.BASEFONT, PdfName.ZAPFDINGBATS);
            dic.put(PdfName.NAME, PdfName.ZADB);
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            fonts.put(PdfName.ZADB, addToBody(dic).getIndirectReference());
        }
        if (acroForm.get(PdfName.DA) == null) {
            acroForm.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
            markUsed(acroForm);
        }
    }

    void expandFields(PdfFormField field, List<PdfAnnotation> annotations) {
        annotations.add(field);
        List<PdfFormField> kids = field.getKidFields();
        if (kids != null) {
            for (PdfFormField kid : kids) {
                expandFields(kid, annotations);
            }
        }
    }

    void addAnnotation(PdfAnnotation annotation, PdfDictionary pageN) {
        List<PdfAnnotation> annotations = prepareAnnotations(annotation);
        for (PdfAnnotation pdfAnnotation : annotations) {
            processAnnotation(pdfAnnotation, pageN);
        }
    }

    private List<PdfAnnotation> prepareAnnotations(PdfAnnotation annotation) {
        List<PdfAnnotation> annotations = new ArrayList<>();
        if (annotation.isForm()) {
            fieldsAdded = true;
            getAcroFields();
            PdfFormField field = (PdfFormField) annotation;
            if (field.getParent() == null) {
                expandFields(field, annotations);
            }
        } else {
            annotations.add(annotation);
        }
        return annotations;
    }

    private PdfArray getOrCreateAnnotationArray(PdfDictionary pageN) {
        PdfObject pdfobj = PdfReader.getPdfObject(pageN.get(PdfName.ANNOTS), pageN);
        PdfArray annots;
        if (pdfobj == null || !pdfobj.isArray()) {
            annots = new PdfArray();
            pageN.put(PdfName.ANNOTS, annots);
            markUsed(pageN);
        } else {
            annots = (PdfArray) pdfobj;
        }
        return annots;
    }


    /**
     * Allows to add e.g. a Radiogroup without specifying a page for the data field parent. The parent (data) form field
     * isn't located on a page thus it doesn't make sense to specify one.
     *
     * @param annot annotation to be added
     */
    private void addAnnotationToDocument(PdfAnnotation annot) throws IOException {
        List<PdfAnnotation> allAnnots = new ArrayList<>();
        if (annot.isForm()) {
            handleFormField(annot, allAnnots);
        } else {
            allAnnots.add(annot);
        }

        processAnnotations(allAnnots);
    }

    private void handleFormField(PdfAnnotation annot, List<PdfAnnotation> allAnnots) {
        fieldsAdded = true;
        getAcroFields();
        PdfFormField field = (PdfFormField) annot;
        if (field.getParent() == null) {
            expandFields(field, allAnnots);
        }
    }

    private void processAnnotations(List<PdfAnnotation> allAnnots) throws IOException {
        PdfDictionary pageN = null;

        for (PdfAnnotation annot : allAnnots) {
            if (annot.getPlaceInPage() > 0) {
                pageN = reader.getPageN(annot.getPlaceInPage());
            }

            processFormField(annot);
            processAnnotation(annot, pageN);

            if (!annot.isUsed()) {
                annot.setUsed();
                addToBody(annot, annot.getIndirectReference());
            }
        }
    }

    private void processFormField(PdfAnnotation annot) {
        if (annot.isForm()) {
            if (!annot.isUsed()) {
                HashMap<PdfTemplate, Object> templates = annot.getTemplates();
                if (templates != null) {
                    fieldTemplates.putAll(templates);
                }
            }
            PdfFormField field = (PdfFormField) annot;
            if (field.getParent() == null) {
                addDocumentField(field.getIndirectReference());
            }
        }
    }

    private void processAnnotation(PdfAnnotation annot, PdfDictionary pageN) {
        if (annot.isAnnotation()) {
            if (annot.isForm() && pageN != null) {
                PdfArray annots = getOrCreateAnnotationArray(pageN);
                annots.add(annot.getIndirectReference());
                markUsed(annots);
                adjustAnnotationRectangle(annot, pageN);
            } else {
                throw new IllegalStateException("The radiobutton widget doesn't have a page: " + annot);
            }
        }
    }

    private void adjustAnnotationRectangle(PdfAnnotation annot, PdfDictionary pageN) {
        PdfRectangle rect = (PdfRectangle) annot.get(PdfName.RECT);
        if (rect != null && (rect.left() != 0 || rect.right() != 0 || rect.top() != 0 || rect.bottom() != 0)) {
            int rotation = PdfReader.getPageRotation(pageN);
            Rectangle pageSize = reader.getPageSizeWithRotation(pageN);
            PdfRectangle newRect = calculateAdjustedRectangle(rect, rotation, pageSize);
            annot.put(PdfName.RECT, newRect);
        }
    }

    private PdfRectangle calculateAdjustedRectangle(PdfRectangle rect, int rotation, Rectangle pageSize) {
        return switch (rotation) {
            case 90 -> new PdfRectangle(
                    pageSize.getTop() - rect.top(),
                    rect.left(),
                    pageSize.getTop() - rect.bottom(),
                    rect.right());
            case 180 -> new PdfRectangle(
                    pageSize.getRight() - rect.right(),
                    pageSize.getTop() - rect.bottom(),
                    pageSize.getRight() - rect.left(),
                    pageSize.getTop() - rect.top());
            case 270 -> new PdfRectangle(
                    rect.top(),
                    pageSize.getRight() - rect.right(),
                    rect.bottom(),
                    pageSize.getRight() - rect.left());
            default -> rect;
        };
    }



    public void addAnnotation(PdfAnnotation annot, int page) {
        //Bugfix to prevent that for autofill parents the /P page reference is added [^Lonzak]
        if (annot.isAnnotation()) {
            annot.setPage(page);
        }
        addAnnotation(annot, reader.getPageN(page));
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#addAnnotation(com.lowagie.text.pdf.PdfAnnotation)
     */
    @Override
    public void addAnnotation(PdfAnnotation annot) throws IOException {
        addAnnotationToDocument(annot);
    }

    private void outlineTravel(PRIndirectReference outline) {
        while (outline != null) {
            PdfDictionary outlineR = (PdfDictionary) PdfReader.getPdfObjectRelease(outline);
            PRIndirectReference first = (PRIndirectReference) outlineR.get(PdfName.FIRST);
            if (first != null) {
                outlineTravel(first);
            }
            PdfReader.killIndirect(outlineR.get(PdfName.DEST));
            PdfReader.killIndirect(outlineR.get(PdfName.A));
            PdfReader.killIndirect(outline);
            outline = (PRIndirectReference) outlineR.get(PdfName.NEXT);
        }
    }

    void deleteOutlines() {
        PdfDictionary catalog = reader.getCatalog();
        PRIndirectReference outlines = (PRIndirectReference) catalog.get(PdfName.OUTLINES);
        if (outlines == null) {
            return;
        }
        outlineTravel(outlines);
        PdfReader.killIndirect(outlines);
        catalog.remove(PdfName.OUTLINES);
        markUsed(catalog);
    }

    void setJavaScript() throws IOException {
        Map<String, PdfIndirectReference> djs = pdf.getDocumentLevelJS();
        if (djs.isEmpty()) {
            return;
        }
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary names = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog);
        if (names == null) {
            names = new PdfDictionary();
            catalog.put(PdfName.NAMES, names);
            markUsed(catalog);
        }
        markUsed(names);
        PdfDictionary tree = PdfNameTree.writeTree(djs, this);
        names.put(PdfName.JAVASCRIPT, addToBody(tree).getIndirectReference());
    }

    void addFileAttachments() throws IOException {
        Map<String, PdfIndirectReference> fs = pdf.getDocumentFileAttachment();
        if (fs.isEmpty()) {
            return;
        }
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary names = (PdfDictionary) PdfReader.getPdfObject(catalog.get(PdfName.NAMES), catalog);
        if (names == null) {
            names = new PdfDictionary();
            catalog.put(PdfName.NAMES, names);
            markUsed(catalog);
        }
        markUsed(names);
        Map<String, PdfObject> old = PdfNameTree.readTree(
                (PdfDictionary) PdfReader.getPdfObjectRelease(names.get(PdfName.EMBEDDEDFILES)));
        for (Map.Entry<String, PdfIndirectReference> entry : fs.entrySet()) {
            String name = entry.getKey();
            int k = 0;
            StringBuilder nn = new StringBuilder();
            nn.append(name);
            while (old.containsKey(nn.toString())) {
                ++k;
                nn.append(" ").append(k);
            }
            old.put(nn.toString(), entry.getValue());
        }
        PdfDictionary tree = PdfNameTree.writeTree(old, this);
        // Remove old EmbeddedFiles object if preset
        PdfObject oldEmbeddedFiles = names.get(PdfName.EMBEDDEDFILES);
        if (oldEmbeddedFiles != null) {
            PdfReader.killIndirect(oldEmbeddedFiles);
        }

        // Add new EmbeddedFiles object
        names.put(PdfName.EMBEDDEDFILES, addToBody(tree).getIndirectReference());
    }

    /**
     * Adds or replaces the Collection Dictionary in the Catalog.
     *
     * @param collection the new collection dictionary.
     */
    void makePackage(PdfCollection collection) {
        PdfDictionary catalog = reader.getCatalog();
        catalog.put(PdfName.COLLECTION, collection);
    }

    void setOutlines() throws IOException {
        if (newBookmarks == null) {
            return;
        }
        deleteOutlines();
        if (newBookmarks.isEmpty()) {
            return;
        }
        PdfDictionary catalog = reader.getCatalog();
        writeOutlines(catalog);
        markUsed(catalog);
    }

    /**
     * Sets the viewer preferences.
     *
     * @param preferences the viewer preferences
     * @see PdfWriter#setViewerPreferences(int)
     */
    @Override
    public void setViewerPreferences(int preferences) {
        useVp = true;
        this.viewerPreferences.setViewerPreferences(preferences);
    }

    /**
     * Adds a viewer preference
     *
     * @param key   a key for a viewer preference
     * @param value the value for the viewer preference
     * @see PdfViewerPreferences#addViewerPreference
     */
    @Override
    public void addViewerPreference(PdfName key, PdfObject value) {
        useVp = true;
        this.viewerPreferences.addViewerPreference(key, value);
    }

    /**
     * Set the signature flags.
     *
     * @param f the flags. This flags are ORed with current ones
     */
    @Override
    public void setSigFlags(int f) {
        sigFlags |= f;
    }

    /**
     * Always throws an <code>UnsupportedOperationException</code>.
     *
     * @param actionType ignore
     * @param action     ignore
     * @throws PdfException ignore
     * @see PdfStamper#setPageAction(PdfName, PdfAction, int)
     */
    @Override
    public void setPageAction(PdfName actionType, PdfAction action) throws PdfException {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage(
                ACTIONTYPE_PDFACTION_ACTION_INT_PAGE));
    }

    /**
     * Sets the open and close page additional action.
     *
     * @param actionType the action type. It can be <CODE>PdfWriter.PAGE_OPEN</CODE> or
     *                   <CODE>PdfWriter.PAGE_CLOSE</CODE>
     * @param action     the action to perform
     * @param page       the page where the action will be applied. The first page is 1
     * @throws PdfException if the action type is invalid
     */
    void setPageAction(PdfName actionType, PdfAction action, int page) throws PdfException {
        if (!actionType.equals(PAGE_OPEN) && !actionType.equals(PAGE_CLOSE)) {
            throw new PdfException(MessageLocalization.getComposedMessage("invalid.page.additional.action.type.1",
                    actionType.toString()));
        }
        PdfDictionary pg = reader.getPageN(page);
        PdfDictionary aa = (PdfDictionary) PdfReader.getPdfObject(pg.get(PdfName.AA), pg);
        if (aa == null) {
            aa = new PdfDictionary();
            pg.put(PdfName.AA, aa);
            markUsed(pg);
        }
        aa.put(actionType, action);
        markUsed(aa);
    }

    /**
     * Always throws an <code>UnsupportedOperationException</code>.
     *
     * @param seconds ignore
     */
    @Override
    public void setDuration(int seconds) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage(
                ACTIONTYPE_PDFACTION_ACTION_INT_PAGE));
    }

    /**
     * Always throws an <code>UnsupportedOperationException</code>.
     *
     * @param transition ignore
     */
    @Override
    public void setTransition(PdfTransition transition) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage(
                ACTIONTYPE_PDFACTION_ACTION_INT_PAGE));
    }

    /**
     * Sets the display duration for the page (for presentations)
     *
     * @param seconds the number of seconds to display the page. A negative value removes the entry
     * @param page    the page where the duration will be applied. The first page is 1
     */
    void setDuration(int seconds, int page) {
        PdfDictionary pg = reader.getPageN(page);
        if (seconds < 0) {
            pg.remove(PdfName.DUR);
        } else {
            pg.put(PdfName.DUR, new PdfNumber(seconds));
        }
        markUsed(pg);
    }

    /**
     * Sets the transition for the page
     *
     * @param transition the transition object. A <code>null</code> removes the transition
     * @param page       the page where the transition will be applied. The first page is 1
     */
    void setTransition(PdfTransition transition, int page) {
        PdfDictionary pg = reader.getPageN(page);
        if (transition == null) {
            pg.remove(PdfName.TRANS);
        } else {
            pg.put(PdfName.TRANS, transition.getTransitionDictionary());
        }
        markUsed(pg);
    }

    protected void markUsed(PdfObject obj) {
        if (append && obj != null) {
            PRIndirectReference ref;
            if (obj.type() == PdfObject.INDIRECT) {
                ref = (PRIndirectReference) obj;
            } else {
                ref = obj.getIndRef();
            }
            if (ref != null) {
                marked.put(ref.getNumber(), 1);
            }
        }
    }

    protected void markUsed(int num) {
        if (append) {
            marked.put(num, 1);
        }
    }

    /**
     * Getter for property append.
     *
     * @return Value of property append.
     */
    boolean isAppend() {
        return append;
    }

    /**
     * Additional-actions defining the actions to be taken in response to various trigger events affecting the document
     * as a whole. The actions types allowed are: <CODE>DOCUMENT_CLOSE</CODE>,
     * <CODE>WILL_SAVE</CODE>, <CODE>DID_SAVE</CODE>, <CODE>WILL_PRINT</CODE>
     * and <CODE>DID_PRINT</CODE>.
     *
     * @param actionType the action type
     * @param action     the action to execute in response to the trigger
     * @throws PdfException on invalid action type
     */
    @Override
    public void setAdditionalAction(PdfName actionType, PdfAction action) throws PdfException {
        if (!(actionType.equals(DOCUMENT_CLOSE) ||
                actionType.equals(WILL_SAVE) ||
                actionType.equals(DID_SAVE) ||
                actionType.equals(WILL_PRINT) ||
                actionType.equals(DID_PRINT))) {
            throw new PdfException(
                    MessageLocalization.getComposedMessage("invalid.additional.action.type.1", actionType.toString()));
        }
        PdfDictionary aa = reader.getCatalog().getAsDict(PdfName.AA);
        if (aa == null) {
            if (action == null) {
                return;
            }
            aa = new PdfDictionary();
            reader.getCatalog().put(PdfName.AA, aa);
        }
        markUsed(aa);
        if (action == null) {
            aa.remove(actionType);
        } else {
            aa.put(actionType, action);
        }
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setOpenAction(com.lowagie.text.pdf.PdfAction)
     */
    @Override
    public void setOpenAction(PdfAction action) {
        openAction = action;
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setOpenAction(java.lang.String)
     */
    @Override
    public void setOpenAction(String name) {
        throw new UnsupportedOperationException(
                MessageLocalization.getComposedMessage("open.actions.by.name.are.not.supported"));
    }

    /**
     * @see com.lowagie.text.pdf.PdfWriter#setThumbnail(com.lowagie.text.Image)
     */
    @Override
    public void setThumbnail(com.lowagie.text.Image image) {
        throw new UnsupportedOperationException(MessageLocalization.getComposedMessage("use.pdfstamper.setthumbnail"));
    }

    void setThumbnail(Image image, int page) throws DocumentException {
        PdfIndirectReference thumb = getImageReference(addDirectImageSimple(image));
        reader.resetReleasePage();
        PdfDictionary dic = reader.getPageN(page);
        dic.put(PdfName.THUMB, thumb);
        reader.resetReleasePage();
    }

    @Override
    public PdfContentByte getDirectContentUnder() {
        throw new UnsupportedOperationException(
                MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"));
    }

    @Override
    public PdfContentByte getDirectContent() {
        throw new UnsupportedOperationException(
                MessageLocalization.getComposedMessage("use.pdfstamper.getundercontent.or.pdfstamper.getovercontent"));
    }

    /**
     * Reads the OCProperties dictionary from the catalog of the existing document and fills the documentOCG,
     * documentOCGorder and OCGRadioGroup variables in PdfWriter. Note that the original OCProperties of the existing
     * document can contain more information.
     *
     * @since 2.1.2
     */
    protected void readOCProperties() {
        if (!documentOCG.isEmpty()) {
            return;
        }
        PdfDictionary dict = reader.getCatalog().getAsDict(PdfName.OCPROPERTIES);
        if (dict == null) {
            return;
        }
        PdfArray ocgs = dict.getAsArray(PdfName.OCGS);
        PdfIndirectReference ref;
        PdfLayer layer;
        Map<String, PdfLayer> ocgmap = new HashMap<>();
        for (PdfObject pdfObject : ocgs.getElements()) {
            ref = (PdfIndirectReference) pdfObject;
            layer = new PdfLayer(null);
            layer.setRef(ref);
            layer.setOnPanel(false);
            layer.merge((PdfDictionary) PdfReader.getPdfObject(ref));
            ocgmap.put(ref.toString(), layer);
        }
        PdfDictionary d = dict.getAsDict(PdfName.D);
        PdfArray off = d.getAsArray(PdfName.OFF);
        if (off != null) {
            for (PdfObject pdfObject : off.getElements()) {
                ref = (PdfIndirectReference) pdfObject;
                layer = ocgmap.get(ref.toString());
                layer.setOn(false);
            }
        }
        PdfArray order = d.getAsArray(PdfName.ORDER);
        if (order != null) {
            addOrder(null, order, ocgmap);
        }
        documentOCG.addAll(ocgmap.values());
        ocgRadioGroup = d.getAsArray(PdfName.RBGROUPS);
        ocgLocked = d.getAsArray(PdfName.LOCKED);
        if (ocgLocked == null) {
            ocgLocked = new PdfArray();
        }
    }

    /**
     * Recursive method to reconstruct the documentOCGorder variable in the writer.
     *
     * @param parent a parent PdfLayer (can be null)
     * @param arr    an array possibly containing children for the parent PdfLayer
     * @param ocgmap a HashMap with indirect reference Strings as keys and PdfLayer objects as values.
     * @since 2.1.2
     */
    private void addOrder(PdfLayer parent, PdfArray arr, Map<String, PdfLayer> ocgmap) {
        addOrderHelper(parent, arr, ocgmap, 0);
    }

    private void addOrderHelper(PdfLayer parent, PdfArray arr, Map<String, PdfLayer> ocgmap, int startIndex) {
        int size = arr.size();
        for (int i = startIndex; i < size; i++) {
            PdfObject obj = arr.getPdfObject(i);

            if (obj.isIndirect()) {
                handleIndirectObject(parent, arr, ocgmap, i, size);
                return; // Exit the loop after handling recursion
            } else if (obj.isArray()) {
                handleArrayObject(parent, (PdfArray) obj, ocgmap);
            }
        }
    }

    private void handleIndirectObject(PdfLayer parent, PdfArray arr, Map<String, PdfLayer> ocgmap, int index, int size) {
        PdfLayer layer = ocgmap.get(arr.getPdfObject(index).toString());
        layer.setOnPanel(true);
        registerLayer(layer);

        if (parent != null) {
            parent.addChild(layer);
        }

        if (shouldRecurse(arr, index, size)) {
            addOrderHelper(layer, (PdfArray) arr.getPdfObject(index + 1), ocgmap, 0);
        }
    }

    private boolean shouldRecurse(PdfArray arr, int index, int size) {
        return (index + 1 < size && arr.getPdfObject(index + 1).isArray());
    }

    private void handleArrayObject(PdfLayer parent, PdfArray sub, Map<String, PdfLayer> ocgmap) {
        if (sub.isEmpty()) {
            return; // Exit if the array is empty
        }

        PdfObject obj = sub.getPdfObject(0);
        if (obj.isString()) {
            handleStringInArray(parent, sub, ocgmap, obj);
        } else {
            addOrderHelper(parent, sub, ocgmap, 0);
        }
    }

    private void handleStringInArray(PdfLayer parent, PdfArray sub, Map<String, PdfLayer> ocgmap, PdfObject obj) {
        PdfLayer layer = new PdfLayer(obj.toString());
        layer.setOnPanel(true);
        registerLayer(layer);

        if (parent != null) {
            parent.addChild(layer);
        }

        PdfArray array = new PdfArray();
        sub.getElements().forEach(array::add);
        addOrderHelper(layer, array, ocgmap, 0);
    }




    /**
     * Gets the PdfLayer objects in an existing document as a Map with the names/titles of the layers as keys.
     *
     * @return a Map with all the PdfLayers in the document (and the name/title of the layer as key)
     * @since 2.1.2
     */
    public Map<String, PdfLayer> getPdfLayers() {
        if (documentOCG.isEmpty()) {
            readOCProperties();
        }
        Map<String, PdfLayer> map = new HashMap<>();
        PdfLayer layer;
        String key;
        for (PdfOCG o : documentOCG) {
            layer = (PdfLayer) o;
            if (layer.getTitle() == null) {
                key = layer.getAsString(PdfName.NAME).toString();
            } else {
                key = layer.getTitle();
            }
            if (map.containsKey(key)) {
                int seq = 2;
                String tmp = key + "(" + seq + ")";
                while (map.containsKey(tmp)) {
                    seq++;
                    tmp = key + "(" + seq + ")";
                }
                key = tmp;
            }
            map.put(key, layer);
        }
        return map;
    }

    /**
     * These methods are used by PdfStamper to override some PDF properties when signing PDF files.
     */

    public boolean isIncludeFileID() {
        return includeFileID;
    }

    public void setIncludeFileID(boolean includeFileID) {
        this.includeFileID = includeFileID;
    }

    public PdfObject getOverrideFileId() {
        return overrideFileId;
    }

    public void setOverrideFileId(PdfObject overrideFileId) {
        this.overrideFileId = overrideFileId;
    }

    public Calendar getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Calendar modificationDate) {
        this.modificationDate = modificationDate;
    }

    public boolean isUpdateMetadata() {
        return updateMetadata;
    }

    public void setUpdateMetadata(boolean updateMetadata) {
        this.updateMetadata = updateMetadata;
    }

    public boolean isUpdateDocInfo() {
        return updateDocInfo;
    }

    public void setUpdateDocInfo(boolean updateDocInfo) {
        this.updateDocInfo = updateDocInfo;
    }

    static class PageStamp {

        PdfDictionary pageN;
        StampContent under;
        StampContent over;
        PageResources pageResources;
        int replacePoint = 0;

        PageStamp(PdfStamperImp stamper, PdfDictionary pageN) {
            this.pageN = pageN;
            pageResources = new PageResources();
            PdfDictionary resources = pageN.getAsDict(PdfName.RESOURCES);
            pageResources.setOriginalResources(resources, stamper.namePtr);
        }
    }

}
