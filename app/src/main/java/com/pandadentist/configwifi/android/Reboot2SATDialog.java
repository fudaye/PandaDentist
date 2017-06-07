package com.pandadentist.configwifi.android;

import com.pandadentist.R;
import com.pandadentist.configwifi.utils.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Reboot2SATDialog extends AlertDialog {

	private Context mContext;
	private EditText mSsidEditText;
	private EditText mEncryptionEditText;
	private EditText mPasswdEditText;
	private CheckBox mShowPasswdCheckBox;
	private Button mRebootButton;
	private Button mCancelButton;

	protected Reboot2SATDialog(Context context, ScanResult scanResult) {
		super(context);

		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout linearLayout = (LinearLayout)inflater.inflate(R.layout.reboot2sta, null);

    	mSsidEditText = (EditText)linearLayout.findViewById(R.id.editText1);
    	mEncryptionEditText = (EditText)linearLayout.findViewById(R.id.editText2);
    	mPasswdEditText = (EditText)linearLayout.findViewById(R.id.editText3);
    	mShowPasswdCheckBox = (CheckBox)linearLayout.findViewById(R.id.checkBox1);
    	mRebootButton = (Button)linearLayout.findViewById(R.id.button1);
    	mCancelButton = (Button)linearLayout.findViewById(R.id.button2);

    	//show text info received
    	mSsidEditText.setText(scanResult.SSID);
    	String security = Utils.parseSecurity(scanResult.capabilities);
    	if (Utils.SECURITY_OPEN_NONE.equals(security)) {
			linearLayout.findViewById(R.id.tableRow1).setVisibility(View.GONE);
			linearLayout.findViewById(R.id.tableRow2).setVisibility(View.GONE);
		}
    	mEncryptionEditText.setText(security.replace(',', '/').toUpperCase());
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

    	mRebootButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				//reboot action
				onRebootChoosed();
			}
		});

    	mCancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dismiss();
				onCancelChoosed();
			}
		});

    	setView(linearLayout, 0, -8, 0, -8);
    	setCancelable(false);
	}

	/**
	 * invoked when clicked reboot button and sure to reboot
	 */
	public void onRebootChoosed() {
	}

	/**
	 * invoked when clicked cancel button and sure to reboot
	 */
	public void onCancelChoosed() {
	}
}
