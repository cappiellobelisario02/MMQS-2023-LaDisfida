/*
 * $Id: ListItem.java 4052 2009-08-28 13:54:31Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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

package com.lowagie.text;

import java.util.ArrayList;

/**
 * A <CODE>ListItem</CODE> is a <CODE>Paragraph</CODE> that can be added to a <CODE>List</CODE>.
 * <p>
 * <B>Example 1:</B>
 * <BLOCKQUOTE><PRE>
 * List list = new List(true, 20); list.add(<STRONG>new ListItem("First line")</STRONG>); list.add(<STRONG>new
 * ListItem("The second line is longer to see what happens once the end of the line is reached. Will it start on a new
 * line?")</STRONG>); list.add(<STRONG>new ListItem("Third line")</STRONG>);
 * </PRE></BLOCKQUOTE>
 * <p>
 * The result of this code looks like this:
 * <OL>
 * <LI>
 * First line
 * </LI>
 * <LI>
 * The second line is longer to see what happens once the end of the line is reached. Will it start on a new line?
 * </LI>
 * <LI>
 * Third line
 * </LI>
 * </OL>
 *
 * <B>Example 2:</B>
 * <BLOCKQUOTE><PRE>
 * List overview = new List(false, 10); overview.add(<STRONG>new ListItem("This is an item")</STRONG>);
 * overview.add("This is another item");
 * </PRE></BLOCKQUOTE>
 * <p>
 * The result of this code looks like this:
 * <UL>
 * <LI>
 * This is an item
 * </LI>
 * <LI>
 * This is another item
 * </LI>
 * </UL>
 *
 * @see Element
 * @see List
 * @see Paragraph
 */

public class ListItem extends Paragraph {

    // constants
    private static final long serialVersionUID = 1970670787169329006L;

    // member variables
    protected Chunk symbol;
    private final Paragraph paragraph; // Use composition

    // constructors

    /**
     * Constructs a <CODE>ListItem</CODE>.
     */
    public ListItem() {
        paragraph = new Paragraph();
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain leading.
     *
     * @param leading the leading
     */
    public ListItem(float leading) {
        paragraph = new Paragraph(leading);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>Chunk</CODE>.
     *
     * @param chunk a <CODE>Chunk</CODE>
     */
    public ListItem(Chunk chunk) {
        paragraph = new Paragraph(chunk);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>String</CODE>.
     *
     * @param string a <CODE>String</CODE>
     */
    public ListItem(String string) {
        paragraph = new Paragraph(string);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>String</CODE> and a certain <CODE>Font</CODE>.
     *
     * @param string a <CODE>String</CODE>
     * @param font   a <CODE>String</CODE>
     */
    public ListItem(String string, Font font) {
        paragraph = new Paragraph(string, font);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>Chunk</CODE> and a certain leading.
     *
     * @param leading the leading
     * @param chunk   a <CODE>Chunk</CODE>
     */
    public ListItem(float leading, Chunk chunk) {
        paragraph = new Paragraph(leading, chunk);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>String</CODE> and a certain leading.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     */
    public ListItem(float leading, String string) {
        paragraph = new Paragraph(leading, string);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain leading, <CODE>String</CODE> and <CODE>Font</CODE>.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     * @param font    a <CODE>Font</CODE>
     */
    public ListItem(float leading, String string, Font font) {
        paragraph = new Paragraph(leading, string, font);
    }

    /**
     * Constructs a <CODE>ListItem</CODE> with a certain <CODE>Phrase</CODE>.
     *
     * @param phrase a <CODE>Phrase</CODE>
     */
    public ListItem(Phrase phrase) {
        paragraph = new Paragraph(phrase);
    }

    // methods delegating to Paragraph

    public void setAlignment(int alignment) {
        paragraph.setAlignment(alignment);
    }

    public void setLeading(float fixedLeading) {
        paragraph.setLeading(fixedLeading);
    }

    public void setLeading(float fixedLeading, float multipliedLeading) {
        paragraph.setLeading(fixedLeading, multipliedLeading);
    }

    public void setIndentationLeft(float indentation) {
        paragraph.setIndentationLeft(indentation);
    }

    public void setIndentationRight(float indentation) {
        paragraph.setIndentationRight(indentation);
    }

    public int type() {
        return Element.LISTITEM;
    }

    // Additional methods of ListItem

    public void setIndentationLeft(float indentation, boolean autoindent) {
        if (autoindent) {
            setIndentationLeft(getListSymbol().getWidthPoint());
        } else {
            setIndentationLeft(indentation);
        }
    }

    public Chunk getListSymbol() {
        return symbol;
    }

    public void setListSymbol(Chunk symbol) {
        if (this.symbol == null) {
            this.symbol = symbol;
            if (this.symbol.getFont().isStandardFont()) {
                this.symbol.setFont(paragraph.getFont()); // Access paragraph's font
            }
        }
    }

    public int getAlignment(){
        return paragraph.getAlignment();
    }

    public float getIndentationLeft(){
        return paragraph.getIndentationLeft();
    }

    public float getIndentationRight(){
        return paragraph.getIndentationRight();
    }

    public float getLeading(){
        return paragraph.getLeading();
    }

    public int getRunDirection(){
        return paragraph.getRunDirection();
    }

    public float getMultipliedLeading(){
        return paragraph.getMultipliedLeading();
    }

    public float getExtraParagraphSpace(){
        return paragraph.getExtraParagraphSpace();
    }

    public float getFirstLineIndent(){
        return paragraph.getFirstLineIndent();
    }

    public float getSpacingBefore(){
        return paragraph.getSpacingBefore();
    }

    public Font getFont(){
        return paragraph.getFont();
    }

    public boolean getKeepTogether(){
        return paragraph.getKeepTogether();
    }

    public float getSpacingAfter(){
        return paragraph.getSpacingAfter();
    }

    public float getTotalLeading(){
        return paragraph.getTotalLeading(getFont());
    }

    public ArrayList<Element> getChunks() {
        return paragraph.getChunks();
    }
}
