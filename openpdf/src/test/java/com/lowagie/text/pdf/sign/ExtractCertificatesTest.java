package com.lowagie.text.pdf.sign;

import static org.assertj.core.api.Assertions.assertThat;


import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfPKCS7.X509Name;
import com.lowagie.text.pdf.PdfReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtractCertificatesTest {

    static final Logger logger = Logger.getLogger(ExtractCertificatesTest.class.getName());

    ExtractCertificatesTest() {
        super();
    }


    @Test
    void testSha1Pass(){
        Assertions.assertDoesNotThrow(this::testSha1);
    }
    void testSha1(){
        extract("src/test/resources/sample_signed-sha1.pdf", false);
    }

    @Test
    void testSha512Pass(){
        Assertions.assertDoesNotThrow(this::testSha512);
    }
    void testSha512(){
        extract("src/test/resources/sample_signed-sha512.pdf", false);
    }

    /**
     * Extract certificates and validate timestamp Sample file taken from <a
     * href="https://www.tecxoft.com/samples.php">...</a> (it has signature with timestamp with SHA-256) NB: Signature
     * will only be valid till 2022/01/02 (Hence only Sout is used not assert)
     */
    @Test
    void testSha256TimeStampPass(){
        Assertions.assertDoesNotThrow(this::testSha256TimeStamp);
    }
    void testSha256TimeStamp(){
        extract("src/test/resources/pdf_digital_signature_timestamp.pdf", true);
    }

    private void extract(String pdf, boolean isExpectedValidTimeStamp) {

        logger.info("pdf getName: " + pdf);

        KeyStore kall = PdfPKCS7.loadCacertsKeyStore();

        try (PdfReader reader = new PdfReader(pdf)) {
            AcroFields fields = reader.getAcroFields();

            List<String> signatures = fields.getSignedFieldNames();
            assertThat(signatures).isNotEmpty().hasSize(1);
            for (String signature : signatures) {

                assertThat(signature).isNotEmpty();
                boolean isWholeDocumentCovered = fields.signatureCoversWholeDocument(signature);
                assertThat(isWholeDocumentCovered).isTrue();
                assertThat(fields.getRevision(signature)).isEqualTo(1);
                assertThat(fields.getTotalRevisions()).isEqualTo(1);

                PdfPKCS7 pk = fields.verifySignature(signature);
                Calendar cal = pk.getSignDate();
                Certificate[] pkc = pk.getCertificates();
                X509Certificate certificate = pk.getSigningCertificate();
                assertThat(cal).isLessThan(Calendar.getInstance());
                X509Name subjectFields = PdfPKCS7.getSubjectFields(certificate);
                assertThat(subjectFields).isNotNull();
                assertThat(subjectFields.getAllFields()).isNotEmpty()
                        .containsKey("C");
                assertThat(pk.verify()).isTrue();
                assertThat(pk.verifyTimestampImprint()).isEqualTo(isExpectedValidTimeStamp);

                Object[] fails = pk.verifyCertificates(pkc, kall, null, cal);
                if (fails == null) {
                    System.out.println("Certificates verified against the KeyStore");
                } else {
                    System.err.println("Certificate failed: " + fails[1]);
                }

            }
        }catch(IOException | SignatureException | NoSuchAlgorithmException | PDFFilterException e){
            logger.info("Exception raised");
        }
    }
}
