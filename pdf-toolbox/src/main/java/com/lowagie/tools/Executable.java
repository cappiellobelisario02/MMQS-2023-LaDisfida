/*
 * $Id: Executable.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2005 by Bruno Lowagie / Roger Mistelli
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

import com.lowagie.text.error_messages.MessageLocalization;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class enables you to call an executable that will show a PDF file.
 */
public class Executable {

    private Executable(){
        //empty on purpose
    }
    /**
     * The path to Acrobat Reader.
     */
    private static String acroread = null;
    static Logger logger = Logger.getLogger(com.lowagie.tools.Executable.class.getName());

    /**
     * Performs an action on a PDF document.
     *
     * @param fileName name of the file
     * @param parameters string of parameters
     * @param waitForTermination has to wait for termination
     * @return a process
     * @throws IOException exception thrown by the method body
     */
    private static Process action(final String fileName,
            String parameters, boolean waitForTermination) throws IOException {
        Process process = null;
        String sanitizedFileName = sanitizeInput(fileName);
        String sanitizedParameters = sanitizeInput(parameters.trim());
        String commandEnv;
        String commandDisk;
        String commandStart = "start";
        String commandProgramStart = "acrord32";


        if (acroread != null) {
            process = Runtime.getRuntime().exec(new String[]{
                    acroread, sanitizedParameters, sanitizedFileName});
        } else if (isWindows()) {
            if (isWindows9X()) {
                commandEnv = "command.com";
                commandDisk = "/C";
                String[] command = {commandEnv, commandDisk, commandStart, commandProgramStart, sanitizedParameters,
                        sanitizedFileName};
                process = Runtime.getRuntime().exec(command);
            } else {
                commandEnv = "cmd";
                commandDisk = "/c";
                String[] command = {commandEnv, commandDisk, commandStart, commandProgramStart, sanitizedParameters,
                        sanitizedFileName};
                process = Runtime.getRuntime().exec(command);
            }
        } else if (isMac()) {
            if (sanitizedParameters.isEmpty()) {
                process = Runtime.getRuntime().exec(new String[]{"/usr/bin/open", sanitizedFileName});
            } else {
                process = Runtime.getRuntime().exec(new String[]{"/usr/bin/open", sanitizedParameters, sanitizedFileName});
            }
        }
        try {
            if (process != null && waitForTermination) {
                process.waitFor();
            }
        } catch (ThreadDeath | InterruptedException e) {
            process.destroy();
            logger.log(Level.SEVERE, "Interrupted.", e);
            throw new ThreadDeath();
        }
        return process;
    }

    private static String sanitizeInput(String input) {
        // Example of a simple sanitization. Customize this to your needs.
        return input.replaceAll("[^a-zA-Z0-9._/-]", "");
    }


    /**
     * Creates a command string array from the string arguments.
     *
     * @param arguments list of arguments for the command
     * @return String[] of commands
     */
    private static String[] createCommand(String... arguments) {
        return arguments;
    }

    /**
     * Opens a PDF document.
     *
     * @param fileName           the getName of the file to open
     * @param waitForTermination true to wait for termination, false otherwise
     * @return a process
     * @throws IOException on error
     */
    public static Process openDocument(String fileName,
            boolean waitForTermination) throws IOException {
        return action(fileName, "", waitForTermination);
    }

    /**
     * Opens a PDF document.
     *
     * @param file               the file to open
     * @param waitForTermination true to wait for termination, false otherwise
     * @throws IOException on error
     */
    public static void openDocument(File file,
            boolean waitForTermination) throws IOException {
        openDocument(file.getAbsolutePath(), waitForTermination);
    }

    /**
     * Opens a PDF document.
     *
     * @param fileName the getName of the file to open
     * @return a process
     * @throws IOException on error
     */
    public static Process openDocument(String fileName) throws IOException {
        return openDocument(fileName, false);
    }

    /**
     * Opens a PDF document.
     *
     * @param file the file to open
     * @throws IOException on error
     */
    public static void openDocument(File file) throws IOException {
        openDocument(file, false);
    }

    /**
     * Prints a PDF document.
     *
     * @param fileName           the getName of the file to print
     * @param waitForTermination true to wait for termination, false otherwise
     * @return a process
     * @throws IOException on error
     */
    public static Process printDocument(String fileName,
            boolean waitForTermination) throws IOException {
        return action(fileName, "/p", waitForTermination);
    }

