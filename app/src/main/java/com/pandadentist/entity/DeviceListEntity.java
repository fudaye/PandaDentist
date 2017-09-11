package com.pandadentist.entity;

import java.util.List;

/**
 * Created by fudaye on 2017/9/2.
 */

public class DeviceListEntity
{
    /**
     * code : 0
     * devices : [{"deviceid":"F0FE6B3D915C","icon":"http://wx.qlogo.cn/mmopen/huaoCxZN8lz3UssWicrhhZiabr4ujTkmllicsArjbRxYeiarI5E1LEd26LjkWWYDZwl2HpojYdEbJJhAlB95vjLSIg/0","userid":9,"username":"代码东"},{"deviceid":"F0FE6B3D8FA7","icon":"http://wx.qlogo.cn/mmopen/ajNVdqHZLLCe5364Szd5icbZcE46mYbg8smia4BXFPSYHGuicsibkMmFsDtQ9818MZqqdQ473ZqdSKzc0pAFFv3F7A/0","userid":376,"username":"Ford"}]
     * message : success
     */

    private int code;
    private String message;
    private List<DevicesBean> devices;

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

    public List<DevicesBean> getDevices() {
        return devices;
    }

    public void setDevices(List<DevicesBean> devices) {
        this.devices = devices;
    }

    public static class DevicesBean {
        /**
         * deviceid : F0FE6B3D915C
         * icon : http://wx.qlogo.cn/mmopen/huaoCxZN8lz3UssWicrhhZiabr4ujTkmllicsArjbRxYeiarI5E1LEd26LjkWWYDZwl2HpojYdEbJJhAlB95vjLSIg/0
         * userid : 9
         * username : 代码东
         */

        private String deviceid;
        private String icon;
        private int userid;
        private String username;
        public boolean isConnected;

        public String getDeviceid() {
            return deviceid;
        }

        public void setDeviceid(String deviceid) {
            this.deviceid = deviceid;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getUserid() {
            return userid;
        }

        public void setUserid(int userid) {
            this.userid = userid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
