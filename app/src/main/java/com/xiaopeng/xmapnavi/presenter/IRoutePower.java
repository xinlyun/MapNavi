package com.xiaopeng.xmapnavi.presenter;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;

import java.util.HashMap;
import java.util.List;
/**
 * Created by linzx on 2016/10/18.
 */
public interface IRoutePower {
    void setPath(HashMap<Integer,AMapNaviPath> paths,int[] ints);
    void setCurretPosi(AMapLocation location);
    void startRoute();
    void stopRoute();
    void setXpRouteListner(XpRouteListener listner);

}
