/*
 * $Id: JavaScriptAction.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */

package com.lowagie.examples.html;

import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.Header;
import com.lowagie.text.Phrase;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.HtmlWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.lowagie.tools.SplitPdf.logger;

/**
 * Creates a documents with different named actions.
 *
 * @author blowagie
 */

public class JavaScriptAction {

    /**
     * Creates a document with Named Actions.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Open Application");

        // step 1: creation of a document-object

        try (Document document = new Document()) {
            // step 2:
            HtmlWriter.getInstance(document, new FileOutputStream("JavaScriptAction.html"));

            // step 3: we add Javascript as Metadata and we open the document

            String javaScriptSection = """
                    \t\tfunction load() {
                    \t\t  alert('Page has been loaded.');
                    \t\t}
                    \t\tfunction unload(){
                    \t\t  alert('Page has been unloaded.');
                    \t\t}
                    \t\tfunction sayHi(){
                    \t\t  alert('Hi !!!');
                    \t\t}""";

            document.add(new Header(HtmlTags.JAVASCRIPT, javaScriptSection));
            document.setJavascriptOnload("load()");
            document.setJavascriptOnunload("unload()");

            document.open();

            // step 4: we add some content
            Phrase phrase1 = new Phrase("""
                    There are 3 JavaScript functions in the HTML page, load(), unload() and sayHi().
                        The first one will be called when the HTML page has been loaded by your browser.
                        The second one will be called when the HTML page is being unloaded,
                        for example when you go to another page.""");
            document.add(phrase1);

            // add a HTML link <A HREF="...">
            Anchor anchor = new Anchor("Click here to execute the third JavaScript function.");
            anchor.setReference("JavaScript:sayHi()");
            document.add(anchor);

        } catch (IOException de) {
            logger.severe("Error occurred while creating JavaScriptAction document: " + de.getMessage());
        }
        // step 5: we close the document
    }
}

