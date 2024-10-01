package com.lowagie.text.pdf;

public class EncodingParams {
    private final byte[] text;
    private final int textOffset;
    private final int textSize;
    private final byte[] data;
    private final int dataOffset;
    private final int dataSize;
    private final int options;
    private final boolean firstMatch;

    public EncodingParams(byte[] text, int textOffset, int textSize, byte[] data, int dataOffset,
                          int dataSize, int options, boolean firstMatch) {
        this.text = text;
        this.textOffset = textOffset;
        this.textSize = textSize;
        this.data = data;
        this.dataOffset = dataOffset;
        this.dataSize = dataSize;
        this.options = options;
        this.firstMatch = firstMatch;
    }

    // Getters for all fields
    public byte[] getText() { return text; }
    public int getTextOffset() { return textOffset; }
    public int getTextSize() { return textSize; }
    public byte[] getData() { return data; }
    public int getDataOffset() { return dataOffset; }
    public int getDataSize() { return dataSize; }
    public int getOptions() { return options; }
    public boolean isFirstMatch() { return firstMatch; }
}
