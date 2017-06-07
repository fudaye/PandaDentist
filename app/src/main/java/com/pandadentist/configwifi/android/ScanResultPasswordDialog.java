package com.pandadentist.configwifi.android;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.pandadentist.R;
import com.pandadentist.configwifi.utils.Utils;

public class ScanResultPasswordDialog extends AlertDialog {

	private Context mContext;
	private ScanResult mScanResult;
	private EditText mSsidEditText;
	private EditText mPasswdEditText;
	private CheckBox mShowPasswdCheckBox;
	private boolean mNotDismiss;

	protected ScanResultPasswordDialog(Context context, ScanResult scanResult) {
		super(context);

		mContext = context;
		mScanResult = scanResult;
		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.scan_result_password, null);

    	mSsidEditText = (EditText)linearLayout.findViewById(R.id.editText1);
    	mPasswdEditText = (EditText)linearLayout.findViewById(R.id.editText2);
    	mShowPasswdCheckBox = (CheckBox)linearLayout.findViewById(R.id.checkBox1);

    	//show text info received
    	mSsidEditText.setText(scanResult.SSID);
    	mPasswdEditText.setText(Utils.getScanResultPassword(context, scanResult));

    	mShowPasswdCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mPasswdEditText.setTransformationMethod(null);
				}else {
					mPasswdEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
			}
    	});

    	setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.save), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				String passwd = mPasswdEditText.getText().toString().trim();
				String security = Utils.parseSecurity(mScanResult.capabilities);
				if (security != null && !security.equals(Utils.SECURITY_OPEN_NONE)) {

					if (passwd.length() == 0) {
						promptPasswordInvalid(mContext.getString(R.string.password_not_empty, mScanResult.SSID));
						return;
					}else if (security.equals(Utils.SECURITY_WEP)) {
						int wepType = Utils.checkWepType(passwd);
						System.out.println("wepType: " + wepType);
						if (wepType == Utils.WEP_INVALID) {
							//prompt dialog that password invalid
							promptPasswordInvalid(mContext.getString(R.string.wep_password_invalid));
							return;
						}
					}
				}

				Utils.saveScanResultPassword(mContext, mScanResult, passwd);
				onSaveButtonClicked(passwd);
			}
		});

    	setView(linearLayout, 0, -8, 0, -8);
    	setTitle(R.string.pls_input_password);
	}

	/**
	 * invoked when clicked save button
	 */
	public void onSaveButtonClicked(String passwordSaved) {
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		mNotDismiss = false;
	}

	@Override
	public void dismiss() {
		if (!mNotDismiss) {
			super.dismiss();
		}
	}

	private void promptPasswordInvalid(String msg) {

		new Builder(mContext)
				.setTitle(R.string.default_dialog_title)
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mNotDismiss = false;
					}
				}).setCancelable(false)
				.create().show();
		mNotDismiss = true;
	}
}
