package com.beanstream.sample.goldeneggs.settings;

import android.bluetooth.BluetoothAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.beanstream.emv.devices.PinPad;

import com.beanstream.mobilesdk.BeanstreamAPI;
import com.beanstream.sample.goldeneggs.GoldenEggsApplication;
import com.beanstream.sample.goldeneggs.R;

import java.util.ArrayList;

/**
 * Copyright © 2016 Beanstream Internet Commerce, Inc. All rights reserved.
 */
public class HardwarePreferenceFragment extends PreferenceFragment {

    public final static String PREFERENCE_KEY_ICMP = "ICMP";

    CharSequence[] bluetoothNameValues;
    boolean bluetoothEnabled;
    BeanstreamAPI beanstreamAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        GoldenEggsApplication application = (GoldenEggsApplication) getActivity().getApplication();
        beanstreamAPI = application.getBeanstreamAPI();

        bluetoothEnabled = getBluetoothStatus();

        setupPinPadSetting();
    }

    public boolean getBluetoothStatus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * Get the list of paired iCMPs from beanstreamAPI and then set the summary to the currently activated iCMP
     * <p/>
     * Lets the user select an iCMP from the list and set it to activated.
     * <p/>
     * To have this setting persist across app restarts, call {@link BeanstreamAPI#setActivePinPad(String)} with
     * the preference value when restarting the application.
     *
     * Note - This gets called in onCreate and onResume.  If it's not called in onCreate the preference loading crashes
     * as there are no values.  OnResume is there to cover any change in states if you leave/return to the app.
     */
    private void setupPinPadSetting() {

        final ArrayList<PinPad> pinPads = beanstreamAPI.getPairedPinPads();
        final ListPreference iCMP = (ListPreference) findPreference(PREFERENCE_KEY_ICMP);

        if (pinPads != null && pinPads.size() > 0) {
            iCMP.setEnabled(true);

            bluetoothNameValues = new CharSequence[pinPads.size()];

            boolean foundActivatedDevice = false;

            //Iterate through the list looking for an activated pin pad
            for (int x = 0; x < pinPads.size(); x++) {
                PinPad pinPad = pinPads.get(x);
                bluetoothNameValues[x] = pinPad.getPinPadName();

                if (pinPads.get(x).isActivated()) {
                    foundActivatedDevice = true;
                    iCMP.setSummary(pinPad.getPinPadName());
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    preferences.edit().putString(PREFERENCE_KEY_ICMP, pinPad.getPinPadName()).apply();
                }
            }

            //If no activated devices were found, set the 0 position record
            if (!foundActivatedDevice) {
                iCMP.setSummary(pinPads.get(0).getPinPadName());
                beanstreamAPI.setActivePinPad(pinPads.get(0).getPinPadName());
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preferences.edit().putString(PREFERENCE_KEY_ICMP, pinPads.get(0).getPinPadName()).apply();
            }

            //Set the list preference values
            iCMP.setEntries(bluetoothNameValues);
            iCMP.setEntryValues(bluetoothNameValues);

            //Update the activated iCMP based off user selection
            iCMP.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    beanstreamAPI.setActivePinPad(newValue.toString());
                    iCMP.setSummary(newValue.toString());
                    return true;
                }
            });
        } else {
            if (bluetoothEnabled) {
                iCMP.setSummary(getString(R.string.settings_no_icmps_paired));
            } else {
                iCMP.setSummary(getString(R.string.settings_bluetooth_disabled));
            }
            iCMP.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupPinPadSetting();
    }
}
