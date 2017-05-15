package com.pandadentist.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;


/**
 * Created by Ford on 2017/5/15.
 */

public class WifiListActivity extends SwipeRefreshBaseActivity {

    //定义WifiManager对象
    private WifiManager mainWifi;
    //扫描出的网络连接列表
    private List<ScanResult> wifiList = new ArrayList<>();
    //扫描完毕接收器
    private WifiReceiver receiverWifi;
    private ProgressDialog dialog;

    RecyclerView mRv;
    Adapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRv = (RecyclerView) findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter();
        mRv.setAdapter(mAdapter);
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_wifi_list;
    }

    /**
     * 扫描wifi,加载进度条
     */
    private void scanWifi() {
        openWifi();
        mainWifi.startScan();
        dialog = ProgressDialog.show(this, "", "正在扫描wifi热点,请稍候");
    }

    /**
     * 打开wifi
     */
    private void openWifi() {
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
    }

    /**
     * wifi广播接收器
     */
    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                //TODO 更新列表
                wifiList = mainWifi.getScanResults();
                dialog.dismiss();
                Toast.makeText(context, "扫描完毕" + wifiList.size(), Toast.LENGTH_SHORT).show();
                mAdapter.setUpdate(wifiList);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverWifi);
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {


        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(new TextView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            ((TextView) holder.itemView).setText(wifiList.get(position).SSID);
        }

        @Override
        public int getItemCount() {
            return wifiList.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            public Holder(View itemView) {
                super(itemView);
            }
        }

        public void setUpdate(List<ScanResult> data) {
            wifiList = data;
            notifyDataSetChanged();
        }
    }
}
