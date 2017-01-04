package com.xiaopeng.xmapnavi.bean;

/**
 * Created by linzx on 2017/1/4.
 */

public class Licen{
    private  String device_id;
    private  String licence;
    private  String ticket;
    public Licen(String device_id, String licence, String ticket){
        this.device_id = device_id;
        this.licence = licence;
        this.ticket = ticket;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getLicence() {
        return licence;
    }

    public String getTicket() {
        return ticket;
    }
}