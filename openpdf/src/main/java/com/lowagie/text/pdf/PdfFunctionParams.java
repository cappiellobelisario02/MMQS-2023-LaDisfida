package com.lowagie.text.pdf;

public class PdfFunctionParams {
    private float[] domain;
    private float[] range;
    private int[] size;
    private int bitsPerSample;
    private int order;
    private float[] encode;
    private float[] decode;
    private byte[] stream;

    // Costruttore
    public PdfFunctionParams(float[] domain, float[] range, int[] size, int bitsPerSample, int order, float[] encode, float[] decode, byte[] stream) {
        this.domain = domain;
        this.range = range;
        this.size = size;
        this.bitsPerSample = bitsPerSample;
        this.order = order;
        this.encode = encode;
        this.decode = decode;
        this.stream = stream;
    }

    // Getter per tutti i campi
    public float[] getDomain() { return domain; }
    public float[] getRange() { return range; }
    public int[] getSize() { return size; }
    public int getBitsPerSample() { return bitsPerSample; }
    public int getOrder() { return order; }
    public float[] getEncode() { return encode; }
    public float[] getDecode() { return decode; }
    public byte[] getStream() { return stream; }
}
