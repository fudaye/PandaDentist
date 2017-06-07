package com.pandadentist.configwifi.android;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pandadentist.configwifi.utils.Utils;

import java.util.List;

public abstract class WifiAutomaticConnecter extends Handler {

	private static final String TAG = WifiAutomaticConnecter.class.getSimpleName();
	private Context context;
	private String ssid;
	private WifiManager wifiManager;
	private boolean useDefault;

	/**
	 * @return the ssid
	 */
	String getSsid() {
		return ssid;
	}

	public WifiAutomaticConnecter(Context context, String ssid) {
		super();
		this.context = context;
		this.ssid = ssid;
		wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		useDefault = false;
	}

	/**
	 * It will use SSID setting in local
	 * @param context
	 * @see Utils#getSettingApSSID(Context)
	 */
	public WifiAutomaticConnecter(Context context) {
		super();
		this.context = context;
		wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		useDefault = true;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		//获得默认的ssid
		if (useDefault) {
			ssid = Utils.getSettingApSSID(context);
		}

		if (ssid == null || ((ssid=ssid.trim()).length() == 0)) {
			return;
		}

		List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
		//禁用AP以为的其他网络
		if (configs != null) {
			for (WifiConfiguration wifiConfiguration : configs) {
				if (!AccessPoint.removeDoubleQuotes(wifiConfiguration.SSID).equals(ssid)) {
					wifiConfiguration.priority = 0;
					wifiManager.disableNetwork(wifiConfiguration.networkId);
					wifiManager.updateNetwork(wifiConfiguration);
				}
			}
		}
		wifiManager.saveConfiguration();
        //获得当前连接的wifi信息
		WifiInfo currWifiInfo = wifiManager.getConnectionInfo();
    	String currSSID = currWifiInfo.getSSID();
//    	Log.d(TAG, "The current connection ssid is " + currSSID);
		if (currWifiInfo != null && currWifiInfo.getNetworkId()!=-1 && currSSID!=null
				&& !AccessPoint.removeDoubleQuotes(currSSID).equals(ssid)) {
	    	Log.d(TAG, "Disconnect AP-" + currSSID);
			wifiManager.disconnect();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

        List<ScanResult> results = wifiManager.getScanResults();
        if (results == null) {
        	retry();
			return;
		}

        AccessPoint accessPoint = null;
        for (ScanResult result : results) {
            // Ignore hidden and ad-hoc networks.
            if (result.SSID == null || result.SSID.length() == 0 ||
                    result.capabilities.contains("[IBSS]")) {
                continue;
            }

            if (result.SSID.equals(ssid)) {
            	accessPoint = new AccessPoint(context, result);
            	break;
			}
        }

        if (accessPoint != null) {

        	currWifiInfo = wifiManager.getConnectionInfo();
        	currSSID = currWifiInfo.getSSID();
        	Log.d(TAG, "The current connection ssid is " + currWifiInfo);

        	if (currSSID == null && currWifiInfo.getSupplicantState().compareTo(SupplicantState.ASSOCIATING) >= 0 &&
        			currWifiInfo.getSupplicantState().compareTo(SupplicantState.COMPLETED) <= 0) {
        		sendEmptyMessageDelayed(0, 2000);
				return;
			}

        	//if the current connection ap is not the given ssid, try to connect the given ssid
        	if (! (currWifiInfo!=null && currWifiInfo.getNetworkId()!=-1 &&
        			currSSID!=null && AccessPoint.removeDoubleQuotes(currSSID).equals(ssid))) {
        		//remove all of wifi configuration which has the same ssid
        		configs = wifiManager.getConfiguredNetworks();
        		for (WifiConfiguration wifiConfiguration : configs) {
        			if (AccessPoint.removeDoubleQuotes(wifiConfiguration.SSID).equals(ssid)) {
        				wifiManager.removeNetwork(wifiConfiguration.networkId);
        			}
        		}
        		wifiManager.saveConfiguration();
        		wifiManager.disconnect();

        		if (accessPoint.getSecurity() == AccessPoint.SECURITY_NONE) {
        			// Shortcut for open networks.
        			Log.d(TAG, "try to connect open none wifi :" + ssid);
        			WifiConfiguration config = new WifiConfiguration();
        			config.SSID = AccessPoint.convertToQuotedString(accessPoint.getSsid());
        			config.allowedKeyManagement.set(KeyMgmt.NONE);
        			config.priority = Integer.MAX_VALUE;
        			int networkId = wifiManager.addNetwork(config);
        			wifiManager.enableNetwork(networkId, true);
        			wifiManager.saveConfiguration();
        			wifiManager.reconnect();
        		} else {
        			Log.d(TAG, "try to connect security wifi :" + ssid);
        			connectSecurity(accessPoint);
        		}
        	}
        }else {
			onSsidNotFind();
		}

		retry();
	}

	public void resume() {
		if (!hasMessages(0)) {
			sendEmptyMessage(0);
		}
	}

	public void pause() {
		removeMessages(0);
	}

	private void retry() {
		sendEmptyMessageDelayed(0, 8000);
	}

	/**
	 * the wifi with the specific ssid is not find
	 * @see #WifiAutomaticConnecter(Context, String)
	 */
	public void onSsidNotFind() {

	}

	/**
	 * connect to the open none wifi, it doesn't need password
	 * @param accessPoint
	 * @param networkId
	 */
	public abstract void connectOpenNone(AccessPoint accessPoint, int networkId);
	/**
	 * connect the specific security wifi, it needs password
	 * @param accessPoint
	 */
	public abstract void connectSecurity(AccessPoint accessPoint);
}
