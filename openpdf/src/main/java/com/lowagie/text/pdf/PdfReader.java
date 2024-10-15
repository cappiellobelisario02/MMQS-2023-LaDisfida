/*
 * $Id: PdfReader.java 4096 2009-11-12 15:31:13Z blowagie $
 *
 * Copyright 2001, 2002 Paulo Soares
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

import com.lowagie.bouncycastle.BouncyCastleHelper;
import com.lowagie.text.DocWriter;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.IllegalBarcode128CharacterException;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.exceptions.UnsupportedPdfException;
import com.lowagie.text.pdf.PdfAnnotation.PdfImportedLink;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import org.apache.fop.pdf.PDFFilterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.InflaterInputStream;

/**
 * Reads a PDF document.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 * @author Kazuya Ujihara
 */
public class PdfReader implements PdfViewerPreferences, Closeable {
    private static final String FALSE_CONST = "false";
    private static final String BAD_USER_PASSWORD_CONST = "bad.user.password";
    private static final String ENDSTREAM_CONST = "endstream";
    private static final String ILLEGAL_LENGTH_VALUE_CONST = "illegal.length.value";

    int lengthValue;
    int cryptoMode;

    private static final Logger logger = Logger.getLogger(PdfReader.class.getName());

    static final PdfName[] pageInhCandidates = {PdfName.MEDIABOX,
            PdfName.ROTATE, PdfName.RESOURCES, PdfName.CROPBOX};

    private static final byte[] endstream = PdfEncodings
            .convertToBytes(ENDSTREAM_CONST, null);
    private static final byte[] endobj = PdfEncodings.convertToBytes("endobj", null);
    private final PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
    protected PRTokeniser tokens;
    // Each xref pair is a position
    // type 0 -> -1, 0
    // type 1 -> offset, 0
    // type 2 -> index, obj num
    protected int[] xref;
    protected Map<Integer, IntHashtable> objStmMark;
    protected IntHashtable objStmToOffset;
    protected boolean newXrefType;
    protected PdfDictionary trailer;
    protected PdfDictionary catalog;
    protected PageRefs pageRefs;
    protected PRAcroForm acroForm = null;
    protected boolean acroFormParsed = false;
    protected boolean encrypted = false;
    protected boolean rebuilt = false;
    protected int freeXref;
    protected boolean tampered = false;
    protected int lastXref;
    protected int eofPos;
    protected char pdfVersion;
    protected PdfEncryption decrypt;
    protected byte[] password = null; // added by ujihara for decryption
    protected Key certificateKey = null; // added by Aiken Sam for certificate
    // decryption
    protected Certificate certificate = null; // added by Aiken Sam for
    // certificate decryption
    protected String certificateKeyProvider = null; // added by Aiken Sam for
    protected List<PdfObject> strings = new ArrayList<>();
    protected boolean sharedStreams = true;
    protected boolean consolidateNamedDestinations = false;
    protected boolean remoteToLocalNamedDestinations = false;
    protected int rValue;
    protected int pValue;
    PdfDictionary rootPages;
    private List<PdfObject> xrefObj;
    // certificate decryption
    private boolean ownerPasswordUsed;
    // allow the PDF to be modified even if the owner password was not supplied
    // if encrypted (non-encrypted documents may be modified regardless)
    private boolean modificationAllowedWithoutOwnerPassword = true;
    private int objNum;
    private int objGen;
    private int fileLength;
    private boolean hybridXref;
    private int lastXrefPartial = -1;
    private boolean partial;
    private PRIndirectReference cryptoRef;
    private boolean encryptionError;

    /**
     * Holds value of property appendable.
     */
    private boolean appendable;
    // Track how deeply nested the current object is, so
    // we know when to return an individual null or boolean, or
    // reuse one of the static ones.
    private int readDepth = 0;

