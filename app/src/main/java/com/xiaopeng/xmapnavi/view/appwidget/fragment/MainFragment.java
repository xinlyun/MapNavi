package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.AoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.wangjie.shadowviewhelper.ShadowProperty;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.lib.utils.utils.UIUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.WherePoi;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.LineShowView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.ShowCollectDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by linzx on 2016/12/15.
 */

public class MainFragment extends Fragment implements AMap.InfoWindowAdapter
        ,View.OnClickListener
        ,XpSearchListner,AMap.OnMarkerDragListener
        ,AMap.OnCameraChangeListener, AMap.OnMarkerClickListener
        ,GeocodeSearch.OnGeocodeSearchListener,LocationSource,XpLocationListener {
    private static final String TAG = "MainFragment";
    private MapView mapView;
    private AMap mAmap;
    private BaseFuncActivityInteface mActivity;
    private View rootView;

    //--activity--//
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final int REQ_HAVE_RESULT = 1;
    private static final String ACTION_MSG = "ACTION_MSG";
    private static final String ACTION_NAVI_COMP = "ACTION_COMP";
    private static final String NAVI_MSG ="ints";
    private static final String NAVI_POI ="NAVI_POI";
    private static final int REQUEST_FIND_HOME = 1;
    private static final int REQUEST_FIND_COMPLETE = 0;
    private static final int WATCH_NORTH = 0;
    private static final int WATCH_2D = 1;
    private static final int WATCH_3D = 2;
    private int[] IMG_WEK = {R.drawable.icon_seewatch_2,R.drawable.icon_seewatch_1,R.drawable.icon_seewatch_0};
    private String[] SEEWATCH_TEXT ;
    private int mWatchStyle = WATCH_NORTH;
    private ILocationProvider mLocationProvider;

    private LocationSource.OnLocationChangedListener mListener;
    // 是否需要跟随定位
    private boolean isNeedFollow = true;
    private String mCity ;
    // 处理静止后跟随的timer
    private Timer needFollowTimer;

    // 屏幕静止DELAY_TIME之后，再次跟随
    private long DELAY_TIME = 5000;

    private Marker marker,mMarkerPoi;
    private PolylineOptions polylineOptions = new PolylineOptions();

    private Polyline mPolyline;
    private ScaleAnimation scaleAnimation = new ScaleAnimation (0,1,0,1);
    private GeocodeSearch geocodeSearch;
    private FrameLayout mDownLayout1;

    private TextView mTvPoiName,mTvPoiStr,mTvPoiDis;
//    private ImageView mImgBtnSeeWay;

    private LatLng mLatLng;
    private boolean isTraff = true;
    private boolean isCanShow = false;//make the mTxShowPoiName can be show
    private LineShowView mLsv;
    private View mMarkInfoView;
    private TextView mTxMarkTitle;
    //    private CircleImageView mCirIV;
    private ShadowProperty mShadowPro ;

    private DateHelper mCollectDateHelper;
    private String poiName,poiDesc;



    private WherePoi mHome,mComplete;
    private int height = 0 ;
    private RelativeLayout mReleavieView;
    private ImageView mIvShowTraffic;
    private ImageView mIvSeeWatch;
    private TextView mTxSeeWatch;
    private Marker mLocationMarker;
    //            ,mLocationMarker1;
    private Button mLoveBtn;
    private CollectItem mCollectNow;
    private LatLonPoint fromPoint,toPoint;
    private TextView mTxBilici;

    private SensorManager mSensorManager;
    private Sensor mOrientation;
    private MySensorEventListener mySensorEventListener;
    private float mSeeFloat = 0;
    private long saveTouchTime = 0;
    private long sensorChangeTime = 0;
    private boolean isFirst = true;
    private int width2 = 540;
    private int height2 = 720;
    float saveA = 0;

    private MarkerOptions mMarkerOptions;
    private LinearLayout mInfoLayout;
    private Handler findMyPoiDeley;
    private boolean isInFace  = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseFuncActivityInteface) getActivity();

        mCollectDateHelper = new DateHelper();
        mCollectDateHelper.setOnWhereListener(mWhereListener);
        mShadowPro = new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDy(0)
                .setShadowRadius(UIUtils.dip2px(getActivity(),5));
        mLocationProvider    = LocationProvider.getInstence(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main,container,false);
        findMyPoiDeley = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    if (isInFace) {
                        if ((System.currentTimeMillis() - saveTouchTime) >= 30 * 1000) {
                            findMyPosi();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        SEEWATCH_TEXT = new String[]{
                getResources().getString(R.string.watch_north),
                getResources().getString(R.string.watch_follow),
                getResources().getString(R.string.watch_3d)
        };
        initView();
        mLsv.setVisibility(View.GONE);






        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mySensorEventListener = new MySensorEventListener();
        mLocationProvider.stopNavi();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }
    private void initView(){
        mReleavieView       = (RelativeLayout) findViewById(R.id.all_view_relati_view);
        mLsv                = (LineShowView) findViewById(R.id.lsv_line);
        mIvShowTraffic      = (ImageView) findViewById(R.id.iv_show_traffic);
        mIvSeeWatch         = (ImageView) findViewById(R.id.iv_seewatch);
        mTxSeeWatch         = (TextView) findViewById(R.id.tx_seewatch);
        mDownLayout1        = (FrameLayout) findViewById(R.id.layout_down);
        mTvPoiName          = (TextView) findViewById(R.id.tv_poi_name);
        mTvPoiStr           = (TextView) findViewById(R.id.tv_poi_str);
        mTvPoiDis           = (TextView) findViewById(R.id.tv_poi_dis);
        mTxBilici           = (TextView) findViewById(R.id.tx_bilici);
        mInfoLayout         = (LinearLayout) findViewById(R.id.layout_info);
        findViewById(R.id.d3_setting).setOnClickListener(this);
        findViewById(R.id.d3_lukuang).setOnClickListener(this);
        findViewById(R.id.btn_begin_navi).setOnClickListener(this);
        findViewById(R.id.civ_all_big).setOnClickListener(this);
        findViewById(R.id.d3_dinwei).setOnClickListener(this);
        findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
        findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
        findViewById(R.id.btn_exit_show).setOnClickListener(this);
        findViewById(R.id.icon_open_collect).setOnClickListener(this);
        findViewById(R.id.tx_goto_complete).setOnClickListener(this);
        findViewById(R.id.tx_goto_home).setOnClickListener(this);
        findViewById(R.id.btn_collect).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        mLoveBtn        = (Button) findViewById(R.id.btn_collect);

        initMarkInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocationProvider   .addLocationListener(this);
        mLocationProvider.addSearchListner(this);
        isInFace = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isInFace = false;
        mLocationProvider.removeSearchListner(this);
        mLocationProvider   .removeLocationListener(this);
    }

    private void initMarkInfo(){
//        mMarkInfoView       = getActivity().getLayoutInflater().inflate(R.layout.layout_tip_show,null);
        mTxMarkTitle        = (TextView) findViewById(R.id.tx_tip_show);
        mTxMarkTitle        .setOnClickListener(this);
        findViewById(R.id.btn_little_begin_navi).setOnClickListener(this);


    }

    @Override
    public View getInfoWindow(Marker marker) {
        LogUtils.d(TAG,"getInfoWindow");
        return mMarkInfoView;
    }

    @Override
    public View getInfoContents(Marker marker)
    {
        LogUtils.d(TAG,"getInfoContents");
        return mMarkInfoView;
    }





    @Override
    public void onClick(View view) {
        if (mDownLayout1.getVisibility()==View.VISIBLE && view.getId()!=R.id.btn_collect) {
            mLoveBtn.setBackgroundResource(R.drawable.icon_collect_2);
            mDownLayout1.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mReleavieView.getLayoutParams();
            layoutParams.height = height;
            mReleavieView.setLayoutParams(layoutParams);
        }

        switch (view.getId()){

            case R.id.btn_begin_navi:
                mActivity.showDialogwithOther();
                startCalueNavi();
                break;

            case R.id.d3_dinwei:
                findMyPosi();
                break;

            case R.id.d3_lukuang:
                changeTriffical();
                break;

            case R.id.d3_setting:
                changeWatchWay();
                break;

            case R.id.btn_zoom_plus:
                saveTouchTime = System.currentTimeMillis();
                startTimerSomeTimeLater();
                findMyPoiDeley.sendEmptyMessageDelayed(0,30 * 1001);
                if (mAmap!=null) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                updateScale();
                break;

            case R.id.btn_zoom_jian:
                saveTouchTime = System.currentTimeMillis();
                startTimerSomeTimeLater();
                findMyPoiDeley.sendEmptyMessageDelayed(0,30 * 1001);
                if (mAmap!=null) {
                    mAmap.animateCamera(CameraUpdateFactory.zoomOut());
                }
                updateScale();
                break;

            case R.id.btn_exit_show:
                mDownLayout1.setVisibility(View.GONE);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mReleavieView.getLayoutParams();
                layoutParams.height = height;
                mReleavieView.setLayoutParams(layoutParams);
                break;

            case R.id.icon_open_collect:

                openCollect();
                break;


            case R.id.civ_all_big:
                startSearch();
                break;

            case R.id.tx_goto_complete:
                clickComplete();
                break;

            case R.id.tx_goto_home:
                clickHome();
                break;


            case R.id.btn_little_begin_navi:
                //fill down
            case R.id.tx_tip_show:
                if (mDownLayout1.getVisibility()== View.GONE) {
                    mDownLayout1.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) mReleavieView.getLayoutParams();
                    height = layoutParams2.height;
                    layoutParams2.height = 1100;
                    mReleavieView.setLayoutParams(layoutParams2);
                }
                break;

            case R.id.btn_collect:
                collectThePoi();
                break;

            case R.id.btn_setting:
                mActivity.startFragment(new SettingFragment());
                break;


            default:
                break;
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mDownLayout1.getVisibility()==View.VISIBLE) {
            mLoveBtn.setBackgroundResource(R.drawable.icon_collect_2);
            mDownLayout1.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mReleavieView.getLayoutParams();
            layoutParams.height = height;
            mReleavieView.setLayoutParams(layoutParams);
        }

        upDateLineTo(cameraPosition);

        if (marker!=null) {
            marker.setVisible(true);
        }
        if (mMarkerPoi!=null ) {
            if (mMarkerPoi.isInfoWindowShown()) {
                mMarkerPoi.hideInfoWindow();

            }
            mMarkerPoi.setVisible(false);

        }
        if (mInfoLayout!=null && mInfoLayout.getVisibility() == View.VISIBLE){
            mInfoLayout.setVisibility(View.GONE);
        }

//
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        upDateLineTo(cameraPosition);
        mLatLng = cameraPosition.target;
        marker.setVisible(false);

        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(cameraPosition.target.latitude,cameraPosition.target.longitude), 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);

        if (!equalNavLat(cameraPosition.target,new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude())) ){

            mMarkerPoi.setAnimation(scaleAnimation);
            mMarkerPoi.setVisible(true);
            mMarkerPoi.startAnimation();
            isCanShow = true;
            mTvPoiName.setText(getString(R.string.load_get_msg));
        }else {
            isCanShow = false;

        }
        updateScale();


        LogUtils.d(TAG,"MarkerPoi is Show?:"+mMarkerPoi.isInfoWindowShown());
