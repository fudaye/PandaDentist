package com.pandadentist.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Ford on 2017/5/25.
 */

public class ConnectWifiActivity extends SwipeRefreshBaseActivity {


    @Bind(R.id.tv_wifi_name)
    TextView tvWifiName;
    @Bind(R.id.tv_wifi_pwd)
    EditText tvWifiPwd;
    @Bind(R.id.cb)
    CheckBox cb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.connectWifi));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_connect_wifi;
    }


    @OnClick(R.id.btn)
    public void onClick() {
        Intent intent = new Intent(ConnectWifiActivity.this,LoadingActivity.class);
        startActivity(intent);
    }
}
