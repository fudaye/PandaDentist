package com.pandadentist.util;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pandadentist.configwifi.android.WifiConfigActivity;
import com.pandadentist.ui.activity.AddDeviceActivity;
import com.pandadentist.ui.activity.ConnectWifiActivity;
import com.pandadentist.ui.activity.EmailRegisterActivity;
import com.pandadentist.ui.activity.LoadingActivity;
import com.pandadentist.ui.activity.LoginActivity;
import com.pandadentist.ui.activity.SmartLinkerWrapperActivity;
import com.pandadentist.ui.activity.UrlDetailActivity;


/**
 * Created by Ford on 2016/9/18.
 * <p>
 * 帮助类
 */
public class IntentHelper {

    private static final String TAG =  IntentHelper.class.getSimpleName();

    public static void gotoMain(Context context){

        Intent intent = new Intent(context, UrlDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void gotoLogin(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void gotoWifiConfig(Context context){
        Intent intent = new Intent(context, WifiConfigActivity.class);
        context.startActivity(intent);
    }
    public static void gotoConnectWifi(Context context){
        Intent intent = new Intent(context, ConnectWifiActivity.class);
        context.startActivity(intent);
    }

    public static void gotoAddDeviceActivity(Context context){
        Intent intent = new Intent(context, AddDeviceActivity.class);
        context.startActivity(intent);
    }

    /**gotoAddDeviceActivity
     * 以抓包的方式绑定设备
     *
     * */
    public static void gotoCapture(Context context){
        context.startActivity(new Intent(context, SmartLinkerWrapperActivity.class));
    }

    public static void gotoEmailRegister(Context context){
        context.startActivity(new Intent(context, EmailRegisterActivity.class));
    }

    public static void gotoloadingActivity(Context context,String mac){
        Intent intent =new Intent(context, LoadingActivity.class);
        intent.putExtra("mac",mac);
        context.startActivity(intent);
    }

}
