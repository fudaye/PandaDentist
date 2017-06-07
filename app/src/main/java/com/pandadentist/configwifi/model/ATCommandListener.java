package com.pandadentist.configwifi.model;

public interface ATCommandListener {

	public void onEnterCMDMode(boolean success);
	public void onExitCMDMode(boolean success, NetworkProtocol protocol);
	public void onSendFile(boolean success);
	public void onReload(boolean success);
	public void onReset(boolean success);
	public void onResponse(String response);
	public void onResponseOfSendFile(String response);
}
