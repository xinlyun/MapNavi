package com.xiaopeng.xmapnavi.presenter.callback;

import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.lib.utils.utils.LogUtils;

/**
 * Created by linzx on 2016/12/24.
 */

public interface XpAimNaviMsgListener {
    void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo info);

    void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo);
}
