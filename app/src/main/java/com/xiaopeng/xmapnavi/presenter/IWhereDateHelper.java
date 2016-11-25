package com.xiaopeng.xmapnavi.presenter;

import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;

/**
 * Created by linzx on 2016/11/18.
 */

public interface IWhereDateHelper {
    void setOnWhereListener(XpWhereListener listener);
    void getWhereItems();
    void saveWhereIten(int type,String name,String desc,double poiLat,double poiLon);
}
