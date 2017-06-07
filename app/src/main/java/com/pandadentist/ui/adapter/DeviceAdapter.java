package com.pandadentist.ui.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.configwifi.android.AccessPoint;
import com.pandadentist.configwifi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ford on 2017/5/24.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<ScanResult> mData = new ArrayList<>();

    private WifiManager mWifiManager;

    private Context mContext;

    private String ssid ="";

    public DeviceAdapter(List<ScanResult> mData,Context context) {
        this.mContext = context;
        this.mData = mData;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_device,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScanResult sr = mData.get(position);
        holder.tv.setText(sr.SSID);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect(mData.get(getAdapterPosition()));
                }
            });
        }
    }

    public void setData (List<ScanResult> data){
        this.mData = data;
        notifyDataSetChanged();
    }

    private void connect(ScanResult result) {

        ssid = Utils.getSettingApSSID(mContext);
        AccessPoint  accessPoint = new AccessPoint(mContext, result);
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = AccessPoint.convertToQuotedString(accessPoint.getSsid());
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.priority = Integer.MAX_VALUE;
        int networkId = mWifiManager.addNetwork(config);
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();
//        if (networkId == -1) {
//            return;
//        }
//
//        // Connect to network by disabling others.
//        mWifiManager.enableNetwork(networkId, true);
//        mWifiManager.reconnect();

    }
}
