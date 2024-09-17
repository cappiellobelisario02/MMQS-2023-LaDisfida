/*
 * $Id: PdfViewerPreferencesImp.java 3867 2009-04-17 17:49:57Z blowagie $
 *
 * Copyright 2006 Bruno Lowagie
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

package com.lowagie.text.pdf.internal;

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;

/**
 * Stores the information concerning viewer preferences, and contains the business logic that allows you to set viewer
 * preferences.
 */

public class PdfViewerPreferencesImp implements PdfViewerPreferences {

    public static final PdfName[] VIEWER_PREFERENCES = {
            PdfName.HIDETOOLBAR,            // 0
            PdfName.HIDEMENUBAR,            // 1
            PdfName.HIDEWINDOWUI,           // 2
            PdfName.FITWINDOW,              // 3
            PdfName.CENTERWINDOW,            // 4
            PdfName.DISPLAYDOCTITLE,        // 5
            PdfName.NONFULLSCREENPAGEMODE,    // 6
            PdfName.DIRECTION,                // 7
            PdfName.VIEWAREA,                // 8
            PdfName.VIEWCLIP,                // 9
            PdfName.PRINTAREA,                // 10
            PdfName.PRINTCLIP,                // 11
            PdfName.PRINTSCALING,            // 12
            PdfName.DUPLEX,                    // 13
            PdfName.PICKTRAYBYPDFSIZE,        // 14
            PdfName.PRINTPAGERANGE,            // 15
            PdfName.NUMCOPIES                // 16
    };


    /**
     * A series of viewer preferences.
     */
    public static final PdfName[] NONFULLSCREENPAGEMODE_PREFERENCES = {
            PdfName.USENONE, PdfName.USEOUTLINES, PdfName.USETHUMBS, PdfName.USEOC
    };
    /**
     * A series of viewer preferences.
     */
    public static final PdfName[] DIRECTION_PREFERENCES = {
            PdfName.L2R, PdfName.R2L
    };
    /**
     * A series of viewer preferences.
     */
    public static final PdfName[] PAGE_BOUNDARIES = {
            PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX
    };
    /**
     * A series of viewer preferences
     */
    public static final PdfName[] PRINTSCALING_PREFERENCES = {
            PdfName.APPDEFAULT, PdfName.NONE
    };
    /**
     * A series of viewer preferences.
     */
    public static final PdfName[] DUPLEX_PREFERENCES = {
            PdfName.SIMPLEX, PdfName.DUPLEXFLIPSHORTEDGE, PdfName.DUPLEXFLIPLONGEDGE
    };
    /**
     * The mask to decide if a ViewerPreferences dictionary is needed
     */
    private static final int VIEWER_PREFERENCES_MASK = 0xfff000;
    /**
     * This value will hold the viewer preferences for the page layout and page mode.
     */
    private int pageLayoutAndMode = 0;
    /**
     * This dictionary holds the viewer preferences (other than page layout and page mode).
     */
    private PdfDictionary viewerPreferences = new PdfDictionary();

    public static com.lowagie.text.pdf.internal.PdfViewerPreferencesImp getViewerPreferences(PdfDictionary catalog) {
        com.lowagie.text.pdf.internal.PdfViewerPreferencesImp preferences = new com.lowagie.text.pdf.internal.PdfViewerPreferencesImp();
        int prefs = 0;

        prefs |= getPageLayoutPreferences(catalog);
        prefs |= getPageModePreferences(catalog);

        preferences.setViewerPreferences(prefs);
        addOtherViewerPreferences(preferences, catalog);

        return preferences;
    }

