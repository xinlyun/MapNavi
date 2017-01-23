package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.WindowManager;

import com.aispeech.aios.common.bean.PoiBean;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RadarNaviFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/23.
 */

public class RadarNaviActivity extends Activity implements BaseFuncActivityInteface,AMap.OnMapLoadedListener{
    private MapView mapView;
    private RadarNaviFragment radarNaviFragment;
    private AMap aMap;
    private ILocationProvider mProvider;
    private static final String TAG = "RadarNaviActivity";
    private ProgressDialog mProgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_posi);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mProvider   = LocationProvider.getInstence(this);
        mProvider   .addNaviCalueListner(mCalueListener);
        initView();
        mapView.onCreate(savedInstanceState);
        radarNaviFragment = new RadarNaviFragment();
        radarNaviFragment.setMapView(mapView);
        aMap = mapView.getMap();
        aMap.setOnMapLoadedListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mapView.onResume();
        mProvider.setAiosListener(aiosMapListener);
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (aMap.getMapType()!=AMap.MAP_TYPE_NAVI) {
                    aMap.setMapType(AMap.MAP_TYPE_NAVI);
                }
            }
        },3 * 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProvider.removeNaviCalueListner(mCalueListener);
    }

    private XpNaviCalueListener mCalueListener = new XpNaviCalueListener() {
        @Override
        public void onCalculateMultipleRoutesSuccess(int[] ints) {
            mProgDialog.dismiss();
        }

        @Override
        public void onCalculateRouteSuccess() {
            mProgDialog.dismiss();
        }

        @Override
        public void onCalculateRouteFailure() {
            mProgDialog.dismiss();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        mapView.onPause();
    }

    private void initView(){
        mapView = (MapView) findViewById(R.id.tmv_search_show);
        mProgDialog = new ProgressDialog(this,"正在搜索数据");
        mProgDialog.setCancelable(false);
        mProgDialog.getWindow().setDimAmount(0.7f);

        //----init listener ---//


        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        readIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readIntent(intent);
    }

    private void readIntent(Intent intent){
        if (intent == null)return;
        Bundle bundle = intent.getExtras();
        if (bundle == null)return;

        double lat = bundle.getDouble("lat");
        double lon = bundle.getDouble("lon");

        LatLonPoint latLonPoint = new LatLonPoint(lat,lon);
        radarNaviFragment.setToPoint(latLonPoint);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.tmv_search_show,radarNaviFragment);
        transaction.commit();
        setIntent(null);
    }

    @Override
    public void exitFragment() {
        finish();
    }

    @Override
    public void shouldFinish() {
        finish();
    }

    @Override
    public void startAcitivity(Class<?> cls, Bundle bundle) {

    }

    @Override
    public void startFragment(Fragment fragment) {
        //no need
    }

    @Override
    public void startFragmentReplace(Fragment fragment) {
        //no need
    }

    @Override
    public void startFragment(Class<?> cls) {
        //no need
    }

    @Override
    public MapView getMapView() {
        return mapView;
    }

    @Override
    public void requestNaviCalue(LatLonPoint fromPoint, LatLonPoint toPoint) {
//no need
    }

    @Override
    public void showDialogwithOther() {
        mProgDialog.show();
    }

    @Override
    public void forShowDeleyDialog() {
        mProgDialog.show();
    }

    @Override
    public void dismissDeleyDialog() {
        mProgDialog.dismiss();
    }

    @Override
    public void showCollectDialog() {

    }

    @Override
    public int getFragmentNum() {
        return 1;
    }

    @Override
    public void exitFragmentDeley() {

    }

    @Override
    public void onMapLoaded() {
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                aMap.setMapType(AMap.MAP_TYPE_NAVI);
            }
        },2000);
    }


    private XpAiosMapListener aiosMapListener = new XpAiosMapListener() {
        @Override
        public void onStartNavi(@NonNull String s, @NonNull PoiBean poiBean) {
            mProvider.stopNavi();
            NaviLatLng naviLatLng = new NaviLatLng(poiBean.getLatitude(),poiBean.getLongitude());
            List<NaviLatLng> endList = new ArrayList<>();
            endList.add(naviLatLng);
            mProvider.tryCalueRunWay(endList);
            if (mProgDialog!=null){
                mProgDialog.show();
            }
        }

        @Override
        public void onStartNavi(double lat, double lon) {
            mProvider.stopNavi();
            NaviLatLng naviLatLng = new NaviLatLng(lat,lon);
            List<NaviLatLng> endList = new ArrayList<>();
            endList.add(naviLatLng);
            mProvider.tryCalueRunWay(endList);
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
}
