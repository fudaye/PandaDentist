package com.pandadentist;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.pandadentist.download.DownloadConfiguration;
import com.pandadentist.download.DownloadManager;
import com.pandadentist.util.DensityUtil;
import com.pandadentist.util.Toasts;
import com.tencent.smtt.sdk.QbSdk;


/**
 * Created by Ford on 2016/9/12.
 */
public class App extends Application {

    public static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        Toasts.register(this);
        DensityUtil.register(this);
        initDownloader();
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }

    public static Context getContext() {
        return sContext;
    }



    private void initDownloader() {
        DownloadConfiguration configuration = new DownloadConfiguration();
        configuration.setMaxThreadNum(10);
        configuration.setThreadNum(3);
        DownloadManager.getInstance().init(getApplicationContext(), configuration);
    }
}
