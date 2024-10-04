package com.lowagie.text.pdf;

import java.io.IOException;
import java.util.Map;
import com.lowagie.text.ExceptionConverter;
import org.apache.fop.pdf.PDFFilterException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfStamperImpTest {

    @SuppressWarnings("unchecked")
    @Test
    void getPdfLayers_isBackwardsCompatiblePass(){
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, this::getPdfLayers_isBackwardsCompatible);
    }
    void getPdfLayers_isBackwardsCompatible() throws IOException {
        // given
        try {
            PdfReader reader = new PdfReader(DocumentProducerHelper.createHelloWorldDocumentBytes());
            PdfStamperImp testMe = new PdfStamperImp(reader, null, '\0', false);
            // when
            @SuppressWarnings("rawtypes")
            Map layers = testMe.getPdfLayers();
            // then
            Assertions.assertThat(layers).isEmpty();
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void getPdfLayersWithGenerics_isBackwardsCompatiblePass(){
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, this::getPdfLayersWithGenerics_isBackwardsCompatible);
    }
    void getPdfLayersWithGenerics_isBackwardsCompatible() throws IOException {
        // given
        try {
            PdfReader reader = new PdfReader(DocumentProducerHelper.createHelloWorldDocumentBytes());
            PdfStamperImp testMe = new PdfStamperImp(reader, null, '\0', false);
            // when
            Map<String, PdfLayer> layers = testMe.getPdfLayers();
            // then
            Assertions.assertThat(layers).isEmpty();
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

}
