package com.pandadentist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.pandadentist.App;


/**
 * SharedPreferences工具类
 * Created by ld on 16/1/18.
 */
public class SPUitl {
    private static String CONFIG = "config";
    public static SharedPreferences sp;

    /**
     * 保存boolean类型的数据到config文件中
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveBooleanData(Context context, String key, boolean value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putBoolean(key, value).commit();
    }


    /**
     * 在config文件中获取boolean类型的数据
     *
     * @param context
     * @param key
     * @param defvalue
     * @return
     */
    public static boolean getBooleanData(Context context, String key, boolean defvalue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defvalue);
    }

    /**
     * 保存String类型的数据到config文件中
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveStringData(Context context, String key, String value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putString(key, value).commit();
    }


    /**
     * 在config文件中获取String类型的数据
     *
     * @param context
     * @param key
     * @param defvalue
     * @return
     */
    public static String getStringData(Context context, String key, String defvalue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getString(key, defvalue);
    }/**
     * 保存Int类型的数据到config文件中
     *
     * @param context
     * @param key
     * @param value
     */
    public static void saveIntData(Context context, String key, int value) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sp.edit().putInt(key,value).commit();
    }


    /**
     * 在config文件中获取String类型的数据
     *
     * @param context
     * @param key
     * @param defvalue
     * @return
     */
    public static int getIntData(Context context, String key, int defvalue) {
        if (sp == null) {
            sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sp.getInt(key, defvalue);
    }

    public static void saveSettingVibrate(boolean b){
        SharedPreferences s = App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE);
        s.edit().putBoolean("Vibrate",b).apply();
    }
    public static boolean getSettingVibrate(){
        return App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE).getBoolean("Vibrate",false);
    }


    public static void saveSettingAlarm(boolean b){
        SharedPreferences s = App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE);
        s.edit().putBoolean("Alarm",b).apply();
    }
    public static boolean getSettingAlarm(){
        return App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE).getBoolean("Alarm",false);
    }

    public static void saveSettingNotify(boolean b){
        SharedPreferences s = App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE);
        s.edit().putBoolean("Notify",b).apply();
    }
    public static boolean getSettingNotify(){
        return App.sContext.getSharedPreferences("setting",Context.MODE_PRIVATE).getBoolean("Notify",
                true);
    }

    public static void saveUser(String jsonStr){
        SharedPreferences s = App.sContext.getSharedPreferences("user",Context.MODE_PRIVATE);
        s.edit().putString("userStr",jsonStr).apply();
    }

    public static void saveChannelId(String channelId){
        SharedPreferences s = App.sContext.getSharedPreferences("channelId",Context.MODE_PRIVATE);
        s.edit().putString("channelId",channelId).apply();
    }

    public static String getChannelId(){
        return  App.sContext.getSharedPreferences("channelId",Context.MODE_PRIVATE).getString("channelId","");
    }

//    public static UserEntity getLocalUser(){
//        SharedPreferences s = App.sContext.getSharedPreferences("user",Context.MODE_PRIVATE);
//        String str = AESUtils.desAESCode(s.getString("userStr",""));
//        if(TextUtils.isEmpty(str)){
//            return null;
//        }else{
//            return new Gson().fromJson(str,UserEntity.class);
//        }
//    }

    public static void clear(){
        SharedPreferences s = App.sContext.getSharedPreferences("user",Context.MODE_PRIVATE);
        s.edit().clear().apply();
    }
}
