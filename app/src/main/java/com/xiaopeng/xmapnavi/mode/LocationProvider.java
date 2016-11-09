package com.xiaopeng.xmapnavi.mode;

import android.content.ComponentName;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.xiaopeng.lib.utils.utils.LogUtils;
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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.amaplib.util.TTSController;
import com.xiaopeng.xmapnavi.bean.LocationSaver;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.IRoutePower;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpSensorListener;

import java.util.ArrayList;
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
    private TTSController ttsManager;
    private static Context mContext;
    private static ILocationProvider mLp;
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
    private OfflineMapManager.OfflineMapDownloadListener mMapDownListener;
    private OfflineMapManager mDownMapManager;
    private PoiSearch mPoiSearch;
    private SensorManager manager;
    private boolean congestion = true, cost = false, hightspeed = false, avoidhightspeed = false;

    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    //-Resume-//
    private PoiResult mPoiResult;
    private PoiItem mPoiItem;
    //--------//

    private IRoutePower mRoutePower;

    private long timeSave  = 0;


    //---static useful object----//
    private static final int QUEST_PAGE_SIZE = 10;
    private static final int EMULATOR_NAVISPEED = 60;
    //---------------------------//

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
    public void trySearchPosi(String str) {
        beginSearchAddr(str);
    }

    @Override
    public void calueRunWay(List<NaviLatLng> startList,List<NaviLatLng> wayList,List<NaviLatLng> endList) {
        aMapNavi.stopNavi();
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
            aMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
            LogUtils.d(TAG,"策略:" + strategyFlag);
        }
    }

    @Override
    public boolean tryCalueRunWay(List<NaviLatLng> endList) {
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
            aMapNavi.calculateDriveRoute(startList, endList, wayList, strategyFlag);
            LogUtils.d(TAG,"策略:" + strategyFlag);
        }
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
        return aMapNavi.getNaviPaths();
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
        aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);

        ttsManager = TTSController.getInstance(context.getApplicationContext());
        ttsManager.init();

        aMapNavi.addAMapNaviListener(this);
        aMapNavi.addAMapNaviListener(ttsManager);

        updateLoction.sendEmptyMessageDelayed(REQUEST_INIT,1000);

        mRoutePower = new RoutePower();
        mRoutePower .setXpRouteListner(this);

       manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mDownMapManager = new OfflineMapManager(context,this);
    }

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




    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
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
        LogUtils.d(NAVI_TAG,"onTrafficStatusUpdate");
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
    public void onCalculateRouteSuccess() {
        LogUtils.d(NAVI_TAG,"onCalculateRouteSuccess");
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            listener.onCalculateRouteSuccess();
        }
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        LogUtils.d(NAVI_TAG,"onCalculateRouteFailure i="+i);
    }

    @Override
    public void onReCalculateRouteForYaw() {
        LogUtils.d(NAVI_TAG,"onReCalculateRouteForYaw");
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
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
        LogUtils.d(NAVI_TAG,"onNaviInfoUpdated");
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        LogUtils.d(NAVI_TAG,"onNaviInfoUpdate");
        for (XpNaviInfoListener naviInfoListener:mNaviInfoListners){
            naviInfoListener.onNaviInfoUpdate(naviInfo);
        }

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
    }

    @Override
    public void hideCross() {
        LogUtils.d(NAVI_TAG,"hideCross");
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        LogUtils.d(NAVI_TAG,"showLaneInfo");
    }

    @Override
    public void hideLaneInfo() {
        LogUtils.d(NAVI_TAG,"hideLaneInfo");
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(NAVI_TAG,"onCalculateMultipleRoutesSuccess");
        mRoutePower.setPath(aMapNavi.getNaviPaths(),ints);
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            listener.onCalculateMultipleRoutesSuccess(ints);
        }
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
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30 *1000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(30 * 1000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是ture
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTPS);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        return mOption;
    }

    /**
     * 尝试搜索目标点
     * @param str
     */
    private void beginSearchAddr(String str){
        String cityCode = "";
        if (mAmapLocation != null){
            cityCode = mAmapLocation.getCityCode();
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
        aMapNavi.selectRouteId(id);
    }

    @Override
    public boolean startNavi(int var1) {
        aMapNavi.stopAimlessMode();
        aMapNavi.setEmulatorNaviSpeed(EMULATOR_NAVISPEED);
        return aMapNavi.startNavi(var1);
    }

    @Override
    public void stopNavi() {
        aMapNavi.stopNavi();
        aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
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
        aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);

        mRoutePower.stopRoute();
    }

    @Override
    public void setNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed) {
        this.congestion = congestion;
        this.avoidhightspeed = avHighSpeed;
        this.cost = avCost;
        this.hightspeed = highSpeed;
    }

    @Override
    public OfflineMapManager getOfflineMapManager() {
        return mDownMapManager;
    }


    @Override
    public void nearBy(int pathId, int stepNum, int poiNum) {
        aMapNavi.stopNavi();
        aMapNavi.selectRouteId(pathId);
        aMapNavi.startNavi(AMapNavi.GPSNaviMode);
        for (XpRouteListener listener:mRouteListeners){
            listener.nearBy(pathId,stepNum,poiNum);
        }
    }




}
