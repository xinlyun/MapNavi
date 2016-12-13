package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.view.RouteOverLay;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.amap.api.services.core.LatLonPoint;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.FindForWardPoi;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpRouteListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSensorListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RouteNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MRouteOverLay;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MapFloatView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/18.
 */
public class RadarNaviFragment  extends Fragment implements XpRouteListener,XpNaviCalueListener
        ,XpLocationListener , LocationSource
        , XpNaviInfoListener,View.OnClickListener
        , XpSensorListener ,AMap.OnCameraChangeListener

{
    private static final String TAG = "RadarNaviFragment";
    private MapView mMapView;
    private AMap mAmap;
    private HashMap<Integer, AMapNaviPath> mPaths;
    private int[] ints;
    private OnLocationChangedListener mLisenerClient;
    private LatLonPoint fromPoint, toPoint;
    int routeID = 1;
//    private int zindex = 2;
    private ImageView mIvLukuang;
    private long timeSave;
    private LatLng poiSave;
    private MapFloatView mMapFloatView;
    private Marker mLineMarker0, mLineMarker1;
    private Marker mMarker0, mMarker1;
    private ImageView mIvShowNaviInfo,mIvBroadCast;

    private static final int LINE_TYPE_TOP = 0;
    private static final int LINE_TYPE_BOTTOM = 1;
    private static final int LINE_TYPE_LEFT = 2;
    private static final int LINE_TYPE_RIGHT = 3;
    private static final int[] ANGLE_STYLE = {
            0, 180, 90, 270
    };

    private int[] imgId = {
            R.drawable.navi_icon_9_small,R.drawable.navi_icon_9_small,R.drawable.navi_icon_2_small,R.drawable.navi_icon_3_small
            ,R.drawable.navi_icon_4_small,R.drawable.navi_icon_5_small,R.drawable.navi_icon_6_small,R.drawable.navi_icon_7_small
            ,R.drawable.navi_icon_8_small
            ,R.drawable.navi_icon_9_small,R.drawable.navi_icon_10_small,R.drawable.navi_icon_11_small,R.drawable.navi_icon_12_small
            ,R.drawable.navi_icon_13_small,R.drawable.navi_icon_14_small,R.drawable.navi_icon_15_small,R.drawable.navi_icon_16_small
    };

    private TextView mTxShowShengyu;

    BaseFuncActivityInteface mActivity;

    private Polyline mPoline0, mPoline1, mPoline2;
    private int style0, style1;

    public void setMapView(MapView mapView) {
        this.mMapView = mapView;
        mAmap = mapView.getMap();

    }

    public void setToPoint(LatLonPoint toPoint) {
        LogUtils.d(TAG, "setToPoint:toPoint:" + toPoint);

        this.toPoint = new LatLonPoint(toPoint.getLatitude(), toPoint.getLongitude());
    }


    private View rootView;
    private ILocationProvider mLocationPro;
    private HashMap<Integer,MRouteOverLay> routeOverLays = new HashMap<>();
    private List<MRouteOverLay> routeOverLayList = new ArrayList<>();
    /**
     * 保存当前算好的路线
     */
//    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();
    private RouteOverLay mRouteOverlay;
    private TextView mTvShowMsg,mTvShowRoad;

    private Marker mLocationMarker;
    private boolean isFirst = true;
    private boolean isTriffice = false;

    private Marker markerFromPoi, markerEndPoi;

    private int remebTime = 0;
    private int remebLenght = 0;
    private boolean isBroadCast = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START, TAG, BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
        mLocationPro = LocationProvider.getInstence(getActivity());

        mAmap.setLocationSource(this);// 设置定位监听

        mAmap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mAmap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        mActivity = (BaseFuncActivityInteface) getActivity();

        mAmap.setOnPolylineClickListener(polylineClickListener);
        mLocationPro.muteLaught();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "onDestroy");
        mLocationPro.unmuteLaught();
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
        mAmap.setMapType(AMap.MAP_TYPE_NAVI);
        BugHunter.countTimeEnd(getActivity().getApplication(), BugHunter.TIME_TYPE_START, TAG, BugHunter.SWITCH_TYPE_START_COOL);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_rader_navi, container, false);
