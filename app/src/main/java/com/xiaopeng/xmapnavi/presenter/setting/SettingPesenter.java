package com.xiaopeng.xmapnavi.presenter.setting;

import android.content.Context;

import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.WherePoi;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;

import java.util.List;

/**
 * Created by linzx on 2016/11/21.
 */

public class SettingPesenter implements ISettingPresenter {
    private SettingListener mListener;
    private Context mContext;
    private ILocationProvider mProvider;
    private int mBroadCastStyle = NEW_CUSTOM;
    private DateHelper mDateHelper;
    public SettingPesenter(Context context){
        mContext = context;
        mProvider = LocationProvider.getInstence(context);
        mDateHelper = new DateHelper();
        mDateHelper.setOnWhereListener(whereListener);
        mDateHelper.setOnCollectListener(collectListener);
    }


    @Override
    public void setSettingListener(SettingListener listener) {
        mListener = listener;
    }

    @Override
    public void setNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed) {
        mProvider.setNaviStyle(congestion,avHighSpeed,avCost,highSpeed);

    }

    @Override
    public void setBroadCastChange(int style) {
        mBroadCastStyle  = style;
        if (mListener!=null){
            mListener.broadCastChange(style);
        }
    }

    @Override
    public void setMixNaviBroadChange(boolean lukuang, boolean dianzi, boolean anquan) {
        if (mListener!=null){
            mListener.mixNaviBroadChange(lukuang,dianzi,anquan);
        }
    }

    @Override
    public void getCollectItem() {
        mDateHelper.getCollectItems();
    }

    @Override
    public void getWhereItem() {
        mDateHelper.getWhereItems();
    }

    private XpWhereListener whereListener = new XpWhereListener() {
        @Override
        public void onWhereCallBack(List<WherePoi> wherePois) {
            if (mListener!=null){
                mListener.changeWhereItem(wherePois);
            }
        }
    };

    private XpCollectListener collectListener = new XpCollectListener() {
        @Override
        public void onCollectCallBack(List<CollectItem> collectItems) {
            if (mListener!=null){
                mListener.onCollectItem(collectItems);
            }
        }
    };



}
