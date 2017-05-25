package com.pandadentist.ui.activity;

import android.os.Bundle;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.widget.RoundProgressBarWidthNumber;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;

/**
 * Created by Ford on 2017/5/25.
 */

public class LoadingActivity extends SwipeRefreshBaseActivity {
    @Bind(R.id.prog)
    RoundProgressBarWidthNumber prog;
    private int progNum = 0;
    private Timer timer;
    private TimerTask timerTask;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.connectWifi));
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                progNum += 1;
                prog.setProgress(progNum);
            }
        };
        timer.schedule(timerTask, 0, 100);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_loading;
    }
}
