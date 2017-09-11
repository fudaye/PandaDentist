package com.pandadentist.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;


import com.pandadentist.R;
import com.pandadentist.listener.OnRcvScrollListener;
import com.pandadentist.widget.MultiSwipeRefreshLayout;

import butterknife.Bind;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;


public abstract class SwipeRefreshBaseActivity extends BaseActivity
        implements SwipeRefreshLayer {

    private  static final String TAG = "";

    protected ProgressDialog mProg;


    private CompositeSubscription mCompositeSubscription;

    @Nullable
    @Bind(R.id.swipe_refresh_layout)
    public MultiSwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsRequestDataRefresh = false;


    public OnRcvScrollListener onRcvScrollListener = new OnRcvScrollListener() {
        @Override
        public void onLoadMore() {
            super.onLoadMore();
            loadMore();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        trySetupSwipeRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void trySetupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.refresh_progress_3, R.color.refresh_progress_2, R.color.refresh_progress_1);
            // Do not use lambda here!
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestDataRefresh();
                }
            });
        }
    }

    public  void  loadMore(){}

    public void setLoadEnable(boolean b){
        onRcvScrollListener.setLoadEnable(b);
    }

    @Override
    public void requestDataRefresh() {
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
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            }, 1000);
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
        }

    }

    public void setLoadingMore(boolean b ){
        setRefresh(b);
        onRcvScrollListener.setLoadingMore(b);
    }

    public void loadError(View view, Throwable throwable) {
        if(view == null){
            return;
        }
        throwable.printStackTrace();
        Snackbar.make(view, R.string.snap_load_fail, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> {
                    requestDataRefresh();
                })
                .show();
    }

    public boolean isRequestDataRefresh() {
        return mIsRequestDataRefresh;
    }

    public void  showLoading(){
        new Handler().postDelayed(() -> setRefresh(true), 358);
    }

    protected void showProgress(){
        if(mProg == null){
            mProg = new ProgressDialog(this);
            mProg.setMessage("正在加载中...");
            mProg.setCancelable(false);
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

    public void addSubscription(Subscription s) {
        if (this.mCompositeSubscription == null) {
            this.mCompositeSubscription = new CompositeSubscription();
        }
        this.mCompositeSubscription.add(s);
    }

}
