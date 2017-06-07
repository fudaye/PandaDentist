package com.pandadentist.configwifi.android;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.pandadentist.R;

import java.util.List;

public class AccessPoint implements Comparable<AccessPoint> {

	public static final int[] STATE_SECURED = {R.attr.state_encrypted};
	public static final int[] STATE_NONE = {};

    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    public static final int SECURITY_EAP = 3;

    private final String ssid;
    private final int security;
    private final int networkId;

    private Context mContext;
    private WifiConfiguration mConfig;
    private ScanResult mScanResult;
    private WifiInfo mInfo;
    private DetailedState mState;
    private int mRssi;
    private String mSummary;
    private String mCapabilites;
    private String mBSSID;

    public static String getSecurity(int security) {

    	String name = null;
    	switch (security) {
		case SECURITY_NONE:
			name = "open,none";
			break;
		case SECURITY_WEP:
			name = "open,wep";
			break;
		case SECURITY_PSK:
			name = "OPEN/WEP";
			break;
		case SECURITY_EAP:
			name = "OPEN/WEP";
			break;

		default:
			break;
		}

    	return name;
    }

    static int getSecurity(WifiConfiguration config) {

        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }

        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }

        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    /**
	 * @return the ssid
	 */
	public String getSsid() {
		return ssid;
	}

	/**
	 * @return the networkId
	 */
	public int getNetworkId() {
		return networkId;
	}

	/**
	 * @return the security
	 */
	public int getSecurity() {
		return security;
	}

	/**
	 * @return the mRssi
	 */
	public int getRssi() {
		return mRssi;
	}

    AccessPoint(Context context, WifiConfiguration config) {
    	mContext = context;
        ssid = (config.SSID == null ? "" : removeDoubleQuotes(config.SSID));
        security = getSecurity(config);
        networkId = config.networkId;
        mConfig = config;
        mRssi = Integer.MAX_VALUE;
    }

    public AccessPoint(Context context, ScanResult result) {
    	mContext = context;
        ssid = result.SSID;
        security = getSecurity(result);
        networkId = -1;
        mRssi = result.level;
        mScanResult = result;
        mCapabilites = result.capabilities;
        mBSSID = result.BSSID;
    }

    @Override
    public int compareTo(AccessPoint other ) {

    	// Active one goes first.
        if (mInfo != other.mInfo) {
            return (mInfo != null) ? -1 : 1;
        }
        // Reachable one goes before unreachable one.
        if ((mRssi ^ other.mRssi) < 0) {
            return (mRssi != Integer.MAX_VALUE) ? -1 : 1;
        }
        // Configured one goes before unconfigured one.
        if ((networkId ^ other.networkId) < 0) {
            return (networkId != -1) ? -1 : 1;
        }
        // Sort by signal strength.
        int difference = WifiManager.compareSignalLevel(other.mRssi, mRssi);
        if (difference != 0) {
            return difference;
        }
        // Sort by ssid.
        return ssid.compareToIgnoreCase(other.ssid);
    }

    boolean update(ScanResult result) {
    	//only has the same ssid and the same security
        if (ssid.equals(result.SSID) && security == getSecurity(result)) {
            if (WifiManager.compareSignalLevel(result.level, mRssi) > 0) {
                mRssi = result.level;
            }
            return true;
        }
        return false;
    }

    void update(WifiInfo info, DetailedState state) {
        if (info != null && networkId != -1 && networkId == info.getNetworkId()) {
            mRssi = info.getRssi();
            mInfo = info;
            mState = state;
            refreshSummary();
        } else if (mInfo != null) {
            mInfo = null;
            mState = null;
            refreshSummary();
        }
    }
