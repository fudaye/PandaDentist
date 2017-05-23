package com.pandadentist.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pandadentist.R;
import com.pandadentist.entity.UserInfo;
import com.pandadentist.listener.OnLoginListener;
import com.pandadentist.network.LoginApi;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;



/**
 * Created by Ford on 2016/10/14.
 *
 * test
 */
public class LoginActivity extends SwipeRefreshBaseActivity {

    private static final String APP_ID = "wxa2fe13a5495f3908";
    private IWXAPI api;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this,APP_ID);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.gotoMain(LoginActivity.this);
            }
        });
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_login;
    }

    private void WXLogin(){
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }

}
