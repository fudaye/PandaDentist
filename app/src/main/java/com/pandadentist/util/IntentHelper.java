package com.pandadentist.util;


import android.content.Context;
import android.content.Intent;


import com.pandadentist.ui.activity.AddDeviceActivity;
import com.pandadentist.ui.activity.LoginActivity;
import com.pandadentist.ui.activity.UrlDetailActivity;


/**
 * Created by Ford on 2016/9/18.
 * <p>
 * 帮助类
 */
public class IntentHelper {


    public static void gotoMain(Context context){
        Intent intent = new Intent(context, UrlDetailActivity.class);
        context.startActivity(intent);
    }

    public static void gotoLogin(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void gotoAddDevice(Context context){
        Intent intent = new Intent(context, AddDeviceActivity.class);
        context.startActivity(intent);
    }

}
