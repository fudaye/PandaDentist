package com.pandadentist.configwifi.android;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pandadentist.R;
import com.pandadentist.configwifi.utils.Constants;
import com.pandadentist.configwifi.utils.Utils;

public class SettingActivity extends PreferenceActivity implements OnPreferenceChangeListener{

	private EditTextPreference mCMDScanModulesPreference;
	private EditTextPreference mSsidPreference;
	private EditTextPreference mPasswdPreference;
	private EditTextPreference mPortPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		setupPreferences();
	}

	private void setupPreferences() {
		mCMDScanModulesPreference = (EditTextPreference)findPreference(Constants.KEY_CMD_SCAN_MODULES);
		mSsidPreference = (EditTextPreference)findPreference(Constants.KEY_SSID);
		mPasswdPreference = (EditTextPreference)findPreference(Constants.KEY_PASSWORD);
		mPortPreference = (EditTextPreference)findPreference(Constants.KEY_UDP_PORT);
		mCMDScanModulesPreference.setOnPreferenceClickListener(
				createPreferenceClickListener(mCMDScanModulesPreference));
		mSsidPreference.setOnPreferenceClickListener(
				createPreferenceClickListener(mSsidPreference));
		mPasswdPreference.setOnPreferenceClickListener(
				createPreferenceClickListener(mPasswdPreference));

		mPortPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub

				final EditText editText = mPortPreference.getEditText();
				final AlertDialog dialog = (AlertDialog)mPortPreference.getDialog();

				editText.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {

						try {
							int port = Integer.valueOf(editText.getText().toString());
							if (port>=0 && port<=65535) {
								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
							}else {
								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							}
						} catch (Exception e) {
							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
						}
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {

						try {
							int port = Integer.valueOf(editText.getText().toString());
							if (port>=0 && port<=65535) {
								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
							}else {
								dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							}
						} catch (Exception e) {
							dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
						}
					}

					@Override
					public void afterTextChanged(Editable s) {
					}
				});

				return false;
			}
		});

		mPortPreference.setOnPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		displaySummary(mCMDScanModulesPreference);
		displaySummary(mSsidPreference);
		displaySummary(mPasswdPreference);
		displaySummary(mPortPreference);
	}

	private void displaySummary(Preference preference) {

		String summary = preference.getSharedPreferences().getString(
				preference.getKey(), null);
		if (summary == null || summary.trim().equals("")) {
			preference.setSummary(getString(R.string.null_));
		}else if (preference == mPasswdPreference) {

			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < summary.length(); i++) {
				sb.append("•");
			}
			preference.setSummary(sb.toString());
		}else {
			preference.setSummary(summary);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		if (preference == mPortPreference) {
			mPortPreference.setSummary(newValue.toString());
		}
		return true;
	}

	private OnPreferenceClickListener createPreferenceClickListener(final EditTextPreference editTextPreference) {
		return new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				final Dialog dialog = editTextPreference.getDialog();
				final Button button = (Button)dialog.findViewById(Utils.getAndroidInternalId("button1"));
				final EditText editText = (EditText)dialog.findViewById(Utils.getAndroidInternalId("edit"));
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						new AlertDialog.Builder(SettingActivity.this)
								.setTitle(R.string.warning)
								.setMessage(R.string.comfirm_modify)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface _dialog, int which) {
										editTextPreference.setText(editText.getText().toString().trim());
										displaySummary(editTextPreference);
										dialog.dismiss();
									}
								})
								.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface _dialog, int which) {
										dialog.dismiss();
									}
								})
								.create().show();
					}
				});
				return false;
			}
		};
	}
}
