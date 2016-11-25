package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.NextTurnTipView;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.navi.view.TrafficBarView;
import com.autonavi.tbt.NaviStaticInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.xiaopeng.amaplib.util.TTSController;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.CircleImageView;


public class RouteNaviActivity extends Activity implements  AMapNaviViewListener
		, View.OnClickListener
		, AMap.OnMapLoadedListener
		, XpNaviInfoListener
		, AMap.OnCameraChangeListener{
	private static final String TAG = "RoteNaviActivity";
	private static final int UPDATE_LITTLE_MAP = 0;
	AMapNaviView mAMapNaviView;
	TextureMapView mTmap;
	private AMap mAmap;
	private AMap mNaviAmap;
	private ILocationProvider mLocationPro;
	private Bundle saveBundle;
	NextTurnTipView mNextView;
	private TextView mTxLenght,mTxFrom,mTxTo,mTxTimeNeed,mTxLenghtNeed;
	private CircleImageView mCivShow;
	private RelativeLayout mUnLockView;
	private FrameLayout mTmapLayout;
	private boolean isTraff = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BugHunter.statisticsStart(BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_basic_navi);

		saveBundle = savedInstanceState;
		mLocationPro = LocationProvider.getInstence(this);

		mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);


		initView();
		mAMapNaviView.postDelayed(new Runnable() {
			@Override
			public void run() {


				mTmap		= (TextureMapView) findViewById(R.id.navi_map_view);
				mTmap		.onCreate(saveBundle);
				mAmap = mTmap.getMap();
				mAmap		.setOnMapLoadedListener(RouteNaviActivity.this);
				mTmap.onResume();
			}
		},100);

		mAMapNaviView.onCreate(saveBundle);
		mAMapNaviView.setAMapNaviViewListener(RouteNaviActivity.this);

		mNaviAmap = mAMapNaviView.getMap();
		mNaviAmap.setTrafficEnabled(isTraff);
		boolean gps=getIntent().getBooleanExtra("gps", true);
		if(gps){
			mLocationPro.startNavi(AMapNavi.EmulatorNaviMode);
//			mLocationPro.startNavi(AMapNavi.GPSNaviMode);
		}else{
			mLocationPro.startNavi(AMapNavi.EmulatorNaviMode);

		}


		initMap();

		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mTmap.onSaveInstanceState(outState);
		mAMapNaviView.onSaveInstanceState(outState);
	}

	private void initView(){
		mTxLenght 		= (TextView) findViewById(R.id.tx_length);
		mTxFrom 		= (TextView) findViewById(R.id.tx_from_road);
		mTxTo			= (TextView) findViewById(R.id.tx_to_road);
		mTxTimeNeed		= (TextView) findViewById(R.id.tx_time_need);
		mTxLenghtNeed	= (TextView) findViewById(R.id.tx_length_need);
		mCivShow		= (CircleImageView) findViewById(R.id.civ_show_all);
		mUnLockView		= (RelativeLayout) findViewById(R.id.view_unlock);
		mTmapLayout		= (FrameLayout) findViewById(R.id.mapview_frlayout);

		findViewById(R.id.btn_exit).setOnClickListener(this);
		findViewById(R.id.btn_rader_nave).setOnClickListener(this);
		findViewById(R.id.btn_recalue).setOnClickListener(this);
		findViewById(R.id.btn_traffi_state).setOnClickListener(this);
		findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
		findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
		findViewById(R.id.btn_setting).setOnClickListener(this);
		findViewById(R.id.btn_goto_again).setOnClickListener(this);
	}
	private void initMap(){
		AMapNaviViewOptions viewOptions = mAMapNaviView.getViewOptions();
		viewOptions.setLayoutVisible(false);
		mAMapNaviView.setViewOptions(viewOptions);
		mNextView = (NextTurnTipView) findViewById(R.id.nttv_navi);
		mAMapNaviView.setLazyNextTurnTipView(mNextView);
		mAMapNaviView.setLazyTrafficBarView((TrafficBarView) findViewById(R.id.tbv_show));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAMapNaviView!=null) {
			mAMapNaviView.onResume();
		}
		if (mTmap!=null) {
			mTmap.onResume();
		}
		BugHunter.statisticsEnd(getApplication(),BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAMapNaviView.onPause();
		mTmap.onPause();
		//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		//
		//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
		//        mAMapNavi.stopNavi();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mAMapNaviView.onDestroy();
		mTmap.onDestroy();
		mLocationPro.stopNavi();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationPro.addNaviInfoListner(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationPro.removeNaviInfoListener(this);
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



	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {
		int length = naviinfo.getCurStepRetainDistance();
		if (length>1000){
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length/1000);
			stringBuffer.append(getString(R.string.killmile_more));
			mTxLenght.setText(stringBuffer);
		}else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length);
			stringBuffer.append(getString(R.string.mile_more));
			mTxLenght.setText(stringBuffer);
		}

		StringBuffer strRoadNew = new StringBuffer();
		strRoadNew.append(getString(R.string.from));
		strRoadNew.append(naviinfo.getCurrentRoadName());
		strRoadNew.append(getString(R.string.into));
		mTxFrom.setText(strRoadNew);
		mTxTo.setText(naviinfo.getNextRoadName());


		int allTime = naviinfo.getPathRetainTime();
		int timeMin = allTime/60;
		StringBuffer strTimeNeed= new StringBuffer();
		if (timeMin>60){
			int timeHour = timeMin/60;
			int timeMinN = timeMin%60;
			strTimeNeed.append(timeHour);
			strTimeNeed.append(getString(R.string.hour));
			strTimeNeed.append(timeMinN);
			strTimeNeed.append(getString(R.string.min));
//			strTimeNeed.append("\n");
//			strTimeNeed.append(getString(R.string.need));
		}else if (timeMin>0){
			strTimeNeed.append(timeMin);
			strTimeNeed.append(getString(R.string.min));
//			strTimeNeed.append("\n");
//			strTimeNeed.append(getString(R.string.need));
		}else {
			strTimeNeed.append(allTime);
			strTimeNeed.append(getString(R.string.second));
//			strTimeNeed.append("\n");
//			strTimeNeed.append(getString(R.string.need));
		}
		mTxTimeNeed.setText(strTimeNeed);


		int allPathLenght = naviinfo.getPathRetainDistance();
		StringBuffer strLengthNeed = new StringBuffer();
		if (allPathLenght>1000){
			int killMile = allPathLenght/1000;
			strLengthNeed.append(killMile);
			strLengthNeed.append(getString(R.string.kmile));
//			strLengthNeed.append("\n");
//			strLengthNeed.append(getString(R.string.need));
		}else {
			strLengthNeed.append(allPathLenght);
			strLengthNeed.append(getString(R.string.mile));
//			strLengthNeed.append("\n");
//			strLengthNeed.append(getString(R.string.need));
		}
		mTxLenghtNeed.setText(strLengthNeed);
	}



	@Override
	public void onLockMap(boolean isLock) {
		LogUtils.d(TAG,"onLockMap:"+isLock);
		if (isLock){
			mUnLockView.setVisibility(View.GONE);
			mTmapLayout.setVisibility(View.VISIBLE);
		}else {
			mUnLockView.setVisibility(View.VISIBLE);
			mTmapLayout.setVisibility(View.GONE);
		}
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
			case R.id.btn_exit:
				finish();
				break;

			case R.id.btn_rader_nave:
				//开始轻导航
				beginRaderNavi();
				break;

			case R.id.btn_recalue:
				//重新规划路径
				mLocationPro.reCalue();
				break;

			case R.id.btn_traffi_state:
				//路况
				changeTriffical();
				break;

			case R.id.btn_zoom_plus:
				//放大
				if (mNaviAmap!=null) {
					mNaviAmap.animateCamera(CameraUpdateFactory.zoomIn());
				}
				break;

			case R.id.btn_zoom_jian:
				//缩小
				if (mNaviAmap!=null) {
					mNaviAmap.animateCamera(CameraUpdateFactory.zoomOut());
				}
				break;

			case R.id.btn_setting:
				//TODO
				//打开设置
				break;

			case R.id.btn_goto_again:
				//继续导航
				mAMapNaviView.recoverLockMode();
				break;

			default:
				break;
		}
	}

	@Override
	public void onMapLoaded() {
		mAmap.setMapType(AMap.MAP_TYPE_NIGHT);
		// 初始化 显示我的位置的Marker
		mAmap.setTrafficEnabled(false);
		mAmap.getUiSettings().setZoomControlsEnabled(false);
		mAmap.getUiSettings().setAllGesturesEnabled(false);
		mAmap.showMapText(false);
		LogUtils.d(TAG,"MSG:"+mLocationPro.getNaviPath());
		mAmap.setOnCameraChangeListener(this);
		showPathInListtle();

	}

	private void showPathInListtle(){
		mAMapNaviView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mAMapNaviView.getMap().setMyLocationType(AMap.MAP_TYPE_NAVI);
				AMapNaviPath path ;
				if ((path = mLocationPro.getNaviPath())!=null) {
					RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, RouteNaviActivity.this);
					routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.title_back_00));
					routeOverLay.setTrafficLine(true);
					routeOverLay.addToMap();
					NaviLatLng fromPoint = path.getStartPoint();
					NaviLatLng toPoint = path.getEndPoint();
					if (toPoint!=null && fromPoint!=null) {
						LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
								.include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
						CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 30);
						mAmap.animateCamera(update);

					}
				}

			}
		},500);
	}

	private AMap.OnMapScreenShotListener listener = new AMap.OnMapScreenShotListener() {
		@Override
		public void onMapScreenShot(Bitmap bitmap) {
			if (mCivShow!=null && bitmap!=null){
				mCivShow.setImageBitmap(bitmap);
			}
		}

		@Override
		public void onMapScreenShot(Bitmap bitmap, int status) {
			if (bitmap!=null && mCivShow!=null) {
				mCivShow.setImageBitmap(bitmap);
			}

		}
	};

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
//		mCivShow.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				mAmap.getMapScreenShot(listener);
//				deleyHandler.sendEmptyMessageDelayed(UPDATE_LITTLE_MAP,60 * 1000);
//			}
//		},600);

	}

	private Handler deleyHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
				case UPDATE_LITTLE_MAP:
					showPathInListtle();
					break;

			}
		}
	};

	private void changeTriffical(){
		if (mNaviAmap!=null) {
			isTraff = !isTraff;
			mNaviAmap.setTrafficEnabled(isTraff);
			if (isTraff){
//				mImgBtnLukuang.setImageResource(R.drawable.lukuang_00);
			}else {
//				mImgBtnLukuang.setImageResource(R.drawable.lukuang_01);
			}

		}
	}

	private void beginRaderNavi(){
		NaviLatLng naviLatLng = mLocationPro.getNaviEndPoi();
		Intent intent = new Intent(this,RadarNaviActivity.class);
		Bundle bundle = new Bundle();
		bundle.putDouble("lat",naviLatLng.getLatitude());
		bundle.putDouble("lon",naviLatLng.getLongitude());
		intent.putExtras(bundle);
		startActivity(intent);
		this.finish();
	}
}
