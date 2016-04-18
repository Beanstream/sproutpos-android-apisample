package com.beanstream.sample.goldeneggs;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.events.BluetoothStateChangeEvent;
import com.beanstream.simulator.BeanstreamAPISimulator;

import de.greenrobot.event.EventBus;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 * <p/>
 * Created by babramovitch on 03/02/2016.
 * <p/>
 * Application class to host the BeanstreamAPI and manage the start/stop of the PIN pad service.
 * <p/>
 * We manage the service from here so it can survive changes in activities and rotations without restarting
 */
public class GoldenEggsApplication extends Application {

    BeanstreamAPI beanstreamAPI;
    public int activitiesResumed = 0;
    boolean wasPinpadStopped = true;
    boolean isBluetoothEnabled = false;
    Handler handler;

    /**
     * You can enable or disable the API simulator by toggling the API used below.
     *
     * If you disable the simulator, the API will point to our production systems so you can try
     * with a real merchant account and a physical iCMP.
     */
    public boolean isSimulation() {
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (isSimulation()) {

            /*
            The simulator has an interactive mode which you can enable with the true/false flag.
            When enabled, it will prompt you for the response you would like to receive.

            2000 is how many milliseconds it waits before a response gets be returned, allowing you to
            test things like device rotation, or leaving the app etc.
            */

            beanstreamAPI = new BeanstreamAPISimulator(this, false, 2000);
        } else {
            beanstreamAPI = new BeanstreamAPI(this);
        }

        beanstreamAPI.enablePasswordRetryWhenRememberMeOff();

        handler = new Handler();
        isBluetoothEnabled = getBluetoothStatus();

        EventBus.getDefault().registerSticky(this);
    }

    /**
     * Event that gets triggered from {@link com.beanstream.sample.goldeneggs.receivers.BluetoothReceiver}
     * <p/>
     * Used to start/stop the service as long as an activity is resumed.
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(BluetoothStateChangeEvent event) {

        isBluetoothEnabled = event.isBluetoothOn();

        if (activitiesResumed != 0) {
            if (isBluetoothEnabled) {
                beanstreamAPI.startPinPadService();
            } else {
                beanstreamAPI.stopPinPadService();
            }
        }
    }

    public boolean getBluetoothStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * We only want to start the service if it was previous stopped and bluetooth is enabled.
     * <p/>
     * We're also keeping track of how many activities call this method so onPause won't stop it
     * unless all relevant activities are closed (app minimized)
     * <p/>
     * We put a 1500 millisecond delay on this so we aren't able to rapidly start/stop the service.
     */
    public void activityResumed() {
        activitiesResumed++;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (beanstreamAPI != null && wasPinpadStopped && isBluetoothEnabled) {
                    beanstreamAPI.startPinPadService();
                    wasPinpadStopped = false;
                }
            }
        };
        handler.postDelayed(runnable, 1500);
    }

    /**
     * We only want to stop the service if no activities remain open.
     * <p/>
     * We put a delay on this so the activity is resumed from rotation / activity changes
     * before the stop gets called.
     * <p/>
     * The delay also helps prevent a rapid start/stop of the service
     */
    public void activityPaused() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                activitiesResumed--;
                if (activitiesResumed == 0 && beanstreamAPI != null) {
                    beanstreamAPI.stopPinPadService();
                    wasPinpadStopped = true;
                }
            }
        };
        handler.postDelayed(runnable, 300);
    }

    public BeanstreamAPI getBeanstreamAPI() {
        return this.beanstreamAPI;
    }

}