//        mMarkerPoi.showInfoWindow();


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getId() == mMarkerPoi.getId()) {
            isCanShow = false;
            LatLng latLng = mMarkerPoi.getPosition();


            int dis = (int) AMapUtils.calculateLineDistance(latLng,new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
            if (dis > 1000){
                mTvPoiDis.setText(""+(dis/1000)+getString(R.string.kmile));
            }else {
                mTvPoiDis.setText(""+dis+getString(R.string.mile));
            }
            mTvPoiStr.setText("");
            if (mDownLayout1.getVisibility()==View.GONE) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mReleavieView.getLayoutParams();
                height = layoutParams.height;
                mDownLayout1.setVisibility(View.VISIBLE);
                layoutParams.height = 1100;
                mReleavieView.setLayoutParams(layoutParams);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();

        if (address.getAois()!=null && address.getAois().size()>0) {
            AoiItem aoiItem = address.getAois().get(0);

            poiName = aoiItem.getAoiName();
            poiDesc = getUsefulInfo(address);
            mTvPoiName.setText(poiName);
            mTvPoiStr.setText(poiDesc);
            mTxMarkTitle.setText(poiName);

        }else {

            poiName = getUsefulInfo(address);
            poiDesc = "";
            if (poiName.length()<3){
                poiName = getString(R.string.unknow_road);
            }
            mTvPoiName.setText(poiName);
            mTxMarkTitle.setText(poiName);
        }

        mCollectNow = mCollectDateHelper.getCollectByName(poiName);
        if (mCollectNow!=null){
            mLoveBtn.setBackgroundResource(R.drawable.icon_collect_1);
        }

        LatLonPoint latLonPoint = address.getPois().get(0).getLatLonPoint();
        LatLng latLng = new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());

        float dis = (int) AMapUtils.calculateLineDistance(latLng,new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
        if (dis > 1000){
            dis = dis/1000;
            DecimalFormat df = new DecimalFormat("0.0");
            String result = df.format(dis);
            mTvPoiDis.setText(result+getString(R.string.kmile));
        }else {
            mTvPoiDis.setText(""+dis+getString(R.string.mile));
        }

        if (mMarkerPoi!=null && mMarkerPoi.isVisible()) {
            if (mInfoLayout != null && mInfoLayout.getVisibility() == View.GONE) {
                mInfoLayout.setVisibility(View.VISIBLE);
            }
        }




    }



    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }



    @Override
    public void searchSucceful() {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;

    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            mCity = aMapLocation.getCity();
            if (mAmap !=null && mWatchStyle!= WATCH_NORTH){
            }
            if (mAmap!=null && isFirst){
                mapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude())
//                    新的中心点坐标
                                ,17, //新的缩放级别
                                0, //俯仰角0°~45°（垂直与地图时为0）
                                0  ////偏航角 0~360° (正北方为0)
                        ));
                        mAmap.animateCamera(update);

                    }
                },700);
                isFirst = false;
            }

        }
        if (mListener != null){
//            mListener.onLocationChanged(aMapLocation);
        }

        if (mLocationMarker!=null){
            mLocationMarker.setPosition(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
            if (mWatchStyle == WATCH_NORTH){
                mLocationMarker.setVisible(true);
            }
        }

    }


    private void removeLocationMarker(){
        try{
            if (mLocationMarker!=null){
                mLocationMarker.remove();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mLocationMarker = null;
        }
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
//            float a = event.values[0];
//            if (mLocationMarker!=null){
//                mLocationMarker.setRotateAngle(360 - a);
//            }
            saveA = event.values[0];
            if (mLocationMarker != null) {
                mLocationMarker.setRotateAngle(360 - saveA);
            }

            if (mWatchStyle != WATCH_NORTH) {
                if ((System.currentTimeMillis() - sensorChangeTime > 300)) {
                    if (Math.abs(event.values[0] - saveA) > 5) {
                        saveA = event.values[0];

                        mSeeFloat = saveA;
                        mAmap.animateCamera(CameraUpdateFactory.changeBearing(saveA));
                        sensorChangeTime = System.currentTimeMillis();
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }


    private void updateScale(){
        float pixel = mAmap.getScalePerPixel();
        int mi = (int) (pixel*94);
        if (mi<1000) {
            if (mi>100){
                mi = mi/100 *100;
            }else {
                if (mi > 50){
                    mi = 50;
                }else {
                    mi = 25;
                }
            }
            String str = String.format(getString(R.string.num_mile),""+mi);
            mTxBilici.setText(str);
        }else if (mi > 1000){
            mi = mi/1000;
            String str = String.format(getString(R.string.num_kilemile),""+mi);
            mTxBilici.setText(str);
        }
    }


    private void initWidth(){
        WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        width2 = windowManager.getDefaultDisplay().getWidth()/2;
        height2 = windowManager.getDefaultDisplay().getHeight()/2;
        mMarkerPoi.setPositionByPixels(width2,height2);
        marker.setPositionByPixels(width2,height2);

    }

    private void initMarker(){
        marker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_drag_point))
                .draggable(true));
        marker.setPositionByPixels(width2,height2);
        marker.setAnchor(0.5f,0.5f);

        mMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pre_list_img2))
                .title("标题")
                .snippet("详细信息")
                .draggable(true);

        mMarkerPoi = mAmap.addMarker(mMarkerOptions);
        mMarkerPoi.setAutoOverturnInfoWindow(true);
        mMarkerPoi.setPositionByPixels(width2,height2);
        mMarkerPoi.setAnchor(0.5f,1f);
        mMarkerPoi.setInfoWindowEnable(true);
        mMarkerPoi.setVisible(false);

        scaleAnimation.setDuration(540);
        mapView.getMap().setOnMarkerClickListener(this);
        geocodeSearch = new GeocodeSearch(getActivity());
        geocodeSearch.setOnGeocodeSearchListener(this);

        removeLocationMarker();
        mLocationMarker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_seewatch_1))
                .draggable(false)
        );
        mLocationMarker.setAnchor(0.5f,0.5f);
        mLocationMarker.setVisible(false);
        mLocationMarker.setFlat(true);


        mLocationProvider.reCallLocation();
        try{
            findMyPosi();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void findMyPosi(){
        LogUtils.d(TAG,"findMyPosi");
        try {

            marker.setVisible(false);
            if (mLocationProvider != null && mLocationProvider.getAmapLocation() != null && mAmap != null) {
                int bear = 0;
                if (mWatchStyle == WATCH_3D) bear = 35;
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude())
//                    新的中心点坐标
                        , 17, //新的缩放级别
                        bear, //俯仰角0°~45°（垂直与地图时为0）
                        mSeeFloat  ////偏航角 0~360° (正北方为0)
                ));

                mAmap.moveCamera(update);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        if (mapView==null){
            mapView = mActivity.getMapView();
            init();
        }
        mAmap.setCustomMapStylePath("/sdcard/main_style_true.json");
        mAmap.setMapCustomEnable(true);
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLsv.setVisibility(View.VISIBLE);
            }
        },2000);

        mSensorManager.registerListener(mySensorEventListener,
                mOrientation, SensorManager.SENSOR_DELAY_NORMAL);


        mLoveBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    reInit();
                    initMarker();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },500);
        mCollectDateHelper.getWhereItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mySensorEventListener);
    }

    private void reInit(){
        mAmap.clear();
        mAmap.setTrafficEnabled(isTraff);
        mAmap.setInfoWindowAdapter(this);
        mAmap.setLocationSource(this);// 设置定位监听
        mAmap.setOnPOIClickListener(mPoiClickListener);
        UiSettings settings = mAmap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setTiltGesturesEnabled(false);
        settings.setRotateGesturesEnabled(false);
        mAmap.setOnMarkerDragListener(this);
        mAmap.setOnCameraChangeListener(this);
        polylineOptions = new PolylineOptions();
        polylineOptions.width(2);
        polylineOptions.color(Color.argb(255, 1, 1, 1));
        polylineOptions.setDottedLine(true);
        polylineOptions.aboveMaskLayer(true);
        mPolyline = mAmap.addPolyline(polylineOptions);

        removeLocationMarker();
        mLocationMarker = mAmap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_seewatch_1))
                .draggable(false)
        );
        if (mWatchStyle!=WATCH_NORTH){
            mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_0));
        }
        mLocationMarker.setAnchor(0.5f,0.5f);
        mLocationMarker.setVisible(false);
        mLocationMarker.setFlat(true);
        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));


        mLocationProvider.reCallLocation();
    }

    private AMap.OnPOIClickListener mPoiClickListener = new AMap.OnPOIClickListener() {
        @Override
        public void onPOIClick(Poi poi) {
            try {
                mAmap.moveCamera(CameraUpdateFactory.changeLatLng(poi.getCoordinate()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private XpWhereListener mWhereListener = new XpWhereListener() {
        @Override
        public void onWhereCallBack(List<WherePoi> wherePois) {
            for (WherePoi wherePoi:wherePois){
                if (wherePoi.type == REQUEST_FIND_COMPLETE){
                    mComplete = wherePoi;
                }else {
                    mHome = wherePoi;
                }
            }

        }
    };

    private void whenNorthChange(){

        mAmap.moveCamera(CameraUpdateFactory.changeTilt(0));
        mAmap.moveCamera(CameraUpdateFactory.changeBearing(0));
    }

    private void showWatchWay(){
        if (mAmap!=null){
            switch (mWatchStyle){
                case WATCH_NORTH:
                    mSeeFloat = 0f;
                    if ( mLocationProvider!=null && mLocationProvider.getAmapLocation()!=null) {
//
//                        findMyPosi();
                        whenNorthChange();
//
                        if (mLocationMarker!=null){
                            mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_1));
                            mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                        }else{
                            removeLocationMarker();
                            mLocationMarker  = mAmap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory
                                            .fromResource(R.drawable.icon_seewatch_1))
                                    .draggable(false));
                            mLocationMarker.setAnchor(0.5f,0.5f);
                            mLocationMarker.setVisible(false);
                            mLocationMarker.setFlat(true);
                            mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                        }

//
                    }

                    break;

                case WATCH_2D:
                    if ( mLocationProvider!=null && mLocationProvider.getAmapLocation()!=null) {
                        CameraUpdate update1 = CameraUpdateFactory.changeTilt(0);
                        mAmap.animateCamera(update1);
                    }
                    if (mLocationMarker!=null){
                        mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_0));
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }else{
                        removeLocationMarker();
                        mLocationMarker  = mAmap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.icon_seewatch_0))
                                .draggable(false));
                        mLocationMarker.setAnchor(0.5f,0.5f);
                        mLocationMarker.setVisible(false);
                        mLocationMarker.setFlat(true);
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }
                    break;

                case WATCH_3D:
                    CameraUpdate update1 = CameraUpdateFactory.changeTilt(30);
                    mAmap.animateCamera(update1);
                    if (mLocationMarker!=null){
                        mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_0));
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }else{
                        removeLocationMarker();
                        mLocationMarker  = mAmap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.icon_seewatch_0))
                                .draggable(false));
                        mLocationMarker.setAnchor(0.5f,0.5f);
                        mLocationMarker.setVisible(false);
                        mLocationMarker.setFlat(true);
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }
                    break;
            }}
    }

    private void changeWatchWay(){
        mWatchStyle = (mWatchStyle+1)%3;
        mIvSeeWatch.setImageResource(IMG_WEK[mWatchStyle]);
        mTxSeeWatch.setText(SEEWATCH_TEXT[mWatchStyle]);
        showWatchWay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        findMyPoiDeley.removeCallbacksAndMessages(null);
    }

    /**
     * 取消timer任务
     */
    private void clearTimer() {
        if (needFollowTimer != null) {
            needFollowTimer.cancel();
            needFollowTimer = null;
        }
    }



    /**
     * 如果地图在静止的情况下
     */
    private void startTimerSomeTimeLater() {
        // 首先关闭上一个timer
        clearTimer();
        needFollowTimer = new Timer();
        // 开启一个延时任务，改变跟随状态
        needFollowTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isNeedFollow = true;
            }
        }, DELAY_TIME);
    }





    /**
     * 初始化各种对象
     */
    private void init() {
        if (mAmap == null) {
            mAmap = mapView.getMap();

            mAmap.setMapTextZIndex(0);

            mAmap.setMapCustomEnable(false);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                    new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude())
//                    新的中心点坐标
                    , 17, //新的缩放级别
                    0, //俯仰角0°~45°（垂直与地图时为0）
                    0  ////偏航角 0~360° (正北方为0)
            ));
            mAmap.moveCamera(update);
