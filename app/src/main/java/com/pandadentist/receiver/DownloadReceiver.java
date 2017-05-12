package com.pandadentist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.pandadentist.entity.AppInfoEntity;
import com.pandadentist.service.DownloadService;
import com.pandadentist.util.AppHelper;

import java.io.File;

/**
 * Created by Ford on 2016/7/11.
 *
 * 下载文件广播
 *
 */
public class DownloadReceiver extends BroadcastReceiver {

    private File mDownloadDir;

    private Context mContext;

    public DownloadReceiver(Context context,File file) {
        this.mDownloadDir = file;
        this.mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null || !action.equals(DownloadService.ACTION_DOWNLOAD_BROAD_CAST)) {
            return;
        }
        final int position = intent.getIntExtra(DownloadService.EXTRA_POSITION, -1);
        final AppInfoEntity tmpInfo = (AppInfoEntity) intent.getSerializableExtra(DownloadService.EXTRA_APP_INFO);
        if (tmpInfo == null || position == -1) {
            return;
        }
        final int status = tmpInfo.getStatus();
        switch (status) {
            case AppInfoEntity.STATUS_CONNECTING:
                Log.d("STATUS_CONNECTING","STATUS_CONNECTING");
                break;

            case AppInfoEntity.STATUS_DOWNLOADING:
                Log.d("STATUS_CONNECTING","STATUS_CONNECTING");
                break;
            case AppInfoEntity.STATUS_COMPLETE:
                File apk = new File(mDownloadDir, tmpInfo.getName() + ".apk");
                if (apk.isFile() && apk.exists()) {
                    AppHelper.installApp(mContext,apk);
                }
                break;

            case AppInfoEntity.STATUS_PAUSED:
                Log.d("STATUS_PAUSED","STATUS_PAUSED");
//                appInfo.setStatus(AppInfo.STATUS_PAUSED);
//                if (isCurrentListViewItemVisible(position)) {
//                    ListViewAdapter.ViewHolder holder = getViewHolder(position);
//                    holder.tvStatus.setText(appInfo.getStatusText());
//                    holder.btnDownload.setText(appInfo.getButtonText());
//                }
                break;
            case AppInfoEntity.STATUS_NOT_DOWNLOAD:
                Log.d("STATUS_NOT_DOWNLOAD","STATUS_NOT_DOWNLOAD");
                break;
            case AppInfoEntity.STATUS_DOWNLOAD_ERROR:
                Log.d("STATUS_DOWNLOAD_ERROR","STATUS_DOWNLOAD_ERROR");
                break;
        }
    }
}