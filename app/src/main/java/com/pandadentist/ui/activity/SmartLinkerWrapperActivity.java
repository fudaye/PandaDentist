package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hiflying.smartlink.SmartLinkedModule;
import com.hiflying.smartlink.v7.MulticastSmartLinkerActivity;
import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.Toasts;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by fudaye on 2017/6/7.
 */

public class SmartLinkerWrapperActivity extends MulticastSmartLinkerActivity {


    private static final String TAG = SmartLinkerWrapperActivity.class.getSimpleName();

    private boolean isBind = false;
    private boolean isCompleted = false;
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) findViewById(R.id.tv_toolbar_title);
        tv.setText("链接WiFi");
        findViewById(R.id.rl_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onCompleted() {
        super.onCompleted();
        Log.d(TAG, "-----------------------完成----------------------------");
        isCompleted = true;
        gotoMain();
    }

    @Override
    public void onLinked(SmartLinkedModule module) {
        super.onLinked(module);
        Log.d(TAG, "MAC 地址 ----->" + module.getMac());
        bindDevice(module.getMac());
    }


    private void gotoMain() {
        if (isBind && isCompleted) {
            IntentHelper.gotoMain(this);
            finish();
        }
    }


    private void bindDevice(String mac){
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.bindDevice(mac, SPUitl.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        if(Constants.SUCCESS == wxEntity.getCode()){
                            isBind = true;
                            gotoMain();
                        }else{
                            Toasts.showShort("错误code ："+wxEntity.getCode() + "错误信息："+wxEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable","throwable-->"+throwable.toString());
                    }
                });
        addSubscription(s);
    }



    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }
}
