/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lowagie.text.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author psoares
 */
public class XmlDomWriter {

    /**
     * Print writer.
     */
    protected PrintWriter fOut;

    /**
     * Canonical output.
     */
    protected boolean fCanonical;

    /**
     * Processing XML 1.1 document.
     */
    protected boolean fXML11;

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    public XmlDomWriter() {
    } // <init>()

    public XmlDomWriter(boolean canonical) {
        fCanonical = canonical;
    } // <init>(boolean)

    //
    // Public methods
    //

    /**
     * Sets whether output is canonical.
     *
     * @param canonical true if canonical, false otherwise
     */
    public void setCanonical(boolean canonical) {
        fCanonical = canonical;
    } // setCanonical(boolean)

    /**
     * Sets the output stream for printing.
     *
     * @param stream   the OutputString
     * @param encoding the encoding
     * @throws UnsupportedEncodingException on error of encoding
     */
    public void setOutput(OutputStream stream, String encoding)
            throws UnsupportedEncodingException {

        if (encoding == null) {
            encoding = "UTF8";
        }

        java.io.Writer writer = new OutputStreamWriter(stream, encoding);
        fOut = new PrintWriter(writer);

    } // setOutput(OutputStream,String)

    /**
     * Sets the output writer.
     *
     * @param writer the writer to set
     */
    public void setOutput(java.io.Writer writer) {

        fOut = writer instanceof PrintWriter printWriter
                ? printWriter : new PrintWriter(writer);

    } // setOutput(java.io.Writer)

    /**
     * Writes the specified node, recursively.
     *
     * @param node the Node to write
     */
    public void write(Node node) {
        if (node == null) {
            return;
        }

        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                writeDocumentNode((Document) node);
                break;

            case Node.DOCUMENT_TYPE_NODE:
                writeDocumentTypeNode((DocumentType) node);
                break;

            case Node.ELEMENT_NODE:
                writeElementNode(node);
                break;

            case Node.ENTITY_REFERENCE_NODE:
                writeEntityReferenceNode(node);
                break;

            case Node.CDATA_SECTION_NODE:
                writeCDATASectionNode(node);
                break;

            case Node.TEXT_NODE:
                writeTextNode(node);
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                writeProcessingInstructionNode(node);
                break;

            case Node.COMMENT_NODE:
                writeCommentNode(node);
                break;

            default:
                // handle other node types if needed
                break;
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            closeElementNode(node);
        }
    }

