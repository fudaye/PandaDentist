package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.Toasts;
import com.pandadentist.widget.RoundProgressBarWidthNumber;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Ford on 2017/5/25.
 */

public class LoadingActivity extends SwipeRefreshBaseActivity {
    private static final String TAG = LoadingActivity.class.getSimpleName();


    @Bind(R.id.prog)
    RoundProgressBarWidthNumber prog;
    @Bind(R.id.tv_tips)
    TextView mTip;
    @Bind(R.id.tv)
    TextView mTv;
    private int progNum = 0;
    private Timer timer;
    private TimerTask timerTask;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mac = getIntent().getStringExtra("mac");
        Log.d("TAG", "mac-->" + mac);
        handler = new Handler();
        mToolBarTtitle.setText(getResources().getString(R.string.connectWifi));
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                progNum += 10;
                prog.setProgress(progNum);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTv.setText(prog.getProgress()/10+"%");
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 100);
        mTip.setText("连接中.....");
        bindDevice(mac);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_loading;
    }

    private void bindDevice(String mac) {
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.bindDevice(mac, SPUitl.getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        timer.cancel();
                        timerTask.cancel();
                        prog.setProgress(1000);
                        mTv.setText("100%");
                        if (Constants.SUCCESS == wxEntity.getCode()) {
                            mTip.setText("连接成功");
                        } else {
                            Toasts.showShort("错误code ：" + wxEntity.getCode() + "错误信息：" + wxEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        timer.cancel();
        timerTask.cancel();
        IntentHelper.gotoMain(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timerTask.cancel();
    }
}
