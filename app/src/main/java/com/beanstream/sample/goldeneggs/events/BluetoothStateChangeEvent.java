package com.beanstream.sample.goldeneggs.events;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 *
 * Created by babramovitch on 03/02/2016.
 */
public class BluetoothStateChangeEvent {

    private boolean bluetoothOn;

    public BluetoothStateChangeEvent(boolean bluetoothOn) {
        this.bluetoothOn = bluetoothOn;
    }

    public boolean isBluetoothOn() {
        return bluetoothOn;
    }
}
