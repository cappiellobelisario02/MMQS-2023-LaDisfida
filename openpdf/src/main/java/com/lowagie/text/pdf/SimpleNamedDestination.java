/*
 * Copyright 2004 by Paulo Soares.
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.TagException;
import com.lowagie.text.xml.XMLUtil;
import com.lowagie.text.xml.simpleparser.IanaEncodings;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;

/**
 * @author Paulo Soares (psoares@consiste.pt)
 */
public final class SimpleNamedDestination implements SimpleXMLDocHandler {

    private HashMap<String, String> xmlNames;
    private Map<String, String> xmlLast;
    private static final Logger logger = Logger.getLogger(SimpleNamedDestination.class.getName());

    private SimpleNamedDestination() {
    }

    @SuppressWarnings("unchecked")
    public static HashMap<Object, Object> getNamedDestination(PdfReader reader, boolean fromNames) {
        IntHashtable pages = new IntHashtable();
        int numPages = reader.getNumberOfPages();
        for (int k = 1; k <= numPages; ++k) {
            pages.put(reader.getPageOrigRef(k).getNumber(), k);
        }
        HashMap<?, ?> names = fromNames ? (HashMap<Object, PdfObject>) reader.getNamedDestinationFromNames()
                : (HashMap<String, PdfObject>) reader.getNamedDestinationFromStrings();
        for (Iterator<? extends Entry<?, ?>> it = names.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Object, Object> entry = (Entry<Object, Object>) it.next();
            PdfArray arr = (PdfArray) entry.getValue();
            StringBuilder s = new StringBuilder();
            try {
                s.append(pages.get(arr.getAsIndirectObject(0).getNumber()));
                s.append(' ').append(arr.getPdfObject(1).toString().substring(1));
                for (int k = 2; k < arr.size(); ++k) {
                    s.append(' ').append(arr.getPdfObject(k).toString());
                }
                entry.setValue(s.toString());
            } catch (NullPointerException e) {
                it.remove();
            }
        }
        return (HashMap<Object, Object>) names;
    }

    /**
     * Exports the destinations to XML. The DTD for this XML is:
     * <pre>
     * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
     * &lt;!ELEMENT Name (#PCDATA)&gt;
     * &lt;!ATTLIST Name
     *    Page CDATA #IMPLIED
     * &gt;
     * &lt;!ELEMENT Destination (Name)*&gt;
     * </pre>
     *
     * @param names     the names
     * @param out       the export destination. The stream is not closed
     * @param encoding  the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>, whatever the
     *                  encoding
     * @throws IOException on error
     */
    public static void exportToXML(HashMap<?,?> names, OutputStream out, String encoding, boolean onlyASCII)
            throws IOException {
        String jenc = IanaEncodings.getJavaEncoding(encoding);
        Writer wrt = new BufferedWriter(new OutputStreamWriter(out, jenc));
        exportToXML(names, wrt, encoding, onlyASCII);
    }

    /**
     * Exports the destinations to XML.
     *
     * @param names     the names
     * @param wrt       the export destination. The writer is not closed
     * @param encoding  the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>, whatever the
     *                  encoding
     * @throws IOException on error
     */
    public static void exportToXML(HashMap<?,?> names, Writer wrt, String encoding, boolean onlyASCII) throws IOException {
        wrt.write("<?xml version=\"1.0\" encoding=\"");
        wrt.write(XMLUtil.escapeXML(encoding, onlyASCII));
        wrt.write("\"?>\n<Destination>\n");
        for (Entry<?,?> o : names.entrySet()) {
            String key = (String) o.getKey();
            String value = (String) o.getValue();
            wrt.write("  <Name Page=\"");
            wrt.write(XMLUtil.escapeXML(value, onlyASCII));
            wrt.write("\">");
            wrt.write(XMLUtil.escapeXML(escapeBinaryString(key), onlyASCII));
            wrt.write("</Name>\n");
        }
        wrt.write("</Destination>\n");
        wrt.flush();
    }

    /**
     * Import the names from XML.
     *
     * @param in the XML source. The stream is not closed
     * @return the names
     * @throws IOException on error
     */
    public static HashMap<String, String> importFromXML(InputStream in) throws IOException {
        SimpleNamedDestination names = new SimpleNamedDestination();
        SimpleXMLParser.parse(names, in);
        return names.xmlNames;
    }

    /**
     * Import the names from XML.
     *
     * @param in the XML source. The reader is not closed
     * @return the names
     * @throws IOException on error
     */
    public static HashMap<String, String> importFromXML(Reader in) throws IOException {
        SimpleNamedDestination names = new SimpleNamedDestination();
        SimpleXMLParser.parse(names, in);
        return names.xmlNames;
    }

    static PdfArray createDestinationArray(String value, PdfWriter writer) {
        PdfArray ar = new PdfArray();
        StringTokenizer tk = new StringTokenizer(value);
        int n = Integer.parseInt(tk.nextToken());
        ar.add(writer.getPageReference(n));
        if (!tk.hasMoreTokens()) {
            ar.add(PdfName.XYZ);
            ar.add(new float[]{0, 10000, 0});
        } else {
            String fn = tk.nextToken();
            if (fn.startsWith("/")) {
                fn = fn.substring(1);
            }
            ar.add(new PdfName(fn));
            for (int k = 0; k < 4 && tk.hasMoreTokens(); ++k) {
                fn = tk.nextToken();
                if (fn.equals("null")) {
                    ar.add(PdfNull.PDFNULL);
                } else {
                    ar.add(new PdfNumber(fn));
                }
            }
        }
        return ar;
    }

