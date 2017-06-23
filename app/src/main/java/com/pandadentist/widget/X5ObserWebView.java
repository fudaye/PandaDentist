package com.pandadentist.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.WebView;

/**
 * Created by fudaye on 2017/6/20.
 *
 * 解决滑动冲突
 */

public class X5ObserWebView extends WebView {
    private OnScrollChangedCallback mOnScrollChangedCallback;

    public X5ObserWebView(final Context context) {
        super(context);
    }

    public X5ObserWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

//  @Override
//    protected void tbs_onScrollChanged(int l, int t, int oldl, int oldt, View view) {
//        this.super_onScrollChanged(l, t, oldl, oldt);
//        //X5WebView 父类屏蔽了 onScrollChanged 方法 要用该方法
//        if (mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(l, t);
//    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //普通webview用这个
        if (mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(l, t);
    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }


    public interface OnScrollChangedCallback {
        void onScroll(int l, int t);
    }
}
