package com.lowagie.text.pdf;

// Class to group related attributes
class PdfFunctionAttributes {
    private float[] domain;
    private float[] range;
    private int[] size;
    private float[] encode;
    private float[] decode;

    public PdfFunctionAttributes(float[] domain, float[] range, int[] size, float[] encode, float[] decode) {
        this.domain = domain;
        this.range = range;
        this.size = size;
        this.encode = encode;
        this.decode = decode;
    }

    public float[] getDomain() { return domain; }
    public float[] getRange() { return range; }
    public int[] getSize() { return size; }
    public float[] getEncode() { return encode; }
    public float[] getDecode() { return decode; }
}

// Updated PdfFunctionParams class
public class PdfFunctionParams {
    private PdfFunctionAttributes attributes;
    private int bitsPerSample;
    private int order;
    private byte[] stream;

    // Constructor with reduced parameters
    public PdfFunctionParams(PdfFunctionAttributes attributes, int bitsPerSample, int order, byte[] stream) {
        this.attributes = attributes;
        this.bitsPerSample = bitsPerSample;
        this.order = order;
        this.stream = stream;
    }

    // Getter methods
    public PdfFunctionAttributes getAttributes() { return attributes; }
    public int getBitsPerSample() { return bitsPerSample; }
    public int getOrder() { return order; }
    public byte[] getStream() { return stream; }
}
