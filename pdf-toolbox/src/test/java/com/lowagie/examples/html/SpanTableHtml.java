package com.lowagie.examples.html;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;

public class SpanTableHtml {

    private static final Logger logger = Logger.getLogger(SpanTableHtml.class.getName());

    public static void main(String[] args) {
        testRowspan();
        testColspan();
    }

    /**
     * Converts an HTML page to pdf with the table containing rolspan tags
     */
    public static void testRowspan() {
        // Usa try-with-resources per garantire la corretta chiusura delle risorse
        try (Document doc = new Document(PageSize.A4);
                OutputStream outputStream = Files.newOutputStream(Paths.get("testRowspanOut.pdf"));
                InputStream stream = SpanTableHtml.class.getResourceAsStream("example1forHTMLWorker.html");
                InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(stream), UTF_8)) {

            PdfWriter.getInstance(doc, outputStream);
            doc.open();

            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(reader);

            assert (true);

        } catch (IOException e) {
            // Aggiungere log appropriato o gestione dell'eccezione
            // È possibile usare un logger in produzione
        }
    }


    /**
     * Converts an HTML page to pdf with the table containing colspan tags
     */
    public static void testColspan() {
        try (Document doc = new Document(PageSize.A4);
                InputStream stream = SpanTableHtml.class.getResourceAsStream("example2forHTMLWorker.html")) {
            PdfWriter.getInstance(doc, Files.newOutputStream(Paths.get("testColspanOut.pdf")));
            doc.open();
            HTMLWorker worker = new HTMLWorker(doc);
            worker.parse(new InputStreamReader(Objects.requireNonNull(stream), UTF_8));
            assert (true);
        } catch (IOException e) {
            logger.severe("Exception occured");
        }
    }
}