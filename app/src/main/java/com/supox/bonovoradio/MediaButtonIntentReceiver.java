package com.supox.bonovoradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.supox.bonovoradio.service.RadioService;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    private static final boolean mStartOnBoot = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_MEDIA_BUTTON:
                handleMediaButton(context, intent);
                break;
            case "android.intent.action.BONOVO_RADIO_KEY":
                Intent newActivityIntent = new Intent(context, MainActivity.class);
                context.startActivity(newActivityIntent);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                Intent newServiceIntent = new Intent(context, RadioService.class);
                context.startService(newServiceIntent);
                break;
        }
    }

    private void handleMediaButton(Context context, Intent intent) {
        KeyEvent keyEvent = (KeyEvent)
                intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int keyCode = keyEvent.getKeyCode();
        int keyAction = keyEvent.getAction();

        Intent it;
        if (keyAction == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    it = new Intent(context, RadioService.class);
                    it.setAction(RadioService.ACTION_NEXT);
                    context.startService(it);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    it = new Intent(context, RadioService.class);
                    it.setAction(RadioService.ACTION_PREVIOUS);
                    context.startService(it);
                    break;
            }
            if (isOrderedBroadcast()) {
                abortBroadcast();
            }
        }
    }
}
