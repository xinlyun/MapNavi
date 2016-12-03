package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Fragment;
import android.os.Bundle;

import com.amap.api.maps.MapView;
import com.amap.api.services.core.LatLonPoint;

/**
 * Created by linzx on 2016/11/9.
 */
public interface BaseFuncActivityInteface {
    void exitFragment();
    void shouldFinish();
    void startAcitivity(Class<?> cls, Bundle bundle);
    void startFragment(Fragment fragment);
    void startFragment(Class<?> cls);
    MapView getMapView();
    void requestNaviCalue(LatLonPoint fromPoint, LatLonPoint toPoint);
}
