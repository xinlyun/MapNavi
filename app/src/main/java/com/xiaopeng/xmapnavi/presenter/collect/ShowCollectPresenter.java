package com.xiaopeng.xmapnavi.presenter.collect;

import android.content.Context;

import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ICollectDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/8.
 */
public class ShowCollectPresenter implements IShowCollectPresenter ,XpCollectListener{
    private Context mContext;
    private XpCollectListener mListener;
    private ILocationProvider mProvider;
    private ICollectDateHelper mDateHelper;
    public ShowCollectPresenter(Context context){
        mContext = context;

    }

    public void init(){
        mProvider = LocationProvider.getInstence(mContext);
        mDateHelper = new DateHelper();
        mDateHelper.setOnCollectListener(this);
    }
    public void release(){
        mDateHelper.setOnCollectListener(null);
        mDateHelper = null;
        mProvider = null;
    }


    @Override
    public void getCollect() {
        mDateHelper.getCollectItems();
    }

    @Override
    public void setOnCollectListener(XpCollectListener listener) {
        mListener = listener;
    }

    @Override
    public boolean startNavi(CollectItem item) {
        List<NaviLatLng> naviLatLngs = new ArrayList<>();
        naviLatLngs.add(new NaviLatLng(item.posLat,item.posLon));
        return mProvider.tryCalueRunWay(naviLatLngs);
    }

    @Override
    public void onCollectCallBack(List<CollectItem> collectItems) {
        mListener.onCollectCallBack(collectItems);
    }
}
