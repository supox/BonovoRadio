
package com.supox.bonovoradio.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.supox.bonovoradio.domain.Preset;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class PresetsManager {
    private static final String SHARED_PREFS_FILE = "PresetsFile";
    private static final String PRESETS = "PRESETS";
    private final Context m_context;
    private List<Preset> mPresets;
    private int m_currentIndex = 0;

    public PresetsManager(Context context) {
        m_context = context;
        SharedPreferences prefs = m_context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            mPresets = deserializePresets(prefs.getString(PRESETS, "[]"));
        } catch (Exception e) {
            mPresets = new LinkedList<>();
        }
    }

    public static String serializePresets(List<Preset> presets) {
        return new Gson().toJson(presets);
    }

    public String serializePresets() {
        return serializePresets(getPresets());
    }

    public static List<Preset> deserializePresets(String presets) {
        return new LinkedList(Arrays.asList(new Gson().fromJson(presets, Preset[].class)));
    }

    public int getActivePresetIndex() {
        return m_currentIndex;
    }

    public void savePreset(int index, Preset preset) {
        while (mPresets.size() <= index) {
            mPresets.add(new Preset());
        }
        mPresets.set(index, preset);
        saveState();
    }

    public Preset getPreset(int index) {
        try {
            return mPresets.get(index);
        } catch (IndexOutOfBoundsException ex) {
            return new Preset();
        }
    }

    public List<Preset> getPresets() {
        return mPresets;
    }

    public Preset setActivePreset(int index) {
        m_currentIndex = index;
        return getPreset(index);
    }

    public Preset nextPreset(boolean up) {
        int presetIndex = m_currentIndex;
        for (int index = 0; index < mPresets.size(); index++) {
            presetIndex = (presetIndex + (up ? 1 : -1)) % mPresets.size();
            if (presetIndex < 0) presetIndex += mPresets.size();
            if (getPreset(presetIndex).isValid) {
                return setActivePreset(presetIndex);
            }
        }
        // fail, probably no presets at all
        return getPreset(m_currentIndex);
    }

    public void deletePreset(int presetIndex) {
        try {
            mPresets.get(presetIndex).isValid = false;
            saveState();
        } catch (Exception ex) {
        }
    }

    public void renamePreset(int presetIndex, String name) {
        try {
            mPresets.get(presetIndex).name = name;
            saveState();
        } catch (Exception ex) {
        }
    }

    private void saveState() {
        //save the task list to preference
        SharedPreferences prefs = m_context.getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PRESETS, serializePresets());
        editor.commit();
    }

}
