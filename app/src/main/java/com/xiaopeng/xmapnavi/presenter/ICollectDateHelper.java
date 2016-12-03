package com.xiaopeng.xmapnavi.presenter;

import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;

/**
 * Created by linzx on 2016/11/8.
 */
public interface ICollectDateHelper {
    void setOnCollectListener(XpCollectListener listener);
    void getCollectItems();
    void saveCollect(String name,String desc,double poiLat,double poiLon);
    CollectItem getCollectByPoi(double lat,double lon);
    CollectItem getCollectByName(String name);
}
