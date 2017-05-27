package com.pandadentist.network;


import com.pandadentist.entity.WXEntity;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Ford on 2016/7/6.
 */
public interface APIService {

    @FormUrlEncoded
    @POST("wx/2/user/wxauth")
    Observable<WXEntity> getWXToken(@Field("code") String code, @Field("aaaa") String aaaa);


}
