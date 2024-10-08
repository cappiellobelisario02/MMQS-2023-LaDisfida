/*
 * $Id: OutlineTree.java 3117 2008-01-31 05:53:22Z xlv $
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
import com.lowagie.rups.model.TreeNodeFactory;
import com.lowagie.rups.view.icons.IconTreeCellRenderer;
import com.lowagie.rups.view.itext.treenodes.OutlineTreeNode;
import com.lowagie.rups.view.itext.treenodes.PdfObjectTreeNode;
import com.lowagie.text.pdf.PdfName;
import java.io.Serial;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

/**
 * A JTree visualizing information about the outlines (aka bookmarks) of the PDF file (if any).
 */
public class OutlineTree extends JTree implements TreeSelectionListener {

    /**
     * A serial version uid.
     */
    @Serial
    private static final long serialVersionUID = 5646572654823301007L;
    /**
     * Nodes in the FormTree correspond with nodes in the main PdfTree.
     */
    protected transient PdfReaderController controller;

    /**
     * Creates a new outline tree.
     *
     * @param controller The renderer that will render an object when selected in the table.
     */
    public OutlineTree(PdfReaderController controller) {
        super();
        this.controller = controller;
        setCellRenderer(new IconTreeCellRenderer());
        setModel(new DefaultTreeModel(new OutlineTreeNode()));
        addTreeSelectionListener(this);
    }

    /**
     * Method that can be used recursively to load the outline hierarchy into the tree.
     */
    private void loadOutline(TreeNodeFactory factory, OutlineTreeNode parent, PdfObjectTreeNode child) {
        OutlineTreeNode childnode = new OutlineTreeNode(child);
        parent.add(childnode);
        PdfObjectTreeNode first = factory.getChildNode(child, PdfName.FIRST);
        if (first != null) {
            loadOutline(factory, childnode, first);
        }
        PdfObjectTreeNode next = factory.getChildNode(child, PdfName.NEXT);
        if (next != null) {
            loadOutline(factory, parent, next);
        }
    }

    /**
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent evt) {
        if (controller == null) {
            return;
        }
        OutlineTreeNode selectednode = (OutlineTreeNode) this.getLastSelectedPathComponent();
        PdfObjectTreeNode node = selectednode.getCorrespondingPdfObjectNode();
        if (node != null) {
            controller.selectNode(node);
        }
    }

}
