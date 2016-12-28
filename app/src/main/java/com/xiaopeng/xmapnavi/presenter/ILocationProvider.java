package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.xiaopeng.xmapnavi.presenter.callback.XpAimNaviMsgListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpSensorListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/12.
 */
public interface ILocationProvider {
    static final int REQUEST_INIT = 1;
    static final int REQUEST_UPDATE = 0 ;


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

    void addSensorListner(XpSensorListener xpSensorListener);
    void removeSensorListner(XpSensorListener xpSensorListener);

    void setOfflineMapListner(OfflineMapManager.OfflineMapDownloadListener listner);
    void setAiosListener(XpAiosMapListener xpAiosMapListener);

    void addAimNaviListener(XpAimNaviMsgListener listener);
    void removeAimNaviListener(XpAimNaviMsgListener listener);




    void trySearchPosi(String str);
    boolean tryAddWayPoiCalue(NaviLatLng wayPoi);
    void calueRunWay(List<NaviLatLng> startList, List<NaviLatLng> wayList, List<NaviLatLng> endList);
    boolean tryCalueRunWay(List<NaviLatLng> endList);
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
    void setNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed);
    boolean reCalueInNavi();
    void reCalue();
    int[] getPathsInts();
    NaviLatLng getNaviEndPoi();
    OfflineMapManager getOfflineMapManager();

    String getSearchStr();

    int getBroadCastMode();
    void setBroadCastMode(int mode);

    boolean getNaviLikeStyle(int num);

    void reCallLocation();

    void setAimState(int state);
    int getAimState();

    void muteLaught();
    void unmuteLaught();
    void muteSomeLaught();

    void getStubGroups(double lat,double lon);
    void getStubGroups(String city);
    NaviInfo getNaviInfo();
}
