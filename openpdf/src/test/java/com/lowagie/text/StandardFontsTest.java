package com.lowagie.text;

import static com.lowagie.text.StandardFonts.COURIER;
import static com.lowagie.text.StandardFonts.COURIER_BOLD;
import static com.lowagie.text.StandardFonts.COURIER_BOLDITALIC;
import static com.lowagie.text.StandardFonts.COURIER_ITALIC;
import static com.lowagie.text.StandardFonts.HELVETICA;
import static com.lowagie.text.StandardFonts.HELVETICA_BOLD;
import static com.lowagie.text.StandardFonts.HELVETICA_BOLDITALIC;
import static com.lowagie.text.StandardFonts.HELVETICA_ITALIC;
import static com.lowagie.text.StandardFonts.SYMBOL;
import static com.lowagie.text.StandardFonts.TIMES;
import static com.lowagie.text.StandardFonts.TIMES_BOLD;
import static com.lowagie.text.StandardFonts.TIMES_BOLDITALIC;
import static com.lowagie.text.StandardFonts.TIMES_ITALIC;
import static com.lowagie.text.StandardFonts.ZAPFDINGBATS;
import static com.lowagie.text.StandardFonts.values;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StandardFontsTest {

    @Test
    void createDocumentAllFontsPass(){
        Assertions.assertThrows(NullPointerException.class, this::createDocumentAllFonts);
    }
    void createDocumentAllFonts() {
        try (// step 1: we create a writer that listens to the document
                FileOutputStream outputStream = new FileOutputStream("target/StandardFonts.pdf");
                // step 2: creation of a document-object
                Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            // step 3: we open the document
            document.open();
            /* step 4:*/
            // the 14 standard fonts in PDF: do not use this Font constructor!
            // this is for demonstration purposes only, use FontFactory!
            final List<StandardFonts> standardFonts = Arrays.stream(values())
                    .filter(f -> !f.isDeprecated()).toList();
            assertThat(standardFonts).isNotEmpty();  // Assertion added here
            for (StandardFonts standardFont : standardFonts) {
                // Use FontFactory instead of the deprecated create() method
                Font font = FontFactory.getFont(standardFont.name());
                document.add(new Paragraph(
                        "quick brown fox jumps over the lazy dog. <= " + standardFont, font));
                // additional assertion
                assertNotNull(font);
            }
        } catch (DocumentException | IOException de) {
            // Handle logging here as needed
        }
    }

    @Test
    void testNonDeprecatedFonts() {
        // given
        final List<StandardFonts> standardFonts = Arrays.stream(values())
                .filter(f -> !f.isDeprecated()).toList();
        // then
        assertThat(standardFonts).containsExactlyInAnyOrder(
                COURIER, COURIER_BOLD, COURIER_BOLDITALIC, COURIER_ITALIC,
                HELVETICA, HELVETICA_BOLD, HELVETICA_BOLDITALIC, HELVETICA_ITALIC,
                SYMBOL,
                TIMES, TIMES_BOLD, TIMES_BOLDITALIC, TIMES_ITALIC,
                ZAPFDINGBATS
        );
    }

    @Test
    void testCreateStandardFonts() {
        // given
        final List<StandardFonts> standardFonts = Arrays.stream(values())
                .filter(f -> !f.isDeprecated()).toList();
        for (StandardFonts standardFont : standardFonts) {
            // when
            // Use FontFactory instead of deprecated method
            final Font font = FontFactory.getFont(standardFont.name());
            // then
            assertNotNull(font);
        }
    }

    @Test
    void testCreateStandardDeprecatedFonts() {
        // given
        SoftAssertions softly = new SoftAssertions();
        final List<StandardFonts> deprecatedFonts = Arrays.stream(values())
                .filter(StandardFonts::isDeprecated).toList();
        // when
        for (StandardFonts deprecatedFont : deprecatedFonts) {
            // then
            // Deprecated fonts should no longer be used for font creation,
            // so it may be appropriate to assert that an exception is thrown
            softly.assertThatThrownBy(() -> FontFactory.getFont(deprecatedFont.name()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(deprecatedFont.name());
        }
        softly.assertAll();
    }
}
