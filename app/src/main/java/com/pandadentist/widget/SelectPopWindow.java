package com.pandadentist.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pandadentist.R;
import com.pandadentist.entity.SelectEntity;

import java.util.ArrayList;


/**
 * Created by Ford on 2016/8/1.
 * <p>
 * tab选择提示view
 */
public class SelectPopWindow extends PopupWindow {

    //    private String[] mData;
    private ArrayList<SelectEntity> mData;
    private OnItemClickListener l;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext;
    private SelectAdapter mAdapter;
    private boolean isGrid = false;//判断layout布局为什么类型
    /**
     * 父标签的位置
     */
    private int mParentPosition;

    public SelectPopWindow(Context context, ArrayList<SelectEntity> data, int parentPosition, RecyclerView.LayoutManager layoutManager, boolean isGrid) {
        super(context);
        this.mContext = context;
        this.mParentPosition = parentPosition;
        this.mData = data;
        this.mLayoutManager = layoutManager;
        this.isGrid = isGrid;
        init(context);
    }

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_select, null);
        this.setContentView(contentView);
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        this.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        RecyclerView rv = (RecyclerView) contentView.findViewById(R.id.lv);
        contentView.findViewById(R.id.view_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        rv.setLayoutManager(mLayoutManager);
        mAdapter = new SelectAdapter(mData);
        rv.setAdapter(mAdapter);
    }


    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent);
        } else {
            this.dismiss();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int parentPosition, int childPosition, String label);
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.l = l;
    }


    private class SelectAdapter extends RecyclerView.Adapter<SelectViewHolder> {

        private ArrayList<SelectEntity> data;

        public SelectAdapter(ArrayList<SelectEntity> data) {
            this.data = data;
        }

        @Override
        public SelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (!isGrid) {
                return new SelectViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_pop_linear, parent, false));
            } else {
                return new SelectGridViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_pop_grid, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(SelectViewHolder holder, int position) {
            SelectEntity se = data.get(position);
            if (!isGrid) {
                holder.name.setText(se.name);
                if (se.isSelect) {
                    holder.check.setVisibility(View.VISIBLE);
                    holder.name.setTextColor(mContext.getResources().getColor(R.color.themeColor));
//                    holder.check.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_right_green));
                } else {
                    holder.name.setTextColor(mContext.getResources().getColor(android.R.color.white));
//                    holder.check.setVisibility(View.GONE);
                }
            } else {
                ((SelectGridViewHolder) holder).gridName.setText(se.name);
                if (se.isSelect) {
                    ((SelectGridViewHolder) holder).gridName.setTextColor(mContext.getResources().getColor(R.color.themeColor));
                } else {
                    ((SelectGridViewHolder) holder).gridName.setTextColor(mContext.getResources().getColor(android.R.color.white));
                }

            }

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    public class SelectViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView check;

        public SelectViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_name);
            check = (ImageView) itemView.findViewById(R.id.iv_check);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSelect();
                    mData.get(getPosition()).isSelect = true;
                    mAdapter.notifyDataSetChanged();
                    if (l != null) {
                        l.onItemClick(v, mParentPosition, getPosition(), mData.get(getPosition()).name);
                    }
                    dismiss();
                }
            });
        }
    }

    public class SelectGridViewHolder extends SelectViewHolder {
        TextView gridName;

        public SelectGridViewHolder(View itemView) {
            super(itemView);
            gridName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }

    private void clearSelect() {
        for (SelectEntity se : mData) {
            se.isSelect = false;
        }
    }
}
