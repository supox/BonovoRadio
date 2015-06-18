package com.supox.bonovoradio.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;

import com.google.gson.Gson;
import com.supox.bonovoradio.MainActivity;
import com.supox.bonovoradio.R;
import com.supox.bonovoradio.api.IRadio;
import com.supox.bonovoradio.api.IRadioListener;
import com.supox.bonovoradio.api.NativeRadio;
import com.supox.bonovoradio.domain.AFState;
import com.supox.bonovoradio.domain.Band;
import com.supox.bonovoradio.domain.Frequency;
import com.supox.bonovoradio.domain.Preset;
import com.supox.bonovoradio.domain.RDSState;
import com.supox.bonovoradio.domain.RadioState;
import com.supox.bonovoradio.domain.SeekState;
import com.supox.bonovoradio.domain.TunerState;

import java.util.Timer;
import java.util.TimerTask;

public class RadioService extends Service implements IRadio, AudioManager.OnAudioFocusChangeListener {
    private final static int NOTIFICATION_ID = 0x1234;
    private final static String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    private final static String ACTION_NEXT = "ACTION_NEXT";

    private IBinder mBinder = new LocalBinder();
    private NativeRadio mRadio = new NativeRadio();
    private AudioManager mAudioManager;
    private RadioState mState = new RadioState();
    private SharedPreferences settings;
    private IRadioListener mListener;
    private Gson gson = new Gson();
    private PresetsManager mPresetsManager;
    private Timer mPollTimer;
    private static final int poll_ms = 1000;
    private static final int wait_ms = 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences("RadioPreferences", MODE_PRIVATE);

        restoreState();

        mPresetsManager = new PresetsManager(this);

