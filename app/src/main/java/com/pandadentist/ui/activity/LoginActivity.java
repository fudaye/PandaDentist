package com.pandadentist.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

/**
 * Created by Ford on 2016/10/14.
 *
 * test
 */
public class LoginActivity extends SwipeRefreshBaseActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,WifiListActivity.class));
            }
        });
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_login;
    }


}
