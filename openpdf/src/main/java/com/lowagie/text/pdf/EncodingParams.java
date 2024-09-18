package com.lowagie.text.pdf;

import com.lowagie.text.pdf.EncodingParams.Builder;

public class EncodingParams {
    private final byte[] text;
    private final int textOffset;
    private final int textSize;
    private final byte[] data;
    private final int dataOffset;
    private final int dataSize;
    private final int options;
    private final boolean firstMatch;

    private EncodingParams(com.lowagie.text.pdf.EncodingParams.Builder builder) {
        this.text = builder.text;
        this.textOffset = builder.textOffset;
        this.textSize = builder.textSize;
        this.data = builder.data;
        this.dataOffset = builder.dataOffset;
        this.dataSize = builder.dataSize;
        this.options = builder.options;
        this.firstMatch = builder.firstMatch;
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

    public static class Builder {
        private byte[] text;
        private int textOffset;
        private int textSize;
        private byte[] data;
        private int dataOffset;
        private int dataSize;
        private int options;
        private boolean firstMatch;

        public com.lowagie.text.pdf.EncodingParams.Builder text(byte[] text) {
            this.text = text;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder textOffset(int textOffset) {
            this.textOffset = textOffset;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder dataOffset(int dataOffset) {
            this.dataOffset = dataOffset;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder dataSize(int dataSize) {
            this.dataSize = dataSize;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder options(int options) {
            this.options = options;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams.Builder firstMatch(boolean firstMatch) {
            this.firstMatch = firstMatch;
            return this;
        }

        public com.lowagie.text.pdf.EncodingParams build() {
            return new com.lowagie.text.pdf.EncodingParams(this);
        }
    }
}
