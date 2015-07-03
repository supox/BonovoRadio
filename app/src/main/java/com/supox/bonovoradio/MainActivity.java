package com.supox.bonovoradio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.supox.bonovoradio.api.IRadio;
import com.supox.bonovoradio.api.IRadioListener;
import com.supox.bonovoradio.domain.Frequency;
import com.supox.bonovoradio.domain.Preset;
import com.supox.bonovoradio.domain.RadioState;
import com.supox.bonovoradio.domain.SeekState;
import com.supox.bonovoradio.domain.TunerState;
import com.supox.bonovoradio.service.RadioService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity implements ServiceConnection {
    private final List<ImageButton> mPresetsButtons = new ArrayList<>();
    private final List<TextView> mPresetsTexts = new ArrayList<>();
    private IRadio mRadio;
    private RadioState mState;
    private TextView mFreqView;
    private SeekBar mVolumeSeekbar;
    private ImageView mPausePlayButton;
    private ImageView mPowerButton;

    private BroadcastReceiver mBroadcastReceiver;
    private final SimpleDateFormat sdfWatchTime = new SimpleDateFormat("HH:mm");
    private TextView mClockTV;
    private FrequencyDial mFrequencyDialer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public void onStart() {
        super.onStart();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0 && mClockTV != null) {
                    mClockTV.setText(sdfWatchTime.format(new Date()));
                }
            }
        };

        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBroadcastReceiver != null)
            unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindToRadioService();
    }

    private void bindToRadioService() {
        Intent serviceIntent = new Intent(this, RadioService.class);
        this.startService(serviceIntent);
        bindService(serviceIntent, this, BIND_AUTO_CREATE);
    }

    private void findViewElements() {
        mClockTV = (TextView) findViewById(R.id.clock_tv);
        mClockTV.setText(sdfWatchTime.format(new Date()));

        mFreqView = (TextView) findViewById(R.id.tv_freq);
        findViewById(R.id.iv_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.toggleMute();
                } catch (NullPointerException ex) {
                }
            }
        });
        findViewById(R.id.iv_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.stepUp();
                } catch (NullPointerException ex) {
                }

            }
        });
        findViewById(R.id.iv_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.stepDown();
                } catch (NullPointerException ex) {
                }

            }
        });
        mPausePlayButton = (ImageView) findViewById(R.id.iv_paupla);
        mPausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.toggleMute();
                } catch (NullPointerException ex) {
                }

            }
        });
        findViewById(R.id.iv_seekdn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.setSeekState(SeekState.Down);
                } catch (NullPointerException ex) {
                }
            }
        });
        findViewById(R.id.iv_seekup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mRadio.setSeekState(SeekState.Up);
                } catch (NullPointerException ex) {
                }
            }
        });
        mPowerButton = (ImageView) findViewById(R.id.iv_power);
        mPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mRadio.getTunerState() == TunerState.Start) {
                        mRadio.setTunerState(TunerState.Stop);
		    } else {
			bindToRadioService();
                        // mRadio.setTunerState(TunerState.Start);
		    }
                } catch (NullPointerException ex) {
                }
            }
        });

        mPresetsButtons.clear();
        mPresetsTexts.clear();
        for (int index = 0; ; index++) {
            ImageButton ib = (ImageButton) findViewByName("ib_preset_" + index);
            TextView tv = (TextView) findViewByName("tv_preset_" + index);
            if (ib == null || tv == null)
                break;
            mPresetsButtons.add(ib);
            mPresetsTexts.add(tv);
        }
        for (View presetView : mPresetsButtons) {
            presetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = mPresetsButtons.indexOf((ImageButton) v);
                    if (index >= 0)
                        mRadio.setCurrentPreset(index);
                }
            });
            presetView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int index = mPresetsButtons.indexOf((ImageButton) v);
                    if (index >= 0) {
                        showEditPresetDialog(index);
                        return true;
                    }
                    return false;
                }
            });
        }
        try {
            updatePresets(mRadio.getPresets());
        } catch (NullPointerException ex) {
        }

        mVolumeSeekbar = (SeekBar) findViewById(R.id.seek_volume);
        mVolumeSeekbar.setMax(100);
        if (mRadio != null)
            mVolumeSeekbar.setProgress(mRadio.getVolume());

        mVolumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadio.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        mFrequencyDialer = new FrequencyDial(findViewById(R.id.frequency_needle));
        mFrequencyDialer.setListener(new FrequencyDial.Listener() {
            @Override
            public void onFreqChanged(int freq) {
                try {
                    mRadio.setFrequency(new Frequency(freq));
                } catch(NullPointerException ex) {
                }
            }
        });

    }

    private View findViewByName(String resName) {
        Resources res = getResources();
        int id = res.getIdentifier(resName, "id", getPackageName());
        return findViewById(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mRadio = ((RadioService.LocalBinder) service).getServerInstance();
        mRadio.setListener(mRadioListener);

        findViewElements();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mRadio = null;
    }

    private IRadioListener mRadioListener = new IRadioListener() {
        @Override
        public void onStateChanged(final RadioState state) {
            mState = state;
            runOnUiThread(new Runnable() {
                public void run() {
                    updateStateView();
                }
            });
        }

        @Override
        public void onPresetsChanged(final Preset[] presets) {
            runOnUiThread(new Runnable() {
                public void run() {
                    updatePresets(presets);
                }
            });

        }
    };

    private void updateStateView() {
        if (mState == null)
            return;
        mFreqView.setText(mState.frequency.toMHzString());
        if (!mVolumeSeekbar.isPressed())
            mVolumeSeekbar.setProgress(mState.volume);

        boolean muted = mState.volume == 0;
        if (muted)
            mPausePlayButton.setImageResource(R.drawable.btn_play);
        else
            mPausePlayButton.setImageResource(R.drawable.sel_pause);

        if (mState.tunerState == TunerState.Start) {
            mPowerButton.setImageResource(R.drawable.power_on);
        } else {
            mPowerButton.setImageResource(R.drawable.power_off);
        }

        mFrequencyDialer.setFrequency(mState.frequency.toInt());
    }

    private void updatePresets(Preset[] presets) {
        try {
            for (int index = 0; index < presets.length; index++) {
                final Preset preset = presets[index];
                if (preset.isValid) {
                    mPresetsButtons.get(index).setImageResource(R.drawable.transparent);
                    mPresetsTexts.get(index).setText(preset.name);
                } else {
                    mPresetsButtons.get(index).setImageResource(R.drawable.btn_preset);
                    mPresetsTexts.get(index).setText("");
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
    }

    private void showEditPresetDialog(final int presetIndex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Preset");

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        builder.setItems(new String[]{"Replace", "Rename", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case -1:
                        mRadio.setCurrentPreset(presetIndex);
                        break;
                    case 0:
                        mRadio.saveCurrentAsPreset(presetIndex);
                        break;
                    case 1:
                        showRenameDialog(presetIndex);
                        break;
                    case 2:
                        mRadio.deletePreset(presetIndex);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void showRenameDialog(final int presetIndex) {
        if(mRadio == null)
            return;

        LayoutInflater factory = LayoutInflater.from (this);
        final View editTextView = factory.inflate(R.layout.edit_text, null);
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setTitle("Preset Rename");
        builder.setView(editTextView);
        final EditText presetNameEditText = (EditText) editTextView.findViewById(R.id.et_name);
        presetNameEditText.setText(mRadio.getPresets()[presetIndex].name);
        // final EditText presetFreqEditText = (EditText) editTextView.findViewById(R.id.et_freq);
        // presetFreqEditText.setText(mRadio.getPresets()[presetIndex].freq.toMHzString());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mRadio.renamePreset(presetIndex, presetNameEditText.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }
}