//            mAmap.setMapType(AMap.MAP_TYPE_NAVI);
            mAmap.setTrafficEnabled(isTraff);
            mAmap.setInfoWindowAdapter(this);
            mAmap.setOnPOIClickListener(mPoiClickListener);
            UiSettings settings = mAmap.getUiSettings();
            settings.setTiltGesturesEnabled(false);
            settings.setRotateGesturesEnabled(false);
            mAmap.setLocationSource(this);// 设置定位监听
            mAmap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
            mAmap.getUiSettings().setZoomControlsEnabled(false);

            mAmap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            mAmap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate update = CameraUpdateFactory.zoomTo(17);
                    mAmap.animateCamera(update);
                }
            },800);
            mAmap.setOnMarkerDragListener(this);
            mAmap.setOnCameraChangeListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(2);
            polylineOptions.color(Color.argb(255, 1, 1, 1));
            polylineOptions.setDottedLine(true);
            polylineOptions.aboveMaskLayer(true);
            mPolyline = mAmap.addPolyline(polylineOptions);
            setMapInteractiveListener();


            initMarker();
        }

    }

    /**
     * 设置导航监听
     */
    private void setMapInteractiveListener() {

        mAmap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        hideKeyboard(mEtvSearch);
                        // 按下屏幕
                        // 如果timer在执行，关掉它
                        saveTouchTime = System.currentTimeMillis();
                        clearTimer();
                        // 改变跟随状态
                        isNeedFollow = false;
                        try {
                            findMyPoiDeley.removeMessages(0);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        // 离开屏幕
                        startTimerSomeTimeLater();
                        findMyPoiDeley.sendEmptyMessageDelayed(0,30 * 1001);
                        updateScale();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;

                    default:
                        break;
                }
            }
        });

    }

    private void clickComplete(){
        if (mComplete==null) {
//            startActivityForResult(new Intent(this, SearchCollectActivity.class), REQUEST_FIND_COMPLETE);
            SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
            searchCollectFragment.setRequestCode(REQUEST_FIND_COMPLETE);
            searchCollectFragment.setMapView(mapView);
            mActivity.startFragment(searchCollectFragment);
        }else {
            mLatLng = new LatLng(mComplete.posLat,mComplete.posLon);
            mActivity.showDialogwithOther();
//            mProgDialog.show();
            startCalueNavi();
        }
    }

    private void clickHome(){
        if (mHome==null) {
//            startActivityForResult(new Intent(this, SearchCollectActivity.class), REQUEST_FIND_HOME);
            SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
            searchCollectFragment.setRequestCode(REQUEST_FIND_HOME);
            searchCollectFragment.setMapView(mapView);
            mActivity.startFragment(searchCollectFragment);
        }else {
            mLatLng = new LatLng(mHome.posLat,mHome.posLon);
            mActivity.showDialogwithOther();
//            mProgDialog.show();
            startCalueNavi();
        }
    }

    private void collectThePoi(){
        if (mCollectNow==null) {
            mLoveBtn.setBackgroundResource(R.drawable.icon_collect_1);
            mCollectDateHelper.saveCollect(poiName, poiDesc, mLatLng.latitude, mLatLng.longitude);
            mCollectNow = mCollectDateHelper.getCollectByName(poiName);
        }else {
            mLoveBtn.setBackgroundResource(R.drawable.icon_collect_2);
            mCollectNow.delete();
            mCollectNow = null;
        }
    }


    private void openCollect(){
        mActivity.showCollectDialog();
    }


    private void startCalueNavi(){
        LogUtils.d(TAG,"startCalueNavi");
        mActivity.showDialogwithOther();
        List<NaviLatLng> startPoi = new ArrayList<>();
        startPoi.add(new NaviLatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
        List<NaviLatLng> wayPoi = new ArrayList<>();
        List<NaviLatLng> endPoi = new ArrayList<>();
        if (mLatLng == null)return;
        endPoi.add(new NaviLatLng(mLatLng.latitude,mLatLng.longitude));
        mLocationProvider.calueRunWay(startPoi,wayPoi,endPoi);
    }

    private void startSearch(){
        SearchPosiFragment mSearchFragment = new SearchPosiFragment();
        mActivity.startFragment(mSearchFragment);
    }


    //    private void upDateLineTo(CameraPosition cameraPosition){
    private void upDateLineTo(CameraPosition position){
        try {
            LatLng mLatlon = new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude());
            Projection projection = mAmap.getProjection();
            Point pM = projection.toScreenLocation(mLatlon);
            mLsv.setPoint(pM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean equalNavLat(LatLng latLon1,LatLng latLng2){
        if ((Math.abs(latLng2.latitude - latLon1.latitude) < 0.0012f) && (Math.abs(latLng2.longitude - latLon1.longitude) < 0.0012f)){
            return true;
        }else {
            return false;
        }
    }

    private void updateLine(CameraPosition cameraPosition){
        if (marker != null){
            List<LatLng> latLngs = new ArrayList<LatLng>();
            latLngs.add(cameraPosition.target);
            latLngs.add(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
            mPolyline.setPoints(latLngs);
        }
    }

    private String getUsefulInfo(RegeocodeAddress address){
        String msg ;
        if(address.getBuilding().length()>0){
            msg = address.getBuilding()+getString(R.string.negiht);
        }else if(address.getFormatAddress().length()>0) {
            msg = Utils.getAddressSimple(address.getFormatAddress());
        }
        else if(address.getNeighborhood().length()>0) {
            msg = Utils.getAddressSimple(address.getNeighborhood());
        }else if(address.getRoads().size()>0){
            msg = Utils.getAddressSimple(address.getRoads().get(0).getName());

        }else {
            msg = getString(R.string.unknow_road);
        }
        return msg;
    }

    private void changeTriffical(){
        if (mAmap!=null) {
            isTraff = !isTraff;
            mAmap.setTrafficEnabled(isTraff);

            if (isTraff){
//                mAmap.setMapType(AMap.MAP_TYPE_NAVI);
                mAmap.setCustomMapStylePath("/sdcard/main_style_true.json");
                mAmap.setMapCustomEnable(true);
                mapView.getMap().setMaskLayerParams(500,500,500,500,500,500);
                mIvShowTraffic.setImageResource(R.drawable.icon_lukuang_01);
                mAmap.showIndoorMap(false);
                mAmap.showBuildings(false);
            }else {
                mAmap.setCustomMapStylePath("/sdcard/main_style_false.json");
                mAmap.setMapCustomEnable(true);

                mapView.getMap().setMaskLayerParams(500,500,500,500,500,500);
                mIvShowTraffic.setImageResource(R.drawable.icon_lukuang_02);
            }

            mAmap.setMapTextZIndex(0);
        }
    }

    public void setPosi(double lat,double lon){
        mLatLng = new LatLng(lat,lon);
    }





    public void showCollectDialog(){
        mCollectDateHelper.getCollectItems();
    }

    private void beginCalueNavi(){
        if (fromPoint!=null && toPoint!=null){
            List<NaviLatLng> startList = new ArrayList<>();
            List<NaviLatLng> wayList = new ArrayList<>();
            List<NaviLatLng> endList = new ArrayList<>();
            startList.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
            endList.clear();
            endList.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
//            mProgDialog.show();
            mActivity.showDialogwithOther();
            mLocationProvider.calueRunWay(startList,wayList,endList);
        }
    }

}