    public static PdfDictionary outputNamedDestinationAsNames(HashMap<?, ?> names, PdfWriter writer) {
        PdfDictionary dic = new PdfDictionary();

        // Stream through the entries and handle potential nulls before they cause exceptions
        names.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof String) // Filter non-string keys/values
                .map(entry -> (Entry<String, String>) entry) // Cast safely after filtering
                .forEachOrdered(entry -> {
                    String key = entry.getKey(); // No longer need to check for null
                    String value = entry.getValue(); // No longer need to check for null
                    PdfArray ar = createDestinationArray(value, writer);
                    PdfName kn = new PdfName(key);
                    dic.put(kn, ar);
                });

        return dic;
    }


    public static PdfDictionary outputNamedDestinationAsStrings(Map<String, String> names, PdfWriter writer)
            throws IOException {
        Map<String, PdfIndirectReference> n2 = new HashMap<>();
        for (Iterator<Map.Entry<String, String>> it = names.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            try {
                String value = entry.getValue();
                PdfArray ar = createDestinationArray(value, writer);
                n2.put(entry.getKey(), writer.addToBody(ar).getIndirectReference());
            } catch (NullPointerException e) {
                it.remove();
            }
        }
        return PdfNameTree.writeTree(n2, writer);
    }

    public static String escapeBinaryString(String s) {
        StringBuilder buf = new StringBuilder();
        char[] cc = s.toCharArray();
        for (char c : cc) {
            if (c < ' ') {
                buf.append('\\');
                String octal = "00" + Integer.toOctalString(c);
                buf.append(octal.substring(octal.length() - 3));
            } else if (c == '\\') {
                buf.append("\\\\");
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String unEscapeBinaryString(String s) {
        StringBuilder buf = new StringBuilder();
        char[] chars = s.toCharArray();
        int length = chars.length;
        int index = 0;

        while (index < length) {
            char currentChar = chars[index];
            if (currentChar == '\\') {
                index++;
                if (index < length) {
                    char nextChar = chars[index];
                    if (isOctalDigit(nextChar)) {
                        buf.append(handleOctalEscape(chars, length, index));
                        // Skip the characters processed in handleOctalEscape
                        index += getOctalEscapeLength(chars, length, index) - 1;
                    } else {
                        buf.append(nextChar);
                    }
                } else {
                    buf.append('\\');
                }
            } else {
                buf.append(currentChar);
            }
            index++;
        }
        return buf.toString();
    }

    private static boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }

    private static char handleOctalEscape(char[] chars, int length, int index) {
        int value = chars[index] - '0';
        index++;
        for (int i = 0; i < 2 && index < length; i++) {
            char c = chars[index];
            if (isOctalDigit(c)) {
                value = value * 8 + c - '0';
                index++;
            } else {
                break;
            }
        }
        return (char) value;
    }

    private static int getOctalEscapeLength(char[] chars, int length, int index) {
        int count = 0;
        for (int i = 0; i < 2 && index + i < length; i++) {
            if (isOctalDigit(chars[index + i])) {
                count++;
            } else {
                break;
            }
        }
        return count + 1; // Including the first octal digit
    }


    public void endDocument() {
        //empty on purpose (for now)
    }

    public void endElement(String tag) {
        if (tag.equals("Destination")) {
            if (xmlLast == null && xmlNames != null) {
                return;
            } else {
                throw new TagException(MessageLocalization.getComposedMessage("destination.end.tag.out.of.place"));
            }
        }
        if (!tag.equals("Name")) {
            throw new TagException(MessageLocalization.getComposedMessage("invalid.end.tag.1", tag));
        }
        if (xmlLast == null || xmlNames == null) {
            throw new TagException(MessageLocalization.getComposedMessage("getName.end.tag.out.of.place"));
        }
        if (!xmlLast.containsKey("Page")) {
            throw new TagException(MessageLocalization.getComposedMessage("page.attribute.missing"));
        }
        xmlNames.put(unEscapeBinaryString(xmlLast.get("Name")), xmlLast.get("Page"));
        xmlLast = null;
    }

    public void startDocument() {
        //empty on purpose (for now)
    }

    public void startElement(String tag, Map<String, String> h) {
        if (xmlNames == null) {
            if (tag.equals("Destination")) {
                xmlNames = new HashMap<>();
                return;
            } else {
                throw new TagException(MessageLocalization.getComposedMessage("root.element.is.not.destination"));
            }
        }
        if (!tag.equals("Name")) {
            throw new TagException(MessageLocalization.getComposedMessage("tag.1.not.allowed", tag));
        }
        if (xmlLast != null) {
            throw new TagException(MessageLocalization.getComposedMessage("nested.tags.are.not.allowed"));
        }
        xmlLast = new HashMap<>(h);
        xmlLast.put("Name", "");
    }

    public void text(String str) {
        if (xmlLast == null) {
            return;
        }
        String name = xmlLast.get("Name");
        name += str;
        xmlLast.put("Name", name);
    }
}
