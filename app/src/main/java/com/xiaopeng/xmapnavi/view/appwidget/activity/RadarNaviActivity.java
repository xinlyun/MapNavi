package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.MapView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.RadarNaviFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/23.
 */

public class RadarNaviActivity extends Activity implements BaseFuncActivityInteface{
    private MapView mapView;
    private RadarNaviFragment radarNaviFragment;
//    private ILocationProvider mProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_posi);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        mapView.onCreate(savedInstanceState);
        radarNaviFragment = new RadarNaviFragment();
        radarNaviFragment.setMapView(mapView);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void initView(){
        mapView = (MapView) findViewById(R.id.tmv_search_show);
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

    }

    @Override
    public void forShowDeleyDialog() {

    }

    @Override
    public void dismissDeleyDialog() {

    }

    @Override
    public void showCollectDialog() {

    }

    @Override
    public int getFragmentNum() {
        return 1;
    }
}
