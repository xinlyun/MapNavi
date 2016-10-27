package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.xiaopeng.xmapnavi.view.appwidget.activity.RouteNaviActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linzx on 2016/10/18.
 */
public class RadarNaviFragment  extends Fragment implements XpRouteListener,XpNaviCalueListener
            ,XpLocationListener , LocationSource
            , XpNaviInfoListener,View.OnClickListener{
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
    public void setMapView(MapView mapView){
        this.mMapView = mapView;
        mAmap = mapView.getMap();
    }

    public void setToPoint(LatLonPoint toPoint){
        this.toPoint = toPoint;
    }


    private View rootView;
    private ILocationProvider mLocationPro;
    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

    private Button mBtnStart;
    private TextView mTvShowMsg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationPro = LocationProvider.getInstence(getActivity());
        mAmap.setLocationSource(this);// 设置定位监听

        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mLocationPro.startRouteNavi();
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
        findViewById(R.id.btn_see_all).setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }


    @Override
    public void onStart() {
        super.onStart();
        LogUtils.d(TAG,"onStart");
        mLocationPro.addLocationListener(this);
        mLocationPro.addNaviCalueListner(this);
        mLocationPro.addRouteListener(this);
        mLocationPro.addNaviInfoListner(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.d(TAG,"onStop");
        try {
            mLocationPro.removeNaviInfoListener(this);
            mLocationPro.removeLocationListener(this);
            mLocationPro.removeNaviCalueListner(this);
            mLocationPro.removeRouteListener(this);
        }catch (Exception e){

        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (mLisenerClient != null){
            mLisenerClient.onLocationChanged(aMapLocation);
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
        List<NaviLatLng> startPoi = new ArrayList<>();
        List<NaviLatLng> wayPoi = new ArrayList<>();
        List<NaviLatLng> endPoi = new ArrayList<>();
        startPoi.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
        endPoi.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
        mLocationPro.calueRunWay(startPoi,wayPoi,endPoi);
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


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess ints size:"+ints.length);
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
            routeOverlays.get(key).setTransparency(0.4f);
        }
        try {

            routeOverlays.get(routeID).setTransparency(1f);
            /**
             * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
             **/
            routeOverlays.get(routeID).setZindex(zindex+1);
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
}
