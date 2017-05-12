package com.pandadentist.util;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.pandadentist.R;


/**
 * Created by Ford on 2016/8/4.
 */
public class AnimationUtil {

    public static void rotateUpView(Context context , View v){
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.rotate_up_anim);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }

    public static void rotateDownView(Context context , View v){
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.rotate_down_anim);
        anim.setFillAfter(true);
        v.startAnimation(anim);
    }
}
