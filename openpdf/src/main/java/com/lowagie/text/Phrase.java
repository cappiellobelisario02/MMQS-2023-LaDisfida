/*
 * $Id: Phrase.java 4065 2009-09-16 23:09:11Z psoares33 $
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

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.HyphenationEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A <CODE>Phrase</CODE> is a series of <CODE>Chunk</CODE>s.
 * <p>
 * A <CODE>Phrase</CODE> has a main <CODE>Font</CODE>, but some chunks within the phrase can have a <CODE>Font</CODE>
 * that differs from the main <CODE>Font</CODE>. All the <CODE>Chunk</CODE>s in a <CODE>Phrase</CODE> have the same
 * <CODE>leading</CODE>.
 * <p>
 * Example:
 * <BLOCKQUOTE><PRE>
 * // When no parameters are passed, the default leading = 16
 * <STRONG>Phrase phrase0 = new Phrase();</STRONG>
 * <STRONG>Phrase phrase1 = new Phrase("this is a phrase");</STRONG>
 * // In this example the leading is passed as a parameter
 * <STRONG>Phrase phrase2 = new Phrase(16, "this is a phrase with leading 16");</STRONG>
 * // When a Font is passed (explicitly or embedded in a chunk), the default leading = 1.5 * size of the font
 * <STRONG>Phrase phrase3 = new Phrase("this is a phrase with a red, normal font Courier, size 12",
 * FontFactory.getFont(FontFactory.COURIER, 12, Font.NORMAL, new Color(255, 0, 0)));</STRONG>
 * <STRONG>Phrase phrase4 = new Phrase(new Chunk("this is a phrase"));</STRONG>
 * <STRONG>Phrase phrase5 = new Phrase(18, new Chunk("this is a phrase", FontFactory.getFont(FontFactory.HELVETICA, 16,
 * Font.BOLD, new Color(255, 0, 0)));</STRONG>
 * </PRE></BLOCKQUOTE>
 *
 * @see Element
 * @see Chunk
 * @see Paragraph
 * @see Anchor
 */

public class Phrase extends ArrayList<Element> implements TextElementArray {

    // constants
    @Serial
    private static final long serialVersionUID = 2643594602455068231L;

    // membervariables

    private static final Logger logger = Logger.getLogger(com.lowagie.text.Phrase.class.getName());

    /**
     * This is the leading of this phrase.
     */
    protected float leading = Float.NaN;

    /**
     * This is the font of this phrase.
     */
    protected transient Font fontAttr;

    /**
     * Null, unless the Phrase has to be hyphenated.
     *
     * @since 2.1.2
     */
    protected transient HyphenationEvent hyphenation = null;

    // constructors

    /**
     * Constructs a <CODE>Phrase</CODE> without specifying a leading.
     */
    public Phrase() {
        this(16);
    }

    /**
     * Copy constructor for <CODE>Phrase</CODE>.
     *
     * @param phrase an object of {@link com.lowagie.text.Phrase}, that you want to create a new object from
     */
    public Phrase(com.lowagie.text.Phrase phrase) {
        super();
        initPhrase(phrase);
    }

