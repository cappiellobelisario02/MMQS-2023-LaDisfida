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

    private EncodingParams(Builder builder) {
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

        public Builder text(byte[] text) {
            this.text = text;
            return this;
        }

        public Builder textOffset(int textOffset) {
            this.textOffset = textOffset;
            return this;
        }

        public Builder textSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder dataOffset(int dataOffset) {
            this.dataOffset = dataOffset;
            return this;
        }

        public Builder dataSize(int dataSize) {
            this.dataSize = dataSize;
            return this;
        }

        public Builder options(int options) {
            this.options = options;
            return this;
        }

        public Builder firstMatch(boolean firstMatch) {
            this.firstMatch = firstMatch;
            return this;
        }

        public EncodingParams build() {
            return new EncodingParams(this);
        }
    }
}
