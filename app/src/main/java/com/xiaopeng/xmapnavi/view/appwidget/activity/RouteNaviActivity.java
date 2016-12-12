package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aispeech.aios.common.bean.MapInfo;
import com.aispeech.aios.common.bean.PoiBean;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.NextTurnTipView;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.navi.view.TrafficBarView;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.CircleImageView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MTrafficBarView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MTrafficBarView2;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.RouteNaviSettingDialog;

import java.util.List;

import okhttp3.Route;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


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
	private TextView mTxLenght,mTxTo,mTxTimeNeed,mTxLenghtNeed;//mTxFrom
	private CircleImageView mCivShow;
	private RelativeLayout mUnLockView;
	private FrameLayout mTmapLayout;
	private boolean isTraff = true;
	private ImageView mIvLkIcon;
	private RouteNaviSettingDialog mSettingDialog;
	private TextView mDanwei;
	private ImageView mZoomInIntersectionView;
	private boolean isShowDownLayout = false;
	private RelativeLayout mRlChange;

	private static final int OLD_HEIDHT = 1332;
	private static final int NEW_HEIDHT = 1147;
	private static final int RIGHT_WIDHT = 500;
	private MTrafficBarView mTrafficBarView ;
	private MTrafficBarView2 mTrafficBarView2;

	private TextView mTxBilici,mTxBilici1;
	private RelativeLayout mRLBilici;
	private ImageView mImgNavi;
	private int[] imgId = {
			R.drawable.navi_icon_9,R.drawable.navi_icon_9,R.drawable.navi_icon_2,R.drawable.navi_icon_3
			,R.drawable.navi_icon_4,R.drawable.navi_icon_5,R.drawable.navi_icon_6,R.drawable.navi_icon_7,R.drawable.navi_icon_8
			,R.drawable.navi_icon_9,R.drawable.navi_icon_10,R.drawable.navi_icon_11,R.drawable.navi_icon_12
			,R.drawable.navi_icon_13,R.drawable.navi_icon_14,R.drawable.navi_icon_15,R.drawable.navi_icon_16
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_basic_navi);

		saveBundle = savedInstanceState;
		mLocationPro = LocationProvider.getInstence(this);

		mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
		mAMapNaviView.onCreate(saveBundle);

		initView();
		mAMapNaviView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mTmap		= (TextureMapView) findViewById(R.id.navi_map_view);
				mTmap		.onCreate(saveBundle);
				mTmap		.setOnClickListener(RouteNaviActivity.this);
				mAmap = mTmap.getMap();
				mAmap		.setOnMapLoadedListener(RouteNaviActivity.this);
				mTmap.onResume();

			}
		},500);


		mAMapNaviView.setAMapNaviViewListener(RouteNaviActivity.this);
		mNaviAmap = mAMapNaviView.getMap();
		mNaviAmap.setTrafficEnabled(isTraff);
		mNaviAmap.setOnCameraChangeListener(this);
		boolean gps=getIntent().getBooleanExtra("gps", true);
		if(gps){
//			mLocationPro.startNavi(AMapNavi.EmulatorNaviMode);

			mLocationPro.startNavi(AMapNavi.GPSNaviMode);
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
//		mTxFrom 		= (TextView) findViewById(R.id.tx_from_road);
		mTxTo			= (TextView) findViewById(R.id.tx_to_road);
		mTxTimeNeed		= (TextView) findViewById(R.id.tx_time_need);
		mTxLenghtNeed	= (TextView) findViewById(R.id.tx_length_need);
		mCivShow		= (CircleImageView) findViewById(R.id.civ_show_all);
		mUnLockView		= (RelativeLayout) findViewById(R.id.view_unlock);
		mTmapLayout		= (FrameLayout) findViewById(R.id.mapview_frlayout);
		mIvLkIcon 		= (ImageView) findViewById(R.id.iv_lukuang_icon);
		mDanwei			= (TextView) findViewById(R.id.tx_danwei);
		mZoomInIntersectionView = (ImageView) findViewById(R.id.myZoomInIntersectionView);
		mRlChange		= (RelativeLayout) findViewById(R.id.rv_be_change);
		mTrafficBarView	= (MTrafficBarView) findViewById(R.id.tbv_show);
		mTrafficBarView2 = (MTrafficBarView2) findViewById(R.id.tbv_show_1);
		mTxBilici		= (TextView) findViewById(R.id.tx_bilici);
		mTxBilici1		= (TextView) findViewById(R.id.tx_bilici_1);
		mRLBilici		= (RelativeLayout) findViewById(R.id.ll_bilici);
		mImgNavi		= (ImageView) findViewById(R.id.img_navi);
		findViewById(R.id.btn_exit).setOnClickListener(this);
		findViewById(R.id.btn_rader_nave).setOnClickListener(this);
		findViewById(R.id.btn_recalue).setOnClickListener(this);
		findViewById(R.id.btn_traffi_state).setOnClickListener(this);
		findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
		findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
		findViewById(R.id.btn_setting).setOnClickListener(this);
		findViewById(R.id.btn_goto_again).setOnClickListener(this);
		findViewById(R.id.btn_lukuang).setOnClickListener(this);
		mTrafficBarView.setTrafficListener(trafficBarListener);
		mSettingDialog 	= new RouteNaviSettingDialog(this);
		mSettingDialog	.setOnDialogListener(dialogListener);

	}
	private void initMap(){
		AMapNaviViewOptions viewOptions = mAMapNaviView.getViewOptions();
		viewOptions.setLayoutVisible(false);
		viewOptions.setTrafficBarEnabled(false);
		viewOptions.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_from_poi));
		viewOptions.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_end_poi));
		viewOptions.setWayPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_way_poi));
		mAMapNaviView.setViewOptions(viewOptions);
		mNextView = (NextTurnTipView) findViewById(R.id.nttv_navi);
		mAMapNaviView.setLazyNextTurnTipView(mNextView);
		mAMapNaviView.setLazyTrafficBarView(mTrafficBarView);
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
		BugHunter.countTimeEnd(getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
		mLocationPro.setAiosListener(aiosMapListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAMapNaviView.onPause();
		mTmap.onPause();
		//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		//
		//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
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
		int iconType = naviinfo.getIconType();
		if (iconType>imgId.length-1){
			mImgNavi.setImageResource(R.drawable.navi_icon_9);
		}else {
			mImgNavi.setImageResource(imgId[iconType]);
		}
		int length = naviinfo.getCurStepRetainDistance();
		if (length>1000){
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length/1000);
			mDanwei.setText(R.string.killmile_more);
//			stringBuffer.append(getString());
			mTxLenght.setText(stringBuffer);
		}else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length);
