/*
 * $Id: PdfStream.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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

import com.lowagie.text.DocWriter;
import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


/**
 * <CODE>PdfStream</CODE> is the Pdf stream object.
 * <p>
 * A stream, like a string, is a sequence of characters. However, an application can read a small portion of a stream at
 * a time, while a string must be read in its entirety. For this reason, objects with potentially large amounts of data,
 * such as images and page descriptions, are represented as streams.<BR> A stream consists of a dictionary that
 * describes a sequence of characters, followed by the keyword <B>stream</B>, followed by zero or more lines of
 * characters, followed by the keyword <B>endstream</B>.<BR> All streams must be <CODE>PdfIndirectObject</CODE>s. The
 * stream dictionary must be a direct object. The keyword <B>stream</B> that follows the stream dictionary should be
 * followed by a carriage return and linefeed or just a linefeed.<BR> Remark: In this version only the
 * FLATEDECODE-filter is supported.<BR> This object is described in the 'Portable Document Format Reference Manual
 * version 1.7' section 3.2.7 (page 60-63).<BR>
 *
 * @see PdfObject
 * @see PdfDictionary
 */

public class PdfStream extends PdfDictionary {

    // membervariables

    /**
     * A possible compression level.
     *
     * @since 2.1.3
     */
    public static final int DEFAULT_COMPRESSION = -1;
    /**
     * A possible compression level.
     *
     * @since 2.1.3
     */
    public static final int NO_COMPRESSION = 0;
    /**
     * A possible compression level.
     *
     * @since 2.1.3
     */
    public static final int BEST_SPEED = 1;
    /**
     * A possible compression level.
     *
     * @since 2.1.3
     */
    public static final int BEST_COMPRESSION = 9;
    static final byte[] STARTSTREAM = DocWriter.getISOBytes("stream\n");
    static final byte[] ENDSTREAM = DocWriter.getISOBytes("\nendstream");
    static final int SIZESTREAM = STARTSTREAM.length + ENDSTREAM.length;
    /**
     * is the stream compressed?
     */
    protected boolean compressed = false;
    /**
     * The level of compression.
     *
     * @since 2.1.3
     */
    protected int compressionLevel = NO_COMPRESSION;
    protected ByteArrayOutputStream streamBytes = null;
    protected InputStream inputStream;
    protected PdfIndirectReference ref;
    protected long inputStreamLength = -1;
    protected PdfWriter writer;
    protected long rawLength;

    // constructors

    /**
     * Constructs a <CODE>PdfStream</CODE>-object.
     *
     * @param bytes content of the new <CODE>PdfObject</CODE> as an array of <CODE>byte</CODE>.
     */

    public PdfStream(byte[] bytes) {
        super();
        type = STREAM;
        this.bytes = bytes;
        rawLength = bytes.length;
        put(PdfName.PDF_NAME_LENGTH, new PdfNumber(bytes.length));
    }

    /**
     * Creates an efficient stream. No temporary array is ever created. The <CODE>InputStream</CODE> is totally consumed
     * but is not closed. The general usage is:
     * <pre>
     * InputStream in = ...;
     * PdfStream stream = new PdfStream(in, writer);
     * stream.flateCompress();
     * writer.addToBody(stream);
     * stream.writeLength();
     * in.close();
     * </pre>
     *
     * @param inputStream the data to write to this stream
     * @param writer      the <CODE>PdfWriter</CODE> for this stream
     */
    public PdfStream(InputStream inputStream, PdfWriter writer) {
        super();
        type = STREAM;
        this.inputStream = inputStream;
        this.writer = writer;
        ref = writer.getPdfIndirectReference();
        put(PdfName.PDF_NAME_LENGTH, ref);
    }

    /**
     * Constructs a <CODE>PdfStream</CODE>-object.
     */

    protected PdfStream() {
        super();
        type = STREAM;
    }

