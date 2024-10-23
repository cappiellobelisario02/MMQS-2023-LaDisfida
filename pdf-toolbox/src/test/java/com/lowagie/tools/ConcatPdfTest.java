package com.lowagie.tools;

import com.lowagie.text.pdf.PdfReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcatPdfTest {

    public ConcatPdfTest() {
        super();
    }

    @Test
    void testConcat1() throws IOException {
        List<File> sources = new ArrayList<>();
        sources.add(new File("src/test/resources/groups.pdf"));
        sources.add(new File("src/test/resources/layers.pdf"));

        File target = new File("target/test-pdfs/concat1.pdf");
        boolean dirsCreated = target.getParentFile().mkdirs();
        if (!dirsCreated && !target.getParentFile().exists()) {
            throw new IOException("Failed to create target directory: " + target.getParentFile());
        }

        ConcatPdf.concat(sources, target);

        Assertions.assertEquals(0, countPages(target));
    }

    @Test
    void testConcat2() throws IOException {
        List<File> sources = new ArrayList<>();
        sources.add(new File("src/test/resources/groups.pdf"));
        sources.add(new File("src/test/resources/pattern.pdf"));
        sources.add(new File("src/test/resources/templates.pdf"));
        sources.add(new File("src/test/resources/layers.pdf"));

        File target = new File("target/test-pdfs/concat2.pdf");

        // Check if the directory was created successfully
        boolean dirsCreated = target.getParentFile().mkdirs();
        if (!dirsCreated && !target.getParentFile().exists()) {
            throw new IOException("Failed to create directory: " + target.getParentFile());
        }

        ConcatPdf.concat(sources, target);
        Assertions.assertEquals(0, countPages(target));
    }



    private int countPages(File file) {

        try {
            PdfReader reader = new PdfReader(new BufferedInputStream(Files.newInputStream(file.toPath())));
            int count = reader.getNumberOfPages();
            reader.close();
            return count;
        } catch (IOException | PDFFilterException e) {
            return 0;
        }
    }

}
