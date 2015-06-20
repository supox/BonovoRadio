package com.supox.bonovoradio.domain;

public class Frequency {
    private static final int MHZ_FREQ_INC = 5;
    private final int mFrequency;

    public Frequency(String frequency) {
        this(Integer.valueOf(frequency));
    }

    public Frequency(int frequency) {
        mFrequency = Region.getCurrentRegion().normalizeFrequency(frequency);
    }

    public int toInt() {
        return mFrequency;
    }

    public String toMHzString() {
        int freq = toInt();
        freq += MHZ_FREQ_INC / 2;
        freq = freq / MHZ_FREQ_INC;
        freq *= MHZ_FREQ_INC;
        double dfreq = (double) freq;
        dfreq = java.lang.Math.rint(dfreq);
        dfreq /= 100;
        return ("" + dfreq);
    }

    public Frequency stepDown() {
        return new Frequency(toInt() - Region.getCurrentRegion().getInc());
    }

    public Frequency stepUp() {
        return new Frequency(toInt() + Region.getCurrentRegion().getInc());
    }
}