//			stringBuffer.append(getString(R.string.mile_more));
			mDanwei.setText(R.string.mile_more);
			mTxLenght.setText(stringBuffer);
		}

		StringBuffer strRoadNew = new StringBuffer();
		strRoadNew.append(getString(R.string.from));
		strRoadNew.append(naviinfo.getCurrentRoadName());
		strRoadNew.append(getString(R.string.into));
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
		}else if (timeMin>0){
			strTimeNeed.append(timeMin);
			strTimeNeed.append(getString(R.string.min));
		}else {
			strTimeNeed.append(allTime);
			strTimeNeed.append(getString(R.string.second));
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
	public void showCross(AMapNaviCross aMapNaviCross) {
//		Utils.setImageViewMathParent(this,mZoomInIntersectionView,aMapNaviCross.getBitmap());
		mZoomInIntersectionView.setImageBitmap(aMapNaviCross.getBitmap());
		mZoomInIntersectionView.setVisibility(View.VISIBLE);

	}

	@Override
	public void hideCross() {
		mZoomInIntersectionView.setVisibility(View.INVISIBLE);
	}


	@Override
	public void onLockMap(boolean isLock) {
		LogUtils.d(TAG,"onLockMap:"+isLock);
		LogUtils.d(TAG,"onLockMap2:"+mSettingDialog.isShow());
		if (isLock){
			isShowDownLayout = false;
			if (!mSettingDialog.isShow()) {
				mUnLockView.setVisibility(View.GONE);
				mTmapLayout.setVisibility(View.VISIBLE);
				mRLBilici.setVisibility(View.VISIBLE);
				findViewById(R.id.tbv_show_1).setVisibility(View.VISIBLE);
			}
		}else {
			isShowDownLayout = true;
			mUnLockView.setVisibility(View.VISIBLE);
			mTmapLayout.setVisibility(View.GONE);
			mRLBilici.setVisibility(View.GONE);
			findViewById(R.id.tbv_show_1).setVisibility(View.GONE);
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
				mLocationPro.reCalueInNavi();
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
				//打开设置
				mSettingDialog.show();
				break;

			case R.id.btn_goto_again:
				//继续导航
				mAMapNaviView.recoverLockMode();
				break;

			case R.id.btn_lukuang:
				changeTriffical();
				break;

			case R.id.navi_map_view:
				mAMapNaviView.displayOverview();
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
				AMapNaviPath path ;
				if ((path = mLocationPro.getNaviPath())!=null) {
					RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, RouteNaviActivity.this);
					routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_from_poi));
					routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_way_poi));
					routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_end_poi));
					routeOverLay.setTrafficLine(true);
					routeOverLay.addToMap();
					NaviLatLng fromPoint = path.getStartPoint();
					NaviLatLng toPoint = path.getEndPoint();
					if (toPoint!=null && fromPoint!=null) {
						LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
								.include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
						CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 30);
						mAmap.animateCamera(update,cancelableCallback);

					}
				}

			}
		},1500);
	}


	private AMap.CancelableCallback cancelableCallback = new AMap.CancelableCallback() {
		@Override
		public void onFinish() {

		}

		@Override
		public void onCancel() {
			showPathInListtle();
		}
	};

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
		updateScale();
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
				mIvLkIcon.setImageResource(R.drawable.icon_lukuang_01);
