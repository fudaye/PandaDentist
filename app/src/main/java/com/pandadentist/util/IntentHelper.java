package com.pandadentist.util;


import android.content.Context;
import android.content.Intent;


import com.pandadentist.ui.activity.LoginActivity;


/**
 * Created by Ford on 2016/9/18.
 * <p>
 * 帮助类
 */
public class IntentHelper {


    public static void gotoLogin(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

}