    /**
     * Prints a PDF document.
     *
     * @param file               the File to print
     * @param waitForTermination true to wait for termination, false otherwise
     * @throws IOException on error
     */
    public static void printDocument(File file,
            boolean waitForTermination) throws IOException {
        printDocument(file.getAbsolutePath(), waitForTermination);
    }

    /**
     * Prints a PDF document.
     *
     * @param fileName the getName of the file to print
     * @return a process
     * @throws IOException on error
     */
    public static Process printDocument(String fileName) throws IOException {
        return printDocument(fileName, false);
    }

    /**
     * Prints a PDF document.
     *
     * @param file the File to print
     * @throws IOException on error
     */
    public static void printDocument(File file) throws IOException {
        printDocument(file, false);
    }

    /**
     * Prints a PDF document without opening a Dialog box.
     *
     * @param fileName           the getName of the file to print
     * @param waitForTermination true to wait for termination, false otherwise
     * @return a process
     * @throws IOException on error
     */
    public static Process printDocumentSilent(String fileName,
            boolean waitForTermination) throws IOException {
        return action(fileName, "/p /h", waitForTermination);
    }

    /**
     * Prints a PDF document without opening a Dialog box.
     *
     * @param file               the File to print
     * @param waitForTermination true to wait for termination, false otherwise
     * @throws IOException on error
     */
    public static void printDocumentSilent(File file,
            boolean waitForTermination) throws IOException {
        printDocumentSilent(file.getAbsolutePath(), waitForTermination);
    }

    /**
     * Prints a PDF document without opening a Dialog box.
     *
     * @param fileName the getName of the file to print
     * @return a process
     * @throws IOException on error
     */
    public static Process printDocumentSilent(String fileName) throws IOException {
        return printDocumentSilent(fileName, false);
    }

    /**
     * Prints a PDF document without opening a Dialog box.
     *
     * @param file the File to print
     * @throws IOException on error
     */
    public static void printDocumentSilent(File file) throws IOException {
        printDocumentSilent(file, false);
    }

    /**
     * Launches a browser opening an URL.
     *
     * @param url the URL you want to open in the browser
     * @throws IOException on error
     */
    public static void launchBrowser(String url) throws IOException, InterruptedException, ReflectiveOperationException {
        try {
            if (isMac()) {
                launchMacBrowser(url);
            } else if (isWindows()) {
                Runtime.getRuntime().exec(createCommand("rundll32 url.dll,FileProtocolHandler ", url));
            } else { //assume Unix or Linux
                String[] browsers = {
                        "firefox", "opera", "konqueror", "mozilla", "netscape"};
                String browser = null;
                String which = "which";
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{which, browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new IOException(MessageLocalization.getComposedMessage("could.not.find.web.browser"));
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (InterruptedException e) {
            throw new InterruptedException(MessageLocalization.getComposedMessage("error.attempting.to.launch.web.browser"));
        } catch (ReflectiveOperationException e) {
            throw new ReflectiveOperationException("Error >> ", e);
        }
    }

    private static void launchMacBrowser(String url) throws ReflectiveOperationException {
        try{
            Class<?> macUtils = Class.forName("com.apple.mrj.MRJFileUtils");
            Method openURL = macUtils.getDeclaredMethod("openURL", String.class);
            openURL.invoke(null, url);
        } catch(ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new ReflectiveOperationException("Error >> ", e);
        }
    }

    /**
     * Checks the Operating System.
     *
     */
    private static final String OS = "os.getName";
    public static boolean isWindows() {
        String os = System.getProperty(OS).toLowerCase();
        return os.contains("windows") || os.contains("nt");
    }

    /**
     * Checks the Operating System.
     *
     * @return true if the current os is Windows
     */
    public static boolean isWindows9X() {
        String os = System.getProperty(OS).toLowerCase();
        return os.equals("windows 95") || os.equals("windows 98");
    }

    /**
     * Checks the Operating System.
     *
     * @return true if the current os is Apple
     */
    public static boolean isMac() {
        String os = System.getProperty(OS).toLowerCase();
        return os.contains("mac");
    }

    /**
     * Checks the Operating System.
     *
     * @return true if the current os is Linux
     */
    public static boolean isLinux() {
        String os = System.getProperty(OS).toLowerCase();
        return os.contains("linux");
    }
}
