package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.xiaopeng.amaplib.util.Utils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HistoryPosi;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistoryAndNaviAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RadarNaviFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RunNaviWayFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowPosiFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linzx on 2016/10/13.
 */
public class ShowPosiActivity extends Activity implements XpNaviCalueListener,BaseFuncActivityInteface
{

    private static final String TAG = "ShowPosiActivity";
    private static final String ACTION_NAVI_COMP = "ACTION_COMP";
    private static final String NAVI_MSG ="ints";
    private static final String NAVI_POI ="NAVI_POI";
    private List<Fragment> mFragments;

    private MapView mapView ;
    private FrameLayout mLlFragAdd;
    RunNaviWayFragment runNaviWayFragment = new RunNaviWayFragment();
    ShowPosiFragment showPosiFragment = new ShowPosiFragment();
    RadarNaviFragment radarNaviFragment = new RadarNaviFragment();
    private ILocationProvider mLocationPro;

    private LatLonPoint fromPoint,toPoint;
    private List<NaviLatLng> startList = new ArrayList<NaviLatLng>();
    /**
     * 途径点坐标集合
     */
    private List<NaviLatLng> wayList = new ArrayList<NaviLatLng>();
    /**
     * 终点坐标集合［建议就一个终点］
     */
    private List<NaviLatLng> endList = new ArrayList<NaviLatLng>();
    private ProgressDialog mProgDialog;

    private boolean isReadyNavi = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        BugHunter.statisticsStart(BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
        super.onCreate(savedInstanceState);
        mFragments = new ArrayList<>();
        setContentView(R.layout.activity_search_posi);
//        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        mLocationPro = LocationProvider.getInstence(this);

        mapView   = (MapView) findViewById(R.id.tmv_search_show);
        mapView   .onCreate(savedInstanceState);
        mapView     .getMap().setMapType(AMap.MAP_TYPE_NAVI);
        mLlFragAdd= (FrameLayout) findViewById(R.id.ll_show_fragment);

        initView();
        mapView .postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!readNaviIntent(getIntent())) {
                    mLlFragAdd.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FragmentManager manager = ShowPosiActivity.this.getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            showPosiFragment.setMapView(mapView);
                            transaction.replace(R.id.ll_show_fragment, showPosiFragment);
                            transaction.commit();
                        }
                    }, 150);
                }
            }
        },350);
    }


    private void initView(){
        mProgDialog = new ProgressDialog(this);
        mProgDialog.setTitle("多样化路径计算");
        mProgDialog.setMessage("正在计算路径......");
        mProgDialog.setCancelable(true);
        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView   .onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        LogUtils.d(TAG,"onStart");
        super.onStart();
        mLocationPro.addNaviCalueListner(this);



    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d(TAG,"onStop");
        mLocationPro.removeNaviCalueListner(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readNaviIntent(intent);
    }



    @Override
    protected void onDestroy() {
        LogUtils.d(TAG,"onDestroy");
        super.onDestroy();
        mapView.onDestroy();

    }

    public void requestCalueNaviPlan(LatLonPoint fromPoint,LatLonPoint toPoint){
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        beginCalueNavi();

    }


    private void beginCalueNavi(){
        if (fromPoint!=null && toPoint!=null){
            isReadyNavi = true;
            startList.clear();
            startList.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
            endList.clear();
            endList.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
            mProgDialog.show();
            if (mapView!=null){
                mapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isReadyNavi = false;
                        mProgDialog.dismiss();
                    }
                },6000);

            }
            mLocationPro.calueRunWay(startList,wayList,endList);
        }
    }

    private boolean readNaviIntent(Intent intent){
        if (intent == null)return false;
        Bundle bundle = intent.getBundleExtra(ACTION_NAVI_COMP);
        if (bundle == null) return false;
        int[] ints = bundle.getIntArray(NAVI_MSG);
        if (ints == null || ints.length<1)return false;

        runNaviWayFragment.setSucceful(ints);
        double[] doubles = bundle.getDoubleArray(NAVI_POI);
        LatLonPoint fromPoi = new LatLonPoint(doubles[0],doubles[1]);
        LatLonPoint toPoi = new LatLonPoint(doubles[2],doubles[3]);
        this.fromPoint = fromPoi;
        this.toPoint = toPoi;
        LogUtils.d(TAG,"from:"+fromPoi+"\ntoPoi:"+toPoi);
        runNaviWayFragment.setPosiFromTo(fromPoi,toPoi);
        showRunNaviFragment();
        setIntent(null);
        return true;
    }


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        mProgDialog.dismiss();
        if (isReadyNavi) {
            runNaviWayFragment.setSucceful(ints);
            showRunNaviFragment();
        }
    }

    @Override
    public void onCalculateRouteSuccess() {
        mProgDialog.dismiss();
        if (isReadyNavi) {
            runNaviWayFragment.setSucceful(null);
            showRunNaviFragment();
        }
    }

    private void showRunNaviFragment(){
        FragmentManager fragmentManager = getFragmentManager();
        runNaviWayFragment.setMapView(mapView);
        runNaviWayFragment.setPosiFromTo(fromPoint,toPoint);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_show_fragment,runNaviWayFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void requestStartNavi(){
        Intent intent = new Intent(this,RouteNaviActivity.class);
        intent.putExtra("gps", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    public void requestRouteNavi(){
//        isReadyNavi = false;
//        FragmentManager fragmentManager = getFragmentManager();
//        radarNaviFragment.setMapView(mapView);
//        radarNaviFragment.setToPoint(toPoint);
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.replace(R.id.ll_show_fragment,radarNaviFragment);
//        transaction.commit();
//        mapView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (fromPoint!=null && toPoint!=null){
//                    LogUtils.d(TAG,"reCalue navi");
//                    startList.clear();
//                    startList.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
//                    endList.clear();
//                    endList.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
//                    mLocationPro.calueRunWay(startList,wayList,endList);
//                }
//            }
//        },150);

        Intent intent = new Intent(this,RadarNaviActivity.class);
        Bundle bundle = new Bundle();
        bundle.putDouble("lat",toPoint.getLatitude());
        bundle.putDouble("lon",toPoint.getLongitude());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        BugHunter.statisticsEnd(getApplication(),BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void exitFragment() {
        if (mFragments.size()==0)return;
        if (mFragments.size()==1) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(mFragments.remove(mFragments.size() - 1));
            transaction.commit();
        }else {
            mFragments.remove(mFragments.size()-1);
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            Fragment tFragment = mFragments.get(mFragments.size()-1);
            transaction.replace(R.id.ll_show_fragment,tFragment);
            transaction.commit();
        }
    }

    @Override
    public void shouldFinish() {
        finish();
    }

    @Override
    public void startAcitivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(this,cls);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void startFragment(Fragment fragment) {
        mFragments.add(fragment);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.ll_show_fragment,fragment);
        transaction.commit();
    }

    @Override
    public void startFragment(Class<?> cls) {

    }
}
