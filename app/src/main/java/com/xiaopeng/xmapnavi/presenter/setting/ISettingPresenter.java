package com.xiaopeng.xmapnavi.presenter.setting;

import android.text.BoringLayout;

import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.WherePoi;

import java.util.List;

/**
 * Created by linzx on 2016/11/21.
 */

public interface ISettingPresenter {
    public static final int NEW_CUSTOM = 0,AV_COST = 1;
    void setSettingListener(SettingListener listener);
    void setNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed);
    void setBroadCastChange(int style);
    void setMixNaviBroadChange(boolean lukuang,boolean dianzi,boolean anquan);

    void getCollectItem();
    void getWhereItem();



    public interface SettingListener{
        void naviStyleChange(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed);
        void broadCastChange(int style);
        void mixNaviBroadChange(boolean lukuang,boolean dianzi,boolean anquan);
        void changeWhereItem(List<WherePoi> poiList);
        void onCollectItem(List<CollectItem> items);
    }
}
