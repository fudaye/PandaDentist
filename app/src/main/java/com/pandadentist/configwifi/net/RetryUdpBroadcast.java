package com.pandadentist.configwifi.net;

import java.net.DatagramPacket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

public class RetryUdpBroadcast {

	private UdpBroadcast udpBroadcast;
	private Timer timer;
	private TimerTask task;
	private Handler handler;
	private Object object;

	public RetryUdpBroadcast() {
		super();

		udpBroadcast = new UdpBroadcast() {

			@Override
			public void onReceived(List<DatagramPacket> packets) {

				synchronized (object) {
					object.notify();
				}
			}
		};
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				udpBroadcast.send(msg.obj.toString());
				synchronized (object) {
					try {
						object.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		task = new TimerTask() {

			@Override
			public void run() {

			}
		};
	}

	public void send(String text) {
		Message msg = handler.obtainMessage();
		msg.obj = text;
		handler.sendMessage(msg);
	}

	public void onReceived(List<DatagramPacket> packets) {

	}
}