    protected PdfReader() {
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param filename the file name of the document
     * @throws IOException on error
     */
    public PdfReader(String filename) throws IOException, PDFFilterException {
        this(filename, null);
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param filename      the file name of the document
     * @param ownerPassword the password to read the document
     * @throws IOException on error
     */
    public PdfReader(String filename, byte[] ownerPassword) throws IOException, PDFFilterException {
        password = ownerPassword;
        tokens = new PRTokeniser(filename);
        readPdf();
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param pdfIn the byte array with the document
     * @throws IOException on error
     */
    public PdfReader(byte[] pdfIn) throws IOException, PDFFilterException {
        this(pdfIn, null);
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param pdfIn         the byte array with the document
     * @param ownerPassword the password to read the document
     * @throws IOException on error
     */
    public PdfReader(byte[] pdfIn, byte[] ownerPassword) throws IOException, PDFFilterException {
        password = ownerPassword;
        tokens = new PRTokeniser(pdfIn);
        readPdf();
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param filename               the file name of the document
     * @param certificate            the certificate to read the document
     * @param certificateKey         the private key of the certificate
     * @param certificateKeyProvider the security provider for certificateKey
     * @throws IOException on error
     */
    public PdfReader(String filename, Certificate certificate,
            Key certificateKey, String certificateKeyProvider) throws IOException, PDFFilterException {
        this.certificate = certificate;
        this.certificateKey = certificateKey;
        this.certificateKeyProvider = certificateKeyProvider;
        tokens = new PRTokeniser(filename);
        readPdf();
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param url the URL of the document
     * @throws IOException on error
     */
    public PdfReader(URL url) throws IOException, PDFFilterException {
        this(url, null);
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param url           the URL of the document
     * @param ownerPassword the password to read the document
     * @throws IOException on error
     */
    public PdfReader(URL url, byte[] ownerPassword) throws IOException, PDFFilterException {
        if (ownerPassword == null || ownerPassword.length == 0) {
            throw new IllegalArgumentException("Password cannot be null or empty for security reasons.");
        }

        this.password = ownerPassword;
        this.tokens = new PRTokeniser(new RandomAccessFileOrArray(url));
        readPdf();
    }


    /**
     * Reads and parses a PDF document.
     *
     * @param is            the <CODE>InputStream</CODE> containing the document. The stream is read to the end but is
     *                      not closed
     * @param ownerPassword the password to read the document
     * @throws IOException on error
     */
    public PdfReader(InputStream is, byte[] ownerPassword) throws IOException, PDFFilterException {
        password = ownerPassword;
        tokens = new PRTokeniser(new RandomAccessFileOrArray(is));
        readPdf();
    }

    /**
     * Reads and parses a PDF document.
     *
     * @param is the <CODE>InputStream</CODE> containing the document. The stream is read to the end but is not closed
     * @throws IOException on error
     */
    public PdfReader(InputStream is) throws IOException, PDFFilterException {
        this(is, null);
    }

    /**
     * Reads and parses a pdf document. Contrary to the other constructors only the xref is read into memory. The reader
     * is said to be working in "partial" mode as only parts of the pdf are read as needed. The pdf is left open but may
     * be closed at any time with <CODE>PdfReader.close()</CODE>, reopen is automatic.
     *
     * @param raf           the document location
     * @param ownerPassword the password or <CODE>null</CODE> for no password
     * @throws IOException on error
     */
    public PdfReader(RandomAccessFileOrArray raf, byte[] ownerPassword)
            throws IOException {
        password = ownerPassword;
        partial = true;
        tokens = new PRTokeniser(raf);
        readPdfPartial();
    }

    /**
     * Creates an independent duplicate.
     *
     * @param reader the <CODE>PdfReader</CODE> to duplicate
     */
    public PdfReader(PdfReader reader) {
        this.appendable = reader.appendable;
        this.consolidateNamedDestinations = reader.consolidateNamedDestinations;
        this.encrypted = reader.encrypted;
        this.rebuilt = reader.rebuilt;
        this.sharedStreams = reader.sharedStreams;
        this.tampered = reader.tampered;
        this.password = reader.password;
        this.pdfVersion = reader.pdfVersion;
        this.eofPos = reader.eofPos;
        this.freeXref = reader.freeXref;
        this.lastXref = reader.lastXref;
        this.tokens = new PRTokeniser(reader.tokens.getSafeFile());
        if (reader.decrypt != null) {
            this.decrypt = new PdfEncryption(reader.decrypt);
        }
        this.pValue = reader.pValue;
        this.rValue = reader.rValue;
        this.xrefObj = new ArrayList<>(reader.xrefObj);
        for (int k = 0; k < reader.xrefObj.size(); ++k) {
            this.xrefObj.set(k,
                    duplicatePdfObject(reader.xrefObj.get(k), this));
        }
        this.pageRefs = new PageRefs(reader.pageRefs, this);
        this.trailer = (PdfDictionary) duplicatePdfObject(reader.trailer, this);
        this.catalog = (trailer != null) ? trailer.getAsDict(PdfName.ROOT) : null;
        assert catalog != null;
        this.rootPages = catalog.getAsDict(PdfName.PAGES);
        this.fileLength = reader.fileLength;
        this.partial = reader.partial;
        this.hybridXref = reader.hybridXref;
        this.objStmToOffset = reader.objStmToOffset;
        this.xref = reader.xref;
        this.cryptoRef = (PRIndirectReference) duplicatePdfObject(reader.cryptoRef,
                this);
        this.ownerPasswordUsed = reader.ownerPasswordUsed;
    }

    static int getPageRotation(PdfDictionary page) {
        if (page == null) {
            throw new NullPointerException("To get the rotation the page must not be null!");
        }
        PdfNumber rotate = page.getAsNumber(PdfName.ROTATE);
        if (rotate == null) {
            return 0;
        } else {
            int n = rotate.intValue();
            n %= 360;
            return n < 0 ? n + 360 : n;
        }
    }

    /**
     * Normalizes a <CODE>Rectangle</CODE> so that llx and lly are smaller than urx and ury.
     *
     * @param box the original rectangle
     * @return a normalized <CODE>Rectangle</CODE>
     */
    public static Rectangle getNormalizedRectangle(PdfArray box) {
        // Check if the box array is null or has fewer than 4 elements
        if (box == null || box.size() < 4) {
            throw new IllegalArgumentException("PdfArray must contain at least four elements.");
        }

        float llx = getFloatValue(box, 0);
        float lly = getFloatValue(box, 1);
        float urx = getFloatValue(box, 2);
        float ury = getFloatValue(box, 3);

        return new Rectangle(
                Math.min(llx, urx),
                Math.min(lly, ury),
                Math.max(llx, urx),
                Math.max(lly, ury)
        );
    }

    private static float getFloatValue(PdfArray box, int index) {
        PdfObject pdfObject = getPdfObjectRelease(box.getPdfObject(index));

        if (!(pdfObject instanceof PdfNumber)) {
            throw new IllegalArgumentException("Expected a PdfNumber at index " + index);
        }

        return ((PdfNumber) pdfObject).floatValue();
    }


    /**
     * @param obj an object of {@link PdfObject}
     * @return a PdfObject
     */
    public static PdfObject getPdfObjectRelease(PdfObject obj) {
        PdfObject obj2 = getPdfObject(obj);
        releaseLastXrefPartial(obj);
        return obj2;
    }

    /**
     * If given object is instance of {@link PdfNull}, then {@code null} is returned. The provided object otherwise.
     *
     * @param obj object to convert
     * @return provided object or null
     */
    public static PdfObject convertPdfNull(PdfObject obj) {
        if (obj == null || obj instanceof PdfNull) {
            return null;
        }
        return obj;
    }

    /**
     * Returns {@link #getPdfObjectRelease(PdfObject)} with applied {@link #convertPdfNull(PdfObject)}.
     */
    public static PdfObject getPdfObjectReleaseNullConverting(PdfObject obj) {
        return convertPdfNull(getPdfObjectRelease(obj));
    }

    /**
     * Reads a <CODE>PdfObject</CODE> resolving an indirect reference if needed.
     *
     * @param obj the <CODE>PdfObject</CODE> to read
     * @return the resolved <CODE>PdfObject</CODE>
     */
    public static PdfObject getPdfObject(PdfObject obj) {
        if (obj == null) {
            return null;
        }
        if (!obj.isIndirect()) {
            return obj;
        }
        try {
            PRIndirectReference ref = (PRIndirectReference) obj;
            int idx = ref.getNumber();
            boolean appendable = ref.getReader().appendable;
            obj = ref.getReader().getPdfObject(idx);
            if (obj == null) {
                return null;
            } else {
                if (appendable) {
                    switch (obj.type()) {
                        case PdfObject.NULL:
                            obj = new PdfNull();
                            break;
                        case PdfObject.BOOLEAN:
                            obj = new PdfBoolean(((PdfBoolean) obj).booleanValue());
                            break;
                        case PdfObject.NAME:
                            obj = new PdfName(obj.getBytes());
                            break;
                        default:
                            break;
                    }
                    obj.setIndRef(ref);
                }
                return obj;
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Reads a <CODE>PdfObject</CODE> resolving an indirect reference if needed. If the reader was opened in partial
     * mode the object will be released to save memory.
     *
     * @param obj    the <CODE>PdfObject</CODE> to read
     * @param parent parent object
     * @return a PdfObject
     */
    public static PdfObject getPdfObjectRelease(PdfObject obj, PdfObject parent) {
        PdfObject obj2 = getPdfObject(obj, parent);
        releaseLastXrefPartial(obj);
        return obj2;
    }

    /**
     * @param obj    the <CODE>PdfObject</CODE> to read
     * @param parent parent object
     * @return a PdfObject
     */
    public static PdfObject getPdfObject(PdfObject obj, PdfObject parent) {
        if (obj == null) {
            return null;
        }
        if (!obj.isIndirect()) {
            PRIndirectReference ref;
            if (parent != null && (ref = parent.getIndRef()) != null
                    && ref.getReader().isAppendable()) {
                switch (obj.type()) {
                    case PdfObject.NULL:
                        obj = new PdfNull();
                        break;
                    case PdfObject.BOOLEAN:
                        obj = new PdfBoolean(((PdfBoolean) obj).booleanValue());
                        break;
                    case PdfObject.NAME:
                        obj = new PdfName(obj.getBytes());
                        break;
                    default:
                        break;
                }
                obj.setIndRef(ref);
            }
            return obj;
        }
        return getPdfObject(obj);
    }

    /**
     * Returns {@link #getPdfObject(PdfObject, PdfObject)} with applied {@link #convertPdfNull(PdfObject)}.
     */
    public static PdfObject getPdfObjectNullConverting(PdfObject obj, PdfObject parent) {
        return convertPdfNull(getPdfObject(obj, parent));
    }

    /**
     * @param obj an object of {@link PdfObject}
     */
    public static void releaseLastXrefPartial(PdfObject obj) {
        if (obj == null) {
            return;
        }
        if (!obj.isIndirect()) {
            return;
        }
        if (!(obj instanceof PRIndirectReference ref)) {
            return;
        }

        PdfReader reader = ref.getReader();
        if (reader.partial && reader.lastXrefPartial != -1
                && reader.lastXrefPartial == ref.getNumber()) {
            reader.xrefObj.set(reader.lastXrefPartial, null);
        }
        reader.lastXrefPartial = -1;
    }

    /**
     * Eliminates the reference to the object freeing the memory used by it and clearing the xref entry.
     *
     * @param obj the object. If it's an indirect reference it will be eliminated
     * @return the object or the already erased dereferenced object
     */
    public static PdfObject killIndirect(PdfObject obj) {
        if (obj == null || obj.isNull()) {
            return null;
        }
        PdfObject ret = getPdfObjectRelease(obj);
        if (obj.isIndirect()) {
            PRIndirectReference ref = (PRIndirectReference) obj;
            PdfReader reader = ref.getReader();
            int n = ref.getNumber();
            reader.xrefObj.set(n, null);
            if (reader.partial) {
                reader.xref[n * 2] = -1;
            }
        }
        return ret;
    }

    /**
     * Decodes a stream that has the FlateDecode filter.
     *
     * @param in the input data
     * @return the decoded data
     */
    public static byte[] flateDecode(byte[] in) {
        byte[] b = flateDecode(in, true);
        if (b == null) {
            return flateDecode(in, false);
        }
        return b;
    }

    /**
     * @param in     the input data
     * @param dicPar an object of {@link PdfObject}
     * @return a byte array
     */
    public static byte[] decodePredictor(byte[] in, PdfObject dicPar) throws PDFFilterException {
        if (dicPar == null || !dicPar.isDictionary()) {
            return in;
        }
        PdfDictionary dic = (PdfDictionary) dicPar;

        int predictor = getPredictor(dic);
        if (predictor < 10) {
            return in;
        }

        int width = getIntValue(dic, PdfName.COLUMNS, 1);
        int colors = getIntValue(dic, PdfName.COLORS, 1);
        int bpc = getIntValue(dic, PdfName.BITSPERCOMPONENT, 8);

        return decodeImage(in, colors, width, bpc);
    }

    private static int getPredictor(PdfDictionary dic) {
        PdfObject obj = getPdfObject(dic.get(PdfName.PREDICTOR));
        return obj != null && obj.isNumber() ? ((PdfNumber) obj).intValue() : 1;
    }

    private static int getIntValue(PdfDictionary dic, PdfName key, int defaultValue) {
        PdfObject obj = getPdfObject(dic.get(key));
        return obj != null && obj.isNumber() ? ((PdfNumber) obj).intValue() : defaultValue;
    }

    private static byte[] decodeImage(byte[] in, int colors, int width, int bpc) throws PDFFilterException {
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(in));
        ByteArrayOutputStream fout = new ByteArrayOutputStream(in.length);
        int bytesPerPixel = colors * bpc / 8;
        int bytesPerRow = (colors * width * bpc + 7) / 8;
        byte[] curr = new byte[bytesPerRow];
        byte[] prior = new byte[bytesPerRow];

        try {
            while (true) {
                int filter = dataStream.read();
                if (filter < 0) {
                    break;
                }
                dataStream.readFully(curr, 0, bytesPerRow);

                applyFilter(filter, curr, prior, bytesPerPixel, bytesPerRow);
                fout.write(curr);

                // Swap curr and prior
                byte[] tmp = prior;
                prior = curr;
                curr = tmp;
            }
        } catch (IOException e) {
            throw new PDFFilterException("Error decoding image: " + e.getMessage());
        }

        return fout.toByteArray();
    }

    private static void applyFilter(int filter, byte[] curr, byte[] prior, int bytesPerPixel, int bytesPerRow) throws PDFFilterException {
        switch (filter) {
            case 0: // PNG_FILTER_NONE
                break;
            case 1: // PNG_FILTER_SUB
                applySubFilter(curr, bytesPerPixel, bytesPerRow);
                break;
            case 2: // PNG_FILTER_UP
                applyUpFilter(curr, prior, bytesPerRow);
                break;
            case 3: // PNG_FILTER_AVERAGE
                applyAverageFilter(curr, prior, bytesPerPixel, bytesPerRow);
                break;
            case 4: // PNG_FILTER_PAETH
                applyPaethFilter(curr, prior, bytesPerPixel, bytesPerRow);
                break;
            default:
                throw new PDFFilterException("Unknown PNG filter: " + filter);
        }
    }

    private static void applySubFilter(byte[] curr, int bytesPerPixel, int bytesPerRow) {
        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
            curr[i] += curr[i - bytesPerPixel];
        }
    }

    private static void applyUpFilter(byte[] curr, byte[] prior, int bytesPerRow) {
        for (int i = 0; i < bytesPerRow; i++) {
            curr[i] += prior[i];
        }
    }

    private static void applyAverageFilter(byte[] curr, byte[] prior, int bytesPerPixel, int bytesPerRow) {
        for (int i = 0; i < bytesPerPixel; i++) {
            curr[i] += (byte) (prior[i] / 2);
        }
        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
            curr[i] = (byte) ((curr[i] + (curr[i - bytesPerPixel] & 0xff) + (prior[i] & 0xff)) / 2);
        }
    }

    private static void applyPaethFilter(byte[] curr, byte[] prior, int bytesPerPixel, int bytesPerRow) {
        for (int i = 0; i < bytesPerPixel; i++) {
            curr[i] += prior[i];
        }

        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
            int a = curr[i - bytesPerPixel] & 0xff;
            int b = prior[i] & 0xff;
            int c = prior[i - bytesPerPixel] & 0xff;

            int p = a + b - c;
            int pa = Math.abs(p - a);
            int pb = Math.abs(p - b);
            int pc = Math.abs(p - c);

            int ret;

            if(pa <= pb && pa <= pc){
                ret = a;
            }
            else if(pb <= pc){
                ret = b;
            }
            else{
                ret = c;
            }

            curr[i] += (byte) (ret);
        }
    }

    /**
     * A helper to FlateDecode.
     *
     * @param in     the input data
     * @param strict <CODE>true</CODE> to read a correct stream. <CODE>false</CODE> to
     *               try to read a corrupted stream
     * @return the decoded data
     */
    public static byte[] flateDecode(byte[] in, boolean strict) {
        ByteArrayInputStream stream = new ByteArrayInputStream(in);
        InflaterInputStream zip = new InflaterInputStream(stream);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[strict ? 4092 : 1];
        try {
            int n;
            while ((n = zip.read(b)) >= 0) {
                out.write(b, 0, n);
            }
            zip.close();
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
            if (strict) {
                return new byte[]{};
            }
            return out.toByteArray();
        }
    }

    /**
     * Decodes a stream that has the ASCIIHexDecode filter.
     *
     * @param in the input data
     * @return the decoded data
     */
    public static byte[] asciiHexDecode(byte[] in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean first = true;
        int n1 = 0;
        for (byte b : in) {
            int ch = b & 0xff;
            if (ch == '>') {
                break;
            }
            if (PRTokeniser.isWhitespace(ch)) {
                continue;
            }
            int n = PRTokeniser.getHex(ch);
            if (n == -1) {
                throw new IllegalBarcode128CharacterException(
                        MessageLocalization
                                .getComposedMessage("illegal.character.in.asciihexdecode"));
            }
            if (first) {
                n1 = n;
            } else {
                out.write((byte) ((n1 << 4) + n));
            }
            first = !first;
        }
        if (!first) {
            out.write((byte) (n1 << 4));
        }
        return out.toByteArray();
    }

    /**
     * Decodes a stream that has the ASCII85Decode filter.
     *
     * @param in the input data
     * @return the decoded data
     */
    public static byte[] ascii85Decode(byte[] in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int state = 0;
        int[] chn = new int[5];
        for (byte b : in) {
            int ch = b & 0xff;
            if (ch == '~') {
                break;
            }
            else if (PRTokeniser.isWhitespace(ch)) {
                continue;
            }
            else if (ch == 'z' && state == 0) {
                out.write(0);
                out.write(0);
                out.write(0);
                out.write(0);
                continue;
            }
            else if (ch < '!' || ch > 'u') {
                throw new IllegalBarcode128CharacterException(
                        MessageLocalization
                                .getComposedMessage("illegal.character.in.ascii85decode"));
            }
            chn[state] = ch - '!';
            ++state;
            if (state == 5) {
                state = 0;
                int r = 0;
                for (int j = 0; j < 5; ++j) {
                    r = r * 85 + chn[j];
                }
                out.write((byte) (r >> 24));
                out.write((byte) (r >> 16));
                out.write((byte) (r >> 8));
                out.write((byte) r);
            }
        }
        int r;
        // We'll ignore the next two lines for the sake of perpetuating broken
        // PDFs

        switch(state){
            case 2:
                r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + 85 * 85 * 85
                        + 85 * 85 + 85;
                out.write((byte) (r >> 24));
                break;
            case 3:
                r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85
                        + 85 * 85 + 85;
                out.write((byte) (r >> 24));
                out.write((byte) (r >> 16));
                break;
            case 4:
                r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85
                        + chn[3] * 85 + 85;
                out.write((byte) (r >> 24));
                out.write((byte) (r >> 16));
                out.write((byte) (r >> 8));
                break;
            default:
                break;
        }

        return out.toByteArray();
    }

    /**
     * Decodes a stream that has the LZWDecode filter.
     *
     * @param in the input data
     * @return the decoded data
     */
    public static byte[] lzwDecode(byte[] in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LZWDecoder lzw = new LZWDecoder();
        lzw.decode(in, out);
        return out.toByteArray();
    }

    /**
     * Get the content from a stream applying the required filters.
     *
     * @param stream the stream
     * @param file   the location where the stream is
     * @return the stream content
     * @throws IOException on error
     */
    public static byte[] getStreamBytes(PRStream stream, RandomAccessFileOrArray file)
            throws IOException, PDFFilterException {
        PdfObject filter = getPdfObjectRelease(stream.get(PdfName.FILTER));
        byte[] b = getStreamBytesRaw(stream, file);

        List<PdfObject> filters = addFilters(new ArrayList<>(), filter);
        List<PdfObject> dp = getDecodeParams(stream);

        return applyFilters(b, filters, dp);
    }

    private static List<PdfObject> getDecodeParams(PRStream stream) {
        List<PdfObject> dp = new ArrayList<>();
        PdfObject dpo = getPdfObjectRelease(stream.get(PdfName.DECODEPARMS));
        if (dpo == null || (!dpo.isDictionary() && !dpo.isArray())) {
            dpo = getPdfObjectRelease(stream.get(PdfName.DP));
        }
        if (dpo != null) {
            if (dpo.isDictionary()) {
                dp.add(dpo);
            } else if (dpo.isArray()) {
                dp = ((PdfArray) dpo).getElements();
            }
        }
        return dp;
    }

    private static byte[] applyFilters(byte[] b, List<PdfObject> filters, List<PdfObject> dp)
            throws PDFFilterException, IOException {
        for (int j = 0; j < filters.size(); ++j) {
            PdfObject filterObject = getPdfObjectRelease(filters.get(j));
            if (filterObject == null) {
                throw new UnsupportedPdfException(
                        MessageLocalization.getComposedMessage("null.filter.object.found")
                );
            }

            String name = filterObject.toString();
            b = decodeStream(b, name, j < dp.size() ? dp.get(j) : null);
        }
        return b;
    }

    private static byte[] decodeStream(byte[] b, String filterName, PdfObject decodeParam)
            throws PDFFilterException, IOException {
        switch (filterName) {
            case "/FlateDecode", "/Fl":
                b = flateDecode(b);
                if (decodeParam != null) {
                    b = decodePredictor(b, decodeParam);
                }
                break;
            case "/ASCIIHexDecode", "/AHx":
                b = asciiHexDecode(b);
                break;
            case "/ASCII85Decode", "/A85":
                b = ascii85Decode(b);
                break;
            case "/LZWDecode":
                b = lzwDecode(b);
                if (decodeParam != null) {
                    b = decodePredictor(b, decodeParam);
                }
                break;
            case "/Crypt":
                // Handle Crypt filter if necessary
                break;
            default:
                throw new UnsupportedPdfException(
                        MessageLocalization.getComposedMessage(
                                "the.filter.1.is.not.supported", filterName)
                );
        }
        return b;
    }


    /**
     * Get the content from a stream applying the required filters.
     *
     * @param stream the stream
     * @return the stream content
     * @throws IOException on error
     */
    public static byte[] getStreamBytes(PRStream stream) throws IOException, PDFFilterException {
        try (RandomAccessFileOrArray rf = stream.getReader().getSafeFile()) {
            rf.reOpen();
            return getStreamBytes(stream, rf);
        } catch (PDFFilterException e) {
            throw new PDFFilterException(e.getMessage());
        }
        //da vedere come effettuare il log
    }

    /**
     * Get the content from a stream as it is without applying any filter.
     *
     * @param stream the stream
     * @param file   the location where the stream is
     * @return the stream content
     * @throws IOException on error
     */
    public static byte[] getStreamBytesRaw(PRStream stream,
            RandomAccessFileOrArray file) throws IOException {
        PdfReader reader = stream.getReader();
        byte[] b;
        if (stream.getOffset() < 0) {
            b = stream.getBytes();
        } else {
            b = new byte[stream.getLength()];
            file.seek(stream.getOffset());
            file.readFully(b);
            PdfEncryption decrypt = reader.getDecrypt();
            if (decrypt != null) {
                PdfObject filter = getPdfObjectRelease(stream.get(PdfName.FILTER));
                List<PdfObject> filters = new ArrayList<>();
                filters = addFilters(filters, filter);
                boolean skip = false;
                for (PdfObject filter1 : filters) {
                    PdfObject obj = getPdfObjectRelease(filter1);
                    if (obj != null && obj.toString().equals("/Crypt")) {
                        skip = true;
                        break;
                    }
                }
                if (!skip) {
                    decrypt.setHashKey(stream.getObjNum(), stream.getObjGen());
                    b = decrypt.decryptByteArray(b);
                }
            }
        }
        return b;
    }

    private static List<PdfObject> addFilters(List<PdfObject> filters, PdfObject filter) {
        if (filter != null) {
            if (filter.isName()) {
                filters.add(filter);
            } else if (filter.isArray()) {
                filters = ((PdfArray) filter).getElements();
            }
        }
        return filters;
    }

    /**
     * Get the content from a stream as it is without applying any filter.
     *
     * @param stream the stream
     * @return the stream content
     * @throws IOException on error
     */
    public static byte[] getStreamBytesRaw(PRStream stream) throws IOException {
        try (RandomAccessFileOrArray rf = stream.getReader().getSafeFile()) {
            rf.reOpen();
            return getStreamBytesRaw(stream, rf);
        }
        //da vedere come effettuare il log
    }

    private static boolean equalsn(byte[] a1, byte[] a2) {
        int length = a2.length;
        for (int k = 0; k < length; ++k) {
            if (a1[k] != a2[k]) {
                return false;
            }
        }
        return true;
    }

    private static boolean existsName(PdfDictionary dic, PdfName key, PdfName value) {
        PdfObject type = getPdfObjectRelease(dic.get(key));
        if (type == null || !type.isName()) {
            return false;
        }
        PdfName name = (PdfName) type;
        return name.equals(value);
    }

    static String getFontNameFromDescriptor(PdfDictionary dic) {
        return getFontName(dic, PdfName.FONTNAME);
    }

    private static String getFontName(PdfDictionary dic) {
        return getFontName(dic, PdfName.BASEFONT);
    }

    private static String getFontName(PdfDictionary dic, PdfName property) {
        if (dic == null) {
            return null;
        }
        PdfObject type = getPdfObjectRelease(dic.get(property));
        if (type == null || !type.isName()) {
            return null;
        }
        return PdfName.decodeName(type.toString());
    }

    static boolean isFontSubset(String fontName) {
        return fontName != null && fontName.length() >= 8
                && fontName.charAt(6) == '+';
    }

    private static String getSubsetPrefix(PdfDictionary dic) {
        if (dic == null) {
            return null;
        }
        String s = getFontName(dic);
        if (s == null) {
            return null;
        }
        if (s.length() < 8 || s.charAt(6) != '+') {
            return null;
        }
        for (int k = 0; k < 6; ++k) {
            char c = s.charAt(k);
            if (c < 'A' || c > 'Z') {
                return null;
            }
        }
        return s;
    }

    private static PdfArray getNameArray(PdfObject obj) {
        if (obj == null) {
            return null;
        }
        obj = getPdfObjectRelease(obj);
        if (obj == null) {
            return null;
        }
        if (obj.isArray()) {
            return (PdfArray) obj;
        } else if (obj.isDictionary()) {
            PdfObject arr2 = getPdfObjectRelease(((PdfDictionary) obj).get(PdfName.D));
            if (arr2 != null && arr2.isArray()) {
                return (PdfArray) arr2;
            }
        }
        return null;
    }

    protected static PdfDictionary duplicatePdfDictionary(PdfDictionary original,
            PdfDictionary copy, PdfReader newReader) {
        if (copy == null) {
            copy = new PdfDictionary();
        }
        for (PdfName o : original.getKeys()) {
            copy.put(o, duplicatePdfObject(original.get(o), newReader));
        }
        return copy;
    }

    protected static PdfObject duplicatePdfObject(PdfObject original,
            PdfReader newReader) {
        if (original == null) {
            return null;
        }
        switch (original.type()) {
            case PdfObject.DICTIONARY: {
                return duplicatePdfDictionary((PdfDictionary) original, null, newReader);
            }
            case PdfObject.STREAM: {
                PRStream org = (PRStream) original;
                PRStream stream = new PRStream(org, null, newReader);
                duplicatePdfDictionary(org, stream, newReader);
                return stream;
            }
            case PdfObject.ARRAY: {
                PdfArray arr = new PdfArray();
                ((PdfArray) original).getElements()
                        .forEach(pdfObject -> arr.add(duplicatePdfObject(pdfObject, newReader)));
                return arr;
            }
            case PdfObject.INDIRECT: {
                PRIndirectReference org = (PRIndirectReference) original;
                return new PRIndirectReference(newReader, org.getNumber(),
                        org.getGeneration());
            }
            default:
                return original;
        }
    }

    /**
     * Gets a new file instance of the original PDF document.
     *
     * @return a new file instance of the original PDF document
     */
    public RandomAccessFileOrArray getSafeFile() {
        return tokens.getSafeFile();
    }

    protected PdfReaderInstance getPdfReaderInstance(PdfWriter writer) {
        return new PdfReaderInstance(this, writer);
    }

    /**
     * Gets the number of pages in the document.
     *
     * @return the number of pages in the document
     */
    public int getNumberOfPages() {
        return pageRefs.size();
    }

    /**
     * Returns the document's catalog. This dictionary is not a copy, any changes will be reflected in the catalog.
     *
     * @return the document's catalog
     */
    public PdfDictionary getCatalog() {
        return catalog;
    }

    /**
     * Returns the document's acroform, if it has one.
     *
     * @return the document's acroform
     */
    public PRAcroForm getAcroForm() {
        if (!acroFormParsed) {
            acroFormParsed = true;
            PdfObject form = catalog.get(PdfName.ACROFORM);
            if (form != null) {
                try {
                    acroForm = new PRAcroForm(this);
                    acroForm.readAcroForm((PdfDictionary) getPdfObject(form));
                } catch (Exception e) {
                    acroForm = null;
                }
            }
        }
        return acroForm;
    }

    /**
     * Gets the page rotation. This value can be 0, 90, 180 or 270.
     *
     * @param index the page number. The first page is 1
     * @return the page rotation
     */
    public int getPageRotation(int index) {
        return getPageRotation(pageRefs.getPageNRelease(index));
    }

    /**
     * Gets the page size, taking rotation into account. This is a
     * <CODE>Rectangle</CODE> with the value of the /MediaBox and the /Rotate key.
     *
     * @param index the page number. The first page is 1
     * @return a <CODE>Rectangle</CODE>
     */
    public Rectangle getPageSizeWithRotation(int index) {
        return getPageSizeWithRotation(pageRefs.getPageNRelease(index));
    }

    /**
     * Gets the rotated page from a page dictionary.
     *
     * @param page the page dictionary
     * @return the rotated page or null when the page does not exists
     */
    public Rectangle getPageSizeWithRotation(PdfDictionary page) {

        if (page != null) {
            Rectangle rect = getPageSize(page);
            int rotation = getPageRotation(page);
            while (rotation > 0) {
                rect = rect.rotate();
                rotation -= 90;
            }
            return rect;
        }
        return null;
    }

    /**
     * Gets the page size, taking rotation into account. This is a <CODE>Rectangle</CODE> with the value of a an
     * arbitrary box and the /Rotate key.
     *
     * @param index   the page number. The first page is 1
     * @param boxName of the rotated box. Allowed names are: "crop", "trim", "art", "bleed" and "media".
     * @return a <CODE>Rectangle</CODE> or null if the page does not exist
     */
    public Rectangle getPageSizeWithRotation(int index, String boxName) {

        Rectangle rect = getBoxSize(index, boxName);
        PdfDictionary page = this.pageRefs.getPageNRelease(index);

        if (rect == null || page == null) {
            return null;
        }

        int rotation = getPageRotation(page);
        //except for the mediabox all other boxes can be null
        while (rotation > 0) {
            rect = rect.rotate();
            rotation -= 90;
        }
        return rect;
    }

    /**
     * Gets the page size without taking rotation into account. This is the value of the /MediaBox key.
     *
     * @param index the page number. The first page is 1
     * @return the page size
     */
    public Rectangle getPageSize(int index) {
        return getPageSize(pageRefs.getPageNRelease(index));
    }

    /**
     * Gets the page from a page dictionary
     *
     * @param page the page dictionary
     * @return the page
     */
    public Rectangle getPageSize(PdfDictionary page) {
        PdfArray mediaBox = page.getAsArray(PdfName.MEDIABOX);
        return getNormalizedRectangle(mediaBox);
    }

    /**
     * Gets the crop box without taking rotation into account. This is the value of the /CropBox key. The crop box is
     * the part of the document to be displayed or printed. It usually is the same as the media box but may be smaller.
     * If the page doesn't have a crop box the page size will be returned.
     *
     * @param index the page number. The first page is 1
     * @return the crop box
     */
    public Rectangle getCropBox(int index) {
        PdfDictionary page = pageRefs.getPageNRelease(index);
        PdfArray cropBox = (PdfArray) getPdfObjectRelease(page.get(PdfName.CROPBOX));
        if (cropBox == null) {
            return getPageSize(page);
        }
        return getNormalizedRectangle(cropBox);
    }

    /**
     * Gets the box size. Allowed names are: "crop", "trim", "art", "bleed" and "media".
     *
     * @param index   the page number. The first page is 1
     * @param boxName the box name
     * @return the box rectangle or null
     */
    public Rectangle getBoxSize(int index, String boxName) {
        PdfDictionary page = pageRefs.getPageNRelease(index);
        PdfArray box = null;
        switch (boxName) {
            case "trim":
                box = (PdfArray) getPdfObjectRelease(page.get(PdfName.TRIMBOX));
                break;
            case "art":
                box = (PdfArray) getPdfObjectRelease(page.get(PdfName.ARTBOX));
                break;
            case "bleed":
                box = (PdfArray) getPdfObjectRelease(page.get(PdfName.BLEEDBOX));
                break;
            case "crop":
                box = (PdfArray) getPdfObjectRelease(page.get(PdfName.CROPBOX));
                break;
            case "media":
                box = (PdfArray) getPdfObjectRelease(page.get(PdfName.MEDIABOX));
                break;
            default:
                break;
        }
        if (box == null) {
            return null;
        }
        return getNormalizedRectangle(box);
    }

    /**
     * Returns the content of the document information dictionary as a
     * <CODE>HashMap</CODE> of <CODE>String</CODE>.
     *
     * @return content of the document information dictionary
     */
    public Map<String, String> getInfo() {
        Map<String, String> map = new HashMap<>();
        PdfDictionary info = trailer.getAsDict(PdfName.INFO);
        if (info == null) {
            return map;
        }
        for (PdfName o : info.getKeys()) {
            PdfObject obj = getPdfObject(info.get(o));
            if (obj == null) {
                continue;
            }
            String value = obj.toString();
            switch (obj.type()) {
                case PdfObject.STRING: {
                    value = ((PdfString) obj).toUnicodeString();
                    break;
                }
                case PdfObject.NAME: {
                    value = PdfName.decodeName(value);
                    break;
                }
                default:
                    break;
            }
            map.put(PdfName.decodeName(o.toString()), value);
        }
        return map;
    }

    protected void readPdf() throws IOException, PDFFilterException {
        try {
            fileLength = tokens.getFile().length();
            pdfVersion = tokens.checkPdfHeader();
            try {
                readXref();
            } catch (Exception e) {
                try {
                    rebuilt = true;
                    rebuildXref();
                    lastXref = -1;
                } catch (Exception ne) {
                    throw new InvalidPdfException(MessageLocalization.getComposedMessage(
                            "rebuild.failed.1.original.message.2", ne.getMessage(),
                            e.getMessage()));
                }
            }
            try {
                readDocObj();
            } catch (Exception e) {
                if (e instanceof BadPasswordException) {
                    throw new BadPasswordException();
                }
                if (rebuilt || encryptionError) {
                    throw new InvalidPdfException(e.getMessage());
                }
                rebuilt = true;
                encrypted = false;
                rebuildXref();
                lastXref = -1;
                readDocObj();
            }

            strings.clear();
            readPages();

            removeUnusedObjects();
        } finally {
            try {
                tokens.close();
            } catch (Exception e) {
                // empty on purpose
            }
        }
    }

    protected void readPdfPartial() throws IOException {
        try {
            fileLength = tokens.getFile().length();
            pdfVersion = tokens.checkPdfHeader();
            readAndRebuildXRef();
            readDocObjPartial();
            readPages();
        } catch (IOException e) {
            try {
                tokens.close();
            } catch (Exception exc) {
                logger.info("Error closing tokens: " + exc.getMessage());
            }
            throw e;
        }
    }

    void readAndRebuildXRef() throws InvalidPdfException {
        try {
            readXref();
        } catch (Exception e) {
            tryRebuildXref(e);
        }
    }

    void tryRebuildXref(Exception e) throws InvalidPdfException {
        try {
            rebuilt = true;
            rebuildXref();
            lastXref = -1;
        } catch (Exception ne) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage(
                    "rebuild.failed.1.original.message.2", ne.getMessage(),
                    e.getMessage()));
        }
    }

    private boolean equalsArray(byte[] ar1, byte[] ar2, int size) {
        for (int k = 0; k < size; ++k) {
            if (ar1[k] != ar2[k]) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */

    private byte[] controlIfDocumentIDsAreNull(PdfArray documentIDs, byte[] documentID){
        String s;
        PdfObject o;

        if (documentIDs != null) {
            o = documentIDs.getPdfObject(0);
            strings.remove(o);
            s = o.toString();
            documentID = DocWriter.getISOBytes(s);
            if (documentIDs.size() > 1) {
                strings.remove(documentIDs.getPdfObject(1));
            }

            return documentID;
        }
        // just in case we have a broken producer

        return new byte[0];
    }

    private Object[] controlIfIsFilterPdfNameStandard(PdfObject filter,
            PdfDictionary enc) throws InvalidPdfException {
        String s;
        PdfObject o;
        byte[] uValue = null;
        byte[] oValue = null;

        if (filter.equals(PdfName.STANDARD)) {
            s = enc.get(PdfName.U).toString();
            strings.remove(enc.get(PdfName.U));
            uValue = DocWriter.getISOBytes(s);
            s = enc.get(PdfName.O).toString();
            strings.remove(enc.get(PdfName.O));
            oValue = DocWriter.getISOBytes(s);

            o = enc.get(PdfName.P);
            if (!o.isNumber()) {
                throw new InvalidPdfException(
                        MessageLocalization.getComposedMessage("illegal.p.value"));
            }
            pValue = ((PdfNumber) o).intValue();

            o = enc.get(PdfName.R);
            if (!o.isNumber()) {
                throw new InvalidPdfException(
                        MessageLocalization.getComposedMessage("illegal.r.value"));
            }
            rValue = ((PdfNumber) o).intValue();

            enc = (PdfDictionary) Objects.requireNonNull(switchRValue(enc))[0];
            cryptoMode = (int) Objects.requireNonNull(switchRValue(enc))[1];
            lengthValue = (int) Objects.requireNonNull(switchRValue(enc))[2];
        }

        return new Object[]{enc, cryptoMode, lengthValue, oValue, uValue};
    }

    private void readDecryptedDocObj() throws IOException {
        if (encrypted || trailer == null) {
            return;
        }
        PdfObject encDic = trailer.get(PdfName.ENCRYPT);
        if (encDic == null || encDic.toString().equals("null")) {
            return;
        }
        encryptionError = true;
        encrypted = true;

        PdfDictionary enc = (PdfDictionary) getPdfObject(encDic);
        byte[] documentID = getDocumentIDMethod();
        PdfObject filter = getPdfObjectRelease(enc.get(PdfName.FILTER));

        Object[] result = processFilter(enc, filter);
        enc = (PdfDictionary) result[0];
        cryptoMode = (int) result[1];
        lengthValue = (int) result[2];
        byte[] oValue = (byte[]) result[3];
        byte[] uValue = (byte[]) result[4];

        if (filter.equals(PdfName.PUBSEC)) {
            handlePublicSecurity(enc);
        } else if (filter.equals(PdfName.STANDARD)) {
            handleStandardEncryption(enc, oValue, uValue, documentID);
        }

        finalizeDecryption(encDic);
    }

    private byte[] getDocumentIDMethod() {
        PdfArray documentIDs = trailer.getAsArray(PdfName.ID);
        byte[] documentID = null;
        return controlIfDocumentIDsAreNull(documentIDs, documentID);
    }

    private Object[] processFilter(PdfDictionary enc, PdfObject filter) throws InvalidPdfException {
        cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40;
        lengthValue = 0;
        return controlIfIsFilterPdfNameStandard(filter, enc);
    }

    private void handlePublicSecurity(PdfDictionary enc) throws IOException {
        PdfArray recipients = getRecipients(enc);
        byte[] encryptionKey = getEnvelopedData(recipients);
        decrypt.setCryptoMode(PdfWriter.ENCRYPTION_AES_256_V3, lengthValue);
        decrypt.setupByEncryptionKey(encryptionKey, lengthValue);
        ownerPasswordUsed = true;
    }

    private void handleStandardEncryption(PdfDictionary enc, byte[] oValue,
            byte[] uValue, byte[] documentID) throws IOException {
        if (rValue < 6) {
            setupByPassword(oValue, uValue, documentID);
        } else {
            handleAdvancedEncryption(enc, oValue, uValue, documentID);
        }
    }

    private void finalizeDecryption(PdfObject encDic) {
        decryptAllStrings();
        if (encDic.isIndirect()) {
            cryptoRef = (PRIndirectReference) encDic;
            xrefObj.set(cryptoRef.getNumber(), null);
        }
        encryptionError = false;
    }

    private PdfArray getRecipients(PdfDictionary enc) throws InvalidPdfException {
        PdfObject o = enc.get(PdfName.V);
        if (!o.isNumber()) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("illegal.v.value"));
        }
        int vValue = ((PdfNumber) o).intValue();

        return switch (vValue) {
            case 1 -> (PdfArray) enc.get(PdfName.RECIPIENTS);
            case 2 -> {
                validateLengthValue(enc);
                yield (PdfArray) enc.get(PdfName.RECIPIENTS);
            }
            case 4 -> processDefaultCryptFilter(enc);
            default -> throw new UnsupportedPdfException(
                    MessageLocalization.getComposedMessage("unknown.encryption.type.v.eq.1", vValue));
        };
    }

    private PdfArray processDefaultCryptFilter(PdfDictionary enc) throws InvalidPdfException {
        PdfDictionary dic = (PdfDictionary) enc.get(PdfName.CF);
        if (dic == null) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("cf.not.found.encryption"));
        }
        dic = (PdfDictionary) dic.get(PdfName.DEFAULTCRYPTFILTER);
        if (dic == null) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("defaultcryptfilter.not.found.encryption"));
        }

        if (PdfName.V2.equals(dic.get(PdfName.CFM))) {
            lengthValue = 128;
        } else if (PdfName.AESV2.equals(dic.get(PdfName.CFM))) {
            lengthValue = 128;
        } else {
            throw new UnsupportedPdfException(MessageLocalization.getComposedMessage("no.compatible.encryption.found"));
        }

        PdfObject em = dic.get(PdfName.ENCRYPTMETADATA);
        if (em != null && em.toString().equals(FALSE_CONST)) {
            cryptoMode |= PdfWriter.DO_NOT_ENCRYPT_METADATA;
        }

        return (PdfArray) dic.get(PdfName.RECIPIENTS);
    }

