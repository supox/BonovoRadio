package com.supox.bonovoradio.domain;

public class RadioState implements java.io.Serializable {
    public Band band;
    public Frequency frequency;
    public RDSState rdsState;
    public SeekState seekState;
    public StereoState stereoState;
    public TunerState tunerState;
    public RDSInfo rdsInfo;
    public TunerPilot tunerPilot;
    public int rssi;
    public int volume;
    public int volumeBeforeLostFocus;
    public int volumeBeforeMute;
}
