package com.pandadentist;

import android.app.Application;
import android.content.Context;

import com.pandadentist.download.DownloadConfiguration;
import com.pandadentist.download.DownloadManager;
import com.pandadentist.util.DensityUtil;
import com.pandadentist.util.Toasts;


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
