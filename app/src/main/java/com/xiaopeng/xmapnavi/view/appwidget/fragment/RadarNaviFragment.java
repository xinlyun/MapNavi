package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.iflytek.cloud.util.ResourceUtil;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSensorListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RouteNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MapFloatView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linzx on 2016/10/18.
 */
public class RadarNaviFragment  extends Fragment implements XpRouteListener,XpNaviCalueListener
        ,XpLocationListener , LocationSource
        , XpNaviInfoListener,View.OnClickListener
        , XpSensorListener ,AMap.OnCameraChangeListener{
    private static final String TAG = "RadarNaviFragment";
    private MapView mMapView;
    private AMap mAmap;
    private HashMap<Integer,AMapNaviPath> mPaths;
    private int[] ints;
    private OnLocationChangedListener mLisenerClient;
    private LatLonPoint fromPoint,toPoint;
    int routeID =1 ;
    private int zindex = 1;

    private long timeSave;
    private LatLng poiSave;

    private MapFloatView mMapFloatView;
    private Marker mLineMarker0,mLineMarker1;

    private static final int LINE_TYPE_TOP=0 ;
    private static final int LINE_TYPE_BOTTOM=1 ;
    private static final int LINE_TYPE_LEFT=2 ;
    private static final int LINE_TYPE_RIGHT=3 ;
    private static final int[] ANGLE_STYLE = {
            0,180,90,270
    };
    private int style0,style1;
    public void setMapView(MapView mapView){
        this.mMapView = mapView;
        mAmap = mapView.getMap();
        mAmap .setMapType(AMap.MAP_TYPE_NAVI);
    }

    public void setToPoint(LatLonPoint toPoint){
        LogUtils.d(TAG,"setToPoint:toPoint:"+toPoint);

        this.toPoint = new LatLonPoint(toPoint.getLatitude(),toPoint.getLongitude());
    }


    private View rootView;
    private ILocationProvider mLocationPro;
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    private Button mBtnStart;
    private TextView mTvShowMsg;
    private Marker mLocationMarker;
    private boolean isFirst = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.statisticsStart(BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
        super.onCreate(savedInstanceState);
        mLocationPro = LocationProvider.getInstence(getActivity());

        mAmap.setLocationSource(this);// 设置定位监听

        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mAmap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG,"onDestroy");

    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        BugHunter.statisticsEnd(getActivity().getApplication(),BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_rader_navi,container,false);
        mAmap.clear();

        initView();
        return rootView;
    }

    void initView(){
        mBtnStart = (Button) findViewById(R.id.btn_start_navi);
        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);
        mMapFloatView = (MapFloatView) findViewById(R.id.mfv_show);
        mMapFloatView.initAmap(mAmap);
        findViewById(R.id.btn_see_all).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
        MarkerOptions options  = new MarkerOptions();

        options.icon(BitmapDescriptorFactory.fromResource(com.xiaopeng.amaplib.R.drawable.navi_map_gps_locked));

        mLocationMarker = mAmap.addMarker(options);
        mLocationMarker.setAnchor(0.5f,0.5f);

    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }


    @Override
    public void onStart() {
        super.onStart();
        LogUtils.d(TAG,"onStart");
        mAmap.getUiSettings().setAllGesturesEnabled(true);
        mAmap.setOnCameraChangeListener(this);
        mLocationPro.addLocationListener(this);
        mLocationPro.addNaviCalueListner(this);
        mLocationPro.addRouteListener(this);
        mLocationPro.addNaviInfoListner(this);
        mLocationPro.addSensorListner(this);
        if (isFirst){
            if(mLocationPro==null || mLocationPro.getAmapLocation()==null){
                getActivity().finish();
            }
            beginFirst();
            AMapLocation location = mLocationPro.getAmapLocation();
            fromPoint = new LatLonPoint(location.getLatitude(),location.getLongitude());
            LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                    .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 30);
            mAmap.animateCamera(update);

            mAmap.animateCamera(CameraUpdateFactory.zoomTo(15));

            reCalue();
            isFirst = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.d(TAG,"onStop");
        try {
            mLocationPro.removeSensorListner(this);
            mLocationPro.removeNaviInfoListener(this);
            mLocationPro.removeLocationListener(this);
            mLocationPro.removeNaviCalueListner(this);
            mLocationPro.removeRouteListener(this);
            mAmap.setOnCameraChangeListener(null);
        }catch (Exception e){

        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

//        if (mLisenerClient != null){
//            mLisenerClient.onLocationChanged(aMapLocation);
//        }

        if (mLocationMarker != null){
            LogUtils.d(TAG,"mlocation:"+aMapLocation);
            LogUtils.d(TAG,"mlocation:Angle:"+aMapLocation.getBearing());
            mLocationMarker.setPosition(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));

            mLocationMarker.setRotateAngle(aMapLocation.getBearing());
        }

        if (fromPoint!=null) {
            fromPoint.setLatitude(aMapLocation.getLatitude());
            fromPoint.setLongitude(aMapLocation.getLongitude());
        }else {
            fromPoint = new LatLonPoint(aMapLocation.getLatitude(),aMapLocation.getLongitude());
        }

        if (poiSave == null){
            poiSave = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
            timeSave = System.currentTimeMillis();
        }else {
            float dis = AMapUtils.calculateLineDistance(poiSave, new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
            if (dis > 500 || (System.currentTimeMillis() - timeSave)> 60 * 1000){
                poiSave = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                timeSave = System.currentTimeMillis();
                LogUtils.d(TAG,"ready rechange:");
                reCalue();
            }
        }

    }

    private void reCalue(){
        LogUtils.d(TAG,"reCalue: fromPoint:"+fromPoint
        );
        LogUtils.d(TAG,"reCalue: toPoint:"+toPoint);
//        List<NaviLatLng> startPoi = new ArrayList<>();
//        List<NaviLatLng> wayPoi = new ArrayList<>();
        List<NaviLatLng> endPoi = new ArrayList<>();

//        startPoi.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));

        endPoi.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
        mLocationPro.tryCalueRunWay(endPoi);
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }


    private void clearPathLay(){
        if (routeOverlays.size() > 0) {
            for (int i = 0; i < ints.length; i++) {
                try {
                    routeOverlays.get(ints[i]).removeFromMap();
                } catch (Exception e) {

                }

            }
        }
        routeOverlays.clear();
        if (mLineMarker0!=null){
            mLineMarker0.remove();
            mLineMarker0 = null;
        }
        if (mLineMarker1!=null){
            mLineMarker1.remove();
            mLineMarker1 = null;
        }
    }

    private void seeAll(){
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);

                    if (toPoint!=null && fromPoint!=null) {
                        LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                                .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
                        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                        mAmap.animateCamera(update);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },50);
    }

    private void beginFirst(){
        this.ints = mLocationPro.getPathsInts();
        routeID = ints[0];
        mPaths = mLocationPro.getNaviPaths();
        if (mPaths !=null) {
            LogUtils.d(TAG, "onCalculateMultipleRoutesSuccess getPath size:" + mPaths.size());
        }

        if (mPaths != null && mPaths.size() > 1 && ints != null){
            for (int i = 0; i < ints.length; i++) {
                AMapNaviPath path = mPaths.get(ints[i]);
                if (path != null) {
                    drawRoutes(ints[i], path);
                }
            }
        } else {
//            AMapNaviPath path = mLocationPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
//            drawRoutes(-1, path);
        }


        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);

                    if (toPoint!=null && fromPoint!=null) {
                        LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                                .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
                        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                        mAmap.animateCamera(update);
                    }
                    changeRoute();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },50);
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawInfoMarker();
            }
        },100);

    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess ints size:"+ints.length);
        mLocationPro.startRouteNavi();
        clearPathLay();
        this.ints = ints;
        routeID = ints[0];
        mPaths = mLocationPro.getNaviPaths();

        if (mPaths !=null) {
            LogUtils.d(TAG, "onCalculateMultipleRoutesSuccess getPath size:" + mPaths.size());
        }

        if (mPaths != null && mPaths.size() > 1 && ints != null){
            for (int i = 0; i < ints.length; i++) {
                AMapNaviPath path = mPaths.get(ints[i]);
                if (path != null) {
                    drawRoutes(ints[i], path);
                }
            }
        } else {
//            AMapNaviPath path = mLocationPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
//            drawRoutes(-1, path);
        }


        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);

                    if (toPoint!=null && fromPoint!=null) {
                        LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                                .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
                        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                        mAmap.animateCamera(update);
                    }
                    changeRoute();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },50);
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawInfoMarker();
            }
        },100);

    }

    private void drawInfoMarker(){
        if (mPaths==null || mPaths.size()==1)return;
        AMapNaviPath path0 = mPaths.get(ints[0]);
        AMapNaviPath path1 = mPaths.get(ints[1]);
        AMapNaviPath path2 = null;
        if (mPaths.size()==3){
            path2 = mPaths.get(ints[2]);
        }

        int allsize0 = path0.getCoordList().size();
        int allsize1 = path1.getCoordList().size();
        int allsize2 = 0;
        if (path2!=null){
            allsize2 = path2.getCoordList().size();
        }
        NaviLatLng wayPath0Point0 = path0.getCoordList().get(allsize0/3);
        NaviLatLng wayPath0Point1 = path0.getCoordList().get(2*allsize0/3);
        NaviLatLng wayPath1Point0 = path1.getCoordList().get(allsize1/3);
        NaviLatLng wayPath1Point1 = path1.getCoordList().get(2*allsize1/3);
        NaviLatLng wayPath2Point0 = null;
        NaviLatLng wayPath2Point1 = null;
        if (path2!=null) {
            wayPath2Point0 = path2.getCoordList().get(allsize2 / 3);
            wayPath2Point1 = path2.getCoordList().get(2 * allsize2 / 3);
        }
        style0 = pointInto(wayPath1Point0,wayPath0Point0,wayPath2Point0);
        style1 = -1;
        if (path2!=null){
            style1 = pointInto(wayPath2Point1,wayPath0Point1,wayPath1Point1);
        }

        MarkerOptions options  = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.line_icon));
        mLineMarker0 = mAmap.addMarker(options);
        mLineMarker0.setAnchor(0.5f,1f);
        mLineMarker0.setPosition(new LatLng(wayPath1Point0.getLatitude(),wayPath1Point0.getLongitude()));
        mLineMarker0.setRotateAngle(ANGLE_STYLE[style0]);

        if (path2!=null) {
            MarkerOptions options1 = new MarkerOptions();
            options1.icon(BitmapDescriptorFactory.fromResource(R.drawable.line_icon));
            mLineMarker1 = mAmap.addMarker(options1);
            mLineMarker1.setAnchor(0.5f, 1f);
            mLineMarker1.setPosition(new LatLng(wayPath2Point1.getLatitude(), wayPath2Point1.getLongitude()));
            mLineMarker1.setRotateAngle(ANGLE_STYLE[style1]);
        }

        if (mLineMarker1==null) {
            mMapFloatView.setPoint(mLineMarker0.getPosition(),style0,null,-1);
        }else {
            mMapFloatView.setPoint(mLineMarker0.getPosition(),style0,mLineMarker1.getPosition(),style1);
        }

    }

    private int pointInto(NaviLatLng startLatLng,NaviLatLng rightLatlng,@Nullable NaviLatLng otherLatlng){
        //TODO
        if (otherLatlng!=null){
//            PolylineOptions polylineOptions = new PolylineOptions();
//            polylineOptions.add(new LatLng(rightLatlng.getLatitude(),rightLatlng.getLongitude()));
//            polylineOptions.add(new LatLng(otherLatlng.getLatitude(),otherLatlng.getLongitude()));
//            polylineOptions.color(getResources().getColor(R.color.red));
//            mAmap.addPolyline(polylineOptions);
            double newLat = (rightLatlng.getLatitude()+otherLatlng.getLatitude())/2f;
            double newLon = (rightLatlng.getLongitude()+otherLatlng.getLongitude())/2f;
            double disLat = newLat - startLatLng.getLatitude();
            double disLon = newLon - startLatLng.getLongitude();
            if (Math.abs(disLat) > Math.abs(disLon)){
                if (disLat>0)return LINE_TYPE_BOTTOM;
                else return LINE_TYPE_TOP;
            }else {
                if (disLat>0)return LINE_TYPE_LEFT;
                else return LINE_TYPE_RIGHT;
            }
        }else {
            double disLat = rightLatlng.getLatitude() - startLatLng.getLatitude();
            double disLon = rightLatlng.getLongitude() - startLatLng.getLongitude();
            if (Math.abs(disLat)>Math.abs(disLon)){
                //is not right ?
                if (disLon>0)return LINE_TYPE_RIGHT;
                else return LINE_TYPE_LEFT;
            }else {
                if (disLat>0)return LINE_TYPE_BOTTOM;
                else return LINE_TYPE_TOP;
            }
        }
    }

    public void changeRoute() {

        if (routeOverlays.size() == 1) {
//            chooseRouteSuccess = true;
//            Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }


        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.54f);
        }
        try {

            LogUtils.d(TAG,"changeRoute:routeID="+routeID);
            RouteOverLay routeOverLay = routeOverlays.get(routeID);
            if (routeOverLay==null)return;
            routeOverLay.setTransparency(1f);
            /**
             * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
             **/
            routeOverLay.setZindex(zindex+1);
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.e(TAG,"changeRoute error:routeId = "+routeID);
        }

