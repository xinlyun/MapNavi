package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;

import com.aispeech.aios.common.bean.PoiBean;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.Poi;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.wangjie.shadowviewhelper.ShadowProperty;
import com.wangjie.shadowviewhelper.ShadowViewHelper;
import com.xiaopeng.amaplib.util.ToastUtil;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;

import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
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
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xiaopeng.lib.utils.utils.UIUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.LocationSaver;
import com.xiaopeng.xmapnavi.bean.WherePoi;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ICollectDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.IWhereDateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpWhereListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RadarNaviFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RunNaviWayFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SearchCollectFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SearchPosiFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SettingFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowCollectFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowPosiFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.second.ShowSearchPoiFragment;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.CircleImageView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.LineShowView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.ShowCollectDialog;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.StereoView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.TipPopWindow;
import com.xiaopeng.xmapnavi.view.appwidget.services.LocationProService;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import okhttp3.internal.Util;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends Activity implements BaseFuncActivityInteface,LocationSource,XpLocationListener,View.OnClickListener
        ,XpSearchListner ,AMap.OnMarkerDragListener
        ,AMap.OnCameraChangeListener, AMap.OnMarkerClickListener
        ,GeocodeSearch.OnGeocodeSearchListener,XpNaviCalueListener
        ,AMap.InfoWindowAdapter , XpCollectListener
        ,ShowCollectDialog.CollectDialogListener
        ,View.OnTouchListener
{
    public static final String TAG = "MainActivity";
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
    private String mSearchName ;
    private ILocationProvider mLocationProvider;
    private MapView mapView;
    private AMap aMap;
    private Marker myLocationMarker;
    //    private TipPopWindow mTipWindow;
    private OnLocationChangedListener mListener;
    // 是否需要跟随定位
    private boolean isNeedFollow = true;
    private String mCity ;
    // 处理静止后跟随的timer
    private Timer needFollowTimer;

    // 屏幕静止DELAY_TIME之后，再次跟随
    private long DELAY_TIME = 5000;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    private int screenHeight = 0;
    //View//
    //Activity最外层的Layout视图
    private View activityRootView;
    private List<Fragment> mFragments;
    private Marker marker,mMarkerPoi;
    private PolylineOptions polylineOptions = new PolylineOptions();

    private Polyline mPolyline;
    private ScaleAnimation scaleAnimation = new ScaleAnimation (0,1,0,1);
    private GeocodeSearch geocodeSearch;
    private FrameLayout mDownLayout1;

    private TextView mTvPoiName,mTvPoiStr,mTvPoiDis;
//    private ImageView mImgBtnSeeWay;

    private LatLng mLatLng;
    //----//
    private ProgressDialog mProgDialog;
    private boolean isTraff = true;
    private boolean isCanShow = false;//make the mTxShowPoiName can be show
    private LineShowView mLsv;
    private View mMarkInfoView;
    private TextView mTxMarkTitle;
    //    private CircleImageView mCirIV;
    private ShadowProperty mShadowPro ;

    private DateHelper mCollectDateHelper;
    private String poiName,poiDesc;
    private ShowCollectDialog mCollectDialog;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/5
        keyHeight = screenHeight/5;
        mFragments = new ArrayList<>();

        LocationProvider.getInstence(this);

        mCollectDateHelper = new DateHelper();
        mCollectDateHelper.setOnCollectListener(this);
        mCollectDateHelper.setOnWhereListener(mWhereListener);
        mShadowPro = new ShadowProperty()
                .setShadowColor(0x77000000)
                .setShadowDy(0)
                .setShadowRadius(UIUtils.dip2px(this,5));
        mLocationProvider    = LocationProvider.getInstence(this);

        activityRootView = findViewById(R.id.root_layout);
        startService(new Intent(this, LocationProService.class));

        SEEWATCH_TEXT = new String[]{
                getResources().getString(R.string.watch_north),
                getResources().getString(R.string.watch_follow),
                getResources().getString(R.string.watch_3d)
        };
        initView();
        mLsv.setVisibility(View.GONE);



        mReleavieView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCollectDialog = new ShowCollectDialog(MainActivity.this);
                mCollectDialog .setCollectDialogListener(MainActivity.this);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) activityRootView.getLayoutParams();
                LogUtils.d(TAG,"layoutparams width:"+layoutParams.width);

            }
        },3000);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mySensorEventListener = new MySensorEventListener();


    }

    private void initMarker(){
        marker = aMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_drag_point))
                .draggable(true));
        marker.setPositionByPixels(width2,height2);
        marker.setAnchor(0.5f,0.5f);

        mMarkerPoi = aMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pre_list_img2))
                .draggable(true));

        mMarkerPoi.setTitle("title");
        mMarkerPoi.setSnippet("snippet");
        mMarkerPoi.setPositionByPixels(width2,height2);
        mMarkerPoi.setAnchor(0.5f,1f);
        mMarkerPoi.setVisible(false);
        mMarkerPoi.setInfoWindowEnable(true);
        scaleAnimation.setDuration(540);
        mapView.getMap().setOnMarkerClickListener(MainActivity.this);
        geocodeSearch = new GeocodeSearch(MainActivity.this);
        geocodeSearch.setOnGeocodeSearchListener(MainActivity.this);


        mLocationMarker = aMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_seewatch_1))
                .draggable(false)
        );
        mLocationMarker.setAnchor(0.5f,0.5f);
        mLocationMarker.setVisible(false);
        mLocationMarker.setFlat(true);

