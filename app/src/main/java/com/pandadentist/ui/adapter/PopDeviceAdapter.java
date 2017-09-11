package com.pandadentist.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.entity.DeviceListEntity;
import com.pandadentist.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ford on 2017/5/24.
 */

public class PopDeviceAdapter extends RecyclerView.Adapter<PopDeviceAdapter.ViewHolder> {

    private List<DeviceListEntity.DevicesBean> mData = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public PopDeviceAdapter(List<DeviceListEntity.DevicesBean> mData) {
        this.mData = mData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pop_device_list,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DeviceListEntity.DevicesBean db = mData.get(position);
        holder.name.setText(db.getUsername()+"-"+db.getDeviceid());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView isConnect;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            isConnect = (TextView) itemView.findViewById(R.id.tv_isConnect);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null ){
                        onItemClickListener.onItemClick(itemView,getAdapterPosition());
                    }
                }
            });

        }
    }

    public void setData (List<DeviceListEntity.DevicesBean> data){
        this.mData = data;
        notifyDataSetChanged();
    }

}
