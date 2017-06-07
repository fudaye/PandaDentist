package com.pandadentist.configwifi.model;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pandadentist.configwifi.net.UdpUnicast;
import com.pandadentist.configwifi.utils.Constants;
import com.pandadentist.configwifi.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class ATCommand {

	private static final String TAG = "ATCommand";
	private static final String RESPONSE = "RESPONSE";

	private static final int CODE_ENTER_CMD_MODE_SUCCESS = 1;
	private static final int CODE_ENTER_CMD_MODE_FAILURE = 2;
	private static final int CODE_EXIT_CMD_MODE_SUCCESS = 3;
	private static final int CODE_EXIT_CMD_MODE_FAILURE = 4;
	private static final int CODE_RELOAD_SUCCESS = 5;
	private static final int CODE_RELOAD_FAILURE = 6;
	private static final int CODE_RESET_SUCCESS = 7;
	private static final int CODE_RESET_FAILURE = 8;
	private static final int CODE_CMD = 9;
	private static final int CODE_SEND_CMD_FILE_SUCCESS = 10;
	private static final int CODE_SEND_CMD_FILE_FAILURE = 11;
	private static final int CODE_SEND_CMD_FILE_RESPONSE = 12;

	private ATCommandListener listener;
	private UdpUnicast udpUnicast;
	private Handler handler;
	private boolean isCommonCMD;

//	private String enterCMDModeResponse;
	private StringBuffer enterCMDModeResponse = new StringBuffer();
	private String exitCMDModeResponse;
	private String sendCMDFileResponse;
	private String reloadResponse;
	private String resetResponse;
	private StringBuffer tryEnterCMDModeResponse = new StringBuffer();
//	private String tryEnterCMDModeResponse;
	private String response;
	private int timesToTry;
	private int times;
	private NetworkProtocol protocol;

	public ATCommand() {
		super();

		timesToTry = 2;
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case CODE_ENTER_CMD_MODE_SUCCESS:
					onEnterCMDMode(true);
					break;
				case CODE_ENTER_CMD_MODE_FAILURE:
					onEnterCMDMode(false);
					break;
				case CODE_EXIT_CMD_MODE_SUCCESS:
					onExitCMDMode(true, protocol);
					break;
				case CODE_EXIT_CMD_MODE_FAILURE:
					onExitCMDMode(false, null);
					break;
				case CODE_RELOAD_SUCCESS:
					onReload(true);
					break;
				case CODE_RELOAD_FAILURE:
					onReload(false);
					break;
				case CODE_RESET_SUCCESS:
					onReset(true);
					break;
				case CODE_RESET_FAILURE:
					onReset(false);
					break;
				case CODE_CMD:
					onResponse(msg.getData().getString(RESPONSE));
					break;
				case CODE_SEND_CMD_FILE_SUCCESS:
					onSendFile(true);
					break;
				case CODE_SEND_CMD_FILE_FAILURE:
					onSendFile(false);
					break;
				case CODE_SEND_CMD_FILE_RESPONSE:
					onResponseOfSendFile(msg.getData().getString(RESPONSE));
					break;
				default:
					break;
				}
			}
		};
	}

	public ATCommand(UdpUnicast udpUnicast) {
		this();
		this.udpUnicast = udpUnicast;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(ATCommandListener listener) {
		this.listener = listener;
	}

	/**
	 * @param udpUnicast the udpUnicast to set
	 */
	public void setUdpUnicast(UdpUnicast udpUnicast) {
		this.udpUnicast = udpUnicast;
	}

	/**
	 * send a common command
	 * @param cmd
	 */
	public void send(String cmd) {
		if (!isCommonCMD) {
			udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

				@Override
				public void onReceived(byte[] data, int length) {

					Log.d(TAG, "onReceived[send]:" + new String(data, 0, length));

					Message msg = handler.obtainMessage(CODE_CMD);
					Bundle bundle = new Bundle();
					bundle.putString(RESPONSE, new String(data, 0, length));
					msg.setData(bundle);
					handler.sendMessage(msg);
				}
			});
			isCommonCMD = true;
		}
		udpUnicast.send(cmd);
	}

	/**
	 * Send a commmand file to device
	 * @param file
	 */
	public void sendFile(final File file) {

		times++;
		isCommonCMD = false;
		sendCMDFileResponse = null;

		//send a test cmd to verify the module is in cmd mode
		udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

			@Override
			public void onReceived(byte[] data, int length) {
				response = new String(data, 0, length);
				sendCMDFileResponse = response.trim();
			}
		});
		if (!udpUnicast.send(Constants.CMD_TEST)) {
			handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
			return;
		}

		waitReceiveResponse(3500, sendCMDFileResponse);

		Log.d(TAG, "Response of No." + times + " times to test cmd mode:" + sendCMDFileResponse);

		if (sendCMDFileResponse == null) {
			//if there's no response, set the module enter cmd mode

			if (times < timesToTry) {

				//try to enter cmd mode
				new CMDModeTryer() {

					@Override
					void onResult(boolean success) {
						if (success) {
							//try send again
							sendFile(file);
						}else {
							handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
						}
					}
				}.toTry(true);
			}else {
				handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
			}
		}else if (sendCMDFileResponse.equals(Constants.RESPONSE_OK)) {
			//if it's in cmd mode, start a thread to send file

			new Thread(new Runnable() {

				@Override
				public void run() {

					try {

						BufferedReader reader = new BufferedReader(new FileReader(file));
						boolean success = true;
						String cmd = null;
						while ((cmd=reader.readLine()) != null) {

							cmd = cmd.trim();
							sendCMDFileResponse = null;
							Log.d(TAG, "send cmd:" + cmd);
							routeResponse(">" + cmd +"\n");
							if (!udpUnicast.send(Utils.gernerateCMD(cmd))) {
								Log.w(TAG, "Send cmd fail!");
								handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
								success = false;
								break;
							}else {
								waitReceiveResponse(6000, sendCMDFileResponse);

								Log.d(TAG, "Response of cmd[" + cmd + "]:" + sendCMDFileResponse);

								if (sendCMDFileResponse != null) {
									routeResponse(response);
								}

								if (sendCMDFileResponse == null || !sendCMDFileResponse.startsWith(Constants.RESPONSE_OK)) {
									handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
									success = false;
									break;
								}
							}
						}

						if (success) {
							handler.sendEmptyMessage(CODE_SEND_CMD_FILE_SUCCESS);
						}
					} catch (Exception e) {
						e.printStackTrace();
						handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
					}
				}
			}).start();
		}else {
			//if the response means it error
			handler.sendEmptyMessage(CODE_SEND_CMD_FILE_FAILURE);
		}

	}

	/**
	 * enter to command mode
	 */
	public void enterCMDMode() {

		isCommonCMD = false;
		enterCMDModeResponse.setLength(0);// = null;

		//send a test cmd to verify the module is in cmd mode
		udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

			@Override
			public void onReceived(byte[] data, int length) {
				synchronized (enterCMDModeResponse) {
					enterCMDModeResponse.append(new String(data, 0, length).trim());
					enterCMDModeResponse.notify();
				}
//				enterCMDModeResponse = new String(data, 0, length).trim();
			}
		});

		synchronized (enterCMDModeResponse) {
			if (!udpUnicast.send(Constants.CMD_TEST)) {
				handler.sendEmptyMessage(CODE_ENTER_CMD_MODE_FAILURE);
				return;
			}
			try {
				enterCMDModeResponse.wait(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		waitReceiveResponse(3500, enterCMDModeResponse);

		Log.d(TAG, "Response of test cmd mode:" + enterCMDModeResponse);

		if (enterCMDModeResponse.length() == 0) {
			//if there's no response, set the module enter cmd mode
			//try to enter cmd mode
			new CMDModeTryer() {

				@Override
				void onResult(boolean success) {
					if (success) {
						handler.sendEmptyMessage(CODE_ENTER_CMD_MODE_SUCCESS);
					}else {
						handler.sendEmptyMessage(CODE_ENTER_CMD_MODE_FAILURE);
					}
				}
			}.toTry(true);
		}else{
			handler.sendEmptyMessage(CODE_ENTER_CMD_MODE_SUCCESS);
		}
	}

	/**
	 * exit from command mode
	 */
	public void exitCMDMode() {

		times++;
		isCommonCMD = false;
		exitCMDModeResponse = null;

		//send a test cmd to verify the module is in cmd mode
		udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

			@Override
			public void onReceived(byte[] data, int length) {
				exitCMDModeResponse = new String(data, 0, length).trim();
			}
		});
		if (!udpUnicast.send(Constants.CMD_NETWORK_PROTOCOL)) {
			handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
			return;
		}

		waitReceiveResponse(4000, exitCMDModeResponse);

		Log.d(TAG, "Response of get protocol info:" + exitCMDModeResponse);

		if (exitCMDModeResponse == null) {
			//if there's no response, try again
			if (times < timesToTry) {

				exitCMDMode();
			}else {
				handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
			}
		}else if (exitCMDModeResponse.startsWith(Constants.RESPONSE_OK_OPTION)) {
			/**if the response start with "+ok="**/

			exitCMDModeResponse = exitCMDModeResponse.substring(4);
			protocol = Utils.decodeProtocol(exitCMDModeResponse);
			if (protocol == null) {
				handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
				return;
			}

			/**try to set device into transparent transmission mode**/
			exitCMDModeResponse = null;
			if (udpUnicast.send(Constants.CMD_TRANSPARENT_TRANSMISSION)) {

				waitReceiveResponse(5000, exitCMDModeResponse);

				Log.d(TAG, "Response of set transparent transmission mode:" + exitCMDModeResponse);

				if (exitCMDModeResponse == null || !exitCMDModeResponse.equals(Constants.RESPONSE_OK)) {
					handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
				}else if (exitCMDModeResponse.equals(Constants.RESPONSE_OK)) {
					//set device into transparent transmission mode success
					if (udpUnicast.send(Constants.CMD_EXIT_CMD_MODE)) {
						handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_SUCCESS);
					}else {
						handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
					}
				}
			}else {
				handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
			}
		}else {
			//if the response means it error
			handler.sendEmptyMessage(CODE_EXIT_CMD_MODE_FAILURE);
		}
	}

	/**
	 * reload module to reset settings
	 */
	public void reload() {

		times++;
		isCommonCMD = false;
		reloadResponse = null;

		//send a test cmd to verify the module is in cmd mode
		udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

			@Override
			public void onReceived(byte[] data, int length) {
				reloadResponse = new String(data, 0, length).trim();
			}
		});
		if (!udpUnicast.send(Constants.CMD_TEST)) {
			handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
			return;
		}

		waitReceiveResponse(3500, reloadResponse);

		Log.d(TAG, "Response of No." + times + " times to test cmd mode:" + reloadResponse);

		if (reloadResponse == null) {
			//if there's no response, set the module enter cmd mode

			if (times < timesToTry) {

				//try to enter cmd mode
				new CMDModeTryer() {

					@Override
					void onResult(boolean success) {
						if (success) {
							//try reload again
							reload();
						}else {
							handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
						}
					}
				}.toTry(true);
			}else {
				handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
			}
		}else if (reloadResponse.equals(Constants.RESPONSE_OK)) {
			//if it's in cmd mode, send reload command

			reloadResponse = null;
			if (udpUnicast.send(Constants.CMD_RELOAD)) {

				waitReceiveResponse(10000, reloadResponse);

				Log.d(TAG, "Response of reload cmd:" + reloadResponse);

				if (reloadResponse == null || !reloadResponse.startsWith(Constants.RESPONSE_REBOOT_OK)) {
					handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
				}else if (reloadResponse.startsWith(Constants.RESPONSE_REBOOT_OK)) {
					handler.sendEmptyMessage(CODE_RELOAD_SUCCESS);
				}
			}else {
				handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
			}
		}else {
			//if the response means it error
			handler.sendEmptyMessage(CODE_RELOAD_FAILURE);
		}
	}

	/**
	 * restart module
	 */
	public synchronized void reset() {

		times++;
		isCommonCMD = false;
		resetResponse = null;

		udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

			@Override
			public void onReceived(byte[] data, int length) {
				resetResponse = new String(data, 0, length).trim();
			}
		});
		if (!udpUnicast.send(Constants.CMD_TEST)) {
			handler.sendEmptyMessage(CODE_RESET_FAILURE);
			return;
		}

		waitReceiveResponse(3500, resetResponse);

		Log.d(TAG, "Response of No." + times + " times to test cmd mode:" + resetResponse);

		if (resetResponse == null) {

			if (times < timesToTry) {

				//try to enter cmd mode
				new CMDModeTryer() {

					@Override
					void onResult(boolean success) {
						if (success) {
							reset();
						}else {
							handler.sendEmptyMessage(CODE_RESET_FAILURE);
						}
					}
				}.toTry(true);
			}else {
				handler.sendEmptyMessage(CODE_RESET_FAILURE);
			}
		}else if (resetResponse.equals(Constants.RESPONSE_OK)) {
			if (udpUnicast.send(Constants.CMD_RESET)) {
				handler.sendEmptyMessage(CODE_RESET_SUCCESS);
			}else {
				handler.sendEmptyMessage(CODE_RESET_FAILURE);
			}
		}else {
			handler.sendEmptyMessage(CODE_RESET_FAILURE);
		}
	}

	private void onEnterCMDMode(boolean success) {
		if (listener != null) {
			listener.onEnterCMDMode(success);
		}
	}

	private void onExitCMDMode(boolean success, NetworkProtocol protocol) {
		if (listener != null) {
			listener.onExitCMDMode(success, protocol);
		}
	}

	private void onReload(boolean success) {
		if (listener != null) {
			listener.onReload(success);
		}
	}

	private void onReset(boolean success) {
		if (listener != null) {
			listener.onReset(success);
		}
	}

	private void onResponse(String response) {
		if (listener != null) {
			listener.onResponse(response);
		}
	}

	private void onSendFile(boolean success) {
		if (listener != null) {
			listener.onSendFile(success);
		}
	}

	private void onResponseOfSendFile(String response) {
		if (listener != null) {
			listener.onResponseOfSendFile(response);
		}
	}

	private abstract class CMDModeTryer {

		void toTry(boolean enter) {

			tryEnterCMDModeResponse.setLength(0);
//			tryEnterCMDModeResponse = null;

			if (enter) {

				udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {

					@Override
					public void onReceived(byte[] data, int length) {

//						tryEnterCMDModeResponse = new String(data, 0, length);
						synchronized (tryEnterCMDModeResponse) {
							tryEnterCMDModeResponse.append(new String(data, 0, length));
							tryEnterCMDModeResponse.notify();
						}
					}
				});

				synchronized (tryEnterCMDModeResponse) {
					if (!udpUnicast.send(Constants.CMD_SCAN_MODULES)) {
						onResult(false);
						return;
					}
					try {
						tryEnterCMDModeResponse.wait(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				waitReceiveResponse(5000, tryEnterCMDModeResponse);

				Log.d(TAG, "Response when to try enter:" + tryEnterCMDModeResponse);
				if (tryEnterCMDModeResponse.length() == 0) {
					onResult(false);
				}else {
					String[] array = tryEnterCMDModeResponse.toString().split(",");
					if (array != null && array.length>0 && Utils.isIP(array[0])) {

						udpUnicast.send(Constants.CMD_ENTER_CMD_MODE);
						synchronized (tryEnterCMDModeResponse) {
							try {
								tryEnterCMDModeResponse.wait(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
//						waitReceiveResponse(500, null);

						tryEnterCMDModeResponse.setLength(0);
//						tryEnterCMDModeResponse = null;
						udpUnicast.send(Constants.CMD_TEST);
						synchronized (tryEnterCMDModeResponse) {
							try {
								tryEnterCMDModeResponse.wait(1500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
//						waitReceiveResponse(1500, tryEnterCMDModeResponse);
						if (tryEnterCMDModeResponse.length() != 0) {
							onResult(true);
						}else {
							onResult(false);
						}
					}else {
						onResult(false);
					}
				}
			}
		}

		abstract void onResult(boolean success);
	}

	public void resetTimes() {
		times = 0;
	}

	private void waitReceiveResponse(long wait, String response) {

		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < wait  && response == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}
	}

	private void routeResponse(String response) {

		Message msg = handler.obtainMessage(CODE_SEND_CMD_FILE_RESPONSE);
		Bundle bundle = new Bundle();
		bundle.putString(RESPONSE, response);
		msg.setData(bundle);
		handler.sendMessage(msg);
	}
}
