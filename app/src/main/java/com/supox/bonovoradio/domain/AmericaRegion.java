package com.supox.bonovoradio.domain;

public class AmericaRegion extends Region {
    @Override
    public int getInc() {
        return 20;
    }

    @Override
    public boolean getFreqOdd() {
        return true;
    }
}

