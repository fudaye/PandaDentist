package com.pandadentist.entity;

/**
 * Created by Ford on 2017/5/27.
 */

public class WXEntity {


    /**
     * code : 0
     * info : {"birthday":"","brushday":0,"createTime":1497914885210,"dentaltype":"0","icon":"http://wx.qlogo.cn/mmopen/ajNVdqHZLLDzmewcsajD9NJj50L8hFUwd2Hfic9pS9da57a594I0LS2I1oSaNL6z3QK1fiaHBmuXbbQmsQ2ZILEA/0","name":"Ford","openid":"oLbYswsh8SjoQUDETBG01M-MCrWA","sex":1,"unionid":"oicB_xA5dbwwmySyYMwl2A1kFiQM","userid":-1}
     * message : success
     * token : c0b286bf37db4db35dd570ecfac0d018457c5cdd9800b2f24217e79a25b69dfa0beab2c0
     */

    private int code;
    private InfoBean info;
    private String message;
    private String token;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public InfoBean getInfo() {
        return info;
    }

    public void setInfo(InfoBean info) {
        this.info = info;
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

    public static class InfoBean {
        /**
         * birthday :
         * brushday : 0
         * createTime : 1497914885210
         * dentaltype : 0
         * icon : http://wx.qlogo.cn/mmopen/ajNVdqHZLLDzmewcsajD9NJj50L8hFUwd2Hfic9pS9da57a594I0LS2I1oSaNL6z3QK1fiaHBmuXbbQmsQ2ZILEA/0
         * name : Ford
         * openid : oLbYswsh8SjoQUDETBG01M-MCrWA
         * sex : 1
         * unionid : oicB_xA5dbwwmySyYMwl2A1kFiQM
         * userid : -1
         */

        private String birthday;
        private int brushday;
        private long createTime;
        private String dentaltype;
        private String icon;
        private String name;
        private String openid;
        private int sex;
        private String unionid;
        private int userid;

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public int getBrushday() {
            return brushday;
        }

        public void setBrushday(int brushday) {
            this.brushday = brushday;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public String getDentaltype() {
            return dentaltype;
        }

        public void setDentaltype(String dentaltype) {
            this.dentaltype = dentaltype;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getUnionid() {
            return unionid;
        }

        public void setUnionid(String unionid) {
            this.unionid = unionid;
        }

        public int getUserid() {
            return userid;
        }

        public void setUserid(int userid) {
            this.userid = userid;
        }
    }
}
