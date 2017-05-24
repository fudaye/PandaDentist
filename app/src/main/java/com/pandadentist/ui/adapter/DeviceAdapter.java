package com.pandadentist.ui.adapter;

import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pandadentist.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ford on 2017/5/24.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<ScanResult> mData = new ArrayList<>();

    public DeviceAdapter(List<ScanResult> mData) {
        this.mData = mData;
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
        }
    }

    public void setData (List<ScanResult> data){
        this.mData = data;
        notifyDataSetChanged();
    }
}