    private void initPhrase(com.lowagie.text.Phrase phrase) {
        this.addAll(phrase);  // Questo metodo viene chiamato solo dopo che il costruttore ha finito
        leading = phrase.getLeading();
        fontAttr = phrase.getFont();
        setHyphenation(phrase.getHyphenation());
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain leading.
     *
     * @param leading the leading
     */
    public Phrase(float leading) {
        this.leading = leading;
        fontAttr = new Font();
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>Chunk</CODE>.
     *
     * @param chunk a <CODE>Chunk</CODE>
     */
    public Phrase(Chunk chunk) {
        super.add(chunk);
        fontAttr = chunk.getFont();
        callSetHyphenation(chunk.getHyphenation());
    }

    private void callSetHyphenation(HyphenationEvent hyphEv) {
        setHyphenation(hyphEv);
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>Chunk</CODE> and a certain leading.
     *
     * @param leading the leading
     * @param chunk   a <CODE>Chunk</CODE>
     */
    public Phrase(float leading, Chunk chunk) {
        this.leading = leading;
        super.add(chunk);
        fontAttr = chunk.getFont();
        setHyphenation(chunk.getHyphenation());
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>String</CODE>.
     *
     * @param string a <CODE>String</CODE>
     */
    public Phrase(String string) {
        this(Float.NaN, string, new Font());
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain <CODE>String</CODE> and a certain <CODE>Font</CODE>.
     *
     * @param string a <CODE>String</CODE>
     * @param fontAttr   a <CODE>Font</CODE>
     */
    public Phrase(String string, Font fontAttr) {
        this(Float.NaN, string, fontAttr);
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain leading and a certain <CODE>String</CODE>.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     */
    public Phrase(float leading, String string) {
        this(leading, string, new Font());
    }

    /**
     * Constructs a <CODE>Phrase</CODE> with a certain leading, a certain <CODE>String</CODE> and a certain
     * <CODE>Font</CODE>.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     * @param fontAttr    a <CODE>Font</CODE>
     */
    public Phrase(float leading, String string, Font fontAttr) {
        this.leading = leading;
        this.fontAttr = fontAttr;
        /* bugfix by August Detlefsen */
        if (string != null && !string.isEmpty()) {
            super.add(new Chunk(string, fontAttr));
        }
    }

    // implementation of the Element-methods

    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     *
     * @param string input
     * @return a newly constructed Phrase
     */
    public static com.lowagie.text.Phrase getInstance(String string) {
        return getInstance(16, string, new Font());
    }

    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     *
     * @param leading leading spaces
     * @param string  input
     * @return a newly constructed Phrase
     */
    public static com.lowagie.text.Phrase getInstance(int leading, String string) {
        return getInstance(leading, string, new Font());
    }

    /**
     * Gets a special kind of Phrase that changes some characters into corresponding symbols.
     *
     * @param leading leading spaces
     * @param string  input
     * @param fontAttr    font to be used
     * @return a newly constructed Phrase
     */
    public static com.lowagie.text.Phrase getInstance(int leading, String string, Font fontAttr) {
        com.lowagie.text.Phrase p = new com.lowagie.text.Phrase();
        p.setLeading(leading);
        p.fontAttr = fontAttr;
        if (fontAttr.getFamily() != Font.SYMBOL && fontAttr.getFamily() != Font.ZAPFDINGBATS && fontAttr.getBaseFont() == null) {
            int index;
            while ((index = SpecialSymbol.index(string)) > -1) {
                if (index > 0) {
                    String firstPart = string.substring(0, index);
                    p.add(new Chunk(firstPart, fontAttr));
                    string = string.substring(index);
                }
                Font symbol = new Font(Font.SYMBOL, fontAttr.getSize(), fontAttr.getStyle(), fontAttr.getColor());
                StringBuilder buf = new StringBuilder();
                do {
                    buf.append(SpecialSymbol.getCorrespondingSymbol(string.charAt(0)));
                    string = string.substring(1);
                } while (SpecialSymbol.index(string) == 0);
                p.add(new Chunk(buf.toString(), symbol));
            }
        }
        if (string != null && !string.isEmpty()) {
            p.add(new Chunk(string, fontAttr));
        }
        return p;
    }

    /**
     * Processes the element by adding it (or the different parts) to an
     * <CODE>ElementListener</CODE>.
     *
     * @param listener an <CODE>ElementListener</CODE>
     * @return <CODE>true</CODE> if the element was processed successfully
     */
    @Override
    public boolean process(ElementListener listener) {
        try {
            for (Element o : this) {
                listener.add(o);
            }
            return true;
        } catch (DocumentException de) {
            return false;
        }
    }

    // overriding some of the ArrayList-methods

    /**
     * Gets the type of the text element.
     *
     * @return a type
     */
    @Override
    public int type() {
        return Element.PHRASE;
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return an <CODE>ArrayList</CODE>
     */
    @Override
    public ArrayList<Element> getChunks() {
        ArrayList<Element> tmp = new ArrayList<>();
        for (Element element : this) {
            tmp.addAll(element.getChunks());
        }
        return tmp;
    }

    @Override
    public float llx() {
        return 0;
    }

    @Override
    public float lly() {
        return 0;
    }

    @Override
    public float urx() {
        return 0;
    }

    @Override
    public float ury() {
        return 0;
    }

    /**
     * @see com.lowagie.text.Element#isContent()
     * @since iText 2.0.8
     */
    @Override
    public boolean isContent() {
        return true;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    @Override
    public boolean isNestable() {
        return true;
    }

    /**
     * Adds a <CODE>Chunk</CODE>, an <CODE>Anchor</CODE> or another <CODE>Phrase</CODE> to this <CODE>Phrase</CODE>.
     *
     * @param index   index at which the specified element is to be inserted
     * @param element an object of type <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     * @throws ClassCastException when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or
     *                            <CODE>Phrase</CODE>
     */
    @Override
    public void add(int index, Element element) {
        if (element == null) {
            return;
        }
        try {
            if (element.type() == Element.CHUNK) {
                Chunk chunk = (Chunk) element;
                if (!fontAttr.isStandardFont()) {
                    chunk.setFont(fontAttr.difference(chunk.getFont()));
                }
                if (hyphenation != null && chunk.getHyphenation() == null && !chunk.isEmpty()) {
                    chunk.setHyphenation(hyphenation);
                }
                super.add(index, chunk);
            } else if (element.type() == Element.PHRASE ||
                    element.type() == Element.ANCHOR ||
                    element.type() == Element.ANNOTATION ||
                    element.type() == Element.TABLE || // line added by David Freels
                    element.type() == Element.YMARK ||
                    element.type() == Element.MARKED) {
                super.add(index, element);
            } else {
                throw new ClassCastException(String.valueOf(element.type()));
            }
        } catch (ClassCastException cce) {
            throw new ClassCastException(
                    MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.getMessage()));
        }
    }

    /**
     * Adds a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or another <CODE>Phrase</CODE> to this <CODE>Phrase</CODE>.
     *
     * @param o an object of type <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or <CODE>Phrase</CODE>
     * @return a boolean
     * @throws ClassCastException when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or
     *                            <CODE>Phrase</CODE>
     */
    public boolean add(String o) {
        if (o == null) {
            return false;
        }
        return super.add(new Chunk(o, fontAttr));
    }

    // other methods that change the member variables

    @Override
    public boolean add(Element element) {
        if (element == null) {
            return false;
        }
        if (element instanceof RtfElementInterface) {
            return super.add(element);
        }
        try {
            switch (element.type()) {
                case Element.CHUNK:
                    return addChunk((Chunk) element);
                case Element.PHRASE,
                     Element.PARAGRAPH:
                    com.lowagie.text.Phrase phrase = (com.lowagie.text.Phrase) element;
                    boolean success = true;
                    Element e;
                    for (Element o1 : phrase) {
                        e = o1;
                        if (e instanceof Chunk eChunk) {
                            success &= addChunk(eChunk);
                        } else {
                            success &= this.add(e);
                        }
                    }
                    return success;
                case Element.MARKED,
                     Element.ANCHOR,
                     Element.ANNOTATION,
                     Element.FOOTNOTE,
                     Element.TABLE,
                     Element.PTABLE,
                     Element.LIST,
                     Element.YMARK:
                    return super.add(element);
                default:
                    throw new ClassCastException(String.valueOf(element.type()));
            }
        } catch (ClassCastException cce) {
            throw new ClassCastException(
                    MessageLocalization.getComposedMessage("insertion.of.illegal.element.1", cce.getMessage()));
        }
    }

    /**
     * Adds a collection of <CODE>Chunk</CODE>s to this <CODE>Phrase</CODE>.
     *
     * @param collection a collection of <CODE>Chunk</CODE>s, <CODE>Anchor</CODE>s and <CODE>Phrase</CODE>s.
     * @return <CODE>true</CODE> if the action succeeded, <CODE>false</CODE> if not.
     * @throws ClassCastException when you try to add something that isn't a <CODE>Chunk</CODE>, <CODE>Anchor</CODE> or
     *                            <CODE>Phrase</CODE>
     */
    @Override
    public boolean addAll(Collection<? extends Element> collection) {
        for (Element o : collection) {
            this.add(o);
        }
        return true;
    }

    // methods to retrieve information

    /**
     * Adds a Chunk.
     * <p>
     * This method is a hack to solve a problem I had with phrases that were split between chunks in the wrong place.
     *
     * @param chunk a Chunk to add to the Phrase
     * @return true if adding the Chunk succeeded
     */
    protected boolean addChunk(Chunk chunk) {
        Font f = chunk.getFont();
        String c = chunk.getContent();
        if (fontAttr != null && !fontAttr.isStandardFont()) {
            f = fontAttr.difference(chunk.getFont());
        }
        if (size() > 0 && !chunk.hasAttributes()) {
            try {
                Chunk previous = (Chunk) get(size() - 1);
                if (!previous.hasAttributes()
                        && (f == null
                        || f.compareTo(previous.getFont()) == 0)
                        && !previous.getContent().trim().isEmpty()
                        && !c.trim().isEmpty()) {
                    previous.append(c);
                    return true;
                }
            } catch (IndexOutOfBoundsException e) {
                logger.info("Index out of bounds: " + e.getMessage());
            }
        }
        Chunk newChunk = new Chunk(c, f);
        newChunk.setChunkAttributes(chunk.getChunkAttributes());
        if (hyphenation != null && newChunk.getHyphenation() == null && !newChunk.isEmpty()) {
            newChunk.setHyphenation(hyphenation);
        }
        return super.add(newChunk);
    }

    /**
     * Adds a <CODE>Object</CODE> to the <CODE>Paragraph</CODE>.
     *
     * @param object the object to add.
     */
    protected void addSpecial(Object object) {
        super.add((Element) object);
    }

    /**
     * Gets the leading of this phrase.
     *
     * @return the linespacing
     */
    public float getLeading() {
        if (Float.isNaN(leading) && fontAttr != null) {
            return fontAttr.getCalculatedLeading(1.5f);
        }
        return leading;
    }

    /**
     * Sets the leading of this phrase.
     *
     * @param leading the new leading
     */

    public void setLeading(float leading) {
        this.leading = leading;
    }

    /**
     * Checks you if the leading of this phrase is defined.
     *
     * @return true if the leading is defined
     */
    public boolean hasLeading() {
        return !Float.isNaN(leading);
    }

    /**
     * Gets the font of the first <CODE>Chunk</CODE> that appears in this <CODE>Phrase</CODE>.
     *
     * @return a <CODE>Font</CODE>
     */
    public Font getFont() {
        return fontAttr;
    }

    /**
     * Sets the main font of this Phrase.
     * <p>
     * Please note that this font will only be used on new content added to the Phrase. It won't change the font of
     * existing content.
     *
     * @param fontAttr the new font
     */
    public void setFont(Font fontAttr) {
        this.fontAttr = fontAttr;
    }

    // kept for historical reasons; people should use FontSelector
    // eligible for deprecation, but the methods are mentioned in the book p277.

    /**
     * Returns the content as a String object. This method differs from toString because toString will return an
     * ArrayList with the toString value of the Chunks in this Phrase.
     *
     * @return an <code>String</code>
     */
    public String getContent() {
        StringBuilder buf = new StringBuilder();
        for (Object o : getChunks()) {
            buf.append(o.toString());
        }
        return buf.toString();
    }

    /**
     * Checks is this <CODE>Phrase</CODE> contains no or 1 empty <CODE>Chunk</CODE>.
     *
     * @return <CODE>false</CODE> if the <CODE>Phrase</CODE>
     * contains more than one or more non-empty<CODE>Chunk</CODE>s.
     */
    @Override
    public boolean isEmpty() {
        return switch (size()) {
            case 0 -> true;
            case 1 -> {
                Element element = get(0);
                yield element.type() == Element.CHUNK && ((Chunk) element).isEmpty();
            }
            default -> false;
        };
    }

    /**
     * Getter for the hyphenation settings.
     *
     * @return a HyphenationEvent
     * @since 2.1.2
     */
    public HyphenationEvent getHyphenation() {
        return hyphenation;
    }

    /**
     * Setter for the hyphenation.
     *
     * @param hyphenation a HyphenationEvent instance
     * @since 2.1.2
     */
    public void setHyphenation(HyphenationEvent hyphenation) {
        this.hyphenation = hyphenation;
    }

}
