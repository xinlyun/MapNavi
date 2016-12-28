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
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.navi.model.AMapNaviPath;
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
import com.xiaopeng.xmapnavi.view.appwidget.fragment.MainFragment;
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

public class MainActivity extends Activity implements BaseFuncActivityInteface,XpNaviCalueListener
        , XpCollectListener
        ,ShowCollectDialog.CollectDialogListener

{
    public static final String TAG = "MainActivity";
    private DateHelper mCollectDateHelper;
    private MapView mapView;
    private AMap aMap;
    //    private TipPopWindow mTipWindow;

    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    private int screenHeight = 0;
    //View//
    //Activity最外层的Layout视图
    private List<Fragment> mFragments;

    //----//
    private ProgressDialog mProgDialog,mProgDialog2;

    private ILocationProvider mLocationProvider;
    private ShowCollectDialog mCollectDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        super.onCreate(savedInstanceState);
        mFragments = new ArrayList<>();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startFragment(new MainFragment());
        mapView = (MapView) findViewById(R.id.map_main);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
        //阀值设置为屏幕高度的1/5
        keyHeight = screenHeight/5;

        mLocationProvider = LocationProvider.getInstence(this);
        startService(new Intent(this, LocationProService.class));
        initView();
        mLocationProvider   .addNaviCalueListner(this);
        mCollectDateHelper = new DateHelper();
        mCollectDateHelper.setOnCollectListener(this);

    }





    @Override
    protected void onStart() {
        super.onStart();
        if (aMap==null) {

            mapView.onCreate(null);// 此方法必须重写
            aMap = mapView.getMap();
            MyTrafficStyle myTrafficStyle = new MyTrafficStyle();

        }
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCollectDialog==null) {
                    mCollectDialog = new ShowCollectDialog(MainActivity.this);
                    mCollectDialog.setCollectDialogListener(MainActivity.this);
                }

            }
        },1000);

        mLocationProvider    = LocationProvider.getInstence(this);
    }






    @Override
    protected void onStop() {
        super.onStop();

//        mapView.setVisibility(View.GONE);
        if (mFragments.size()==0){
            aMap.clear();
        }
    }





    /**
     *  初始化View
     */
    private void initView(){
//        mCirIV              = (CircleImageView) findViewById(R.id.civ_all_big);


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

        mProgDialog2 = new ProgressDialog(this,"正在绘制，请稍后...");
//        mProgDialog.setTitle("正在搜索数据...");
//        mProgDialog.set("正在搜索相关信息....");
        mProgDialog2.setCancelable(false);
        mProgDialog2.getWindow().setDimAmount(0.7f);
        //----init listener ---//
        mProgDialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();


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
        mLocationProvider   .removeNaviCalueListner(this);
        mapView.onDestroy();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            /*隐藏软键盘*/
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void exitFragment(){
        if (mFragments.size()==0){

            return;
        }
        if (mFragments.size()==1) {
//            if (aMap!=null) {
//                aMap.clear();
//            }
            return;
//            FragmentManager manager = getFragmentManager();
//            FragmentTransaction transaction = manager.beginTransaction();
//            transaction.remove(mFragments.remove(mFragments.size() - 1));
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//            transaction.commit();

        }else {
            mFragments.remove(mFragments.size()-1);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment tFragment = mFragments.get(mFragments.size()-1);
            transaction.replace(R.id.ll_show_fragment,tFragment);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();
        }
        if (mFragments.size() > 0) {
            Fragment fragment = mFragments.get(mFragments.size() - 1);
            if (!(fragment instanceof RunNaviWayFragment || fragment instanceof RadarNaviFragment)) {
                if (aMap!=null) {
                    aMap.setCustomMapStylePath("/sdcard/style_json.json");
                    aMap.setMapCustomEnable(aMap.isTrafficEnabled());
                }
            }
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
        if (mFragments.size()==1){
            CameraUpdate update1 = CameraUpdateFactory.changeBearing(0);
            if (aMap!=null) {
                aMap.moveCamera(update1);
            }
        }



        if (aMap!=null) {
            aMap.setMyLocationEnabled(false);
            aMap.clear();
        }

        if (fragment instanceof RadarNaviFragment || fragment instanceof RunNaviWayFragment){
//            aMap.setCustomMapStylePath("/sdcard/style2.json");
            if (aMap!=null) {
                aMap.setCustomMapStylePath("/sdcard/style_json.json");
            }

        }else {

        }

//        mLsv.setVisibility(View.GONE);
//        mDownLayout1.setVisibility(View.GONE);
        mFragments.add(fragment);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.ll_show_fragment,fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.commit();
//        if (mReleavieView.getVisibility() == View.VISIBLE) {
//            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
//            alphaAnimation.setDuration(500);
//            mReleavieView.setAnimation(alphaAnimation);
//            mReleavieView.setVisibility(View.GONE);
//        }
//        if (mLocationMarker!=null){
//            mLocationMarker.remove();
//            mLocationMarker = null;
//        }

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
//        this.fromPoint = fromPoint;
//        this.toPoint = toPoint;
//        beginCalueNavi();
        if (fromPoint!=null && toPoint!=null){
            List<NaviLatLng> startList = new ArrayList<>();
            List<NaviLatLng> wayList = new ArrayList<>();
            List<NaviLatLng> endList = new ArrayList<>();
            startList.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
            endList.clear();
            endList.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
//            mProgDialog.show();
            showDialogwithOther();
            mLocationProvider.calueRunWay(startList,wayList,endList);
        }
    }

    @Override
    public void showCollectDialog(){
        mCollectDateHelper.getCollectItems();
    }

    @Override
    public int getFragmentNum() {
        return mFragments.size();
    }



    @Override
    public void showDialogwithOther() {
        LogUtils.d(TAG,"showDialogwithOther");
        if (mProgDialog!=null){
            mProgDialog.show();
        }
    }

    @Override
    public void forShowDeleyDialog() {
        if (mProgDialog2!=null){
            mProgDialog2.show();
        }
        if (mProgDialog!=null && mProgDialog.isShowing()){
            mProgDialog.dismiss();
        }
    }

    @Override
    public void dismissDeleyDialog() {
        if (mProgDialog2!=null){
            mProgDialog2.dismiss();
        }
    }



    public void haveCalueNaviSucceful(int[] ints,double posLat,double posLon){
        LogUtils.d(TAG,"haveCalueNaviSucceful:posLat:"+posLat+"  posLon:"+posLon);
        mapView.postDelayed(new Runnable() {
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
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    mProgDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },2000);
        double lat =0,lon =0;
        HashMap<Integer,AMapNaviPath> pathHashMap = mLocationProvider.getNaviPaths();
        if (pathHashMap!=null && pathHashMap.size()>0){
            NaviLatLng naviLatLng = pathHashMap.get(ints[0]).getEndPoint();
            lat = naviLatLng.getLatitude();
            lon = naviLatLng.getLongitude();
        }


        if (mFragments.size()>0){
            Fragment fragment = mFragments.get(mFragments.size()-1);
            if (fragment instanceof RunNaviWayFragment ||
                    fragment instanceof RadarNaviFragment ||
                        fragment instanceof SearchCollectFragment ||
                            fragment instanceof ShowSearchPoiFragment){

            }else {
                haveCalueNaviSucceful(ints, lat, lon);
            }
        }else {
            haveCalueNaviSucceful(ints, lat, lon);
        }
    }

    @Override
    public void onCalculateRouteSuccess() {
        mapView.postDelayed(new Runnable() {
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
        mapView.postDelayed(new Runnable() {
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
//            findMyPosi();
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
//            findMyPosi();
        }
    };


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
        mCollectDialog.dismiss();
        if (item!=null) {

            List<NaviLatLng> endWay = new ArrayList<>();
            endWay.add(new NaviLatLng(item.posLat,item.posLon));
            mLocationProvider.tryCalueRunWay(endWay);
            showDialogwithOther();
        }
    }


}
