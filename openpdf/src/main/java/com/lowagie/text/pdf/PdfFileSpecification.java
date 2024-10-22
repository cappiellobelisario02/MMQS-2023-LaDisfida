/*
 * Copyright 2003 Paulo Soares
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

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.collection.PdfCollectionItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Specifies a file or a URL. The file can be extern or embedded.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfFileSpecification extends PdfDictionary {

    protected PdfWriter writer;
    protected PdfIndirectReference ref;

    private static final Logger logger = Logger.getLogger(PdfFileSpecification.class.getName());

    /**
     * Creates a new instance of PdfFileSpecification. The static methods are preferred.
     */
    public PdfFileSpecification() {
        super(PdfName.FILESPEC);
    }

    /**
     * Creates a file specification of getTypeImpl URL.
     *
     * @param writer the <CODE>PdfWriter</CODE>
     * @param url    the URL
     * @return the file specification
     */
    public static PdfFileSpecification url(PdfWriter writer, String url) {
        PdfFileSpecification fs = new PdfFileSpecification();
        fs.writer = writer;
        fs.put(PdfName.FS, PdfName.URL);
        fs.put(PdfName.F, new PdfString(url));
        return fs;
    }

    /**
     * Creates a file specification with the file embedded. The file may come from the file system or from a byte array.
     * The data is flate compressed.
     *
     * @param writer      the <CODE>PdfWriter</CODE>
     * @param filePath    the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore   the byte array with the file. If it is not <CODE>null</CODE> it takes precedence over
     *                    <CODE>filePath</CODE>
     * @return the file specification
     * @throws IOException on error
     */
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay,
            byte[] fileStore) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, PdfStream.BEST_COMPRESSION);
    }

    /**
     * Creates a file specification with the file embedded. The file may come from the file system or from a byte array.
     * The data is flate compressed.
     *
     * @param writer           the <CODE>PdfWriter</CODE>
     * @param filePath         the file path
     * @param fileDisplay      the file information that is presented to the user
     * @param fileStore        the byte array with the file. If it is not <CODE>null</CODE> it takes precedence over
     *                         <CODE>filePath</CODE>
     * @param compressionLevel the compression level to be used for compressing the file it takes precedence over
     *                         <CODE>filePath</CODE>
     * @return the file specification
     * @throws IOException on error
     * @since 2.1.3
     */
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay,
            byte[] fileStore, int compressionLevel) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null, compressionLevel);
    }


    /**
     * Creates a file specification with the file embedded. The file may come from the file system or from a byte
     * array.
     *
     * @param writer      the <CODE>PdfWriter</CODE>
     * @param filePath    the file path
     * @param fileDisplay the file information that is presented to the user
     * @param fileStore   the byte array with the file. If it is not <CODE>null</CODE> it takes precedence over
     *                    <CODE>filePath</CODE>
     * @param compress    sets the compression on the data. Multimedia content will benefit little from compression
     * @return the file specification
     * @throws IOException on error
     */
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay,
            byte[] fileStore, boolean compress) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, null, null,
                compress ? PdfStream.BEST_COMPRESSION : PdfStream.NO_COMPRESSION);
    }

    /**
     * Creates a file specification with the file embedded. The file may come from the file system or from a byte
     * array.
     *
     * @param writer        the <CODE>PdfWriter</CODE>
     * @param filePath      the file path
     * @param fileDisplay   the file information that is presented to the user
     * @param fileStore     the byte array with the file. If it is not <CODE>null</CODE> it takes precedence over
     *                      <CODE>filePath</CODE>
     * @param compress      sets the compression on the data. Multimedia content will benefit little from compression
     * @param mimeType      the optional mimeType
     * @param fileParameter the optional extra file parameters such as the creation or modification date
     * @return the file specification
     * @throws IOException on error
     */
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay,
            byte[] fileStore, boolean compress, String mimeType, PdfDictionary fileParameter) throws IOException {
        return fileEmbedded(writer, filePath, fileDisplay, fileStore, mimeType, fileParameter,
                compress ? PdfStream.BEST_COMPRESSION : PdfStream.NO_COMPRESSION);
    }

    /**
     * Creates a file specification with the file embedded. The file may come from the file system or from a byte
     * array.
     *
     * @param writer           the <CODE>PdfWriter</CODE>
     * @param filePath         the file path
     * @param fileDisplay      the file information that is presented to the user
     * @param fileStore        the byte array with the file. If it is not <CODE>null</CODE> it takes precedence over
     *                         <CODE>filePath</CODE>
     * @param mimeType         the optional mimeType
     * @param fileParameter    the optional extra file parameters such as the creation or modification date
     * @param compressionLevel the level of compression
     * @return the file specification
     * @throws IOException on error
     * @since 2.1.3
     */
    public static PdfFileSpecification fileEmbedded(PdfWriter writer, String filePath, String fileDisplay,
            byte[] fileStore, String mimeType, PdfDictionary fileParameter, int compressionLevel) throws IOException {
        PdfFileSpecification fs = fileExtern(writer, fileDisplay);
        PdfEFStream stream = createPdfEFStream(writer, filePath, fileStore, compressionLevel);
        PdfDictionary param = createFileParameter(fileParameter, fileStore, stream);
        addOptionalMimeType(mimeType, stream);

        PdfIndirectReference ref = writer.addToBody(stream).getIndirectReference();
        if (fileStore == null) {
            handleFileStore(writer, stream, param);
        }

        return createPdfFileSpecification(fs, ref);
    }

    private static PdfEFStream createPdfEFStream(PdfWriter writer, String filePath, byte[] fileStore, int compressionLevel) throws IOException {
        InputStream in = null;
        try {
            if (fileStore != null) {
                return new PdfEFStream(fileStore);
            }

            in = openInputStream(filePath);
            PdfEFStream stream = new PdfEFStream(in, writer);
            stream.flateCompress(compressionLevel);
            return stream;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    // Log exception (could use a logging framework)
                    logger.severe("Exception occured");
                }
            }
        }
    }

    private static InputStream openInputStream(String filePath) throws IOException {
        if (filePath.startsWith("file:/") || filePath.startsWith("http://") || filePath.startsWith("https://") || filePath.startsWith("jar:")) {
            return new URL(filePath).openStream();
        }

        File file = new File(filePath);
        if (file.canRead()) {
            return new FileInputStream(filePath);
        }

        InputStream in = BaseFont.getResourceStream(filePath);
        if (in == null) {
            throw new IOException(MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filePath));
        }
        return in;
    }

    private static PdfDictionary createFileParameter(PdfDictionary fileParameter, byte[] fileStore, PdfEFStream stream) {
        PdfDictionary param = new PdfDictionary();
        if (fileParameter != null) {
            param.merge(fileParameter);
        }
        if (fileStore != null) {
            param.put(PdfName.SIZE, new PdfNumber(stream.getRawLength()));
            stream.put(PdfName.PARAMS, param);
        }
        return param;
    }

    private static void addOptionalMimeType(String mimeType, PdfEFStream stream) {
        if (mimeType != null) {
            stream.put(PdfName.SUBTYPE, new PdfName(mimeType));
        }
    }

    private static void handleFileStore(PdfWriter writer, PdfEFStream stream, PdfDictionary param) throws IOException {
        PdfIndirectReference refFileLength = writer.getPdfIndirectReference();
        stream.writeLength();
        param.put(PdfName.SIZE, new PdfNumber(stream.getRawLength()));
        writer.addToBody(param, refFileLength);
    }

    private static PdfFileSpecification createPdfFileSpecification(PdfFileSpecification fs, PdfIndirectReference ref) {
        PdfDictionary f = new PdfDictionary();
        f.put(PdfName.F, ref);
        f.put(PdfName.UF, ref);
        fs.put(PdfName.EF, f);
        return fs;
    }


    /**
     * Creates a file specification for an external file.
     *
     * @param writer   the <CODE>PdfWriter</CODE>
     * @param filePath the file path
     * @return the file specification
     */
    public static PdfFileSpecification fileExtern(PdfWriter writer, String filePath) {
        PdfFileSpecification fs = new PdfFileSpecification();
        fs.writer = writer;
        fs.put(PdfName.F, new PdfString(filePath));
        fs.setUnicodeFileName(filePath, !PdfEncodings.isPdfDocEncoding(filePath));
        return fs;
    }

    /**
     * Gets the indirect reference to this file specification. Multiple invocations will retrieve the same value.
     *
     * @return the indirect reference
     * @throws IOException on error
     */
    public PdfIndirectReference getReference() throws IOException {
        if (ref != null) {
            return ref;
        }
        ref = writer.addToBody(this).getIndirectReference();
        return ref;
    }

    /**
     * Sets the file getName (the key /F) string as an hex representation to support multi byte file names. The getName must
     * have the slash and backslash escaped according to the file specification rules
     *
     * @param fileName the file getName as a byte array
     */
    public void setMultiByteFileName(byte[] fileName) {
        put(PdfName.F, new PdfString(fileName).setHexWriting(true));
    }

    /**
     * Adds the unicode file getName (the key /UF). This entry was introduced in PDF 1.7. The filename must have the slash
     * and backslash escaped according to the file specification rules.
     *
     * @param filename the filename
     * @param unicode  if true, the filename is UTF-16BE encoded; otherwise PDFDocEncoding is used;
     */
    public void setUnicodeFileName(String filename, boolean unicode) {
        put(PdfName.UF, new PdfString(filename, unicode ? PdfObject.TEXT_UNICODE : PdfObject.TEXT_PDFDOCENCODING));
    }

    /**
     * Sets a flag that indicates whether an external file referenced by the file specification is volatile. If the
     * value is true, applications should never cache a copy of the file.
     *
     * @param volatileFile if true, the external file should not be cached
     */
    public void setVolatile(boolean volatileFile) {
        put(PdfName.V, new PdfBoolean(volatileFile));
    }

    /**
     * Adds a description for the file that is specified here.
     *
     * @param description some text
     * @param unicode     if true, the text is added as a unicode string
     */
    public void addDescription(String description, boolean unicode) {
        put(PdfName.DESC, new PdfString(description, unicode ? PdfObject.TEXT_UNICODE : PdfObject.TEXT_PDFDOCENCODING));
    }

    /**
     * Adds the Collection item dictionary.
     *
     * @param ci the PdfCollectionItem to add
     */
    public void addCollectionItem(PdfCollectionItem ci) {
        put(PdfName.CI, ci);
    }
}
