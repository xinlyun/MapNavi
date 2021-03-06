package com.xiaopeng.xmapnavi.presenter.callback;

import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;

import java.util.List;

/**
 * Created by linzx on 2016/10/19.
 */
public interface XpNaviInfoListener {
    void onNaviInfoUpdate(NaviInfo naviInfo);
    void showCross(AMapNaviCross aMapNaviCross) ;
    void hideCross();
    void showLaneInfo(AMapLaneInfo[] var1, byte[] var2, byte[] var3);
    void onNaviTrafficStatusUpdate(List<AMapTrafficStatus> date,int remainingDistance);
}
