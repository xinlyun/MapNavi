package com.xiaopeng.xmapnavi.mode;

import android.content.Context;

import com.amap.api.navi.model.NaviInfo;

/**
 * Created by linzx on 2016/11/22.
 */

public interface ISendNaviBroad {
    static final String ACTION_NAVI = "AUTONAVI_STANDARD_BROADCAST_SEND";
    void initBroad(Context context);
    void sendNaviMsg(NaviInfo naviInfo);
    void stopNavi();

}
