package com.pandadentist.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.pandadentist.R;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;

import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by fudaye on 2017/8/3.
 * 语言选择
 */

public class LanguageSwitchActivity extends SwipeRefreshBaseActivity {

    @Bind(R.id.iv1)
    ImageView iv1;
    @Bind(R.id.iv2)
    ImageView iv2;
    @Bind(R.id.iv3)
    ImageView iv3;

    private int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getText(R.string.LanguageSwitch));
        mToolbarFuncTv.setText(getResources().getText(R.string.save));
        mToolbarFuncTv.setTextColor(Color.BLACK);
        mToolbarFuncRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Resources resources = getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                Configuration config = resources.getConfiguration();
                // 应用用户选择语言
                if(type == 1){
                    config.locale = Locale.getDefault();
                }else if (type == 2){
                    config.locale = Locale.CHINESE;
                }else if(type == 3) {
                    config.locale = Locale.ENGLISH;
                }
                resources.updateConfiguration(config, dm);
                Intent intent1 = new Intent(LanguageSwitchActivity.this, UrlDetailActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
            }
        });
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (config.locale.getLanguage().equals(Locale.ENGLISH.getLanguage()) ) {
            iv1.setVisibility(View.GONE);
            iv2.setVisibility(View.GONE);
            iv3.setVisibility(View.VISIBLE);
            type = 3;
        }  else if (config.locale.getLanguage().equals(Locale.getDefault().getLanguage()) ) {
            type = 1;
            iv1.setVisibility(View.VISIBLE);
            iv2.setVisibility(View.GONE);
            iv3.setVisibility(View.GONE);
        }else if (config.locale.getLanguage().equals(Locale.CHINESE.getLanguage()) ) {
            type = 2;
            iv1.setVisibility(View.GONE);
            iv2.setVisibility(View.VISIBLE);
            iv3.setVisibility(View.GONE);
        }
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_language_switch;
    }


    @OnClick({R.id.rl_default, R.id.rl_chinese, R.id.rl_english})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_default:
                type = 1;
                iv1.setVisibility(View.VISIBLE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.GONE);
                break;
            case R.id.rl_chinese:
                type = 2 ;
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.VISIBLE);
                iv3.setVisibility(View.GONE);
                break;
            case R.id.rl_english:
                type = 3;
                iv1.setVisibility(View.GONE);
                iv2.setVisibility(View.GONE);
                iv3.setVisibility(View.VISIBLE);
                break;
        }
    }
}
