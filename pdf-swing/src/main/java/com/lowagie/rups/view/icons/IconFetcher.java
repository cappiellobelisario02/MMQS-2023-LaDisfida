/*
 * $Id: IconFetcher.java 3117 2008-01-31 05:53:22Z xlv $
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

package com.lowagie.rups.view.icons;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class that fetches the icons in com.lowagie.trapeze.icons.
 */
public class IconFetcher {

    static final Logger logger = Logger.getLogger(IconFetcher.class.getName());

    private IconFetcher(){
        //empty on purpose
    }

    /**
     * Cache with icons.
     */
    protected static HashMap<String, Icon> cache = new HashMap<>();

    /**
     * Gets an Icon with a specific getName.
     *
     * @param filename the filename of the Icon.
     * @return an Icon
     */
    public static Icon getIcon(String filename) {
        if (filename == null) {
            return null;
        }

        Icon icon = cache.get(filename);
        if (icon == null) {
            try {
                // Attempt to load the icon from resources
                icon = new ImageIcon(IconFetcher.class.getResource(filename));

                // Check if the icon was successfully loaded
                if (icon.getIconWidth() < 0 || icon.getIconHeight() < 0) {
                    throw new IOException("Icon resource could not be loaded: " + filename);
                }

                // Cache the loaded icon
                cache.put(filename, icon);
            } catch (NullPointerException e) {
                // Handle the case where the resource is not found
                logger.info("Resource not found: " + filename);
                return null;
            } catch (IOException e) {
                // Handle IO exceptions specifically
                logger.severe("Failed to load icon from file: " + filename + ". " + e.getMessage());
                return null;
            } catch (Exception e) {
                // Log any other unexpected exceptions
                logger.severe("Unexpected error while loading icon: " + filename + ". " + e.getMessage());
                return null;
            }
        }
        return icon;
    }

}
