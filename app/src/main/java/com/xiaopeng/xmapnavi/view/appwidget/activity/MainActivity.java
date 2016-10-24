package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.animation.Animation;
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
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SearchPosiFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowPosiFragment;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.StereoView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.TipPopWindow;
import com.xiaopeng.xmapnavi.view.appwidget.services.LocationProService;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.internal.Util;

public class MainActivity extends Activity implements LocationSource,XpLocationListener,View.OnClickListener,TextWatcher
        ,View.OnFocusChangeListener, View.OnLayoutChangeListener
        ,Inputtips.InputtipsListener ,TipItemClickListener
        ,XpSearchListner ,AMap.OnMarkerDragListener
        ,AMap.OnCameraChangeListener, AMap.OnMarkerClickListener
        ,GeocodeSearch.OnGeocodeSearchListener,XpNaviCalueListener
{
    public static final String TAG = "MainActivity";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final int REQ_HAVE_RESULT = 1;
    private static final String ACTION_MSG = "ACTION_MSG";
    private static final String ACTION_NAVI_COMP = "ACTION_COMP";
    private static final String NAVI_MSG ="ints";

    private static final String NAVI_POI ="NAVI_POI";

    private String mSearchName ;
    private ILocationProvider mLocationProvider;
    private MapView mapView;
    private AMap aMap;
    private Marker myLocationMarker;
    private TipPopWindow mTipWindow;
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
    private TextView mTvSearch;
    private StereoView mStvSearch;
    private EditText mEtvSearch;
    //Activity最外层的Layout视图
    private View activityRootView;
    private SearchPosiFragment mSearchFragment;
    private Marker marker,mMarkerPoi;
    private PolylineOptions polylineOptions = new PolylineOptions();

    private Polyline mPolyline;
    private ScaleAnimation scaleAnimation = new ScaleAnimation (0,1,0,1);
    private GeocodeSearch geocodeSearch;
    private LinearLayout mDownLayout0,mDownLayout1;

    private TextView mTvPoiName,mTvPoiStr,mTvPoiDis;


    private LatLng mLatLng;
    //----//
    private ProgressDialog mProgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationProvider    = LocationProvider.getInstence(this);
        mLocationProvider   .addNaviCalueListner(this);
        activityRootView = findViewById(R.id.root_layout);
        startService(new Intent(this, LocationProService.class));
        mapView = (MapView) findViewById(R.id.map_main);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        initView();

        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/5
        keyHeight = screenHeight/5;

        mSearchFragment = new SearchPosiFragment();
        LocationProvider.getInstence(this);

        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) activityRootView.getLayoutParams();
                Log.d(TAG,"layoutparams width:"+layoutParams.width);
                marker = aMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.amap_car))
                        .draggable(true));
                marker.setPositionByPixels(540,720);
                marker.setAnchor(0.5f,0.5f);

                mMarkerPoi = aMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.pre_list_img0))
                        .draggable(true));
                mMarkerPoi.setPositionByPixels(540,720);
                mMarkerPoi.setAnchor(0.5f,1f);
                mMarkerPoi.setVisible(false);
                scaleAnimation.setDuration(540);
                mapView.getMap().setOnMarkerClickListener(MainActivity.this);
                geocodeSearch = new GeocodeSearch(MainActivity.this);
                geocodeSearch.setOnGeocodeSearchListener(MainActivity.this);
            }
        },1000);

    }


    @Override
    protected void onStart() {
        super.onStart();
        init();

        mLocationProvider    = LocationProvider.getInstence(this);

//        mLocationProvider   .addSearchListner(this);
        activityRootView    .addOnLayoutChangeListener(this);

        mapView.setVisibility(View.VISIBLE);

    }




    @Override
    protected void onStop() {
        super.onStop();
//        mLocationProvider   .removeLocationListener(this);
        mLocationProvider   .removeNaviCalueListner(this);
//        mLocationProvider   = null;
        activityRootView    .removeOnLayoutChangeListener(this);
        mapView.setVisibility(View.GONE);

    }

    /**
     * 初始化各种对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NAVI);
            // 初始化 显示我的位置的Marker
            myLocationMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.car))));
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CameraUpdate update = CameraUpdateFactory.zoomTo(15);
                    aMap.animateCamera(update);
                }
            },500);
            aMap.setOnMarkerDragListener(this);
            aMap.setOnCameraChangeListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(2);
            polylineOptions.color(Color.argb(255, 1, 1, 1));
            polylineOptions.setDottedLine(true);
            polylineOptions.aboveMaskLayer(true);
//            polylineOptions.addAll(latLngs);

//                            if (mPolyline !=null){
//                                mPolyline.remove();
//                                mPolyline = null;
//                            }
            mPolyline = aMap.addPolyline(polylineOptions);
            setMapInteractiveListener();

        }

    }

    /**
     *  初始化View
     */
    private void initView(){
        mEtvSearch          = (EditText) findViewById(R.id.edt_search);
        mEtvSearch          .setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mTvSearch           = (TextView) findViewById(R.id.tx_search_start);
        mStvSearch          = (StereoView) findViewById(R.id.stv_search);
        mTipWindow          = new TipPopWindow(mEtvSearch);
        mDownLayout0        = (LinearLayout) findViewById(R.id.navistart_down_llayout3);
        mDownLayout1        = (LinearLayout) findViewById(R.id.layout_down);
        mTvPoiName          = (TextView) findViewById(R.id.tv_poi_name);
        mTvPoiStr           = (TextView) findViewById(R.id.tv_poi_str);
        mTvPoiDis           = (TextView) findViewById(R.id.tv_poi_dis);

        //--listener--//
        mTvSearch           .setOnClickListener(this);
        mEtvSearch          .addTextChangedListener(this);
        mEtvSearch          .setOnFocusChangeListener(this);
        mTipWindow          .setOnTipItemClickListener(this);
        findViewById(R.id.btn_begin_navi).setOnClickListener(this);

        mProgDialog = new ProgressDialog(this);
        mProgDialog.setTitle("正在搜索数据");
        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(true);


        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
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
                        hideKeyboard(mEtvSearch);
                        // 按下屏幕
                        // 如果timer在执行，关掉它
                        clearTimer();
                        // 改变跟随状态
                        isNeedFollow = false;
                        break;

                    case MotionEvent.ACTION_UP:
                        // 离开屏幕
                        startTimerSomeTimeLater();
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;

                    default:
                        break;
                }
            }
        });

    }
    //隐藏虚拟键盘
    public static void hideKeyboard(View v)
    {
        InputMethodManager imm = ( InputMethodManager ) v.getContext( ).getSystemService( Context.INPUT_METHOD_SERVICE );
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow( v.getApplicationWindowToken( ) , 0 );

        }
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
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

        mLocationProvider   .addLocationListener(this);
        mLocationProvider.addSearchListner(this);
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

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            mCity = aMapLocation.getCity();
        }
        if (mListener != null){
            mListener.onLocationChanged(aMapLocation);
        }
    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    @Override
    public void searchSucceful() {

        mTipWindow.dismiss();
        tryToShowPosi();
    }
    public void tryToShowPosi(){
        if (!handler.hasMessages(0)){
            handler.sendEmptyMessageDelayed(0,10);
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG,"searchSucceful");
            if (mStvSearch != null){
                if (mLocationProvider.getPoiResult() != null && mLocationProvider.getPoiResult().getPois().size()>=1){

                    Intent intent = new Intent(MainActivity.this,ShowPosiActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra(ACTION_SEARCH,REQ_HAVE_RESULT);
                    intent.putExtra(ACTION_MSG,mSearchName);
                    startActivity(intent);
                } else if ( mLocationProvider.getPoiResult() == null  || mLocationProvider.getPoiResult().getPois().size() < 1){
                    Toast.makeText(MainActivity.this,"查无结果，请重试",Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tx_search_start:
//                mStvSearch.setFocusable(false);
//                mStvSearch.setFocusableInTouchMode(false);
//
//                mEtvSearch.setFocusable(true);
//                mEtvSearch.setFocusableInTouchMode(true);
//                mEtvSearch.requestFocus();
//                mEtvSearch.findFocus();

                startSearch();

//                Intent intent = new Intent(this,SearchPosiActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                startActivity(intent);
//                finish();
                break;

            case R.id.btn_begin_navi:
                mProgDialog.show();
               startCalueNavi();
                mDownLayout0.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgDialog.dismiss();
                    }
                },6 * 1000);
                break;

            default:
                break;
        }
    }

    private void startCalueNavi(){
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String newText = charSequence.toString().trim();
        if (newText.length()>1) {
            InputtipsQuery inputquery = new InputtipsQuery(newText, mCity);
            inputquery.setCityLimit(true);
            Inputtips inputTips = new Inputtips(MainActivity.this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        }else {
            mTipWindow.dismiss();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (view.getId() == R.id.edt_search){
            if (b){

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((InputMethodManager)MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(mEtvSearch,InputMethodManager.SHOW_FORCED);
                    }
                },200);

                if (mStvSearch.getCurScreen() == 1){
                    mStvSearch.toNext();
                }
            }else {
                if (mStvSearch.getCurScreen() != 1){
                    mStvSearch.toPre();
                }
            }
        }
    }

    @Override
    public void  onLayoutChange(View v, int left, int top, int right,
                                int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > keyHeight)){

//            Toast.makeText(MainActivity.this, "监听到软键盘弹起...", Toast.LENGTH_SHORT).show();

        }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > keyHeight)){

//            Toast.makeText(MainActivity.this, "监听到软件盘关闭...", Toast.LENGTH_SHORT).show();

            if (mEtvSearch!=null && mStvSearch != null){
                mEtvSearch.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEtvSearch.setFocusable(false);
                        mEtvSearch.setFocusableInTouchMode(false);
                        if (mStvSearch.getCurScreen() == 2){
                            mStvSearch.toPre();
                        }
                        mEtvSearch.setText("");
                    }
                },200);

            }
        }
    }


    @Override
    public void onGetInputtips(List<Tip> tipList, int index) {
        List<HashMap<String, String>> listString = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < tipList.size(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", tipList.get(i).getName());
            map.put("address", tipList.get(i).getDistrict());
            listString.add(map);
        }
        if (mTipWindow != null){
            mTipWindow.setData(listString);
            mTipWindow .show();
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            /*隐藏软键盘*/
            hideKeyboard(mEtvSearch);
            return readAndCompleInput();
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean readAndCompleInput(){
        String searchReqStr = mEtvSearch.getText().toString();
        mSearchName = searchReqStr;
        if (searchReqStr.length() > 1){
            if (mStvSearch.getCurScreen() == 2){
                mStvSearch.toNext();
            }
            mLocationProvider.trySearchPosi(searchReqStr);
            return true;
        }else {
            return false;
        }
    }


    @Override
    public void onClickItem(int index, HashMap<String, String> hashStr) {
        String strName = hashStr.get("name");
        mEtvSearch  .setText(strName);
        hideKeyboard(mEtvSearch);
        readAndCompleInput();
    }

    private void startSearch(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.ll_show_fragment,mSearchFragment);
        transaction.commit();
        mLocationProvider.removeNaviCalueListner(this);
    }

    public void exitFragment(){
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(mSearchFragment);
        transaction.commit();
        mLocationProvider.addNaviCalueListner(this);

    }

    public void haveCalueNaviSucceful(int[] ints,float posLat,float posLon){
        Intent intent = new Intent(this, ShowPosiActivity.class);
        Bundle bundle = new Bundle();
        bundle.putIntArray(NAVI_MSG,ints);
        double[] doubles = new double[4];
        doubles[0] = mLocationProvider.getAmapLocation().getLatitude();
        doubles[1] = mLocationProvider.getAmapLocation().getLongitude();
        doubles[2] = posLat;
        doubles[3] = posLon;
        bundle.putDoubleArray(NAVI_POI,doubles);
        intent.putExtra(ACTION_NAVI_COMP,bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

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
    public void onCameraChange(CameraPosition cameraPosition) {
        if (marker!=null) {
            marker.setVisible(true);
        }

        if (mMarkerPoi !=null) {
            mMarkerPoi.setVisible(false);
        }

        if (mDownLayout0.getVisibility() == View.GONE){
            mDownLayout1.setVisibility(View.GONE);
            mDownLayout0.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mLatLng = cameraPosition.target;
        marker.setVisible(false);

        if (!equalNavLat(cameraPosition.target,new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude())) ){
            mMarkerPoi.setAnimation(scaleAnimation);
            mMarkerPoi.setVisible(true);
            mMarkerPoi.startAnimation();
        }
    }



    private boolean equalNavLat(LatLng latLon1,LatLng latLng2){
        if ((latLng2.latitude == latLon1.latitude) && latLng2.longitude == latLon1.longitude){
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
            LatLng latLng = mMarkerPoi.getPosition();
            mTvPoiName.setText(getString(R.string.load_get_msg));

            int dis = (int) AMapUtils.calculateLineDistance(latLng,new LatLng(mLocationProvider.getAmapLocation().getLatitude(),mLocationProvider.getAmapLocation().getLongitude()));
            if (dis > 1000){
                mTvPoiDis.setText(""+(dis/1000)+getString(R.string.kmile));
            }else {
                mTvPoiDis.setText(""+dis+getString(R.string.mile));
            }
            mTvPoiStr.setText("");
            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude,latLng.longitude), 200, GeocodeSearch.AMAP);
            geocodeSearch.getFromLocationAsyn(query);
            mDownLayout0.setVisibility(View.GONE);
            mDownLayout1.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
        if (address.getAois()!=null && address.getAois().size()>0) {
            AoiItem aoiItem = address.getAois().get(0);
            mTvPoiName.setText(aoiItem.getAoiName());
            mTvPoiStr.setText(getUsefulInfo(address));
        }else {
            mTvPoiName.setText(getUsefulInfo(address));
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

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        haveCalueNaviSucceful(ints,(float)mLatLng.latitude,(float)mLatLng.longitude);
    }

    @Override
    public void onCalculateRouteSuccess() {

    }
}
