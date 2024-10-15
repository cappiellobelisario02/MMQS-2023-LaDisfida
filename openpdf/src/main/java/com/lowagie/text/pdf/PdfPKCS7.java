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

import static org.bouncycastle.asn1.x509.Extension.authorityInfoAccess;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.lowagie.text.exceptions.InvalidTokenException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.X509CRLParser;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;

/**
 * This class does all the processing related to signing and verifying a PKCS#7 signature.
 * <p>
 * It's based in code found at org.bouncycastle.
 */
public class PdfPKCS7 {

    private static final String ID_PKCS7_DATA = "1.2.840.113549.1.7.1";
    private static final String ID_PKCS7_SIGNED_DATA = "1.2.840.113549.1.7.2";
    private static final String ID_RSA = "1.2.840.113549.1.1.1";
    private static final String ID_DSA = "1.2.840.10040.4.1";
    private static final String ID_ECDSA = "1.2.840.10045.2.1";
    private static final String ID_CONTENT_TYPE = "1.2.840.113549.1.9.3";
    private static final String ID_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";
    private static final String ID_SIGNING_TIME = "1.2.840.113549.1.9.5";
    private static final String ID_ADBE_REVOCATION = "1.2.840.113583.1.1.8";
    private static final Map<String, String> digestNames = new HashMap<>();
    private static final Map<String, String> algorithmNames = new HashMap<>();
    private static final Map<String, String> allowedDigests = new HashMap<>();

    public static final String KEY11354925 = "1.2.840.113549.2.5";

    public static final String KEY11354922 = "1.2.840.113549.2.2";

    public static final String KEY143226 = "1.3.14.3.2.26";

    public static final String KEY1013424 = "2.16.840.1.101.3.4.2.4";

    public static final String SHA_224 = "SHA224";

    public static final String KEY1013421 = "2.16.840.1.101.3.4.2.1";

    public static final String ID_TIME_STAMP_TOKEN = "1.2.840.113549.1.9.16.2.14"; // RFC 3161

    public static final String SHA_256 = "SHA256";

    public static final String SHA_384 = "SHA384";

    public static final String SHA_512 = "SHA512";

    public static final String RIPEMD_128 = "RIPEMD128";

    public static final String RIPEMD_160 = "RIPEMD160";

    public static final String RIPEMD_256 = "RIPEMD256";

    public static final String KEY1013422 = "2.16.840.1.101.3.4.2.2";

    public static final String KEY1013423 = "2.16.840.1.101.3.4.2.3";

    public static final String KEY36322 = "1.3.36.3.2.2";

    public static final String KEY36321 = "1.3.36.3.2.1";

    public static final String KEY36323 = "1.3.36.3.2.3";

    public static final String ECDSA = "ECDSA";

    public static final String RSA = "RSA";

    public static final String DSA = "DSA";

    public static final String SHA_1 = "SHA1";

    public static final String MD_2 = "MD2";

    public static final String MD_5 = "MD5";

    public static final String RIPEMD_2561 = "RIPEMD-256";

    static {
        digestNames.put(KEY11354925, MD_5);
        digestNames.put(KEY11354922, MD_2);
        digestNames.put(KEY143226, SHA_1);
        digestNames.put(KEY1013424, SHA_224);
        digestNames.put(KEY1013421, SHA_256);
        digestNames.put(KEY1013422, SHA_384);
        digestNames.put(KEY1013423, SHA_512);
        digestNames.put(KEY36322, RIPEMD_128);
        digestNames.put(KEY36321, RIPEMD_160);
        digestNames.put(KEY36323, RIPEMD_256);
        digestNames.put("1.2.840.113549.1.1.4", MD_5);
        digestNames.put("1.2.840.113549.1.1.2", MD_2);
        digestNames.put("1.2.840.113549.1.1.5", SHA_1);
        digestNames.put("1.2.840.113549.1.1.14", SHA_224);
        digestNames.put("1.2.840.113549.1.1.11", SHA_256);
        digestNames.put("1.2.840.113549.1.1.12", SHA_384);
        digestNames.put("1.2.840.113549.1.1.13", SHA_512);
        digestNames.put("1.2.840.10040.4.3", SHA_1);    // bug - duplicate key - overwrites this with DSA
        digestNames.put("2.16.840.1.101.3.4.3.1", SHA_224);  // bug - duplicate key - overwrites this with DSA
        digestNames.put("2.16.840.1.101.3.4.3.2", SHA_256);
        digestNames.put("2.16.840.1.101.3.4.3.3", SHA_384);
        digestNames.put("2.16.840.1.101.3.4.3.4", SHA_512);
        digestNames.put("1.3.36.3.3.1.3", RIPEMD_128);
        digestNames.put("1.3.36.3.3.1.2", RIPEMD_160);
        digestNames.put("1.3.36.3.3.1.4", RIPEMD_256);

        algorithmNames.put(ID_RSA, RSA);
        algorithmNames.put(ID_DSA, DSA);
        algorithmNames.put("1.2.840.113549.1.1.2", RSA);
        algorithmNames.put("1.2.840.113549.1.1.4", RSA);
        algorithmNames.put("1.2.840.113549.1.1.5", RSA);
        algorithmNames.put("1.2.840.113549.1.1.14", RSA);
        algorithmNames.put("1.2.840.113549.1.1.11", RSA);
        algorithmNames.put("1.2.840.113549.1.1.12", RSA);
        algorithmNames.put("1.2.840.113549.1.1.13", RSA);
        algorithmNames.put("1.2.840.10040.4.3", DSA);
        algorithmNames.put("2.16.840.1.101.3.4.3.1", DSA);
        algorithmNames.put("2.16.840.1.101.3.4.3.2", DSA);
        algorithmNames.put("1.3.36.3.3.1.3", RSA);
        algorithmNames.put("1.3.36.3.3.1.2", RSA);
        algorithmNames.put("1.3.36.3.3.1.4", RSA);
        algorithmNames.put(ID_ECDSA, ECDSA);
        algorithmNames.put("1.2.840.10045.4.1", ECDSA);
        algorithmNames.put("1.2.840.10045.4.3", ECDSA);
        algorithmNames.put("1.2.840.10045.4.3.2", ECDSA);
        algorithmNames.put("1.2.840.10045.4.3.3", ECDSA);
        algorithmNames.put("1.2.840.10045.4.3.4", ECDSA);
        algorithmNames.put("1.2.840.113549.1.1.10", "RSAandMGF1");
        allowedDigests.put(MD_5, KEY11354925);
        allowedDigests.put(MD_2, KEY11354922);
        allowedDigests.put(SHA_1, KEY143226);
        allowedDigests.put(SHA_224, KEY1013424);
        allowedDigests.put(SHA_256, KEY1013421);
        allowedDigests.put(SHA_384, KEY1013422);
        allowedDigests.put(SHA_512, KEY1013423);
        allowedDigests.put("MD-5", KEY11354925);
        allowedDigests.put("MD-2", KEY11354922);
        allowedDigests.put("SHA-1", KEY143226);
        allowedDigests.put("SHA-224", KEY1013424);
        allowedDigests.put("SHA-256", KEY1013421);
        allowedDigests.put("SHA-384", KEY1013422);
        allowedDigests.put("SHA-512", KEY1013423);
        allowedDigests.put(RIPEMD_128, KEY36322);
        allowedDigests.put("RIPEMD-128", KEY36322);
        allowedDigests.put(RIPEMD_160, KEY36321);
        allowedDigests.put("RIPEMD-160", KEY36321);
        allowedDigests.put(RIPEMD_256, KEY36323);
        allowedDigests.put(RIPEMD_2561, KEY36323);
    }