    private byte[] getEnvelopedData(PdfArray recipients) throws IOException {
        BouncyCastleHelper.checkCertificateEncodingOrThrowException(certificate);
        byte[] envelopedData = BouncyCastleHelper.getEnvelopedData(recipients, strings, certificate, certificateKey, certificateKeyProvider);

        if (envelopedData == null) {
            throw new UnsupportedPdfException(MessageLocalization.getComposedMessage("bad.certificate.and.key"));
        }

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(envelopedData, 0, 20);
            for (int i = 0; i < recipients.size(); i++) {
                byte[] encodedRecipient = recipients.getPdfObject(i).getBytes();
                md.update(encodedRecipient);
            }
            if ((cryptoMode & PdfWriter.DO_NOT_ENCRYPT_METADATA) != 0) {
                md.update(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
            }
            return md.digest();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void setupByPassword(byte[] oValue, byte[] uValue, byte[] documentID) throws IOException {
        decrypt.setupByOwnerPassword(documentID, password, oValue, pValue);
        if (!equalsArray(uValue, decrypt.userKey, (rValue == 3 || rValue == 4) ? 16 : 32)) {
            decrypt.setupByUserPassword(documentID, password, uValue, pValue);
            if (!equalsArray(uValue, decrypt.userKey, (rValue == 3 || rValue == 4) ? 16 : 32)) {
                throw new BadPasswordException();
            }
        } else {
            ownerPasswordUsed = true;
        }
    }

    private void handleAdvancedEncryption(PdfDictionary enc, byte[] oValue, byte[] uValue, byte[] documentID) throws IOException {
        strings.remove(enc.get(PdfName.UE));
        strings.remove(enc.get(PdfName.OE));
        strings.remove(enc.get(PdfName.PERMS));

        byte[] permsValue = DocWriter.getISOBytes(enc.get(PdfName.PERMS).toString());

        if (uValue != null && uValue.length > 48) {
            uValue = Arrays.copyOf(uValue, 48);
        }

        try {
            byte[] hashAlg2B = decrypt.hashAlg2B(password, Arrays.copyOfRange(oValue, 32, 40), uValue);
            if (equalsArray(hashAlg2B, oValue, 32)) {
                decrypt.setupByOwnerPassword(documentID, password, oValue, pValue);
                if (decrypt.decryptAndCheckPerms(permsValue)) {
                    ownerPasswordUsed = true;
                }
            }

            if (!ownerPasswordUsed) {
                hashAlg2B = decrypt.hashAlg2B(password, Arrays.copyOfRange(uValue, 32, 40), null);
                if (!equalsArray(hashAlg2B, uValue, 32)) {
                    throw new BadPasswordException();
                }
                decrypt.setupByUserPassword(documentID, password, uValue, pValue);
                if (!decrypt.decryptAndCheckPerms(permsValue)) {
                    throw new BadPasswordException();
                }
            }
            pValue = decrypt.permissions;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void decryptAllStrings() {
        for (PdfObject string : strings) {
            PdfString str = (PdfString) string;
            str.decrypt(this);
        }
    }

    private Object[] switchRValue(PdfDictionary enc) throws InvalidPdfException {
        switch (rValue) {
            case 2:
                cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40;
                break;
            case 3:
                lengthValue = validateLengthValue(enc);
                cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128;
                break;
            case 4:
                cryptoMode = processCFDictionary(enc);
                break;
            case 6:
                cryptoMode = PdfWriter.ENCRYPTION_AES_256_V3;
                validateEncryptMetadata(enc);
                break;
            default:
                throwUnsupportedEncryptionType();
                return new Object[0]; // per evitare avvisi di non ritorno
        }
        return new Object[]{enc, cryptoMode, lengthValue};
    }

    private int validateLengthValue(PdfDictionary enc) throws InvalidPdfException {
        PdfObject o = enc.get(PdfName.PDF_NAME_LENGTH);
        if (!o.isNumber()) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage(ILLEGAL_LENGTH_VALUE_CONST));
        }
        lengthValue = ((PdfNumber) o).intValue();
        if (lengthValue > 128 || lengthValue < 40 || lengthValue % 8 != 0) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage(ILLEGAL_LENGTH_VALUE_CONST));
        }
        return lengthValue;
    }

