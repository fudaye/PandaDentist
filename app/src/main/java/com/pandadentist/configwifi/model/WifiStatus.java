package com.pandadentist.configwifi.model;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.pandadentist.configwifi.utils.Utils;

import java.util.List;

public class WifiStatus {

	private boolean enable;
	private String ssid;
	private String BSSID;
	private int networkId = Integer.MIN_VALUE;
	private WifiManager wifiManager;

	public WifiStatus(Context context) {
		wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * @return the enable
	 */
	public boolean isEnable() {
		return enable;
	}
	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * @return the ssid
	 */
	public String getSsid() {
		return ssid;
	}
	/**
	 * @param ssid the ssid to set
	 */
	public void setSsid(String ssid) {
		this.ssid = ssid;
	}
	/**
	 * @return the networkId
	 */
	public int getNetworkId() {
		return networkId;
	}
	/**
	 * @param networkId the networkId to set
	 */
	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	/**
	 * @return the bSSID
	 */
	public String getBSSID() {
		return BSSID;
	}

	/**
	 * @param bSSID the bSSID to set
	 */
	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WifiStatus [enable=" + enable + ", ssid=" + ssid + ", BSSID="
				+ BSSID + ", networkId=" + networkId + ", wifiManager="
				+ wifiManager + "]";
	}

	public void load() {

		enable = wifiManager.isWifiEnabled();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
        	BSSID = wifiInfo.getBSSID();
        	ssid = wifiInfo.getSSID()==null ? null : wifiInfo.getSSID().trim();
        	networkId = wifiInfo.getNetworkId();
		}
	}

	public void reload() {

		if (!enable) {
			wifiManager.setWifiEnabled(false);
		}else {

			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}

			WifiInfo current = wifiManager.getConnectionInfo();
			if (current!=null && current.getBSSID()!=null && current.getBSSID().equals(BSSID)) {
				return;
			}
			wifiManager.disconnect();

			List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
			if (configurations != null) {

				WifiConfiguration bestConfiguration = null;
				WifiConfiguration betterConfiguration = null;
				WifiConfiguration goodConfiguration = null;
				boolean networkIdFind = false;
				boolean ssidFind = false;
				for (WifiConfiguration wifiConfiguration : configurations) {

					networkIdFind = wifiConfiguration.networkId==networkId;
					ssidFind = Utils.removeDoubleQuotes(wifiConfiguration.SSID).equals(ssid);
					if (networkIdFind && ssidFind) {
						bestConfiguration = wifiConfiguration;
						break;
					}else if (networkIdFind) {
						betterConfiguration = wifiConfiguration;
					}else if (ssidFind) {
						goodConfiguration = wifiConfiguration;
					}
				}

				if (bestConfiguration != null) {
					wifiManager.enableNetwork(bestConfiguration.networkId, true);
				}else if (betterConfiguration != null) {
					wifiManager.enableNetwork(betterConfiguration.networkId, true);
				}else if (goodConfiguration != null) {
					wifiManager.enableNetwork(goodConfiguration.networkId, true);
				}
			}
		}
	}
}
