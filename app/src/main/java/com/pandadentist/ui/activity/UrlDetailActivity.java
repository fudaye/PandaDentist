package com.pandadentist.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;

import butterknife.Bind;

/**
 * Created by Ford on 2016/10/14.
 */
public class UrlDetailActivity extends SwipeRefreshBaseActivity {

    @Bind(R.id.iv_hint)
    ImageView mivHint;
    @Bind(R.id.wv)
    WebView mWebView;

    private String mUrl = "http://www.easylinkage.cn/webapp2/analysis/today?id=361&index=0&r=0.4784193145863376&token=";
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
                IntentHelper.gotoCapture(UrlDetailActivity.this);
            }
        });

        if (SPUitl.isLogin()) {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("加载中....");
            mivHint.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            loadUrl(mUrl+SPUitl.getToken());
        } else {
            mivHint.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }




    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_url;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient());



        mDialog.show();
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDomStorageEnabled(true);
//        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d("newProgress", "newProgress-->" + newProgress);
                if (mDialog == null) {
                    return;
                }
                if (newProgress == 100) {
                    mDialog.dismiss();
                    setRefresh(false);
                }
                super.onProgressChanged(view, newProgress);
            }

        });
//
////        if (Build.VERSION.SDK_INT >= 19) {
////            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
////        }
//
        mWebView.loadUrl(url);
//        mWebView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                Log.d("Loading", "Loading");
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                Log.d("onPageFinished", "onPageFinished");
//            }
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//                Log.d("onReceivedSslError", "onReceivedSslError");
//            }
//        });
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        loadUrl(mUrl+SPUitl.getToken());
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
