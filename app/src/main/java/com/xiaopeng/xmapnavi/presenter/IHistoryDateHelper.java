package com.xiaopeng.xmapnavi.presenter;

import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.services.core.PoiItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;

/**
 * Created by linzx on 2016/10/17.
 */
public interface IHistoryDateHelper {
    void savePosiStr(String str);
    void savePoiItem(PoiItem item);
    void updateHisItem(HisItem hisItem);
    void saveGoAway(String path);
    void getHisItem(int count);
    void setHisDateListner(XpHisDateListner listner);
    void clearDate();
}
