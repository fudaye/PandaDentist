package com.pandadentist.config;

import android.os.Environment;

/**
 * Created by Ford on 2016/9/19.
 */
public interface Constants {

    /**
     * 服务器返回状态码
     */
    int SUCCESS = 0;
    //登录过期
    int NOLOGIN = 2001;
    //修改密码时旧密码错误
    int OLDPWD_ERROR= 2004;
    //登陆时  账号密码有误
    int LOGIN_ERROR= 2000;


    String CACHE_ROOT_DIR_NAME = "jt";
    String DISK_HEAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CACHE_ROOT_DIR_NAME + "/";

    interface BUNDLE_KEY {
        String VALUE = "value";
        String VALUE1 = "value1";
    }

    /**
     * activity result 类型
     */
    int ACTIVITY_FOR_RESULT_REQUEST_CODE_SELECT_CONTACT = 0x10;//选择联系人
    int ACTIVITY_FOR_RESULT_REQUEST_CODE_PHOTO = 0x11;//选择拍照
    int ACTIVITY_FOR_RESULT_REQUEST_CODE_ALBUM = 0x12;//选择相册
    int ACTIVITY_FOR_RESULT_REQUEST_CODE_ATTACHMENT = 0X13;//附件管理
    int ACTIVITY_FOR_RESULT_REQUEST_CODE_SELECT_DEVICE = 0x14;//选择蓝牙设备



    /**
     * 广播常量
     * */
    String  BROADCAST_FLAG_REFRESH_MESSAGE = "com.jt.broadcast.refresh";
    String  BROADCAST_FLAG_CODE_MESSAGE = "com.jt.broadcast.code";

    String AAAA = "a2a52f9e25c0a4dbcbdbabd529becd71";

}