    private int processCFDictionary(PdfDictionary enc) throws InvalidPdfException {
        PdfDictionary dic = (PdfDictionary) enc.get(PdfName.CF);
        if (dic == null) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("cf.not.found.encryption"));
        }
        dic = (PdfDictionary) dic.get(PdfName.STDCF);
        if (dic == null) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("stdcf.not.found.encryption"));
        }
        if (PdfName.V2.equals(dic.get(PdfName.CFM))) {
            return PdfWriter.STANDARD_ENCRYPTION_128;
        } else if (PdfName.AESV2.equals(dic.get(PdfName.CFM))) {
            return PdfWriter.ENCRYPTION_AES_128;
        } else {
            throw new UnsupportedPdfException(MessageLocalization.getComposedMessage("no.compatible.encryption.found"));
        }
    }

    private void validateEncryptMetadata(PdfDictionary enc) {
        PdfObject em = enc.get(PdfName.ENCRYPTMETADATA);
        if (em != null && em.toString().equals(FALSE_CONST)) {
            cryptoMode |= PdfWriter.DO_NOT_ENCRYPT_METADATA;
        }
    }

    private void throwUnsupportedEncryptionType() throws UnsupportedPdfException {
        throw new UnsupportedPdfException(MessageLocalization.getComposedMessage("unknown.encryption.type.r.eq.1", rValue));
    }

    /**
     * @param idx index
     * @return a PdfObject
     */
    public PdfObject getPdfObjectRelease(int idx) {
        PdfObject obj = getPdfObject(idx);
        releaseLastXrefPartial();
        return obj;
    }

    /**
     * @param idx index
     * @return aPdfObject
     */
    public PdfObject getPdfObject(int idx) {
        try {
            lastXrefPartial = -1;
            if (idx < 0 || idx >= xrefObj.size()) {
                return null;
            }
            PdfObject obj = xrefObj.get(idx);
            if (!partial || obj != null) {
                return obj;
            }
            if (idx * 2 >= xref.length) {
                return null;
            }
            obj = readSingleObject(idx);
            lastXrefPartial = -1;
            if (obj != null) {
                lastXrefPartial = idx;
            }
            return obj;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     *
     */
    public void resetLastXrefPartial() {
        lastXrefPartial = -1;
    }

    /**
     *
     */
    public void releaseLastXrefPartial() {
        if (partial && lastXrefPartial != -1) {
            xrefObj.set(lastXrefPartial, null);
            lastXrefPartial = -1;
        }
    }

    private void setXrefPartialObject(int idx, PdfObject obj) {
        if (!partial || idx < 0) {
            return;
        }
        xrefObj.set(idx, obj);
    }

    /**
     * @param obj an object of {@link PdfObject}
     * @return an indirect reference
     */
    public PRIndirectReference addPdfObject(PdfObject obj) {
        xrefObj.add(obj);
        return new PRIndirectReference(this, xrefObj.size() - 1);
    }

    protected void readPages(){
        catalog = trailer.getAsDict(PdfName.ROOT);
        rootPages = catalog.getAsDict(PdfName.PAGES);
        pageRefs = new PageRefs(this);
    }

    protected void readDocObjPartial() throws IOException {
        xrefObj = new ArrayList<>(xref.length / 2);
        xrefObj.addAll(Collections.nCopies(xref.length / 2, null));
        readDecryptedDocObj();
        if (objStmToOffset != null) {
            int[] keys = objStmToOffset.getKeys();
            for (int n : keys) {
                objStmToOffset.put(n, xref[n * 2]);
                xref[n * 2] = -1;
            }
        }
    }

    protected PdfObject readSingleObject(int k) throws IOException, PDFFilterException {
        strings.clear();
        int k2 = k * 2;
        int pos = xref[k2];
        if (pos < 0) {
            return null;
        }
        if (xref[k2 + 1] > 0) {
            pos = objStmToOffset.get(xref[k2 + 1]);
        }
        if (pos == 0) {
            return null;
        }
        tokens.seek(pos);
        tokens.nextValidToken();
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            tokens.throwError(MessageLocalization
                    .getComposedMessage("invalid.object.number"));
        }
        objNum = tokens.intValue();
        tokens.nextValidToken();
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            tokens.throwError(MessageLocalization
                    .getComposedMessage("invalid.generation.number"));
        }
        objGen = tokens.intValue();
        tokens.nextValidToken();
        if (!tokens.getStringValue().equals("obj")) {
            tokens.throwError(MessageLocalization
                    .getComposedMessage("token.obj.expected"));
        }
        PdfObject obj;
        try {
            obj = readPRObject();
            for (PdfObject string : strings) {
                PdfString str = (PdfString) string;
                str.decrypt(this);
            }
            if (obj.isStream()) {
                checkPRStreamLength((PRStream) obj);
            }
        } catch (Exception e) {
            obj = null;
        }
        if (xref[k2 + 1] > 0) {
            obj = readOneObjStm((PRStream) obj, xref[k2]);
        }
        xrefObj.set(k, obj);
        return obj;
    }

    protected PdfObject readOneObjStm(PRStream stream, int idx)
            throws IOException, PDFFilterException {
        int first = stream.getAsNumber(PdfName.FIRST).intValue();
        byte[] b = getStreamBytes(stream, tokens.getFile());
        PRTokeniser saveTokens = tokens;
        tokens = new PRTokeniser(b);
        try {
            int address = 0;
            boolean ok = true;
            ++idx;
            for (int k = 0; k < idx; ++k) {
                ok = tokens.nextToken();
                if (!ok) {
                    break;
                }
                if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                    ok = false;
                    break;
                }
                ok = tokens.nextToken();
                if (!ok) {
                    break;
                }
                if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                    ok = false;
                    break;
                }
                address = tokens.intValue() + first;
            }
            if (!ok) {
                throw new InvalidPdfException(
                        MessageLocalization.getComposedMessage("error.reading.objstm"));
            }
            tokens.seek(address);
            return readPRObject();
        } finally {
            tokens = saveTokens;
        }
    }

    /**
     * @return the percentage of the cross reference table that has been read
     */
    public double dumpPerc() {
        int total = 0;
        for (PdfObject aXrefObj : xrefObj) {
            if (aXrefObj != null) {
                ++total;
            }
        }
        return (total * 100.0 / xrefObj.size());
    }

    protected void readDocObj() throws IOException, PDFFilterException {
        List<PdfObject> streams = new ArrayList<>();
        xrefObj = new ArrayList<>(xref.length / 2);
        xrefObj.addAll(Collections.nCopies(xref.length / 2, null));
        for (int k = 2; k < xref.length; k += 2) {
            int pos = xref[k];
            if (pos <= 0 || ((xref.length > k + 1) && (xref[k + 1] > 0))) {
                continue;
            }
            tokens.seek(pos);
            tokens.nextValidToken();
            if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("invalid.object.number"));
            }
            objNum = tokens.intValue();
            tokens.nextValidToken();
            if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("invalid.generation.number"));
            }
            objGen = tokens.intValue();
            tokens.nextValidToken();
            if (!tokens.getStringValue().equals("obj")) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("token.obj.expected"));
            }
            PdfObject obj;
            try {
                obj = readPRObject();
                if (obj.isStream()) {
                    streams.add(obj);
                }
            } catch (Exception e) {
                obj = null;
            }
            xrefObj.set(k / 2, obj);
        }
        for (PdfObject stream : streams) {
            checkPRStreamLength((PRStream) stream);
        }
        readDecryptedDocObj();
        if (objStmMark != null) {
            for (Entry<?,?> o : objStmMark.entrySet()) {
                int n = (Integer) o.getKey();
                IntHashtable h = (IntHashtable) o.getValue();
                readObjStm((PRStream) xrefObj.get(n), h);
                xrefObj.set(n, null);
            }
            objStmMark = null;
        }
        xref = null;
    }

    private void checkPRStreamLength(PRStream stream) throws IOException {
        int fileTokensLength = tokens.length();
        int start = stream.getOffset();
        boolean calc = false;
        int streamLength = 0;
        PdfObject obj = getPdfObjectRelease(stream.get(PdfName.PDF_NAME_LENGTH));
        if (obj != null && obj.type() == PdfObject.NUMBER) {
            streamLength = ((PdfNumber) obj).intValue();
            if (streamLength + start > fileTokensLength - 20) {
                calc = true;
            } else {
                tokens.seek(start + streamLength);
                String line = tokens.readString(20);
                if (!line.startsWith("\nendstream")
                        && !line.startsWith("\r\nendstream")
                        && !line.startsWith("\rendstream") && !line.startsWith(ENDSTREAM_CONST)) {
                    calc = true;
                }
            }
        } else {
            calc = true;
        }
        if (calc) {
            byte[] tline = new byte[16];
            tokens.seek(start);
            while (true) {
                int pos = tokens.getFilePointer();
                if (tokens.readLineSegment(tline)) {
                    break;
                }
                if (equalsn(tline, endstream)) {
                    streamLength = pos - start;
                    break;
                }
                if (equalsn(tline, endobj)) {
                    tokens.seek(pos - 16);
                    String s = tokens.readString(16);
                    int index = s.indexOf(ENDSTREAM_CONST);
                    if (index >= 0) {
                        pos = pos - 16 + index;
                    }
                    streamLength = pos - start;
                    break;
                }
            }
        }
        stream.setLength(streamLength);
    }

    protected void readObjStm(PRStream stream, IntHashtable map)
            throws IOException, PDFFilterException {
        int first = stream.getAsNumber(PdfName.FIRST).intValue();
        int n = stream.getAsNumber(PdfName.N).intValue();
        byte[] b = getStreamBytes(stream, tokens.getFile());
        PRTokeniser saveTokens = tokens;
        tokens = new PRTokeniser(b);
        try {
            int[] address = new int[n];
            int[] objNumber = new int[n];
            boolean ok = true;
            for (int k = 0; k < n; ++k) {
                ok = tokens.nextToken();
                if (!ok) {
                    break;
                }
                if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                    ok = false;
                    break;
                }
                objNumber[k] = tokens.intValue();
                ok = tokens.nextToken();
                if (!ok) {
                    break;
                }
                if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
                    ok = false;
                    break;
                }
                address[k] = tokens.intValue() + first;
            }
            if (!ok) {
                throw new InvalidPdfException(
                        MessageLocalization.getComposedMessage("error.reading.objstm"));
            }
            for (int k = 0; k < n; ++k) {
                if (map.containsKey(k)) {
                    tokens.seek(address[k]);
                    PdfObject obj = readPRObject();
                    xrefObj.set(objNumber[k], obj);
                }
            }
        } finally {
            tokens = saveTokens;
        }
    }

    private void ensureXrefSize(int size) {
        if (size == 0) {
            return;
        }
        if (xref == null) {
            xref = new int[size];
        } else {
            if (xref.length < size) {
                int[] xref2 = new int[size];
                System.arraycopy(xref, 0, xref2, 0, xref.length);
                xref = xref2;
            }
        }
    }

    protected void readXref() throws IOException, PDFFilterException {
        hybridXref = false;
        newXrefType = false;
        tokens.seek(tokens.getStartxref());
        tokens.nextToken();
        if (!tokens.getStringValue().equals("startxref")) {
            throw new InvalidPdfException(
                    MessageLocalization.getComposedMessage("startxref.not.found"));
        }
        tokens.nextToken();
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            throw new InvalidPdfException(
                    MessageLocalization
                            .getComposedMessage("startxref.is.not.followed.by.a.number"));
        }
        int startxref = tokens.intValue();
        lastXref = startxref;
        eofPos = tokens.getFilePointer();
        try {
            if (readXRefStream(startxref)) {
                newXrefType = true;
                return;
            }
        } catch (Exception ignored) {
            //da vedere come effettuare il log
        }
        xref = null;
        tokens.seek(startxref);
        trailer = readXrefSection();
        PdfDictionary trailer2 = trailer;
        while (true) {
            PdfNumber prev = (PdfNumber) trailer2.get(PdfName.PREV);
            if (prev == null) {
                break;
            }
            if (prev.intValue() == startxref) {
                throw new InvalidPdfException(
                        MessageLocalization
                                .getComposedMessage("xref.infinite.loop"));
            }
            tokens.seek(prev.intValue());
            trailer2 = readXrefSection();
        }
    }

    protected PdfDictionary readXrefSection() throws IOException, PDFFilterException {
        validateXrefHeader();

        while (true) {
            tokens.nextValidToken();
            if (tokens.getStringValue().equals("trailer")) {
                break;
            }
            processXrefSubsection();
        }

        PdfDictionary trailerRead = readTrailer();
        handleXrefStm(trailerRead);

        return trailer;
    }

    private void validateXrefHeader() throws IOException {
        tokens.nextValidToken();
        if (!tokens.getStringValue().equals("xref")) {
            tokens.throwError(MessageLocalization.getComposedMessage("xref.subsection.not.found"));
        }
    }

    private void processXrefSubsection() throws IOException {
        int start = readSubsectionStart();
        int end = readSubsectionEnd(start);

        if (start == 1) {
            start = (int) fixIncorrectStartNumber(start, end)[0];
            end = (int) fixIncorrectStartNumber(start, end)[1];
        }

        ensureXrefSize(end * 2);
        processXrefEntries(start, end);
    }

    private int readSubsectionStart() throws IOException {
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            tokens.throwError(MessageLocalization.getComposedMessage("object.number.of.the.first.object.in.this.xref.subsection.not.found"));
        }
        return tokens.intValue();
    }

    private int readSubsectionEnd(int start) throws IOException {
        tokens.nextValidToken();
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            tokens.throwError(MessageLocalization.getComposedMessage("number.of.entries.in.this.xref.subsection.not.found"));
        }
        return tokens.intValue() + start;
    }

    private Object[] fixIncorrectStartNumber(int start, int end) throws IOException {
        int back = tokens.getFilePointer();
        tokens.nextValidToken();
        int pos = tokens.intValue();
        tokens.nextValidToken();
        int gen = tokens.intValue();
        if (pos == 0 && gen == PdfWriter.GENERATION_MAX) {
            --start;
            --end;
        }
        tokens.seek(back);
        return new Object[]{start, end};
    }

    private void processXrefEntries(int start, int end) throws IOException {
        for (int k = start; k < end; ++k) {
            tokens.nextValidToken();
            int pos = tokens.intValue();
            tokens.nextValidToken();  // Skip generation number
            tokens.nextValidToken();
            updateXrefEntry(k, pos);
        }
    }

    private void updateXrefEntry(int k, int pos) throws IOException {
        int p = k * 2;
        if (tokens.getStringValue().equals("n")) {
            if (xref[p] == 0 && xref[p + 1] == 0) {
                xref[p] = pos;
            }
        } else if (tokens.getStringValue().equals("f")) {
            if (xref[p] == 0 && xref[p + 1] == 0) {
                xref[p] = -1;
            }
        } else {
            tokens.throwError(MessageLocalization.getComposedMessage("invalid.cross.reference.entry.in.this.xref.subsection"));
        }
    }

    private PdfDictionary readTrailer() throws IOException {
        PdfDictionary trailerRead = (PdfDictionary) readPRObject();
        PdfNumber xrefSize = (PdfNumber) trailerRead.get(PdfName.SIZE);
        ensureXrefSize(xrefSize.intValue() * 2);
        return trailerRead;
    }

    private void handleXrefStm(PdfDictionary trailerRead) throws IOException, PDFFilterException {
        PdfObject xrs = trailerRead.get(PdfName.XREFSTM);
        if (xrs != null && xrs.isNumber()) {
            int loc = ((PdfNumber) xrs).intValue();
            try {
                readXRefStream(loc);
                newXrefType = true;
                hybridXref = true;
            } catch (IOException | PDFFilterException e) {
                xref = null;
                throw e;
            }
        }
    }

    protected boolean readXRefStream(int ptr) throws IOException, PDFFilterException {
        tokens.seek(ptr);
        if (!validateInitialToken()) {
            return false;
        }

        PdfObject object = readPRObject();
        PRStream stm = extractStreamIfValid(object);
        if (stm == null) {
            return false;
        }

        if (trailer == null) {
            initializeTrailer(stm);
        }

        int size = ((PdfNumber) stm.get(PdfName.SIZE)).intValue();
        PdfArray index = getIndex(stm, size);
        PdfArray w = (PdfArray) stm.get(PdfName.W);
        int prev = getPrev(stm);

        ensureXrefSize(size * 2);
        initializeObjStmCollections();

        byte[] streamBytes = getStreamBytes(stm, tokens.getFile());
        processXrefEntries(index, w, streamBytes);

        return handlePrevXrefStream(prev);
    }

    private boolean validateInitialToken() throws IOException {
        if (!tokens.nextToken() || tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
            return false;
        }
        return tokens.nextToken() && tokens.getTokenType() == PRTokeniser.TK_NUMBER
                && tokens.getStringValue().equals("obj");
    }

    private PRStream extractStreamIfValid(PdfObject object) {
        if (object.isStream()) {
            PRStream stm = (PRStream) object;
            if (PdfName.XREF.equals(stm.get(PdfName.TYPE_CONST))) {
                return stm;
            }
        }
        return null;
    }

    private void initializeTrailer(PRStream stm) {
        trailer = new PdfDictionary();
        trailer.putAll(stm);
    }

    private PdfArray getIndex(PRStream stm, int size) {
        PdfObject obj = stm.get(PdfName.INDEX);
        if (obj == null) {
            PdfArray index = new PdfArray();
            index.add(new int[]{0, size});
            return index;
        }
        return (PdfArray) obj;
    }

    private int getPrev(PRStream stm) {
        PdfObject obj = stm.get(PdfName.PREV);
        if (obj != null) {
            return ((PdfNumber) obj).intValue();
        }
        return -1;
    }

    private void initializeObjStmCollections() {
        if (objStmMark == null && !partial) {
            objStmMark = new HashMap<>();
        }
        if (objStmToOffset == null && partial) {
            objStmToOffset = new IntHashtable();
        }
    }

    private void processXrefEntries(PdfArray index, PdfArray w, byte[] streamBytes) {
        int[] wc = new int[3];
        for (int k = 0; k < 3; ++k) {
            wc[k] = w.getAsNumber(k).intValue();
        }

        int bptr = 0;
        for (int idx = 0; idx < index.size(); idx += 2) {
            int start = index.getAsNumber(idx).intValue();
            int length = index.getAsNumber(idx + 1).intValue();
            ensureXrefSize((start + length) * 2);
            processXrefEntry(streamBytes, wc, bptr, start, length);
        }
    }

    private void processXrefEntry(byte[] streamBytes, int[] wc, int bptr, int start, int length) {
        while (length-- > 0) {
            int type = readType(streamBytes, wc[0], bptr);
            int field2 = readField(streamBytes, wc[1], bptr);
            int field3 = readField(streamBytes, wc[2], bptr);

            int base = start * 2;
            updateXrefEntry(base, type, field2, field3);

            ++start;
        }
    }

    private int readType(byte[] streamBytes, int fieldSize, int bptr) {
        int type = 1;
        if (fieldSize > 0) {
            type = 0;
            for (int k = 0; k < fieldSize; ++k) {
                type = (type << 8) + (streamBytes[bptr++] & 0xff);
            }
        }
        return type;
    }

    private int readField(byte[] streamBytes, int fieldSize, int bptr) {
        int field = 0;
        for (int k = 0; k < fieldSize; ++k) {
            field = (field << 8) + (streamBytes[bptr++] & 0xff);
        }
        return field;
    }

    private void updateXrefEntry(int base, int type, int field2, int field3) {
        if (xref[base] == 0 && xref[base + 1] == 0) {
            switch (type) {
                case 0:
                    xref[base] = -1;
                    break;
                case 1:
                    xref[base] = field2;
                    break;
                case 2:
                    xref[base] = field3;
                    xref[base + 1] = field2;
                    updateObjStmCollections(field2, field3);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateObjStmCollections(int field2, int field3) {
        if (partial) {
            objStmToOffset.put(field2, 0);
        } else {
            Integer on = field2;
            IntHashtable seq = objStmMark.get(on);
            if (seq == null) {
                seq = new IntHashtable();
                seq.put(field3, 1);
                objStmMark.put(on, seq);
            } else {
                seq.put(field3, 1);
            }
        }
    }

    private boolean handlePrevXrefStream(int prev) throws IOException, PDFFilterException {
        if (prev == -1) {
            return true;
        }
        return readXRefStream(prev);
    }

    protected void rebuildXref() throws IOException {
        hybridXref = false;
        newXrefType = false;
        tokens.seek(0);
        trailer = null;

        int[][] xr = initializeXrefArray();
        int top = processPdfFile(xr);

        if (trailer == null) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("trailer.not.found"));
        }

        buildXrefTable(xr, top);
    }

    private int[][] initializeXrefArray() {
        return new int[1024][];
    }

    private int processPdfFile(int[][] xr) throws IOException {
        byte[] line = new byte[64];
        int top = 0;

        while (true) {
            int pos = tokens.getFilePointer();
            if (tokens.readLineSegment(line)) {
                break;
            }
            if (isTrailer(line)) {
                handleTrailer(pos, line);
            } else if (isObjectStart(line)) {
                top = handleObjectStart(xr, line, pos, top);
            }
        }

        return top;
    }

    private boolean isTrailer(byte[] line) {
        return line[0] == 't';
    }

    private void handleTrailer(int pos, byte[] line) throws IOException {
        if (!PdfEncodings.convertToString(line, null).startsWith("trailer")) {
            return;
        }
        tokens.seek(pos);
        tokens.nextToken();
        pos = tokens.getFilePointer();

        try {
            PdfDictionary dic = (PdfDictionary) readPRObject();
            if (dic.get(PdfName.ROOT) != null) {
                trailer = dic;
            } else {
                tokens.seek(pos);
            }
        } catch (Exception e) {
            tokens.seek(pos);
        }
    }

    private boolean isObjectStart(byte[] line) {
        return line[0] >= '0' && line[0] <= '9';
    }

    private int handleObjectStart(int[][] xr, byte[] line, int pos, int top) {
        int[] obj = PRTokeniser.checkObjectStart(line);
        if (obj == null) {
            return top;
        }

        int num = obj[0];
        int gen = obj[1];

        if (num >= xr.length) {
            xr = expandXrefArray(xr, num);
        }

        if (num >= top) {
            top = num + 1;
        }

        if (xr[num] == null || gen >= xr[num][1]) {
            obj[0] = pos;
            xr[num] = obj;
        }

        return top;
    }

    private int[][] expandXrefArray(int[][] xr, int num) {
        int newLength = num * 2;
        int[][] xr2 = new int[newLength][];
        System.arraycopy(xr, 0, xr2, 0, xr.length);
        return xr2;
    }

    private void buildXrefTable(int[][] xr, int top) {
        xref = new int[top * 2];
        for (int k = 0; k < top; ++k) {
            int[] obj = xr[k];
            if (obj != null) {
                xref[k * 2] = obj[0];
            }
        }
    }

    protected PdfDictionary readDictionary() throws IOException {
        PdfDictionary dic = new PdfDictionary();
        while (true) {
            tokens.nextValidToken();
            if (tokens.getTokenType() == PRTokeniser.TK_END_DIC) {
                break;
            }
            if (tokens.getTokenType() != PRTokeniser.TK_NAME) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("dictionary.key.is.not.a.name"));
            }
            PdfName name = new PdfName(tokens.getStringValue(), false);
            PdfObject obj = readPRObject();
            int type = obj.type();
            if (-type == PRTokeniser.TK_END_DIC) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("unexpected.gt.gt"));
            }
            if (-type == PRTokeniser.TK_END_ARRAY) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("unexpected.close.bracket"));
            }
            dic.put(name, obj);
        }
        return dic;
    }

    protected PdfArray readArray() throws IOException {
        PdfArray array = new PdfArray();
        while (true) {
            PdfObject obj = readPRObject();
            int type = obj.type();
            if (-type == PRTokeniser.TK_END_ARRAY) {
                break;
            }
            if (-type == PRTokeniser.TK_END_DIC) {
                tokens.throwError(MessageLocalization
                        .getComposedMessage("unexpected.gt.gt"));
            }
            array.add(obj);
        }
        return array;
    }

    protected PdfObject readPRObject() throws IOException {
        tokens.nextValidToken();
        int type = tokens.getTokenType();

        return switch (type) {
            case PRTokeniser.TK_START_DIC -> handleDictionary();
            case PRTokeniser.TK_START_ARRAY -> handleArray();
            case PRTokeniser.TK_NUMBER -> handleNumber();
            case PRTokeniser.TK_STRING -> handleString();
            case PRTokeniser.TK_NAME -> handleName();
            case PRTokeniser.TK_REF -> handleReference();
            case PRTokeniser.TK_ENDOFFILE ->
                    throw new IOException(MessageLocalization.getComposedMessage("unexpected.end.of.file"));
            default -> handleDefault();
        };
    }

    private PdfObject handleDictionary() throws IOException {
        ++readDepth;
        PdfDictionary dic = readDictionary();
        --readDepth;
        int pos = tokens.getFilePointer();
        boolean hasNext = skipCommentsAndCheckNextToken();

        if (hasNext && tokens.getStringValue().equals("stream")) {
            processStream(dic);
        } else {
            tokens.seek(pos);
            return dic;
        }
        return null; // Questo è qui per conformarsi alla firma del metodo
    }

    private boolean skipCommentsAndCheckNextToken() throws IOException {
        boolean hasNext;
        do {
            hasNext = tokens.nextToken();
        } while (hasNext && tokens.getTokenType() == PRTokeniser.TK_COMMENT);
        return hasNext;
    }

    private PdfObject processStream(PdfDictionary dic) throws IOException {
        int ch;
        do {
            ch = tokens.read();
        } while (isWhitespace(ch));

        if (ch != '\n') {
            ch = tokens.read();
        }
        if (ch != '\n') {
            tokens.backOnePosition(ch);
        }

        PRStream stream = new PRStream(this, tokens.getFilePointer());
        stream.putAll(dic);
        stream.setObjNum(objNum, objGen);

        return stream;
    }

    private boolean isWhitespace(int ch) {
        return ch == 32 || ch == 9 || ch == 0 || ch == 12;
    }

    private PdfArray handleArray() throws IOException {
        ++readDepth;
        PdfArray arr = readArray();
        --readDepth;
        return arr;
    }

    private PdfNumber handleNumber() {
        return new PdfNumber(tokens.getStringValue());
    }

    private PdfString handleString() {
        PdfString str = new PdfString(tokens.getStringValue(), null).setHexWriting(tokens.isHexString());
        str.setObjNum(objNum, objGen);
        if (strings != null) {
            strings.add(str);
        }
        return str;
    }

    private PdfName handleName() {
        PdfName cachedName = PdfName.staticNames.get(tokens.getStringValue());
        if (readDepth > 0 && cachedName != null) {
            return cachedName;
        } else {
            return new PdfName(tokens.getStringValue(), false);
        }
    }

    private PRIndirectReference handleReference() {
        int num = tokens.getReference();
        return new PRIndirectReference(this, num, tokens.getGeneration());
    }

    private PdfObject handleDefault() {
        String sv = tokens.getStringValue();
        return switch (sv) {
            case "null" -> (readDepth == 0) ? new PdfNull() : PdfNull.PDFNULL;
            case "true" -> (readDepth == 0) ? new PdfBoolean(true) : PdfBoolean.PDFTRUE;
            case FALSE_CONST -> (readDepth == 0) ? new PdfBoolean(false) : PdfBoolean.PDFFALSE;
            default -> new PdfLiteral(-tokens.getTokenType(), sv);
        };
    }

    /**
     * Checks if the document had errors and was rebuilt.
     *
     * @return true if rebuilt.
     */
    public boolean isRebuilt() {
        return this.rebuilt;
    }

    /**
     * Gets the dictionary that represents a page.
     *
     * @param pageNum the page number. 1 is the first
     * @return the page dictionary or null when the page does not exist
     */
    public PdfDictionary getPageN(int pageNum) {
        PdfDictionary dic = pageRefs.getPageN(pageNum);
        if (dic == null) {
            return null;
        }
        if (appendable) {
            dic.setIndRef(pageRefs.getPageOrigRef(pageNum));
        }
        return dic;
    }

    /**
     * @param pageNum page number
     * @return a Dictionary object
     */
    public PdfDictionary getPageNRelease(int pageNum) {
        PdfDictionary dic = getPageN(pageNum);
        pageRefs.releasePage(pageNum);
        return dic;
    }

    /**
     * @param pageNum page number
     */
    public void releasePage(int pageNum) {
        pageRefs.releasePage(pageNum);
    }

    /**
     *
     */
    public void resetReleasePage() {
        pageRefs.resetReleasePage();
    }

    /**
     * Gets the page reference to this page.
     *
     * @param pageNum the page number. 1 is the first
     * @return the page reference
     */
    public PRIndirectReference getPageOrigRef(int pageNum) {
        return pageRefs.getPageOrigRef(pageNum);
    }

    /**
     * Gets the contents of the page.
     *
     * @param pageNum the page number. 1 is the first
     * @param file    the location of the PDF document
     * @return the content
     * @throws IOException on error
     */
    public byte[] getPageContent(int pageNum, RandomAccessFileOrArray file)
            throws IOException, PDFFilterException {
        PdfDictionary page = getPageNRelease(pageNum);
        if (page == null) {
            return new byte[]{};
        }
        PdfObject contents = getPdfObjectRelease(page.get(PdfName.CONTENTS));
        if (contents == null) {
            return new byte[0];
        }
        ByteArrayOutputStream bout;
        if (contents.isStream()) {
            return getStreamBytes((PRStream) contents, file);
        } else if (contents.isArray()) {
            PdfArray array = (PdfArray) contents;
            bout = new ByteArrayOutputStream();
            for (int k = 0; k < array.size(); ++k) {
                PdfObject item = getPdfObjectRelease(array.getPdfObject(k));
                if (item == null || !item.isStream()) {
                    continue;
                }
                byte[] b = getStreamBytes((PRStream) item, file);
                bout.write(b);
                if (k != array.size() - 1) {
                    bout.write('\n');
                }
            }
            return bout.toByteArray();
        } else {
            return new byte[0];
        }
    }

    /**
     * Gets the contents of the page.
     *
     * @param pageNum the page number. 1 is the first
     * @return the content
     * @throws IOException on error
     */
    public byte[] getPageContent(int pageNum) throws IOException, PDFFilterException {
        RandomAccessFileOrArray rf = getSafeFile();
        try {
            rf.reOpen();
            return getPageContent(pageNum, rf);
        } catch (PDFFilterException e) {
            throw new PDFFilterException(e.getMessage());
        } finally {
            try {
                rf.close();
            } catch (IOException exc) {
                logger.info("Random Access File or Array ERROR: " + exc.getMessage());
            }
        }
    }

    protected void killXref(PdfObject obj) {
        if (obj == null) {
            return;
        }
        if ((obj instanceof PdfIndirectReference) && !obj.isIndirect()) {
            return;
        }
        switch (obj.type()) {
            case PdfObject.INDIRECT: {
                int xr = ((PRIndirectReference) obj).getNumber();
                obj = xrefObj.get(xr);
                xrefObj.set(xr, null);
                freeXref = xr;
                killXref(obj);
                break;
            }
            case PdfObject.ARRAY: {
                PdfArray t = (PdfArray) obj;
                for (int i = 0; i < t.size(); ++i) {
                    killXref(t.getPdfObject(i));
                }
                break;
            }
            case PdfObject.STREAM, PdfObject.DICTIONARY: {
                PdfDictionary dic = (PdfDictionary) obj;
                for (PdfName o : dic.getKeys()) {
                    killXref(dic.get(o));
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * Sets the contents of the page.
     *
     * @param content the new page content
     * @param pageNum the page number. 1 is the first
     */
    public void setPageContent(int pageNum, byte[] content) {
        setPageContent(pageNum, content, PdfStream.DEFAULT_COMPRESSION);
    }

    /**
     * Sets the contents of the page.
     *
     * @param content          the new page content
     * @param pageNum          the page number. 1 is the first
     * @param compressionLevel compression level
     * @since 2.1.3 (the method already existed without param compressionLevel)
     */
    public void setPageContent(int pageNum, byte[] content, int compressionLevel) {
        PdfDictionary page = getPageN(pageNum);
        if (page == null) {
            return;
        }
        PdfObject contents = page.get(PdfName.CONTENTS);
        freeXref = -1;
        killXref(contents);
        if (freeXref == -1) {
            xrefObj.add(null);
            freeXref = xrefObj.size() - 1;
        }
        page.put(PdfName.CONTENTS, new PRIndirectReference(this, freeXref));
        xrefObj.set(freeXref, new PRStream(this, content, compressionLevel));
    }

    /**
     * Eliminates shared streams if they exist.
     */
    public void eliminateSharedStreams() {
        if (!sharedStreams) {
            return;
        }
        sharedStreams = false;
        if (pageRefs.size() == 1) {
            return;
        }
        List<PdfObject> newRefs = new ArrayList<>();
        List<PdfObject> newStreams = new ArrayList<>();
        IntHashtable visited = new IntHashtable();
        for (int k = 1; k <= pageRefs.size(); ++k) {
            PdfDictionary page = pageRefs.getPageN(k);
            if (page == null) {
                continue;
            }
            PdfObject contents = getPdfObject(page.get(PdfName.CONTENTS));
            if (contents == null) {
                continue;
            }
            if (contents.isStream()) {
                PRIndirectReference ref = (PRIndirectReference) page
                        .get(PdfName.CONTENTS);
                if (visited.containsKey(ref.getNumber())) {
                    // need to duplicate
                    newRefs.add(ref);
                    newStreams.add(new PRStream((PRStream) contents, null));
                } else {
                    visited.put(ref.getNumber(), 1);
                }
            } else if (contents.isArray()) {
                PdfArray array = (PdfArray) contents;
                for (int j = 0; j < array.size(); ++j) {
                    PRIndirectReference ref = (PRIndirectReference) array.getPdfObject(j);
                    if (visited.containsKey(ref.getNumber())) {
                        // need to duplicate
                        newRefs.add(ref);
                        newStreams.add(new PRStream((PRStream) getPdfObject(ref), null));
                    } else {
                        visited.put(ref.getNumber(), 1);
                    }
                }
            }
        }
        if (newStreams.isEmpty()) {
            return;
        }
        for (int k = 0; k < newStreams.size(); ++k) {
            xrefObj.add(newStreams.get(k));
            PRIndirectReference ref = (PRIndirectReference) newRefs.get(k);
            ref.setNumber(xrefObj.size() - 1, 0);
        }
    }

    /**
     * Checks if the document was changed.
     *
     * @return <CODE>true</CODE> if the document was changed, <CODE>false</CODE>
     * otherwise
     */
    public boolean isTampered() {
        return tampered;
    }

    /**
     * Sets the tampered state. A tampered PdfReader cannot be reused in PdfStamper.
     *
     * @param tampered the tampered state
     */
    public void setTampered(boolean tampered) {
        this.tampered = tampered;
        pageRefs.keepPages();
    }

    /**
     * Gets the XML metadata.
     *
     * @return the XML metadata
     * @throws IOException on error
     */
    public byte[] getMetadata() throws IOException {
        PdfObject obj = getPdfObject(catalog.get(PdfName.METADATA));
        if (!(obj instanceof PRStream)) {
            return new byte[]{};
        }
        RandomAccessFileOrArray rf = getSafeFile();
        byte[] b;
        try {
            rf.reOpen();
            b = getStreamBytes((PRStream) obj, rf);
        } catch (PDFFilterException e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                rf.close();
            } catch (Exception e) {
                // empty on purpose
            }
        }
        return b;
    }

    /**
     * Gets the byte address of the last xref table.
     *
     * @return the byte address of the last xref table
     */
    public int getLastXref() {
        return lastXref;
    }

    /**
     * Gets the number of xref objects.
     *
     * @return the number of xref objects
     */
    public int getXrefSize() {
        return xrefObj.size();
    }

    /**
     * Gets the byte address of the %%EOF marker.
     *
     * @return the byte address of the %%EOF marker
     */
    public int getEofPos() {
        return eofPos;
    }

    /**
     * Gets the PDF version. Only the last version char is returned. For example version 1.4 is returned as '4'.
     *
     * @return the PDF version
     */
    public char getPdfVersion() {
        return pdfVersion;
    }

    /**
     * Returns <CODE>true</CODE> if the PDF is encrypted.
     *
     * @return <CODE>true</CODE> if the PDF is encrypted
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Returns <CODE>true</CODE> if the owner password has been used to open the document.
     *
     * @return <CODE>true</CODE> if the owner password has been used to open the document.
     */
    public boolean isOwnerPasswordUsed() {
        return ownerPasswordUsed;
    }

    /**
     * Gets the encryption permissions. It can be used directly in
     * <CODE>PdfWriter.setEncryption()</CODE>.
     *
     * @return the encryption permissions
     */
    public int getPermissions() {
        return pValue;
    }

    public void setPermissions(int permissionValue) {
        this.pValue = permissionValue;
    }

    /**
     * Returns <CODE>true</CODE> if the PDF has a 128 bit key encryption.
     *
     * @return <CODE>true</CODE> if the PDF has a 128 bit key encryption
     */
    public boolean is128Key() {
        return rValue == 3;
    }

    /**
     * Gets the trailer dictionary
     *
     * @return the trailer dictionary
     */
    public PdfDictionary getTrailer() {
        return trailer;
    }

    PdfEncryption getDecrypt() {
        return decrypt;
    }

    /**
     * Finds all the font subsets and changes the prefixes to some random values.
     *
     * @return the number of font subsets altered
     */
    public int shuffleSubsetNames() {
        int total = 0;
        for (int k = 1; k < xrefObj.size(); ++k) {
            PdfObject obj = getPdfObjectRelease(k);
            if (isFontDictionary(obj)) {
                PdfDictionary dic = (PdfDictionary) obj;
                total += processFontDictionary(dic, k);
            }
        }
        return total;
    }

    private boolean isFontDictionary(PdfObject obj) {
        if (obj == null || !obj.isDictionary()) {
            return false;
        }
        PdfDictionary dic = (PdfDictionary) obj;
        return existsName(dic, PdfName.TYPE_CONST, PdfName.FONT);
    }

    private int processFontDictionary(PdfDictionary dic, int k) {
        if (isType1OrTrueTypeFont(dic)) {
            return handleSimpleFont(dic, k);
        } else if (isType0Font(dic)) {
            return handleCompositeFont(dic, k);
        }
        return 0;
    }

    private boolean isType1OrTrueTypeFont(PdfDictionary dic) {
        return existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1)
                || existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1)
                || existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE);
    }

    private boolean isType0Font(PdfDictionary dic) {
        return existsName(dic, PdfName.SUBTYPE, PdfName.TYPE0);
    }

    private int handleSimpleFont(PdfDictionary dic, int k) {
        String s = getSubsetPrefix(dic);
        if (s == null) {
            return 0;
        }
        String ns = createRandomSubsetPrefix() + s.substring(7);
        PdfName newName = new PdfName(ns);
        dic.put(PdfName.BASEFONT, newName);
        setXrefPartialObject(k, dic);

        PdfDictionary fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
        if (fd != null) {
            fd.put(PdfName.FONTNAME, newName);
        }
        return 1;
    }

    private int handleCompositeFont(PdfDictionary dic, int k) {
        String s = getSubsetPrefix(dic);
        PdfArray arr = dic.getAsArray(PdfName.DESCENDANTFONTS);
        if (arr == null || arr.isEmpty()) {
            return 0;
        }

        PdfDictionary desc = arr.getAsDict(0);
        String sde = getSubsetPrefix(desc);
        if (sde == null) {
            return 0;
        }

        String ns = createRandomSubsetPrefix();
        if (s != null) {
            dic.put(PdfName.BASEFONT, new PdfName(ns + s.substring(7)));
        }
        setXrefPartialObject(k, dic);

        PdfName newName = new PdfName(ns + sde.substring(7));
        desc.put(PdfName.BASEFONT, newName);

        PdfDictionary fd = desc.getAsDict(PdfName.FONTDESCRIPTOR);
        if (fd != null) {
            fd.put(PdfName.FONTNAME, newName);
        }
        return 1;
    }

    /**
     * Creates a unique subset prefix to be added to the font name when the font is embedded and subset.
     *
     * @return the subset prefix
     */


    private String createRandomSubsetPrefix() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < 6; ++k) {
            int randomIndex = secureRandom.nextInt(26); // Get a random index between 0 and 25
            s.append((char) (randomIndex + 'A')); // Append the corresponding character (A-Z)
        }
        return s + "+";
    }


    /**
     * Finds all the fonts not subset but embedded and marks them as subset.
     *
     * @return the number of fonts altered
     */
    public int createFakeFontSubsets() {
        int total = 0;
        for (int k = 1; k < xrefObj.size(); ++k) {
            PdfObject obj = getPdfObjectRelease(k);
            if (obj == null || !obj.isDictionary()) {
                continue;
            }
            PdfDictionary dic = (PdfDictionary) obj;
            if (!existsName(dic, PdfName.TYPE_CONST, PdfName.FONT)) {
                continue;
            }
            if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1)
                    || existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1)
                    || existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE)) {
                String s = getSubsetPrefix(dic);
                if (s != null) {
                    continue;
                }
                s = getFontName(dic);
                if (s == null) {
                    continue;
                }
                String ns = createRandomSubsetPrefix() + s;
                PdfDictionary fd = (PdfDictionary) getPdfObjectRelease(dic
                        .get(PdfName.FONTDESCRIPTOR));
                if (fd == null) {
                    continue;
                }
                if (fd.get(PdfName.FONTFILE) == null
                        && fd.get(PdfName.FONTFILE2) == null
                        && fd.get(PdfName.FONTFILE3) == null) {
                    continue;
                }
                fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
                PdfName newName = new PdfName(ns);
                dic.put(PdfName.BASEFONT, newName);
                fd.put(PdfName.FONTNAME, newName);
                setXrefPartialObject(k, dic);
                ++total;
            }
        }
        return total;
    }

    /**
     * Gets all the named destinations as an <CODE>HashMap</CODE>. The key is the name and the value is the destinations
     * array.
     *
     * @return gets all the named destinations
     */
    public Map<Object, PdfObject> getNamedDestination() {
        return getNamedDestination(false);
    }

    /**
     * Gets all the named destinations as an <CODE>HashMap</CODE>. The key is the name and the value is the destinations
     * array.
     *
     * @param keepNames true if you want the keys to be real PdfNames instead of Strings
     * @return gets all the named destinations
     * @since 2.1.6
     */
    public Map<Object, PdfObject> getNamedDestination(boolean keepNames) {
        HashMap<Object, PdfObject> names = (HashMap<Object, PdfObject>) getNamedDestinationFromNames(keepNames);
        names.putAll(getNamedDestinationFromStrings());
        return names;
    }

    /**
     * Gets the named destinations from the /Dests key in the catalog as an
     * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
     * array.
     *
     * @return gets the named destinations
     */
    public Map<Object, PdfObject> getNamedDestinationFromNames() {
        return getNamedDestinationFromNames(false);
    }

    /**
     * Gets the named destinations from the /Dests key in the catalog as an
     * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
     * array.
     *
     * @param keepNames true if you want the keys to be real PdfNames instead of Strings
     * @return gets the named destinations
     * @since 2.1.6
     */
    public Map<Object, PdfObject> getNamedDestinationFromNames(boolean keepNames) {
        HashMap<Object, PdfObject> names = new HashMap<>();
        if (catalog.get(PdfName.DESTS) != null) {
            PdfDictionary dic = (PdfDictionary) getPdfObjectRelease(catalog.get(PdfName.DESTS));
            if (dic == null) {
                return names;
            }
            Set<PdfName> keys = dic.getKeys();
            for (PdfName key1 : keys) {
                PdfArray arr = getNameArray(dic.get(key1));
                if (arr == null) {
                    continue;
                }
                if (keepNames) {
                    names.put(key1, arr);
                } else {
                    String name = PdfName.decodeName(key1.toString());
                    names.put(name, arr);
                }
            }
        }
        return names;
    }

    /**
     * Gets the named destinations from the /Names key in the catalog as an
     * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
     * array.
     *
     * @return gets the named destinations
     */
    public Map<String, PdfObject> getNamedDestinationFromStrings() {
        if (catalog.get(PdfName.NAMES) != null) {
            PdfDictionary dic = (PdfDictionary) getPdfObjectRelease(catalog
                    .get(PdfName.NAMES));
            if (dic != null) {
                dic = (PdfDictionary) getPdfObjectRelease(dic.get(PdfName.DESTS));
                if (dic != null) {
                    HashMap<String, PdfObject> names = PdfNameTree.readTree(dic);
                    for (Iterator<Entry<String, PdfObject>> it = names.entrySet().iterator(); it.hasNext(); ) {
                        Entry<String, PdfObject> entry = it.next();
                        PdfArray arr = getNameArray(entry.getValue());
                        if (arr != null) {
                            entry.setValue(arr);
                        } else {
                            it.remove();
                        }
                    }
                    return names;
                }
            }
        }
        return new HashMap<>();
    }

    /**
     * Removes all the fields from the document.
     */
    public void removeFields() {
        pageRefs.resetReleasePage();

        // Iterate over the pages
        for (int k = 1; k <= pageRefs.size(); ++k) {
            PdfDictionary page = pageRefs.getPageN(k);
            PdfArray annots = page.getAsArray(PdfName.ANNOTS);

            if (annots == null) {
                pageRefs.releasePage(k);
                continue;
            }

            // Use ListIterator for safe removal
            ListIterator<PdfObject> iterator = annots.listIterator();

            while (iterator.hasNext()) {
                PdfObject obj = getPdfObjectRelease(iterator.next());

                removeIteratorIfIsObjDictionary(obj, iterator);
            }

            // Remove annotations if empty
            if (annots.isEmpty()) {
                page.remove(PdfName.ANNOTS);
            } else {
                pageRefs.releasePage(k);
            }
        }

        catalog.remove(PdfName.ACROFORM);
        pageRefs.resetReleasePage();
    }

    private void removeIteratorIfIsObjDictionary(PdfObject obj, ListIterator<PdfObject> iterator){
        if (obj != null && obj.isDictionary()) {
            PdfDictionary annot = (PdfDictionary) obj;

            if (PdfName.WIDGET.equals(annot.get(PdfName.SUBTYPE))) {
                iterator.remove();// Remove safely using iterator
            }
        }
    }


    /**
     * Removes all the annotations and fields from the document.
     */
    public void removeAnnotations() {
        pageRefs.resetReleasePage();
        for (int k = 1; k <= pageRefs.size(); ++k) {
            PdfDictionary page = pageRefs.getPageN(k);
            if (page.get(PdfName.ANNOTS) == null) {
                pageRefs.releasePage(k);
            } else {
                page.remove(PdfName.ANNOTS);
            }
        }
        catalog.remove(PdfName.ACROFORM);
        pageRefs.resetReleasePage();
    }

    public List<PdfImportedLink> getLinks(int page) {
        pageRefs.resetReleasePage();
        ArrayList<PdfImportedLink> result = new ArrayList<>();
        PdfDictionary pageDic = pageRefs.getPageN(page);

        if (pageDic != null) { // Check if pageDic is null
            PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);

            if (annots != null) { // Check if annots is null
                for (int j = 0; j < annots.size(); ++j) {
                    PdfDictionary annot = (PdfDictionary) getPdfObjectRelease(annots.getPdfObject(j));

                    if (annot != null && PdfName.LINK.equals(annot.get(PdfName.SUBTYPE))) {
                        result.add(new PdfImportedLink(annot));
                    }
                }
            }
        }
        pageRefs.releasePage(page);
        pageRefs.resetReleasePage();
        return result;
    }


    private void iterateBookmarks(PdfObject outlineRef, Map<Object, PdfObject> names) {
        while (outlineRef != null) {
            replaceNamedDestination(outlineRef, names);

            PdfDictionary outline = (PdfDictionary) getPdfObjectRelease(outlineRef);

            // Check if outline is null before accessing it
            if (outline != null) {
                PdfObject first = outline.get(PdfName.FIRST);
                if (first != null) {
                    iterateBookmarks(first, names);
                }
                outlineRef = outline.get(PdfName.NEXT);
            } else {
                // Handle the case where outline is null if necessary
                // e.g., log a warning, throw an exception, or set outlineRef to null to exit the loop
                outlineRef = null; // or continue/break, based on your needs
            }
        }
    }


    /**
     * Replaces remote named links with local destinations that have the same name.
     *
     * @since 5.0
     */
    public void makeRemoteNamedDestinationsLocal() {
        if (remoteToLocalNamedDestinations) {
            return;
        }
        remoteToLocalNamedDestinations = true;
        Map<Object, PdfObject> names = getNamedDestination(true);
        if (names.isEmpty()) {
            return;
        }
        for (int k = 1; k <= pageRefs.size(); ++k) {
            PdfDictionary page = pageRefs.getPageN(k);
            PdfObject annotsRef = page.get(PdfName.ANNOTS);
            PdfArray annots = (PdfArray) getPdfObject(annotsRef);
            int annotIdx = lastXrefPartial;
            releaseLastXrefPartial();
            if (annots == null) {
                pageRefs.releasePage(k);
                continue;
            }
            boolean commitAnnots = false;
            for (int an = 0; an < annots.size(); ++an) {
                PdfObject objRef = annots.getPdfObject(an);
                if (convertNamedDestination(objRef, names) && !objRef.isIndirect()) {
                    commitAnnots = true;
                }
            }
            if (commitAnnots) {
                setXrefPartialObject(annotIdx, annots);
            }
            if (!commitAnnots || annotsRef.isIndirect()) {
                pageRefs.releasePage(k);
            }
        }
    }

    /**
     * Converts a remote named destination GoToR with a local named destination if there's a corresponding name.
     *
     * @param obj   an annotation that needs to be screened for links to external named destinations.
     * @param names a map with names of local named destinations
     * @since iText 5.0
     */
    private boolean convertNamedDestination(PdfObject obj, Map<Object, PdfObject> names) {
        obj = getPdfObject(obj);
        int objIdx = lastXrefPartial;
        releaseLastXrefPartial();
        if (obj != null && obj.isDictionary()) {
            PdfObject ob2 = getPdfObject(((PdfDictionary) obj).get(PdfName.A));
            if (ob2 != null) {
                int obj2Idx = lastXrefPartial;
                releaseLastXrefPartial();
                PdfDictionary dic = (PdfDictionary) ob2;
                PdfName type = (PdfName) getPdfObjectRelease(dic.get(PdfName.S));
                if (PdfName.GOTOR.equals(type)) {
                    PdfObject ob3 = getPdfObjectRelease(dic.get(PdfName.D));
                    Object name = null;
                    if (ob3 != null) {
                        if (ob3.isName()) {
                            name = ob3;
                        } else if (ob3.isString()) {
                            name = ob3.toString();
                        }
                        PdfArray dest = (PdfArray) names.get(name);
                        if (dest != null) {
                            dic.remove(PdfName.F);
                            dic.remove(PdfName.NEWWINDOW);
                            dic.put(PdfName.S, PdfName.GOTO);
                            setXrefPartialObject(obj2Idx, ob2);
                            setXrefPartialObject(objIdx, obj);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Replaces all the local named links with the actual destinations.
     */
    public void consolidateNamedDestinations() {
        if (consolidateNamedDestinations) {
            return;
        }
        consolidateNamedDestinations = true;
        Map<Object, PdfObject> names = getNamedDestination(true);
        if (names.isEmpty()) {
            return;
        }
        for (int k = 1; k <= pageRefs.size(); ++k) {
            PdfDictionary page = pageRefs.getPageN(k);
            PdfObject annotsRef = page.get(PdfName.ANNOTS);
            PdfArray annots = (PdfArray) getPdfObject(annotsRef);
            int annotIdx = lastXrefPartial;
            releaseLastXrefPartial();
            if (annots == null) {
                pageRefs.releasePage(k);
                continue;
            }
            boolean commitAnnots = false;
            for (int an = 0; an < annots.size(); ++an) {
                PdfObject objRef = annots.getPdfObject(an);
                if (replaceNamedDestination(objRef, names) && !objRef.isIndirect()) {
                    commitAnnots = true;
                }
            }
            if (commitAnnots) {
                setXrefPartialObject(annotIdx, annots);
            }
            if (!commitAnnots || annotsRef.isIndirect()) {
                pageRefs.releasePage(k);
            }
        }
        PdfDictionary outlines = (PdfDictionary) getPdfObjectRelease(catalog
                .get(PdfName.OUTLINES));
        if (outlines == null) {
            return;
        }
        iterateBookmarks(outlines.get(PdfName.FIRST), names);
    }

    private boolean replaceNamedDestination(PdfObject obj, Map<Object, PdfObject> names) {
        obj = getPdfObject(obj);
        int objIdx = lastXrefPartial;
        releaseLastXrefPartial();
        if (obj != null && obj.isDictionary()) {
            PdfObject ob2 = getPdfObjectRelease(((PdfDictionary) obj)
                    .get(PdfName.DEST));
            Object name = null;
            if (ob2 != null) {
                if (ob2.isName()) {
                    name = ob2;
                } else if (ob2.isString()) {
                    name = ob2.toString();
                }
                PdfArray dest = (PdfArray) names.get(name);
                if (dest != null) {
                    ((PdfDictionary) obj).put(PdfName.DEST, dest);
                    setXrefPartialObject(objIdx, obj);
                    return true;
                }
            } else if ((ob2 = getPdfObject(((PdfDictionary) obj).get(PdfName.A))) != null) {
                int obj2Idx = lastXrefPartial;
                releaseLastXrefPartial();
                PdfDictionary dic = (PdfDictionary) ob2;
                PdfName type = (PdfName) getPdfObjectRelease(dic.get(PdfName.S));
                if (PdfName.GOTO.equals(type)) {
                    PdfObject ob3 = getPdfObjectRelease(dic.get(PdfName.D));
                    if (ob3 != null) {
                        if (ob3.isName()) {
                            name = ob3;
                        } else if (ob3.isString()) {
                            name = ob3.toString();
                        }
                    }
                    PdfArray dest = (PdfArray) names.get(name);
                    if (dest != null) {
                        dic.put(PdfName.D, dest);
                        setXrefPartialObject(obj2Idx, ob2);
                        setXrefPartialObject(objIdx, obj);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Closes the reader
     */
    @Override
    public void close() {
        if (!partial) {
            return;
        }
        try {
            tokens.close();
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeUnusedNode(PdfObject obj, boolean[] hits) {
        Deque<Object> state = new ArrayDeque<>();
        state.push(obj);

        while (!state.isEmpty()) {
            Object current = state.pop();
            if (current == null) {
                continue;
            }

            if (current instanceof PdfObject pdfObject) {
                handlePdfObject(state, hits, pdfObject);
            } else if (current instanceof Object[] objects) {  // Aggiungiamo la condizione corretta qui
                handleStateObject(state, objects);
            }
        }
    }


    private void handlePdfObject(Deque<Object> state, boolean[] hits, PdfObject obj) {
        switch (obj.type()) {
            case PdfObject.DICTIONARY, PdfObject.STREAM:
                PdfDictionary dic = (PdfDictionary) obj;
                PdfName[] keys = new PdfName[dic.size()];
                dic.getKeys().toArray(keys);
                for(PdfName key:keys){
                    state.push(key);
                }
                state.push(dic);
                break;
            case PdfObject.ARRAY:
                List<PdfObject> ar = ((PdfArray) obj).getElements();
                for(PdfObject element:ar){
                    state.push(element);
                }
                break;
            case PdfObject.INDIRECT:
                handleIndirectReference(state, hits, (PRIndirectReference) obj);
                break;
            default:
                break;
        }
    }

    private void handleStateObject(Deque<Object> state, Object[] objs) {
        if (objs[0] instanceof List) {
            handleArrayState(state, (List<PdfObject>) objs[0], (int) objs[1]);
        } else {
            handleDictionaryState(state, (PdfName[]) objs[0], (PdfDictionary) objs[1], (int) objs[2]);
        }
    }

    private void handleArrayState(Deque<Object> state, List<PdfObject> ar, int idx) {
        for (int k = idx; k < ar.size(); ++k) {
            PdfObject v = ar.get(k);
            if (v.isIndirect()) {
                handleIndirectArrayElement(ar, k, v);
            }
            state.push(v);
        }
    }

    private void handleDictionaryState(Deque<Object> state, PdfName[] keys, PdfDictionary dic, int idx) {
        for (int k = idx; k < keys.length; ++k) {
            PdfName key = keys[k];
            PdfObject v = dic.get(key);
            if (v.isIndirect()) {
                handleIndirectDictionaryElement(dic, key, v);
            }
            state.push(v);
        }
    }

    private void handleIndirectReference(Deque<Object> state, boolean[] hits, PRIndirectReference ref) {
        int num = ref.getNumber();
        if (!hits[num]) {
            hits[num] = true;
            state.push(getPdfObjectRelease(ref));
        }
    }

    private void handleIndirectArrayElement(List<PdfObject> ar, int k, PdfObject v) {
        int num = ((PRIndirectReference) v).getNumber();
        if (num < 0 || num >= xrefObj.size() || (!partial && xrefObj.get(num) == null)) {
            ar.set(k, PdfNull.PDFNULL);
        }
    }

    private void handleIndirectDictionaryElement(PdfDictionary dic, PdfName key, PdfObject v) {
        int num = ((PRIndirectReference) v).getNumber();
        if (num < 0 || num >= xrefObj.size() || (!partial && xrefObj.get(num) == null)) {
            dic.put(key, PdfNull.PDFNULL);
        }
    }

    /**
     * Removes all the unreachable objects.
     *
     * @return the number of indirect objects removed
     */
    public int removeUnusedObjects() {
        boolean[] hits = new boolean[xrefObj.size()];
        removeUnusedNode(trailer, hits);
        int total = 0;
        if (partial) {
            for (int k = 1; k < hits.length; ++k) {
                if (!hits[k]) {
                    xref[k * 2] = -1;
                    xref[k * 2 + 1] = 0;
                    xrefObj.set(k, null);
                    ++total;
                }
            }
        } else {
            for (int k = 1; k < hits.length; ++k) {
                if (!hits[k]) {
                    xrefObj.set(k, null);
                    ++total;
                }
            }
        }
        return total;
    }

    /**
     * Gets a read-only version of <CODE>AcroFields</CODE>.
     *
     * @return a read-only version of <CODE>AcroFields</CODE>
     */
    public AcroFields getAcroFields() {
        return new AcroFields(this, null);
    }

    /**
     * Gets the global document JavaScript.
     *
     * @param file the document file
     * @return the global document JavaScript
     * @throws IOException on error
     */
    public String getJavaScript(RandomAccessFileOrArray file) throws IOException, PDFFilterException {
        PdfDictionary names = (PdfDictionary) getPdfObjectRelease(catalog
                .get(PdfName.NAMES));
        if (names == null) {
            return null;
        }
        PdfDictionary js = (PdfDictionary) getPdfObjectRelease(names
                .get(PdfName.JAVASCRIPT));
        if (js == null) {
            return null;
        }
        Map<String, PdfObject> jscript = PdfNameTree.readTree(js);
        String[] sortedNames = new String[jscript.size()];
        sortedNames = jscript.keySet().toArray(sortedNames);
        Arrays.sort(sortedNames);
        StringBuilder buf = new StringBuilder();
        for (String sortedName : sortedNames) {
            PdfDictionary j = (PdfDictionary) getPdfObjectRelease(jscript.get(sortedName));
            if (j == null) {
                continue;
            }
            PdfObject obj = getPdfObjectRelease(j.get(PdfName.JS));
            if (obj != null) {
                if (obj.isString()) {
                    buf.append(((PdfString) obj).toUnicodeString()).append('\n');
                } else if (obj.isStream()) {
                    byte[] bytes = getStreamBytes((PRStream) obj, file);
                    if (bytes.length >= 2 && bytes[0] == (byte) 254
                            && bytes[1] == (byte) 255) {
                        buf.append(PdfEncodings.convertToString(bytes,
                                PdfObject.TEXT_UNICODE));
                    } else {
                        buf.append(PdfEncodings.convertToString(bytes,
                                PdfObject.TEXT_PDFDOCENCODING));
                    }
                    buf.append('\n');
                }
            }
        }
        return buf.toString();
    }

    /**
     * Gets the global document JavaScript.
     *
     * @return the global document JavaScript
     * @throws IOException on error
     */
    public String getJavaScript() throws IOException {
        RandomAccessFileOrArray rf = getSafeFile();
        try {
            rf.reOpen();
            return getJavaScript(rf);
        } catch (PDFFilterException e) {
            throw new IOException(e.getMessage());
        } finally {
            try {
                rf.close();
            } catch (Exception ignored) {
                //da vedere come effettuare il log
            }
        }
    }

    /**
     * Selects the pages to keep in the document. The pages are described as ranges. The page ordering can be changed
     * but no page repetitions are allowed. Note that it may be very slow in partial mode.
     *
     * @param ranges the comma separated ranges as described in {@link SequenceList}
     */
    public void selectPages(String ranges) {
        selectPages(SequenceList.expand(ranges, getNumberOfPages()));
    }

    /**
     * Selects the pages to keep in the document. The pages are described as a
     * <CODE>List</CODE> of <CODE>Integer</CODE>. The page ordering can be changed
     * but no page repetitions are allowed. Note that it may be very slow in partial mode.
     *
     * @param pagesToKeep the pages to keep in the document
     */
    public void selectPages(List<Integer> pagesToKeep) {
        pageRefs.selectPages(pagesToKeep);
        removeUnusedObjects();
    }

    /**
     * Sets the viewer preferences as the sum of several constants.
     *
     * @param preferences the viewer preferences
     * @see PdfViewerPreferences#setViewerPreferences
     */
    @Override
    public void setViewerPreferences(int preferences) {
        this.viewerPreferences.setViewerPreferences(preferences);
        setViewerPreferences(this.viewerPreferences);
    }

    /**
     * Adds a viewer preference
     *
     * @param key   a key for a viewer preference
     * @param value a value for the viewer preference
     * @see PdfViewerPreferences#addViewerPreference
     */
    @Override
    public void addViewerPreference(PdfName key, PdfObject value) {
        this.viewerPreferences.addViewerPreference(key, value);
        setViewerPreferences(this.viewerPreferences);
    }

    void setViewerPreferences(PdfViewerPreferencesImp vp) {
        vp.addToCatalog(catalog);
    }

    /**
     * Returns a bitset representing the PageMode and PageLayout viewer preferences. Doesn't return any information
     * about the ViewerPreferences dictionary.
     *
     * @return an int that contains the Viewer Preferences.
     */
    public int getSimpleViewerPreferences() {
        return PdfViewerPreferencesImp.getViewerPreferences(catalog)
                .getPageLayoutAndMode();
    }

    /**
     * Getter for property appendable.
     *
     * @return Value of property appendable.
     */
    public boolean isAppendable() {
        return this.appendable;
    }

    /**
     * Setter for property appendable.
     *
     * @param appendable New value of property appendable.
     */
    public void setAppendable(boolean appendable) {
        this.appendable = appendable;
        if (appendable) {
            getPdfObject(trailer.get(PdfName.ROOT));
        }
    }

    /**
     * Getter for property newXrefType.
     *
     * @return Value of property newXrefType.
     */
    public boolean isNewXrefType() {
        return newXrefType;
    }

    /**
     * Getter for property fileLength.
     *
     * @return Value of property fileLength.
     */
    public int getFileLength() {
        return fileLength;
    }

    /**
     * Getter for property hybridXref.
     *
     * @return Value of property hybridXref.
     */
    public boolean isHybridXref() {
        return hybridXref;
    }

    PdfIndirectReference getCryptoRef() {
        if (cryptoRef == null) {
            return null;
        }
        return new PdfIndirectReference(0, cryptoRef.getNumber(),
                cryptoRef.getGeneration());
    }

    /**
     * Removes any usage rights that this PDF may have. Only Adobe can grant usage rights and any PDF modification with
     * iText will invalidate them. Invalidated usage rights may confuse Acrobat and it's advisable to remove them
     * altogether.
     */
    public void removeUsageRights() {
        PdfDictionary perms = catalog.getAsDict(PdfName.PERMS);
        if (perms == null) {
            return;
        }
        perms.remove(PdfName.UR);
        perms.remove(PdfName.UR3);
        if (perms.size() == 0) {
            catalog.remove(PdfName.PERMS);
        }
    }

    /**
     * Gets the certification level for this document. The return values can be
     * <code>PdfSignatureAppearance.NOT_CERTIFIED</code>,
     * <code>PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED</code>,
     * <code>PdfSignatureAppearance.CERTIFIED_FORM_FILLING</code> and
     * <code>PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS</code>
     * .
     * <p>
     * No signature validation is made, use the methods available for that in
     * <CODE>AcroFields</CODE>.
     * </p>
     *
     * @return gets the certification level for this document
     */
    public int getCertificationLevel() {
        PdfDictionary dic = catalog.getAsDict(PdfName.PERMS);
        if (dic == null) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        dic = dic.getAsDict(PdfName.DOCMDP);
        if (dic == null) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        PdfArray arr = dic.getAsArray(PdfName.REFERENCE);
        if (arr == null || arr.isEmpty()) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        dic = arr.getAsDict(0);
        if (dic == null) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        dic = dic.getAsDict(PdfName.TRANSFORMPARAMS);
        if (dic == null) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        PdfNumber p = dic.getAsNumber(PdfName.P);
        if (p == null) {
            return PdfSignatureAppearance.NOT_CERTIFIED;
        }
        return p.intValue();
    }

    /**
     * Checks if an encrypted document may be modified if the owner password was not supplied. If the document is not
     * encrypted, the setting has no effect.
     *
     * @return <CODE>true</CODE> if the document may be modified even if the owner password was not
     * supplied <CODE>false</CODE> otherwise
     */
    public boolean isModificationlowedWithoutOwnerPassword() {
        return this.modificationAllowedWithoutOwnerPassword;
    }

    /**
     * Sets whether the document (if encrypted) may be modified even if the owner password was not supplied. If this is
     * set to <CODE>false</CODE> an exception will be thrown when attempting to access the Document if the owner
     * password was not supplied (for encrypted documents.)
     *
     * @param modificationAllowedWithoutOwnerPassword the modificationAllowedWithoutOwnerPassword state.
     */
    public void setModificationAllowedWithoutOwnerPassword(
            boolean modificationAllowedWithoutOwnerPassword) {
        this.modificationAllowedWithoutOwnerPassword = modificationAllowedWithoutOwnerPassword;
    }

    /**
     * Checks if the document was opened with the owner password so that the end application can decide what level of
     * access restrictions to apply. If the document is not encrypted it will return <CODE>true</CODE>.
     *
     * @return <CODE>true</CODE> if the document was opened with the owner password or if it's not
     * encrypted or the modificationAllowedWithoutOwnerPassword flag is set,
     * <CODE>false</CODE> otherwise.
     */
    public final boolean isOpenedWithFullPermissions() {
        return !encrypted || ownerPasswordUsed || modificationAllowedWithoutOwnerPassword;
    }

    public int getCryptoMode() {
        if (decrypt == null) {
            return -1;
        } else {
            return decrypt.getCryptoMode();
        }
    }

    public boolean isMetadataEncrypted() {
        if (decrypt == null) {
            return false;
        } else {
            return decrypt.isMetadataEncrypted();
        }
    }

    public byte[] computeUserPassword() {
        if (!encrypted || !ownerPasswordUsed) {
            return new byte[]{};
        }
        return decrypt.computeUserPassword(password);
    }

    /**
     * Returns a permanent document identifier extracted from trailer /ID entry, when present
     *
     * @return byte array representing the document permanent identifier
     */
    public byte[] getDocumentId() {
        if (trailer == null) {
            return new byte[]{};
        }
        PdfArray documentIDs = trailer.getAsArray(PdfName.ID);
        if (documentIDs == null || documentIDs.isEmpty()) {
            return new byte[]{};
        }
        PdfObject o = documentIDs.getPdfObject(0);
        return DocWriter.getISOBytes(o.toString());
    }

    static class PageRefs {

        private final PdfReader reader;
        /**
         * ArrayList with the indirect references to every page. Element 0 = page 1; 1 = page 2;... Not used for partial
         * reading.
         */
        private List<PdfObject> refsn;
        /**
         * The number of pages, updated only in case of partial reading.
         */
        private int sizep;
        /**
         * intHashtable that does the same thing as refsn in case of partial reading: major difference: not all the
         * pages are read.
         */
        private IntHashtable refsp;
        /**
         * Page number of the last page that was read (partial reading only)
         */
        private int lastPageRead = -1;
        /**
         * stack to which pages dictionaries are pushed to keep track of the current page attributes
         */
        private List<PdfDictionary> pageInh;
        private boolean keepPages;

        private PageRefs(PdfReader reader) {
            this.reader = reader;
            if (reader.partial) {
                refsp = new IntHashtable();
                PdfNumber npages = (PdfNumber) PdfReader.getPdfObjectRelease(reader.rootPages.get(PdfName.COUNT));

                // Check if npages is null before calling intValue()
                if (npages != null) {
                    sizep = npages.intValue();
                } else {
                    // Handle the case where npages is null
                    // Option 1: Throw an exception
                    throw new IllegalStateException("Page count is not available. 'npages' is null.");

                    // Option 2: Set sizep to a default value
                    // sizep = 0; // or another appropriate default value

                    // Option 3: Log a warning message (if using a logging framework)
                    // Logger.warn("Page count is null. Defaulting sizep to 0.")
                }
            } else {
                readPages();
            }
        }


        PageRefs(PageRefs other, PdfReader reader) {
            this.reader = reader;
            this.sizep = other.sizep;
            if (other.refsn != null) {
                refsn = new ArrayList<>(other.refsn);
                refsn.replaceAll(original -> duplicatePdfObject(original, reader));
            } else {
                this.refsp = other.refsp;
            }
        }

        int size() {
            if (refsn != null) {
                return refsn.size();
            } else {
                return sizep;
            }
        }

        void readPages() {
            if (refsn != null) {
                return;
            }
            refsp = null;
            refsn = new ArrayList<>();
            pageInh = new ArrayList<>();
            PdfObject obj = reader.catalog.get(PdfName.PAGES);
            if (obj instanceof PRIndirectReference prIndirectReference) {
                iteratePages(prIndirectReference);
            } else if (obj instanceof PdfDictionary pdfDictionary) {
                iteratePages(pdfDictionary);
            }
            pageInh = null;
            reader.rootPages.put(PdfName.COUNT, new PdfNumber(refsn.size()));
        }

        void reReadPages() {
            refsn = null;
            readPages();
        }

        /**
         * Gets the page dictionary of the specified page
         *
         * @param pageNum the page number. 1 is the first
         * @return the page dictionary
         */
        public PdfDictionary getPageN(int pageNum) {
            PRIndirectReference ref = getPageOrigRef(pageNum);
            return (PdfDictionary) PdfReader.getPdfObject(ref);
        }

        /**
         * Gets the page reference to this page.
         *
         * @param pageNum the page number.
         * @return a dictionary object or null when the page does not exist
         */
        public PdfDictionary getPageNRelease(int pageNum) {
            PdfDictionary page = getPageN(pageNum);
            releasePage(pageNum);
            return page;
        }

        /**
         * Releases the page reference to this page.
         *
         * @param pageNum the page number.
         * @return an indirect reference
         */
        public PRIndirectReference getPageOrigRefRelease(int pageNum) {
            PRIndirectReference ref = getPageOrigRef(pageNum);
            releasePage(pageNum);
            return ref;
        }

        /**
         * Gets the page reference to this page.
         *
         * @param pageNum the page number. 1 is the first
         * @return the page reference or null if the page does not exist
         */
        public PRIndirectReference getPageOrigRef(int pageNum) {
            try {
                --pageNum;
                if (pageNum < 0 || pageNum >= size()) {
                    return null;
                }
                if (refsn != null) {
                    return (PRIndirectReference) refsn.get(pageNum);
                } else {
                    int n = refsp.get(pageNum);
                    if (n == 0) {
                        PRIndirectReference ref = getSinglePage(pageNum);
                        if (reader.lastXrefPartial == -1) {
                            lastPageRead = -1;
                        } else {
                            lastPageRead = pageNum;
                        }
                        reader.lastXrefPartial = -1;
                        refsp.put(pageNum, ref.getNumber());
                        if (keepPages) {
                            lastPageRead = -1;
                        }
                        return ref;
                    } else {
                        if (lastPageRead != pageNum) {
                            lastPageRead = -1;
                        }
                        if (keepPages) {
                            lastPageRead = -1;
                        }
                        return new PRIndirectReference(reader, n);
                    }
                }
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }

        void keepPages() {
            if (refsp == null || keepPages) {
                return;
            }
            keepPages = true;
            refsp.clear();
        }

        /**
         *
         */
        public void releasePage(int pageNum) {
            if (refsp == null) {
                return;
            }
            --pageNum;
            if (pageNum < 0 || pageNum >= size()) {
                return;
            }
            if (pageNum != lastPageRead) {
                return;
            }
            lastPageRead = -1;
            reader.lastXrefPartial = refsp.get(pageNum);
            reader.releaseLastXrefPartial();
            refsp.remove(pageNum);
        }

        /**
         *
         */
        public void resetReleasePage() {
            if (refsp == null) {
                return;
            }
            lastPageRead = -1;
        }

        void insertPage(int pageNum, PRIndirectReference ref) {
            --pageNum;
            if (refsn != null) {
                if (pageNum >= refsn.size()) {
                    refsn.add(ref);
                } else {
                    refsn.add(pageNum, ref);
                }
            } else {
                ++sizep;
                lastPageRead = -1;
                if (pageNum >= size()) {
                    refsp.put(size(), ref.getNumber());
                } else {
                    IntHashtable refs2 = new IntHashtable((refsp.size() + 1) * 2);
                    for (Iterator<?> it = refsp.getEntryIterator(); it.hasNext(); ) {
                        IntHashtable.Entry entry = (IntHashtable.Entry) it.next();
                        int p = entry.getKey();
                        refs2.put(p >= pageNum ? p + 1 : p, entry.getValue());
                    }
                    refs2.put(pageNum, ref.getNumber());
                    refsp = refs2;
                }
            }
        }

        /**
         * Adds a PdfDictionary to the pageInh stack to keep track of the page attributes.
         *
         * @param nodePages a Pages dictionary
         */
        private void pushPageAttributes(PdfDictionary nodePages) {
            PdfDictionary dic = new PdfDictionary();
            if (!pageInh.isEmpty()) {
                dic.putAll(pageInh.get(pageInh.size() - 1));
            }
            for (PdfName pageInhCandidate : pageInhCandidates) {
                PdfObject obj = nodePages.get(pageInhCandidate);
                if (obj != null) {
                    dic.put(pageInhCandidate, obj);
                }
            }
            pageInh.add(dic);
        }

        /**
         * Removes the last PdfDictionary that was pushed to the pageInh stack.
         */
        private void popPageAttributes() {
            pageInh.remove(pageInh.size() - 1);
        }

        private void iteratePages(PRIndirectReference rpage) {
            PdfDictionary page = (PdfDictionary) getPdfObject(rpage);
            if (page == null) {
                return;
            }
            PdfArray kidsPR = page.getAsArray(PdfName.KIDS);
            // reference to a leaf
            if (kidsPR == null) {
                page.put(PdfName.TYPE_CONST, PdfName.PAGE);
                PdfDictionary dic = pageInh.get(pageInh.size() - 1);
                for (PdfName key : dic.getKeys()) {
                    if (page.get(key) == null) {
                        page.put(key, dic.get(key));
                    }
                }
                if (page.get(PdfName.MEDIABOX) == null) {
                    PdfArray arr = new PdfArray(new float[]{0, 0,
                            PageSize.LETTER.getRight(), PageSize.LETTER.getTop()});
                    page.put(PdfName.MEDIABOX, arr);
                }
                refsn.add(rpage);
            } else {
                // reference to a branch
                page.put(PdfName.TYPE_CONST, PdfName.PAGES);
                pushPageAttributes(page);
                for (int k = 0; k < kidsPR.size(); ++k) {
                    PdfObject obj = kidsPR.getPdfObject(k);
                    if (!obj.isIndirect()) {
                        while (k < kidsPR.size()) {
                            kidsPR.remove(k);
                        }
                        break;
                    }
                    if (obj instanceof PRIndirectReference prIndirectReference) {
                        iteratePages(prIndirectReference);
                    } else if (obj instanceof PdfDictionary pdfDictionary) {
                        iteratePages(pdfDictionary);
                    }
                }
                popPageAttributes();
            }
        }

        private void iteratePages(PdfDictionary page) {
            PdfArray kidsPR = page.getAsArray(PdfName.KIDS);
            // reference to a leaf
            if (kidsPR != null) {
                page.put(PdfName.TYPE_CONST, PdfName.PAGES);
                pushPageAttributes(page);
                for (int k = 0; k < kidsPR.size(); ++k) {
                    PdfObject obj = kidsPR.getPdfObject(k);
                    if (!obj.isIndirect()) {
                        while (k < kidsPR.size()) {
                            kidsPR.remove(k);
                        }
                        break;
                    }
                    if (obj instanceof PRIndirectReference prIndirectReference) {
                        iteratePages(prIndirectReference);
                    } else if (obj instanceof PdfDictionary pdfDictionary) {
                        iteratePages(pdfDictionary);
                    }
                }
                popPageAttributes();
            }
        }

        protected PRIndirectReference getSinglePage(int n) {
            PdfDictionary acc = new PdfDictionary();
            PdfDictionary top = reader.rootPages;
            int base = 0;
            while (true) {
                for (PdfName pageInhCandidate : pageInhCandidates) {
                    PdfObject obj = top.get(pageInhCandidate);
                    if (obj != null) {
                        acc.put(pageInhCandidate, obj);
                    }
                }

                PdfArray kids = (PdfArray) PdfReader.getPdfObjectRelease(top.get(PdfName.KIDS));
                // Null check for kids
                if (kids == null) {
                    throw new IllegalStateException("No kids found in the PDF structure.");
                }

                for (PdfObject pdfObject : kids.getElements()) {
                    PRIndirectReference ref = (PRIndirectReference) pdfObject;
                    PdfDictionary dic = (PdfDictionary) getPdfObject(ref);

                    // Null check for dic
                    if (dic == null) {
                        throw new IllegalStateException("Dictionary not found for the PDF object.");
                    }

                    int last = reader.lastXrefPartial;
                    PdfObject count = getPdfObjectRelease(dic.get(PdfName.COUNT));
                    reader.lastXrefPartial = last;
                    int acn = 1;
                    if (count != null && count.type() == PdfObject.NUMBER) {
                        acn = ((PdfNumber) count).intValue();
                    }
                    if (n < base + acn) {
                        if (count == null) {
                            dic.mergeDifferent(acc);
                            return ref;
                        }
                        reader.releaseLastXrefPartial();
                        top = dic;
                        break;
                    }
                    reader.releaseLastXrefPartial();
                    base += acn;
                }
            }
        }


        private void selectPages(List<Integer> pagesToKeep) {
            IntHashtable pg = new IntHashtable();
            List<Integer> finalPages = new ArrayList<>();
            int psize = size();

            for (Integer aPagesToKeep : pagesToKeep) {
                if (aPagesToKeep >= 1 && aPagesToKeep <= psize && pg.put(aPagesToKeep, 1) == 0) {
                    finalPages.add(aPagesToKeep);
                }
            }

            if (reader.partial) {
                for (int k = 1; k <= psize; ++k) {
                    getPageOrigRef(k);
                    resetReleasePage();
                }
            }

            PRIndirectReference parent = (PRIndirectReference) reader.catalog.get(PdfName.PAGES);
            PdfDictionary topPages = (PdfDictionary) PdfReader.getPdfObject(parent);

            if (topPages == null) {
                // Handle the null case: log an error, throw an exception, or return from the method
                throw new IllegalStateException("The top-level pages dictionary is null. Cannot proceed with page selection.");
            }

            List<PdfObject> newPageRefs = new ArrayList<>(finalPages.size());
            PdfArray kids = new PdfArray();

            for (Integer finalPage : finalPages) {
                int p = finalPage;
                PRIndirectReference pref = getPageOrigRef(p);
                resetReleasePage();
                kids.add(pref);
                newPageRefs.add(pref);
                getPageN(p).put(PdfName.PARENT, parent);
            }

            AcroFields af = reader.getAcroFields();
            boolean removeFields = (!af.getAllFields().isEmpty());

            for (int k = 1; k <= psize; ++k) {
                if (!pg.containsKey(k)) {
                    if (removeFields) {
                        af.removeFieldsFromPage(k);
                    }
                    PRIndirectReference pref = getPageOrigRef(k);
                    int nref = pref.getNumber();
                    reader.xrefObj.set(nref, null);
                    if (reader.partial) {
                        reader.xref[nref * 2] = -1;
                        reader.xref[nref * 2 + 1] = 0;
                    }
                }
            }

            // Now, safely use topPages since it's checked for null
            topPages.put(PdfName.COUNT, new PdfNumber(finalPages.size()));
            topPages.put(PdfName.KIDS, kids);

            refsp = null;
            refsn = newPageRefs;
        }


    }
}
