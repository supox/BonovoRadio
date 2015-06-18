package com.supox.bonovoradio.api;

import com.supox.bonovoradio.domain.Preset;
import com.supox.bonovoradio.domain.RadioState;

public interface IRadioListener {
    void onStateChanged(final RadioState state);

    void onPresetsChanged(final Preset[] presets);
}

