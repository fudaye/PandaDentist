package com.pandadentist.configwifi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Build;
import android.os.StrictMode;
import android.security.KeyStore;
import android.widget.Toast;

import com.pandadentist.configwifi.android.AccessPoint;
import com.pandadentist.configwifi.model.Module;
import com.pandadentist.configwifi.model.NetworkProtocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public final class Utils {

	private static final String ENTER = "\r";
	public static int COMMAND = 1;
	/**
	 * Transparent Transmission
	 */
	public static int TTS = 2;
	public static int RESPONSE_CMD = 3;
	public static int RESPONSE_TTS = 4;
	public static final String PREFERENCES_MODULE_MID = "module_mid";
	public static final String PREFERENCES_SCAN_RESULT_PASSWD = "scan_result";
	public static final String SECURITY_WEP = "wep";
	public static final String SECURITY_OPEN = "OPEN";
	public static final String SECURITY_SHARED = "SHARED";
	public static final String SECURITY_WPAPSK = "WPAPSK";
	public static final String SECURITY_WPA2PSK = "WPA2PSK";
	public static final String SECURITY_NONE = "NONE";
	public static final String SECURITY_WEP_A = "WEP-A";
	public static final String SECURITY_WEP_H = "WEP-H";
	public static final String SECURITY_TKIP = "TKIP";
	public static final String SECURITY_AES = "AES";
	public static final String SECURITY_OPEN_NONE = "open,none";
	public static final String SECURITY_OPEN_WEP_A = "open,wep-a";
	public static final String SECURITY_OPEN_WEP_H = "open,wep-h";
	public static final String SECURITY_SHARED_WEP_A = "shared,wep-a";
	public static final String SECURITY_SHARED_WEP_H = "shared,wep-h";
	public static final String SECURITY_WPAPSK_AES = "wpapsk,aes";
	public static final String SECURITY_WPAPSK_TKIP = "wpapsk,tkip";
	public static final String SECURITY_WPA2PSK_AES = "wpa2psk,aes";
	public static final String SECURITY_WPA2PSK_TKIP = "wpa2psk,tkip";
    private static final String KEYSTORE_SPACE = "keystore://";
	public static final String LAST_SCAN_RESULT_CONNECTED = "last_scan_result_connected";
    public static final int WEP_ASCII = 1;
    public static final int WEP_HEX = 2;
    public static final int WEP_INVALID = -1;

	public static String gernerateCMD(String text) {

		if (text == null) {
			return null;
		}

		return text + ENTER;
	}

	/**
	 * @param type
	 * @param text
	 * @return
	 */
	public synchronized static String gernerateEchoText(int type, String text) {

		if (type == COMMAND) {

			if (text == null) {
				return ">\n";
			}else {
				return ">" + text + "\n";
			}
		}else if (type == TTS) {

			if (text == null) {
				return ">\n";
			}else {
				return ">" + text + "\n";
			}
		}else if (type == RESPONSE_CMD) {
			if (text == null) {
				return "\n";
			}else {
				return " " + text;
			}
		}else if (type == RESPONSE_TTS) {
			if (text == null) {
				return "";
			}else {
				return text;
			}
		}else {
			if (text == null) {
				return "";
			}else {
				return text;
			}
		}
	}

	public synchronized static Module decodeBroadcast2Module(String response) {

		if (response == null) {
			return null;
		}

		String[] array = response.split(",");
		if (array==null || (array.length<2 && array.length>3) ||
				!isIP(array[0]) || !isMAC(array[1])) {
			return null;
		}

		Module module = new Module();
		module.setIp(array[0]);
		module.setMac(array[1]);
		if (array.length == 3) {
			module.setModuleID(array[2]);
		}

		return module;
	}

	public static String appendCharacters(String oldStr, String append, int count) {
		if ((oldStr==null && append==null) || count<0) {
			return null;
		}

		if (count == 0) {
			return new String(oldStr);
		}

		StringBuffer sb;

		if (oldStr == null) {
			sb = new StringBuffer();
		}else {
			sb = new StringBuffer(oldStr);
		}
		for (int i = 0; i < count; i++) {
			sb.append(append);
		}

		return sb.toString();
	}

	public static int getUdpPort(Context context) {

		String port = context.getSharedPreferences(
				context.getPackageName() + "_preferences", Context.MODE_PRIVATE)
				.getString(Constants.KEY_UDP_PORT, Constants.UDP_PORT + "");
		try {
			return Integer.valueOf(port);
		} catch (Exception e) {
			return Constants.UDP_PORT;
		}
	}

	public static String getCMDScanModules(Context context) {

		return context.getSharedPreferences(
						context.getPackageName() + "_preferences", Context.MODE_PRIVATE)
					.getString(Constants.KEY_CMD_SCAN_MODULES, Constants.CMD_SCAN_MODULES);
	}

	public static void toast(Context context, int resId) {
		Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
	}

	public static void toast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static boolean isIP(String str) {
		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])" +
				"\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
				"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\." +
				"((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		return pattern.matcher(str).matches();
	}

	public static boolean isMAC(String str) {

		str = str.trim();
		if (str.length() != 12) {
			return false;
		}

		char[] chars = new char[12];
		str.getChars(0, 12, chars, 0);
		for (int i = 0; i < chars.length; i++) {
			if (!((chars[i]>='0' && chars[i]<='9') || (chars[i]>='A' && chars[i]<='F') || (chars[i]>='a' && chars[i]<='f'))) {
				return false;
			}
		}
		return true;
	}

	public synchronized static NetworkProtocol decodeProtocol(String response) {

		if (response == null) {
			return null;
		}

		String[] array = response.split(",");
		if (array == null) {
			return null;
		}

		//look for ip and port
		int index = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals("TCP") || array[i].equals("UDP")) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			return null;
		}

		try {
			if (!isIP(array[index+3])) {
				return null;
			}

			NetworkProtocol protocol = new NetworkProtocol();
			protocol.setProtocol(array[0]);
			protocol.setServer(array[1]);
			protocol.setPort(Integer.valueOf(array[index+2]));
			protocol.setIp(array[index+3]);

			return protocol;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static WifiConfiguration generateWifiConfiguration(AccessPoint mAccessPoint, String mPassword) {
        if (mAccessPoint != null && mAccessPoint.getNetworkId() != -1) {
            return null;
        }

        WifiConfiguration config = new WifiConfiguration();

        if (mAccessPoint == null) {
//            config.SSID = AccessPoint.convertToQuotedString(
//                    mSsid.getText().toString());
            // If the user adds a network manually, assume that it is hidden.
            config.hiddenSSID = true;
        } else if (mAccessPoint.getNetworkId() == -1) {
            config.SSID = AccessPoint.convertToQuotedString(
                    mAccessPoint.getSsid());
        } else {
            config.networkId = mAccessPoint.getNetworkId();
        }

        switch (mAccessPoint.getSecurity()) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                return config;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPassword.length() != 0) {
                    int length = mPassword.length();
                    String password = mPassword;//.getText().toString();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58) &&
                            password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                return config;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPassword.length() != 0) {
                    String password = mPassword;//.getText().toString();
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        config.preSharedKey = '"' + password + '"';
                    }
                }
                return config;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
