package com.pandadentist.configwifi.net;

import com.pandadentist.configwifi.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;



public class TCPClient implements INetworkTransmission{

	private String ip;
	private int port;
	private Socket socket;
	private BufferedInputStream inputStream;
	private BufferedOutputStream outputStream;
	private TCPClientListener listener;
	private byte[] buffer;
	
	public TCPClient(String ip, int port) {
		super();
		this.ip = ip;
		this.port = port;
		buffer = new byte[1024];
		Utils.forceStrictMode();
	}

	@Override
	public void setParameters(String ip, int port) {
		// TODO Auto-generated method stub
		this.ip = ip;
		this.port = port;
	}

	/**
	 * @return the listener
	 */
	public TCPClientListener getListener() {
		return listener;
	}

	/**
	 * @param listener the listener to set
	 */
	public void setListener(TCPClientListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized boolean open() {

		socket = new Socket();
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					socket.connect(new InetSocketAddress(ip, port), 5000);
					
					inputStream = new BufferedInputStream(socket.getInputStream());
					outputStream = new BufferedOutputStream(socket.getOutputStream());
					if (listener != null) {
						listener.onConnect(true);
					}
					
					int length;
					while (true) {
						
						try {

							length = inputStream.read(buffer);
							if (length > 0) {
								onReceive(buffer, length);
							}
						} catch (Exception e) {
							break;
						}
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					listener.onConnect(false);
				}
			}
		}).start();
		return true;
	}

	@Override
	public void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized boolean send(String text) {
		
		if (outputStream != null) {
			try {
				outputStream.write(text.getBytes(), 0, text.getBytes().length);
				outputStream.flush();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return false;
	}

	@Override
	public void onReceive(byte[] buffer, int length) {
		if (listener != null) {
			listener.onReceive(buffer, length);
		}
	}
	
	public interface TCPClientListener {
		public void onConnect(boolean success);
		public void onReceive(byte[] buffer, int length);
	}
}
