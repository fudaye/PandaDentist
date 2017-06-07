/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pandadentist.configwifi.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.pandadentist.R;

public class WifiEnabler implements OnCheckedChangeListener{

    private final Context mContext;
//    private final ToggleButton mToggleButton;
    private final CheckBox mToggleButton;
    private final TextView mTextView;
    private final CharSequence mOriginalSummary;

    private final WifiManager mWifiManager;
    private final IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                        intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                handleStateChanged(((NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
            }
        }
    };

    private boolean valid = true;

    public WifiEnabler(Context context, CheckBox toggleButton, TextView textView) {
        mContext = context;
        mToggleButton = toggleButton;
        mTextView = textView;
        mOriginalSummary = mTextView.getText();

        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        // The order matters! We really should not depend on this. :(
        mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void resume() {
        // Wi-Fi state is sticky, so just let the receiver update UI
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mToggleButton.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
        mToggleButton.setOnCheckedChangeListener(null);
    }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                mTextView.setText(R.string.wifi_starting);
                mToggleButton.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setToggleBtnCheckedWithNoAction(true);
                mTextView.setText(null);
                mToggleButton.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mTextView.setText(R.string.wifi_stopping);
                mToggleButton.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setToggleBtnCheckedWithNoAction(false);
                mTextView.setText(mOriginalSummary);
                mToggleButton.setEnabled(true);
                break;
            default:
                setToggleBtnCheckedWithNoAction(false);
                mTextView.setText(R.string.wifi_error);
                mToggleButton.setEnabled(true);
        }

        valid = true;
    }

    private void handleStateChanged(NetworkInfo.DetailedState state) {
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the check box as an optimization.
        if (state != null && mToggleButton.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                mTextView.setText(Summary.get(mContext, info.getSSID(), state));
            }
        }
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
/**by guoyin*
		setToggleBtnCheckedWithNoAction(!isChecked);
//		Log.d("123", isChecked+"");
//		if (!valid) {
//			return;
//		}

        boolean enable = isChecked;//(Boolean) value;

        // Show toast message if Wi-Fi is not allowed in airplane mode
//        if (enable && !WirelessSettings
//                .isRadioAllowed(mContext, Settings.System.RADIO_WIFI)) {
//        if(2==2){
//            Toast.makeText(mContext, R.string.wifi_in_airplane_mode,
//                    Toast.LENGTH_SHORT).show();
//            setToggleBtnCheckedWithNoAction(false);
//            return;
//        }

        *//**
         * Disable tethering if enabling Wifi
         *//*
        int wifiApState = mWifiManager.getWifiApState();
        if (enable && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
            mWifiManager.setWifiApEnabled(null, false);
        }
        if (mWifiManager.setWifiEnabled(enable)) {
            mToggleButton.setEnabled(false);
        } else {
            mTextView.setText(R.string.wifi_error);
        }

        valid = false;
        // Don't update UI to opposite state until we're sure
        //return false;
***by guoyin***/

        new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected void onPreExecute() {
				setToggleBtnCheckedWithNoAction(!isChecked);
			}

			@Override
			protected Boolean doInBackground(Void... params) {
				boolean enable = isChecked;

				/**
				 * Disable tethering if enabling Wifi
				 */
				int wifiApState = mWifiManager.getWifiState();
				if (enable && ((wifiApState == WifiManager.WIFI_STATE_ENABLING) ||
						(wifiApState == WifiManager.WIFI_STATE_ENABLED))) {
					mWifiManager.setWifiEnabled(false);
				}

				return mWifiManager.setWifiEnabled(enable);
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result != null && result) {
					mToggleButton.setEnabled(false);
				}else {
					mTextView.setText(R.string.wifi_error);
				}

		        valid = false;
			}
		}.execute(null, null, null);
	}

	private void setToggleBtnCheckedWithNoAction(boolean checked) {
		mToggleButton.setOnCheckedChangeListener(null);
		mToggleButton.setChecked(checked);
		mToggleButton.setOnCheckedChangeListener(this);
	}
}