    /**
     * Writes the stream length to the <CODE>PdfWriter</CODE>.
     * <p>
     * This method must be called and can only be called if the constructor {@link #PdfStream(InputStream, PdfWriter)}
     * is used to create the stream.
     *
     * @throws IOException on error
     * @see #PdfStream(InputStream, PdfWriter)
     */
    public void writeLength() throws IOException {
        if (inputStream == null) {
            throw new UnsupportedOperationException(MessageLocalization.getComposedMessage(
                    "writelength.can.only.be.called.in.a.contructed.pdfstream.inputstream.pdfwriter"));
        }
        if (inputStreamLength == -1) {
            throw new IOException(MessageLocalization.getComposedMessage(
                    "writelength.can.only.be.called.after.output.of.the.stream.body"));
        }
        writer.addToBody(new PdfNumber(inputStreamLength), ref, false);
    }

    /**
     * Gets the raw length of the stream.
     *
     * @return the raw length of the stream
     */
    public long getRawLength() {
        return rawLength;
    }

    /**
     * Compresses the stream.
     */
    public void flateCompress() {
        flateCompress(DEFAULT_COMPRESSION);
    }

    /**
     * Compresses the stream.
     *
     * @param compressionLevel the compression level (0 = best speed, 9 = best compression, -1 is default)
     * @since 2.1.3
     */
    public void flateCompress(int compressionLevel) {
        if (!shouldCompress()) {
            return;
        }
        if (inputStream != null) {
            compressWithInputStream();
            return;
        }
        handleFilter();
        performCompression(compressionLevel);
    }

    private boolean shouldCompress() {
        return Document.compress && !compressed;
    }

    private void compressWithInputStream() {
        // If inputStream is not null, just mark as compressed
        compressed = true;
    }

