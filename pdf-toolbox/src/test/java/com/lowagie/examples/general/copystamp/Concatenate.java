/*
 * $Id: Concatenate.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
 * This class demonstrates copying a PDF file using iText.
 *
 * @author Mark Thompson
 */
package com.lowagie.examples.general.copystamp;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import org.apache.commons.io.FilenameUtils;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tool that can be used to concatenate existing PDF files.
 */
public class Concatenate {

    /**
     * This class can be used to concatenate existing PDF files. (This was an example known as PdfCopy.java)
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("arguments: file1 [file2 ...] destfile");
            return;
        }

        System.out.println("PdfCopy example");
        String outFile = args[args.length - 1];

        try {
            int pageOffset = 0;
            List<Map<String, Object>> master = new ArrayList<>();
            Document document = null;
            PdfCopy writer = null;

            for (int f = 0; f < args.length - 1; f++) {
                PdfReader reader = new PdfReader(args[f]);
                reader.consolidateNamedDestinations();

                int n = reader.getNumberOfPages();
                List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);
                if (bookmarks != null) {
                    if (pageOffset != 0) {
                        SimpleBookmark.shiftPageNumbersInRange(bookmarks, pageOffset, null);
                    }
                    master.addAll(bookmarks);
                }
                pageOffset += n;

                if (f == 0) {
                    document = new Document(reader.getPageSizeWithRotation(1));
                    String outfilePath = FilenameUtils.normalize(outFile);
                    writer = new PdfCopy(document, new FileOutputStream(outfilePath));
                    document.open();
                }

                for (int i = 1; i <= n; i++) {
                    PdfImportedPage page = writer.getImportedPage(reader, i);
                    writer.addPage(page);
                }

                PRAcroForm form = reader.getAcroForm();
                if (form != null) {
                    writer.copyAcroForm(reader);
                }
            }

            if (!master.isEmpty()) {
                writer.setOutlines(master);
            }

            document.close();
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

}
