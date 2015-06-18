package com.supox.bonovoradio.api;

public class NativeRadio {
    static {
        System.loadLibrary("app");
    }

    public native final void setState(boolean on) throws IllegalStateException;

    public native final boolean getState() throws IllegalStateException;

    public native final int getFrequency() throws IllegalStateException;

    public native final void setFrequency(int freq) throws IllegalStateException;

    public native final int getBand() throws IllegalStateException;

    public native final void setBand(int band) throws IllegalStateException;

    public native final int getSeekState() throws IllegalStateException;

    public native final void setSeekState(int state) throws IllegalStateException;

    public native final boolean getAFState() throws IllegalStateException;

    public native final void setAFState(boolean on) throws IllegalStateException;

    public native final boolean getRDSState() throws IllegalStateException;

    public native final void setRDSState(boolean on) throws IllegalStateException;

    public native final int getVolume() throws IllegalStateException;

    public native final void setVolume(int volume) throws IllegalStateException;
}
