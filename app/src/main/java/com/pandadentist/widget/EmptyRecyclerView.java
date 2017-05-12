package com.pandadentist.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.pandadentist.R;


/**
 * Created by Ford on 2016/8/23.
 * <p>
 * 数据源为空的时候显示emptyView
 */
public class EmptyRecyclerView extends FrameLayout {

    private View emptyView;

    private RecyclerView mRv;
    private ImageView iv_prompt;

    /**
     * 判断页面空数据原因的type  0 暂无项目  1 暂无消息  2  网络异常  3  系统异常
     */
    private int emptyType = 0;
    private ImageView iv_empty;

    public EmptyRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public EmptyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EmptyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setEmpty(int type) {
        this.emptyType = type;
    }

    private void init(Context context) {
        mRv = new RecyclerView(context);
        mRv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mRv);
        emptyView = LayoutInflater.from(getContext()).inflate(R.layout.common_empty, null);
        iv_empty = (ImageView) emptyView.findViewById(R.id.iv_empty);
        addView(emptyView);
    }


    final private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    private void checkIfEmpty() {
        if (emptyView != null && mRv.getAdapter() != null) {
            final boolean emptyViewVisible = mRv.getAdapter().getItemCount() == 0;
//            if (emptyType == Constants.EMPTY_NO_MESSAGE_TYPE) {
//                iv_empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_message));
//            }else if(emptyType == Constants.EMPTY_ABNORMAL_TYPE){
//                iv_empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_network_anomaly));
//            }else if(emptyType == Constants.EMPTY_NETWORK_TYPE){
//                iv_empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_abnormal_system));
//            }
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
        }
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        final RecyclerView.Adapter oldAdapter = mRv.getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        mRv.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRv.setLayoutManager(layoutManager);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mRv.getLayoutManager();
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        mRv.addOnScrollListener(onScrollListener);
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