    private static int getPageLayoutPreferences(PdfDictionary catalog) {
        int prefs = 0;
        PdfObject obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.PAGELAYOUT));
        if (obj != null && obj.isName()) {
            PdfName name = (PdfName) obj;
            prefs |= getPageLayoutPreference(name);
        }
        return prefs;
    }

    private static int getPageLayoutPreference(PdfName name) {
        if (name.equals(PdfName.SINGLEPAGE)) {
            return PdfWriter.getPageLayoutSinglePage();
        } else if (name.equals(PdfName.ONECOLUMN)) {
            return PdfWriter.getPageLayoutOneColumn();
        } else if (name.equals(PdfName.TWOCOLUMNLEFT)) {
            return PdfWriter.getPageLayoutTwoColumnLeft();
        } else if (name.equals(PdfName.TWOCOLUMNRIGHT)) {
            return PdfWriter.getPageLayoutTwoColumnRight();
        } else if (name.equals(PdfName.TWOPAGELEFT)) {
            return PdfWriter.getPageLayoutTwoPageLeft();
        } else if (name.equals(PdfName.TWOPAGERIGHT)) {
            return PdfWriter.getPageLayoutTwoPageRight();
        }
        return 0;
    }

    private static int getPageModePreferences(PdfDictionary catalog) {
        int prefs = 0;
        PdfObject obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.PAGEMODE));
        if (obj != null && obj.isName()) {
            PdfName name = (PdfName) obj;
            prefs |= getPageModePreference(name);
        }
        return prefs;
    }

    private static int getPageModePreference(PdfName name) {
        if (name.equals(PdfName.USENONE)) {
            return PdfWriter.getPageModeUseNone();
        } else if (name.equals(PdfName.USEOUTLINES)) {
            return PdfWriter.getPageModeUseOutlines();
        } else if (name.equals(PdfName.USETHUMBS)) {
            return PdfWriter.getPageModeUseThumbs();
        } else if (name.equals(PdfName.FULLSCREEN)) {
            return PdfWriter.getPageModeFullScreen();
        } else if (name.equals(PdfName.USEOC)) {
            return PdfWriter.getPageModeUseOC();
        } else if (name.equals(PdfName.USEATTACHMENTS)) {
            return PdfWriter.getPageModeUseAttachments();
        }
        return 0;
    }

    private static void addOtherViewerPreferences(com.lowagie.text.pdf.internal.PdfViewerPreferencesImp preferences, PdfDictionary catalog) {
        PdfObject obj = PdfReader.getPdfObjectRelease(catalog.get(PdfName.VIEWERPREFERENCES));
        if (obj != null && obj.isDictionary()) {
            PdfDictionary vp = (PdfDictionary) obj;
            for (PdfName viewerPreference : VIEWER_PREFERENCES) {
                PdfObject preferenceObj = PdfReader.getPdfObjectRelease(vp.get(viewerPreference));
                preferences.addViewerPreference(viewerPreference, preferenceObj);
            }
        }
    }

    /**
     * Returns the page layout and page mode value.
     *
     * @return an int that hold the viewer preferences for the page layout and page mode.
     */
    public int getPageLayoutAndMode() {
        return pageLayoutAndMode;
    }

    /**
     * Returns the viewer preferences.
     *
     * @return a PdfDictionary containing the viewer's preferences.
     */
    public PdfDictionary getViewerPreferences() {
        return viewerPreferences;
    }

    /**
     * Sets the viewer preferences as the sum of several constants.
     *
     * @param preferences the viewer preferences
     * @see PdfViewerPreferences#setViewerPreferences
     */
    public void setViewerPreferences(int preferences) {
        this.pageLayoutAndMode |= preferences;

        // Handle backward compatibility viewer preferences
        if ((preferences & VIEWER_PREFERENCES_MASK) != 0) {
            pageLayoutAndMode = ~VIEWER_PREFERENCES_MASK & pageLayoutAndMode;

            setBooleanViewerPreferences(preferences);
            setNonFullScreenPageMode(preferences);
            setDirectionPreferences(preferences);
            setPrintScaling(preferences);
        }
    }

    private void setBooleanViewerPreferences(int preferences) {
        if ((preferences & PdfWriter.getHideToolbar()) != 0) {
            viewerPreferences.put(PdfName.HIDETOOLBAR, PdfBoolean.PDFTRUE);
        }
        if ((preferences & PdfWriter.getHideMenubar()) != 0) {
            viewerPreferences.put(PdfName.HIDEMENUBAR, PdfBoolean.PDFTRUE);
        }
        if ((preferences & PdfWriter.getHideWindowUI()) != 0) {
            viewerPreferences.put(PdfName.HIDEWINDOWUI, PdfBoolean.PDFTRUE);
        }
        if ((preferences & PdfWriter.getFitWindow()) != 0) {
            viewerPreferences.put(PdfName.FITWINDOW, PdfBoolean.PDFTRUE);
        }
        if ((preferences & PdfWriter.getCenterWindow()) != 0) {
            viewerPreferences.put(PdfName.CENTERWINDOW, PdfBoolean.PDFTRUE);
        }
        if ((preferences & PdfWriter.getDisplayDocTitle()) != 0) {
            viewerPreferences.put(PdfName.DISPLAYDOCTITLE, PdfBoolean.PDFTRUE);
        }
    }

    private void setNonFullScreenPageMode(int preferences) {
        if ((preferences & PdfWriter.getNonFullScreenPageModeUseNone()) != 0) {
            viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USENONE);
        } else if ((preferences & PdfWriter.getNonFullScreenPageModeUseOutlines()) != 0) {
            viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USEOUTLINES);
        } else if ((preferences & PdfWriter.getNonFullScreenPageModeUseThumbs()) != 0) {
            viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USETHUMBS);
        } else if ((preferences & PdfWriter.getNonFullScreenPageModeUseOC()) != 0) {
            viewerPreferences.put(PdfName.NONFULLSCREENPAGEMODE, PdfName.USEOC);
        }
    }

    private void setDirectionPreferences(int preferences) {
        if ((preferences & PdfWriter.getDirectionL2R()) != 0) {
            viewerPreferences.put(PdfName.DIRECTION, PdfName.L2R);
        } else if ((preferences & PdfWriter.getDirectionR2L()) != 0) {
            viewerPreferences.put(PdfName.DIRECTION, PdfName.R2L);
        }
    }

    private void setPrintScaling(int preferences) {
        if ((preferences & PdfWriter.getPrintScalingNone()) != 0) {
            viewerPreferences.put(PdfName.PRINTSCALING, PdfName.NONE);
        }
    }

    /**
     * Given a key for a viewer preference (a PdfName object), this method returns the index in the VIEWER_PREFERENCES
     * array.
     *
     * @param key a PdfName referring to a viewer preference
     * @return an index in the VIEWER_PREFERENCES array
     */
    private int getIndex(PdfName key) {
        for (int i = 0; i < VIEWER_PREFERENCES.length; i++) {
            if (VIEWER_PREFERENCES[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if some value is valid for a certain key.
     */
    private boolean isPossibleValue(PdfName value, PdfName[] accepted) {
        for (PdfName pdfName : accepted) {
            if (pdfName.equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the viewer preferences for printing.
     */
    public void addViewerPreference(PdfName key, PdfObject value) {
        int index = getIndex(key);

        if (isBooleanPreference(index, value)) {
            viewerPreferences.put(key, value);
        } else if (isNamePreference(index, value, NONFULLSCREENPAGEMODE_PREFERENCES, 6)) {
            viewerPreferences.put(key, value);
        } else if (isNamePreference(index, value, DIRECTION_PREFERENCES, 7)) {
            viewerPreferences.put(key, value);
        } else if (isNamePreference(index, value, PAGE_BOUNDARIES, 8, 9, 10, 11)) {
            viewerPreferences.put(key, value);
        } else if (isNamePreference(index, value, PRINTSCALING_PREFERENCES, 12)) {
            viewerPreferences.put(key, value);
        } else if (isNamePreference(index, value, DUPLEX_PREFERENCES, 13)) {
            viewerPreferences.put(key, value);
        } else if (index == 15 && value instanceof PdfArray) {
            viewerPreferences.put(key, value);
        } else if (index == 16 && value instanceof PdfNumber) {
            viewerPreferences.put(key, value);
        }
    }

    private boolean isBooleanPreference(int index, PdfObject value) {
        return value instanceof PdfBoolean && (index >= 0 && index <= 5 || index == 14);
    }

    private boolean isNamePreference(int index, PdfObject value, PdfName[] validValues, int... validIndices) {
        if (value instanceof PdfName) {
            for (int i : validIndices) {
                if (index == i && isPossibleValue((PdfName) value, validValues)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Adds the viewer preferences defined in the preferences parameter to a PdfDictionary (more specifically the root
     * or catalog of a PDF file).
     *
     * @param catalog The PdfDictionary to add the viewer preferences to
     */
    public void addToCatalog(PdfDictionary catalog) {
        // Page Layout
        catalog.remove(PdfName.PAGELAYOUT);
        if ((pageLayoutAndMode & PdfWriter.getPageLayoutSinglePage()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.SINGLEPAGE);
        } else if ((pageLayoutAndMode & PdfWriter.getPageLayoutOneColumn()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.ONECOLUMN);
        } else if ((pageLayoutAndMode & PdfWriter.getPageLayoutTwoColumnLeft()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOCOLUMNLEFT);
        } else if ((pageLayoutAndMode & PdfWriter.getPageLayoutTwoColumnRight()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOCOLUMNRIGHT);
        } else if ((pageLayoutAndMode & PdfWriter.getPageLayoutTwoPageLeft()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOPAGELEFT);
        } else if ((pageLayoutAndMode & PdfWriter.getPageLayoutTwoPageRight()) != 0) {
            catalog.put(PdfName.PAGELAYOUT, PdfName.TWOPAGERIGHT);
        }

        // Page Mode
        catalog.remove(PdfName.PAGEMODE);
        if ((pageLayoutAndMode & PdfWriter.getPageModeUseNone()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USENONE);
        } else if ((pageLayoutAndMode & PdfWriter.getPageModeUseOutlines()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOUTLINES);
        } else if ((pageLayoutAndMode & PdfWriter.getPageModeUseThumbs()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USETHUMBS);
        } else if ((pageLayoutAndMode & PdfWriter.getPageModeFullScreen()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.FULLSCREEN);
        } else if ((pageLayoutAndMode & PdfWriter.getPageModeUseOC()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEOC);
        } else if ((pageLayoutAndMode & PdfWriter.getPageModeUseAttachments()) != 0) {
            catalog.put(PdfName.PAGEMODE, PdfName.USEATTACHMENTS);
        }

        // viewer preferences (Table 8.1 of the PDF Reference)
        catalog.remove(PdfName.VIEWERPREFERENCES);
        if (viewerPreferences.size() > 0) {
            catalog.put(PdfName.VIEWERPREFERENCES, viewerPreferences);
        }
    }
}