    private void handleFilter() {
        PdfObject filter = PdfReader.getPdfObject(get(PdfName.FILTER));
        if (filter != null) {
            if (filter.isName()) {
                if (PdfName.FLATEDECODE.equals(filter)) {
                    return; // FLATEDECODE is already set
                }
            } else if (filter.isArray()) {
                if (((PdfArray) filter).contains(PdfName.FLATEDECODE)) {
                    return; // FLATEDECODE is already in the array
                }
            } else {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage(
                        "stream.could.not.be.compressed.filter.is.not.a.getName.or.array"));
            }
        }
        // Update filter to include FLATEDECODE if not present
        updateFilter();
    }

    private void updateFilter() {
        PdfObject existingFilter = PdfReader.getPdfObject(get(PdfName.FILTER));
        if (existingFilter == null) {
            put(PdfName.FILTER, PdfName.FLATEDECODE);
        } else {
            PdfArray filters;
            if (existingFilter.isArray()) {
                filters = (PdfArray) existingFilter;
            } else {
                filters = new PdfArray(existingFilter);
            }
            filters.add(PdfName.FLATEDECODE);
            put(PdfName.FILTER, filters);
        }
    }

    private void performCompression(int compressionLevel) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(compressionLevel);
            try (DeflaterOutputStream zip = new DeflaterOutputStream(stream, deflater)) {
                if (streamBytes != null) {
                    streamBytes.writeTo(zip);
                } else {
                    zip.write(bytes);
                }
            }
            deflater.end();
            // Update the object with compressed data
            streamBytes = stream;
            bytes = null;
            put(PdfName.PDF_NAME_LENGTH, new PdfNumber(streamBytes.size()));
            compressed = true;
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }


    protected void superToPdf(PdfWriter writer, OutputStream os) throws IOException {
        super.toPdf(writer, os);
    }

    /**
     * @see com.lowagie.text.pdf.PdfDictionary#toPdf(com.lowagie.text.pdf.PdfWriter, java.io.OutputStream)
     */
    @Override
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        handleCompression();
        PdfEncryption crypto = getEncryption(writer);
        PdfObject lengthObject = get(PdfName.PDF_NAME_LENGTH);

        if (crypto != null && lengthObject != null && lengthObject.isNumber()) {
            updateStreamLength(crypto, lengthObject, writer, os);
        } else {
            superToPdf(writer, os);
        }

        os.write(STARTSTREAM);
        if (inputStream != null) {
            processInputStream(os, crypto);
        } else {
            processBytes(os, crypto);
        }
        os.write(ENDSTREAM);
    }

    private void handleCompression() {
        if (inputStream != null && compressed) {
            put(PdfName.FILTER, PdfName.FLATEDECODE);
        }
    }

    private PdfEncryption getEncryption(PdfWriter writer) {
        if (writer == null) {
            return null;
        }

        PdfEncryption crypto = writer.getEncryption();
        PdfObject filter = get(PdfName.FILTER);
        if (filter != null && isEncryptionFilter(filter)) {
            return null;
        }
        return crypto;
    }

    private boolean isEncryptionFilter(PdfObject filter) {
        if (PdfName.CRYPT.equals(filter)) {
            return true;
        }
        if (filter.isArray()) {
            PdfArray array = (PdfArray) filter;
            return !array.isEmpty() && PdfName.CRYPT.equals(array.getPdfObject(0));
        }
        return false;
    }

    private void updateStreamLength(PdfEncryption crypto, PdfObject lengthObject, PdfWriter writer, OutputStream os) throws IOException {
        int originalSize = ((PdfNumber) lengthObject).intValue();
        put(PdfName.PDF_NAME_LENGTH, new PdfNumber(crypto.calculateStreamSize(originalSize)));
        superToPdf(writer, os); // Use actual writer and os
        put(PdfName.PDF_NAME_LENGTH, lengthObject);
    }

    private void processInputStream(OutputStream os, PdfEncryption crypto) throws IOException {
        try (DeflaterOutputStream deflaterStream = createDeflaterStream(os, crypto);
                OutputStreamCounter counterStream = new OutputStreamCounter(deflaterStream)) {

            byte[] buffer = new byte[4192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                deflaterStream.write(buffer, 0, bytesRead);
            }
            deflaterStream.finish();
            inputStreamLength = counterStream.getCounter();
        }
    }

    private DeflaterOutputStream createDeflaterStream(OutputStream os, PdfEncryption crypto){
        OutputStream outputStream = (crypto != null && !crypto.isEmbeddedFilesOnly())
                ? crypto.getEncryptionStream(os)
                : os;
        if (compressed) {
            Deflater deflater = new Deflater(compressionLevel);
            return new DeflaterOutputStream(outputStream, deflater, 0x8000);
        }
        return new DeflaterOutputStream(outputStream);
    }

    private void processBytes(OutputStream os, PdfEncryption crypto) throws IOException {
        if (crypto != null && !crypto.isEmbeddedFilesOnly()) {
            byte[] encryptedBytes = (streamBytes != null)
                    ? crypto.encryptByteArray(streamBytes.toByteArray())
                    : crypto.encryptByteArray(bytes);
            os.write(encryptedBytes);
        } else {
            if (streamBytes != null) {
                streamBytes.writeTo(os);
            } else {
                os.write(bytes);
            }
        }
    }


    /**
     * Writes the data content to an <CODE>OutputStream</CODE>.
     *
     * @param os the destination to write to
     * @throws IOException on error
     */
    public void writeContent(OutputStream os) throws IOException {
        if (streamBytes != null) {
            streamBytes.writeTo(os);
        } else if (bytes != null) {
            os.write(bytes);
        }
    }

    /**
     * @see com.lowagie.text.pdf.PdfObject#toString()
     */
    @Override
    public String toString() {
        if (get(PdfName.TYPE_CONST) == null) {
            return "Stream";
        }
        return "Stream of getTypeImpl: " + get(PdfName.TYPE_CONST);
    }
}
