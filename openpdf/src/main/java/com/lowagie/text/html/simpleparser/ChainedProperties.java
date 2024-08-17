/*
 * Copyright 2004 Paulo Soares
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
 * Contributions by:
 * Lubos Strapko
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html.simpleparser;

import com.lowagie.text.ElementTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChainedProperties {

    public final static int[] fontSizes = {8, 10, 12, 14, 18, 24, 36};

    /**
     * Will be replaced with types alternative
     */
    public ArrayList<Object[]> chain = new ArrayList<>();

    /**
     * Creates a new instance of ChainedProperties
     */
    public ChainedProperties() {
    }

    public String getProperty(String key) {
        return findProperty(key).orElse(null);
    }

    /**
     * Try find property by its name
     *
     * @param key property name
     * @return {@link Optional} containing the value or {@link Optional#empty()} if there is no value or it equals
     * {@code null}
     */
    public Optional<String> findProperty(String key) {
        for (int k = chain.size() - 1; k >= 0; --k) {
            Object[] obj = chain.get(k);
            HashMap<?, ?> prop = (HashMap<?, ?>) obj[1];
            String ret = (String) prop.get(key);
            if (ret != null) {
                return Optional.of(ret);
            }
        }
        return Optional.empty();
    }

    /**
     * Get property by its name or return default value when property is not present or is <CODE>null</CODE>
     *
     * @param key          property name
     * @param defaultValue default property value
     * @return property or default value if it's null
     */
    public String getOrDefault(String key, String defaultValue) {
        return findProperty(key).orElse(defaultValue);
    }

    public boolean hasProperty(String key) {
        for (int k = chain.size() - 1; k >= 0; --k) {
            Object[] obj = (Object[]) chain.get(k);
            HashMap<String, Integer> prop = (HashMap<String, Integer>) obj[1];
            if (prop.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public void addToChain(String key, Map<String, String> prop) {
        adjustFontSize(prop);
        chain.add(new Object[]{key, prop});
    }

    private void adjustFontSize(Map<String, String> prop) {
        String sizeValue = prop.get(ElementTags.SIZE);
        if (sizeValue == null) {
            return;
        }

        try {
            int fontSizeIndex = calculateFontSizeIndex(sizeValue);
            prop.put(ElementTags.SIZE, Integer.toString(fontSizes[fontSizeIndex]));
        } catch (NumberFormatException e) {
            // Handle invalid size value
            // For example: log a warning or set a default size
        }
    }

    private int calculateFontSizeIndex(String sizeValue) {
        if (sizeValue.endsWith("pt")) {
            sizeValue = sizeValue.substring(0, sizeValue.length() - 2);
        }

        int baseFontSize = Integer.parseInt(getOrDefault("basefontsize", "12"));
        int sizeDelta = parseSizeDelta(sizeValue);
        int index = findFontSizeIndex(baseFontSize + sizeDelta);
        return Math.max(0, Math.min(index, fontSizes.length - 1));
    }

    private int parseSizeDelta(String sizeValue) {
        if (sizeValue.startsWith("+") || sizeValue.startsWith("-")) {
            return Integer.parseInt(sizeValue.substring(1));
        } else {
            return Integer.parseInt(sizeValue) - 1;
        }
    }

    private int findFontSizeIndex(int targetSize) {
        for (int k = fontSizes.length - 1; k >= 0; --k) {
            if (targetSize >= fontSizes[k]) {
                return k;
            }
        }
        return 0;
    }

    public void removeChain(String key) {
        for (int k = chain.size() - 1; k >= 0; --k) {
            if (key.equals(((Object[]) chain.get(k))[0])) {
                chain.remove(k);
                return;
            }
        }
    }
}
