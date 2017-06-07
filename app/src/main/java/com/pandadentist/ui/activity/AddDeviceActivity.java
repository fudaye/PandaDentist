package com.pandadentist.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.widget.Toast;

import com.pandadentist.R;
import com.pandadentist.ui.adapter.DeviceAdapter;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.widget.RecycleDecoration;


import java.util.ArrayList;
import java.util.List;




/**
 * Created by Ford on 2017/5/15.
 */

public class AddDeviceActivity extends SwipeRefreshBaseActivity {

    //定义WifiManager对象
    private WifiManager mainWifi;
    //扫描出的网络连接列表
    private List<ScanResult> wifiList = new ArrayList<>();
    private List<ScanResult> tempwifiList = new ArrayList<>();
    //扫描完毕接收器
    private WifiReceiver receiverWifi;

    RecyclerView mRv;

    private DeviceAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.addDevice));
        mRv = (RecyclerView) findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.addItemDecoration(new RecycleDecoration(AddDeviceActivity.this));
        mAdapter = new DeviceAdapter(wifiList,this);
        mRv.setAdapter(mAdapter);

        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        scanWifi();
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
                wifiList.clear();
                tempwifiList = mainWifi.getScanResults();
                Toast.makeText(context, "扫描完毕" + tempwifiList.size(), Toast.LENGTH_SHORT).show();
                for(ScanResult sr : tempwifiList){
                    if(sr.SSID.contains("BJYDD")){
                        wifiList.add(sr);
                    }
                }
                mAdapter.setData(wifiList);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiverWifi);
    }

}
