package com.lowagie.text.pdf;

public class FontOptions {
    private String name;
    private String encoding;
    private boolean embedded;
    private boolean cached;
    private byte[] ttfAfm;
    private byte[] pfb;
    private boolean noThrow;

    // Constructors, getters, and setters
    public FontOptions(String name, String encoding, boolean embedded, boolean cached, byte[] ttfAfm,
            byte[] pfb, boolean noThrow) {
        this.name = name;
        this.encoding = encoding;
        this.embedded = embedded;
        this.cached = cached;
        this.ttfAfm = ttfAfm;
        this.pfb = pfb;
        this.noThrow = noThrow;
    }

    public String getName() {
        return name;
    }

    public String getEncoding() {
        return encoding;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public boolean isCached() {
        return cached;
    }

    public byte[] getTtfAfm() {
        return ttfAfm;
    }

    public byte[] getPfb() {
        return pfb;
    }

    public boolean isNoThrow() {
        return noThrow;
    }
}
