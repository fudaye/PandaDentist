package com.pandadentist.configwifi.android;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import com.pandadentist.R;

public class AboutActivity extends Activity {

	private TextView mVersionTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		mVersionTextView = (TextView)findViewById(R.id.textView2);
		PackageManager packageManager = getPackageManager();
		try {
			
			PackageInfo packageInfo = packageManager.getPackageInfo(
					AboutActivity.class.getPackage().getName(), PackageManager.GET_ACTIVITIES);
			mVersionTextView.setText(packageInfo.versionName);
		} catch (Exception e) {
			e.printStackTrace();
			mVersionTextView.setText(R.string.unknown);
		}
	}
}
