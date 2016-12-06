package com.xiaopeng.xmapnavi.mode;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.lib.utils.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/12/5.
 */

public class FindForWardPoi extends Thread{
    private static final String TAG = "FindForWardPoi";
    private AMapNaviPath myPath;
    private double maxDis = 0;
    private int index = 0;
    private NaviLatLng maxLatLng;
    private NaviLatLng repikLatLng;
    private OnFindRightPoiInPath listener;

    private double minDis  = 100;
    private List<AMapNaviPath> otherPaths = new ArrayList<>();
    private List<NaviLatLng> allNaviLatLng = new ArrayList<>();
    private List<NaviLatLng> myAllNaviLatlng = new ArrayList<>();

    public FindForWardPoi(AMapNaviPath myPath,List<AMapNaviPath> other){
        this.myPath = myPath;
        this.otherPaths.clear();
        this.otherPaths.addAll(other);
    }
    public void setOnFindPoiInPathListener(OnFindRightPoiInPath listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        super.run();
        LogUtils.d(TAG,"begin to do:"+System.currentTimeMillis());
        for (AMapNaviPath path:otherPaths){
            allNaviLatLng.addAll(path.getCoordList());
        }
        List<NaviLatLng> myNaviLatlngs = myPath.getCoordList();
        for (int i = 0;i<myNaviLatlngs.size();i++){
            NaviLatLng myNaviLatlng = myNaviLatlngs.get(i);
            minDis = 100;
            NaviLatLng trueLatLng = allNaviLatLng.get(0);
            for (NaviLatLng otherLatlng:allNaviLatLng){
                double disX = myNaviLatlng.getLatitude() - otherLatlng.getLatitude();
                double disY = myNaviLatlng.getLongitude() - otherLatlng.getLongitude();
                double allDis = disX * disX + disY * disY;
                if (allDis<minDis){
                    minDis = allDis;
                    trueLatLng = new NaviLatLng(otherLatlng.getLatitude(),otherLatlng.getLongitude());
                }
            }
            if (minDis >= maxDis){
                maxDis = minDis;
                index = i;
                maxLatLng = myNaviLatlng;
                repikLatLng = trueLatLng;
            }
        }


//        Bundle bundle = new Bundle();
//        bundle.putDouble("poi0lat",maxLatLng.getLatitude());
//        bundle.putDouble("poi0lon",maxLatLng.getLongitude());
//        bundle.putDouble("poi1lat",repikLatLng.getLatitude());
//        bundle.putDouble("poi1lon",repikLatLng.getLatitude());
//        bundle.putInt("index",index);
        Message message = handler.obtainMessage();
//        message.obj = bundle;
        message.what = 0;
        handler.sendMessage(message);
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Bundle bundle = (Bundle) msg.obj;
//            NaviLatLng myNavi = new NaviLatLng(bundle.getDouble("poi0lat"),bundle.getDouble("poi0lon"));
//            NaviLatLng otherNavi = new NaviLatLng(bundle.getDouble("poi1lat"),bundle.getDouble("poi1lon"));

            LogUtils.d(TAG,"find the poi:\nindex:"+index+"\npoi:"+maxLatLng+"\nendTime:"+System.currentTimeMillis());

            if (listener!=null){
                listener.OntheRightPoi(index,maxLatLng,repikLatLng);
            }
        }
    };


    public interface OnFindRightPoiInPath{
        void OntheRightPoi(int index ,NaviLatLng myLatLng,NaviLatLng reLatLng);
    }

}
