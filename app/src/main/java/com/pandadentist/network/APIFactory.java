package com.pandadentist.network;


import android.util.Log;

import com.pandadentist.config.AppConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ford on 2016/5/25 0025.
 */
public class APIFactory {
    private static final String TAG = APIFactory.class.getSimpleName();
    public <T> T create( Class c){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                Log.d(TAG,"message-->"+message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(logging);
        builder.connectTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
         return (T)retrofit.create(c);
    }

//    public BaiduApi createBaiduApi(){
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.connectTimeout(AppConfig.DEFAULT_TIMEOUT, TimeUnit.SECONDS);
//        Retrofit retrofit = new Retrofit.Builder()
//                .client(builder.build())
//                .baseUrl(WTSApi.BAIDU_API_BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .build();
//        return  retrofit.create(BaiduApi.class);
//    }
}