//        mAmap.clear();

        initView();
        mLocationPro.addNaviInfoListner(this);
        return rootView;
    }

    void initView() {
        mTvShowMsg = (TextView) findViewById(R.id.tv_show_msg);
        mTvShowRoad = (TextView) findViewById(R.id.tv_show_other);
        mMapFloatView = (MapFloatView) findViewById(R.id.mfv_show);
        mMapFloatView.initAmap(mAmap);
        mIvLukuang = (ImageView) findViewById(R.id.iv_lukuang);
        mTxShowShengyu = (TextView) findViewById(R.id.tv_show_shengyu);
        mIvShowNaviInfo = (ImageView) findViewById(R.id.iv_show_navi_icon);
        mIvBroadCast = (ImageView) findViewById(R.id.iv_broadcast);
        mIvBroadCast.setOnClickListener(this);
        findViewById(R.id.btn_see_all).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_lukuang).setOnClickListener(this);
        findViewById(R.id.btn_start_navi).setOnClickListener(this);
        MarkerOptions options = new MarkerOptions();

        options.icon(BitmapDescriptorFactory.fromResource(com.xiaopeng.amaplib.R.drawable.navi_map_gps_locked));

        mLocationMarker = mAmap.addMarker(options);
        mLocationMarker.setAnchor(0.5f, 0.5f);

        findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
        findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
        mMapFloatView.setFloatViewTouchListener(xpFloatViewTouchListener);

    }


    private View findViewById(int id) {
        return rootView.findViewById(id);
    }


    @Override
    public void onStart() {
        super.onStart();
        LogUtils.d(TAG, "onStart");
        mAmap.getUiSettings().setAllGesturesEnabled(true);
        mAmap.setOnCameraChangeListener(this);
        mLocationPro.addLocationListener(this);
        mLocationPro.addNaviCalueListner(this);
        mLocationPro.addRouteListener(this);

        mLocationPro.addSensorListner(this);


        if (isFirst) {
            if (mLocationPro == null || mLocationPro.getAmapLocation() == null) {
                getActivity().finish();
            }
//            beginFirst();
            AMapLocation location = mLocationPro.getAmapLocation();
            fromPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
//            LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
//                    .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
//            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 30);
//            mAmap.animateCamera(update);
//
//            mAmap.animateCamera(CameraUpdateFactory.zoomTo(15));

            isFirst = false;


            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_from_poi));
            options.anchor(0.5f, 1f);
            options.position(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()));
            markerFromPoi = mAmap.addMarker(options);

            MarkerOptions options1 = new MarkerOptions();
            options1.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end_poi));
            options1.anchor(0.5f, 1f);
            options1.position(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()));
            markerEndPoi = mAmap.addMarker(options1);

            this.onCalculateMultipleRoutesSuccess(mLocationPro.getPathsInts());
