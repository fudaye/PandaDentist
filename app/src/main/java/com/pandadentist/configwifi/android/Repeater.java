package com.pandadentist.configwifi.android;

import android.os.Handler;
import android.os.Message;

public abstract class Repeater extends Handler {

	private long delay;

	public Repeater(long delay) {
		super();
		this.delay = delay;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		repeateAction();
		sendEmptyMessageDelayed(0, delay);
	}

	public void resumeWithDelay() {
		if (!hasMessages(0)) {
			sendEmptyMessageDelayed(0, delay);
		}
	}

	public void resume() {
		if (!hasMessages(0)) {
			sendEmptyMessage(0);
		}
	}

	public void pause() {
		removeMessages(0);
	}

	public abstract void repeateAction();
}
