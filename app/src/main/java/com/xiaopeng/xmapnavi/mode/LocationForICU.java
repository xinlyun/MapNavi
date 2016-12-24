package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.lib.scu.AbsCarControlCallback;
import com.xiaopeng.lib.scu.CarControlBox;
import com.xiaopeng.lib.scu.NcmControlBox;
import com.xiaopeng.lib.scu.ScuMailboxes;
import com.xiaopeng.xmapnavi.presenter.ILocationForICU;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpAimNaviMsgListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.AskDialogActivity;

/**
 * Created by linzx on 2016/12/24.
 */

public class LocationForICU implements ILocationForICU {
    private static final String MSG_INDEX ="MsgIndex";
    private static final String NOMAL_NAME = "NomalRoadName";
    private static final String CDU_REC = "CDUrecording";
    private static final String CDU_USB = "CDUUSBInsert";
    private static final String BLUE_MUSIC = "BluetoothMusic";
    private static final String NOMAL_LIMIT = "NomalSpeedLimit";
    private static final String NOTIFY = "notify";
    private static final String MSG_TYPE = "msgtype";
    private static final String ENABLE= "enable";


    Context context;
    private CarControlBox carControlBox;
    private NcmControlBox mSendControlBox;
    float lat,lot;
    private boolean isConnect = true;
    private int msgId = 0;
    public LocationForICU(Context context,NcmControlBox controlBox){
        this.context = context;
        ScuMailboxes scuMailboxes = ScuMailboxes.getInstance();
        scuMailboxes.setContext(context);
        mSendControlBox = controlBox;
        carControlBox       = scuMailboxes.getCarControlBox(new AbsCarControlCallback() {
        },null);
    }

    @Override
    public void beginToLocation(ILocationProvider locationProvider) {
        locationProvider.addLocationListener(mLocationListener);
        locationProvider.addAimNaviListener(mAimListener);
        locationProvider.addNaviInfoListner(mNaviInfoListener);
    }

    @Override
    public void readIntentInfo(Intent intent) {
        float posilat = intent.getFloatExtra("lat",0f);
        float posilot = intent.getFloatExtra("lot",0f);
        String name = intent.getStringExtra("name");

//        if(posilat!=0&&posilot!=0){
//            Intent intent1 = new Intent();
//            intent1.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
//            intent1.putExtra("KEY_TYPE", 10007);
//            intent1.putExtra("EXTRA_SLAT", (double) lat);
//            intent1.putExtra("EXTRA_SLON", (double) lot);
//            intent1.putExtra("EXTRA_SNAME", "��ǰλ��");
//            intent1.putExtra("EXTRA_DLAT",(double)posilat);
//            intent1.putExtra("EXTRA_DLON",(double)posilot);
//            intent1.putExtra("EXTRA_DNAME",name);
//            intent1.putExtra("EXTRA_DEV",0);
//            intent1.putExtra("EXTRA_M", 4);
////            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.sendBroadcast(intent1);
//        }
        if(posilat!=0&& posilot != 0 ) {
            Bundle bundle = new Bundle();
            bundle.putInt("KEY_TYPE", 10007);
            bundle.putDouble("EXTRA_SLAT", (double) lat);
            bundle.putDouble("EXTRA_SLON", (double) lot);
            bundle.putString("EXTRA_SNAME", "当前位置");
            bundle.putDouble("EXTRA_DLAT", (double) posilat);
            bundle.putDouble("EXTRA_DLON", (double) posilot);
            bundle.putString("EXTRA_DNAME", name);
            bundle.putInt("EXTRA_DEV", 0);
            bundle.putInt("EXTRA_M", 4);
//            Intent actiIntent = new Intent(this,)
            Intent activityIntent = new Intent(context,AskDialogActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activityIntent.putExtra("myown",bundle);
            context.startActivity(activityIntent);
        }
    }

    @Override
    public void setConnectState(boolean isConnect) {
        this.isConnect = isConnect;
    }


    private XpLocationListener mLocationListener = new XpLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            try {
                if (aMapLocation != null) {
                    double lat = aMapLocation.getLatitude();
                    double lon = aMapLocation.getLongitude();

                    int locationType = aMapLocation.getLocationType();
                    long times = aMapLocation.getTime();
                    Log.d("AmapLocation", "lat:" + lat + "  lon:" + lon + "  locationType:" + locationType + "  times:" + times);
                    int time1 = (int) (times / 1000000);
                    int time2 = (int) (times % 1000000);
                    LocationForICU.this.lat = (float) lat;
                    LocationForICU.this.lot = (float) lon;
                    int latt = (int) (lat * 1000000);
                    int lont = (int) (lon * 1000000);
                    try {
                        carControlBox.reportMotorLocation(locationType, latt, lont, time1, time2);
                        if (isConnect) {
                            msgId = (msgId+1)%128;
                            mSendControlBox.sendNotifyMsg(msgId, getRoadJsonStr(aMapLocation.getStreet()).getBytes(), null);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("AmapError", "location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onLocationChange(AMapNaviLocation location) {

        }
    };

    private XpAimNaviMsgListener mAimListener = new XpAimNaviMsgListener(){

        @Override
        public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo info) {

        }

        @Override
        public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

        }
    };

    private XpNaviInfoListener mNaviInfoListener = new XpNaviInfoListener() {
        @Override
        public void onNaviInfoUpdate(NaviInfo naviInfo) {
            try {
                if (isConnect) {
                    msgId = (msgId+1)%128;
                    mSendControlBox.sendNotifyMsg(msgId, getRoadJsonStr(naviInfo.getCurrentRoadName()).getBytes(), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void showCross(AMapNaviCross aMapNaviCross) {

        }

        @Override
        public void hideCross() {

        }

        @Override
        public void showLaneInfo(AMapLaneInfo[] var1, byte[] var2, byte[] var3) {

        }
    };



    private String getRoadJsonStr(String roadName){
        JSONObject jsonObject = new JSONObject();
        JSONObject notify = new JSONObject();
        notify.put(MSG_INDEX,"0");
        notify.put(NOMAL_NAME,roadName);
        notify.put(CDU_REC,"0");
        notify.put(CDU_USB,"0");
        notify.put(BLUE_MUSIC,"0");
        notify.put(NOMAL_LIMIT,"0");
        jsonObject.put(NOTIFY,notify);
        jsonObject.put(ENABLE,"1");
        jsonObject.put(MSG_TYPE,"1");
        return jsonObject.toJSONString();
    }
}
