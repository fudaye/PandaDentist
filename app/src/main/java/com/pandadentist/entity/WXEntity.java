package com.pandadentist.entity;

/**
 * Created by Ford on 2017/5/27.
 */

public class WXEntity {

    /**
     * code : 0
     * message : success
     * token : 58bb74618fe6735e8f4344179eb776d12c85f0d41439f2ee098fab57084b1316b5349a664ee2524348719dcd817865e466366854fd37f801b1b4a7728e654b08
     */

    private int code;
    private String message;
    private String token;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
