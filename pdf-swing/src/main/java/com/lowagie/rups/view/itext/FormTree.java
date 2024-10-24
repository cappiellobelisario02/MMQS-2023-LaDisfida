/*
 * $Id: FormTree.java 3146 2008-02-20 18:10:07Z blowagie $
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

package com.lowagie.rups.view.itext;

import com.lowagie.rups.controller.PdfReaderController;
import com.lowagie.rups.model.ObjectLoader;
import com.lowagie.rups.model.TreeNodeFactory;
import com.lowagie.rups.model.XfaFile;
import com.lowagie.rups.view.icons.IconTreeCellRenderer;
import com.lowagie.rups.view.itext.treenodes.FormTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfTrailerTreeNode;
import com.lowagie.rups.view.itext.treenodes.XfaTreeNode;
import com.lowagie.text.pdf.PdfName;
import java.io.IOException;
import java.io.Serial;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import org.dom4j.DocumentException;

/**
 * A JTree visualizing information about the Interactive Form of the PDF file (if any). Normally shows a tree view of
 * the field hierarchy and individual XDP packets.
 */
public class FormTree extends JTree implements TreeSelectionListener, Observer {

    /**
     * A serial version UID.
     */
    @Serial
    private static final long serialVersionUID = -3584003547303700407L;

    private static final Logger logger = Logger.getLogger(FormTree.class.getName());
    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    protected transient PdfReaderController controller;
    /**
     * If the form is an XFA form, the XML file is stored in this object.
     */
    protected transient XfaFile xfaFile;
    /**
     * Treeview of the XFA file.
     */
    protected XfaTree xfaTree;
    /**
     * Textview of the XFA file.
     */
    protected XfaTextArea xfaTextArea;

    /**
     * Creates a new FormTree.
     *
     * @param controller The renderer that will render an object when selected in the table.
     */
    public FormTree(PdfReaderController controller) {
        super();
        this.controller = controller;
        setCellRenderer(new IconTreeCellRenderer());
        setModel(new DefaultTreeModel(new FormTreeNode()));
        addTreeSelectionListener(this);
        xfaTree = new XfaTree();
        xfaTextArea = new XfaTextArea();
    }

    /**
     * Loads the fields of a PDF document into the FormTree.
     *
     * @param observable the observable object
     * @param obj        the object
     */
    public void update(Observable observable, Object obj) {
        if (obj == null) {
            setModel(new DefaultTreeModel(new FormTreeNode()));
            xfaFile = null;
            xfaTree.clear();
            xfaTextArea.clear();
            repaint();
            return;
        }
        if (obj instanceof ObjectLoader loader) {
            TreeNodeFactory factory = loader.getNodes();
            PdfTrailerTreeNode trailer = controller.getPdfTree().getRoot();
            PdfObjectTreeNode catalog = factory.getChildNode(trailer, PdfName.ROOT);
            PdfObjectTreeNode form = factory.getChildNode(catalog, PdfName.ACROFORM);
            if (form == null) {
                return;
            }
            PdfObjectTreeNode fields = factory.getChildNode(form, PdfName.FIELDS);
            FormTreeNode root = new FormTreeNode();
            if (fields != null) {
                FormTreeNode node = new FormTreeNode(fields);
                node.setUserObject("Fields");
                loadFields(factory, node, fields);
                root.add(node);
            }
            PdfObjectTreeNode xfa = factory.getChildNode(form, PdfName.XFA);
            if (xfa != null) {
                XfaTreeNode node = new XfaTreeNode(xfa);
                node.setUserObject("XFA");
                loadXfa(factory, node, xfa);
                root.add(node);
                try {
                    xfaFile = new XfaFile(node);
                    xfaTree.load(xfaFile);
                    xfaTextArea.load(xfaFile);
                } catch (IOException | DocumentException e) {
                    String msg = "Error loading xfa file: " + e.getMessage();
                    logger.severe(msg);
                }
            }
            setModel(new DefaultTreeModel(root));
        }
    }

    /**
     * Method that can be used recursively to load the fields hierarchy into the tree.
     *
     * @param factory     a factory that can produce new PDF object nodes
     * @param formNode   the getParent node in the form tree
     * @param objectNode the object node that will be used to create a child node
     */
    private void loadFields(TreeNodeFactory factory, FormTreeNode formNode, PdfObjectTreeNode objectNode) {
        if (objectNode == null) {
            return;
        }
        factory.expandNode(objectNode);
        if (objectNode.isIndirectReference()) {
            loadFields(factory, formNode, (PdfObjectTreeNode) objectNode.getFirstChild());
        } else if (objectNode.isArray()) {
            Enumeration<?> children = objectNode.children();
            while (children.hasMoreElements()) {
                loadFields(factory, formNode, (PdfObjectTreeNode) children.nextElement());
            }
        } else if (objectNode.isDictionary()) {
            FormTreeNode leaf = new FormTreeNode(objectNode);
            formNode.add(leaf);
            PdfObjectTreeNode kids = factory.getChildNode(objectNode, PdfName.KIDS);
            loadFields(factory, leaf, kids);
        }
    }

    /**
     * Method that will load the nodes that refer to XFA streams.
     *
     * @param formNode   the getParent node in the form tree
     * @param objectNode the object node that will be used to create a child node
     */
    private void loadXfa(TreeNodeFactory factory, XfaTreeNode formNode, PdfObjectTreeNode objectNode) {
        if (objectNode == null) {
            return;
        }
        factory.expandNode(objectNode);
        if (objectNode.isIndirectReference()) {
            loadXfa(factory, formNode, (PdfObjectTreeNode) objectNode.getFirstChild());
        } else if (objectNode.isArray()) {
            Enumeration<?> children = objectNode.children();
            PdfObjectTreeNode key;
            PdfObjectTreeNode value;
            while (children.hasMoreElements()) {
                key = (PdfObjectTreeNode) children.nextElement();
                value = (PdfObjectTreeNode) children.nextElement();
                if (value.isIndirectReference()) {
                    factory.expandNode(value);
                    value = (PdfObjectTreeNode) value.getFirstChild();
                }
                formNode.addPacket(key.getPdfObject().toString(), value);
            }
        } else if (objectNode.isStream()) {
            formNode.addPacket("xdp", objectNode);
        }
    }

    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent evt) {
        if (controller == null) {
            return;
        }
        FormTreeNode selectednode = (FormTreeNode) this.getLastSelectedPathComponent();
        if (selectednode == null) {
            return;
        }
        PdfObjectTreeNode node = selectednode.getCorrespondingPdfObjectNode();
        if (node != null) {
            controller.selectNode(node);
        }
    }

    public XfaTree getXfaTree() {
        return xfaTree;
    }

    public XfaTextArea getXfaTextArea() {
        return xfaTextArea;
    }

}