//                config.eap.setValue((String) mEapMethod.getSelectedItem());
//
//                config.phase2.setValue((mPhase2.getSelectedItemPosition() == 0) ? "" :
//                        "auth=" + mPhase2.getSelectedItem());
//                config.ca_cert.setValue((mEapCaCert.getSelectedItemPosition() == 0) ? "" :
//                        KEYSTORE_SPACE + Credentials.CA_CERTIFICATE +
//                        (String) mEapCaCert.getSelectedItem());
//                config.client_cert.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
//                        KEYSTORE_SPACE + Credentials.USER_CERTIFICATE +
//                        (String) mEapUserCert.getSelectedItem());
//                config.private_key.setValue((mEapUserCert.getSelectedItemPosition() == 0) ? "" :
//                        KEYSTORE_SPACE + Credentials.USER_PRIVATE_KEY +
//                        (String) mEapUserCert.getSelectedItem());
//                config.identity.setValue((mEapIdentity.length() == 0) ? "" :
//                        mEapIdentity.getText().toString());
//                config.anonymous_identity.setValue((mEapAnonymous.length() == 0) ? "" :
//                        mEapAnonymous.getText().toString());
//                if (mPassword.length() != 0) {
//                    config.password.setValue(mPassword);//.getText().toString());
//                }
                return config;
        }
        return null;
    }

	public static String generateTry2ConnectCmd(String ssid, String security, String password) {
		return String.format(Constants.CMD_WSTRY, ssid + "," + security + "," + password);
	}

	public static String generateTry2ConnectCmd(String ssid, String security) {
		return String.format(Constants.CMD_WSTRY, ssid + "," + security);
	}

	public static String generateWskeyCmd(String auth, String encry, String password) {
		return String.format(Constants.CMD_WSKEY, auth + "," + encry + "," + password);
	}

	public static String generateWskeyCmd(String auth, String encry) {
		return String.format(Constants.CMD_WSKEY, auth + "," + encry);
	}

	public static String generateWsssid(String ssid) {
		return String.format(Constants.CMD_WSSSID, ssid);
	}

	/**
	 * decode pagkets to mudoles
	 * @param packets
	 * @return
	 */
	public static List<Module> decodePackets(Context context, List<DatagramPacket> packets) {

		int i = 1;
		Module module;
		List<String> list = new ArrayList<String>();
		List<Module> modules = new ArrayList<Module>();

		DECODE_PACKETS:
		for (DatagramPacket packet : packets) {

			String data = new String(packet.getData(), 0, packet.getLength());
			if (data.equals(Utils.getCMDScanModules(context))) {
				continue;
			}

			for (String item : list) {
				if (item.equals(data)) {
					continue DECODE_PACKETS;
				}
			}

			list.add(data);
			if ((module = Utils.decodeBroadcast2Module(data)) != null) {
				module.setId(i);
				modules.add(module);
				i++;
			}
		}

		return modules;
	}



	/**
	 * save modules' data to local
	 * @param modules
	 */
	public static void saveDevices(Context context, List<Module> modules) {

		SharedPreferences preferences = context.getSharedPreferences("module_list", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		if (modules.size() > 0) {
			int i = 0;
			for (Module module : modules) {
				editor.putInt(Constants.KEY_PRE_ID + i, module.getId());
				editor.putString(Constants.KEY_PRE_IP + i, module.getIp());
				editor.putString(Constants.KEY_PRE_MAC + i, module.getMac());
				editor.putString(Constants.KEY_PRE_MODULEID + i, module.getModuleID());
				i++;
			}

			editor.putInt(Constants.KEY_MODULE_COUNT, modules.size());
			editor.commit();
		}else {
			editor.clear().commit();
		}
	}

	/**
	 * save module with specific key
	 */
	public static void saveDevice(Context context, String key, Module module) {

		if (module == null) {
			return;
		}

		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_MODULE_MID, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();

		editor.putInt(Constants.KEY_PRE_ID + key, module.getId());
		editor.putString(Constants.KEY_PRE_IP + key, module.getIp());
		editor.putString(Constants.KEY_PRE_MAC + key, module.getMac());
		editor.putString(Constants.KEY_PRE_MODULEID + key, module.getModuleID());
		editor.commit();
	}

	/**
	 * Load modules' data from local
	 * @return
	 */
	public static List<Module> loadDevices(Context context) {

		List<Module> modules = new ArrayList<Module>();
		SharedPreferences preferences = context.getSharedPreferences("module_list", Context.MODE_PRIVATE);
		int count = preferences.getInt(Constants.KEY_MODULE_COUNT, 0);
		Module module;

		for (int i = 0; i < count; i++) {
			module = new Module();
			module.setId(preferences.getInt(Constants.KEY_PRE_ID + i, -1));
			module.setIp(preferences.getString(Constants.KEY_PRE_IP + i, null));
			module.setMac(preferences.getString(Constants.KEY_PRE_MAC + i, null));
			module.setModuleID(preferences.getString(Constants.KEY_PRE_MODULEID + i, null));
			modules.add(module);
		}

		return modules;
	}

	/**
	 * Get the modules by key from local
	 * @return
	 */
	public static Module getDevice(Context context, String key) {

		SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_MODULE_MID, Context.MODE_PRIVATE);
		int id = preferences.getInt(Constants.KEY_PRE_ID + key, Integer.MIN_VALUE);
		if (id == Integer.MIN_VALUE) {
			return null;
		}

		Module module = new Module();
		module.setId(id);
		module.setIp(preferences.getString(Constants.KEY_PRE_IP + key, null));
		module.setMac(preferences.getString(Constants.KEY_PRE_MAC + key, null));
		module.setModuleID(preferences.getString(Constants.KEY_PRE_MODULEID + key, null));

		return module;
	}

	public static Module findModule(List<Module>modules, String key) {
		for (Module module : modules) {
			if (module.getModuleID().equals(key)) {
				return module;
			}
		}

		return null;
	}

	/**
	 * Get the ssid to the default connect
	 * @param context
	 * @return
	 */
	public static String getSettingApSSID(Context context) {

    	return context.getSharedPreferences(
    			context.getPackageName() + Constants.SHARED_PREFERENCES,
    			Context.MODE_PRIVATE)
    			.getString(Constants.KEY_SSID, Constants.DEFAULT_SSID).trim();
    }

	/**
	 * Get the password of the default ap
	 * @param context
	 * @return
	 */
	public static String getSettingApPassword(Context context) {

    	return context.getSharedPreferences(
    			context.getPackageName() + Constants.SHARED_PREFERENCES,
    			Context.MODE_PRIVATE)
    			.getString(Constants.KEY_PASSWORD, "").trim();
    }

	/**
	 * Check the wep type by password
	 * @param password
	 * @return {@link #WEP_INVALID}, {@link #WEP_ASCII}, {@link #WEP_HEX}
	 */
	public static int checkWepType(String password) {
		if (password == null) {
			return WEP_INVALID;
		}

		password = password.trim();
		int length = password.length();
		if (length==5 || length==13) {
			return WEP_ASCII;
		}else if ((length==10 && password.matches("[0-9A-Fa-f]{10}")) || (length==26 && password.matches("[0-9A-Fa-f]{26}"))) {
			return WEP_HEX;
		}else {
			return WEP_INVALID;
		}
	}

	/**
	 * Generate a key for a scan result, the format is : ssid-key_ssid|bssid-bssid|capabilities-capabilities
	 * @param scanResult
	 * @return
	 */
	public synchronized static final String generateScanResultKey(ScanResult scanResult) {
		if (scanResult == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		if (scanResult.SSID != null) {
			sb.append("ssid:");
			sb.append(scanResult.SSID);
			sb.append("[MaGiCsTrInG]");
		}
		if (scanResult.BSSID != null) {
			sb.append("bssid:");
			sb.append(scanResult.BSSID);
			sb.append("[MaGiCsTrInG]");
		}
		if (scanResult.capabilities != null) {
			sb.append("capabilities:");
			sb.append(scanResult.capabilities);
			sb.append("[MaGiCsTrInG]");
		}
		return sb.toString();
	}

	/**
	 * parse a key generated by {@link #generateScanResultKey(ScanResult)} to a scan result, the key's format is : ssid-key_ssid|bssid-bssid|capabilities-capabilities
	 * @param key
	 * @return
	 * @see #generateScanResultKey(ScanResult)
	 */
	public synchronized static final ScanResult parseScanResult(String key) {
		if (key == null) {
			return null;
		}

		key = key.trim();
		String[] array = key.split("\\[MaGiCsTrInG]");
		if (array != null) {
			String ssid = null;
			String bssid = null;
			String caps = null;

			for (String item : array) {
				if (item.startsWith("ssid:")) {
					ssid = item.substring("ssid:".length());
				}else if (item.startsWith("bssid:")) {
					bssid = item.substring("bssid:".length());
				}else if (item.startsWith("capabilities:")) {
					caps = item.substring("capabilities:".length());
				}
			}

			if (ssid!=null && bssid!=null && caps!=null) {

				//if the android version is lower than 4.2
				if (Build.VERSION.SDK_INT < 17) {

					try {
						Constructor<ScanResult> constructor = ScanResult.class.getConstructor(String.class, String.class, String.class, int.class, int.class);
						return  constructor.newInstance(ssid, bssid, caps, 0, 0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					try {
						Class<?> WifiSsid = Class.forName("android.net.wifi.WifiSsid");
						Constructor<ScanResult> constructor = ScanResult.class.getConstructor(WifiSsid, String.class, String.class, int.class, int.class, long.class);
						Method method = WifiSsid.getMethod("createFromAsciiEncoded", String.class);
						return  constructor.newInstance(method.invoke(null, ssid), bssid, caps, 0, 0, System.currentTimeMillis());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	/**
	 * Get the key from local file, the key is user input before
	 * @param context
	 * @param scanResult
	 * @return
	 */
	public synchronized static final String getScanResultPassword(Context context, ScanResult scanResult) {

		if (scanResult == null) {
			return null;
		}

		String key = generateScanResultKey(scanResult);
		return context.getSharedPreferences(PREFERENCES_SCAN_RESULT_PASSWD, Context.MODE_PRIVATE)
				.getString(key, null);
	}

	/**
	 * Save the password of scanResult into a local file
	 * @param context
	 * @param scanResult
	 * @param password
	 */
	public synchronized static final void  saveScanResultPassword(Context context, ScanResult scanResult, String password) {
		if (scanResult == null || password == null) {
			return;
		}

		password = password.trim();
		context.getSharedPreferences(PREFERENCES_SCAN_RESULT_PASSWD, Context.MODE_PRIVATE)
		.edit().putString(generateScanResultKey(scanResult), password).commit();
	}

	public synchronized static final void saveLastScanResult(Context context, ScanResult scanResult) {
		if (scanResult != null) {
			context.getSharedPreferences(PREFERENCES_SCAN_RESULT_PASSWD, Context.MODE_PRIVATE)
			.edit().putString(LAST_SCAN_RESULT_CONNECTED, generateScanResultKey(scanResult)).commit();
		}
	}

	public synchronized static final ScanResult getLastScanResult(Context context) {
		String lastConnected = context.getSharedPreferences(PREFERENCES_SCAN_RESULT_PASSWD, Context.MODE_PRIVATE)
				.getString(LAST_SCAN_RESULT_CONNECTED, null);
		if (lastConnected == null) {
			return null;
		}else {
			return parseScanResult(lastConnected);
		}
	}

	public static String generateTry2ConnectCmd(AccessPoint accessPoint, String password) {

		String cmd = null;
		String security = parseSecurity(accessPoint.getScanResult().capabilities);
		if (accessPoint.getSecurity() == AccessPoint.SECURITY_NONE || SECURITY_OPEN_NONE.equals(security)) {
			cmd = Utils.generateTry2ConnectCmd(accessPoint.getSsid(), SECURITY_OPEN_NONE);
		}else {

			if (SECURITY_WEP.equals(security)) {
				security = checkWepType(password) == WEP_ASCII ? SECURITY_SHARED_WEP_A : SECURITY_SHARED_WEP_H;
			}
			cmd = Utils.generateTry2ConnectCmd(accessPoint.getSsid(), security, password);
		}

		return cmd;
	}

	public static String generateWskeyCmd(ScanResult scanResult, String password) {

		String cmd = null;
		String security = parseSecurity(scanResult.capabilities);
		if (SECURITY_OPEN_NONE.equals(security)) {
			cmd = Utils.generateWskeyCmd(SECURITY_OPEN, SECURITY_NONE);
		}else {

			if (SECURITY_WEP.equals(security)) {

				if (checkWepType(password) == WEP_ASCII) {
					cmd = Utils.generateWskeyCmd(SECURITY_SHARED, SECURITY_WEP_A, password);
				}else {
					cmd = Utils.generateWskeyCmd(SECURITY_SHARED, SECURITY_WEP_H, password);
				}
			}else if (SECURITY_WPA2PSK_AES.equals(security)) {
				cmd = Utils.generateWskeyCmd(SECURITY_WPA2PSK, SECURITY_AES, password);
			}else if (SECURITY_WPA2PSK_TKIP.equals(security)) {
				cmd = Utils.generateWskeyCmd(SECURITY_WPA2PSK, SECURITY_TKIP, password);
			}else if (SECURITY_WPAPSK_AES.equals(security)) {
				cmd = Utils.generateWskeyCmd(SECURITY_WPAPSK, SECURITY_AES, password);
			}else if (SECURITY_WPAPSK_TKIP.equals(security)) {
				cmd = Utils.generateWskeyCmd(SECURITY_WPAPSK, SECURITY_TKIP, password);
			}
		}

		return cmd;
	}

	public synchronized static final String parseSecurity(String capabilities) {

		if (capabilities == null) {
			return null;
		}

		capabilities = capabilities.replace("][", ";").replace("[", "").replace("]", "");
		System.out.println("capabilities: " + capabilities);

		if (capabilities.contains("WEP")) {
			return SECURITY_WEP;
		}

		int wpa = -1;
		int wpa2 = -1;
		String[] caps = capabilities.split(";");
		for (int i = 0; i < caps.length; i++) {
			if (caps[i].contains("WPA2") && caps[i].contains("PSK")) {
				wpa2 = i;
			}else if (caps[i].contains("WPA") && caps[i].contains("PSK")) {
				wpa = i;
			}
		}

		if (wpa2 != -1) {

			if (caps[wpa2].contains("CCMP")) {
				return SECURITY_WPA2PSK_AES;
			}
			if (caps[wpa2].contains("TKIP")) {
				return SECURITY_WPA2PSK_TKIP;
			}
		}

		if (wpa != -1) {

			if (caps[wpa].contains("CCMP")) {
				return SECURITY_WPAPSK_AES;
			}
			if (caps[wpa].contains("TKIP")) {
				return SECURITY_WPAPSK_TKIP;
			}
		}

		return SECURITY_OPEN_NONE;
	}

    /**
     * as the api method requireKeyStore for android has changed in android 4.2, so it use reflect to avoid this change
     * <pre/>
     * <b>android 2.x -- WifiDialog.requireKeyStore</b>
	    static boolean requireKeyStore(WifiConfiguration config) {
	        String values[] = {config.ca_cert.value(), config.client_cert.value(),
	                config.private_key.value()};
	        for (String value : values) {
	            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
	                return true;
	            }
	        }
	        return false;
	    }

     * <b>android 4.x -- WifiConfigController.requireKeyStore</b>
	    static boolean requireKeyStore(WifiConfiguration config) {
	        if (config == null) {
	            return false;
	        }
	        if (!TextUtils.isEmpty(config.key_id.value())) {
	            return true;
	        }
	        String values[] = { config.ca_cert.value(), config.client_cert.value() };
	        for (String value : values) {
	            if (value != null && value.startsWith(KEYSTORE_SPACE)) {
	                return true;
	            }
	        }
	        return false;
	    }
     * </pre>
     * @return
     */
    public synchronized static boolean requireKeyStore(WifiConfiguration config) {

    	if (config == null) {
			return false;
		}

    	Class<?> clazz = WifiConfiguration.class;
    	Field field = null;
    	int find = 0;
    	try {
    		field = clazz.getField("private_key");
    		find = 1;
    		System.out.println("Find field private_key in WifiConfiguration class");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

    	if (find == 0) {

        	try {
        		field = clazz.getField("key_id");
        		find = 2;
        		System.out.println("Find field key_id in WifiConfiguration class");
    		} catch (NoSuchFieldException e) {
    			e.printStackTrace();
    		}
		}

    	if (find == 0) {
			System.out.println("Not find field private_key or key_id in WifiConfiguration class");
			return false;
		}else {

//			String value = null;
//			clazz = EnterpriseField.class;
//			try {
//				Method method = clazz.getMethod("value");
//				Object result = method.invoke(field.get(config));
//				value = (result==null) ? null : (String)result;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			if (find == 2) {
//				if (!TextUtils.isEmpty(value)) {
//		            return true;
//		        }
//			}
//
//			String values[] = null;
//			if (find == 1) {
//				values = new String[]{config.ca_cert.value(), config.client_cert.value(), value};
//			}else {
//				values = new String[]{config.ca_cert.value(), config.client_cert.value()};
//			}
//
//	        for (String _value : values) {
//	            if (_value != null && _value.startsWith(KEYSTORE_SPACE)) {
//	                return true;
//	            }
//	        }
	        return true;
		}
    }


    /**
     * as the api method test for android has changed in android 4.2, so it use reflect to avoid this change
     * for android 2.x: KeyStore.getInstance().test() == KeyStore.NO_ERROR
     * for android 4.2: KeyStore.getInstance().state() == KeyStore.State.UNLOCKED
     * @return
     */
    public synchronized static boolean testKeyStoreNoError() {

    	Class<KeyStore> clazz = KeyStore.class;
    	Method testMethod = null;
    	int find = 0;
    	try {
			testMethod = clazz.getMethod("test");
			find = 1;
			System.out.println("Find method test in KeyStore class");
    	} catch (NoSuchMethodException e) {
    		System.err.println("Not find method test in KeyStore class");
			e.printStackTrace();
		}

    	if (find == 0) {

        	try {
    			testMethod = clazz.getMethod("state");
    			find = 2;
    			System.out.println("Find method state in KeyStore class");
        	} catch (NoSuchMethodException e) {
    			e.printStackTrace();
    		}
		}

    	if (find == 0) {
    		System.out.println("Not find method state or test in KeyStore class");
    		return false;
		}else if (find == 1) {
			try {
				Object result = testMethod.invoke(KeyStore.getInstance());
				System.out.println("result is " + result.toString());
				return ((Integer)result).intValue() == KeyStore.NO_ERROR;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {

			try {
				Object result = testMethod.invoke(KeyStore.getInstance());
				System.out.println("result is " + result.toString());
				return result.toString().equals("UNLOCKED");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

    	return false;
    }

	/**
	 * 去掉两头的双引号
	 * @param string
	 * @return
	 */
	public synchronized static String removeDoubleQuotes(String string) {
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
	public synchronized static String convertToQuotedString(String string) {
	    return "\"" + string + "\"";
	}
	
	public synchronized static int getAndroidInternalId(String id) {
		try {
			return Class.forName("com.android.internal.R$id").getField(id).getInt(null);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static void forceStrictMode() {
		
		if (Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
	}
}
