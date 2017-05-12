package com.pandadentist.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.pandadentist.App;
import com.pandadentist.config.Constants;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;


import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ford on 2016/5/26 0026.
 *
 * app 帮助类
 */
public class AppHelper {

    private static final String TAG = AppHelper.class.getSimpleName();

    public static String getDeviceId() {
//        TelephonyManager tm = (TelephonyManager) App.sContext.getSystemService(Context.TELEPHONY_SERVICE);
//        if (TextUtils.isEmpty(tm.getDeviceId())) {
//            return "";
//        } else {
//            return tm.getDeviceId();
//        }
        return SPUitl.getChannelId();
    }
    //设备类型android为3    ios为4
    public static String getDeviceType(){
        return "3";
    }

    public static HashMap<String, String> enCodeParam(HashMap<String, String> param) {
        HashMap<String, String> p = new HashMap<>();
        p.put("data", AESUtils.encAESCode(new Gson().toJson(param)));
        p.put("deviceToken", getDeviceId());
        p.put("version", getVersionName());
        return p;
    }

    public static String enCodeParamForRetrofit(HashMap<String, String> param) {
        String s = new Gson().toJson(param);
        Log.d("加密前参数", "param->" + s);
        return AESUtils.encAESCode(s);
    }

    public static String getVersionName() {
        // 获取packagemanager的实例
        String version = "";
        try {
            PackageManager packageManager = App.sContext.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo;
            packInfo = packageManager.getPackageInfo(App.sContext.getPackageName(), 0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return version;
        }
        return version;
    }

    public static int getVersionCode() {
        // 获取packagemanager的实例
        int version = 0;
        try {
            PackageManager packageManager = App.sContext.getPackageManager();
            // getPackageName()是你当前类的包名，0代表是获取版本信息
            PackageInfo packInfo;
            packInfo = packageManager.getPackageInfo(App.sContext.getPackageName(), 0);
            version = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return version;
        }
        return version;
    }

//    public static void sendBroadcastRefresh() {
//        Intent intent = new Intent();
//        intent.setAction(Constants.BROADCAST_KEY.BROADCAST_RECEIVER_REFRESH);
//        App.getContext().sendBroadcast(intent);
//    }


    public static void clearGlideCache(Context context) {
        Glide.get(context).clearMemory();
        new Thread(() -> {
            Glide.get(context).clearDiskCache();
        });
    }


    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public static String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }

    public static void installApp(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void clearLocalData() {
        SPUitl.clear();
        File file = new File(Constants.DISK_HEAD_PATH);
        if (file.exists()) {
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
        }
    }

    /**
     * 获得日期以月和日 显示
     * 例 08-21
     */
    public static String getDateForMD(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(str);
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd");
            return sdf1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获得日期
     * 以年月日显示
     * 例 2012-02-02
     */
    public static String getDateForYMD(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(str);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            return sdf1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获得日期
     * 以年月日时分显示
     * 例 2012-02-02 02:13
     */
    public static String getDateForYMDhhmm(String str) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(str);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd ");
            return sdf1.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 打电话
     */
    public static void callPhone(String phoneNumber) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        App.sContext.startActivity(intent);
    }

    public static SpannableString setTextColor(String str, int color, int startIndex, int endIndex) {
        return setTextColor(str, color, startIndex, endIndex, 0, false);
    }

    public static SpannableString setTextColor(String str, int color, int startIndex, int endIndex, int textSize) {
        return setTextColor(str, color, startIndex, endIndex, textSize, true);
    }

    /**
     * 设置文本局部颜色和字体大小
     */
    private static SpannableString setTextColor(String str, int color, int startIndex, int endIndex, int textSize, boolean textFlag) {
        SpannableString ss = new SpannableString(str);
        ss.setSpan(new ForegroundColorSpan(App.sContext.getResources().getColor(color)), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (textFlag) {
            ss.setSpan(new AbsoluteSizeSpan(textSize, true), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /**
     * 计算两个日期之间的天数
     */
    public static int daysBetween(String smdate, String bdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(smdate));
            long time1 = cal.getTimeInMillis();
            cal.setTime(sdf.parse(bdate));
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean isPhoneNum(String str) {
        String s = String.valueOf(str.charAt(0));
        return "1".equals(s) && str.length() == 11;
    }

    public static boolean isOpenGPS(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    /**
     * 隐藏电话号码中间4位
     */
    public static String hidePhoneNum(String phoneNum) {
        return phoneNum.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 是否包含数字和字母
     */
    public static boolean isContainNumWord(String str) {
        String strRule = "(?!^(\\d+|[a-zA-Z]+|[~!@#$%^&*?]+)$)^[\\w~!@#$%\\^&*?]+$";
        return Pattern.compile(strRule).matcher(str).find();
    }

    /**
     * 判断是否是正确的email地址
     *
     * @param email
     * @return
     */
    public static boolean isEmailAddr(String email) {
        String check = "[\\.\\w]{1,}[@]\\w+[.]\\w+";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        return matcher.matches();
    }
}