//        mLocationMarker1 = aMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory
//                        .fromResource(R.drawable.icon_seewatch_0))
//                .draggable(false)
//        );
//        mLocationMarker1.setAnchor(0.5f,0.5f);
//        mLocationMarker1.setVisible(false);
//        mLocationMarker1.setFlat(true);

        mLocationProvider.reCallLocation();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mapView==null) {
            mapView = (MapView) findViewById(R.id.map_main);
            mapView.onCreate(null);// 此方法必须重写
            init();
        }




        mLocationProvider    = LocationProvider.getInstence(this);

//        mLocationProvider   .addSearchListner(this);
        mLocationProvider   .addNaviCalueListner(this);
        mapView.setVisibility(View.VISIBLE);
        mCollectDateHelper.getWhereItems();

        mSensorManager.registerListener(mySensorEventListener,
                mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
//                    initWidth();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },500);



    }

    private void initWidth(){
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        width2 = windowManager.getDefaultDisplay().getWidth()/2;
        height2 = windowManager.getDefaultDisplay().getHeight()/2;
        mMarkerPoi.setPositionByPixels(width2,height2);
        marker.setPositionByPixels(width2,height2);

    }




    @Override
    protected void onStop() {
        super.onStop();
        mLocationProvider   .removeNaviCalueListner(this);
        mapView.setVisibility(View.GONE);
        mSensorManager.unregisterListener(mySensorEventListener);
    }

    /**
     * 初始化各种对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                    new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude())
//                    新的中心点坐标
                    , 17, //新的缩放级别
                    0, //俯仰角0°~45°（垂直与地图时为0）
                    0  ////偏航角 0~360° (正北方为0)
            ));
            aMap.moveCamera(update);
//            aMap.setMapType(AMap.MAP_TYPE_NAVI);
            aMap.setTrafficEnabled(isTraff);
            aMap.setInfoWindowAdapter(this);
            aMap.setOnPOIClickListener(mPoiClickListener);
            myLocationMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.drawable.icon_drag_point))));
            UiSettings settings = aMap.getUiSettings();
            settings.setTiltGesturesEnabled(false);
            settings.setRotateGesturesEnabled(false);
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
            aMap.getUiSettings().setZoomControlsEnabled(false);

            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate update = CameraUpdateFactory.zoomTo(17);
                    aMap.animateCamera(update);
                }
            },800);
            aMap.setOnMarkerDragListener(this);
            aMap.setOnCameraChangeListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(2);
            polylineOptions.color(Color.argb(255, 1, 1, 1));
            polylineOptions.setDottedLine(true);
            polylineOptions.aboveMaskLayer(true);
            mPolyline = aMap.addPolyline(polylineOptions);
            setMapInteractiveListener();

            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLsv.setVisibility(View.VISIBLE);
                }
            },2000);
            initMarker();

        }

    }

    private void reInit(){
        aMap.clear();
        aMap.setTrafficEnabled(isTraff);
        aMap.setInfoWindowAdapter(this);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setOnPOIClickListener(mPoiClickListener);
        UiSettings settings = aMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setTiltGesturesEnabled(false);
        settings.setRotateGesturesEnabled(false);
        aMap.setOnMarkerDragListener(this);
        aMap.setOnCameraChangeListener(this);
        polylineOptions = new PolylineOptions();
        polylineOptions.width(2);
        polylineOptions.color(Color.argb(255, 1, 1, 1));
        polylineOptions.setDottedLine(true);
        polylineOptions.aboveMaskLayer(true);
        mPolyline = aMap.addPolyline(polylineOptions);


        mLocationMarker = aMap.addMarker(new MarkerOptions()
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

    /**
     *  初始化View
     */
    private void initView(){
//        mCirIV              = (CircleImageView) findViewById(R.id.civ_all_big);
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

        mProgDialog = new ProgressDialog(this,"正在搜索数据");
//        mProgDialog.setTitle("正在搜索数据...");
//        mProgDialog.set("正在搜索相关信息....");
        mProgDialog.setCancelable(false);
        mProgDialog.getWindow().setDimAmount(0.7f);
        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        initMarkInfo();

        findViewById(R.id.ll_show_fragment).setOnTouchListener(this);

    }

    private void initMarkInfo(){
        mMarkInfoView       = getLayoutInflater().inflate(R.layout.layout_tip_show,null);
        mTxMarkTitle        = (TextView) mMarkInfoView.findViewById(R.id.tx_tip_show);
        mTxMarkTitle        .setOnClickListener(this);
        mMarkInfoView.findViewById(R.id.btn_little_begin_navi).setOnClickListener(this);
    }


    /**
     * 设置导航监听
     */
    private void setMapInteractiveListener() {

        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {

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

    private Handler findMyPoiDeley = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if ((System.currentTimeMillis() - saveTouchTime) >=  30 * 1000) {
                    if (mFragments.size()==0) {
                        findMyPosi();
                    }else {
                        findMyPoiDeley.sendEmptyMessageDelayed(0,30 * 1001);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };


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
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        mLocationProvider   .addLocationListener(this);
        mLocationProvider.addSearchListner(this);
        mLocationProvider.setAiosListener(aiosMapListener);
        BugHunter.countTimeEnd(getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();


        mLocationProvider.removeSearchListner(this);
        mLocationProvider   .removeLocationListener(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }




    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;

    }

    @Override
    public void deactivate() {
        mListener = null;

    }

    private void whenNorthChange(){

        aMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        aMap.moveCamera(CameraUpdateFactory.changeBearing(0));
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (aMapLocation != null) {
            mCity = aMapLocation.getCity();
            if (aMap !=null && mWatchStyle!= WATCH_NORTH){
            }
            if (aMap!=null && isFirst){
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
                        aMap.animateCamera(update);

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
//        if (mLocationMarker1!=null){
//            mLocationMarker1.setPosition(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()));
//            if (mWatchStyle != WATCH_NORTH){
//                mLocationMarker1.setVisible(true);
//            }
//        }

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    @Override
    public void searchSucceful() {
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
                mProgDialog.show();
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
                if (aMap!=null) {
                    aMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                updateScale();
                break;

            case R.id.btn_zoom_jian:

                if (aMap!=null) {
                    aMap.animateCamera(CameraUpdateFactory.zoomOut());
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

//            case R.id.tx_show_poi_name:
//                collectThePoi();
//                break;

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
                startFragment(new SettingFragment());
                break;


            default:
                break;
        }
    }



    private void clickComplete(){
        if (mComplete==null) {
//            startActivityForResult(new Intent(this, SearchCollectActivity.class), REQUEST_FIND_COMPLETE);
            SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
            searchCollectFragment.setRequestCode(REQUEST_FIND_COMPLETE);
            searchCollectFragment.setMapView(mapView);
            startFragment(searchCollectFragment);
        }else {
            mLatLng = new LatLng(mComplete.posLat,mComplete.posLon);
            mProgDialog.show();
            startCalueNavi();
        }
    }

    private void clickHome(){
        if (mHome==null) {
//            startActivityForResult(new Intent(this, SearchCollectActivity.class), REQUEST_FIND_HOME);
            SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
            searchCollectFragment.setRequestCode(REQUEST_FIND_HOME);
            searchCollectFragment.setMapView(mapView);
            startFragment(searchCollectFragment);
        }else {
            mLatLng = new LatLng(mHome.posLat,mHome.posLon);
            mProgDialog.show();
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
        mCollectDateHelper.getCollectItems();
    }


    private void findMyPosi(){
        LogUtils.d(TAG,"findMyPosi");
        try {

            marker.setVisible(false);
            if (mLocationProvider != null && mLocationProvider.getAmapLocation() != null && aMap != null) {
                int bear = 0;
                if (mWatchStyle == WATCH_3D) bear = 35;
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude())
//                    新的中心点坐标
                        , 17, //新的缩放级别
                        bear, //俯仰角0°~45°（垂直与地图时为0）
                        mSeeFloat  ////偏航角 0~360° (正北方为0)
                ));

                aMap.moveCamera(update);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void startCalueNavi(){
        LogUtils.d(TAG,"startCalueNavi");
        mProgDialog.show();
        mCollectDialog.dismiss();

        mLocationProvider.removeNaviCalueListner(this);
        mLocationProvider.addNaviCalueListner(this);
        List<NaviLatLng> startPoi = new ArrayList<>();
        startPoi.add(new NaviLatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
        List<NaviLatLng> wayPoi = new ArrayList<>();
        List<NaviLatLng> endPoi = new ArrayList<>();
        endPoi.add(new NaviLatLng(mLatLng.latitude,mLatLng.longitude));
        mLocationProvider.calueRunWay(startPoi,wayPoi,endPoi);
    }






    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            /*隐藏软键盘*/
            return true;
        }
        return super.dispatchKeyEvent(event);
    }





    private void startSearch(){
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
        SearchPosiFragment mSearchFragment = new SearchPosiFragment();
        startFragment(mSearchFragment);
//        mFragments.add(mSearchFragment);
//        transaction.replace(R.id.ll_show_fragment,mSearchFragment);
//        transaction.commit();
    }

    @Override
    public void exitFragment(){
        if (mFragments.size()==0){
            mReleavieView.setVisibility(View.VISIBLE);
            reInit();
            initMarker();
            mLsv.setVisibility(View.VISIBLE);
            findMyPosi();
            mLsv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    upDateLineTo(null);
                }
            },400);

            return;
        }
        if (mFragments.size()==1) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(mFragments.remove(mFragments.size() - 1));
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.commit();
            mReleavieView.setVisibility(View.VISIBLE);
            reInit();
            initMarker();
            mLsv.setVisibility(View.VISIBLE);
            findMyPosi();
            mLsv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    upDateLineTo(null);
                }
            },400);
            showWatchWay();
        }else {
            mFragments.remove(mFragments.size()-1);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment tFragment = mFragments.get(mFragments.size()-1);
            transaction.replace(R.id.ll_show_fragment,tFragment);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        }

    }

    @Override
    public void shouldFinish() {
        this.finish();
    }

    @Override
    public void startAcitivity(Class<?> cls ,Bundle bundle) {
        Intent intent = new Intent(this,cls);
        if (bundle!=null){
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    @Override
    public void startFragment(Fragment fragment) {
        if (mFragments.size()==0){
            CameraUpdate update1 = CameraUpdateFactory.changeBearing(0);
            aMap.moveCamera(update1);
        }

        aMap.setMyLocationEnabled(false);
        aMap.clear();
        mLsv.setVisibility(View.GONE);
        mDownLayout1.setVisibility(View.GONE);
        mFragments.add(fragment);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.ll_show_fragment,fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
        if (mReleavieView.getVisibility() == View.VISIBLE) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            alphaAnimation.setDuration(500);
            mReleavieView.setAnimation(alphaAnimation);
            mReleavieView.setVisibility(View.GONE);
        }
        if (mLocationMarker!=null){
            mLocationMarker.remove();
            mLocationMarker = null;
        }

    }

    @Override
    public void startFragmentReplace(Fragment fragment) {
        mFragments.remove(mFragments.size()-1);
        startFragment(fragment);
    }

    @Override
    public void startFragment(Class<?> cls) {
        //TODO
    }

    @Override
    public MapView getMapView() {
        return mapView;
    }

    @Override
    public void requestNaviCalue(LatLonPoint fromPoint, LatLonPoint toPoint) {
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        beginCalueNavi();
    }

    @Override
    public void showDialogwithOther() {
        if (mProgDialog!=null){
            mProgDialog.show();
        }
    }

    public void haveCalueNaviSucceful(int[] ints,double posLat,double posLon){
        LogUtils.d(TAG,"haveCalueNaviSucceful:posLat:"+posLat+"  posLon:"+posLon);
        mTxBilici.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);

        showRunNaviFragment(ints,posLat,posLon);



//        Intent intent = new Intent(this, ssShowPosiActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putIntArray(NAVI_MSG,ints);
//        double[] doubles = new double[4];
//        doubles[0] = mLocationProvider.getAmapLocation().getLatitude();
//        doubles[1] = mLocationProvider.getAmapLocation().getLongitude();
//        doubles[2] = posLat;
//        doubles[3] = posLon;
//        bundle.putDoubleArray(NAVI_POI,doubles);
//        intent.putExtra(ACTION_NAVI_COMP,bundle);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        startActivity(intent);
//        if (mFragments.size()>0) {
//            FragmentManager manager = getFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.remove(mFragments.remove(mFragments.size() - 1));
//            transaction.commit();
//            mFragments .clear();
//        }
    }

    private void showRunNaviFragment(int[] ints , double posLat,double posLon){
        aMap.clear();
        double lat = mLocationProvider.getAmapLocation().getLatitude();
        double lon  = mLocationProvider.getAmapLocation().getLongitude();
        RunNaviWayFragment runNaviWayFragment = new RunNaviWayFragment();
        runNaviWayFragment.setSucceful(ints);
        runNaviWayFragment.setMapView(mapView);
        runNaviWayFragment.setPosiFromTo(new LatLonPoint(lat,lon),new LatLonPoint(posLat,posLon));
        startFragment(runNaviWayFragment);
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

    //    private void upDateLineTo(CameraPosition cameraPosition){
    private void upDateLineTo(CameraPosition position){
//        if (position==null) {
            try {
                LatLng mLatlon = new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude());
                Projection projection = aMap.getProjection();
                Point pM = projection.toScreenLocation(mLatlon);
//            Point pT = projection.toScreenLocation(cameraPosition.target);
//                LogUtils.d(TAG, "upDateLineTo:\nx:" + pM.x + "\ny:" + pM.y);
                mLsv.setPoint(pM);
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }else {
//            LatLng mLatlon = new LatLng(mLocationProvider.getAmapLocation().getLatitude(), mLocationProvider.getAmapLocation().getLongitude());
//            Projection projection = aMap.getProjection();
//            Point pM = projection.toScreenLocation(mLatlon);
//
////            Point pT = projection.toScreenLocation(cameraPosition.target);
//            LogUtils.d(TAG, "upDateLineTo:\nx:" + pM.x + "\ny:" + pM.y);
//
//            mLsv.setPoint(pM);
//        }
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

        if (mMarkerPoi !=null) {
            mMarkerPoi.setVisible(false);
        }

        if (mMarkerPoi!=null) {
            mMarkerPoi.hideInfoWindow();
        }
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
        mMarkerPoi.showInfoWindow();



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

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess: mLatLng:"+mLatLng);
        mTxBilici.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);
        if (mFragments.size()>0){
            Fragment fragment = mFragments.get(mFragments.size()-1);
            if (fragment instanceof RunNaviWayFragment ||
                    fragment instanceof RadarNaviFragment ||
                        fragment instanceof SearchCollectFragment ||
                            fragment instanceof ShowSearchPoiFragment){

            }else {
                haveCalueNaviSucceful(ints, mLatLng.latitude, mLatLng.longitude);
            }
        }else {
            haveCalueNaviSucceful(ints, mLatLng.latitude, mLatLng.longitude);
        }
    }

    @Override
    public void onCalculateRouteSuccess() {
        mTxBilici.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);

    }

    @Override
    public void onCalculateRouteFailure() {
        mTxBilici.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);
        ToastUtil.show(this,"路径规划失败，请稍后再试");
    }


    private void changeTriffical(){
        if (aMap!=null) {
            isTraff = !isTraff;
            aMap.setTrafficEnabled(isTraff);
            if (isTraff){
                mIvShowTraffic.setImageResource(R.drawable.icon_lukuang_01);
            }else {
                mIvShowTraffic.setImageResource(R.drawable.icon_lukuang_02);
            }
        }
    }

    private void showWatchWay(){
        if (aMap!=null){
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
                            mLocationMarker  = aMap.addMarker(new MarkerOptions()
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
                        aMap.animateCamera(update1);
                    }
                    if (mLocationMarker!=null){
                        mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_0));
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }else{
                        mLocationMarker  = aMap.addMarker(new MarkerOptions()
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
                    aMap.animateCamera(update1);
                    if (mLocationMarker!=null){
                        mLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_seewatch_0));
                        mLocationMarker.setPosition(new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
                    }else{
                        mLocationMarker  = aMap.addMarker(new MarkerOptions()
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
    public void onCollectCallBack(List<CollectItem> collectItems) {
        if (collectItems!=null) {
            LogUtils.d(TAG, "onCollectCallBack:SIZE" + collectItems.size());
        }
//        if (collectItems==null || collectItems.size()==0)return;
        if (mCollectDialog!=null) {
            mCollectDialog.setDate(collectItems);
            mCollectDialog.show();
        }
    }

    @Override
    public void onClickCollectItem(int position, CollectItem item) {
        //TODO
        LogUtils.d(TAG,"onClickCollectItem:"+item.pName);
        if (item!=null) {
            mLatLng = new LatLng(item.posLat, item.posLon);
            startCalueNavi();
        }
    }

    public void setPosi(double lat,double lon){
        mLatLng = new LatLng(lat,lon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.d(TAG,"onActivityResult："+"requestCode "+requestCode+"  resultCode:"+resultCode);
        if (resultCode == RESULT_OK){
            if (data==null)return;
            Bundle bundle = data.getExtras();
            if (bundle==null)return;
            String name = bundle.getString("name");
            String desc = bundle.getString("desc");
            double posLat = bundle.getDouble("poiLat");
            double posLon = bundle.getDouble("poiLon");
            mCollectDateHelper.saveWhereIten(requestCode,name,desc,posLat,posLon);
            if (mFragments.size()!=0)return;
            mLatLng = new LatLng(posLat,posLon);
            mProgDialog.show();
            startCalueNavi();
            switch (requestCode){
                case REQUEST_FIND_COMPLETE:

                    break;

                case REQUEST_FIND_HOME:

                    break;

                default:
                    break;
            }

        }
    }



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

    public void showCollectDialog(){
        mCollectDateHelper.getCollectItems();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            if (mFragments.size()>0){
                //TODO
//                exitFragment();
//                return true;
            }
        }
        return false;
    }


    private AMap.OnPOIClickListener mPoiClickListener = new AMap.OnPOIClickListener() {
        @Override
        public void onPOIClick(Poi poi) {
            try {
                if (mFragments.size()==0) {
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(poi.getCoordinate()));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void beginCalueNavi(){
        if (fromPoint!=null && toPoint!=null){
            List<NaviLatLng> startList = new ArrayList<>();
            List<NaviLatLng> wayList = new ArrayList<>();
            List<NaviLatLng> endList = new ArrayList<>();
            startList.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
            endList.clear();
            endList.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
            mProgDialog.show();
            mLocationProvider.calueRunWay(startList,wayList,endList);
        }
    }
    float saveA = 0;
    class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
//            float a = event.values[0];
//            if (mLocationMarker!=null){
//                mLocationMarker.setRotateAngle(360 - a);
//            }
            if (mFragments.size()==0) {
                if (mLocationMarker != null) {
                    mLocationMarker.setRotateAngle(360 - saveA);
                }

                if (mWatchStyle != WATCH_NORTH) {
                    if ((System.currentTimeMillis() - sensorChangeTime > 300)) {
                        if (Math.abs(event.values[0] - saveA) > 5) {
                            saveA = event.values[0];

                            mSeeFloat = saveA;
                            aMap.animateCamera(CameraUpdateFactory.changeBearing(saveA));
                            sensorChangeTime = System.currentTimeMillis();
                        }
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
        float pixel = aMap.getScalePerPixel();
        LogUtils.d(TAG,"updateScale:"+pixel);
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

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }


    private XpAiosMapListener aiosMapListener = new XpAiosMapListener() {
        @Override
        public void onStartNavi(@NonNull String s, @NonNull PoiBean poiBean) {
            if (mFragments.size()>0){
                Fragment fragment = mFragments.get(mFragments.size()-1);
                if (fragment instanceof RadarNaviFragment){
                    return;
                }
            }
            NaviLatLng naviLatLng = new NaviLatLng(poiBean.getLatitude(),poiBean.getLongitude());
            List<NaviLatLng> endList = new ArrayList<>();
            endList.add(naviLatLng);
            mLocationProvider.tryCalueRunWay(endList);
            if (mProgDialog!=null){
                mProgDialog.show();
            }
        }

        @Override
        public void onStartNavi(double lat, double lon) {
            if (mFragments.size()>0){
                Fragment fragment = mFragments.get(mFragments.size()-1);
                if (fragment instanceof RadarNaviFragment){
                    return;
                }
            }
            NaviLatLng naviLatLng = new NaviLatLng(lat,lon);
            List<NaviLatLng> endList = new ArrayList<>();
            endList.add(naviLatLng);
            mLocationProvider.tryCalueRunWay(endList);
            if (mProgDialog!=null){
                mProgDialog.show();
            }
        }

        @Override
        public void onCancelNavi(@NonNull String s) {

        }

        @Override
        public void onOverview(@NonNull String s) {
            findMyPosi();
        }

        @Override
        public void onRoutePlanning(@NonNull String s, @NonNull String s1) {

        }

        @Override
        public void onZoom(@NonNull String s, int i) {
            LogUtils.d(TAG,"onZoom:"+i);
            if (i==0){
                aMap.animateCamera(CameraUpdateFactory.zoomOut());
            }else {
                aMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        }

        @Override
        public void onLocate(@NonNull String s) {
            findMyPosi();
        }
    };

    private void readIntent(Intent intent){

    }


}
