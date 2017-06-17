package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private CheckBox mCb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = (TextView) findViewById(R.id.tv_toolbar_title);
        tv.setText("链接WiFi");
        mCb = (CheckBox) findViewById(R.id.cb);
        findViewById(R.id.rl_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if(!TextUtils.isEmpty(SPUitl.getWiFiPwd(mSsidEditText.getText().toString()))){
            mPasswordEditText.setText(SPUitl.getWiFiPwd(mSsidEditText.getText().toString()));
            mCb.setChecked(true);
        }else{
            mCb.setChecked(false);
        }
        mCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //保存wifi密码
                    SPUitl.saveWiFiPwd(mSsidEditText.getText().toString(),mPasswordEditText.getText().toString());
                }else{
                    //清楚wifi密码
                    SPUitl.clearWiFi(mSsidEditText.getText().toString());
                }
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
        IntentHelper.gotoloadingActivity(this,module.getMac());
        if(mCb.isChecked()){
            SPUitl.saveWiFiPwd(mSsidEditText.getText().toString(),mPasswordEditText.getText().toString());
        }
//        startActivity(new Intent(SmartLinkerWrapperActivity.this,LoadingActivity.class));
//        bindDevice(module.getMac());
    }

    @Override
    public void onTimeOut() {
        super.onTimeOut();
        Toasts.showShort("连接失败，请确定WiFi是否连接，");
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
