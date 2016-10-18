package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
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
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/12.
 */
public class LocationProvider implements ILocationProvider,AMapLocationListener,AMapNaviListener
        ,PoiSearch.OnPoiSearchListener{
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
    private PoiSearch mPoiSearch;

    private boolean congestion, cost, hightspeed, avoidhightspeed;

    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    //-Resume-//
    private PoiResult mPoiResult;
    private PoiItem mPoiItem;
    //--------//

    private long timeSave  = 0;

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
        updateLoction.sendEmptyMessageDelayed(0,200);
    }

    @Override
    public void removeLocationListener(XpLocationListener xpLocationListener) {
        mListeners.remove(xpLocationListener);
    }

    @Override
    public void addSearchListner(XpSearchListner xpSearchListner) {
        mSearchListeners.add(xpSearchListner);
        Log.d(TAG,"addSearchListner size:"+mSearchListeners.size()+"\nlistner:"+xpSearchListner.toString());

    }

    @Override
    public void removeSearchListner(XpSearchListner xpSearchListner) {
        Log.d(TAG,"removeSearchListner size:"+mSearchListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mSearchListeners.remove(xpSearchListner);
    }

    @Override
    public void addNaviCalueListner(XpNaviCalueListener xpSearchListner) {
        Log.d(TAG,"addNaviCalueListner size:"+mNaviCalueListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mNaviCalueListeners.add(xpSearchListner);
    }

    @Override
    public void removeNaviCalueListner(XpNaviCalueListener xpSearchListner) {
        Log.d(TAG,"removeNaviCalueListner size:"+mNaviCalueListeners.size()+"\nlistner:"+xpSearchListner.toString());
        mNaviCalueListeners.remove(xpSearchListner);
    }

    @Override
    public void trySearchPosi(String str) {
        beginSearchAddr(str);
    }

    @Override
    public void calueRunWay(List<NaviLatLng> startList,List<NaviLatLng> wayList,List<NaviLatLng> endList) {
        if (avoidhightspeed && hightspeed) {
            Log.d(TAG,"不走高速与高速优先不能同时为true.");
        }
        if (cost && hightspeed) {
            Log.d(TAG,"高速优先与避免收费不能同时为true");
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
            Log.d(TAG,"策略:" + strategyFlag);
        }
    }

    @Override
    public AMapNaviPath getNaviPath() {
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
        mListeners = new ArrayList<>();
        mSearchListeners = new ArrayList<>();
        mNaviCalueListeners = new ArrayList<>();
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

        updateLoction.sendEmptyMessageDelayed(1,1000);
    }

    Handler updateLoction = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    onLocationChanged(LocationProvider.this.mAmapLocation);
                    break;
                case 1:
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
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }


    /* 以下都是Navi的回调 */
    @Override
    public void onInitNaviFailure() {
        Log.d(NAVI_TAG,"onInitNaviFailure");
    }

    @Override
    public void onInitNaviSuccess() {
        Log.d(NAVI_TAG,"onInitNaviSuccess");
    }

    @Override
    public void onStartNavi(int i) {
        Log.d(NAVI_TAG,"onStartNavi: i ="+i);
    }

    @Override
    public void onTrafficStatusUpdate() {
        Log.d(NAVI_TAG,"onTrafficStatusUpdate");
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        Log.d(NAVI_TAG,"onLocationChange");
        for (XpLocationListener lisener:mListeners){
            lisener.onLocationChange(aMapNaviLocation);
        }
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        Log.d(NAVI_TAG,"onGetNavigationText i="+i+"\ns:"+s);
        ttsManager.startSpeaking(s);
    }

    @Override
    public void onEndEmulatorNavi() {
        Log.d(NAVI_TAG,"onEndEmulatorNavi");
    }

    @Override
    public void onArriveDestination() {
        Log.d(NAVI_TAG,"onArriveDestination");
    }

    @Override
    public void onArriveDestination(NaviStaticInfo naviStaticInfo) {
        Log.d(NAVI_TAG,"onArriveDestination");
    }

    @Override
    public void onCalculateRouteSuccess() {
        Log.d(NAVI_TAG,"onCalculateRouteSuccess");
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            listener.onCalculateRouteSuccess();
        }
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        Log.d(NAVI_TAG,"onCalculateRouteFailure i="+i);
    }

    @Override
    public void onReCalculateRouteForYaw() {
        Log.d(NAVI_TAG,"onReCalculateRouteForYaw");
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        Log.d(NAVI_TAG,"onReCalculateRouteForTrafficJam");
    }

    @Override
    public void onArrivedWayPoint(int i) {
        Log.d(NAVI_TAG,"onArrivedWayPoint");
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        Log.d(NAVI_TAG,"onGpsOpenStatus b="+b);
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
        Log.d(NAVI_TAG,"onNaviInfoUpdated");
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        Log.d(NAVI_TAG,"onNaviInfoUpdate");
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo info) {
        Log.d(NAVI_TAG,"OnUpdateTrafficFacility");
        if (info!=null){
            Log.d(NAVI_TAG, "(trafficFacilityInfo.coor_X+trafficFacilityInfo.coor_Y+trafficFacilityInfo.distance+trafficFacilityInfo.limitSpeed):"
                    + (info.getCoorX() + info.getCoorY() + info.getDistance() + info.getLimitSpeed()));
        }

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        Log.d(NAVI_TAG,"OnUpdateTrafficFacility");

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        Log.d(NAVI_TAG,"showCross");
    }

    @Override
    public void hideCross() {
        Log.d(NAVI_TAG,"hideCross");
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        Log.d(NAVI_TAG,"showLaneInfo");
    }

    @Override
    public void hideLaneInfo() {
        Log.d(NAVI_TAG,"hideLaneInfo");
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        Log.d(NAVI_TAG,"onCalculateMultipleRoutesSuccess");
        for (XpNaviCalueListener listener:mNaviCalueListeners){
            listener.onCalculateMultipleRoutesSuccess(ints);
        }
    }

    @Override
    public void notifyParallelRoad(int i) {
        Log.d(NAVI_TAG,"notifyParallelRoad");
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        Log.d(NAVI_TAG,"OnUpdateTrafficFacility");
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        Log.d(NAVI_TAG,"updateAimlessModeStatistics");
        Log.d(TAG, "distance=" + aimLessModeStat.getAimlessModeDistance());
        Log.d(TAG, "time=" + aimLessModeStat.getAimlessModeTime());
    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        Log.d(NAVI_TAG,"updateAimlessModeCongestionInfo");
        Log.d(TAG, "roadName=" + aimLessModeCongestionInfo.getRoadName());
        Log.d(TAG, "CongestionStatus=" + aimLessModeCongestionInfo.getCongestionStatus());
        Log.d(TAG, "eventLonLat=" + aimLessModeCongestionInfo.getEventLon() + "," + aimLessModeCongestionInfo.getEventLat());
        Log.d(TAG, "length=" + aimLessModeCongestionInfo.getLength());
        Log.d(TAG, "time=" + aimLessModeCongestionInfo.getTime());
        for (AMapCongestionLink link :
                aimLessModeCongestionInfo.getAmapCongestionLinks()) {
            Log.d(TAG, "status=" + link.getCongestionStatus());
            for (NaviLatLng latlng : link.getCoords()
                    ) {
                Log.d(TAG, latlng.toString());
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
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(30*1000);//可选，设置定位间隔。默认为2秒
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
        query.setPageSize(10);
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
        Log.d(TAG,"onPoiSearched i:"+i);
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
        aMapNavi.setEmulatorNaviSpeed(60);
        return aMapNavi.startNavi(var1);
    }

    @Override
    public void stopNavi() {
        aMapNavi.stopNavi();
        aMapNavi.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED);
    }

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


}
