package com.pandadentist.configwifi.net;

public interface INetworkTransmission {

	public void setParameters(String ip, int port);
	public boolean open();
	public void close();
	public boolean send(String text);
	public void onReceive(byte[] buffer, int length);
}
