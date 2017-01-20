package com.xiaopeng.xmapnavi.mode;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.aispeech.aios.common.bean.MapInfo;
import com.aispeech.aios.common.bean.PoiBean;
import com.aispeech.aios.common.property.MapProperty;
import com.aispeech.aios.sdk.listener.AIOSMapListener;
import com.aispeech.aios.sdk.manager.AIOSMapManager;
import com.aispeech.aios.sdk.manager.AIOSSettingManager;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.navi.enums.BroadcastMode;
import com.amap.api.navi.model.AMapNaviStaticInfo;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.nostra13.universalimageloader.utils.L;
import com.xiaopeng.lib.utils.utils.LogUtils;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.enums.AimLessMode;
import com.amap.api.navi.model.AMapCongestionLink;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.amaplib.util.TTSController;
import com.xiaopeng.xmapnavi.bean.LocationSaver;
import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.presenter.ICarControlReple;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.IMusicPoiProvider;
import com.xiaopeng.xmapnavi.presenter.IRoutePower;
import com.xiaopeng.xmapnavi.presenter.IStubGroupProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpAimNaviMsgListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpSensorListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpShouldStubListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpStubGroupListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/12.
 * 中央数据提供者
 *
 */
public class LocationProvider implements ILocationProvider,AMapLocationListener,AMapNaviListener
        ,PoiSearch.OnPoiSearchListener
        ,XpRouteListener , SensorEventListener
        , OfflineMapManager.OfflineMapDownloadListener
{
    private static final String TAG = "LocationProvider";
    private static final String NAVI_TAG = "Lp_NaviMsg";

    private static final String SET_TIME_ACTION = "com.xiaopeng.ACTION_SET_TIME";
    private static final String MSG_TIME = "com.xiaopeng.MSG_TIME";
    private TTSController ttsManager;
    private static Context mContext;
    private static ILocationProvider mLp;
    private int[] mInts;
    private AMapNavi aMapNavi;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private AMapLocation mAmapLocation;
    private static List<XpLocationListener> mListeners;
    private static List<XpSearchListner> mSearchListeners;
    private static List<XpNaviCalueListener> mNaviCalueListeners;
    private static List<XpRouteListener> mRouteListeners;
    private static List<XpNaviInfoListener> mNaviInfoListners;
    private static List<XpSensorListener> mSensorListners;
    private static List<XpCollectListener> mCollectListeners;
    private static List<XpAimNaviMsgListener> mAimNaviListeners;
    private static List<XpStubGroupListener> mStubListeners;
    private XpAiosMapListener mAiosListener;
    private XpShouldStubListener mShouldSbutListener;
    private OfflineMapManager.OfflineMapDownloadListener mMapDownListener;
    private OfflineMapManager mDownMapManager;
    private PoiSearch mPoiSearch;
    private SensorManager manager;
    private boolean congestion = true, cost = false, hightspeed = false, avoidhightspeed = false;

    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    private ISendNaviBroad mSendNaviBroad;
    //-Resume-//
    private PoiResult mPoiResult;
    private PoiItem mPoiItem;
    //--------//

    private IRoutePower mRoutePower;

    private long timeSave  = 0;
    private String saveSearchStr;

    //---static useful object----//
    private static final int QUEST_PAGE_SIZE = 10;
    private static final int EMULATOR_NAVISPEED = 180;
    //---------------------------//
    private int mBroadCastMode = BroadcastMode.CONCISE;
    private List<NaviLatLng> saveEndList = new ArrayList<>();
    private int AIM_STATE = AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED;

    private boolean isCalueIng = false;

    private boolean isWillOOM = false;

    private NaviInfo naviInfoSave ;
    private IStubGroupProvider mStubGroupProvider;
    private IMusicPoiProvider mMusicPoiProvider;

    private ICarControlReple mCarControlReple;
    private boolean isNaviing = false;

    private int remainingDistance;

    private ConnectivityManager mConnectivityManager; // To check for connectivity changes

    private long saveRightTime = 0;

    private boolean isShowFlag = false;

    public static void init(Context context) {
        mContext = context;
        mLp      = new LocationProvider(context);

    }

    public AMapLocation getAmapLocation() {
        return mAmapLocation;
    }

    @Override
    public void addLocationListener(XpLocationListener xpLocationListener) {
        mListeners.add(xpLocationListener);
        updateLoction.sendEmptyMessageDelayed(REQUEST_UPDATE,200);
    }

    @Override
    public void removeLocationListener(XpLocationListener xpLocationListener) {
        mListeners.remove(xpLocationListener);
    }

    @Override
    public void addSearchListner(XpSearchListner xpSearchListner) {
        mSearchListeners.add(xpSearchListner);
        LogUtils.d(TAG,"addSearchListner size:"+mSearchListeners.size()+"\nlistner:"+xpSearchListner.toString());

    }

    @Override
    public void removeSearchListner(XpSearchListner xpSearchListner) {
        LogUtils.d(TAG,"removeSearchListner size:"+mSearchListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mSearchListeners.remove(xpSearchListner);
    }

    @Override
    public void addNaviCalueListner(XpNaviCalueListener xpSearchListner) {
        LogUtils.d(TAG,"addNaviCalueListner size:"+mNaviCalueListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mNaviCalueListeners.add(xpSearchListner);
    }

    @Override
    public void removeNaviCalueListner(XpNaviCalueListener xpSearchListner) {
        LogUtils.d(TAG,"removeNaviCalueListner size:"+mNaviCalueListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mNaviCalueListeners.remove(xpSearchListner);
    }

    @Override
    public void addRouteListener(XpRouteListener xpRouteListener) {
        mRouteListeners.add(xpRouteListener);
    }

    @Override
    public void removeRouteListener(XpRouteListener xpRouteListener) {
        mRouteListeners.remove(xpRouteListener);
    }

    @Override
    public void addNaviInfoListner(XpNaviInfoListener xpNaviInfoListener) {
        mNaviInfoListners.add(xpNaviInfoListener);
    }

    @Override
    public void removeNaviInfoListener(XpNaviInfoListener xpNaviInfoListener) {
        mNaviInfoListners.remove(xpNaviInfoListener);
    }

    @Override
    public void addSensorListner(XpSensorListener xpSensorListener) {
        if (mSensorListners.size() < 1){
            Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            //应用在前台时候注册监听器
            manager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);

        }

        mSensorListners.add(xpSensorListener);
    }

    @Override
    public void removeSensorListner(XpSensorListener xpSensorListener) {
        mSensorListners.remove(xpSensorListener);
        if (mSensorListners.size()<1) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void setOfflineMapListner(OfflineMapManager.OfflineMapDownloadListener listner) {
        mMapDownListener = listner;
    }

    @Override
    public void setAiosListener(XpAiosMapListener xpAiosMapListener) {
        mAiosListener = xpAiosMapListener;
    }

    @Override
    public void addAimNaviListener(XpAimNaviMsgListener listener) {
        mAimNaviListeners.add(listener);
    }

    @Override
    public void removeAimNaviListener(XpAimNaviMsgListener listener) {
        mAimNaviListeners.remove(listener);
    }

    @Override
    public void addStubGroupListener(XpStubGroupListener listener) {
        mStubListeners.add(listener);
    }

    @Override
    public void removeStubGroupListener(XpStubGroupListener listener) {
        mStubListeners.remove(listener);
    }

    @Override
    public void setShouldStubListener(XpShouldStubListener listener) {
        mShouldSbutListener = listener;
    }


    @Override
    public void trySearchPosi(String str) {
        beginSearchAddr(str);
    }

    @Override
    public boolean tryAddWayPoiCalue(NaviLatLng wayPoi) {
        if (saveEndList.size()==0 || mAmapLocation==null)return false;
        List<NaviLatLng> startPois = new ArrayList<>();
        List<NaviLatLng> wayPois = new ArrayList<>();
        List<NaviLatLng> endPois = new ArrayList<>();
        startPois.add(new NaviLatLng(mAmapLocation.getLatitude(),mAmapLocation.getLongitude()));
        wayPois.add(wayPoi);
        endPois.addAll(saveEndList);

        return calueRunWay(startPois,wayPois,endPois);
    }

    private long lastCalueTime = 0;

    @Override
    public boolean calueRunWay(List<NaviLatLng> startList,List<NaviLatLng> wayList,List<NaviLatLng> endList) {
//        if (isCalueIng)return;
        long timeDis;
        if (( timeDis = (System.currentTimeMillis() - lastCalueTime)) < 500){
            if (timeDis < 0){
                lastCalueTime = System.currentTimeMillis();
            }

            return false;
        }
        boolean isCan = false;
        lastCalueTime = System.currentTimeMillis();
        aMapNavi.stopNavi();
        isNaviing = false;
        if (avoidhightspeed && hightspeed) {
            LogUtils.d(TAG,"不走高速与高速优先不能同时为true.");
        }
        if (cost && hightspeed) {
            LogUtils.d(TAG,"高速优先与避免收费不能同时为true");
        }
            /*
			 * strategyFlag转换出来的值都对应PathPlanningStrategy常量，用户也可以直接传入PathPlanningStrategy常量进行算路。
			 * 如:mAMapNavi.calculateDriveRoute(mStartList, mEndList, mWayPointList,PathPlanningStrategy.DRIVING_DEFAULT);
			 */
        int strategyFlag = 0;
        try {
            strategyFlag = aMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (strategyFlag >= 0) {
            isCalueIng = true;
            isCan = aMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);

            LogUtils.d(TAG,"策略:" + strategyFlag);
            disNowCalue.removeMessages(0);
            disNowCalue.sendEmptyMessageDelayed(0,30 * 1000);
        }
        saveEndList.clear();
        saveEndList.addAll(endList);
        return isCan;
    }

    @Override
    public boolean tryCalueRunWay(List<NaviLatLng> endList) {
//        if (isCalueIng)return false;
        if (mAmapLocation==null)return false;

        List<NaviLatLng> startList = new ArrayList<>();
        startList.add(new NaviLatLng(mAmapLocation.getLatitude(),mAmapLocation.getLongitude()));
        List<NaviLatLng> wayList = new ArrayList<>();
        int strategyFlag = 0;
        try {
            strategyFlag = aMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (strategyFlag >= 0) {
            isCalueIng = true;
            aMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
            disNowCalue.removeMessages(0);
            disNowCalue.sendEmptyMessageDelayed(0,30 * 1000);
            LogUtils.d(TAG,"策略:" + strategyFlag);
        }
        saveEndList.clear();
        saveEndList.addAll(endList);
        return true;
    }




    @Override
    public AMapNaviPath getNaviPath() {
        AMapNaviPath path = aMapNavi.getNaviPath();
        AMapNaviStep step = path.getSteps().get(0);
        AMapNaviLink naviLink = step.getLinks().get(0);

        return aMapNavi.getNaviPath();
    }



    @Override
    public HashMap<Integer, AMapNaviPath> getNaviPaths() {
        if (!isWillOOM) {
            return aMapNavi.getNaviPaths();
        }else {
            HashMap<Integer,AMapNaviPath> pathHashMap = aMapNavi.getNaviPaths();
            HashMap<Integer,AMapNaviPath> pathHashMap1 = new HashMap<>();
            pathHashMap1.put(mInts[0],pathHashMap.get(mInts[0]));
            return pathHashMap1;
        }
    }

    public static ILocationProvider getInstence(Context context){
        if (mLp == null){
            init(context);
        }
        return mLp;
    }

    private LocationProvider(Context context){
        mListeners          = new ArrayList<>();
        mSearchListeners    = new ArrayList<>();
        mNaviCalueListeners = new ArrayList<>();
        mRouteListeners     = new ArrayList<>();
        mNaviInfoListners   = new ArrayList<>();
        mSensorListners     = new ArrayList<>();
        mCollectListeners   = new ArrayList<>();
        mAimNaviListeners   = new ArrayList<>();
        mStubListeners      = new ArrayList<>();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mSendNaviBroad = new SendNaviBroad();
        mSendNaviBroad  .initBroad(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("myown",Context.MODE_PRIVATE);
        AIM_STATE = sharedPreferences.getInt("aimState",AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
        mStubGroupProvider  = new StubGroupProvider();
        mMusicPoiProvider   = new MusicPoiProvider();
        mStubGroupProvider  .init(context);
        mMusicPoiProvider   .init(context);
        mStubGroupProvider  .setOnStubDataListener(mStubGroupListener);
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(context.getApplicationContext());
            mLocationOption = getDefaultOption();
            mLocationClient.setLocationOption(mLocationOption);
            //设置定位监听

            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//            mLocationOption.setOnceLocation(false);
            //设置定位参数

            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }

        aMapNavi = AMapNavi.getInstance(context);
        aMapNavi.startAimlessMode(AIM_STATE);

        ttsManager = TTSController.getInstance(context.getApplicationContext());
        ttsManager.init();

        aMapNavi.addAMapNaviListener(this);
        aMapNavi.addAMapNaviListener(ttsManager);

        mAmapLocation = LocationSaver.getSaveLocation();
        updateLoction.sendEmptyMessageDelayed(REQUEST_INIT,1000);

        mRoutePower = new RoutePower();
        mRoutePower .setXpRouteListner(this);

        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mDownMapManager = new OfflineMapManager(context,this);


        initAiosListener();
        initBroadCast();
        mCarControlReple = new CarControlReple();
        mCarControlReple.init(context);
    }

    private void initAiosListener(){
        LogUtils.d(TAG,"initAiosListener");
        MapInfo xpMap = new MapInfo("小鹏地图","com.xiaopeng.xmapnavi");
        xpMap.setCancelNaviSupported(true);
        xpMap.setOverviewSupported(true);
        xpMap.setZoomSupported(true);
        xpMap.setSupportedRoutePlanningStrategy(MapProperty.SupportedRoutePlanningStrategy.DRIVING_AVOID_CONGESTION ,
                MapProperty.SupportedRoutePlanningStrategy.DRIVING_SAVE_MONEY);
        AIOSMapManager.getInstance().setLocalMapInfo(xpMap,false);
        AIOSMapManager.getInstance().registerMapListener(mapAiosListener);

        AIOSSettingManager.getInstance().setDefaultMap("com.xiaopeng.xmapnavi");
    }

    private void initBroadCast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("AUTONAVI_STANDARD_BROADCAST_RECV");
        mContext.registerReceiver(naviStateReceiver,intentFilter);
    }

    BroadcastReceiver naviStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG,"naviStateReceiver onReceive:");
            int keyt = intent.getIntExtra("KEY_TYPE", 10000);
            if (keyt == 10007){
                double lat = intent.getDoubleExtra("EXTRA_DLAT",0d);
                double lon = intent.getDoubleExtra("EXTRA_DLON",0d);
                if (lat ==0 || lon ==0)return;
                Bundle bundle = new Bundle();
                bundle.putDouble("lat",lat);
                bundle.putDouble("lon",lon);
                Message msg = deleyToStartNavi.obtainMessage();
                msg.what = 2;
                msg.obj = bundle;
                deleyToStartNavi.sendMessage(msg);
            }
        }
    };




    Handler updateLoction = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_UPDATE:
                    onLocationChanged(LocationProvider.this.mAmapLocation);
                    break;
                case REQUEST_INIT:
                    mAmapLocation = LocationSaver.getSaveLocation();
                    onLocationChanged(mAmapLocation);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 获取AB连线与正北方向的角度
     * @param A  A点的经纬度
     * @param B  B点的经纬度
     * @return  AB连线与正北方向的角度（0~360）
     */
    public  static double getAngle(MyLatLng A,MyLatLng B){
        double dx=(B.m_RadLo-A.m_RadLo)*A.Ed;
        double dy=(B.m_RadLa-A.m_RadLa)*A.Ec;
        double angle=0.0;
        if (dy !=0) {
            angle = Math.atan(Math.abs(dx / dy)) * 180. / Math.PI;
            double dLo = B.m_Longitude - A.m_Longitude;
            double dLa = B.m_Latitude - A.m_Latitude;
            if (dLo > 0 && dLa <= 0) {
                angle = (90. - angle) + 90;
            } else if (dLo <= 0 && dLa < 0) {
                angle = angle + 180.;
            } else if (dLo < 0 && dLa >= 0) {
                angle = (90. - angle) + 270;
            }
            return angle;
        }else {
            if (dx >= 0){
                return 0;
            }else {
                return 180;
            }
        }
    }

    static class MyLatLng {
        final static double Rc=6378137;
        final static double Rj=6356725;
        double m_LoDeg,m_LoMin,m_LoSec;
        double m_LaDeg,m_LaMin,m_LaSec;
        double m_Longitude,m_Latitude;
        double m_RadLo,m_RadLa;
        double Ec;
        double Ed;
        public MyLatLng(double longitude,double latitude){
            m_LoDeg=(int)longitude;
            m_LoMin=(int)((longitude-m_LoDeg)*60);
            m_LoSec=(longitude-m_LoDeg-m_LoMin/60.)*3600;

            m_LaDeg=(int)latitude;
            m_LaMin=(int)((latitude-m_LaDeg)*60);
            m_LaSec=(latitude-m_LaDeg-m_LaMin/60.)*3600;

            m_Longitude=longitude;
            m_Latitude=latitude;
            m_RadLo=longitude*Math.PI/180.;
            m_RadLa=latitude*Math.PI/180.;
            Ec=Rj+(Rc-Rj)*(90.-m_Latitude)/90.;
            Ed=Ec*Math.cos(m_RadLa);
        }
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                if (mMusicPoiProvider!=null){
                    mMusicPoiProvider.sendProvide(aMapLocation);
                }

                saveRightTime = aMapLocation.getTime() ;

//                try {
//                    MyLatLng myLatLng = new MyLatLng(mAmapLocation.getLongitude(), mAmapLocation.getLatitude());
//                    MyLatLng myLatLng1 = new MyLatLng(aMapLocation.getLongitude(), aMapLocation.getLatitude());
//                    float roatate = (float) getAngle(myLatLng, myLatLng1);
//                    LogUtils.d(TAG, "onLocationChanged :roatate :" + roatate);
//                    aMapLocation.setBearing(roatate);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }


                mAmapLocation = aMapLocation;
                if (System.currentTimeMillis() - timeSave > (60 * 1000)){
                    timeSave = System.currentTimeMillis();
                    new SaveThread(aMapLocation).start();
                }
                for (XpLocationListener listener:mListeners){
                    listener.onLocationChanged(aMapLocation);
                }
                mRoutePower.setCurretPosi(mAmapLocation);
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                LogUtils.e("AmapErr",errText);
            }
        }
    }


    /* 以下都是Navi的回调 */
    @Override
    public void onInitNaviFailure() {
        LogUtils.d(NAVI_TAG,"onInitNaviFailure");
    }

    @Override
    public void onInitNaviSuccess() {
        LogUtils.d(NAVI_TAG,"onInitNaviSuccess");
    }

    @Override
    public void onStartNavi(int i) {
        LogUtils.d(NAVI_TAG,"onStartNavi: i ="+i);
    }

    @Override
    public void onTrafficStatusUpdate() {
        try {
            LogUtils.d(NAVI_TAG, "onTrafficStatusUpdate");
            int end = aMapNavi.getNaviPath().getAllLength();
            int start = end - remainingDistance;
            List<AMapTrafficStatus> remainingRoadCondition = aMapNavi.getTrafficStatuses(start, end);
            for (XpNaviInfoListener xpNaviInfoListener : mNaviInfoListners){
                xpNaviInfoListener.onNaviTrafficStatusUpdate(remainingRoadCondition,remainingDistance);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        LogUtils.d(NAVI_TAG,"onLocationChange");
        for (XpLocationListener lisener:mListeners){
            lisener.onLocationChange(aMapNaviLocation);
        }
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        LogUtils.d(NAVI_TAG,"onGetNavigationText i="+i+"\ns:"+s);
//        ttsManager.startSpeaking(s);
    }

    @Override
    public void onEndEmulatorNavi() {
        LogUtils.d(NAVI_TAG,"onEndEmulatorNavi");
    }

    @Override
    public void onArriveDestination() {
        LogUtils.d(NAVI_TAG,"onArriveDestination");
    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {
        LogUtils.d(NAVI_TAG,"onArriveDestination");
    }

    @Override
    public void onArriveDestination(AMapNaviStaticInfo aMapNaviStaticInfo) {
        LogUtils.d(NAVI_TAG,"onArriveDestination");
    }

    @Override
    public void onCalculateRouteSuccess() {
        isCalueIng = false;
//        Toast.makeText(mContext,"onCalculateRouteSuccess",Toast.LENGTH_SHORT).show();
        LogUtils.d(NAVI_TAG,"onCalculateRouteSuccess");
        disNowCalue.removeMessages(0);
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            listener.onCalculateRouteSuccess();
        }
    }

    @Override
    public void onCalculateRouteFailure(int i) {
//        Toast.makeText(mContext,"onCalculateRouteFailure",Toast.LENGTH_SHORT).show();
        disNowCalue.removeMessages(0);
//        disNowCalue.sendEmptyMessageDelayed(0,30 * 1000);
        isCalueIng = false;
        LogUtils.d(NAVI_TAG,"onCalculateRouteFailure i="+i);
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            try {
                listener.onCalculateRouteFailure();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (i == 2){
            try{
                tryToGetRightTime();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReCalculateRouteForYaw() {
        isCalueIng = false;
        LogUtils.d(NAVI_TAG,"onReCalculateRouteForYaw");
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        isCalueIng = false;
        LogUtils.d(NAVI_TAG,"onReCalculateRouteForTrafficJam");
    }

    @Override
    public void onArrivedWayPoint(int i) {
        LogUtils.d(NAVI_TAG,"onArrivedWayPoint");
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        LogUtils.d(NAVI_TAG,"onGpsOpenStatus b="+b);
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
//        LogUtils.d(NAVI_TAG,"onNaviInfoUpdated");
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
//        LogUtils.d(NAVI_TAG,"onNaviInfoUpdate");
        remainingDistance = naviInfo.getPathRetainDistance();
        naviInfoSave = naviInfo;
        if (naviInfo!=null) {
            if (naviInfo.getCurStepRetainDistance()>2000){
                naviInfo.setIconType(9);
            }
        }
        for (XpNaviInfoListener naviInfoListener:mNaviInfoListners){
            naviInfoListener.onNaviInfoUpdate(naviInfo);
        }
        mSendNaviBroad.sendNaviMsg(naviInfo);

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo info) {
        LogUtils.d(NAVI_TAG,"OnUpdateTrafficFacility");
        if (info!=null){
            LogUtils.d(NAVI_TAG, "(trafficFacilityInfo.coor_X+trafficFacilityInfo.coor_Y+trafficFacilityInfo.distance+trafficFacilityInfo.limitSpeed):"
                    + (info.getCoorX() + info.getCoorY() + info.getDistance() + info.getLimitSpeed()));

        }

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        LogUtils.d(NAVI_TAG,"OnUpdateTrafficFacility");

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        LogUtils.d(NAVI_TAG,"showCross");
        for (XpNaviInfoListener listener:mNaviInfoListners){
            listener.showCross(aMapNaviCross);
        }
    }

    @Override
    public void hideCross() {
        LogUtils.d(NAVI_TAG,"hideCross");
        for (XpNaviInfoListener listener:mNaviInfoListners){
            listener.hideCross();
        }
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        LogUtils.d(NAVI_TAG,"showLaneInfo");
        for (XpNaviInfoListener listener:mNaviInfoListners){
            listener.showLaneInfo(aMapLaneInfos,bytes,bytes1);
        }
    }

    @Override
    public void hideLaneInfo() {
        LogUtils.d(NAVI_TAG,"hideLaneInfo");
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(NAVI_TAG,"onCalculateMultipleRoutesSuccess");
//        Toast.makeText(mContext,"onCalculateMultipleRoutesSuccess",Toast.LENGTH_SHORT).show();
        disNowCalue.removeMessages(0);
//        disNowCalue.sendEmptyMessageDelayed(0,30 * 1000);
        isCalueIng = false;
        mInts = ints;
        mRoutePower.setPath(aMapNavi.getNaviPaths(),ints);
        isWillOOM = isWillOutOfMemory();
        if (!isWillOOM) {
            if (mNaviCalueListeners.size()>0) {
                for (int i = mNaviCalueListeners.size()-1;i >= 0;i--) {
                    XpNaviCalueListener listener = mNaviCalueListeners.get(i);
                    try {
                        listener.onCalculateMultipleRoutesSuccess(ints);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            mInts = new int[]{
                    ints[0]
            };
            aMapNavi.selectRouteId(ints[0]);
            for (XpNaviCalueListener listener : mNaviCalueListeners) {
                try {
                    listener.onCalculateMultipleRoutesSuccess(mInts);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isWillOutOfMemory(){
        try {
            HashMap<Integer, AMapNaviPath> aMapNaviPaths = aMapNavi.getNaviPaths();
            AMapNaviPath path = aMapNaviPaths.get(mInts[0]);
            if (path.getAllLength()>800000){
                LogUtils.d(TAG,"isWillOutOfMemory");
                return true;
            }

        }catch (Exception e){
            e.printStackTrace();
            LogUtils.d(TAG,"will not OOM");
            return false;
        }finally {


        }
        LogUtils.d(TAG,"will not OOM");
        return false;
    }




    @Override
    public void notifyParallelRoad(int i) {
        LogUtils.d(NAVI_TAG,"notifyParallelRoad");
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        LogUtils.d(NAVI_TAG,"OnUpdateTrafficFacility");
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        LogUtils.d(NAVI_TAG,"updateAimlessModeStatistics");
        LogUtils.d(TAG, "distance=" + aimLessModeStat.getAimlessModeDistance());
        LogUtils.d(TAG, "time=" + aimLessModeStat.getAimlessModeTime());

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        LogUtils.d(NAVI_TAG,"updateAimlessModeCongestionInfo");
        LogUtils.d(TAG, "roadName=" + aimLessModeCongestionInfo.getRoadName());
        LogUtils.d(TAG, "CongestionStatus=" + aimLessModeCongestionInfo.getCongestionStatus());
        LogUtils.d(TAG, "eventLonLat=" + aimLessModeCongestionInfo.getEventLon() + "," + aimLessModeCongestionInfo.getEventLat());
        LogUtils.d(TAG, "length=" + aimLessModeCongestionInfo.getLength());
        LogUtils.d(TAG, "time=" + aimLessModeCongestionInfo.getTime());
        for (AMapCongestionLink link :
                aimLessModeCongestionInfo.getAmapCongestionLinks()) {
            LogUtils.d(TAG, "status=" + link.getCongestionStatus());
            for (NaviLatLng latlng : link.getCoords()
                    ) {
                LogUtils.d(TAG, latlng.toString());
            }
        }
    }

    /* 以上都是Navi的回调*/

    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30 *1000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(5 * 1000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        return mOption;
    }

    /**
     * 尝试搜索目标点
     * @param str
     */
    private void beginSearchAddr(String str){
        saveSearchStr = str;
        String cityCode = "";
        if (mAmapLocation != null){
            cityCode = mAmapLocation.getCity();
        }
        PoiSearch.Query query  = new PoiSearch.Query(str, "", cityCode);
        query.setPageSize(QUEST_PAGE_SIZE); //设置10 页
        query.setPageNum(0);

        if (mPoiSearch == null) {
            mPoiSearch  =new PoiSearch(mContext, query);
        }else {
            mPoiSearch.setQuery(query);
        }
        mPoiSearch.setOnPoiSearchListener(this);
        mPoiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        LogUtils.d(TAG,"onPoiSearched i:"+i);
        mPoiResult = poiResult;
        for (XpSearchListner listener:mSearchListeners){
            listener.searchSucceful();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
        mPoiItem = poiItem;
    }

    @Override
    public PoiResult getPoiResult() {
        return mPoiResult;
    }

    @Override
    public PoiItem getPoiItem() {
        return mPoiItem;
    }


    @Override
    public void selectRouteId(int id) {
        if (id != -1) {
            aMapNavi.selectRouteId(id);
        }
    }

    @Override
    public boolean startNavi(int var1) {
        aMapNavi.stopAimlessMode();
        aMapNavi.setEmulatorNaviSpeed(EMULATOR_NAVISPEED);
        boolean like = aMapNavi.startNavi(var1);
        isNaviing = true;
        handler.sendEmptyMessageDelayed(0,3000);

        return like;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onTrafficStatusUpdate();
        }
    };

    @Override
    public void stopNavi() {
        aMapNavi.stopNavi();
        isNaviing = false;
        aMapNavi.startAimlessMode(AIM_STATE);
        mSendNaviBroad.stopNavi();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        for (XpSensorListener listener : mSensorListners){
            listener.onSensorChanged(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        for (XpSensorListener listener : mSensorListners){
            listener.onAccuracyChanged(sensor,i);
        }
    }

    /**
     *
     *  onDownload
     *  onCheckUpdate
     *  onRemove
     */
    //-----------
    @Override
    public void onDownload(int i, int i1, String s) {

    }

    @Override
    public void onCheckUpdate(boolean b, String s) {

    }

    @Override
    public void onRemove(boolean b, String s, String s1) {

    }
    //-------------


    class SaveThread extends Thread{
        AMapLocation location;
        SaveThread(AMapLocation location){
            this.location = location;
        }
        @Override
        public void run() {
            super.run();

            LocationSaver locationSaver = LocationSaver.saveNewLocation(mAmapLocation);
            locationSaver.save();
        }
    }

    @Override
    public void startRouteNavi(){
        aMapNavi.removeAMapNaviListener(ttsManager);
        aMapNavi.stopAimlessMode();
        mRoutePower.startRoute();
    }




    @Override
    public void stopRouteNavi(){
        aMapNavi.addAMapNaviListener(ttsManager);
        aMapNavi.startAimlessMode(AIM_STATE);

        mRoutePower.stopRoute();
    }

    @Override
    public void setNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed) {
        LogUtils.d(TAG,"setNaviStyle:\ncongestion:"+congestion+"\navHightSpeed:"+avHighSpeed+"\navCost:"+avCost+"\nhightSpeed:"+highSpeed);
        this.congestion = congestion;
        this.avoidhightspeed = avHighSpeed;
        this.cost = avCost;
        this.hightspeed = highSpeed;
    }

    @Override
    public boolean reCalueInNavi() {
        try {
            if (isCalueIng)return false;
            isCalueIng = true;
            deleyNewO.sendEmptyMessageDelayed(0,20 * 1000);
//
            if (isNaviing) {
                boolean isCan = aMapNavi.reCalculateRoute(aMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true));
                if (!isCan){
                    isNaviing = false;
                }
                return isCan;
            }else {
                AMapNaviPath path = aMapNavi.getNaviPath();
                List<NaviLatLng> endPois = new ArrayList<>();
                endPois.add(path.getEndPoint());
                List<NaviLatLng> startPoi = new ArrayList<>();
                startPoi.add(path.getStartPoint());
                List<NaviLatLng> wayPoi = new ArrayList<>();
                wayPoi.addAll(path.getWayPoint());
                aMapNavi.stopNavi();
                int trueAvi = aMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, false);
                int falseAvi = aMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, true);
                LogUtils.d(TAG,"\ntrueAvi:"+trueAvi+"\nfalseAvi:"+falseAvi);
                disNowCalue.removeMessages(0);
                disNowCalue.sendEmptyMessageDelayed(0,30 * 1000);
                return aMapNavi.calculateDriveRoute(startPoi, endPois, wayPoi, falseAvi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    Handler deleyNewO = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isCalueIng = false;
        }
    };

    @Override
    public boolean reCalue() {
        //TODO
        LogUtils.d(TAG,"reCalue");
        if (saveEndList.size()>0 ){
            return tryCalueRunWay(new ArrayList<NaviLatLng>(saveEndList));
        }else {
            return false;
        }
    }


    @Override
    public int[] getPathsInts() {

        return mInts;
    }

    @Override
    public NaviLatLng getNaviEndPoi() {
        try {
            return aMapNavi.getNaviPaths().get(mInts[0]).getEndPoint();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public OfflineMapManager getOfflineMapManager() {
        return mDownMapManager;
    }

    @Override
    public String getSearchStr() {
        return saveSearchStr;
    }

    @Override
    public int getBroadCastMode() {
        return mBroadCastMode;
    }

    @Override
    public void setBroadCastMode(int mode) {
        mBroadCastMode = mode;
        aMapNavi.setBroadcastMode(mode);
    }

    @Override
    public boolean getNaviLikeStyle(int num) {
        switch (num){
            case 0:
                return congestion;
            case 1:
                return cost;
            case 2:
                return hightspeed;
            case 3:
                return avoidhightspeed;
        }
        return false;
    }

    @Override
    public void reCallLocation() {
        for (XpLocationListener listener:mListeners){
            listener.onLocationChanged(mAmapLocation);
        }
    }

    @Override
    public void setAimState(int state) {
        SharedPreferences sp = mContext.getSharedPreferences("myown",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("aimState",state);
        editor.commit();
        AIM_STATE = state;
        aMapNavi.stopAimlessMode();
        aMapNavi.startAimlessMode(AIM_STATE);
    }

    @Override
    public int getAimState() {
        return AIM_STATE;
    }

    @Override
    public void muteLaught() {
        ttsManager.setSpeakType(TTSController.ALL_NO_SPEAK);
    }

    @Override
    public void unmuteLaught() {
        ttsManager.setSpeakType(TTSController.ALL_SPEAK);
    }

    @Override
    public void muteSomeLaught() {
        ttsManager.setSpeakType(TTSController.SPEAK_SOMW);
    }

    @Override
    public void getStubGroups(double lat, double lon) {
        mStubGroupProvider.getStubGroupByPoi(lat,lon);
    }

    @Override
    public void getStubGroups(String city) {
        if (city!=null) {
            mStubGroupProvider.getStubGroupByCity(city);
        }else {
            if (mAmapLocation!=null){
                mStubGroupProvider.getStubGroupByCity(mAmapLocation.getCityCode());
            }
        }
    }

    @Override
    public void getStubGroups() {
        if (mAmapLocation!=null){
            mStubGroupProvider.getStubGroupByPoi(mAmapLocation.getLatitude(),mAmapLocation.getLongitude());
        }
    }

    @Override
    public NaviInfo getNaviInfo() {
        return naviInfoSave;
    }

    @Override
    public ICarControlReple getCarControlReple() {
        return mCarControlReple;
    }

    @Override
    public void shouldShowStub() {
        if (mShouldSbutListener!=null){
            mShouldSbutListener.onShowStub();
        }
    }

    @Override
    public void setStubShowFlag(boolean isShowStub) {
        this.isShowFlag = isShowStub;
    }

    @Override
    public boolean getStubShowFlag() {
        return isShowFlag;
    }


    @Override
    public void nearBy(int pathId, int stepNum, int poiNum) {
        //TODO
//        aMapNavi.stopNavi();
//        aMapNavi.selectRouteId(pathId);
//        aMapNavi.startNavi(AMapNavi.GPSNaviMode);
        for (XpRouteListener listener:mRouteListeners){
            listener.nearBy(pathId,stepNum,poiNum);
        }
    }

    Handler deleyToStartNavi = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    PoiBean poiBean = (PoiBean) msg.obj;

                    if (mAiosListener != null) {
                        mAiosListener.onStartNavi("", poiBean);
                    }
                    break;



                case 1:
                    LogUtils.d(TAG,"Handler onStartActivity");
                    LogUtils.d(TAG,"is Service:"+(mContext instanceof Service));
                    LogUtils.d(TAG,"is Activity:"+(mContext instanceof Activity));
                    LogUtils.d(TAG,"is Application:"+(mContext instanceof Application));
                    Intent dialogIntent = new Intent(mContext, MainActivity.class);
//                    Intent dialogIntent = new Intent();
//                    dialogIntent.setClassName("com.xiaopeng.xmapnavi","com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity");
//                    if (!(mContext instanceof MainActivity)) {
                    dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    }
                    mContext.getApplicationContext().startActivity(dialogIntent);
                    break;

                case 2:
                    Bundle bundle = (Bundle) msg.obj;
                    double lat = bundle.getDouble("lat",0);
                    double lon = bundle.getDouble("lon",0);
                    if (mAiosListener != null) {
                        mAiosListener.onStartNavi(lat,lon);
                    }
                    break;
            }
        }
    };


    private AIOSMapListener mapAiosListener = new AIOSMapListener() {
        @Override
        public void onStartNavi(@NonNull String s, @NonNull PoiBean poiBean) {
            LogUtils.d(TAG,"onStartNavi");
            deleyToStartNavi.sendEmptyMessage(1);

            Message message = deleyToStartNavi.obtainMessage();
            message.obj = poiBean;
            deleyToStartNavi.sendMessageDelayed(message,3000);

        }

        @Override
        public void onCancelNavi(@NonNull String s) {
            if (mAiosListener!=null){
                mAiosListener.onCancelNavi(s);
            }
        }

        @Override
        public void onOverview(@NonNull String s) {
            if (mAiosListener!=null){
                mAiosListener.onOverview(s);
            }

        }

        @Override
        public void onRoutePlanning(@NonNull String s, @NonNull String s1) {
            if (mAiosListener!=null){
                mAiosListener.onRoutePlanning(s,s1);
            }
        }

        @Override
        public void onZoom(@NonNull String s, int i) {
            if (mAiosListener!=null){
                mAiosListener.onZoom(s,i);
            }
        }

        @Override
        public void onLocate(@NonNull String s) {
            if (mAiosListener!=null){
                mAiosListener.onLocate(s);
            }

        }

    };

    /**
     * Query's the NetworkInfo via ConnectivityManager
     * to return the current connected state
     *
     * @return boolean true if we are connected false otherwise
     */
    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();

        return (info == null) ? false : info.isConnected();
    }


    private void tryToGetRightTime() throws IOException, InterruptedException {
        try {
            if (isNetworkAvailable() && saveRightTime != 0) {
                Date date = new Date(saveRightTime);
                LogUtils.d(TAG, "setTime:" + saveRightTime + "\ndate:" + date);
                Utils.requestPermission();
                SystemClock.setCurrentTimeMillis(saveRightTime);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.setAction(SET_TIME_ACTION);
        intent.putExtra(MSG_TIME,saveRightTime);
        mContext.sendBroadcast(intent);
    }


    private IStubGroupProvider.OnStubData mStubGroupListener = new IStubGroupProvider.OnStubData() {
        @Override
        public void stubProvide(List<PowerPoint> stubAcs) {
            //TODO
            for (XpStubGroupListener stubGroupListener:mStubListeners){
                stubGroupListener.OnStubData(stubAcs);
            }

        }
    };


    Handler disNowCalue = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.d(TAG,"disNowCalue");
            Toast.makeText(mContext,"disNowCalue",Toast.LENGTH_SHORT).show();
            LocationProvider.this.reCalue();
        }
    };



}
