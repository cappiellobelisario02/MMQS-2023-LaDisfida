/*
 * $Id: PdfEFStream.java 3735 2009-02-26 01:44:03Z xlv $
 *
 * Copyright (c) 2008 by Bruno Lowagie
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Extends PdfStream and should be used to create Streams for Embedded Files (file attachments).
 *
 * @since 2.1.3
 */

public class PdfEFStream extends PdfStream {

    /**
     * Creates a Stream object using an InputStream and a PdfWriter object
     *
     * @param in     the InputStream that will be read to get the Stream object
     * @param writer the writer to which the stream will be added
     */
    public PdfEFStream(InputStream in, PdfWriter writer) {
        super(in, writer);
    }

    /**
     * Creates a Stream object using a byte array
     *
     * @param fileStore the bytes for the stream
     */
    public PdfEFStream(byte[] fileStore) {
        super(fileStore);
    }

    /**
     * @see com.lowagie.text.pdf.PdfDictionary#toPdf(com.lowagie.text.pdf.PdfWriter, java.io.OutputStream)
     */
    @Override
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        PdfEncryption crypto = prepareEncryption(writer);
        configureFilterAndDecodeParams(crypto);

        PdfObject lengthObject = get(PdfName.PDF_NAME_LENGTH);
        if (crypto != null && lengthObject != null && lengthObject.isNumber()) {
            adjustLengthForEncryption(crypto, lengthObject);
        } else {
            superToPdf(writer, os);
        }

        os.write(STARTSTREAM);
        if (inputStream != null) {
            writeStreamWithEncryptionAndCompression(os, crypto);
        } else {
            writeBytesToOutputStream(os, crypto);
        }
        os.write(ENDSTREAM);
    }

    private PdfEncryption prepareEncryption(PdfWriter writer) {
        PdfEncryption crypto = null;
        if (writer != null) {
            crypto = writer.getEncryption();
        }
        if (crypto != null) {
            PdfObject filter = get(PdfName.FILTER);
            if (filter != null && shouldNullifyCrypto(filter)) {
                crypto = null;
            }
        }
        return crypto;
    }

    private boolean shouldNullifyCrypto(PdfObject filter) {
        if (PdfName.CRYPT.equals(filter)) {
            return true;
        } else if (filter.isArray()) {
            PdfArray array = (PdfArray) filter;
            return !array.isEmpty() && PdfName.CRYPT.equals(array.getPdfObject(0));
        }
        return false;
    }

    private void configureFilterAndDecodeParams(PdfEncryption crypto) {
        if (crypto != null && crypto.isEmbeddedFilesOnly()) {
            PdfArray filter = new PdfArray();
            PdfArray decodeParams = new PdfArray();
            PdfDictionary cryptDict = new PdfDictionary();
            cryptDict.put(PdfName.NAME, PdfName.STDCF);
            filter.add(PdfName.CRYPT);
            decodeParams.add(cryptDict);
            if (compressed) {
                filter.add(PdfName.FLATEDECODE);
                decodeParams.add(new PdfNull());
            }
            put(PdfName.FILTER, filter);
            put(PdfName.DECODEPARMS, decodeParams);
        }
    }

    private void adjustLengthForEncryption(PdfEncryption crypto, PdfObject lengthObject) throws IOException {
        int size = ((PdfNumber) lengthObject).intValue();
        put(PdfName.PDF_NAME_LENGTH, new PdfNumber(crypto.calculateStreamSize(size)));
        superToPdf(null, null); // Assuming superToPdf does not need writer and os here
        put(PdfName.PDF_NAME_LENGTH, lengthObject);
    }

    private void writeStreamWithEncryptionAndCompression(OutputStream os, PdfEncryption crypto) throws IOException {
        rawLength = 0;
        try (OutputStreamCounter osc = new OutputStreamCounter(os);
                DeflaterOutputStream deflaterStream = createDeflaterStream(crypto, osc)) {

            byte[] buf = new byte[4192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                deflaterStream.write(buf, 0, bytesRead);
                rawLength += bytesRead;
            }
            deflaterStream.finish();
            if (crypto != null) {
                crypto.getEncryptionStream(osc).finish();
            }
            inputStreamLength = osc.getCounter();
        }
    }

    private DeflaterOutputStream createDeflaterStream(PdfEncryption crypto, OutputStreamCounter osc) {
        OutputStream stream = osc;
        if (crypto != null) {
            stream = crypto.getEncryptionStream(stream);
        }
        if (compressed) {
            Deflater deflater = new Deflater(compressionLevel);
            return new DeflaterOutputStream(stream, deflater, 0x8000);
        }
        return new DeflaterOutputStream(stream);
    }

    private void writeBytesToOutputStream(OutputStream os, PdfEncryption crypto) throws IOException {
        if (crypto == null) {
            if (streamBytes != null) {
                streamBytes.writeTo(os);
            } else {
                os.write(bytes);
            }
        } else {
            byte[] encryptedBytes = (streamBytes != null)
                    ? crypto.encryptByteArray(streamBytes.toByteArray())
                    : crypto.encryptByteArray(bytes);
            os.write(encryptedBytes);
        }
    }

}
