package com.pandadentist.configwifi.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.pandadentist.R;

/**
 * A scanner to scan the wifi access point
 * @author ZhangGuoYin
 *
 */
class Scanner extends Handler {

	private static final String TAG = Scanner.class.getSimpleName();

	private Context context;
	private WifiManager wifiManager;
	private int retry = 0;

	Scanner(Context context) {
		this.context = context;
		wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	}

	void resume() {
		if (!hasMessages(0)) {
			sendEmptyMessage(0);
		}
	}

	void pause() {
		retry = 0;
		removeMessages(0);
	}

	@Override
	public void handleMessage(Message message) {
		Log.d(TAG,"扫描wifi 每6秒一次");
		if (wifiManager.startScan()) {
			retry = 0;
		} else if (++retry >= 3) {
			retry = 0;
			Toast.makeText(context, R.string.wifi_fail_to_scan,
					Toast.LENGTH_LONG).show();
			return;
		}
		sendEmptyMessageDelayed(0, 6000);
	}
}