package com.xiaopeng.xmapnavi.bean;

/**
 * Created by xinlyun on 15-11-20.
 */

public class HistoryPosi{
    private String name;
    private float x,y;
    public HistoryPosi(String name, float x, float y){
        this.name = name;
        this.x = x;
        this.y = y;
    }
    public String getName() {
        return name;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
}