        // handle event with audio focus
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        mPollTimer = new Timer("Poll", true);
        mPollTimer.schedule(new PollTask(), wait_ms, poll_ms);
    }

    private void restoreState() {
        if (settings.contains("RadioState")) {
            mState = gson.fromJson(settings.getString("RadioState", ""), mState.getClass());

            mRadio.setState(true);
            setBand(mState.band);
            mRadio.setFrequency(mState.frequency.toInt());
            mRadio.setVolume(mState.volume);
        }
    }

    private void saveState() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("RadioState", gson.toJson(mState));
        editor.commit();
    }

    @Override
    public void onDestroy() {
        mPollTimer.cancel();
        mAudioManager.abandonAudioFocus(this);
        stopForeground(false);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
        mRadio.setState(false);
        saveState();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public RadioState getState() {
        return mState;
    }

    @Override
    public Frequency getFrequency() {
        mState.frequency = new Frequency(mRadio.getFrequency());
        return mState.frequency;
    }

    @Override
    public void setFrequency(Frequency freq) {
        mState.frequency = freq;
        mRadio.setFrequency(freq.toInt());

        broadcastState();
    }

    @Override
    public Band getBand() {
        switch (mRadio.getBand()) {
            case 0:
            default:
                mState.band = Band.EU;
                break;
            case 1:
                mState.band = Band.UU;
                break;
            case 2:
                mState.band = Band.US;
                break;
        }
        return mState.band;
    }

    @Override
    public void setBand(Band band) {
        int iband;
        switch (band) {
            case EU:
                iband = 0;
                break;
            case UU:
                iband = 1;
                break;
            case US:
                iband = 2;
                break;
            default:
                throw new IllegalArgumentException();
        }
        mRadio.setBand(iband);
        mState.band = band;
    }

    @Override
    public SeekState getSeekState() {
        SeekState state;
        switch (mRadio.getSeekState()) {
            case 0:
            default:
                state = SeekState.NotSeek;
                break;
            case 1:
                state = SeekState.Up;
                break;
            case 2:
                state = SeekState.Down;
                break;
        }
        mState.seekState = state;
        return state;
    }

    @Override
    public void setSeekState(SeekState state) {
        int istate;
        switch (state) {
            case NotSeek:
            default:
                istate = 0;
                break;
            case Up:
                istate = 1;
                break;
            case Down:
                istate = 2;
                break;
        }
        mRadio.setSeekState(istate);
        mState.seekState = state;
    }

    @Override
    public void setListener(IRadioListener listener) {
        mListener = listener;
    }

    @Override
    public AFState getAFState() {
        mState.afState = mRadio.getAFState() ? AFState.Start : AFState.Stop;
        return mState.afState;
    }

    @Override
    public void setAFState(AFState state) {
        mRadio.setAFState(state == AFState.Start);
        mState.afState = state;
    }

    @Override
    public RDSState getRDSState() {
        mState.rdsState = mRadio.getRDSState() ? RDSState.Start : RDSState.Stop;
        return mState.rdsState;
    }

    @Override
    public void setRDSState(RDSState state) {
        mRadio.setRDSState(state == RDSState.Start);
        mState.rdsState = state;
    }

    @Override
    public int getVolume() {
        mState.volume = mRadio.getVolume();
        return mState.volume;
    }

    @Override
    public void setVolume(int volume) {
        mRadio.setVolume(volume);
        mState.volume = volume;

        broadcastState();
    }

    @Override
    public void toggleMute() {
        if (mState.volume == 0) {
            setVolume(mState.volumeBeforeMute);
        } else {
            mState.volumeBeforeMute = mState.volume;
            setVolume(0);
        }
    }

    @Override
    public TunerState getTunerState() {
        mState.tunerState = (mRadio.getState() ? TunerState.Start : TunerState.Stop);
        return mState.tunerState;
    }

    @Override
    public void setTunerState(TunerState state) {
        mRadio.setState(state == TunerState.Start);
        mState.tunerState = state;

        broadcastState();
    }

    @Override
    public void nextStation() {
        Preset preset = mPresetsManager.nextPreset(true);
        if (preset.isValid) {
            setFrequency(preset.freq);
        }
    }

    @Override
    public void prevStation() {
        Preset preset = mPresetsManager.nextPreset(false);
        if (preset.isValid) {
            setFrequency(preset.freq);
        }
    }

    @Override
    public void setCurrentPreset(int index) {
        Preset preset = mPresetsManager.getPreset(index);
        if (preset.isValid) {
            mPresetsManager.setActivePreset(index);
            setFrequency(preset.freq);
        } else {
            saveCurrentAsPreset(index);
        }
    }

    @Override
    public void saveCurrentAsPreset(int presetIndex) {
        Frequency freq = getFrequency();
        mPresetsManager.savePreset(presetIndex, new Preset(freq.toMHzString(), freq));
        if (mListener != null)
            mListener.onPresetsChanged(getPresets());
    }

    @Override
    public void deletePreset(int presetIndex) {
        mPresetsManager.deletePreset(presetIndex);
        if (mListener != null)
            mListener.onPresetsChanged(getPresets());
    }

    @Override
    public Preset[] getPresets() {
        return mPresetsManager.getPresets().toArray(new Preset[0]);
    }

    @Override
    public void stepUp() {
        Frequency freq = getFrequency();
        setFrequency(freq.stepUp());
    }

    @Override
    public void stepDown() {
        Frequency freq = getFrequency();
        setFrequency(freq.stepDown());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            switch (intent.getAction()) {
                case ACTION_NEXT:
                    nextStation();
                    break;
                case ACTION_PREVIOUS:
                    prevStation();
                    break;
            }
        } catch (NullPointerException ex) {
        }
        return Service.START_STICKY;
    }


    private void updateNotification() {
        PendingIntent pIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent prevIntent = new Intent(this, this.getClass());
        prevIntent.setAction(ACTION_PREVIOUS);
        PendingIntent pPrevIntent = PendingIntent.getService(
                this,
                1,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Intent nextIntent = new Intent(this, this.getClass());
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pNextIntent = PendingIntent.getService(
                this,
                2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Radio")
                .setContentText(mState.frequency.toMHzString())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pIntent)
                .addAction(R.drawable.btn_rw, "", pPrevIntent)
                .addAction(R.drawable.btn_ff, "", pNextIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Restore volume
                setVolume(mState.volumeBeforeLostFocus);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopService(new Intent("com.supox.bonovoradio.service.RadioService"));
                this.stopSelf();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Reduce volume to Zero - save current volume;
                mState.volumeBeforeLostFocus = getVolume();
                setVolume(0);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // reduce volume by 50%
                mState.volumeBeforeLostFocus = getVolume();
                setVolume(getVolume() / 2);
                break;
        }
    }

    public class LocalBinder extends Binder {
        public RadioService getServerInstance() {
            return RadioService.this;
        }
    }

    private void broadcastState() {
        if (mListener != null) {
            mListener.onStateChanged(mState);
        }
        updateNotification();
    }

    private class PollTask extends TimerTask {
        @Override
        public void run() {
            if (!mRadio.getState())
                return;
            getFrequency();
            getVolume();
            getAFState();
            getRDSState();
            getBand();
            broadcastState();
        }
    }
}