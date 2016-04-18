package com.beanstream.sample.goldeneggs.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.beanstream.sample.goldeneggs.events.BluetoothStateChangeEvent;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    EventBus.getDefault().post(new BluetoothStateChangeEvent(false));
                    break;

                case BluetoothAdapter.STATE_ON:
                    EventBus.getDefault().post(new BluetoothStateChangeEvent(true));
                    break;

            }
        }
    }
}
