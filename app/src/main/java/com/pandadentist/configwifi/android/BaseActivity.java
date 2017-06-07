package com.pandadentist.configwifi.android;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.pandadentist.R;

import java.io.Serializable;

public class BaseActivity extends Activity {

	private Handler mNotifierHandler;
	String TAG = "SmartLink | ";

	public BaseActivity() {
		TAG += this.getClass().getSimpleName();

		mNotifierHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
					break;
				case 1:
					Toast.makeText(getApplicationContext(), msg.arg1, msg.arg2==1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
					break;
				case 2:

					new Builder(BaseActivity.this)
							.setTitle(R.string.default_dialog_title)
							.setMessage(msg.arg1)
							.setPositiveButton(R.string.ok, null)
							.create().show();
					break;
				case 3:

					new Builder(BaseActivity.this)
							.setTitle(R.string.default_dialog_title)
							.setMessage(msg.obj.toString())
							.setPositiveButton(R.string.ok, null)
							.create().show();
					break;
				case 4:

					Bundle data = msg.getData();
					SimpleDialogListener listener = null;
					if (data.getSerializable("listener") != null) {
						listener = (SimpleDialogListener)data.getSerializable("listener");
					}
					Builder builder = new Builder(BaseActivity.this)
							.setTitle(data.getString("title"))
							.setMessage(data.getString("message"))
							.setPositiveButton(R.string.ok, listener);
					if (data.getBoolean("withCancelButton")) {
						builder.setNegativeButton(R.string.no, null);
					}
					builder.create().show();
					break;

				default:
					break;
				}
			}
		};
	}

	void logD(String msg) {
		Log.d(TAG, msg == null ? "null" : msg);
	}

	void logE(String msg) {
		Log.e(TAG, msg == null ? "null" : msg);
	}

	void logW(String msg) {
		Log.w(TAG, msg == null ? "null" : msg);
	}

	void toast(String text) {
		text = text==null ? "null" : text;
		Message msg = mNotifierHandler.obtainMessage(0);
		msg.obj = text;
		mNotifierHandler.sendMessage(msg);
	}

	void toast(int resId) {
		toast(resId, false);
	}

	void toast(int resId, boolean longer) {
		Message msg = mNotifierHandler.obtainMessage(1);
		msg.arg1 = resId;
		msg.arg2 = longer ? 1 : 0;
		mNotifierHandler.sendMessage(msg);
	}

	void simpleDialog(int resId) {
		Message msg = mNotifierHandler.obtainMessage(2);
		msg.arg1 = resId;
		mNotifierHandler.sendMessage(msg);
	}

	void simpleDialog(String text) {
		text = text==null ? "null" : text;
		Message msg = mNotifierHandler.obtainMessage(3);
		msg.obj = text;
		mNotifierHandler.sendMessage(msg);
	}

	void simpleDialog(String title, String message, boolean withCancelButton, SimpleDialogListener listener) {
		Message msg = mNotifierHandler.obtainMessage(4);
		Bundle data = new Bundle();
		data.putSerializable("listener", listener);
		data.putString("title", title);
		data.putString("message", message);
		data.putBoolean("withCancelButton", withCancelButton);
		msg.setData(data);
		mNotifierHandler.sendMessage(msg);
	}

//	void promptDialog(Dialog dialog) {
//		Message msg = mNotifierHandler.obtainMessage(1);
//		msg.arg1 = resId;
//		mNotifierHandler.sendMessage(msg);
//	}

	class SimpleDialogListener implements DialogInterface.OnClickListener, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(DialogInterface dialog, int which) {

		}
	}
}
