package com.supox.bonovoradio.api;

import com.supox.bonovoradio.domain.AFState;
import com.supox.bonovoradio.domain.Band;
import com.supox.bonovoradio.domain.Frequency;
import com.supox.bonovoradio.domain.Preset;
import com.supox.bonovoradio.domain.RDSState;
import com.supox.bonovoradio.domain.RadioState;
import com.supox.bonovoradio.domain.SeekState;
import com.supox.bonovoradio.domain.TunerState;

public interface IRadio {
    RadioState getState();

    Frequency getFrequency();

    void setFrequency(Frequency freq);

    Band getBand();

    void setBand(Band band);

    SeekState getSeekState();

    void setSeekState(SeekState state);

    void setListener(IRadioListener listener);

    AFState getAFState();

    void setAFState(AFState state);

    RDSState getRDSState();

    void setRDSState(RDSState state);

    int getVolume();

    void setVolume(int volume);

    void toggleMute();

    TunerState getTunerState();

    void setTunerState(TunerState stop);

    void nextStation();

    void prevStation();

    void setCurrentPreset(int index);

    void saveCurrentAsPreset(int presetIndex);

    void deletePreset(int presetIndex);

    Preset[] getPresets();

    void stepUp();

    void stepDown();

}
