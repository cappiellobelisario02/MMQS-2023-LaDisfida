/*
 * $Id: XfaFile.java 3117 2008-01-31 05:53:22Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.lowagie.rups.model;

import com.lowagie.rups.io.OutputStreamResource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.XMLReader;

/**
 * Class that deals with the XFA file that can be inside a PDF file.
 */
public class XfaFile implements OutputStreamResource {

    /**
     * The X4J Document object (XML).
     */
    protected Document xfaDocument;

    /**
     * Constructs an XFA file from an OutputStreamResource. This resource can be an
     * XML file or a node in a RUPS
     * application.
     *
     * @param resource the XFA resource
     * @throws IOException       thrown when an I/O operation goes wrong
     * @throws DocumentException thrown when something goes wrong with a Document
     */
    public XfaFile(OutputStreamResource resource) throws IOException, DocumentException {
        // Creating a piped stream to avoid loading everything in memory
        try (PipedOutputStream pos = new PipedOutputStream();
            PipedInputStream pis = new PipedInputStream(pos)) {
            // Write to piped output stream
            new Thread(() -> {
                try {
                    resource.writeTo(pos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Parsing del PipedInputStream
            SAXReader reader = createSAXReader();
            xfaDocument = reader.read(pis);
        }
    }

    private SAXReader createSAXReader() throws DocumentException {
        SAXReader reader = new SAXReader();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            reader.setXMLReader(xmlReader);
        } catch (Exception e) {
            throw new DocumentException("Error configuring SAXReader", e);
        }
        return reader;
    }

    /**
     * Getter for the XFA Document object.
     *
     * @return a Document object (X4J)
     */
    public Document getXfaDocument() {
        return xfaDocument;
    }

    /**
     * Writes a formatted XML file to the OutputStream.
     *
     * @see com.lowagie.rups.io.OutputStreamResource#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream os) throws IOException {
        if (xfaDocument == null) {
            return;
        }
        OutputFormat format = new OutputFormat("   ", true);
        XMLWriter writer = new XMLWriter(os, format);
        writer.write(xfaDocument);
    }
}
