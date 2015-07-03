package com.supox.bonovoradio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.supox.bonovoradio.service.RadioService;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int keyCode = keyEvent.getKeyCode();
            int keyAction = keyEvent.getAction();

            Intent it;
            if (keyAction == KeyEvent.ACTION_DOWN && keyEvent.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        it = new Intent(context, RadioService.class);
                        it.setAction(RadioService.ACTION_NEXT);
                        context.sendBroadcast(it);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        it = new Intent(context, RadioService.class);
                        it.setAction(RadioService.ACTION_PREVIOUS);
                        context.sendBroadcast(it);
                        break;
                }
            }
            if (isOrderedBroadcast()) {
                abortBroadcast();
            }
        } else if (intent.getAction().equals("android.intent.action.BONOVO_RADIO_KEY")) {
            Intent newActivityIntent = new Intent();
            newActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newActivityIntent.setAction("com.supox.bonovoradio.MainActivity");
            context.startActivity(newActivityIntent);
        }
    }
}
