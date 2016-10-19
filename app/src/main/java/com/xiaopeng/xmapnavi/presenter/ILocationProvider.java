package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;

import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/12.
 */
public interface ILocationProvider {

    void addLocationListener(XpLocationListener xpLocationListener);
    void removeLocationListener(XpLocationListener xpLocationListener);
    void addSearchListner(XpSearchListner xpSearchListner);
    void removeSearchListner(XpSearchListner xpSearchListner);

    void addNaviCalueListner(XpNaviCalueListener xpSearchListner);
    void removeNaviCalueListner(XpNaviCalueListener xpSearchListner);

    void addRouteListener(XpRouteListener xpRouteListener);
    void removeRouteListener(XpRouteListener xpRouteListener);

    void addNaviInfoListner(XpNaviInfoListener xpNaviInfoListener);
    void removeNaviInfoListener(XpNaviInfoListener xpNaviInfoListener);

    void trySearchPosi(String str);
    void calueRunWay(List<NaviLatLng> startList, List<NaviLatLng> wayList, List<NaviLatLng> endList);
    AMapNaviPath getNaviPath();
    HashMap<Integer,AMapNaviPath> getNaviPaths();
    AMapLocation getAmapLocation();
    PoiResult getPoiResult();
    PoiItem getPoiItem();
    void selectRouteId(int id);
    boolean startNavi(int var1);
    void stopNavi();
    void startRouteNavi();
    void stopRouteNavi();

}
