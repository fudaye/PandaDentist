package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.widget.EditText;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by fudaye on 2017/6/14.
 */

public class EmailRegisterActivity extends SwipeRefreshBaseActivity {


    @Bind(R.id.et_username)
    EditText etUsername;
    @Bind(R.id.et_pwd)
    EditText etPwd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.emailRegister));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_email_register;
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
    }
}
