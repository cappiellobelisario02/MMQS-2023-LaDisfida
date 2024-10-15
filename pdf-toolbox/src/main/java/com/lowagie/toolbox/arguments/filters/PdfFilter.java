package com.lowagie.rups.io.filters;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.util.Locale;


/**
 * Filters PDF files in a JFileChooser.
 */
public class PdfFilter extends FileFilter {

    /**
     * A public instance of the PdfFilter.
     */
    public static final com.lowagie.rups.io.filters.PdfFilter INSTANCE = new com.lowagie.rups.io.filters.PdfFilter();

    /**
     * @param f File
     * @return boolean
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        // Use Locale.ENGLISH for consistent behavior across different environments
        String fileName = f.getName().toLowerCase(Locale.ENGLISH);
        return fileName.endsWith(".pdf");
    }


    /**
     * @return String
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return "*.pdf PDF files";
    }

}
