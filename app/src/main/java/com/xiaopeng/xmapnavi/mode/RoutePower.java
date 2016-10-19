package com.xiaopeng.xmapnavi.mode;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;

import com.amap.api.location.AMapLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.xmapnavi.presenter.IRoutePower;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/18.
 */
public class RoutePower implements IRoutePower{
    HashMap<Integer,AMapNaviPath> mPaths;
    int[] ints ;
    private static final int UPDATE_INFO = 0 , MSG_CALL_BACK = 1;
    private XpRouteListener mListner;
//    = new ArrayList<>();
    private AMapLocation mLocation;
    private int pointLin;
    private int tag;
    private boolean isStart = false;
    private long timePre;
    private boolean isFirst;
    @Override
    public void setPath(HashMap<Integer,AMapNaviPath> paths,int[] ints) {
        isFirst = true;
        mPaths = paths;
        this.ints = ints;
        tag = (int) System.currentTimeMillis();
        if (isStart && mLocation != null){
            reciveMsg.sendEmptyMessage(UPDATE_INFO);
            timePre = System.currentTimeMillis();
        }
    }

    @Override
    public void setCurretPosi(AMapLocation location) {
        mLocation = location;

    }

    @Override
    public void startRoute() {

        if (mPaths!=null && mLocation !=null) {
//            new FindNeastPosi(tag, mPaths, mLocation, ints).start();
            if (!isStart) {
                reciveMsg.sendEmptyMessage(UPDATE_INFO);
            }
            timePre = System.currentTimeMillis();
        }
        isStart = true;
    }

    @Override
    public void stopRoute() {
        isStart = false;
    }

    @Override
    public void setXpRouteListner(XpRouteListener listner) {
        mListner = listner;
    }

    class FindNeastPosi extends Thread{
        private int pathNum , stepNum,linkNum,poiNum;
        private int tag;
        private HashMap<Integer,AMapNaviPath> mPaths = new HashMap<Integer,AMapNaviPath>();
        private AMapLocation mLocation;
        private double dis = 100;
        private int[] ints;
        FindNeastPosi(int tag , HashMap<Integer,AMapNaviPath> paths,AMapLocation aMapLocation,int[] ints){
            this.tag = tag;
            mPaths.putAll(paths);
            mLocation = aMapLocation;
            this.ints = ints.clone();
        }

        @Override
        public void run() {
            super.run();
            for (int a = 0;a < ints.length;a++){
                AMapNaviPath path = mPaths.get(ints[a]);
                for (int b = 0;b < path.getSteps().size();b++){
                    AMapNaviStep step =  path.getSteps().get(b);
                    List<NaviLatLng> naviLatLngs = step.getCoords();
                    double newdis;
                    for (int c = 0;c < naviLatLngs.size(); c++){
                        if (( newdis = distance(mLocation,naviLatLngs.get(c))) < dis){
                            dis = newdis;
                            pathNum = ints[a];
                            stepNum = b;
                            poiNum = c;
                        }
                    }
                }
            }
            Message message = reciveMsg.obtainMessage();
            message.what = MSG_CALL_BACK;
            message.arg1 = tag;
            int[] msgNum = new int[]{pathNum,stepNum,poiNum};
            message.obj = msgNum;
            reciveMsg.sendMessage(message);
        }
    }
    private int oldPathId = -1;
    Handler reciveMsg = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_INFO:
                    if (isStart && mPaths!=null && mLocation != null){
                        new FindNeastPosi(tag,mPaths,mLocation,ints).start();
                        timePre = System.currentTimeMillis();
                        reciveMsg.sendEmptyMessageDelayed(UPDATE_INFO, 5 * 1000);
                    }
                    break;

                case MSG_CALL_BACK:
                    int newTag = msg.arg1;
                    if (newTag == tag){
                        int[] rightMsg = (int[]) msg.obj;
                        if (isFirst || oldPathId != rightMsg[0]) {
                            if (mListner != null) {
                                mListner.nearBy(rightMsg[0], rightMsg[1], rightMsg[2]);
                            }
                            oldPathId = rightMsg[0];
                            isFirst = false;
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public static double distance(AMapLocation aMapLocation,NaviLatLng naviLatLng){
        double xDis = aMapLocation.getLatitude() - naviLatLng.getLatitude();
        double yDis = aMapLocation.getLongitude() - naviLatLng.getLongitude();
        double num = xDis * xDis + yDis * yDis;
        return num;
    }



}
