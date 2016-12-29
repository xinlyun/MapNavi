package com.xiaopeng.xmapnavi.presenter;

import com.xiaopeng.xmapnavi.bean.PowerPoint;

import java.util.List;

/**
 * Created by linzx on 2016/12/29.
 */

public interface IPowerPoiDateHelper {
    void getPowerPoiSave();
    List<PowerPoint> getPowerPointById(String poiId);
    void deletPowerPointById(String poiId);
}
