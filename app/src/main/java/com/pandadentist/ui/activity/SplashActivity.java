package com.pandadentist.ui.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.widget.RoundProgressBarWidthNumber;
import com.umeng.analytics.MobclickAgent;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;




/**
 * 欢迎界面
 * Created by packy on 2016/5/31.
 */
public class SplashActivity extends SwipeRefreshBaseActivity {

    private static final String  TAG = SplashActivity.class.getSimpleName();

    @Bind(R.id.welcome_iv)
    ImageView welcomeIv;
    @Bind(R.id.prog)
    RoundProgressBarWidthNumber prog;
    @Bind(R.id.rl_skip)
    RelativeLayout mSkip;
    @Bind(R.id.tv)
    TextView tv;

    private int progNum = 0;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                progNum += 3;
                prog.setProgress(progNum);
                if (progNum >= 100) {
                    timer.cancel();
                    timerTask.cancel();
                    goMainActivity();
                }
            }
        };
        // 倒计时ji
        timer.schedule(timerTask, 0, 110);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_splash;
    }


    private void goMainActivity() {
        if(!TextUtils.isEmpty(SPUitl.getToken())){
            IntentHelper.gotoMain(SplashActivity.this);
        }else{
            IntentHelper.gotoLogin(SplashActivity.this);
        }
        finish();
    }



    @OnClick({R.id.rl_skip, R.id.welcome_iv})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_skip:
                timer.cancel();
                timerTask.cancel();
                goMainActivity();
                break;
            case R.id.welcome_iv:
                //TODO 点击广告
//                if(mSplash == null ){
//                    return;
//                }
//                timer.cancel();
//                timerTask.cancel();
//                goMainActivity();
//                IntentHelper.gotoDetailActivity(this, mSplash.getSpecialPosConts().getLinkType(), mSplash.getSpecialPosConts().getUrl());
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timerTask.cancel();
        timer = null;
        timerTask = null;
    }
}