//				mImgBtnLukuang.setImageResource(R.drawable.lukuang_00);
			}else {
				mIvLkIcon.setImageResource(R.drawable.icon_lukuang_02);
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

	private AMap.OnMapLoadedListener mLoadedListener = new AMap.OnMapLoadedListener() {
		@Override
		public void onMapLoaded() {
//			mAMapNaviView.recoverLockMode();
		}
	};

	private RouteNaviSettingDialog.OnSettingDailoginShowListener dialogListener = new RouteNaviSettingDialog.OnSettingDailoginShowListener() {
		@Override
		public void onDialogShow() {
			//TODO
			beSmallHeight();
		}
		@Override
		public void onDialogMiss() {
			beHighHeight();
			if (!isShowDownLayout){
				mUnLockView.setVisibility(View.GONE);
				mTmapLayout.setVisibility(View.VISIBLE);
				mRLBilici.setVisibility(View.VISIBLE);
				findViewById(R.id.tbv_show_1).setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onChioce() {
			mLocationPro.reCalueInNavi();
		}
	};

	private void beHighHeight(){
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRlChange.getLayoutParams();
		layoutParams.height = OLD_HEIDHT;
		layoutParams.width = RIGHT_WIDHT;
		mRlChange.setLayoutParams(layoutParams);
	}
	private void beSmallHeight(){
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRlChange.getLayoutParams();
		layoutParams.height = NEW_HEIDHT;
		layoutParams.width = RIGHT_WIDHT;
		mRlChange.setLayoutParams(layoutParams);
	}

	private MTrafficBarView.XpTrafficBarListener trafficBarListener = new MTrafficBarView.XpTrafficBarListener() {
		@Override
		public void trafficUpdate(List<AMapTrafficStatus> list, int i) {
			LogUtils.d(TAG,"trafficUpdate:"+list);
			mTrafficBarView2.update(list,i);
		}
	};


	private void updateScale(){
		float pixel = mNaviAmap.getScalePerPixel();
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
			mTxBilici1.setText(str);
		}else if (mi > 1000){
			mi = mi/1000;
			String str = String.format(getString(R.string.num_kilemile),""+mi);
			mTxBilici.setText(str);
			mTxBilici1.setText(str);
		}
	}

	private XpAiosMapListener aiosMapListener = new XpAiosMapListener() {
		@Override
		public void onStartNavi(@NonNull String s, @NonNull PoiBean poiBean) {

		}

		@Override
		public void onStartNavi(double lat, double lon) {

		}

		@Override
		public void onCancelNavi(@NonNull String s) {
			finish();
		}

		@Override
		public void onOverview(@NonNull String s) {
			mAMapNaviView.displayOverview();
		}

		@Override
		public void onRoutePlanning(@NonNull String s, @NonNull String s1) {

		}

		@Override
		public void onZoom(@NonNull String s, int i) {
			LogUtils.d(TAG,"onZoom:"+i);
			if (i==0){
				mNaviAmap.animateCamera(CameraUpdateFactory.zoomOut());
			}else {
				mNaviAmap.animateCamera(CameraUpdateFactory.zoomIn());
			}
		}

		@Override
		public void onLocate(@NonNull String s) {
			mAMapNaviView.recoverLockMode();
		}
	};


}
