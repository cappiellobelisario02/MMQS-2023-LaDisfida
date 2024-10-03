package com.lowagie.text;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An <CODE>Anchor</CODE> can be a reference or a destination of a reference.
 * <p>
 * An <CODE>Anchor</CODE> is a special kind of <CODE>Phrase</CODE>. It is constructed in the same way.
 * <p>
 * Example:
 * <BLOCKQUOTE><PRE>
 * <STRONG>Anchor anchor = new Anchor("this is a link");</STRONG>
 * <STRONG>anchor.setName("LINK");</STRONG>
 * <STRONG>anchor.setReference("http://www.lowagie.com");</STRONG>
 * </PRE></BLOCKQUOTE>
 *
 * @see Element
 * @see Phrase
 */

public class Anchor extends Phrase {

    // constant
    private static final long serialVersionUID = -852278536049236911L;

    // membervariables

    /**
     * This is the name of the <CODE>Anchor</CODE>.
     */
    protected String name = null;

    /**
     * This is the reference of the <CODE>Anchor</CODE>.
     */
    protected String reference = null;

    // constructors

    /**
     * Constructs an <CODE>Anchor</CODE> without specifying a leading.
     */
    public Anchor() {
        super(16);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain leading.
     *
     * @param leading the leading
     */

    public Anchor(float leading) {
        super(leading);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain <CODE>Chunk</CODE>.
     *
     * @param chunk a <CODE>Chunk</CODE>
     */
    public Anchor(Chunk chunk) {
        super(chunk);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain <CODE>String</CODE>.
     *
     * @param string a <CODE>String</CODE>
     */
    public Anchor(String string) {
        super(string);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain <CODE>String</CODE> and a certain <CODE>Font</CODE>.
     *
     * @param string a <CODE>String</CODE>
     * @param font   a <CODE>Font</CODE>
     */
    public Anchor(String string, Font font) {
        super(string, font);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain <CODE>Chunk</CODE> and a certain leading.
     *
     * @param leading the leading
     * @param chunk   a <CODE>Chunk</CODE>
     */
    public Anchor(float leading, Chunk chunk) {
        super(leading, chunk);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain leading and a certain <CODE>String</CODE>.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     */
    public Anchor(float leading, String string) {
        super(leading, string);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain leading, a certain <CODE>String</CODE> and a certain
     * <CODE>Font</CODE>.
     *
     * @param leading the leading
     * @param string  a <CODE>String</CODE>
     * @param font    a <CODE>Font</CODE>
     */
    public Anchor(float leading, String string, Font font) {
        super(leading, string, font);
    }

    /**
     * Constructs an <CODE>Anchor</CODE> with a certain <CODE>Phrase</CODE>.
     *
     * @param phrase a <CODE>Phrase</CODE>
     */
    public Anchor(Phrase phrase) {
        super(phrase);
        if (phrase instanceof Anchor) {
            Anchor a = (Anchor) phrase;
            setName(a.name);
            setReference(a.reference);
        }
    }

    // implementation of the Element-methods

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
            // Iterate over Elements, not directly over Chunks
            Iterator<Element> i = getChunks().iterator();
            boolean localDestination = (reference != null && reference.startsWith("#"));
            boolean notGotoOK = true;

            // Loop through each Element
            while (i.hasNext()) {
                Element element = i.next();

                // Check if the element is a Chunk before casting
                if (element instanceof Chunk) {
                    Chunk chunk = (Chunk) element;

                    // Apply anchor-specific logic to the Chunk
                    if (name != null && notGotoOK && !chunk.isEmpty()) {
                        chunk.setLocalDestination(name);
                        notGotoOK = false;
                    }
                    if (localDestination) {
                        chunk.setLocalGoto(reference.substring(1));
                    }

                    // Pass the chunk to the listener
                    listener.add(chunk);
                }
            }
            return true;
        } catch (DocumentException de) {
            return false;
        }
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return an <CODE>ArrayList</CODE>
     */
    @Override
    public ArrayList<Element> getChunks() {
        ArrayList<Element> tmp = new ArrayList<>();
        boolean localDestination = (reference != null && reference.startsWith("#"));
        boolean notGotoOK = true;

        // Use an iterator to go through the elements of this class (likely Phrase or Anchor)
        Iterator<Element> iterator = this.iterator();  // Ensure 'this' is iterable and returns Elements

        while (iterator.hasNext()) {
            Element element = iterator.next();  // Each element is of type 'Element'

            // Check if the element is a Chunk before casting
            if (element instanceof Chunk) {
                Chunk chunk = (Chunk) element;

                // Apply anchor-specific logic to the Chunk
                if (name != null && notGotoOK && !chunk.isEmpty()) {
                    chunk.setLocalDestination(name);
                    notGotoOK = false;
                }
                if (localDestination) {
                    chunk.setLocalGoto(reference.substring(1));
                } else if (reference != null) {
                    chunk.setAnchor(reference);
                }
            }

            // Add all chunks from the element, whether it's a Chunk or not
            tmp.addAll(element.getChunks());
        }

        return tmp;
    }


    /**
     * Gets the type of the text element.
     *
     * @return a type
     */
    @Override
    public int type() {
        return Element.ANCHOR;
    }

    // methods

    /**
     * Returns the name of this <CODE>Anchor</CODE>.
     *
     * @return a name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this <CODE>Anchor</CODE>.
     *
     * @param name a new name
     */
    public void setName(String name) {
        this.name = name;
    }

    // methods to retrieve information

    /**
     * Gets the reference of this <CODE>Anchor</CODE>.
     *
     * @return a reference
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the reference of this <CODE>Anchor</CODE>.
     *
     * @param reference a new reference
     */
    public void setReference(String reference) {
        this.reference = reference;
    }

    /**
     * Gets the reference of this <CODE>Anchor</CODE>.
     *
     * @return an <CODE>URL</CODE>
     */
    public URL getUrl() {
        try {
            return new URL(reference);
        } catch (MalformedURLException mue) {
            return null;
        }
    }

}
