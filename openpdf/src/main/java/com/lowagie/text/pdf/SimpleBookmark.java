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

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.TagException;
import com.lowagie.text.xml.XMLUtil;
import com.lowagie.text.xml.simpleparser.IanaEncodings;
import com.lowagie.text.xml.simpleparser.SimpleXMLDocHandler;
import com.lowagie.text.xml.simpleparser.SimpleXMLParser;
import org.apache.fop.fo.pagination.bookmarks.Bookmark;
import java.awt.print.Book;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * Bookmark processing in a simple way. It has some limitations, mainly the only action types supported are GoTo, GoToR,
 * URI and Launch.
 * <p>
 * The list structure is composed by a number of HashMap, keyed by strings, one HashMap for each bookmark. The element
 * values are all strings with the exception of the key "Kids" that has another list for the child bookmarks.
 * <p>
 * All the bookmarks have a "Title" with the bookmark title and optionally a "Style" that can be "bold", "italic" or a
 * combination of both. They can also have a "Color" key with a value of three floats separated by spaces. The key
 * "Open" can have the values "true" or "false" and signals the open status of the children. It's "true" by default.
 * <p>
 * The actions and the parameters can be:
 * <ul>
 * <li>"Action" = "GoTo" - "Page" | "Named"
 * <ul>
 * <li>"Page" = "3 XYZ 70 400 null" - page number followed by a destination (/XYZ is also accepted)
 * <li>"Named" = "named_destination"
 * </ul>
 * <li>"Action" = "GoToR" - "Page" | "Named" | "NamedN", "File", ["NewWindow"]
 * <ul>
 * <li>"Page" = "3 XYZ 70 400 null" - page number followed by a destination (/XYZ is also accepted)
 * <li>"Named" = "named_destination_as_a_string"
 * <li>"NamedN" = "named_destination_as_a_name"
 * <li>"File" - "the_file_to_open"
 * <li>"NewWindow" - "true" or "false"
 * </ul>
 * <li>"Action" = "URI" - "URI"
 * <ul>
 * <li>"URI" = "http://sf.net" - URI to jump to
 * </ul>
 * <li>"Action" = "Launch" - "File"
 * <ul>
 * <li>"File" - "the_file_to_open_or_execute"
 * </ul>
 * </ul>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public final class SimpleBookmark implements SimpleXMLDocHandler {

    public static final String TITLE = "Title";
    public static final String FALSE = "false";
    public static final String ACTION = "Action";
    public static final String NAMED = "Named";
    public static final String NAMED_N = "NamedN";
    private final Deque<Map<String, Object>> attr = new ArrayDeque<>();
    private List<Map<String, Object>> topList;

    /**
     * Creates a new instance of SimpleBookmark
     */
    private SimpleBookmark() {
    }

    private static List<Map<String, Object>> bookmarkDepth(PdfDictionary outline, IntHashtable pages) {
        List<Map<String, Object>> list = new ArrayList<>();
        while (outline != null) {
            Map<String, Object> map = new HashMap<>();
            processTitle(outline, map);
            processColor(outline, map);
            processStyle(outline, map);
            processCount(outline, map);
            processDestinationAndAction(outline, map, pages);
            processFirstChild(outline, map, pages);
            list.add(map);
            outline = (PdfDictionary) PdfReader.getPdfObjectRelease(outline.get(PdfName.NEXT));
        }
        return list;
    }

    private static void processTitle(PdfDictionary outline, Map<String, Object> map) {
        PdfString title = (PdfString) PdfReader.getPdfObjectRelease(outline.get(PdfName.TITLE));
        map.put(TITLE, (title != null) ? title.toUnicodeString() : "");
    }

    private static void processColor(PdfDictionary outline, Map<String, Object> map) {
        PdfArray color = (PdfArray) PdfReader.getPdfObjectRelease(outline.get(PdfName.C));
        if (color != null && color.size() == 3) {
            String colorStr = String.format("%f %f %f",
                    color.getAsNumber(0).floatValue(),
                    color.getAsNumber(1).floatValue(),
                    color.getAsNumber(2).floatValue());
            map.put("Color", colorStr);
        }
    }

    private static void processStyle(PdfDictionary outline, Map<String, Object> map) {
        PdfNumber style = (PdfNumber) PdfReader.getPdfObjectRelease(outline.get(PdfName.F));
        if (style != null) {
            int f = style.intValue();
            StringBuilder s = new StringBuilder();
            if ((f & 1) != 0) s.append("italic ");
            if ((f & 2) != 0) s.append("bold ");
            if (s.length() > 0) map.put("Style", s.toString().trim());
        }
    }

    private static void processCount(PdfDictionary outline, Map<String, Object> map) {
        PdfNumber count = (PdfNumber) PdfReader.getPdfObjectRelease(outline.get(PdfName.COUNT));
        if (count != null && count.intValue() < 0) {
            map.put("Open", FALSE); // Assuming FALSE is a string "false"
        }
    }

    private static void processDestinationAndAction(PdfDictionary outline, Map<String, Object> map, IntHashtable pages) {
        try {
            PdfObject dest = PdfReader.getPdfObjectRelease(outline.get(PdfName.DEST));
            if (dest != null) {
                mapGotoBookmark(map, dest, pages);
            } else {
                PdfDictionary action = (PdfDictionary) PdfReader.getPdfObjectRelease(outline.get(PdfName.A));
                if (action != null) {
                    processAction(action, map, pages);
                }
            }
        } catch (Exception e) {
            // Log or handle exception if necessary
        }
    }

    private static void processAction(PdfDictionary action, Map<String, Object> map, IntHashtable pages) {
        PdfName actionType = (PdfName) PdfReader.getPdfObjectRelease(action.get(PdfName.S));
        if (PdfName.GOTO.equals(actionType)) {
            PdfObject dest = PdfReader.getPdfObjectRelease(action.get(PdfName.D));
            if (dest != null) mapGotoBookmark(map, dest, pages);
        } else if (PdfName.URI.equals(actionType)) {
            map.put(ACTION, "URI");
            PdfString uri = (PdfString) PdfReader.getPdfObjectRelease(action.get(PdfName.URI));
            if (uri != null) map.put("URI", uri.toUnicodeString());
        } else if (PdfName.GOTOR.equals(actionType)) {
            processGoToR(action, map);
        } else if (PdfName.LAUNCH.equals(actionType)) {
            processLaunch(action, map);
        }
    }

    private static void processGoToR(PdfDictionary action, Map<String, Object> map) {
        PdfObject dest = PdfReader.getPdfObjectRelease(action.get(PdfName.D));
        if (dest != null) {
            if (dest.isString()) {
                map.put(NAMED, dest.toString());
            } else if (dest.isName()) {
                map.put(NAMED_N, PdfName.decodeName(dest.toString()));
            } else if (dest.isArray()) {
                map.put("Page", convertArrayToString((PdfArray) dest));
            }
        }
        map.put(ACTION, "GoToR");
        processFile(action, map);
        processNewWindow(action, map);
    }

    private static String convertArrayToString(PdfArray arr) {
        StringBuilder s = new StringBuilder();
        s.append(arr.getPdfObject(0).toString());
        s.append(' ').append(arr.getPdfObject(1).toString());
        for (int k = 2; k < arr.size(); ++k) {
            s.append(' ').append(arr.getPdfObject(k).toString());
        }
        return s.toString();
    }

    private static void processLaunch(PdfDictionary action, Map<String, Object> map) {
        map.put(ACTION, "Launch");
        processFile(action, map);
    }

    private static void processFile(PdfDictionary action, Map<String, Object> map) {
        PdfObject file = PdfReader.getPdfObjectRelease(action.get(PdfName.F));
        if (file == null) {
            file = PdfReader.getPdfObjectRelease(action.get(PdfName.WIN));
        }
        if (file != null) {
            if (file.isString()) {
                map.put("File", ((PdfString) file).toUnicodeString());
            } else if (file.isDictionary()) {
                file = PdfReader.getPdfObjectRelease(((PdfDictionary) file).get(PdfName.F));
                if (file.isString()) {
                    map.put("File", ((PdfString) file).toUnicodeString());
                }
            }
        }
    }

    private static void processNewWindow(PdfDictionary action, Map<String, Object> map) {
        PdfObject newWindow = PdfReader.getPdfObjectRelease(action.get(PdfName.NEWWINDOW));
        if (newWindow != null) {
            map.put("NewWindow", newWindow.toString());
        }
    }

    private static void processFirstChild(PdfDictionary outline, Map<String, Object> map, IntHashtable pages) {
        PdfDictionary first = (PdfDictionary) PdfReader.getPdfObjectRelease(outline.get(PdfName.FIRST));
        if (first != null) {
            map.put("Kids", bookmarkDepth(first, pages));
        }
    }


    private static void mapGotoBookmark(Map<String, Object> map, PdfObject dest, IntHashtable pages) {
        if (dest.isString()) {
            map.put(NAMED, dest.toString());
        } else if (dest.isName()) {
            map.put(NAMED, PdfName.decodeName(dest.toString()));
        } else if (dest.isArray()) {
            map.put("Page", makeBookmarkParam((PdfArray) dest, pages)); //changed by ujihara 2004-06-13
        }
        map.put(ACTION, "GoTo");
    }

    private static String makeBookmarkParam(PdfArray dest, IntHashtable pages) {
        StringBuilder s = new StringBuilder();
        if (dest.size() == 0) {
            throw new IllegalArgumentException("Illegal bookmark destination");
        }
        PdfObject obj = dest.getPdfObject(0);
        if (obj.isNumber()) {
            s.append(((PdfNumber) obj).intValue() + 1);
        } else {
            s.append(pages.get(getNumber((PdfIndirectReference) obj))); //changed by ujihara 2004-06-13
        }
        s.append(' ').append(dest.getPdfObject(1).toString().substring(1));
        for (int k = 2; k < dest.size(); ++k) {
            s.append(' ').append(dest.getPdfObject(k).toString());
        }
        return s.toString();
    }

    /**
     * Gets number of indirect. If type of directed indirect is PAGES, it refers PAGE object through KIDS. (Contributed
     * by Kazuya Ujihara)
     *
     * @param indirect 2004-06-13
     */
    private static int getNumber(PdfIndirectReference indirect) {
        PdfDictionary pdfObj = (PdfDictionary) PdfReader.getPdfObjectRelease(indirect);
        if (pdfObj.contains(PdfName.TYPE) && pdfObj.get(PdfName.TYPE).equals(PdfName.PAGES) && pdfObj.contains(
                PdfName.KIDS)) {
            PdfArray kids = (PdfArray) pdfObj.get(PdfName.KIDS);
            indirect = (PdfIndirectReference) kids.getPdfObject(0);
        }
        return indirect.getNumber();
    }

    /**
     * Gets a <CODE>List</CODE> with the bookmarks. It returns <CODE>null</CODE> if the document doesn't have any
     * bookmarks.
     *
     * @param reader the document
     * @return a <CODE>List</CODE> with the bookmarks or <CODE>null</CODE> if the document doesn't have any
     */
    public static List<Map<String, Object>> getBookmarkList(PdfReader reader) {
        PdfDictionary catalog = reader.getCatalog();
        PdfObject obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.OUTLINES));
        if (obj == null || !obj.isDictionary()) {
            return Collections.emptyList();
        }
        PdfDictionary outlines = (PdfDictionary) obj;
        IntHashtable pages = new IntHashtable();
        int numPages = reader.getNumberOfPages();
        for (int k = 1; k <= numPages; ++k) {
            pages.put(reader.getPageOrigRef(k).getNumber(), k);
            reader.releasePage(k);
        }
        return bookmarkDepth(
                (PdfDictionary) PdfReader.getPdfObjectRelease(outlines.get(PdfName.FIRST)), pages);
    }

    /**
     * Removes the bookmark entries for a number of page ranges. The page ranges consists of a number of pairs with the
     * start/end page range. The page numbers are inclusive.
     *
     * @param list      the bookmarks
     * @param pageRange the page ranges, always in pairs.
     */
    public static void eliminatePages(List<Map<String, Object>> list, int[] pageRange) {
        if (list == null) {
            return;
        }

        Iterator<Map<String, Object>> it = list.listIterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            boolean hit = shouldEliminatePage(map, pageRange);

            handleKids(map, pageRange);

            if (hit) {
                removeOrCleanMap(it, map);
            }
        }
    }

    private static boolean shouldEliminatePage(Map<String, Object> map, int[] pageRange) {
        if (!"GoTo".equals(map.get(ACTION))) {
            return false;
        }

        String page = (String) map.get("Page");
        if (page == null) {
            return false;
        }

        int pageNum = extractPageNumber(page.trim());
        return isPageInRange(pageNum, pageRange);
    }

    private static int extractPageNumber(String page) {
        int idx = page.indexOf(' ');
        return idx < 0 ? Integer.parseInt(page) : Integer.parseInt(page.substring(0, idx));
    }

    private static boolean isPageInRange(int pageNum, int[] pageRange) {
        int len = pageRange.length & 0xfffffffe;
        for (int k = 0; k < len; k += 2) {
            if (pageNum >= pageRange[k] && pageNum <= pageRange[k + 1]) {
                return true;
            }
        }
        return false;
    }

    private static void handleKids(Map<String, Object> map, int[] pageRange) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> kids = (List<Map<String, Object>>) map.get("Kids");
        if (kids != null) {
            eliminatePages(kids, pageRange);
            if (kids.isEmpty()) {
                map.remove("Kids");
            }
        }
    }

    private static void removeOrCleanMap(Iterator<Map<String, Object>> it, Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> kids = (List<Map<String, Object>>) map.get("Kids");
        if (kids == null) {
            it.remove();
        } else {
            map.remove(ACTION);
            map.remove("Page");
            map.remove(NAMED);
        }
    }


    static void createOutlineAction(PdfDictionary outline, Map<String, Object> map, PdfWriter writer, boolean namedAsNames) {
        try {
            String action = (String) map.get(ACTION);

            switch (action) {
                case "GoTo":
                    handleGoToAction(outline, map, writer, namedAsNames);
                    break;
                case "GoToR":
                    handleGoToRAction(outline, map, writer);
                    break;
                case "URI":
                    handleUriAction(outline, map);
                    break;
                case "Launch":
                    handleLaunchAction(outline, map);
                    break;
                default:
                    // Handle unknown actions or do nothing
                    break;
            }
        } catch (Exception e) {
            // empty on purpose
        }
    }

    private static void handleGoToAction(PdfDictionary outline, Map<String, Object> map, PdfWriter writer, boolean namedAsNames) {
        String p = (String) map.get(NAMED);
        if (p != null) {
            outline.put(PdfName.DEST, namedAsNames ? new PdfName(p) : new PdfString(p, null));
            return;
        }

        p = (String) map.get("Page");
        if (p != null) {
            PdfArray ar = createPageDestinationArray(p, writer);
            outline.put(PdfName.DEST, ar);
        }
    }

    private static void handleGoToRAction(PdfDictionary outline, Map<String, Object> map, PdfWriter writer) {
        PdfDictionary dic = new PdfDictionary();

        String p = (String) map.get(NAMED);
        if (p != null) {
            dic.put(PdfName.D, new PdfString(p, null));
        } else {
            p = (String) map.get(NAMED_N);
            if (p != null) {
                dic.put(PdfName.D, new PdfName(p));
            } else {
                p = (String) map.get("Page");
                if (p != null) {
                    PdfArray ar = createPageDestinationArray(p, writer);
                    dic.put(PdfName.D, ar);
                }
            }
        }

        String file = (String) map.get("File");
        if (dic.size() > 0 && file != null) {
            dic.put(PdfName.S, PdfName.GOTOR);
            dic.put(PdfName.F, new PdfString(file));
            String nw = (String) map.get("NewWindow");
            if (nw != null) {
                dic.put(PdfName.NEWWINDOW, "true".equals(nw) ? PdfBoolean.PDFTRUE : PdfBoolean.PDFFALSE);
            }
            outline.put(PdfName.A, dic);
        }
    }

    private static void handleUriAction(PdfDictionary outline, Map<String, Object> map) {
        String uri = (String) map.get("URI");
        if (uri != null) {
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.S, PdfName.URI);
            dic.put(PdfName.URI, new PdfString(uri));
            outline.put(PdfName.A, dic);
        }
    }

    private static void handleLaunchAction(PdfDictionary outline, Map<String, Object> map) {
        String file = (String) map.get("File");
        if (file != null) {
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.S, PdfName.LAUNCH);
            dic.put(PdfName.F, new PdfString(file));
            outline.put(PdfName.A, dic);
        }
    }

    private static PdfArray createPageDestinationArray(String p, PdfWriter writer) {
        PdfArray ar = new PdfArray();
        StringTokenizer tk = new StringTokenizer(p);
        ar.add(writer.getPageReference(Integer.parseInt(tk.nextToken())));

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
                ar.add("null".equals(fn) ? PdfNull.PDFNULL : new PdfNumber(fn));
            }
        }

        return ar;
    }


    public static Object[] iterateOutlines(PdfWriter writer, PdfIndirectReference parent,
            List<Map<String, Object>> kids, boolean namedAsNames) throws IOException {

        // Gestisci il caso in cui la lista kids è vuota
        if (kids == null || kids.isEmpty()) {
            return new Object[]{null, null, 0}; // Array vuoto, nessun riferimento e conteggio a 0
        }

        PdfIndirectReference[] refs = new PdfIndirectReference[kids.size()];
        for (int k = 0; k < refs.length; ++k) {
            refs[k] = writer.getPdfIndirectReference();
        }

        int ptr = 0;
        int count = 0;
        for (Iterator<Map<String, Object>> it = kids.listIterator(); it.hasNext(); ++ptr) {
            Map<String, Object> map = it.next();
            Object[] lower = null;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> subKid = (List<Map<String, Object>>) map.get("Kids");
            if (subKid != null && !subKid.isEmpty()) {
                lower = iterateOutlines(writer, refs[ptr], subKid, namedAsNames);
            }
            PdfDictionary outline = createOutline(writer, parent, refs, ptr, map, lower);
            writer.addToBody(outline, refs[ptr]);
            count += extractCount(lower, map);
        }

        // Verifica che refs abbia almeno un elemento prima di restituirlo
        return new Object[]{refs.length > 0 ? refs[0] : null, refs.length > 0 ? refs[refs.length - 1] : null, count};
    }



    private static PdfDictionary createOutline(PdfWriter writer, PdfIndirectReference parent,
            PdfIndirectReference[] refs, int ptr, Map<String, Object> map, Object[] lower) {
        PdfDictionary outline = new PdfDictionary();
        if (lower != null) {
            outline.put(PdfName.FIRST, (PdfIndirectReference) lower[0]);
            outline.put(PdfName.LAST, (PdfIndirectReference) lower[1]);
        }
        outline.put(PdfName.PARENT, parent);
        if (ptr > 0) {
            outline.put(PdfName.PREV, refs[ptr - 1]);
        }
        if (ptr < refs.length - 1) {
            outline.put(PdfName.NEXT, refs[ptr + 1]);
        }
        outline.put(PdfName.TITLE, new PdfString((String) map.get(TITLE), PdfObject.TEXT_UNICODE));
        addColor(outline, map);
        addStyle(outline, map);
        createOutlineAction(outline, map, writer, false); // Assuming `namedAsNames` is `false` by default
        return outline;
    }

    private static void addColor(PdfDictionary outline, Map<String, Object> map) {
        String color = (String) map.get("Color");
        if (color != null) {
            try {
                PdfArray arr = parseColor(color);
                outline.put(PdfName.C, arr);
            } catch (Exception ignored) {
                // In case it's malformed
            }
        }
    }

    private static PdfArray parseColor(String color) {
        PdfArray arr = new PdfArray();
        StringTokenizer tk = new StringTokenizer(color);
        for (int k = 0; k < 3; ++k) {
            float f = Float.parseFloat(tk.nextToken());
            f = Math.max(0, Math.min(1, f)); // Clamp the value between 0 and 1
            arr.add(new PdfNumber(f));
        }
        return arr;
    }

    private static void addStyle(PdfDictionary outline, Map<String, Object> map) {
        String style = (String) map.get("Style");
        if (style != null) {
            style = style.toLowerCase();
            int bits = 0;
            if (style.contains("italic")) {
                bits |= 1;
            }
            if (style.contains("bold")) {
                bits |= 2;
            }
            if (bits != 0) {
                outline.put(PdfName.F, new PdfNumber(bits));
            }
        }
    }

    private static int extractCount(Object[] lower, Map<String, Object> map) {
        if (lower != null) {
            int n = (Integer) lower[2];
            if (FALSE.equals(map.get("Open"))) {
                return -n;
            } else {
                return n;
            }
        }
        return 0;
    }


    /**
     * Exports the bookmarks to XML. Only of use if the generation is to be include in some other XML document.
     *
     * @param list      the bookmarks
     * @param out       the export destination. The writer is not closed
     * @param indent    the indentation level. Pretty printing significant only
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>, whatever the
     *                  encoding
     * @throws IOException on error
     */
    public static void exportToXMLNode(List<Bookmark> list, Writer out, int indent, boolean onlyASCII) throws IOException {
        String indentation = createIndentation(indent);
        Map<String, Bookmark> map = new HashMap<>();

        for (Bookmark item : list) {
            map.put(item.getBookmarkTitle(), item);
        }

        List<Bookmark> kids = processMapEntries(map, out, indentation, onlyASCII); // Note this is now outside the loop
        writeTitleAndChildren(out, map, indentation, kids, indent, onlyASCII);
    }

    private static String createIndentation(int indent) {
        return "  ".repeat(Math.max(0, indent));
    }

    private static List<Bookmark> processMapEntries(Map<String, Bookmark> map, Writer out, String indentation, boolean onlyASCII) throws IOException {
        List<Bookmark> kids = new ArrayList<>();

        out.write(indentation);
        out.write("<Title ");

        for (Map.Entry<String, Bookmark> entry : map.entrySet()) {
            String key = entry.getKey();
            Bookmark value = entry.getValue(); // value is of type Bookmark

            switch (key) {
                case TITLE:
                    // Extract the bookmark title as a string
                    writeAttribute(out, key, value.getBookmarkTitle(), onlyASCII);
                    break;
                case "Kids":
                    kids.add(value); // Add the Bookmark to kids if needed
                    break;
                default:
                    // Extract the appropriate attribute value as a string
                    String attributeValue = getAttributeValueFromBookmark(value, key);
                    writeAttribute(out, key, attributeValue, onlyASCII);
                    break;
            }
        }

        out.write(">");
        return kids;
    }

    // Helper method to get the attribute value based on the key
    private static String getAttributeValueFromBookmark(Bookmark bookmark, String key) {
        // Implement logic to return the correct attribute value based on the key
        switch (key) {
            case "someAttribute":
                return bookmark.getSomeAttribute(); // Replace with actual method
            // Add other cases as needed
            default:
                return ""; // Return default or handle as needed
        }
    }


    private static void writeAttribute(Writer out, String key, String value, boolean onlyASCII) throws IOException {
        out.write(key);
        out.write("=\"");

        if (key.equals(NAMED) || key.equals(NAMED_N)) {
            value = SimpleNamedDestination.escapeBinaryString(value);
        }

        out.write(XMLUtil.escapeXML(value, onlyASCII));
        out.write("\" ");
    }


    private static void writeTitleAndChildren(Writer out, Map<String, String> map, String indentation, List<Bookmark> kids, int indent, boolean onlyASCII) throws IOException {
        String title = map.getOrDefault(TITLE, ""); // TITLE should be a String constant
        out.write(XMLUtil.escapeXML(title, onlyASCII));

        if (kids != null) {
            out.write("\n");
            exportToXMLNode(kids, out, indent + 1, onlyASCII);
            out.write(indentation);
        }

        out.write("</Title>\n");
    }



    /**
     * Exports the bookmarks to XML. The DTD for this XML is:
     * <pre>
     * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
     * &lt;!ELEMENT Title (#PCDATA|Title)*&gt;
     * &lt;!ATTLIST Title
     *    Action CDATA #IMPLIED
     *    Open CDATA #IMPLIED
     *    Page CDATA #IMPLIED
     *    URI CDATA #IMPLIED
     *    File CDATA #IMPLIED
     *    Named CDATA #IMPLIED
     *    NamedN CDATA #IMPLIED
     *    NewWindow CDATA #IMPLIED
     *    Style CDATA #IMPLIED
     *    Color CDATA #IMPLIED
     * &gt;
     * &lt;!ELEMENT Bookmark (Title)*&gt;
     * </pre>
     *
     * @param list      the bookmarks
     * @param out       the export destination. The stream is not closed
     * @param encoding  the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>, whatever the
     *                  encoding
     * @throws IOException on error
     */
    public static void exportToXML(List<?> list, OutputStream out, String encoding, boolean onlyASCII) throws IOException {
        String jenc = IanaEncodings.getJavaEncoding(encoding);
        Writer wrt = new BufferedWriter(new OutputStreamWriter(out, jenc));
        exportToXML(list, wrt, encoding, onlyASCII);
    }

    /**
     * Exports the bookmarks to XML.
     *
     * @param list      the bookmarks
     * @param wrt       the export destination. The writer is not closed
     * @param encoding  the encoding according to IANA conventions
     * @param onlyASCII codes above 127 will always be escaped with &amp;#nn; if <CODE>true</CODE>, whatever the
     *                  encoding
     * @throws IOException on error
     */
    public static void exportToXML(List<Bookmark> list, Writer wrt, String encoding, boolean onlyASCII) throws IOException {
        wrt.write("<?xml version=\"1.0\" encoding=\"");
        wrt.write(XMLUtil.escapeXML(encoding, onlyASCII));
        wrt.write("\"?>\n<Bookmark>\n");
        exportToXMLNode(list, wrt, 1, onlyASCII); // Now this call will work since list is of type List<Bookmark>
        wrt.write("</Bookmark>\n");
        wrt.flush();
    }


    /**
     * Import the bookmarks from XML.
     *
     * @param in the XML source. The stream is not closed
     * @return the bookmarks
     * @throws IOException on error
     */
    public static List<Map<String, Object>> importFromXML(InputStream in) throws IOException {
        SimpleBookmark book = new SimpleBookmark();
        SimpleXMLParser.parse(book, in);
        return book.topList;
    }

    /**
     * Import the bookmarks from XML.
     *
     * @param in the XML source. The reader is not closed
     * @return the bookmarks
     * @throws IOException on error
     */
    public static List<Map<String, Object>> importFromXML(Reader in) throws IOException {
        SimpleBookmark book = new SimpleBookmark();
        SimpleXMLParser.parse(book, in);
        return book.topList;
    }

    public void endDocument() {
        //empty on purpose
    }

    public void endElement(String tag) {
        if (tag.equals("Bookmark")) {
            if (attr.isEmpty()) {
                return;
            } else {
                throw new TagException(MessageLocalization.getComposedMessage("bookmark.end.tag.out.of.place"));
            }
        }
        if (!tag.equals(TITLE)) {
            throw new TagException(MessageLocalization.getComposedMessage("invalid.end.tag.1", tag));
        }
        Map<String, Object> attributes = attr.pop();
        String title = (String) attributes.get(TITLE);
        attributes.put(TITLE, title.trim());
        String named = (String) attributes.get(NAMED);
        if (named != null) {
            attributes.put(NAMED, SimpleNamedDestination.unEscapeBinaryString(named));
        }
        named = (String) attributes.get(NAMED_N);
        if (named != null) {
            attributes.put(NAMED_N, SimpleNamedDestination.unEscapeBinaryString(named));
        }
        if (attr.isEmpty()) {
            topList.add(attributes);
        } else {
            Map<String, Object> parent = attr.peek();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> kids = (List<Map<String, Object>>) parent.get("Kids");
            if (kids == null) {
                kids = new ArrayList<>();
                parent.put("Kids", kids);
            }
            kids.add(attributes);
        }
    }

    public void startDocument() {
        //empty on purpose
    }

    public void startElement(String tag, Map<String, String> h) {
        if (topList == null) {
            if (tag.equals("Bookmark")) {
                topList = new ArrayList<>();
                return;
            } else {
                throw new TagException(
                        MessageLocalization.getComposedMessage("root.element.is.not.bookmark.1", tag));
            }
        }
        if (!tag.equals(TITLE)) {
            throw new TagException(MessageLocalization.getComposedMessage("tag.1.not.allowed", tag));
        }
        Map<String, Object> attributes = new HashMap<>(h);
        attributes.put(TITLE, "");
        attributes.remove("Kids");
        attr.push(attributes);
    }

    public void text(String str) {
        if (attr.isEmpty()) {
            return;
        }
        Map<String, Object> attributes = attr.peek();
        String title = (String) attributes.get(TITLE);
        title += str;
        attributes.put(TITLE, title);
    }
}