// Helper methods for each node getTypeImpl

    private void writeDocumentNode(Document document) {
        fXML11 = false; // "1.1".equals(getVersion(document))
        if (!fCanonical) {
            fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            fOut.flush();
            write(document.getDoctype());
        }
        write(document.getDocumentElement());
    }

    private void writeDocumentTypeNode(DocumentType doctype) {
        fOut.print("<!DOCTYPE ");
        fOut.print(doctype.getName());
        if (doctype.getPublicId() != null) {
            fOut.print(" PUBLIC '");
            fOut.print(doctype.getPublicId());
            fOut.print("' '");
            fOut.print(doctype.getSystemId());
            fOut.print("'");
        } else if (doctype.getSystemId() != null) {
            fOut.print(" SYSTEM '");
            fOut.print(doctype.getSystemId());
            fOut.print("'");
        }
        if (doctype.getInternalSubset() != null) {
            fOut.print(" [");
            fOut.print(doctype.getInternalSubset());
            fOut.print(']');
        }
        fOut.println('>');
    }

    private void writeElementNode(Node node) {
        fOut.print('<');
        fOut.print(node.getNodeName());
        Attr[] attrs = sortAttributes(node.getAttributes());
        for (Attr attr : attrs) {
            fOut.print(' ');
            fOut.print(attr.getNodeName());
            fOut.print("=\"");
            normalizeAndPrint(attr.getNodeValue(), true);
            fOut.print('"');
        }
        fOut.print('>');
        fOut.flush();
        writeChildNodes(node);
    }

    private void writeEntityReferenceNode(Node node) {
        if (fCanonical) {
            writeChildNodes(node);
        } else {
            fOut.print('&');
            fOut.print(node.getNodeName());
            fOut.print(';');
            fOut.flush();
        }
    }

    private void writeCDATASectionNode(Node node) {
        if (fCanonical) {
            normalizeAndPrint(node.getNodeValue(), false);
        } else {
            fOut.print("<![CDATA[");
            fOut.print(node.getNodeValue());
            fOut.print("]]>");
        }
        fOut.flush();
    }

    private void writeTextNode(Node node) {
        normalizeAndPrint(node.getNodeValue(), false);
        fOut.flush();
    }

    private void writeProcessingInstructionNode(Node node) {
        fOut.print("<?");
        fOut.print(node.getNodeName());
        String data = node.getNodeValue();
        if (data != null && !data.isEmpty()) {
            fOut.print(' ');
            fOut.print(data);
        }
        fOut.print("?>");
        fOut.flush();
    }

    private void writeCommentNode(Node node) {
        if (!fCanonical) {
            fOut.print("<!--");
            fOut.print(node.getNodeValue());
            fOut.print("-->");
            fOut.flush();
        }
    }

    private void closeElementNode(Node node) {
        fOut.print("</");
        fOut.print(node.getNodeName());
        fOut.print('>');
        fOut.flush();
    }

    // Helper method to write child nodes
    private void writeChildNodes(Node node) {
        Node child = node.getFirstChild();
        while (child != null) {
            write(child);
            child = child.getNextSibling();
        }
    }


    /**
     * Returns a sorted list of attributes.
     *
     * @param attrs the NameNomeMap of attributes
     * @return an Attr array of sorted attributes
     */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {

        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr[] array = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr) attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }

        return array;

    } // sortAttributes(NamedNodeMap):Attr[]

    //
    // Protected methods
    //

    /**
     * Normalizes and prints the given string.
     *
     * @param isAttValue boolean
     * @param s          the String to normalize and print
     */
    protected void normalizeAndPrint(String s, boolean isAttValue) {

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c, isAttValue);
        }

    } // normalizeAndPrint(String,boolean)

    /**
     * Normalizes and print the given character.
     *
     * @param c          the character
     * @param isAttValue boolean
     */
    protected void normalizeAndPrint(char c, boolean isAttValue) {

        switch (c) {
            case '<': {
                fOut.print("&lt;");
                break;
            }
            case '>': {
                fOut.print("&gt;");
                break;
            }
            case '&': {
                fOut.print("&amp;");
                break;
            }
            case '"': {
                // A '"' that appears in character data
                // does not need to be escaped.
                if (isAttValue) {
                    fOut.print("&quot;");
                } else {
                    fOut.print("\"");
                }
                break;
            }
            case '\r': {
                // If CR is part of the document's content, it
                // must not be printed as a literal otherwise
                // it would be normalized to LF when the document
                // is reparsed.
                fOut.print("&#xD;");
                break;
            }
            case '\n':
                if (fCanonical) {
                    fOut.print("&#xA;");
                    break;
                }
                // fallthrough
            default: {
                // In XML 1.1, control chars in the ranges [#x1-#x1F, #x7F-#x9F] must be escaped.
                //
                // Escape space characters that would be normalized to #x20 in attribute values
                // when the document is reparsed.
                //
                // Escape NEL (0x85) and LSEP (0x2028) that appear in content
                // if the document is XML 1.1, since they would be normalized to LF
                // when the document is reparsed.
                if (fXML11 && ((c >= 0x01 && c <= 0x1F && c != 0x09 && c != 0x0A)
                        || (c >= 0x7F && c <= 0x9F) || c == 0x2028)
                        || isAttValue && (c == 0x09 || c == 0x0A)) {
                    fOut.print("&#x");
                    fOut.print(Integer.toHexString(c).toUpperCase());
                    fOut.print(";");
                } else {
                    fOut.print(c);
                }
            }
        }
    } // normalizeAndPrint(char,boolean)

    /* Extracts the XML version from the Document. */
//    protected String getVersion(Document document)
//        if document == nul
//            return null
//
//        String version = null
//        Method getXMLVersion = null
//        try
//            getXMLVersion = document.getClass().getMethod("getXmlVersion", new Class[]{})
//            // If Document class implements DOM L3, this method will exist.
//            if getXMLVersion != null
//                version = (String) getXMLVersion.invoke(document, (Object[]) null)
//
//         catch Exception e
//            // Either this locator object doesn't have
//            // this method, or we're on an old JDK
//
//        return version
//    } // getVersion(Document
}
