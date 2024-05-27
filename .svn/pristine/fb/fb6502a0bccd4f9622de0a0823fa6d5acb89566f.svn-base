package com.lowagie.text.pdf.hyphenation;

import java.io.Serializable;

/**
 * This class implements a simple char vector with access to the underlying array.
 *
 * @author <a href="cav@uniscope.co.jp">Carlos Villegas</a>
 */
public class CharVector implements Serializable {

    private static final long serialVersionUID = -4875768298308363544L;
    /**
     * Capacity increment size
     */
    private static final int DEFAULT_BLOCK_SIZE = 2048;
    private int blockSize;

    /**
     * The encapsulated array
     */
    private char[] array;

    /**
     * Points to next free item
     */
    private int n;

    public CharVector() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public CharVector(int capacity) {
        if (capacity > 0) {
            blockSize = capacity;
        } else {
            blockSize = DEFAULT_BLOCK_SIZE;
        }
        array = new char[blockSize];
        n = 0;
    }

    public CharVector(char[] a) {
        blockSize = DEFAULT_BLOCK_SIZE;
        array = a;
        n = a.length;
    }

    public CharVector(char[] a, int capacity) {
        if (capacity > 0) {
            blockSize = capacity;
        } else {
            blockSize = DEFAULT_BLOCK_SIZE;
        }
        array = a;
        n = a.length;
    }

    /**
     * Reset Vector but don't resize or clear elements
     */
    public void clear() {
        n = 0;
    }

<<<<<<< Updated upstream
    /**
     * Copy constructor.
     *
     * @param original the CharVector to copy.
     */
=======
>>>>>>> Stashed changes
    public CharVector(CharVector original) {
        this.array = original.array.clone();
        this.blockSize = original.blockSize;
        this.n = original.n;
<<<<<<< Updated upstream
=======
    }

    public CharVector clone() {
        return new CharVector(this);
>>>>>>> Stashed changes
    }

    public char[] getArray() {
        return array;
    }

    /**
     * @return the number of items in array
     */
    public int length() {
        return n;
    }

    /**
     * returns current capacity of array
     *
     * @return the current capacity of array
     */
    public int capacity() {
        return array.length;
    }

    public void put(int index, char val) {
        array[index] = val;
    }

    public char get(int index) {
        return array[index];
    }

    public int alloc(int size) {
        int index = n;
        int len = array.length;
        if (n + size >= len) {
            char[] aux = new char[len + blockSize];
            System.arraycopy(array, 0, aux, 0, len);
            array = aux;
        }
        n += size;
        return index;
    }

    public void trimToSize() {
        if (n < array.length) {
            char[] aux = new char[n];
            System.arraycopy(array, 0, aux, 0, n);
            array = aux;
        }
    }
}
