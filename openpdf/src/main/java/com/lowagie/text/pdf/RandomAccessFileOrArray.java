/*
 * $Id: RandomAccessFileOrArray.java 4065 2009-09-16 23:09:11Z psoares33 $
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

import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 * An implementation of a RandomAccessFile for input only that accepts a file or a byte array as data source.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class RandomAccessFileOrArray implements DataInput, Closeable {

    private static final Logger logger = Logger.getLogger(RandomAccessFileOrArray.class.getName());

    MappedRandomAccessFile rf;
    RandomAccessFile trf;
    boolean plainRandomAccess;
    String filename;
    byte[] arrayIn;
    int arrayInPtr;
    byte back;
    boolean isBack = false;

    /**
     * Holds value of property startOffset.
     */
    private int startOffset = 0;

    public RandomAccessFileOrArray(String filename) throws IOException {
        this(filename, false, Document.PLAIN_RANDOM_ACCESS);
    }

    public RandomAccessFileOrArray(String filename, boolean forceRead, boolean plainRandomAccess) throws IOException {
        this.plainRandomAccess = plainRandomAccess;

        // Normalizza il nome del file per garantire compatibilità tra sistemi operativi
        filename = filename.replace("/", File.separator).replace("\\", File.separator);

        // Converte il nome del file in un oggetto Path per una gestione migliore
        Path filePath = Paths.get(filename).normalize();
        File file = validatePath(filePath.toString());

        if (!file.exists()) {
            // Se il file non esiste, prova a risolvere il nome del file
            String resolvedFilename = tryResolveFilename(filename);
            filePath = Paths.get(resolvedFilename).normalize();
            file = validatePath(filePath.toString());  // Riconvalida il nome del file risolto
        }

        // Controlla se il percorso è valido e non è una potenziale vulnerabilità
        if (!isValidFilePath(file)) {
            throw new IOException("Invalid file path: " + filename);
        }

        if (!file.canRead()) {
            handleUnreadableFile(filename);
        } else if (forceRead) {
            readFileToArray(file);
        } else {
            this.filename = filename;
            openRandomAccessFile(filePath.toString(), plainRandomAccess);
        }
    }

    private boolean isValidFilePath(File file) {
        try {
            // Ensure the file is within an expected directory
            File canonicalFile = file.getCanonicalFile();
            File expectedDirectory = new File("expected/directory"); // Specify your base directory
            return canonicalFile.getPath().startsWith(expectedDirectory.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }


    // Helper method to validate and sanitize file paths
    private File validatePath(String path) throws IOException {
        File file = new File(path);

        // Prevent directory traversal attacks by checking the canonical path
        String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.startsWith(new File(".").getCanonicalPath())) {
            throw new SecurityException("Path manipulation attempt detected: " + path);
        }

        return file;
    }


    private String tryResolveFilename(String filename) {
        if (FontFactory.isRegistered(filename)) {
            return (String) FontFactory.getFontImp().getFontPath(filename);
        }
        return filename;
    }

    private void handleUnreadableFile(String filename) throws IOException {
        if (isRemoteResource(filename)) {
            // Validate the URL to prevent unsafe resource access
            if (isValidUrl(filename)) {
                try (InputStream is = new URL(filename).openStream()) {
                    this.arrayIn = inputStreamToArray(is);
                }
            } else {
                throw new MalformedURLException("Invalid URL: " + filename);
            }
        } else {
            readFromInputStream(filename);
        }
    }

    // Helper method to validate the URL
    private boolean isValidUrl(String url) {
        try {
            URL u = new URL(url);
            // Check the protocol to ensure it's a safe one
            return "http".equalsIgnoreCase(u.getProtocol()) || "https".equalsIgnoreCase(u.getProtocol());
        } catch (MalformedURLException e) {
            return false;
        }
    }


    private boolean isRemoteResource(String filename) {
        // Convalida se il percorso è assoluto
        Path path = Paths.get(filename).normalize();

        // Verifica se il percorso contiene protocolli remoti sicuri
        return path.toString().startsWith("file:/") ||
                path.toString().startsWith("http://") ||
                path.toString().startsWith("https://") ||
                path.toString().startsWith("jar:") ||
                path.toString().startsWith("wsjar:");
    }

    private void readFromInputStream(String filename) throws IOException {
        InputStream is = getResourceStream(filename);
        if (is == null) {
            throw new IOException(
                    MessageLocalization.getComposedMessage("1.not.found.as.file.or.resource", filename));
        }
        try {
            this.arrayIn = inputStreamToArray(is);
        } finally {
            closeStream(is);
        }
    }

    private InputStream getResourceStream(String filename) {
        if ("-".equals(filename)) {
            return System.in;
        } else {
            return BaseFont.getResourceStream(filename);
        }
    }

    private void closeStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException ioe) {
            logger.severe("Exception occured");
        }
    }

    private void readFileToArray(File file) throws IOException {
        try (InputStream s = new FileInputStream(file)) {
            this.arrayIn = inputStreamToArray(s);
        }
    }

    private void openRandomAccessFile(String filename, boolean plainRandomAccess) throws IOException {
        String filenamePathSecure = FilenameUtils.normalize(filename);

        if (plainRandomAccess) {
            trf = new RandomAccessFile(filenamePathSecure, "r");
        } else {
            rf = new MappedRandomAccessFile(filenamePathSecure, "r");
        }
    }


    public RandomAccessFileOrArray(URL url) throws IOException {
        try (InputStream is = url.openStream()) {
            this.arrayIn = inputStreamToArray(is);
        }
    }

    public RandomAccessFileOrArray(InputStream is) throws IOException {
        this.arrayIn = inputStreamToArray(is);
    }

    public RandomAccessFileOrArray(byte[] arrayIn) {
        this.arrayIn = arrayIn;
    }

    public RandomAccessFileOrArray(RandomAccessFileOrArray file) {
        filename = file.filename;
        arrayIn = file.arrayIn;
        startOffset = file.startOffset;
        plainRandomAccess = file.plainRandomAccess;
    }

    public static byte[] inputStreamToArray(InputStream is) throws IOException {
        byte[] b = new byte[8192];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while (true) {
                int read = is.read(b);
                if (read < 1) {
                    break;
                }
                out.write(b, 0, read);
            }
        } catch (IOException e) {
            // Log a generic error message without revealing sensitive information
            logger.warning("An error occurred while reading the input stream.");
            throw e; // Rethrow the exception after logging
        } finally {
            out.close();
        }
        return out.toByteArray();
    }

    public void pushBack(byte b) {
        back = b;
        isBack = true;
    }

    public int read() throws IOException {
        if (isBack) {
            isBack = false;
            return back & 0xff;
        }
        if (arrayIn == null) {
            return plainRandomAccess ? trf.read() : rf.read();
        } else {
            if (arrayInPtr >= arrayIn.length) {
                return -1;
            }
            return arrayIn[arrayInPtr++] & 0xff;
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int n = 0;
        if (isBack) {
            isBack = false;
            if (len == 1) {
                b[off] = back;
                return 1;
            } else {
                n = 1;
                b[off++] = back;
                --len;
            }
        }
        if (arrayIn == null) {
            return (plainRandomAccess ? trf.read(b, off, len) : rf.read(b, off, len)) + n;
        } else {
            if (arrayInPtr >= arrayIn.length) {
                return -1;
            }
            if (arrayInPtr + len > arrayIn.length) {
                len = arrayIn.length - arrayInPtr;
            }
            System.arraycopy(arrayIn, arrayInPtr, b, off, len);
            arrayInPtr += len;
            return len + n;
        }
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        } while (n < len);
    }

    public long skip(long n) throws IOException {
        return skipBytes((int) n);
    }

    public int skipBytes(int n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        int adj = 0;
        if (isBack) {
            isBack = false;
            if (n == 1) {
                return 1;
            } else {
                --n;
                adj = 1;
            }
        }
        int pos;
        int len;
        int newpos;

        pos = getFilePointer();
        len = length();
        newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);

        /* return the actual number of bytes skipped */
        return newpos - pos + adj;
    }

    public void reOpen() throws IOException {
        if (filename != null && rf == null && trf == null) {
            if (plainRandomAccess) {
                trf = new RandomAccessFile(filename, "r");
            } else {
                rf = new MappedRandomAccessFile(filename, "r");
            }
        }
        seek(0);
    }

    protected void insureOpen() throws IOException {
        if (filename != null && rf == null && trf == null) {
            reOpen();
        }
    }

    public boolean isOpen() {
        return (filename == null || rf != null || trf != null);
    }

    public void close() throws IOException {
        isBack = false;
        if (rf != null) {
            rf.close();
            rf = null;
            // it's very expensive to open a memory mapped file and for the usage pattern of this class
            // in iText it's faster the next re-openings to be done as plain random access
            // file
            plainRandomAccess = true;
        } else if (trf != null) {
            trf.close();
            trf = null;
        }
    }

    public int length() throws IOException {
        if (arrayIn == null) {
            insureOpen();
            return (int) (plainRandomAccess ? trf.length() : rf.length()) - startOffset;
        } else {
            return arrayIn.length - startOffset;
        }
    }

    public void seek(int pos) throws IOException {
        pos += startOffset;
        isBack = false;
        if (arrayIn == null) {
            insureOpen();
            if (plainRandomAccess) {
                trf.seek(pos);
            } else {
                rf.seek(pos);
            }
        } else {
            arrayInPtr = pos;
        }
    }

    public void seek(long pos) throws IOException {
        seek((int) pos);
    }

    public int getFilePointer() throws IOException {
        insureOpen();
        int n = isBack ? 1 : 0;
        if (arrayIn == null) {
            return (int) (plainRandomAccess ? trf.getFilePointer() : rf.getFilePointer()) - n - startOffset;
        } else {
            return arrayInPtr - n - startOffset;
        }
    }

    public boolean readBoolean() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    public byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte) (ch);
    }

    public int readUnsignedByte() throws IOException {
        int ch = this.read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    public short readShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch1 << 8) + ch2);
    }

    /**
     * Reads a signed 16-bit number from this stream in little-endian order. The method reads two bytes from this
     * stream, starting at the current stream pointer. If the two bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where each of the two values is
     * between <code>0</code> and <code>255</code>, inclusive, then the result is equal to:
     * <blockquote><pre>
     *     (short)((b2 &lt;&lt; 8) | b1)
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next two bytes of this stream, interpreted as a signed 16-bit number.
     * @throws EOFException if this stream reaches the end before reading two bytes.
     * @throws IOException  if an I/O error occurs.
     */
    public final short readShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (short) ((ch2 << 8) + (ch1));
    }

    public int readUnsignedShort() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch1 << 8) + ch2;
    }

    /**
     * Reads an unsigned 16-bit number from this stream in little-endian order. This method reads two bytes from the
     * stream, starting at the current stream pointer. If the bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b2 &lt;&lt; 8) | b1
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next two bytes of this stream, interpreted as an unsigned 16-bit integer.
     * @throws EOFException if this stream reaches the end before reading two bytes.
     * @throws IOException  if an I/O error occurs.
     */
    public final int readUnsignedShortLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch2 << 8) + (ch1);
    }

    public char readChar() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (char) ((ch1 << 8) + ch2);
    }

    /**
     * Reads a Unicode character from this stream in little-endian order. This method reads two bytes from the stream,
     * starting at the current stream pointer. If the bytes read, in order, are
     * <code>b1</code> and <code>b2</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1,&nbsp;b2&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (char)((b2 &lt;&lt; 8) | b1)
     * </pre></blockquote>
     * <p>
     * This method blocks until the two bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next two bytes of this stream as a Unicode character.
     * @throws EOFException if this stream reaches the end before reading two bytes.
     * @throws IOException  if an I/O error occurs.
     */
    public final char readCharLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (char) ((ch2 << 8) + (ch1));
    }

    public int readInt() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4);
    }

    /**
     * Reads a signed 32-bit integer from this stream in little-endian order. This method reads 4 bytes from the stream,
     * starting at the current stream pointer. If the bytes read, in order, are <code>b1</code>,
     * <code>b2</code>, <code>b3</code>, and <code>b4</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b4 &lt;&lt; 24) | (b3 &lt;&lt; 16) + (b2 &lt;&lt; 8) + b1
     * </pre></blockquote>
     * <p>
     * This method blocks until the four bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next four bytes of this stream, interpreted as an
     * <code>int</code>.
     * @throws EOFException if this stream reaches the end before reading four bytes.
     * @throws IOException  if an I/O error occurs.
     */
    public final int readIntLE() throws IOException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1));
    }

    /**
     * Reads an unsigned 32-bit integer from this stream. This method reads 4 bytes from the stream, starting at the
     * current stream pointer. If the bytes read, in order, are <code>b1</code>,
     * <code>b2</code>, <code>b3</code>, and <code>b4</code>, where
     * <code>0&nbsp;&lt;=&nbsp;b1, b2, b3, b4&nbsp;&lt;=&nbsp;255</code>,
     * then the result is equal to:
     * <blockquote><pre>
     *     (b1 &lt;&lt; 24) | (b2 &lt;&lt; 16) + (b3 &lt;&lt; 8) + b4
     * </pre></blockquote>
     * <p>
     * This method blocks until the four bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @return the next four bytes of this stream, interpreted as a
     * <code>long</code>.
     * @throws EOFException if this stream reaches the end before reading four bytes.
     * @throws IOException  if an I/O error occurs.
     */
    public final long readUnsignedInt() throws IOException {
        long ch1 = this.read();
        long ch2 = this.read();
        long ch3 = this.read();
        long ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    public final long readUnsignedIntLE() throws IOException {
        long ch1 = this.read();
        long ch2 = this.read();
        long ch3 = this.read();
        long ch4 = this.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0) {
            throw new EOFException();
        }
        return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1));
    }

    public long readLong() throws IOException {
        return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    public final long readLongLE() throws IOException {
        int i1 = readIntLE();
        int i2 = readIntLE();
        return ((long) i2 << 32) + (i1 & 0xFFFFFFFFL);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final float readFloatLE() throws IOException {
        return Float.intBitsToFloat(readIntLE());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final double readDoubleLE() throws IOException {
        return Double.longBitsToDouble(readLongLE());
    }

    public String readLine() throws IOException {
        StringBuilder input = new StringBuilder();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            c = read();
            switch (c) {
                case -1, '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    int cur = getFilePointer();
                    if ((read()) != '\n') {
                        seek(cur);
                    }
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.isEmpty())) {
            return null;
        }
        return input.toString();
    }

    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    /**
     * Getter for property startOffset.
     *
     * @return Value of property startOffset.
     */
    public int getStartOffset() {
        return this.startOffset;
    }

    /**
     * Setter for property startOffset.
     *
     * @param startOffset New value of property startOffset.
     */
    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    /**
     * @return a ByteBuffer
     * @throws IOException on error
     * @since 2.0.8
     */
    public java.nio.ByteBuffer getNioByteBuffer() throws IOException {
        if (filename != null) {
            FileChannel channel;
            if (plainRandomAccess) {
                channel = trf.getChannel();
            } else {
                channel = rf.getChannel();
            }
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
        return java.nio.ByteBuffer.wrap(arrayIn);
    }
}
