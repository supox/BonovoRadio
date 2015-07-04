package com.supox.bonovoradio;

import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class FrequencyDial implements View.OnTouchListener {
    public void setMaxFreq(int maxFreq) {
        mMaxAngle = frequencyToAngle(maxFreq);
    }

    public void setMinFreq(int minFreq) {
        mMinAngle = frequencyToAngle(minFreq);
    }

    public interface Listener {
        void onFreqChanged(int freq);
    }

    private Listener mListener;
    private boolean isDownDial = false;
    private final View mView;
    private double mDesiredAngle = 0;
    private Timer timer;

    private double mMaxAngle = frequencyToAngle(10800);
    private double mMinAngle = frequencyToAngle(8760);

    private static final double FREQ_PERCENT_FACTOR = 25.15 / 3;
    private static final double FREQ_AT_210 = 8520 + 150 * FREQ_PERCENT_FACTOR;

    public FrequencyDial(View view) {
        mView = view;
        mView.setOnTouchListener(this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setFrequency(int frequency) {
        if (!isDownDial)
            setViewAngle(frequencyToAngle(frequency));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isDownDial = true;
                return updateScrollPoint(getAngle(event));
            case MotionEvent.ACTION_UP:
                isDownDial = false;
                return true;
        }
        return false;
    }

    public void updateListener(double angle) {
        mDesiredAngle = angle;
        try {
            timer.cancel();
        } catch (NullPointerException ex) {}
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null)
                            mListener.onFreqChanged(angleToFrequency(mDesiredAngle));

                    }
                });
            }
        }, 50);
    }

    private void setViewAngle(double angle) {
        mView.setRotation((float) angle);
    }

    private static final double frequencyToAngle(int frequency) {
        return (double) (frequency - FREQ_AT_210) / FREQ_PERCENT_FACTOR;
    }

    private static final int angleToFrequency(double angle) {
        return (int) (FREQ_PERCENT_FACTOR * angle + FREQ_AT_210);
    }

    private double getAngle(MotionEvent event) {
        double eventAngle = Math.toDegrees(Math.atan2(event.getX() - mView.getWidth() / 2,
                mView.getHeight() / 2 - event.getY()));
        double rotatedAngle = eventAngle + mView.getRotation();
        double fixedAngle = rotatedAngle % 360;
        while (fixedAngle > mMaxAngle)
            fixedAngle -= 360;
        while (fixedAngle < mMinAngle)
            fixedAngle += 360;

        return fixedAngle;
    }

    private boolean updateScrollPoint(double angle) {
        if(!isAngleValid(angle))
            return false;

        setViewAngle(angle);
        updateListener(angle);

        return true;
    }

    private boolean isAngleValid(double angle) {
        if (Double.isNaN(angle))
            return (false);
        return (angle >= mMinAngle && angle <= mMaxAngle);
    }
}

