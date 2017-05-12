package com.pandadentist.util;


import android.content.Context;
import android.util.TypedValue;


/**
 * @author  Ford
 * */

public class DensityUtil {

    public static Context sContext;

    private DensityUtil() {
    }

    public static void register(Context context) {
        sContext = context.getApplicationContext();
    }

    private static void check() {
        if (sContext == null) {
            throw new NullPointerException(
                    "Must initial call DensityUtil.register(Context context) in your " +
                            "<? " +
                            "extends Application class>");
        }
    }

    public static float dp(int dip){
        check();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, sContext.getResources().getDisplayMetrics());
    }

    public static float px (int px){
        check();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px,sContext.getResources().getDisplayMetrics());
    }

    public static float sp (int sp){
        check();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, sContext.getResources().getDisplayMetrics());
    }

}