//
//    public void update(AccessPoint accessPoint) {
//		if (accessPoint != null) {
//			if (accessPoint.mConfig != null) {
//
//		    	mContext = mContext;
//		        ssid = (accessPoint.mConfig.SSID == null ? "" : removeDoubleQuotes(accessPoint.mConfig.SSID));
//		        security = getSecurity(accessPoint.mConfig);
//		        networkId = accessPoint.mConfig.networkId;
//		        mConfig = accessPoint.mConfig;
//		        mRssi = Integer.MAX_VALUE;
//			}else if (accessPoint.mScanResult != null) {
//
//		    	mContext = mContext;
//		        ssid = accessPoint.mScanResult.SSID;
//		        security = getSecurity(accessPoint.mScanResult);
//		        networkId = -1;
//		        mRssi = accessPoint.mScanResult.level;
//		        mScanResult = accessPoint.mScanResult;
//		        mCapabilites = accessPoint.mScanResult.capabilities;
//		        mBSSID = accessPoint.mScanResult.BSSID;
//			}
//		}
//	}

    /**
     * The level is in 0~3
     * @return
     * 			if -1 error
     */
    int getLevel() {
        if (mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(mRssi, 4);
    }

    WifiConfiguration getConfig() {
        return mConfig;
    }

    /**
	 * @return the mScanResult
	 */
	public ScanResult getScanResult() {
		return mScanResult;
	}

	WifiInfo getInfo() {
        return mInfo;
    }

    DetailedState getState() {
        return mState;
    }

    /**
	 * @return the mCapabilite
	 */
	public String getCapabilite() {
		return mCapabilites;
	}

	/**
	 * @return the mBSSID
	 */
	public String getBSSID() {
		return mBSSID;
	}

	/**
     * 去掉两头的双引号
     * @param string
     * @return
     */
    static String removeDoubleQuotes(String string) {
    	if (string == null) {
			return null;
		}
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    /**
     * 给两头加上双引号
     * @param string
     * @return
     */
    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    private void refreshSummary() {

        if (mState != null) {
            setSummary(Summary.get(mContext, mState));
        } else {
            String status = null;
            if (mRssi == Integer.MAX_VALUE) {
                status = mContext.getString(R.string.wifi_not_in_range);
            } else if (mConfig != null) {
                status = mContext.getString((mConfig.status == WifiConfiguration.Status.DISABLED) ?
                        R.string.wifi_disabled : R.string.wifi_remembered);
            }

            if (security == SECURITY_NONE) {
                setSummary(status);
            } else {
                String format = mContext.getString((status == null) ?
                        R.string.wifi_secured : R.string.wifi_secured_with_status);
                String[] type = mContext.getResources().getStringArray(R.array.wifi_security);
                setSummary(String.format(format, type[security], status));
            }
        }
    }

    private void setSummary(String text) {
    	this.mSummary = text;
    }

	/**
	 * It will refresh the sumary and return it
	 * @return the summary
	 * @see #refreshSummary()
	 */
	public String getSummary() {
		refreshSummary();
		return mSummary;
	}

	@Override
	public String toString() {
		return "AccessPoint [ssid=" + ssid + ", security=" + security
				+ ", networkId=" + networkId + ", mRssi=" + mRssi
				+ ", mSummary=" + mSummary + "]";
	}

	public boolean scanedConnectInfoEquals(AccessPoint accessPoint) {

		try {
			if (accessPoint!=null && accessPoint.getScanResult()!=null && mScanResult!=null) {
				if (accessPoint.ssid.trim().equals(ssid.trim()) && accessPoint.mBSSID.trim().equals(mBSSID.trim()) &&
						accessPoint.mCapabilites.trim().equals(mCapabilites.trim())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}

	public int indexInList(List<AccessPoint> accessPoints) {

		if (accessPoints != null) {
			for (int i = 0; i < accessPoints.size(); i++) {
				if (this.scanedConnectInfoEquals(accessPoints.get(i))) {
					return i;
				}
			}
		}

		return -1;
	}

	public synchronized static final boolean accessPointsEquals(List<AccessPoint> one, List<AccessPoint>other) {

		if (one!=null && other!=null && one.size()==other.size()) {
			for (AccessPoint accessPoint : one) {

				int count = 0;
				for (AccessPoint accessPoint2 : other) {
					if (accessPoint.scanedConnectInfoEquals(accessPoint2)) {
						break;
					}
					count ++;
				}

				if (count == other.size()) {
					return false;
				}
			}

			return true;
		}

		return false;
	}
}