//        AMapNaviPath path  = mPaths.get(ints[routeIndex]);
//        int traListhNum = getTrafficLightNum(path);
//        int cost = path.getTollCost();
//        String msgShow = "花费："+cost+"元   经过"+traListhNum+"个红绿灯";
//        mTvShowMsg.setText(msgShow);
    }

    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void nearBy(int pathId, int stepNum, int poiNum) {
        LogUtils.d(TAG,"nearBy:"+pathId);
        routeID = pathId;
        changeRoute();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLisenerClient = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mLisenerClient = null;
    }


    private void drawRoutes(int routeId, AMapNaviPath path) {
        LogUtils.d(TAG,"drawRoutes id:"+routeId);
        mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, getActivity());
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.title_back_00));
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();

        routeOverlays.put(routeId, routeOverLay);
    }


    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        if (naviInfo != null) {
            String str = "" + naviInfo.getCurStepRetainDistance() + "米后,从" + naviInfo.getCurrentRoadName() + "进入" + naviInfo.getNextRoadName() +
                    "\n剩余" + naviInfo.getPathRetainTime() / 60 + "分钟," + naviInfo.getPathRetainDistance() / 1000 + "公里";
            if (mTvShowMsg != null) {
                mTvShowMsg.setText(str);
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_see_all:
                seeAll();
                break;

            case R.id.btn_start_navi:
                startNavi();
                break;

            case R.id.btn_exit:
                getActivity().finish();
                break;

            default:
                break;

        }
    }

    private void startNavi(){
        Intent intent = new Intent(getActivity(), RouteNaviActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG,"onDestroyView");
        try {
            if (ints != null){
                routeID = ints[ints.length-1];
            }

            mLocationPro.removeRouteListener(this);
//            mAmap.setMyLocationEnabled(false);
//        mAmap.setLocationSource(null);
            mLocationPro.stopRouteNavi();
            super.onDestroyView();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.values!=null && event.values.length>0) {
            float bearing = event.values[0];
            if (mLocationMarker!=null){
                mLocationMarker.setRotateAngle(360 - bearing);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mLineMarker0!=null){
            if (mLineMarker1!=null) {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, mLineMarker1.getPosition(), style1);
            }else {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, null, style1);
            }
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (mLineMarker0!=null){
            if (mLineMarker1!=null) {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, mLineMarker1.getPosition(), style1);
            }else {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, null, style1);
            }
        }
    }
}