    private final List<Certificate> certs;
    private final List<CRL> crls;
    private final String provider;
    private byte[] sigAttr;
    private byte[] digestAttr;
    private int version;
    private int signerversion;
    private Set<String> digestalgos;
    private List<Certificate> signCerts;
    private X509Certificate signCert;
    private byte[] digest;
    private MessageDigest messageDigest;
    private String digestAlgorithm;
    private String digestEncryptionAlgorithm;
    private Signature sig;
    protected PrivateKey privKey;
    private byte[] rsaData;
    private boolean verified;
    private boolean verifyResult;
    private byte[] externalDigest;
    private byte[] externalRSAdata;
    /**
     * Holds value of property reason.
     */
    private String reason;
    /**
     * Holds value of property location.
     */
    private String location;
    /**
     * Holds value of property signDate.
     */
    private Calendar signDate;
    /**
     * Holds value of property signName.
     */
    private String signName;
    private TimeStampToken timeStampToken;
    private BasicOCSPResp basicResp;

    /**
     * Verifies a signature using the sub-filter adbe.x509.rsa_sha1.
     *
     * @param contentsKey the /Contents key
     * @param certsKey    the /Cert key
     * @param provider    the provider or <code>null</code> for the default provider
     */
    @SuppressWarnings("unchecked")
    public PdfPKCS7(byte[] contentsKey, byte[] certsKey, String provider) {
        try {
            this.provider = provider;
            CertificateFactory certificateFactory = new CertificateFactory();
            Collection<Certificate> certificates = certificateFactory.engineGenerateCertificates(
                    new ByteArrayInputStream(certsKey));
            certs = new ArrayList<>(certificates);
            signCerts = certs;
            signCert = (X509Certificate) certs.iterator().next();
            crls = new ArrayList<>();
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(contentsKey));
            digest = ((DEROctetString) in.readObject()).getOctets();
            if (provider == null) {
                sig = Signature.getInstance("SHA1withRSA");
            } else {
                sig = Signature.getInstance("SHA1withRSA", provider);
            }
            sig.initVerify(signCert.getPublicKey());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Verifies a signature using the sub-filter adbe.pkcs7.detached or adbe.pkcs7.sha1.
     *
     * @param contentsKey the /Contents key
     * @param provider    the provider or <code>null</code> for the default provider
     */
    @SuppressWarnings("unchecked")
    public PdfPKCS7(byte[] contentsKey, String provider) {
        try {
            this.provider = provider;
            ASN1InputStream din = new ASN1InputStream(new ByteArrayInputStream(contentsKey));

            // Basic checks to make sure it's a PKCS#7 SignedData Object
            ASN1Primitive pkcs = readASN1InputStream(din);
            if (!(pkcs instanceof ASN1Sequence signedData)) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("not.a.valid.pkcs.7.object.not.a.sequence"));
            }

            ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier) signedData.getObjectAt(0);
            if (!objId.getId().equals(ID_PKCS7_SIGNED_DATA)) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("not.a.valid.pkcs.7.object.not.signed.data"));
            }

            ASN1Sequence content = (ASN1Sequence) ((ASN1TaggedObject) signedData.getObjectAt(1)).getBaseObject();

            // Process the content...
            version = ((ASN1Integer) content.getObjectAt(0)).getValue().intValue();
            digestalgos = new HashSet<>();
            Enumeration<?> e = ((ASN1Set) content.getObjectAt(1)).getObjects();
            while (e.hasMoreElements()) {
                ASN1Sequence s = (ASN1Sequence) e.nextElement();
                ASN1ObjectIdentifier o = (ASN1ObjectIdentifier) s.getObjectAt(0);
                digestalgos.add(o.getId());
            }

            // Certificates and CRLs processing...
            CertificateFactory certificateFactory = new CertificateFactory();
            Collection<Certificate> certificates = certificateFactory.engineGenerateCertificates(
                    new ByteArrayInputStream(contentsKey));
            this.certs = new ArrayList<>(certificates);
            X509CRLParser cl = new X509CRLParser();
            cl.engineInit(new ByteArrayInputStream(contentsKey));
            crls = (List<CRL>) cl.engineReadAll();

            // Process possible ID_PKCS7_DATA...
            ASN1Sequence rsaDataSequence = (ASN1Sequence) content.getObjectAt(2);
            if (rsaDataSequence.size() > 1) {
                ASN1OctetString rsaDataContent = (ASN1OctetString) ((ASN1TaggedObject) rsaDataSequence.getObjectAt(1)).getBaseObject();
                rsaData = rsaDataContent.getOctets();
            }

            int next = 3;
            while (content.getObjectAt(next) instanceof ASN1TaggedObject) {
                ++next;
            }

            // Signer info processing...
            ASN1Set signerInfos = (ASN1Set) content.getObjectAt(next);
            if (signerInfos.size() != 1) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("this.pkcs.7.object.has.multiple.signerinfos.only.one.is.supported.at.this.time"));
            }
            ASN1Sequence signerInfo = (ASN1Sequence) signerInfos.getObjectAt(0);

            // Extract signer certificate...
            signerversion = ((ASN1Integer) signerInfo.getObjectAt(0)).getValue().intValue();
            ASN1Sequence issuerAndSerialNumber = (ASN1Sequence) signerInfo.getObjectAt(1);
            BigInteger serialNumber = ((ASN1Integer) issuerAndSerialNumber.getObjectAt(1)).getValue();
            for (Certificate cert1 : this.certs) {
                X509Certificate cert = (X509Certificate) cert1;
                if (serialNumber.equals(cert.getSerialNumber())) {
                    signCert = cert;
                    break;
                }
            }
            if (signCert == null) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("can.t.find.signing.certificate.with.serial.1", serialNumber.toString(16)));
            }
            signCertificateChain();
            digestAlgorithm = ((ASN1ObjectIdentifier) ((ASN1Sequence) signerInfo.getObjectAt(2)).getObjectAt(0)).getId();
            next = 3;

            if (signerInfo.getObjectAt(next) instanceof ASN1TaggedObject taggedObject) {
                ASN1Set sseq = ASN1Set.getInstance(taggedObject, false);
                sigAttr = sseq.getEncoded(ASN1Encoding.DER);

                for (int k = 0; k < sseq.size(); ++k) {
                    ASN1Sequence seq2 = (ASN1Sequence) sseq.getObjectAt(k);
                    if (((ASN1ObjectIdentifier) seq2.getObjectAt(0)).getId().equals(ID_MESSAGE_DIGEST)) {
                        ASN1Set set = (ASN1Set) seq2.getObjectAt(1);
                        digestAttr = ((DEROctetString) set.getObjectAt(0)).getOctets();
                    } else if (((ASN1ObjectIdentifier) seq2.getObjectAt(0)).getId().equals(ID_ADBE_REVOCATION)) {
                        ASN1Set setout = (ASN1Set) seq2.getObjectAt(1);
                        ASN1Sequence seqout = (ASN1Sequence) setout.getObjectAt(0);
                        for (int j = 0; j < seqout.size(); ++j) {
                            ASN1TaggedObject tg = (ASN1TaggedObject) seqout.getObjectAt(j);
                            if (tg.getTagNo() != 1) {
                                continue;
                            }
                            ASN1Sequence seqin = (ASN1Sequence) tg.getBaseObject();
                            findOcsp(seqin);
                        }
                    }
                }
                if (digestAttr == null) {
                    throw new IllegalArgumentException(
                            MessageLocalization.getComposedMessage("authenticated.attribute.is.missing.the.digest"));
                }
                ++next;
            }
            digestEncryptionAlgorithm = ((ASN1ObjectIdentifier) ((ASN1Sequence) signerInfo.getObjectAt(next++)).getObjectAt(0)).getId();
            digest = ((DEROctetString) signerInfo.getObjectAt(next++)).getOctets();

            if (next < signerInfo.size() && (signerInfo.getObjectAt(next) instanceof ASN1TaggedObject asn1TaggedObject)) {
                ASN1Set unat = ASN1Set.getInstance(asn1TaggedObject, false);
                AttributeTable attble = new AttributeTable(unat);
                Attribute ts = attble.get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
                if (ts != null && ts.getAttrValues().size() > 0) {
                    ASN1Set attributeValues = ts.getAttrValues();
                    ASN1Sequence tokenSequence = ASN1Sequence.getInstance(attributeValues.getObjectAt(0));
                    ContentInfo contentInfo = ContentInfo.getInstance(tokenSequence);
                    this.timeStampToken = new TimeStampToken(contentInfo);
                }
            }

            // Initialize message digest
            if (rsaData != null || digestAttr != null) {
                if (provider != null) {
                    System.out.println("Provider is set to: " + provider);
                    if (provider.startsWith("SunPKCS11")) {
                        messageDigest = MessageDigest.getInstance(getStandardJavaName(getHashAlgorithm()));
                    } else {
                        messageDigest = MessageDigest.getInstance(getStandardJavaName(getHashAlgorithm()), provider);
                    }
                } else {
                    System.out.println("Provider is null, using default");
                    messageDigest = MessageDigest.getInstance(getStandardJavaName(getHashAlgorithm()));
                }
            }

            // Initialize signature
            if (provider == null) {
                System.out.println("Provider is null, using default signature provider");
                sig = Signature.getInstance(getDigestAlgorithm());
            } else {
                System.out.println("Using provider for signature: " + provider);
                sig = Signature.getInstance(getDigestAlgorithm(), provider);
            }
            sig.initVerify(signCert.getPublicKey());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }



    private ASN1Primitive readASN1InputStream(ASN1InputStream stream) {
        try {
            return stream.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    MessageLocalization
                            .getComposedMessage("can.t.decode.pkcs7signeddata.object"));
        }
    }

    /**
     * Generates a signature.
     *
     * @param privKey       the private key
     * @param certChain     the certificate chain
     * @param crlList       the certificate revocation list
     * @param hashAlgorithm the hash algorithm
     * @param provider      the provider or <code>null</code> for the default provider
     * @param hasRSAdata    <CODE>true</CODE> if the sub-filter is adbe.pkcs7.sha1
     * @throws InvalidKeyException      on error
     * @throws NoSuchProviderException  on error
     * @throws NoSuchAlgorithmException on error
     */
    public PdfPKCS7(PrivateKey privKey, Certificate[] certChain, CRL[] crlList,
            String hashAlgorithm, String provider, boolean hasRSAdata)
            throws InvalidKeyException, NoSuchProviderException,
            NoSuchAlgorithmException {

        this.privKey = privKey;
        this.provider = provider;
        certs = new ArrayList<>();
        crls = new ArrayList<>();
        digestalgos = new HashSet<>();

        digestAlgorithm = getDigestAlgorithm(hashAlgorithm);
        initializeCertificatesAndCRLs(certChain, crlList);
        if (privKey != null) {
            digestEncryptionAlgorithm = getDigestEncryptionAlgorithm(privKey);
        }
        if (hasRSAdata) {
            initializeRSAMessageDigest();
        }
        if (privKey != null) {
            initializeSignature();
        }
    }

    private String getDigestAlgorithm(String hashAlgorithm) throws NoSuchAlgorithmException {
        String digestAlgorithmM = allowedDigests.get(hashAlgorithm.toUpperCase());
        if (digestAlgorithmM == null) {
            throw new NoSuchAlgorithmException(
                    MessageLocalization.getComposedMessage("unknown.hash.algorithm.1", hashAlgorithm));
        }
        digestalgos.add(digestAlgorithmM);
        return digestAlgorithmM;
    }

    private void initializeCertificatesAndCRLs(Certificate[] certChain, CRL[] crlList) {
        signCert = (X509Certificate) certChain[0];
        certs.addAll(Arrays.asList(certChain));
        if (crlList != null) {
            crls.addAll(Arrays.asList(crlList));
        }
    }

    private String getDigestEncryptionAlgorithm(PrivateKey privKey) throws NoSuchAlgorithmException {
        String algorithm = privKey.getAlgorithm();
        return switch (algorithm) {
            case RSA -> ID_RSA;
            case DSA -> ID_DSA;
            case "EC", ECDSA -> ID_ECDSA;
            default -> throw new NoSuchAlgorithmException(
                    MessageLocalization.getComposedMessage("unknown.key.algorithm.1", algorithm));
        };
    }

    private void initializeRSAMessageDigest() throws NoSuchAlgorithmException, NoSuchProviderException {
        rsaData = new byte[0];
        if (provider == null || provider.startsWith("SunPKCS11")) {
            messageDigest = MessageDigest.getInstance(getStandardJavaName(getHashAlgorithm()));
        } else {
            messageDigest = MessageDigest.getInstance(getStandardJavaName(getHashAlgorithm()), provider);
        }
    }

    private void initializeSignature() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
        if (provider == null) {
            sig = Signature.getInstance(getDigestAlgorithm());
        } else {
            sig = Signature.getInstance(getDigestAlgorithm(), provider);
        }
        sig.initSign(privKey);
    }


    /**
     * Gets the digest name for a certain id
     *
     * @param oid an id (for instance "1.2.840.113549.2.5")
     * @return a digest name (for instance "MD5")
     * @since 2.1.6
     */
    public static String getDigest(String oid) {
        return Optional.ofNullable(digestNames.get(oid))
                .orElse(oid);
    }

    /**
     * Gets the algorithm name for a certain id.
     *
     * @param oid an id (for instance "1.2.840.113549.1.1.1")
     * @return an algorithm name (for instance "RSA")
     * @since 2.1.6
     */
    public static String getAlgorithm(String oid) {
        return Optional.ofNullable(algorithmNames.get(oid))
                .orElse(oid);
    }

    /**
     * Gets the oid for given digest name.
     *
     * @param digestName digest name (for instance "SHA-256")
     * @return a digest OID (for instance "2.16.840.1.101.3.4.2.1") or {@code null} if the oid for provided name is not
     * found
     */
    public static String getDigestOid(String digestName) {
        return digestName != null ? allowedDigests.get(digestName) : null;
    }

    /**
     * Loads the default root certificates at &lt;java.home&gt;/lib/security/cacerts with the default provider.
     *
     * @return a <CODE>KeyStore</CODE>
     */
    public static KeyStore loadCacertsKeyStore() {
        return loadCacertsKeyStore(null);
    }

    /**
     * Loads the default root certificates at &lt;java.home&gt;/lib/security/cacerts.
     *
     * @param provider the provider or <code>null</code> for the default provider
     * @return a <CODE>KeyStore</CODE>
     */
    public static KeyStore loadCacertsKeyStore(String provider) {
        File file = new File(System.getProperty("java.home"), "lib");
        file = new File(file, "security");
        file = new File(file, "cacerts");
        try (FileInputStream fin = new FileInputStream(file)) {
            KeyStore k;
            if (provider == null) {
                k = KeyStore.getInstance("JKS");
            } else {
                k = KeyStore.getInstance("JKS", provider);
            }
            k.load(fin, null);
            return k;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Verifies a single certificate.
     *
     * @param cert     the certificate to verify
     * @param crls     the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date or <CODE>null</CODE> for the current date
     * @return a <CODE>String</CODE> with the error description or
     * <CODE>null</CODE> if no error
     */
    public static String verifyCertificate(X509Certificate cert, Collection<?> crls,
            Calendar calendar) {
        if (calendar == null) {
            calendar = new GregorianCalendar();
        }
        if (cert.hasUnsupportedCriticalExtension()) {
            return "Has unsupported critical extension";
        }
        try {
            cert.checkValidity(calendar.getTime());
        } catch (Exception e) {
            return e.getMessage();
        }
        if (crls != null) {
            for (Object crl : crls) {
                if (((CRL) crl).isRevoked(cert)) {
                    return "Certificate revoked";
                }
            }
        }
        return null;
    }

    /**
     * Verifies a certificate chain against a KeyStore.
     *
     * @param certs    the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @param crls     the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date or <CODE>null</CODE> for the current date
     * @return <CODE>null</CODE> if the certificate chain could be validated or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     */
    public Object[] verifyCertificates(Certificate[] certs,
            KeyStore keystore, Collection<?> crls, Calendar calendar) {
        if (calendar == null) {
            calendar = new GregorianCalendar();
        }

        // Iterate through the certificates
        for (int k = 0; k < certs.length; ++k) {
            X509Certificate cert = (X509Certificate) certs[k];

            // Check for errors in certificate verification
            Object[] errorResult = checkCertificate(cert, crls, calendar);
            if (errorResult != null) {
                return errorResult; // Return on error
            }

            // Verify the certificate against the keystore
            Object[] verificationResult = certificateVerification(keystore, calendar, cert);
            if (verificationResult != null) {
                return verificationResult; // Return the verification result if not null
            }

            // Verify the certificate against other certificates
            if (!isCertificateVerified(cert, certs, k)) {
                return new Object[]{cert, "Cannot be verified against the KeyStore or the certificate chain"};
            }
        }

        // If all certificates were processed and no issues found
        return new Object[]{null, "Invalid state. Possible circular certificate chain"};
    }

    // Checks if there are any verification errors with the certificate
    private Object[] checkCertificate(X509Certificate cert, Collection<?> crls, Calendar calendar) {
        String err = verifyCertificate(cert, crls, calendar);
        return (err != null) ? new Object[]{cert, err} : null;
    }

    // Verifies the certificate against the other certificates
    private boolean isCertificateVerified(X509Certificate cert, Certificate[] certs, int currentIndex) {
        for (int j = 0; j < certs.length; ++j) {
            if (j == currentIndex) {
                continue; // Skip the current certificate
            }
            X509Certificate certNext = (X509Certificate) certs[j];
            if (tryVerify(cert, certNext)) {
                return true; // Successfully verified
            }
        }
        return false; // No verification successful
    }

    // Tries to verify the current certificate against the next one
    private boolean tryVerify(X509Certificate cert, X509Certificate certNext) {
        try {
            cert.verify(certNext.getPublicKey());
            return true; // Verified successfully
        } catch (Exception ignored) {
            // Handle logging or any other actions needed
            return false; // Verification failed
        }
    }



    private Object[] certificateVerification(KeyStore keyStore, Calendar calendar,
            X509Certificate certificate) {
        try{
            for (Enumeration<?> aliases = keyStore.aliases(); aliases
                    .hasMoreElements(); ) {
                Object[] certificate1 = getObjects(keyStore, calendar, certificate, aliases);
                if (certificate1 != null)
                    return certificate1;
            }
        } catch (KeyStoreException ignored) {
            return new Object[0];
        }
        return new Object[0];
    }

    private Object[] getObjects(KeyStore keyStore, Calendar calendar, X509Certificate certificate,
            Enumeration<?> aliases) {
        try {
            String alias = (String) aliases.nextElement();
            if (!keyStore.isCertificateEntry(alias)) {
                return new Object[0];
            }
            X509Certificate certStoreX509 = (X509Certificate) keyStore
                    .getCertificate(alias);
            if (verifyCertificate(certStoreX509, crls, calendar) != null) {
                return new Object[0];
            }
            return x509CertificateVerification(certificate, certStoreX509);
        } catch (Exception ignored) {
            return new Object[0];
        }
    }

    private Object[] x509CertificateVerification(X509Certificate certificate, X509Certificate certStoreX509) {
        try{
            certificate.verify(certStoreX509.getPublicKey());
            return new Object[0];
        } catch (Exception ignored) {
            return new Object[0];
        }
    }

    /**
     * Retrieves the OCSP URL from the given certificate.
     *
     * @param certificate the certificate
     * @return the URL or null
     * @since 2.1.6
     */
    public static String getOCSPURL(X509Certificate certificate) {
        try {
            ASN1Primitive obj = getExtensionValue(certificate, authorityInfoAccess.getId());
            if (obj == null) {
                return null;
            }

            ASN1Sequence accessDescriptions = (ASN1Sequence) obj;
            for (int i = 0; i < accessDescriptions.size(); i++) {
                ASN1Sequence accessDescription = (ASN1Sequence) accessDescriptions
                        .getObjectAt(i);
                if (accessDescription.size() == 2 &&
                        (accessDescription.getObjectAt(0) instanceof ASN1ObjectIdentifier identifier)
                        && identifier.getId().equals("1.3.6.1.5.5.7.48.1")) {

                    return getStringFromGeneralName((ASN1Primitive) accessDescription
                            .getObjectAt(1));

                }
            }
        } catch (Exception ignored) {
//da vedere come effettuare il log
        }
        return null;
    }

    private static ASN1Primitive getExtensionValue(X509Certificate cert,
            String oid) throws IOException {
        byte[] bytes = cert.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    }

    private static String getStringFromGeneralName(ASN1Primitive names) {
        ASN1TaggedObject taggedObject = (ASN1TaggedObject) names;
        return new String(ASN1OctetString.getInstance(taggedObject, false)
                .getOctets(), StandardCharsets.ISO_8859_1);
    }

    /**
     * Get the "issuer" from the TBSCertificate bytes that are passed in
     *
     * @param enc a TBSCertificate in a byte array
     * @return a ASN1Primitive
     */
    private static ASN1Primitive getIssuer(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence) in.readObject();
            return (ASN1Primitive) seq
                    .getObjectAt(seq.getObjectAt(0) instanceof ASN1TaggedObject ? 3 : 2);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the "subject" from the TBSCertificate bytes that are passed in
     *
     * @param enc A TBSCertificate in a byte array
     * @return a ASN1Primitive
     */
    private static ASN1Primitive getSubject(byte[] enc) {
        try {
            ASN1InputStream in = new ASN1InputStream(new ByteArrayInputStream(enc));
            ASN1Sequence seq = (ASN1Sequence) in.readObject();
            return (ASN1Primitive) seq
                    .getObjectAt(seq.getObjectAt(0) instanceof ASN1TaggedObject ? 5 : 4);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the issuer fields from an X509 Certificate
     *
     * @param cert an X509Certificate
     * @return an X509Name
     */
    public static com.lowagie.text.pdf.PdfPKCS7.X509Name getIssuerFields(X509Certificate cert) {
        try {
            return new com.lowagie.text.pdf.PdfPKCS7.X509Name((ASN1Sequence) getIssuer(cert.getTBSCertificate()));
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Get the subject fields from an X509 Certificate
     *
     * @param cert an X509Certificate
     * @return an X509Name
     */
    public static com.lowagie.text.pdf.PdfPKCS7.X509Name getSubjectFields(X509Certificate cert) {
        try {
            return new com.lowagie.text.pdf.PdfPKCS7.X509Name((ASN1Sequence) getSubject(cert.getTBSCertificate()));
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private static String getStandardJavaName(String algName) {
        if (SHA_1.equals(algName)) {
            return "SHA-1";
        }
        if (SHA_224.equals(algName)) {
            return "SHA-224";
        }
        if (SHA_256.equals(algName)) {
            return "SHA-256";
        }
        if (SHA_384.equals(algName)) {
            return "SHA-384";
        }
        if (SHA_512.equals(algName)) {
            return "SHA-512";
        }
        return algName;

    }

    /**
     * Gets the timestamp token if there is one.
     *
     * @return the timestamp token or null
     * @since 2.1.6
     */
    public TimeStampToken getTimeStampToken() {
        return timeStampToken;
    }

    /**
     * Gets the timestamp date
     *
     * @return a date
     * @since 2.1.6
     */
    public Calendar getTimeStampDate() {
        if (timeStampToken == null) {
            return null;
        }
        Calendar cal = new GregorianCalendar();
        Date date = timeStampToken.getTimeStampInfo().getGenTime();
        cal.setTime(date);
        return cal;
    }

    /**
     * Gets the OCSP basic response if there is one.
     *
     * @return the OCSP basic response or null
     * @since 2.1.6
     */
    public BasicOCSPResp getOcsp() {
        return basicResp;
    }

    private void findOcsp(ASN1Sequence seq) throws IOException {
        basicResp = null;

        // Process the sequence until we find a valid OCSP identifier
        while (!isOcspBasicResponse(seq)) {
            if (!processSequence(seq)) {
                return; // If we can't process further, exit early
            }
        }

        // Extract the octet string and process the response
        DEROctetString os = (DEROctetString) seq.getObjectAt(1);
        processOcspResponse(os);
    }

    // Helper method to check if the sequence contains the OCSP basic response identifier
    private boolean isOcspBasicResponse(ASN1Sequence seq) {
        if (!(seq.getObjectAt(0) instanceof ASN1ObjectIdentifier identifier)) {
            return false;
        }
        return identifier.getId().equals(OCSPObjectIdentifiers.id_pkix_ocsp_basic.getId());
    }

    // Helper method to process the ASN1 sequence and update the reference
    private boolean processSequence(ASN1Sequence seq) {
        for (int k = 0; k < seq.size(); ++k) {
            if (seq.getObjectAt(k) instanceof ASN1Sequence) {
                return true;
            }

            if (seq.getObjectAt(k) instanceof ASN1TaggedObject taggedObject) {
                return taggedObject.getBaseObject() instanceof ASN1Sequence;// Exit early if it's not a sequence
            }
        }
        return false;
    }

    // Helper method to process the OCSP response
    private void processOcspResponse(DEROctetString os) throws IOException {
        try (ASN1InputStream inp = new ASN1InputStream(os.getOctets())) {
            BasicOCSPResponse resp = BasicOCSPResponse.getInstance(inp.readObject());
            basicResp = new BasicOCSPResp(resp); // Set the basic response
        }
    }


    /**
     * Update the digest with the specified bytes. This method is used both for signing and verifying
     *
     * @param buf the data buffer
     * @param off the offset in the data buffer
     * @param len the data length
     * @throws SignatureException on error
     */
    public void update(byte[] buf, int off, int len) throws SignatureException {
        if (rsaData != null || digestAttr != null) {
            messageDigest.update(buf, off, len);
        } else {
            sig.update(buf, off, len);
        }
    }

    /**
     * Verify the digest.
     *
     * @return <CODE>true</CODE> if the signature checks out, <CODE>false</CODE>
     * otherwise
     * @throws SignatureException on error
     */
    public boolean verify() throws SignatureException {
        if (verified) {
            return verifyResult;
        }
        if (sigAttr != null) {
            sig.update(sigAttr);
            if (rsaData != null) {
                byte[] msd = messageDigest.digest();
                messageDigest.update(msd);
            }
            verifyResult = (Arrays.equals(messageDigest.digest(), digestAttr) && sig
                    .verify(digest));
        } else {
            if (rsaData != null) {
                sig.update(messageDigest.digest());
            }
            verifyResult = sig.verify(digest);
        }
        verified = true;
        return verifyResult;
    }

    /**
     * Checks if the timestamp refers to this document.
     *
     * @return true if it checks false otherwise
     * @throws java.security.NoSuchAlgorithmException on error
     * @since 2.1.6
     */
    public boolean verifyTimestampImprint() throws NoSuchAlgorithmException {
        if (timeStampToken == null) {
            return false;
        }
        MessageImprint imprint = timeStampToken.getTimeStampInfo().toASN1Structure()
                .getMessageImprint();
        TimeStampTokenInfo info = timeStampToken.getTimeStampInfo();
        String algOID = info.getMessageImprintAlgOID().getId();
        byte[] md = MessageDigest.getInstance(getStandardJavaName(getDigest(algOID))).digest(digest);
        byte[] imphashed = imprint.getHashedMessage();
        return Arrays.equals(md, imphashed);
    }

    /**
     * Get all the X.509 certificates associated with this PKCS#7 object in no particular order. Other certificates,
     * from OCSP for example, will also be included.
     *
     * @return the X.509 certificates associated with this PKCS#7 object
     */
    public Certificate[] getCertificates() {
        return certs.toArray(new Certificate[0]);
    }

    /**
     * Get the X.509 sign certificate chain associated with this PKCS#7 object. Only the certificates used for the main
     * signature will be returned, with the signing certificate first.
     *
     * @return the X.509 certificates associated with this PKCS#7 object
     * @since 2.1.6
     */
    public Certificate[] getSignCertificateChain() {
        return signCerts.toArray(new Certificate[0]);
    }

    private void signCertificateChain() {
        List<Certificate> cc = new ArrayList<>();
        cc.add(signCert);
        List<Certificate> oc = new ArrayList<>(certs);
        int k = 0;
        while (k < oc.size()) {
            if (signCert.getSerialNumber().equals(
                    ((X509Certificate) oc.get(k)).getSerialNumber())) {
                oc.remove(k);
            }
            else{
                k++;
            }
        }
        boolean found = true;
        while (found) {
            X509Certificate v = (X509Certificate) cc.get(cc.size() - 1);
            found = false;
            for (int h = 0; h < oc.size(); ++h) {
                try {
                    if (provider == null) {
                        v.verify(oc.get(k).getPublicKey());
                    } else {
                        v.verify(oc.get(k).getPublicKey(), provider);
                    }
                    found = true;
                    cc.add(oc.get(k));
                    oc.remove(k);
                    break;
                } catch (Exception ignored) {
//da vedere come effettuare il log
                }
            }
        }
        signCerts = cc;
    }

    /**
     * Get the X.509 certificate revocation lists associated with this PKCS#7 object
     *
     * @return the X.509 certificate revocation lists associated with this PKCS#7 object
     */
    public Collection<CRL> getCRLs() {
        return crls;
    }

    /**
     * Get the X.509 certificate actually used to sign the digest.
     *
     * @return the X.509 certificate actually used to sign the digest
     */
    public X509Certificate getSigningCertificate() {
        return signCert;
    }

    /**
     * Get the version of the PKCS#7 object. Always 1
     *
     * @return the version of the PKCS#7 object. Always 1
     */
    public int getVersion() {
        return version;
    }

    /**
     * Get the version of the PKCS#7 "SignerInfo" object. Always 1
     *
     * @return the version of the PKCS#7 "SignerInfo" object. Always 1
     */
    public int getSigningInfoVersion() {
        return signerversion;
    }

    /**
     * Get the algorithm used to calculate the message digest
     *
     * @return the algorithm used to calculate the message digest
     */
    public String getDigestAlgorithm() {
        String dea = getAlgorithm(digestEncryptionAlgorithm);

        return getHashAlgorithm() + "with" + dea;
    }

    /**
     * Returns the algorithm.
     *
     * @return the digest algorithm
     */
    public String getHashAlgorithm() {
        return getDigest(digestAlgorithm);
    }

    /**
     * Checks if OCSP revocation refers to the document signing certificate.
     *
     * @return true if it checks false otherwise
     * @since 2.1.6
     */
    public boolean isRevocationValid() {
        if (basicResp == null) {
            return false;
        }
        if (signCerts.size() < 2) {
            return false;
        }
        try {
            X509Certificate[] cs = (X509Certificate[]) getSignCertificateChain();
            SingleResp sr = basicResp.getResponses()[0];
            CertificateID cid = sr.getCertID();
            X509Certificate sigcer = getSigningCertificate();
            X509Certificate isscer = cs[1];
            // OJO... Modificacion de
            // Felix--------------------------------------------------
            // CertificateID tis = new CertificateID(CertificateID.HASH_SHA1, isscer,

            DigestCalculatorProvider digCalcProv = new JcaDigestCalculatorProviderBuilder()
                    .setProvider(provider).build();
            CertificateID id = new CertificateID(
                    digCalcProv.get(CertificateID.HASH_SHA1),
                    new JcaX509CertificateHolder(isscer), sigcer.getSerialNumber());

            return id.equals(cid);
            // ******************************************************************************
        } catch (Exception ignored) {
//da vedere come effettuare il log
        }
        return false;
    }

    /**
     * Gets the bytes for the PKCS#1 object.
     *
     * @return a byte array
     */
    public byte[] getEncodedPKCS1() {
        try {
            if (externalDigest != null) {
                digest = externalDigest;
            } else {
                digest = sig.sign();
            }
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            ASN1OutputStream dout = ASN1OutputStream.create(bOut);
            dout.writeObject(new DEROctetString(digest));
            dout.close();

            return bOut.toByteArray();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Sets the digest/signature to an external calculated value.
     *
     * @param digest                    the digest. This is the actual signature
     * @param rsaDataByte                   the extra data that goes into the data tag in PKCS#7
     * @param digestEncryptionAlgorithm the encryption algorithm. It may must be <CODE>null</CODE> if the
     *                                  <CODE>digest</CODE> is also <CODE>null</CODE>. If the
     *                                  <CODE>digest</CODE> is not <CODE>null</CODE> then it may be "RSA"
     *                                  or "DSA"
     */
    public void setExternalDigest(byte[] digest, byte[] rsaDataByte,
            String digestEncryptionAlgorithm) {
        externalDigest = digest;
        externalRSAdata = rsaDataByte;
        if (digestEncryptionAlgorithm != null) {
            if (digestEncryptionAlgorithm.equals(RSA)) {
                this.digestEncryptionAlgorithm = ID_RSA;
            } else if (digestEncryptionAlgorithm.equals(DSA)) {
                this.digestEncryptionAlgorithm = ID_DSA;
            } else {
                throw new ExceptionConverter(new NoSuchAlgorithmException(
                        MessageLocalization.getComposedMessage("unknown.key.algorithm.1",
                                digestEncryptionAlgorithm)));
            }
        }
    }

    /**
     * Gets the bytes for the PKCS7SignedData object.
     *
     * @return the bytes for the PKCS7SignedData object
     */
    public byte[] getEncodedPKCS7() {
        return getEncodedPKCS7(null, null, null, null);
    }

    /**
     * Gets the bytes for the PKCS7SignedData object. Optionally the authenticatedAttributes in the signerInfo can also
     * be set. If either of the parameters is <CODE>null</CODE>, none will be used.
     *
     * @param secondDigest the digest in the authenticatedAttributes
     * @param signingTime  the signing time in the authenticatedAttributes
     * @return the bytes for the PKCS7SignedData object
     */
    public byte[] getEncodedPKCS7(byte[] secondDigest, Calendar signingTime) {
        return getEncodedPKCS7(secondDigest, signingTime, null, null);
    }

    /**
     * Gets the bytes for the PKCS7SignedData object. Optionally the authenticatedAttributes in the signerInfo can also
     * be set, OR a time-stamp-authority client may be provided.
     *
     * @param secondDigest the digest in the authenticatedAttributes
     * @param signingTime  the signing time in the authenticatedAttributes
     * @param tsaClient    TSAClient - null or an optional time stamp authority client
     * @param ocsp         a byte array
     * @return byte[] the bytes for the PKCS7SignedData object
     * @since 2.1.6
     */
    public byte[] getEncodedPKCS7(byte[] secondDigest, Calendar signingTime,
            TSAClient tsaClient, byte[] ocsp) {
        try {
            // Step 1: Compute the Digest
            byte[] mDigest = computeDigest();

            // Step 2: Create Digest Algorithms Sequence
            DERSequence digestAlgorithms = createDigestAlgorithmsSequence();

            // Step 3: Create Content Info
            DERSequence contentInfo = createContentInfo();

            // Step 4: Create Certificates Set
            DERSet certificatesSet = createCertificatesSet();

            // Step 5: Create Signer Info
            DERSet signerInfo = createSignerInfo(secondDigest, signingTime, ocsp, mDigest, tsaClient);

            // Step 6: Assemble the Final PKCS7 Structure
            return buildPKCS7Structure(digestAlgorithms, contentInfo, certificatesSet, signerInfo);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    // Compute the Digest
    private byte[] computeDigest() throws GeneralSecurityException {
        if (externalDigest != null) {
            digest = externalDigest;
            if (rsaData != null) {
                rsaData = externalRSAdata;
            }
        } else if (externalRSAdata != null && rsaData != null) {
            rsaData = externalRSAdata;
            sig.update(rsaData);
            digest = sig.sign();
        } else {
            if (rsaData != null) {
                rsaData = messageDigest.digest();
                sig.update(rsaData);
            }
            digest = sig.sign();
        }
        return digest;
    }

    // Create Digest Algorithms Sequence
    private DERSequence createDigestAlgorithmsSequence() {
        ASN1EncodableVector digestAlgorithms = new ASN1EncodableVector();
        for (String digestAlgo : digestalgos) {
            ASN1EncodableVector algos = new ASN1EncodableVector();
            algos.add(new ASN1ObjectIdentifier(digestAlgo));
            algos.add(DERNull.INSTANCE);
            digestAlgorithms.add(new DERSequence(algos));
        }
        return new DERSequence(digestAlgorithms);
    }

    // Create Content Info
    private DERSequence createContentInfo() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier(ID_PKCS7_DATA));
        if (rsaData != null) {
            v.add(new DERTaggedObject(0, new DEROctetString(rsaData)));
        }
        return new DERSequence(v);
    }

    // Create Certificates Set
    private DERSet createCertificatesSet() throws IOException, CertificateEncodingException {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (Certificate cert : certs) {
            ASN1InputStream tempStream;
            try {
                tempStream = new ASN1InputStream(
                        new ByteArrayInputStream(((X509Certificate) cert).getEncoded()));
            } catch (CertificateEncodingException e) {
                throw new CertificateEncodingException(e);
            }
            v.add(tempStream.readObject());
        }
        return new DERSet(v);
    }

    // Create Signer Info
    private DERSet createSignerInfo(byte[] secondDigest, Calendar signingTime, byte[] ocsp,
            byte[] digest, TSAClient tsaClient) throws IOException, GeneralSecurityException, InvalidTokenException {
        ASN1EncodableVector signerInfo = new ASN1EncodableVector();

        // Add the signerInfo version
        signerInfo.add(new ASN1Integer(signerversion));

        // Add issuer and serial number
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(getIssuer(signCert.getTBSCertificate()));
        v.add(new ASN1Integer(signCert.getSerialNumber()));
        signerInfo.add(new DERSequence(v));

        // Add digestAlgorithm
        v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier(digestAlgorithm));
        v.add(DERNull.INSTANCE);
        signerInfo.add(new DERSequence(v));

        // Add authenticated attributes if present
        if (secondDigest != null && signingTime != null) {
            signerInfo.add(new DERTaggedObject(false, 0,
                    getAuthenticatedAttributeSet(secondDigest, signingTime, ocsp)));
        }

        // Add digestEncryptionAlgorithm
        v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier(digestEncryptionAlgorithm));
        v.add(DERNull.INSTANCE);
        signerInfo.add(new DERSequence(v));

        // Add digest
        signerInfo.add(new DEROctetString(digest));

        // Add timestamp if TSAClient is available
        if (tsaClient != null) {
            byte[] tsImprint = tsaClient.getMessageDigest().digest(digest);
            byte[] tsToken;
            try {
                tsToken = tsaClient.getTimeStampToken(this, tsImprint);
            } catch (InvalidTokenException e) {
                throw new InvalidTokenException(e.getMessage());
            }
            if (tsToken != null) {
                ASN1EncodableVector unauthAttributes = buildUnauthenticatedAttributes(tsToken);
                if (unauthAttributes != null) {
                    signerInfo.add(new DERTaggedObject(false, 1, new DERSet(unauthAttributes)));
                }
            }
        }

        return new DERSet(new DERSequence(signerInfo));
    }

    // Build PKCS7 Structure
    private byte[] buildPKCS7Structure(DERSequence digestAlgorithms, DERSequence contentInfo,
            DERSet certificatesSet, DERSet signerInfo) throws IOException {
        ASN1EncodableVector body = new ASN1EncodableVector();
        body.add(new ASN1Integer(version));
        body.add(digestAlgorithms);
        body.add(contentInfo);
        body.add(new DERTaggedObject(false, 0, certificatesSet));
        body.add(signerInfo);

        ASN1EncodableVector whole = new ASN1EncodableVector();
        whole.add(new ASN1ObjectIdentifier(ID_PKCS7_SIGNED_DATA));
        whole.add(new DERTaggedObject(0, new DERSequence(body)));

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream dout = ASN1OutputStream.create(bOut);
        dout.writeObject(new DERSequence(whole));
        dout.close();

        return bOut.toByteArray();
    }


    /**
     * Added by Aiken Sam, 2006-11-15, modifed by Martin Brunecky 07/12/2007 to start with the timeStampToken
     * (signedData 1.2.840.113549.1.7.2). Token is the TSA response without response status, which is usually handled by
     * the (vendor supplied) TSA request/response interface).
     *
     * @param timeStampToken byte[] - time stamp token, DER encoded signedData
     * @return ASN1EncodableVector
     */
    private ASN1EncodableVector buildUnauthenticatedAttributes(
            byte[] timeStampToken) throws IOException {
        if (timeStampToken == null) {
            return null;
        }

        ASN1InputStream tempstream = new ASN1InputStream(new ByteArrayInputStream(
                timeStampToken));
        ASN1EncodableVector unauthAttributes = new ASN1EncodableVector();

        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1ObjectIdentifier(ID_TIME_STAMP_TOKEN)); // id-aa-timeStampToken
        ASN1Sequence seq = (ASN1Sequence) tempstream.readObject();
        v.add(new DERSet(seq));

        unauthAttributes.add(new DERSequence(v));
        return unauthAttributes;
    }

    /**
     * When using authenticatedAttributes the authentication process is different. The document digest is generated and
     * put inside the attribute. The signing is done over the DER encoded authenticatedAttributes. This method provides
     * that encoding and the parameters must be exactly the same as in {@link #getEncodedPKCS7(byte[], Calendar)}. A
     * simple example:
     *
     * <pre>
     * Calendar cal = Calendar.getInstance();
     * PdfPKCS7 pk7 = new PdfPKCS7(key, chain, null, &quot;SHA1&quot;, null, false);
     * MessageDigest messageDigest = MessageDigest.getInstance(&quot;SHA1&quot;);
     * byte buf[] = new byte[8192];
     * int n;
     * InputStream inp = sap.getRangeStream();
     * while ((n = inp.read(buf)) &gt; 0) {
     *   messageDigest.update(buf, 0, n);
     * }
     * byte hash[] = messageDigest.digest();
     * byte sh[] = pk7.getAuthenticatedAttributeBytes(hash, cal);
     * pk7.update(sh, 0, sh.length);
     * byte sg[] = pk7.getEncodedPKCS7(hash, cal);
     * </pre>
     *
     * @param secondDigest the content digest
     * @param signingTime  the signing time
     * @param ocsp         a byte array
     * @return the byte array representation of the authenticatedAttributes ready to be signed
     */
    public byte[] getAuthenticatedAttributeBytes(byte[] secondDigest,
            Calendar signingTime, byte[] ocsp) {
        try {
            return getAuthenticatedAttributeSet(secondDigest, signingTime, ocsp)
                    .getEncoded(ASN1Encoding.DER);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private DERSet getAuthenticatedAttributeSet(byte[] secondDigest,
            Calendar signingTime, byte[] ocsp) {
        try {
            ASN1EncodableVector attribute = new ASN1EncodableVector();
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(ID_CONTENT_TYPE));
            v.add(new DERSet(new ASN1ObjectIdentifier(ID_PKCS7_DATA)));
            attribute.add(new DERSequence(v));
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(ID_SIGNING_TIME));
            v.add(new DERSet(new DERUTCTime(signingTime.getTime())));
            attribute.add(new DERSequence(v));
            v = new ASN1EncodableVector();
            v.add(new ASN1ObjectIdentifier(ID_MESSAGE_DIGEST));
            v.add(new DERSet(new DEROctetString(secondDigest)));
            attribute.add(new DERSequence(v));
            if (ocsp != null) {
                v = new ASN1EncodableVector();
                v.add(new ASN1ObjectIdentifier(ID_ADBE_REVOCATION));
                DEROctetString doctet = new DEROctetString(ocsp);
                ASN1EncodableVector vo1 = new ASN1EncodableVector();
                ASN1EncodableVector v2 = new ASN1EncodableVector();
                v2.add(OCSPObjectIdentifiers.id_pkix_ocsp_basic);
                v2.add(doctet);
                ASN1Enumerated den = new ASN1Enumerated(0);
                ASN1EncodableVector v3 = new ASN1EncodableVector();
                v3.add(den);
                v3.add(new DERTaggedObject(true, 0, new DERSequence(v2)));
                vo1.add(new DERSequence(v3));
                v.add(new DERSet(new DERSequence(new DERTaggedObject(true, 1,
                        new DERSequence(vo1)))));
                attribute.add(new DERSequence(v));
            } else if (!crls.isEmpty()) {
                v = new ASN1EncodableVector();
                v.add(new ASN1ObjectIdentifier(ID_ADBE_REVOCATION));
                ASN1EncodableVector v2 = new ASN1EncodableVector();
                for (CRL crl : crls) {
                    ASN1InputStream t = new ASN1InputStream(new ByteArrayInputStream(
                            ((X509CRL) crl).getEncoded()));
                    v2.add(t.readObject());
                }
                v.add(new DERSet(new DERSequence(new DERTaggedObject(true, 0,
                        new DERSequence(v2)))));
                attribute.add(new DERSequence(v));
            }
            return new DERSet(attribute);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Getter for property reason.
     *
     * @return Value of property reason.
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Setter for property reason.
     *
     * @param reason New value of property reason.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Getter for property location.
     *
     * @return Value of property location.
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Setter for property location.
     *
     * @param location New value of property location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter for property signDate.
     *
     * @return Value of property signDate.
     */
    public Calendar getSignDate() {
        return this.signDate;
    }

    /**
     * Setter for property signDate.
     *
     * @param signDate New value of property signDate.
     */
    public void setSignDate(Calendar signDate) {
        this.signDate = signDate;
    }

    /**
     * Getter for property sigName.
     *
     * @return Value of property sigName.
     */
    public String getSignName() {
        return this.signName;
    }

    /**
     * Setter for property sigName.
     *
     * @param signName New value of property sigName.
     */
    public void setSignName(String signName) {
        this.signName = signName;
    }

    /**
     * a class that holds an X509 name
     */
    public static class X509Name {

        /**
         * country code - StringType(SIZE(2))
         */
        public static final ASN1ObjectIdentifier C = new ASN1ObjectIdentifier(
                "2.5.4.6");

        /**
         * organization - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier O = new ASN1ObjectIdentifier(
                "2.5.4.10");

        /**
         * organizational unit name - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier OU = new ASN1ObjectIdentifier(
                "2.5.4.11");

        /**
         * Title
         */
        public static final ASN1ObjectIdentifier T = new ASN1ObjectIdentifier(
                "2.5.4.12");

        /**
         * common name - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier CN = new ASN1ObjectIdentifier(
                "2.5.4.3");

        /**
         * device serial number name - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier SN = new ASN1ObjectIdentifier(
                "2.5.4.5");

        /**
         * locality name - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier L = new ASN1ObjectIdentifier(
                "2.5.4.7");

        /**
         * state, or province name - StringType(SIZE(1..64))
         */
        public static final ASN1ObjectIdentifier ST = new ASN1ObjectIdentifier(
                "2.5.4.8");

        /**
         * Naming attribute of type X520name
         */
        public static final ASN1ObjectIdentifier SURNAME = new ASN1ObjectIdentifier(
                "2.5.4.4");
        /**
         * Naming attribute of type X520name
         */
        public static final ASN1ObjectIdentifier GIVENNAME = new ASN1ObjectIdentifier(
                "2.5.4.42");
        /**
         * Naming attribute of type X520name
         */
        public static final ASN1ObjectIdentifier INITIALS = new ASN1ObjectIdentifier(
                "2.5.4.43");
        /**
         * Naming attribute of type X520name
         */
        public static final ASN1ObjectIdentifier GENERATION = new ASN1ObjectIdentifier(
                "2.5.4.44");
        /**
         * Naming attribute of type X520name
         */
        public static final ASN1ObjectIdentifier UNIQUE_IDENTIFIER = new ASN1ObjectIdentifier(
                "2.5.4.45");

        /**
         * Email address (RSA PKCS#9 extension) - IA5String.
         * <p>
         * Note: if you're trying to be ultra orthodox, don't use this! It shouldn't be in here.
         */
        public static final ASN1ObjectIdentifier EmailAddress = new ASN1ObjectIdentifier(
                "1.2.840.113549.1.9.1");

        /**
         * email address in Verisign certificates
         */
        public static final ASN1ObjectIdentifier E = EmailAddress;

        /**
         * object identifier
         */
        public static final ASN1ObjectIdentifier DC = new ASN1ObjectIdentifier(
                "0.9.2342.19200300.100.1.25");

        /**
         * LDAP User id.
         */
        public static final ASN1ObjectIdentifier UID = new ASN1ObjectIdentifier(
                "0.9.2342.19200300.100.1.1");

        /**
         * A HashMap with default symbols
         */
        protected static final Map<ASN1Encodable, String> defaultSymbols = new HashMap<>();

        static {
            defaultSymbols.put(C, "C");
            defaultSymbols.put(O, "O");
            defaultSymbols.put(T, "T");
            defaultSymbols.put(OU, "OU");
            defaultSymbols.put(CN, "CN");
            defaultSymbols.put(L, "L");
            defaultSymbols.put(ST, "ST");
            defaultSymbols.put(SN, "SN");
            defaultSymbols.put(EmailAddress, "E");
            defaultSymbols.put(DC, "DC");
            defaultSymbols.put(UID, "UID");
            defaultSymbols.put(SURNAME, "SURNAME");
            defaultSymbols.put(GIVENNAME, "GIVENNAME");
            defaultSymbols.put(INITIALS, "INITIALS");
            defaultSymbols.put(GENERATION, "GENERATION");

        }

        /**
         * A HashMap with values
         */
        public final Map<String, List<String>> valuesMap = new HashMap<>();

        /**
         * Constructs an X509 name
         *
         * @param seq an ASN1 Sequence
         */
        public X509Name(ASN1Sequence seq) {
            Enumeration<?> e = seq.getObjects();

            while (e.hasMoreElements()) {
                ASN1Set set = (ASN1Set) e.nextElement();

                for (int i = 0; i < set.size(); i++) {
                    ASN1Sequence s = (ASN1Sequence) set.getObjectAt(i);
                    ASN1Encodable encodable = s.getObjectAt(0);
                    String id = defaultSymbols.get(encodable);
                    if (id == null) {
                        continue;
                    }
                    List<String> vs = valuesMap.computeIfAbsent(id, k -> new ArrayList<>());
                    vs.add(((ASN1String) s.getObjectAt(1)).getString());
                }
            }
        }

        /**
         * Constructs an X509 name
         *
         * @param dirName a directory name
         */
        public X509Name(String dirName) {
            com.lowagie.text.pdf.PdfPKCS7.X509NameTokenizer nTok = new com.lowagie.text.pdf.PdfPKCS7.X509NameTokenizer(dirName);

            while (nTok.hasMoreTokens()) {
                String token = nTok.nextToken();
                int index = token.indexOf('=');

                if (index == -1) {
                    throw new IllegalArgumentException(
                            MessageLocalization
                                    .getComposedMessage("badly.formated.directory.string"));
                }

                String id = token.substring(0, index).toUpperCase();
                String value = token.substring(index + 1);
                List<String> vs = valuesMap.computeIfAbsent(id, k -> new ArrayList<>());
                vs.add(value);
            }

        }

        public String getField(String name) {
            List<String> vs = valuesMap.get(name);
            return vs == null ? null : vs.get(0);
        }

        /**
         * gets a field array from the values Hashmap
         *
         * @param name the name of the field array to get
         * @return an ArrayList
         */
        public List<String> getFieldsByName(String name) {
            return valuesMap.get(name);
        }

        /**
         * getter for values
         *
         * @return a HashMap with the fields of the X509 name
         */
        public Map<String, List<String>> getAllFields() {
            return valuesMap;
        }

        /**
         * @return values string representation
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return valuesMap.toString();
        }
    }

    /**
     * class for breaking up an X500 Name into its component tokens, ala java.util.StringTokenizer. We need this class
     * as some of the lightweight Java environment don't support classes like StringTokenizer.
     */
    public static class X509NameTokenizer {

        private final String oid;
        private final StringBuilder buf = new StringBuilder();
        private int index;

        public X509NameTokenizer(String oid) {
            this.oid = oid;
            this.index = -1;
        }

        public boolean hasMoreTokens() {
            return (index != oid.length());
        }

        public String nextToken() {
            if (index == oid.length()) {
                return null;
            }

            buf.setLength(0);

            index = processToken();
            return buf.toString().trim();
        }

        private int processToken() {
            int end = index;
            boolean quoted = false;
            boolean escaped = false;

            while (end < oid.length()) {
                char c = oid.charAt(end);

                switch (c) {
                    case '"':
                        end = handleQuote(end, escaped);
                        quoted = !quoted;
                        break;
                    case '\\':
                        end = handleBackslash(end, escaped);
                        escaped = !escaped;
                        break;
                    case ',':
                        if (!quoted) {
                            break;
                        }
                        // fall through
                    default:
                        escaped = false;
                        break;
                }
                end++;
            }

            return end;
        }

        private int handleQuote(int end, boolean escaped) {
            if (escaped) {
                buf.append('"');
            }
            return end;
        }

        private int handleBackslash(int end, boolean escaped) {
            if (escaped) {
                buf.append('\\');
                return end;
            }
            // Move to the next character and re-evaluate
            end++;
            return end;
        }


    }
}
