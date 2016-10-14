package com.xiaopeng.xmapnavi.presenter.callback;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviLocation;

/**
 * Created by linzx on 2016/10/12.
 */
public interface XpLocationListener {
    void onLocationChanged(AMapLocation aMapLocation);
    void onLocationChange(AMapNaviLocation location);

}