//            beginFirst();

        }
        isTriffice = mAmap.isTrafficEnabled();
        if (mIvLukuang!=null) {
            if (isTriffice) {
                mIvLukuang.setImageResource(R.drawable.icon_lukuang_01);
            } else {
                mIvLukuang.setImageResource(R.drawable.icon_lukuang_02);
            }
        }
        mAmap.setTrafficEnabled(isTriffice);

    }

    @Override
    public void onStop() {
        super.onStop();
        LogUtils.d(TAG, "onStop");
        try {
            mLocationPro.removeSensorListner(this);

            mLocationPro.removeLocationListener(this);
            mLocationPro.removeNaviCalueListner(this);
            mLocationPro.removeRouteListener(this);
            mAmap.setOnCameraChangeListener(null);
        } catch (Exception e) {

        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {


        if (mLocationMarker != null) {
            LogUtils.d(TAG, "mlocation:" + aMapLocation);
            LogUtils.d(TAG, "mlocation:Angle:" + aMapLocation.getBearing());
            mLocationMarker.setPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));

            mLocationMarker.setRotateAngle(aMapLocation.getBearing());
        }

        if (fromPoint != null) {
            fromPoint.setLatitude(aMapLocation.getLatitude());
            fromPoint.setLongitude(aMapLocation.getLongitude());
        } else {
            fromPoint = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        }

        if (poiSave == null) {
            poiSave = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            timeSave = System.currentTimeMillis();
        } else {
            float dis = AMapUtils.calculateLineDistance(poiSave, new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
            if (dis > 500 || (System.currentTimeMillis() - timeSave) > 60 * 1000) {
                poiSave = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                timeSave = System.currentTimeMillis();
                LogUtils.d(TAG, "ready rechange:");
                reCalue();
            }
        }

    }

    private void reCalue() {
        LogUtils.d(TAG, "reCalueInNavi: fromPoint:" + fromPoint
        );
        LogUtils.d(TAG, "reCalueInNavi: toPoint:" + toPoint);
        List<NaviLatLng> endPoi = new ArrayList<>();


        endPoi.add(new NaviLatLng(toPoint.getLatitude(), toPoint.getLongitude()));
        mLocationPro.stopNavi();
        mLocationPro.tryCalueRunWay(endPoi);
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }


    private void seeAll() {
        watchAll();
    }

    private int getMainRouteByMsg(){
        int disDiff = 6000;
        int rouid = ints[0];
        for (int i = 0;i<ints.length;i++){
            AMapNaviPath path = mPaths.get(ints[i]);
            int lenght = path.getAllLength();
            int dif = Math.abs(lenght - remebLenght);
            if (dif<disDiff){
                disDiff = dif;
                rouid = ints[i];
            }
        }
        return rouid;
    }


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(TAG, "onCalculateMultipleRoutesSuccess ints size:" + ints.length);
//        mLocationPro.startRouteNavi();
//        clearPathLay();
        this.ints = ints;
        this.mPaths = mLocationPro.getNaviPaths();
        routeID = getMainRouteByMsg();
        if (mPaths != null) {
            LogUtils.d(TAG, "onCalculateMultipleRoutesSuccess getPath size:" + mPaths.size());
        }
        if (ints.length > 1) {
            AMapNaviPath path0 = mPaths.get(ints[0]);
            AMapNaviPath path1 = mPaths.get(ints[1]);
            AMapNaviPath path2 = null;
            if (ints.length == 3) {
                path2 = mPaths.get(ints[2]);
            }
            if (mPoline0 != null) {
                mPoline0.remove();
                mPoline0 = null;

            }
            mPoline0 = drawPolyLine(path0);
            mPoline0.setZIndex(0);

            if (mPoline1 != null) {
                mPoline1.remove();
                mPoline1 = null;
            }
            mPoline1 = drawPolyLine(path1);
            mPoline1.setZIndex(0);

            if (mPoline2 != null) {
                mPoline2.remove();
                mPoline2 = null;
            }
            if (path2 != null) {
                mPoline2 = drawPolyLine(path2);
                mPoline2.setZIndex(0);
            }
        }
        watchAll();
        drawAllPathLine();

        changeRoute();
    }

    private void drawInfoLine() {
        if (mPaths.size() > 1) {
            AMapNaviPath path0 = mPaths.get(routeID);
            AMapNaviPath path1 = null, path2 = null;

            int in0 = ints[0];
            int in1 = ints[1];
            int in2 = -1;
            if (ints.length == 3) {
                in2 = ints[2];
            }
            if (routeID == in0) {
                path1 = mPaths.get(in1);
                if (in2 != -1) {
                    path2 = mPaths.get(in2);
                }

            } else if (routeID == in1) {
                path1 = mPaths.get(in0);
                if (in2 != -1) {
                    path2 = mPaths.get(in2);
                }

            } else if (routeID == in2) {
                path1 = mPaths.get(in0);
                path2 = mPaths.get(in1);
            }


            List<AMapNaviPath> paths = new ArrayList<>();
            paths.add(path0);
            if (path2 != null) {
                paths.add(path2);
            }
            FindForWardPoi findForWardPoi = new FindForWardPoi(path1, path0, path2);
            findForWardPoi.setOnFindPoiInPathListener(poiInPath0);
            findForWardPoi.start();

            if (path2 != null) {
                List<AMapNaviPath> paths0 = new ArrayList<>();
                paths0.add(path0);
                paths0.add(path1);
                FindForWardPoi findForWardPoix = new FindForWardPoi(path2, path0, path1);
                findForWardPoix.setOnFindPoiInPathListener(poiInPath1);
                findForWardPoix.start();
            }
        }
    }


    public void changeRoute() {
        try {
            LogUtils.d(TAG, "changeRoute:routeID=" + routeID);
            watchAll();
            mLocationPro.stopNavi();
            drawInfoLine();
//            RouteOverLay routeOverLay = routeOverlays.get(routeID);
//            if (routeOverLay==null)return;
//            routeOverLay.setTransparency(1f);
            /**
             * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
             **/
            for (int i =0;i<ints.length;i++){
                int id = ints[i];
                if (id == routeID){
                    MRouteOverLay routeOverLay = routeOverLays.get(id);
                    routeOverLay.setTransparency(1f);
                }else {
                    MRouteOverLay routeOverLay = routeOverLays.get(id);
                    routeOverLay.setTransparency(1f);
                }
            }

//            routeOverLay.setZindex(zindex+1);

            AMapNaviPath path = mLocationPro.getNaviPaths().get(routeID);
            toPoint = new LatLonPoint(path.getEndPoint().getLatitude(),path.getEndPoint().getLongitude());
//            drawRoutes(routeID, path);
            drawApath(path);
            mLocationPro.selectRouteId(routeID);
            mLocationPro.startNavi(AMapNavi.GPSNaviMode);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "changeRoute error:routeId = " + routeID);
        }

