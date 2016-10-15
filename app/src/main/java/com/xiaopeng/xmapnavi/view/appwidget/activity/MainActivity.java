package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.StereoView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.TipPopWindow;
import com.xiaopeng.xmapnavi.view.appwidget.services.LocationProService;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements LocationSource,XpLocationListener,View.OnClickListener,TextWatcher
        ,View.OnFocusChangeListener, View.OnLayoutChangeListener
        ,Inputtips.InputtipsListener ,TipItemClickListener
        ,XpSearchListner
{
    public static final String TAG = "MainActivity";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final int REQ_HAVE_RESULT = 1;
    private static final String ACTION_MSG = "ACTION_MSG";
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

    //----//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityRootView = findViewById(R.id.root_layout);
        startService(new Intent(this, LocationProService.class));
        mapView = (MapView) findViewById(R.id.map_main);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        initView();
        init();
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/5
        keyHeight = screenHeight/5;


    }


    @Override
    protected void onStart() {
        super.onStart();
        mLocationProvider    = LocationProvider.getInstence(this);
        mLocationProvider   .addLocationListener(this);
//        mLocationProvider   .addSearchListner(this);
        activityRootView    .addOnLayoutChangeListener(this);

    }



    @Override
    protected void onStop() {
        super.onStop();
        mLocationProvider   .removeLocationListener(this);
        mLocationProvider   = null;
        activityRootView    .removeOnLayoutChangeListener(this);
    }

    /**
     * 初始化各种对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 初始化 显示我的位置的Marker
            myLocationMarker = aMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.car))));
            aMap.setLocationSource(this);// 设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
            aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
            aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE);



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

        //--listener--//
        mTvSearch           .setOnClickListener(this);
        mEtvSearch          .addTextChangedListener(this);
        mEtvSearch          .setOnFocusChangeListener(this);
        mTipWindow          .setOnTipItemClickListener(this);
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
        mapView.setVisibility(View.VISIBLE);
        mLocationProvider.addSearchListner(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        mapView.setVisibility(View.GONE);
        mLocationProvider.removeSearchListner(this);
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
        if (!handler.hasMessages(0)){
            handler.sendEmptyMessageDelayed(0,50);
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
//                mStvSearch.toNext();
                mStvSearch.setFocusable(false);
                mStvSearch.setFocusableInTouchMode(false);

                mEtvSearch.setFocusable(true);
                mEtvSearch.setFocusableInTouchMode(true);
                mEtvSearch.requestFocus();
                mEtvSearch.findFocus();

                break;

            default:
                break;
        }
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


}
