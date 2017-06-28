package com.pandadentist.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.pandadentist.R;
import com.pandadentist.config.Constants;
import com.pandadentist.entity.WXEntity;
import com.pandadentist.network.APIFactory;
import com.pandadentist.network.APIService;
import com.pandadentist.ui.base.SwipeRefreshBaseActivity;
import com.pandadentist.util.IntentHelper;
import com.pandadentist.util.SPUitl;
import com.pandadentist.util.Toasts;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by fudaye on 2017/6/14.
 */

public class EmailRegisterActivity extends SwipeRefreshBaseActivity {

    private static final String TAG = EmailRegisterActivity.class.getSimpleName();

    @Bind(R.id.et_username)
    EditText etUsername;
    @Bind(R.id.et_pwd)
    EditText etPwd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolBarTtitle.setText(getResources().getString(R.string.emailRegister));
    }

    @Override
    public int providerLayoutId() {
        return R.layout.activity_email_register;
    }

    @OnClick(R.id.btn)
    public void onViewClicked() {
        String email = etUsername.getText().toString();
        String pwd = etPwd.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toasts.showShort("邮箱不能为空");
            return;
        }
        if(TextUtils.isEmpty(pwd)){
            Toasts.showShort("密码不能为空");
            return;
        }
        register(email,pwd);
    }


    private void  register (String username,String pwd){
        APIService api = new APIFactory().create(APIService.class);
        Subscription s = api.emailRegister(username,pwd,"",Constants.AAAA)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WXEntity>() {
                    @Override
                    public void call(WXEntity wxEntity) {
                        if (Constants.SUCCESS == wxEntity.getCode()) {
                            SPUitl.saveToken(wxEntity.getToken());
                            IntentHelper.gotoMain(EmailRegisterActivity.this);
                            finish();
                        } else {
                            Toasts.showShort("注册失败");
                            Log.d(TAG,"错误code ：" + wxEntity.getCode() + "错误信息：" + wxEntity.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("throwable", "throwable-->" + throwable.toString());
                    }
                });
        addSubscription(s);
    }
}
