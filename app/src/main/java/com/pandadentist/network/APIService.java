package com.pandadentist.network;


import com.pandadentist.entity.DeviceListEntity;
import com.pandadentist.entity.WXEntity;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Ford on 2016/7/6.
 */
public interface APIService {

    /**
     * 微信登录获得token
     */
    @FormUrlEncoded
    @POST("wx/2/user/wxauth")
    Observable<WXEntity> getWXToken(@Field("code") String code, @Field("aaaa") String aaaa);


    /**
     * 绑定设备
     */
    @FormUrlEncoded
    @POST("wx/2/device/bind")
    Observable<WXEntity> bindDevice(@Field("deviceid") String deviceid, @Field("token") String token);

    /**
     * 邮箱注册
     */
    @FormUrlEncoded
    @POST("wx/2/user/register")
    Observable<WXEntity> emailRegister(@Field("username") String username, @Field("password") String password,@Field("openid") String openid,@Field("aaaa") String aaaa);


    /**
     * 邮箱登录
     */
    @FormUrlEncoded
    @POST("wx/2/user/login")
    Observable<WXEntity> loginForEmail(@Field("username") String username, @Field("password") String password,@Field("aaaa") String aaaa);

    /**
     * 获得绑定设备列表
     */
    @FormUrlEncoded
    @POST("wx/2/device/list")
    Observable<DeviceListEntity> getDeviceList(@Field("token") String token);

    /**
     * 上传蓝牙数据
     */
    @FormUrlEncoded
    @POST("/bleprocess/sync")
    Observable<WXEntity> uploadData(@Field("deviceid") String deviceid,
                                    @Field("software") String software,
                                    @Field("factory") String factory,//厂家
                                    @Field("model") String model,//型号
                                    @Field("power") String power,
                                    @Field("time") String time,
                                    @Field("hardware") String hardware,
                                    @Field("content")  String content,
                                    @Field("dataType") String dataType
                                    );
}
