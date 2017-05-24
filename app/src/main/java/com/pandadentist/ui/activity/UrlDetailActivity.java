package com.pandadentist.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.Toasts;

/**
 * Created by Ford on 2016/10/14.
 */
public class UrlDetailActivity extends SwipeRefreshBaseActivity {

    private WebView mWebView;
    private String mUrl = "http://www.easylinkage.cn/webapp2/analysis/today?id=361&index=0&token=3f84091d80c3ed572f95347d95d66baf&r=0.4784193145863376";
    private ProgressDialog mDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.app_name));
        mToolBackRl.setVisibility(View.INVISIBLE);
        mToolbarFuncIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_device));
        mToolbarFuncRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toasts.showShort("添加设备");
                IntentHelper.gotoAddDevice(UrlDetailActivity.this);
            }
        });
        mWebView = (WebView) findViewById(R.id.wv);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("加载中....");
//        loadUrl(mUrl);
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_url;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        mDialog.show();
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d("newProgress", "newProgress-->" + newProgress);
                if (mDialog == null) {
                    return;
                }
                if (newProgress == 100) {
                    mDialog.dismiss();
                }
                super.onProgressChanged(view, newProgress);
            }

        });
        settings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDomStorageEnabled(true);
        mWebView.loadUrl(url);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                Log.d("Loading", "Loading");
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("onPageFinished", "onPageFinished");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                Log.d("onReceivedSslError", "onReceivedSslError");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.onPause();
            mWebView.clearCache(true);
            mWebView.clearFormData();
            mWebView.clearHistory();
            mWebView.destroy();
        }

    }
}
