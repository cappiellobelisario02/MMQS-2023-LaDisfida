/*
 * $Id: Versions.java 3372 2008-05-12 03:16:52Z xlv $
 * Copyright (c) 2005-2007 Carsten Hammer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class was originally published under the MPL by Bruno Lowagie,
 * Paulo Soares, and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

/*
 * This class was originally written by Carsten Hammer.
 * Changes were made by Bruno Lowagie, Paulo Soares and Xavier Le Vourch.
 * These people were contacted before changing the license from MPL/LGPL to MIT.
 * Current copyright holders are Bruno Lowagie and Carsten Hammer.
 */

package com.lowagie.toolbox;

import com.lowagie.text.Document;
import com.lowagie.text.exceptions.InitializationException;
import java.awt.BorderLayout;
import java.io.IOException;
import java.io.Serial;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * JFrame that shows the plugin_versions of all the plugins.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Versions
        extends JFrame {

    /**
     * The serial version UID of this class.
     */
    @Serial
    private static final long serialVersionUID = 2925242862240301106L;

    private static final Logger logger = Logger.getLogger(Versions.class.getName());

    /**
     * A label with info about the library, JVM,...
     */
    JLabel libraryVersions = new JLabel();

    /**
     * The table with all the plug-ins (getName, version and date).
     */
    JTable pluginVersions = new JTable();

    /**
     * A scrollpane for the plugin_versions table.
     */
    JScrollPane scrollVersions = new JScrollPane();

    /**
     * Constructs a Versions object.
     */
    public Versions() {
        super("Plugins and their version");
        // Handle the I/O exception appropriately, e.g., retry or notify the user
        // Handle configuration issues, such as missing or incorrect settings
        try {
            initialize();
        } catch (InitializationException ex) {
            // Catch any unexpected exceptions and rethrow or handle them
            logger.severe("Unexpected exception in Versions: " + ex.getMessage());
            throw new RuntimeException("Unexpected exception during initialization", ex);
        }
    }


    /**
     * Initialization of the jFrame.
     *
     */
    private void initialize() throws InitializationException {
        try {
            this.getContentPane().setLayout(new BorderLayout());
            scrollVersions.setViewportView(pluginVersions);
            libraryVersions.setIcon(new ImageIcon(Objects.requireNonNull(Versions.class.getResource("1t3xt.gif"))));
            this.getContentPane().add(libraryVersions, BorderLayout.NORTH);
            this.getContentPane().add(scrollVersions, BorderLayout.CENTER);

            Properties properties = System.getProperties();
            Runtime runtime = Runtime.getRuntime();

            // Set an initial capacity based on an estimated size
            StringBuilder sb = new StringBuilder(512); // Adjust the size as needed
            sb.append("<html>");
            appendVersionInfo(sb, "iTexttoolbox version", com.lowagie.toolbox.Versions.class.getPackage().getImplementationVersion());
            appendVersionInfo(sb, "iText version", Document.getVersion());
            appendVersionInfo(sb, "java.version", properties.getProperty("java.version"));
            appendVersionInfo(sb, "java.vendor", properties.getProperty("java.vendor"));
            appendVersionInfo(sb, "java.home", properties.getProperty("java.home"));
            sb.append("<p>java.freeMemory: ").append(runtime.freeMemory()).append(" bytes</p>");
            sb.append("<p>java.totalMemory: ").append(runtime.totalMemory()).append(" bytes</p>");
            appendVersionInfo(sb, "user.home", properties.getProperty("user.home"));
            appendVersionInfo(sb, "os.getName", properties.getProperty("os.getName"));
            appendVersionInfo(sb, "os.arch", properties.getProperty("os.arch"));
            appendVersionInfo(sb, "os.version", properties.getProperty("os.version"));
            sb.append("</html>");

            libraryVersions.setText(sb.toString());

            TableModel model = getVersionTableModel(AbstractTool.versionsarray);
            RowSorter<TableModel> sorter = new TableRowSorter<>(model);
            pluginVersions.setRowSorter(sorter);
            pluginVersions.setModel(model);

            pack();
        } catch (SecurityException e) {
            throw new InitializationException("Error during initialization", e);
        }
    }

    // Helper method to append version info with basic validation
    private void appendVersionInfo(StringBuilder sb, String label, String value) {
        if (value != null) {
            sb.append("<p>").append(label).append(": ").append(value).append("</p>");
        } else {
            sb.append("<p>").append(label).append(": Not Available</p>");
        }
    }

    /**
     * Returns the TableModel implementation that will be used to show the plugin_versions.
     *
     * @param versionsarray ArrayList
     * @return TableModel
     */
    public TableModel getVersionTableModel(final List<String> versionsarray) {
        return new AbstractTableModel() {

            @Serial
            private static final long serialVersionUID = 5105003782164682777L;

            public int getColumnCount() {
                return 4;
            }

            public int getRowCount() {
                return versionsarray.size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                String dummy;
                switch (columnIndex) {
                    case 0:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(".java")[0];
                    case 1:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(" ")[1];
                    case 2:
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dummy = versionsarray.get(rowIndex);
                        try {
                            return df.parse(dummy.split(" ")[2] + " "
                                    + dummy.split(" ")[3]);
                        } catch (ParseException ex) {
                            return null;
                        }
                    case 3:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(" ")[4];
                    default:
                        break;

                }
                return versionsarray;
            }

            @Override
            public String getColumnName(int column) {
                return switch (column) {
                    case 0 -> "Name";
                    case 1 -> "Version";
                    case 2 -> "Changed";
                    case 3 -> "ChangeBy";
                    default -> "";
                };
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return switch (column) {
                    case 0, 1, 3 -> String.class;
                    case 2 -> java.util.Date.class;
                    default -> null;
                };
            }
        };

    }
}
