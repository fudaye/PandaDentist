package com.pandadentist.configwifi.model;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.pandadentist.configwifi.net.INetworkTransmission;
import com.pandadentist.configwifi.net.TCPClient;
import com.pandadentist.configwifi.net.TCPServer;
import com.pandadentist.configwifi.net.UdpUnicast;


public class TransparentTransmission {

	private static final String KEY_BUFFER = "KEY_BUFFER";
	private static final String KEY_LENGTH = "KEY_LENGTH";
	private static final String KEY_TCPCLIENT_OPEN = "KEY_TCPCLIENT_OPEN";
	private static final int MSG_DATA = 1;
	private static final int MSG_TCPCLIENT = 2;
	
	private NetworkProtocol protocol;
	private TransparentTransmissionListener listener;
	private INetworkTransmission transmission;
	private Handler handler;
	
	/**
	 * @return the protocol
	 */
	public NetworkProtocol getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(NetworkProtocol protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the listener
	 */
	public TransparentTransmissionListener getListener() {
		return listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(TransparentTransmissionListener listener) {
		this.listener = listener;
	}
	
	public TransparentTransmission() {
		super();
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_DATA:

					if (listener != null) {
						listener.onReceive(
								msg.getData().getByteArray(KEY_BUFFER), msg.getData().getInt(KEY_LENGTH));
					}
					break;
				case MSG_TCPCLIENT:

					if (listener != null) {
						listener.onOpen(msg.getData().getBoolean(KEY_TCPCLIENT_OPEN));
					}
					break;

				default:
					break;
				}
			}
		};
	}

	public boolean init() {
		int type = protocol.getType();
		if (type == 0) {
			return false;
		}else if (type == NetworkProtocol.PROTOCAL_TCP_SERVER) {
			
			transmission = new TCPClient(protocol.getIp(), protocol.getPort());
			TCPClient tcpClient = (TCPClient)transmission;
			tcpClient.setListener(new TCPClient.TCPClientListener() {
				
				@Override
				public void onReceive(byte[] buffer, int length) {
					handleData(buffer, length);
				}
				
				@Override
				public void onConnect(boolean success) {

					Message message = handler.obtainMessage(MSG_TCPCLIENT);
					Bundle bundle = new Bundle();
					bundle.putBoolean(KEY_TCPCLIENT_OPEN, success);
					message.setData(bundle);
					handler.sendMessage(message);
				}
			});
			
			return true;
		}else if (type == NetworkProtocol.PROTOCAL_TCP_CLIENT) {
			
			transmission = new TCPServer(protocol.getIp(), protocol.getPort());
			TCPServer tcpServer = (TCPServer)transmission;
			tcpServer.setListener(new TCPServer.TCPServerListener() {
				
				@Override
				public void onReceive(byte[] buffer, int length) {
					handleData(buffer, length);
				}
			});
			
			return true;
		}else if (type == NetworkProtocol.PROTOCAL_UDP) {
			
			transmission = new UdpUnicast(protocol.getIp(), protocol.getPort());
			UdpUnicast udpUnicast = (UdpUnicast)transmission;
			udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {
				
				@Override
				public void onReceived(byte[] data, int length) {
					handleData(data, length);
				}
			});
			
			return true;
		}else {
			return false;
		}
	}

	public boolean open() {
		
		transmission.setParameters(protocol.getIp(), protocol.getPort());
		boolean success = transmission.open();
		if (transmission instanceof TCPServer || transmission instanceof UdpUnicast) {
			if (listener != null) {
				listener.onOpen(success);
			}
		}/*else if (transmission instanceof TCPClient) {
			
		}*/
		
		return success;
	}
	
	public void close() {
		transmission.close();
	}
	
	public boolean send(String text) {
		return transmission.send(text);
	}
	
	/**
	 * Handle data received from read thread
	 * @param data
	 * @param length
	 */
	private void handleData(byte[] data, int length) {
		Message message = handler.obtainMessage(MSG_DATA);
		Bundle bundle = new Bundle();
		bundle.putByteArray(KEY_BUFFER, data);
		bundle.putInt(KEY_LENGTH, length);
		message.setData(bundle);
		handler.sendMessage(message);
	}
}
