package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.amaplib.util.TTSController;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;


public class RouteNaviActivity extends Activity implements AMapNaviListener
		, AMapNaviViewListener
		, View.OnClickListener{

	AMapNaviView mAMapNaviView;
	private ILocationProvider mLocationPro;
	private EditText mEtv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_basic_navi);
		mLocationPro = LocationProvider.getInstence(this);

		mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
		mAMapNaviView.onCreate(savedInstanceState);
		mAMapNaviView.setAMapNaviViewListener(this);
		findViewById(R.id.btn_change).setOnClickListener(this);
		mEtv = (EditText) findViewById(R.id.etv_input);
		boolean gps=getIntent().getBooleanExtra("gps", false);
		if(gps){
			mLocationPro.startNavi(AMapNavi.EmulatorNaviMode);
//			mLocationPro.startNavi(AMapNavi.GPSNaviMode);
		}else{
			mLocationPro.startNavi(AMapNavi.EmulatorNaviMode);
		}
		

	}

	@Override
	protected void onResume() {
		super.onResume();
		mAMapNaviView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAMapNaviView.onPause();
		//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		//
		//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
		//        mAMapNavi.stopNavi();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAMapNaviView.onDestroy();
		mLocationPro.stopNavi();
	}

	@Override
	public void onInitNaviFailure() {
		Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInitNaviSuccess() {
	}

	@Override
	public void onStartNavi(int type) {

	}

	@Override
	public void onTrafficStatusUpdate() {

	}

	@Override
	public void onLocationChange(AMapNaviLocation location) {

	}

	@Override
	public void onGetNavigationText(int type, String text) {

	}

	@Override
	public void onEndEmulatorNavi() {
	}

	@Override
	public void onArriveDestination() {
	}

	@Override
	public void onArriveDestination(NaviStaticInfo naviStaticInfo) {

	}

	@Override
	public void onCalculateRouteSuccess() {
	}

	@Override
	public void onCalculateRouteFailure(int errorInfo) {
	}

	@Override
	public void onReCalculateRouteForYaw() {

	}

	@Override
	public void onReCalculateRouteForTrafficJam() {

	}

	@Override
	public void onArrivedWayPoint(int wayID) {

	}

	@Override
	public void onGpsOpenStatus(boolean enabled) {
	}

	@Override
	public void onNaviSetting() {
	}

	@Override
	public void onNaviMapMode(int isLock) {

	}

	@Override
	public void onNaviCancel() {
		finish();
	}

	@Override
	public void onNaviTurnClick() {

	}

	@Override
	public void onNextRoadClick() {

	}

	@Override
	public void onScanViewButtonClick() {
	}

	@Deprecated
	@Override
	public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
	}

	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {

	}

	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

	}

	@Override
	public void showCross(AMapNaviCross aMapNaviCross) {
	}

	@Override
	public void hideCross() {
	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] laneInfos, byte[] laneBackgroundInfo, byte[] laneRecommendedInfo) {

	}

	@Override
	public void hideLaneInfo() {

	}

	@Override
	public void onCalculateMultipleRoutesSuccess(int[] ints) {

	}

	@Override
	public void notifyParallelRoad(int i) {

	}

	@Override
	public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

	}

	@Override
	public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

	}

	@Override
	public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

	}

	@Override
	public void onLockMap(boolean isLock) {
	}

	@Override
	public void onNaviViewLoaded() {
	}

	@Override
	public boolean onNaviBackClick() {
		return false;
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.btn_change:
				String str = mEtv.getText().toString();
				if (!"".equals(str)) {
					int i = Integer.valueOf(str);
					mLocationPro.stopNavi();
					mLocationPro.selectRouteId(i);
					mLocationPro.startNavi(AMapNavi.GPSNaviMode);
				}
				break;

			default:
				break;
		}
	}
}
