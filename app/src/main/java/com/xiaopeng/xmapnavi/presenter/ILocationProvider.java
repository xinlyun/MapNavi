package com.xiaopeng.xmapnavi.presenter;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;

/**
 * Created by linzx on 2016/10/12.
 */
public interface ILocationProvider {

    void addLocationListener(XpLocationListener xpLocationListener);
    void removeLocationListener(XpLocationListener xpLocationListener);
    void addSearchListner(XpSearchListner xpSearchListner);
    void removeSearchListner(XpSearchListner xpSearchListner);
    void trySearchPosi(String str);
    AMapLocation getAmapLocation();
    PoiResult getPoiResult();
    PoiItem getPoiItem();

}
