package com.supox.bonovoradio.domain;

public class Preset {
    public String name;
    public Frequency freq;
    public boolean isValid;

    public Preset() {
        name = "";
        freq = null;
        isValid = false;
    }

    public Preset(String name, Frequency freq) {
        this.name = name;
        this.freq = freq;
        isValid = true;
    }
}