//        AMapNaviPath path  = mPaths.get(ints[routeIndex]);
//        int traListhNum = getTrafficLightNum(path);
//        int cost = path.getTollCost();
//        String msgShow = "花费："+cost+"元   经过"+traListhNum+"个红绿灯";
//        mTvShowMsg.setText(msgShow);
    }

    private void drawApath(AMapNaviPath path){
        RouteOverLay onlySaveOverlay = null;
        if (mRouteOverlay!=null){
            onlySaveOverlay = mRouteOverlay;
            mRouteOverlay = null;
        }
        RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, getActivity());

        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));
        routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));

        markerFromPoi.setPosition(new LatLng(path.getStartPoint().getLatitude(), path.getStartPoint().getLongitude()));
        markerEndPoi.setPosition(new LatLng(path.getEndPoint().getLatitude(), path.getEndPoint().getLongitude()));

        routeOverLay.setZindex(2);
        routeOverLay.setTransparency(1f);
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        mRouteOverlay = routeOverLay;
        if (onlySaveOverlay!=null){
            onlySaveOverlay.removeFromMap();
        }
    }


    private void drawAllPathLine(){
        for (MRouteOverLay path :routeOverLayList){
            path.removeFromMap();
        }
        routeOverLays.clear();
        routeOverLayList.clear();
        for (int i = 0 ;i<ints.length;i++){
            int id = ints[i];
            AMapNaviPath mapNaviPath = mPaths.get(id);
            MRouteOverLay routeOverLay = drawRoutes(id,mapNaviPath);
            routeOverLays.put(id,routeOverLay);
            routeOverLayList.add(routeOverLay);
        }
    }


    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void onCalculateRouteFailure() {

    }

    @Override
    public void nearBy(int pathId, int stepNum, int poiNum) {
        LogUtils.d(TAG, "nearBy:" + pathId);
//        if (pathId!=routeID) {
//            routeID = pathId;
//            changeRoute();
//        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLisenerClient = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mLisenerClient = null;
    }


    private MRouteOverLay drawRoutes(int routeId, AMapNaviPath path) {
        LogUtils.d(TAG, "drawRoutes id:" + routeId);
//        if (mRouteOverlay != null) {
//            mRouteOverlay.removeFromMap();
//            mRouteOverlay = null;
//        }

        mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
        MRouteOverLay routeOverLay = new MRouteOverLay(mAmap, path, getActivity());

        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));
        routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.nothing_poi));

        markerFromPoi.setPosition(new LatLng(path.getStartPoint().getLatitude(), path.getStartPoint().getLongitude()));
        markerEndPoi.setPosition(new LatLng(path.getEndPoint().getLatitude(), path.getEndPoint().getLongitude()));

        routeOverLay.setZindex(1);
        routeOverLay.setTransparency(0.95f);
        routeOverLay.setTrafficLine(true);
        routeOverLay.addToMap();
        return routeOverLay;
