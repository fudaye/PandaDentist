package com.pandadentist.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;




/**
 * Created by Ford on 2016/10/14.
 *
 * test
 */
public class LoginActivity extends SwipeRefreshBaseActivity {

    private static final String APP_ID = "wxa2fe13a5495f3908";
    private IWXAPI api;
    private CodeReceiverBroadcast broadcast;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!TextUtils.isEmpty(SPUitl.getToken())){
            IntentHelper.gotoMain(this);
            finish();
        }
        api = WXAPIFactory.createWXAPI(this,APP_ID);
        LocalBroadcastManager lbm =  LocalBroadcastManager.getInstance(this);
        broadcast = new CodeReceiverBroadcast();
        lbm.registerReceiver(broadcast,new IntentFilter(Constants.BROADCAST_FLAG_CODE_MESSAGE));
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IntentHelper.gotoMain(LoginActivity.this);
                WXLogin();
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

    private void getToken(String code){
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.getWXToken(code, Constants.AAAA)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        Log.d("throwable","throwable-->"+wxEntity.toString());
                        SPUitl.saveToken(wxEntity.getToken());
                        IntentHelper.gotoMain(LoginActivity.this);
                        finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable","throwable-->"+throwable.toString());
                    }
                });
        addSubscription(s);
    }

    class CodeReceiverBroadcast extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String  str = intent.getStringExtra(Constants.BUNDLE_KEY.VALUE);
            Log.d("str","str--->"+str);
            getToken(str);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcast);
    }
}
