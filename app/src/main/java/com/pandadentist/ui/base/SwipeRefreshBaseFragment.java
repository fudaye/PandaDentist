package com.pandadentist.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;


import com.pandadentist.R;
import com.pandadentist.listener.OnRcvScrollListener;
import com.pandadentist.widget.MultiSwipeRefreshLayout;

import butterknife.Bind;

public abstract class SwipeRefreshBaseFragment extends BaseFragment
        implements SwipeRefreshLayer {

    protected ProgressDialog mProg;
   @Nullable
    @Bind(R.id.swipe_refresh_layout) public MultiSwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsRequestDataRefresh = false;

    public OnRcvScrollListener onRcvScrollListener = new OnRcvScrollListener() {
        @Override
        public void onLoadMore() {
            super.onLoadMore();
            loadMore();
        }
    };

    public  void  loadMore(){}

    public void setLoadEnable(boolean b){
        onRcvScrollListener.setLoadEnable(b);
    }

    public void setLoadingMore(boolean b ){
        setRefresh(b);
        onRcvScrollListener.setLoadingMore(b);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trySetupSwipeRefresh();
    }

    void trySetupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_1,R.color.refresh_progress_2,R.color.refresh_progress_3 );
            // Do not use lambda here!
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override public void onRefresh() {
                    requestDataRefresh();
                }
            });
        }
    }


    @Override public void requestDataRefresh() {
        mIsRequestDataRefresh = true;
    }

    public void setRefresh(boolean requestDataRefresh) {
        if (mSwipeRefreshLayout == null) {
            return;
        }
        if (!requestDataRefresh) {
            mIsRequestDataRefresh = false;
            // 防止刷新消失太快，让子弹飞一会儿.
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }, 1000);
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    public boolean isRequestDataRefresh() {
        return mIsRequestDataRefresh;
    }

    public void loadError(View view, Throwable throwable) {
        throwable.printStackTrace();
        Snackbar.make(view, R.string.snap_load_fail, Snackbar.LENGTH_LONG).setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDataRefresh();
            }
        }).show();
    }

    public void  showLoading(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setRefresh(true);
            }
        },358);
    }

    protected void showProgress(){
        if(mProg == null){
            mProg = new ProgressDialog(getActivity());
            mProg.setMessage("正在加载中...");
            mProg.show();
        }else{
            mProg.show();
        }
    }
    protected void dismiss(){
        if(mProg != null){
            mProg.dismiss();
        }
    }
}