//        mRouteOverlay = routeOverLay;
    }


    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        if (naviInfo != null) {
            int naviType = naviInfo.getIconType();
            if (naviType > imgId.length-1){
                mIvShowNaviInfo.setImageResource(R.drawable.navi_icon_9_small);
            }else {
                mIvShowNaviInfo.setImageResource(imgId[naviType]);
            }

            remebLenght = naviInfo.getPathRetainDistance();
            remebTime   = naviInfo.getPathRetainTime();

            String str = "" + naviInfo.getCurStepRetainDistance() + "米后" ;
            String str2 = "进入" + naviInfo.getNextRoadName();
            if (mTvShowMsg != null) {
                mTvShowMsg.setText(str);
            }
            if (mTvShowRoad !=null){
                mTvShowRoad.setText(str2);
            }
            String timeL;
            int allTime = naviInfo.getPathRetainTime();
            if (allTime > 60) allTime = allTime / 60;
            if (allTime > 60) {
                timeL = "剩余" + allTime / 60 + "小时" + allTime % 60 + "分钟 ";
            } else {
                timeL = "剩余" + allTime + "分钟 ";
            }

            String lenght;
            int dis = naviInfo.getPathRetainDistance();
            if (dis > 1000) {
                dis = dis / 1000;
                DecimalFormat df = new DecimalFormat("0.0");
                String result = df.format(dis);
                lenght = "" + result + "公里";
            } else {
                lenght = dis + "米";
            }
            mTxShowShengyu.setText(timeL + lenght);

        }

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_see_all:
                seeAll();
                break;

            case R.id.btn_start_navi:
                startNavi();
                break;

            case R.id.btn_exit:
