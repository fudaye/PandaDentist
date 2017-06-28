package com.pandadentist.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pandadentist.R;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.Toasts;
import com.pandadentist.widget.X5ObserWebView;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import butterknife.Bind;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.pandadentist.R.id.textView3;

/**
 * Created by Ford on 2016/10/14.
 */
public class UrlDetailActivity extends SwipeRefreshBaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = UrlDetailActivity.class.getSimpleName();

    @Bind(R.id.iv_hint)
    ImageView mivHint;
    @Bind(R.id.wv)
    X5ObserWebView mWebView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;


    private String mUrl = "http://www.easylinkage.cn/webapp2/analysis/today?id=361&index=0&r=0.4784193145863376&token=";
    private ProgressDialog mDialog;
    private CircleImageView headerIv;
    private TextView usernameTv;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.app_name));
        mToolBackRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mTitleBackIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_main_personal));
        mToolbarFuncIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_device));
        mToolbarFuncRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.gotoAddDeviceActivity(UrlDetailActivity.this);
//                IntentHelper.gotoCapture(UrlDetailActivity.this);
//                IntentHelper.gotoWifiConfig(UrlDetailActivity.this);
            }
        });
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        View headerView = navigationView.getHeaderView(0);
        headerIv = (CircleImageView) findViewById(R.id.imageView);
        usernameTv = (TextView) findViewById(textView3);
        mWebView.setOnScrollChangedCallback(new X5ObserWebView.OnScrollChangedCallback() {
            public void onScroll(int l, int t) {
//                Log.d(TAG, "We Scrolled etc..." + l + " t =" + t);
                if (t == 0) {//webView在顶部
                    mSwipeRefreshLayout.setEnabled(true);
                } else {//webView不是顶部
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }
        });
        if (SPUitl.isLogin()) {
            mDialog = new ProgressDialog(this);
            mDialog.setMessage("加载中....");
            mivHint.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            loadUrl(mUrl + SPUitl.getToken());
            WXEntity wxEntity = SPUitl.getWXUser();
            if (wxEntity != null) {
                Glide.with(this).load(wxEntity.getInfo().getIcon()).into(headerIv);
                usernameTv.setText(wxEntity.getInfo().getName());
            }
        } else {
            mivHint.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }


    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_main;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void loadUrl(String url) {
        mWebView.setWebViewClient(new WebViewClient() {
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
        mWebView.loadUrl(url);
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        loadUrl(mUrl + SPUitl.getToken());
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_brush_teeth_record:
            case R.id.nav_member_point:
            case R.id.nav_panda_store:
            case R.id.nav_typeface:
            case R.id.nav_wx_friend:
                Toasts.showShort("功能暂未开放");
                break;
            case R.id.nav_logout:
                SPUitl.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick({R.id.ll_member_point, R.id.ll_panda_store, R.id.ll_typeface, R.id.ll_wx_friend, R.id.btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_member_point:
            case R.id.ll_panda_store:
            case R.id.ll_typeface:
            case R.id.ll_wx_friend:
                Toasts.showShort("功能暂未开放");
                break;
            case R.id.btn:
                SPUitl.clear();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
