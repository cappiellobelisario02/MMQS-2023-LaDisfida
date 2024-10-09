/*
 * $Id: BuildTutorial.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 by Bruno Lowagie.
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
package com.lowagie.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class can be used to build the iText website.
 *
 * @author Bruno Lowagie
 */
public class BuildTutorial {

    static String root;
    static FileWriter build;

    public static final Logger logger = Logger.getLogger(BuildTutorial.class.getName());

    //~ Methods
    // ----------------------------------------------------------------

    /**
     * Main method so you can call the convert method from the command line.
     *
     * @param args 4 arguments are expected:
     *             <ul><li>a sourcedirectory (root of the tutorial xml-files),
     *             <li>a destination directory (where the html and build.xml files will be generated),
     *             <li>an xsl to transform the index.xml into a build.xml
     *             <li>an xsl to transform the index.xml into am index.html</ul>
     */

    public static void main(String[] args) {
        if (args.length == 4) {
            File srcdir = new File(args[0]);
            File destdir = new File(args[1]);
            File xslExamples = new File(srcdir, args[2]);
            File xslSite = new File(srcdir, args[3]);

            // Validate source and destination directories
            if (!srcdir.isDirectory() || !destdir.isDirectory()) {
                logger.severe("Source or destination directory is invalid.");
                return;
            }

            // Get canonical paths for security checks
            try {
                String root = new File(destdir, srcdir.getName()).getCanonicalPath();
                String allowedDirectory = "/allowed/directory/"; // Replace with your allowed directory

                // Check if the root path is within an allowed directory
                if (!root.startsWith(allowedDirectory)) {
                    logger.severe("Access to the directory is denied: " + root);
                    return;
                }

                logger.info("Building tutorial: ");
                logger.info(root);

                // Use try-with-resources to ensure proper closure of FileWriter
                try (FileWriter build = new FileWriter(new File(root, "build.xml"))) {
                    build.write("<project name=\"tutorial\" default=\"all\" basedir=\".\">\n");
                    build.write("<target name=\"all\">\n");
                    action(srcdir, destdir, xslExamples, xslSite);
                    build.write("</target>\n</project>");
                    build.flush();
                } catch (IOException ioe) {
                    logger.severe("I/O error occurred: " + ioe.getMessage());
                }
            } catch (IOException e) {
                logger.severe("Error obtaining canonical path: " + e.getMessage());
            }
        } else {
            logger.severe("Wrong number of parameters.\nUsage: BuildSite srcdir destdir xsl_examples xsl_site");
        }
    }


    // Helper method to validate and sanitize file paths
    private static File validatePath(String path) {
        File file = new File(path);

        // Prevent directory traversal attacks by checking the canonical path
        try {
            String canonicalPath = file.getCanonicalPath();
            String userHomePath = new File(System.getProperty("user.home")).getCanonicalPath();
            if (!canonicalPath.startsWith(userHomePath)) {
                throw new SecurityException("Path manipulation attempt detected: " + path);
            }
        } catch (IOException e) {
            throw new SecurityException("Invalid file path: " + path);
        }

        return file;
    }


    /**
     * Inspects a file or directory that is given and performs the necessary actions on it (transformation or
     * recursion).
     *
     * @param source        a sourcedirectory (possibly with a tutorial xml-file)
     * @param destination   a destination directory (where the html and build.xml file will be generated, if necessary)
     * @param xslExamplesIn an xsl to transform the index.xml into a build.xml
     * @param xslSiteIn     an xsl to transform the index.xml into am index.html
     * @throws IOException when something goes wrong while reading or creating a file or directory
     */
    public static void action(File source, File destination, File xslExamplesIn, File xslSiteIn) throws IOException {
        // Define a base directory for safe operations
        File baseDirectory = new File("/expected/base/directory");

        // Check if the source is part of the allowed base directory
        if (!source.getCanonicalPath().startsWith(baseDirectory.getCanonicalPath())) {
            throw new SecurityException("Access to the specified path is denied.");
        }

        if (".svn".equals(source.getName())) {
            return;
        }

        logger.info(source.getName());

        if (source.isDirectory()) {
            logger.info(" ");
            logger.info(source.getCanonicalPath());
            File dest = new File(destination, source.getName());
            File[] xmlFiles = source.listFiles();

            if (xmlFiles != null) {
                for (File xmlFile : xmlFiles) {
                    action(xmlFile, dest, xslExamplesIn, xslSiteIn);
                }
            } else {
                logger.info("... skipped");
            }
        } else if (source.getName().equals("index.xml")) {
            logger.info("... transformed");
            convert(source, xslSiteIn, new File(destination, "index.php"));

            File buildfile = new File(destination, "build.xml");
            String path = buildfile.getCanonicalPath().substring(root.length());
            path = path.replace(File.separatorChar, '/');

            if ("/build.xml".equals(path)) {
                return;
            }

            convert(source, xslExamplesIn, buildfile);
            build.write("\t<ant antfile=\"${basedir}" + path + "\" target=\"install\" inheritAll=\"false\" />\n");
        } else {
            logger.info("... skipped");
        }
    }

    // Additional method to validate XML files
    private static boolean isValidXmlFile(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".xml");
    }


    /**
     * Converts an <code>infile</code>, using an <code>xslfile</code> to an
     * <code>outfile</code>.
     *
     * @param infile  the path to an XML file
     * @param xslfile the path to the XSL file
     * @param outfile the path for the output file
     */
    public static void convert(File infile, File xslfile, File outfile) {
        // Use try-with-resources to ensure all streams are closed properly
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // Use the factory to create a template containing the xsl file
            Templates template;
            try (FileInputStream xslInputStream = new FileInputStream(xslfile)) {
                template = factory.newTemplates(new StreamSource(xslInputStream));
            }

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // Passing 2 parameters
            String branch = outfile.getParentFile().getCanonicalPath().substring(root.length());
            branch = branch.replace(File.separatorChar, '/');
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < branch.length(); i++) {
                if (branch.charAt(i) == '/') {
                    path.append("/pdf-core/src/test");
                }
            }

            xformer.setParameter("branch", branch);
            xformer.setParameter("root", path.toString());

            // Prepare the input and output files
            try (FileInputStream inputFileStream = new FileInputStream(infile);
                    FileOutputStream outputFileStream = new FileOutputStream(outfile)) {
                Source source = new StreamSource(inputFileStream);
                Result result = new StreamResult(outputFileStream);

                // Apply the xsl file to the source file and write the result to the output file
                xformer.transform(source, result);
            }
        } catch (Exception e) {
            // Log the exception for better diagnostics
            logger.severe("Error during transformation: " + e.getMessage());
        }
    }
}

//The End