//                getActivity().finish();
                mActivity.exitFragment();
                break;

            case R.id.btn_zoom_plus:
                if (mAmap != null) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                break;

            case R.id.btn_zoom_jian:
                if (mAmap != null) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomOut());
                }
                break;

            case R.id.btn_lukuang:
                isTriffice = !isTriffice;
                if (mAmap != null) {
                    mAmap.setTrafficEnabled(isTriffice);
                    if (isTriffice) {
                        mIvLukuang.setImageResource(R.drawable.icon_lukuang_01);
                    } else {
                        mIvLukuang.setImageResource(R.drawable.icon_lukuang_02);
                    }
                }
                break;

            case R.id.iv_broadcast:
                isBroadCast = !isBroadCast;
                if (isBroadCast){
                    mLocationPro.unmuteLaught();
                    mIvBroadCast.setImageResource(R.drawable.icon_broad_true);
                }else {
                    mLocationPro.muteLaught();
                    mIvBroadCast.setImageResource(R.drawable.icon_broad_false);
                }

                break;

            default:
                break;

        }
    }

    private void startNavi() {
        LogUtils.d(TAG, "startNavi");
        Intent intent = new Intent(getActivity(), RouteNaviActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
        mActivity.exitFragment();
    }

    @Override
    public void onDestroyView() {
        LogUtils.d(TAG, "onDestroyView");
        try {
            if (ints != null) {
                routeID = ints[ints.length - 1];
            }
            mLocationPro.removeNaviInfoListener(this);
            mLocationPro.removeRouteListener(this);
//            mAmap.setMyLocationEnabled(false);
//        mAmap.setLocationSource(null);
            mLocationPro.stopRouteNavi();
            super.onDestroyView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.values != null && event.values.length > 0) {
            float bearing = event.values[0];
            if (mLocationMarker != null) {
                mLocationMarker.setRotateAngle(360 - bearing);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        updateLineMarkerPosition();
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        updateLineMarkerPosition();
    }

    private void updateLineMarkerPosition() {
        if (mLineMarker0 != null) {
            if (mLineMarker1 != null) {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, mLineMarker1.getPosition(), style1);
            } else {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, null, style1);
            }
        }
    }

//    private Marker marker;


    private Polyline drawPolyLine(AMapNaviPath path) {
        List<LatLng> latLngs = naviLatlonToLatLon(path.getCoordList());
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new)).width(32).zIndex(0);
        return mAmap.addPolyline(polylineOptions);
    }

    private List<LatLng> naviLatlonToLatLon(List<NaviLatLng> naviLatLngs) {
        List<LatLng> latLngs = new ArrayList<>();
        for (NaviLatLng naviLatLng : naviLatLngs) {
            latLngs.add(new LatLng(naviLatLng.getLatitude(), naviLatLng.getLongitude()));
        }
        return latLngs;
    }

    private float disMwithLat(NaviLatLng latLng, NaviLatLng other) {
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(), latLng.getLongitude()), new LatLng(other.getLatitude(), latLng.getLongitude()));
    }

    private float disMwithLon(NaviLatLng latLng, NaviLatLng other) {
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(), latLng.getLongitude()), new LatLng(latLng.getLatitude(), other.getLongitude()));
    }

    FindForWardPoi.OnFindRightPoiInPath poiInPath0 = new FindForWardPoi.OnFindRightPoiInPath() {
        @Override
        public void OntheRightPoi(int index, NaviLatLng myLatLng, NaviLatLng reLatLng, String infoText) {
            LogUtils.e(TAG, "\npoi0:" + myLatLng + "\npoi1:" + reLatLng + "\ninfo:" + infoText);
//            double disX = Math.abs(myLatLng.getLatitude()-reLatLng.getLatitude());
//            double disY = Math.abs(myLatLng.getLongitude()  - reLatLng.getLongitude());

            float disX = disMwithLat(myLatLng, reLatLng);
            float disY = disMwithLon(myLatLng, reLatLng);
            if (disX > disY) {
                double dis = myLatLng.getLongitude() - reLatLng.getLongitude();
                if (dis > 0) {
                    style0 = LINE_TYPE_TOP;
                } else {
                    style0 = LINE_TYPE_BOTTOM;
                }
            } else {
                double dis = myLatLng.getLatitude() - reLatLng.getLatitude();
                if (dis > 0) {
                    style0 = LINE_TYPE_RIGHT;
                } else {
                    style0 = LINE_TYPE_LEFT;
                }
            }
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.nothing_poi));

            if (mLineMarker0 != null) {
                mLineMarker0.remove();
                mLineMarker0 = null;
            }

            mLineMarker0 = mAmap.addMarker(options);
            mLineMarker0.setAnchor(0.5f, 1f);
            mLineMarker0.setClickable(false);
            mLineMarker0.setPosition(new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude()));
            mLineMarker0.setRotateAngle(ANGLE_STYLE[style0]);

            if (mMarker0 != null) {
                mMarker0.remove();
                mMarker0 = null;
            }
            mMarker0 = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.nothing_poi)));
            mMarker0.setAnchor(0.5f, 0.5f);
            mMarker0.setPosition(new LatLng(reLatLng.getLatitude(), reLatLng.getLongitude()));
            if (mMapFloatView != null) {
                mMapFloatView.setFirstString(infoText);
            }
            updateLineMarkerPosition();
        }
    };
    FindForWardPoi.OnFindRightPoiInPath poiInPath1 = new FindForWardPoi.OnFindRightPoiInPath() {
        @Override
        public void OntheRightPoi(int index, NaviLatLng myLatLng, NaviLatLng reLatLng, String infoText) {
//            double disX = Math.abs(myLatLng.getLatitude()-reLatLng.getLatitude());
//            double disY = Math.abs(myLatLng.getLongitude()  - reLatLng.getLongitude());

            float disX = disMwithLat(myLatLng, reLatLng);
            float disY = disMwithLon(myLatLng, reLatLng);

            if (disX > disY) {
                double dis = myLatLng.getLongitude() - reLatLng.getLongitude();
                if (dis > 0) {
                    style1 = LINE_TYPE_TOP;
                } else {
                    style1 = LINE_TYPE_BOTTOM;
                }
            } else {
                double dis = myLatLng.getLatitude() - reLatLng.getLatitude();
                if (dis > 0) {
                    style1 = LINE_TYPE_RIGHT;
                } else {
                    style1 = LINE_TYPE_LEFT;
                }
            }
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.nothing_poi));

            if (mLineMarker1 != null) {
                mLineMarker1.remove();
                mLineMarker1 = null;
            }

            mLineMarker1 = mAmap.addMarker(options);
            mLineMarker1.setAnchor(0.5f, 1f);
            mLineMarker1.setPosition(new LatLng(myLatLng.getLatitude(), myLatLng.getLongitude()));
            mLineMarker1.setRotateAngle(ANGLE_STYLE[style1]);
            mLineMarker1.setClickable(false);

            if (mLineMarker0 != null) {
                mMapFloatView.setPoint(mLineMarker0.getPosition(), style0, mLineMarker1.getPosition(), style1);
            }

            if (mMarker1 != null) {
                mMarker1.remove();
                mMarker1 = null;
            }
            mMarker1 = mAmap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.nothing_poi)));
            mMarker1.setAnchor(0.5f, 0.5f);
            mMarker1.setPosition(new LatLng(reLatLng.getLatitude(), reLatLng.getLongitude()));
            if (mMapFloatView != null) {
                mMapFloatView.setSecondString(infoText);
            }
            updateLineMarkerPosition();
        }
    };

    private void watchAll() {
        new WatchSee().start();
    }


    class WatchSee extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                LogUtils.d(TAG, "ready to changeRoute \nfrom:" + fromPoint + "\n toPoint：" + toPoint);
                HashMap<Integer, AMapNaviPath> pathHashMap = mLocationPro.getNaviPaths();
                int[] ints = mLocationPro.getPathsInts();

                LatLngBounds.Builder builder = LatLngBounds.builder();
                NaviLatLng startPoi = pathHashMap.get(ints[0]).getStartPoint();
                NaviLatLng endPoi = pathHashMap.get(ints[0]).getEndPoint();
                float disF = disMwithLat(startPoi, endPoi);
                float disK = disMwithLon(startPoi, endPoi);
                double alllenght = Math.sqrt(disF * disF + disK * disK);
                double num = disF / alllenght;
                double scall = Math.toDegrees(Math.atan(disF / disK));
                LogUtils.d(TAG, "scall:" + scall);
                for (int i = 0; i < ints.length; i++) {
                    AMapNaviPath path = pathHashMap.get(ints[i]);
                    for (NaviLatLng latLng : path.getCoordList()) {
                        builder.include(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
                    }
                }
//                    CameraUpdate update1 = CameraUpdateFactory.changeBearing((float) scall);
//                    mAMap.animateCamera(update1,0,null);
                LatLngBounds latLngBounds = builder.build();
                CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (30 + num * 160));

                Message message = handlTheWatch.obtainMessage();
                message.what = 0;
                message.obj = update;
                handlTheWatch.sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Handler handlTheWatch = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            CameraUpdate update = (CameraUpdate) msg.obj;
            mAmap.animateCamera(update);
        }
    };

    AMap.OnPolylineClickListener polylineClickListener = new AMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            LogUtils.d(TAG, "\n touch one:" + polyline + "\n first:" + mPoline0 + "\n first1:" + mPoline1 + "\n first2:" + mPoline2);
            if (polyline.equals(mPoline0)) {
                if (mLocationPro.getPathsInts()[0] != routeID) {
                    routeID = mLocationPro.getPathsInts()[0];
                    changeRoute();
                }
            } else if (polyline.equals(mPoline1)) {
                if (mLocationPro.getPathsInts()[1] != routeID) {
                    routeID = mLocationPro.getPathsInts()[1];
                    changeRoute();
                }
            } else if (polyline.equals(mPoline2)) {
                if (mLocationPro.getPathsInts()[2] != routeID) {
                    routeID = mLocationPro.getPathsInts()[2];
                    changeRoute();
                }
            }
        }
    };


    private MapFloatView.XpFloatViewTouchListener xpFloatViewTouchListener = new MapFloatView.XpFloatViewTouchListener() {

        @Override
        public void touchOne(int clickOne) {
            int[] intses = mLocationPro.getPathsInts();
            if (routeID == intses[0]) {
                if (clickOne == 0) {
                    if (intses.length > 1) {
                        routeID = intses[1];
                        changeRoute();
                    }
                    return;
                } else {
                    if (intses.length > 2) {
                        routeID = intses[2];
                        changeRoute();
                    }
                    return;
                }
            }

            if (intses.length > 1 && routeID == intses[1]) {
                if (clickOne == 0) {
                    routeID = intses[0];
                    changeRoute();
                    return;
                } else {
                    if (intses.length > 2) {
                        routeID = intses[2];
                        changeRoute();
                    }
                    return;
                }
            }

            if (intses.length > 2 && routeID == intses[2]) {
                if (clickOne == 0) {
                    routeID = intses[0];
                    changeRoute();
                    return;
                } else {
                    routeID = intses[1];
                    changeRoute();
                    return;
                }
            }
        }
    };
}