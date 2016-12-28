package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.amap.api.location.AMapLocation;

/**
 * Created by linzx on 2016/12/28.
 */

public interface IMusicPoiProvider {
    public static final String ACTION_BROADCAST_LOCATION_PROVINCE = "com.xiaopeng.broadcast.LOCATION_PROVINCE";
    public static final String LOCATION_PROVINCE_KEY = "province";
    public static final String LOCATION_CITY_KEY="city";

    void init(Context context);
    void sendProvide(AMapLocation aMapLocation);

}
