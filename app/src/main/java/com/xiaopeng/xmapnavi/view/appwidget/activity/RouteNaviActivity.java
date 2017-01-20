package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aispeech.aios.common.bean.MapInfo;
import com.aispeech.aios.common.bean.PoiBean;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DriveWayView;
import com.amap.api.navi.view.NextTurnTipView;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.navi.view.TrafficBarView;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.xiaopeng.amaplib.util.AMapUtil;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.XpApplication;
import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ICarControlReple;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpAiosMapListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCarMsgListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviInfoListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpShouldStubListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpStubGroupListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.NaviStubAdapater;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.CircleImageView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MTrafficBarView;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MTrafficBarView2;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.RouteNaviSettingDialog;

import java.util.Date;
import java.util.List;

import okhttp3.Route;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class RouteNaviActivity extends Activity implements  AMapNaviViewListener
		, View.OnClickListener
		, AMap.OnMapLoadedListener
		, XpNaviInfoListener
		, AMap.OnCameraChangeListener{
	private static final String TAG = "RoteNaviActivity";

	private static final int WATCH_3D=0,WATCH_2D=1,WATCH_NORTH=2;
	RelativeLayout mLinView;
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
	private static final int TITLE_NUM = 39;
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

	private TextView mTxSeeWatch;
	private ImageView mImgSeeWatch;
	private int isFollowCar = 1;
	private ProgressDialog mProgDialog,mProgDialog2;
	private boolean isFirstTime = true;
	private boolean isFirstInit = true;
	private boolean isNotUseLock = false;

	private DriveWayView mDrawWayView;
	private ImageView mIvShadow;

	private TextView mTvNanoDis0,mTvNanoDis1,mTvArrayTime;
	private LinearLayout mLlEnage,mLlNoEnage;
	private ListView mLvShowStub;
	private ICarControlReple mCarControlPeple;

	private int lenghtDis,lengthNeed;
	private NaviStubAdapater mStubAdapater;

	private long DEFEA_DELET ;

	Marker mStubMarker;

	private int Navi_Style = AMapNavi.GPSNaviMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_basic_navi);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		saveBundle = savedInstanceState;
		mLocationPro = LocationProvider.getInstence(this);
		mCarControlPeple = mLocationPro.getCarControlReple();
		mLocationPro.addStubGroupListener(mStubGroupListener);
	}

	/**
	 * 初始化地图相关内容
	 * 进入界面时初始化
	 * 重新规划路径后初始化
	 */
	private void initAll(){
		if (isFirstInit) {
			isFirstInit = false;
			mAMapNaviView = new AMapNaviView(this);
			mAMapNaviView.onCreate(null);
			mLinView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mLinView.addView(mAMapNaviView);

				}
			},400);
			mLinView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mIvShadow.setVisibility(View.GONE);
				}
			},2000);
			mAMapNaviView.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mTmap==null) {
						mTmap = (TextureMapView) findViewById(R.id.navi_map_view);
						mTmap.onCreate(saveBundle);
						mTmap.setOnClickListener(RouteNaviActivity.this);
						mAmap = mTmap.getMap();
						mAmap.setOnMapLoadedListener(RouteNaviActivity.this);
						mTmap.onResume();
					}
				}
			}, 500);
			mAMapNaviView.setAMapNaviViewListener(RouteNaviActivity.this);
			mAMapNaviView.setLockZoom(16);
			mAMapNaviView.setLockTilt(TITLE_NUM);
			DEFEA_DELET = mAMapNaviView.getViewOptions().getLockMapDelayed();
			mNaviAmap = mAMapNaviView.getMap();
			mNaviAmap.setTrafficEnabled(isTraff);
			mNaviAmap.setOnCameraChangeListener(this);
			boolean gps = getIntent().getBooleanExtra("gps", true);
			if (gps){
				Navi_Style = AMapNavi.GPSNaviMode;
			}else {
				Navi_Style = AMapNavi.EmulatorNaviMode;
			}
			mAMapNaviView.postDelayed(new Runnable() {
				@Override
				public void run() {

					mLocationPro.startNavi(Navi_Style);
					mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
					changeMapType.sendEmptyMessageDelayed(0,800);
				}
			},750);
			initMap();
		}
	}

	Handler changeMapType = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveBundle = outState;
		if (mTmap!=null) {
			mTmap.onSaveInstanceState(outState);
		}
		if (mAMapNaviView!=null) {
			mAMapNaviView.onSaveInstanceState(outState);
		}

	}

	private void initView(){
		mIvShadow		= (ImageView) findViewById(R.id.navi_see);
		mLinView = (RelativeLayout) findViewById(R.id.navi_view);
		mTxLenght 		= (TextView) findViewById(R.id.tx_length);
		mTxTo			= (TextView) findViewById(R.id.tx_to_road);
		mTxTimeNeed		= (TextView) findViewById(R.id.tx_time_need);
		mTxLenghtNeed	= (TextView) findViewById(R.id.tx_length_need);
		mCivShow		= (CircleImageView) findViewById(R.id.civ_show_all);
		mUnLockView		= (RelativeLayout) findViewById(R.id.view_unlock);
		mTmapLayout		= (FrameLayout) findViewById(R.id.mapview_frlayout);
		mIvLkIcon 		= (ImageView) findViewById(R.id.iv_lukuang_icon);
		mDanwei			= (TextView) findViewById(R.id.tx_danwei);
		mDrawWayView	= (DriveWayView) findViewById(R.id.myNextTurnTipView);
		mDrawWayView	.setAMapNaviView(mAMapNaviView);
		mZoomInIntersectionView = (ImageView) findViewById(R.id.myZoomInIntersectionView);
		mRlChange		= (RelativeLayout) findViewById(R.id.rv_be_change);
		mTrafficBarView2 = (MTrafficBarView2) findViewById(R.id.tbv_show_1);
		mTxBilici		= (TextView) findViewById(R.id.tx_bilici);
		mTxBilici1		= (TextView) findViewById(R.id.tx_bilici_1);
		mRLBilici		= (RelativeLayout) findViewById(R.id.ll_bilici);
		mImgNavi		= (ImageView) findViewById(R.id.img_navi);
		mImgSeeWatch	= (ImageView) findViewById(R.id.iv_see_watch);
		mTxSeeWatch		= (TextView) findViewById(R.id.tx_see_watch);
		mTvNanoDis0		= (TextView) findViewById(R.id.tv_nano_dis_0);
		mTvNanoDis1		= (TextView) findViewById(R.id.tv_nano_dis_1);
		mTvArrayTime	= (TextView) findViewById(R.id.tv_array_time);
		mLvShowStub		= (ListView) findViewById(R.id.lv_show_stub);
		mLlEnage		= (LinearLayout) findViewById(R.id.ll_enage);
		mLlNoEnage		= (LinearLayout) findViewById(R.id.ll_no_enage);


		findViewById(R.id.btn_exit).setOnClickListener(this);
		findViewById(R.id.btn_rader_nave).setOnClickListener(this);
		findViewById(R.id.btn_recalue).setOnClickListener(this);
		findViewById(R.id.btn_traffi_state).setOnClickListener(this);
		findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
		findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
		findViewById(R.id.btn_setting).setOnClickListener(this);
		findViewById(R.id.btn_goto_again).setOnClickListener(this);
		findViewById(R.id.btn_lukuang).setOnClickListener(this);
		findViewById(R.id.btn_see_watch).setOnClickListener(this);
		findViewById(R.id.navi_map_view_0).setOnClickListener(this);
		findViewById(R.id.btn_search_stub).setOnClickListener(this);

		mLvShowStub		.setOnItemClickListener(onItemClickListener);
		mSettingDialog 	= new RouteNaviSettingDialog(this);
		mSettingDialog	.setOnDialogListener(dialogListener);
		mTxSeeWatch.postDelayed(new Runnable() {
			@Override
			public void run() {
				mProgDialog = new ProgressDialog(RouteNaviActivity.this,"正在更新路径");
				mProgDialog.setCancelable(false);
				mProgDialog.getWindow().setDimAmount(0.7f);
				//----init listener ---//
				mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

					}
				});
			}
		},4000);
	}

	private boolean isFrist = true;

	/**
	 * 调整锁定视角
	 * 3D,2D,北上
	 */
	private void changeSeeWatch(){
		isFollowCar = (isFollowCar+1)%3;
		isNotUseLock = true;
		switch (isFollowCar){
			case WATCH_3D:
				mImgSeeWatch.setImageResource(R.drawable.icon_seewatch_0);
				mTxSeeWatch.setText(R.string.watch_3d);
				if (mAMapNaviView!=null){
					mAMapNaviView.setNaviMode(AMapNaviView.CAR_UP_MODE);
					mAMapNaviView.setLockTilt(TITLE_NUM);
					mAMapNaviView.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (mNaviAmap!=null && isFrist) {
//
								mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
							}
						}
					},80);
				}
				break;

			case WATCH_2D:
				mImgSeeWatch.setImageResource(R.drawable.icon_seewatch_1);
				mTxSeeWatch.setText(R.string.watch_follow);
				if (mAMapNaviView!=null){
					mAMapNaviView.setNaviMode(AMapNaviView.CAR_UP_MODE);
					mAMapNaviView.setLockTilt(0);
					mAMapNaviView.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (mNaviAmap!=null && isFrist) {
//
								mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
							}
						}
					},80);
				}

				break;

			case WATCH_NORTH:
				mImgSeeWatch.setImageResource(R.drawable.icon_seewatch_2);
				mTxSeeWatch.setText(R.string.watch_north);
				if (mAMapNaviView!=null){
					mAMapNaviView.setNaviMode(AMapNaviView.NORTH_UP_MODE);
				}
				break;
		}
	}


	/**
	 * 初始化地图设置
	 */
	private void initMap(){
		AMapNaviViewOptions viewOptions = mAMapNaviView.getViewOptions();
		viewOptions.setLayoutVisible(false);
		viewOptions.setTrafficBarEnabled(false);
		viewOptions.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_from_poi));
		viewOptions.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_end_poi));
		viewOptions.setWayPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_way_poi));
		viewOptions.setFourCornersBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.img_navi_north_big));
		viewOptions.setCarBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.img_navi_car_big));
		mAMapNaviView.setViewOptions(viewOptions);
		mNextView = (NextTurnTipView) findViewById(R.id.nttv_navi);
		mAMapNaviView.setLazyNextTurnTipView(mNextView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAMapNaviView!=null) {
			mAMapNaviView.onResume();

			mAMapNaviView.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mNaviAmap!=null) {
						mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
						mNaviAmap.setTrafficEnabled(isTraff);

						if (isFollowCar == 1 && mAMapNaviView!=null){
							mAMapNaviView.setLockTilt(0);
						}
					}
				}
			},1500);
		}
		if (mTmap!=null) {
			mTmap.onResume();
		}


		BugHunter.countTimeEnd(getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
		mLocationPro.setAiosListener(aiosMapListener);
		NaviInfo naviInfo = mLocationPro.getNaviInfo();
		if (naviInfo!=null) {
			onNaviInfoUpdate(naviInfo);
		}

		if (mCarControlPeple!=null){
			mCarControlPeple.addXpCarMsgListener(mXpCarMsgListener);
		}
		mProgDialog2 = new ProgressDialog(RouteNaviActivity.this,"正在搜索数据");
		mProgDialog2.setCancelable(false);
		mProgDialog2.getWindow().setDimAmount(0.7f);
		//----init listener ---//
		mProgDialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {

			}
		});
		mStubAdapater = new NaviStubAdapater(this,R.layout.layout_item_collect_in_setting,mLocationPro.getAmapLocation());
		mStubAdapater	.setOnClickRightItem(mRightItemListener);
		mLvShowStub.setAdapter(mStubAdapater);
	}

	@Override
	protected void onPause() {
		super.onPause();
		try {
			if (mCarControlPeple!=null){
				mCarControlPeple.removeXpCarMsgListener(mXpCarMsgListener);
			}
			mAMapNaviView.onPause();
			mTmap.onPause();

		}catch (Exception e){
			e.printStackTrace();
		}
		//        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
		//
		//        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCarControlPeple.removeXpCarMsgListener(mXpCarMsgListener);
		mLocationPro.removeStubGroupListener(mStubGroupListener);
		mTrafficBarView2.recycleResource();
		mAMapNaviView.onDestroy();
		mTmap.onDestroy();
//		XpApplication.getRefWatcher().watch(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initView();
		initAll();
		mLocationPro.addNaviInfoListner(this);
		mLocationPro.addNaviCalueListner(xpNaviCalueListener);
		mLocationPro.setShouldStubListener(mShouldStubListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mLocationPro.removeNaviInfoListener(this);
		mLocationPro.removeNaviCalueListner(xpNaviCalueListener);
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


	/**
	 * 导航数据更新
	 * @param naviinfo
     */
	@Override
	public void onNaviInfoUpdate(NaviInfo naviinfo) {
		int iconType = naviinfo.getIconType();
		if (iconType>imgId.length-1){
			mImgNavi.setImageResource(R.drawable.navi_icon_9);
		}else {
			mImgNavi.setImageResource(imgId[iconType]);
		}
		int length = naviinfo.getCurStepRetainDistance();
		if (length>=1000){
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length/1000);
			mDanwei.setText(R.string.killmile_more);
			mTxLenght.setText(stringBuffer);
		}else {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(length);
			mDanwei.setText(R.string.mile_more);
			mTxLenght.setText(stringBuffer);
		}

		mTxTo.setText(naviinfo.getNextRoadName());
		int allTime = naviinfo.getPathRetainTime();
		long arrayTime = System.currentTimeMillis() + (allTime * 1000);//m->ms
		int timeMin = allTime/60;
		StringBuffer strTimeNeed= new StringBuffer();
		if (timeMin>60){
			int timeHour = timeMin/60;
			int timeMinN = timeMin%60;
			strTimeNeed.append(timeHour);
			strTimeNeed.append(getString(R.string.hour));
			strTimeNeed.append(timeMinN);
			strTimeNeed.append(getString(R.string.onlymin));
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
		lengthNeed = allPathLenght;
		if (allPathLenght>=1000){
			int killMile = allPathLenght/1000;
			strLengthNeed.append(killMile);
			strLengthNeed.append(getString(R.string.kmile));
		}else {
			strLengthNeed.append(allPathLenght);
			strLengthNeed.append(getString(R.string.mile));
		}
		mTxLenghtNeed.setText(strLengthNeed);

		showEnageMsg();

		Date date = new Date(arrayTime);
		StringBuffer arrTimeBuff = new StringBuffer();
		arrTimeBuff.append(date.getHours());
		arrTimeBuff.append(":");
		arrTimeBuff.append(date.getMinutes());
		mTvArrayTime.setText(arrTimeBuff);
	}

	/**
	 * 显示路口放大图
	 * @param aMapNaviCross
     */
	@Override
	public void showCross(AMapNaviCross aMapNaviCross) {
		mZoomInIntersectionView.setImageBitmap(aMapNaviCross.getBitmap());
		mZoomInIntersectionView.setVisibility(View.VISIBLE);

	}

	/**
	 * 隐藏路口放大图
	 */
	@Override
	public void hideCross() {
		mZoomInIntersectionView.setVisibility(View.GONE);
	}

	/**
	 * 显示路口道路行驶提示
	 * @param var1
	 * @param var2
	 * @param var3
     */
	@Override
	public void showLaneInfo(AMapLaneInfo[] var1, byte[] var2, byte[] var3) {
		LogUtils.d(TAG,"showLaneInfo:var:"+var1);
		if (mDrawWayView!=null){

			mDrawWayView.loadDriveWayBitmap(var2,var3);
		}
	}


	/**
	 * 更新交通路口信息
	 * 更新了交通光柱和右下角全图内容
	 * @param date
	 * @param remainingDistance
     */
	@Override
	public void onNaviTrafficStatusUpdate(List<AMapTrafficStatus> date,int remainingDistance) {
		mTrafficBarView2.update(date,remainingDistance);
		if (isFirstTime){
			isFirstTime = false;
		}else {
			if (mAmap!=null){
				mAmap.clear();
			}
			showPathInListtle();
		}
	}

	/**
	 * 延时让非锁定界面消失
	 */
	Handler delectMissLayout = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isShowDownLayout = false;
			if (!mSettingDialog.isShow()) {
				mUnLockView.setVisibility(View.GONE);
				mTmapLayout.setVisibility(View.VISIBLE);
				mRLBilici.setVisibility(View.VISIBLE);
				findViewById(R.id.tbv_show_1).setVisibility(View.VISIBLE);
			}
		}
	};

	/**
	 * 当地图锁定时调用
	 * 隐藏非锁定界面
	 * 显示锁定界面
	 * @param isLock
     */
	@Override
	public void onLockMap(boolean isLock) {
		LogUtils.d(TAG,"onLockMap:"+isLock);
		LogUtils.d(TAG,"onLockMap2:"+mSettingDialog.isShow());
		if (isLock){
			if (!isNotUseLock) {
				isShowDownLayout = false;
				if (!mSettingDialog.isShow()) {
					mUnLockView.setVisibility(View.GONE);
					mTmapLayout.setVisibility(View.VISIBLE);
					mRLBilici.setVisibility(View.VISIBLE);
					findViewById(R.id.tbv_show_1).setVisibility(View.VISIBLE);
				}
			}else {
				isNotUseLock = false;
				delectMissLayout.removeMessages(0);
				delectMissLayout.sendEmptyMessageDelayed(0,5000);
			}
			reLockTime();
			if (mLvShowStub.getVisibility() == View.VISIBLE){
				mLvShowStub.setVisibility(View.GONE);
				showEnageMsg();
				((TextView)findViewById(R.id.btn_exit)).setText(R.string.exit_navi);
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
				if (mLvShowStub.getVisibility() == View.GONE) {
					mLocationPro.stopNavi();
					finish();
				}else {
					mAMapNaviView.recoverLockMode();
					mLvShowStub.setVisibility(View.GONE);
					showEnageMsg();
					((TextView)findViewById(R.id.btn_exit)).setText(R.string.exit_navi);
				}
				break;

			case R.id.btn_rader_nave:
				//开始轻导航
				mLocationPro.stopNavi();
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
				onLockMap(true);
				delectMissLayout.sendEmptyMessage(0);
				break;

			case R.id.btn_lukuang:
				changeTriffical();
				break;

			case R.id.navi_map_view_0:
				//fill down
			case R.id.navi_map_view:
				LogUtils.d(TAG,"onClick:navi_map_view");
				mAMapNaviView.displayOverview();
				break;

			case R.id.btn_see_watch:
				changeSeeWatch();
				break;

			case R.id.btn_search_stub:
				mProgDialog2.show();
				mLocationPro.getStubGroups();
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

	private void showEnageMsg(){
		int allLenght = lenghtDis - lengthNeed;
		if (allLenght>=60 * 1000){
			mTvNanoDis0.setTextColor(getResources().getColor(R.color.text_green_2));
			if (mLvShowStub.getVisibility() ==View.GONE) {
				mLlEnage.setVisibility(View.VISIBLE);
				mLlNoEnage.setVisibility(View.GONE);
			}
		}else if (allLenght>= 30 * 1000){
			mTvNanoDis0.setTextColor(getResources().getColor(R.color.text_origer));
			if (mLvShowStub.getVisibility() ==View.GONE) {
				mLlEnage.setVisibility(View.VISIBLE);
				mLlNoEnage.setVisibility(View.GONE);
			}
		}else {
			if (mLvShowStub.getVisibility() ==View.GONE) {
				mLlEnage.setVisibility(View.GONE);
				mLlNoEnage.setVisibility(View.VISIBLE);
			}
		}
		boolean isNum = (allLenght > 0);
		String msg;
		if (!isNum){
			allLenght = 0 - allLenght;
			msg = "-"+AMapUtil.getFriendlyLength(allLenght);
		}else {
			msg = AMapUtil.getFriendlyLength(allLenght);
		}
		mTvNanoDis0	.setText(msg);
		mTvNanoDis1.setText(msg);


		if(mLvShowStub.getVisibility() == View.GONE){



		}
	}

	/**
	 * 更新右下角小地图的信息
	 */
	private void showPathInListtle(){
		mAMapNaviView.postDelayed(new Runnable() {
			@Override
			public void run() {
				AMapNaviPath path ;
				LogUtils.d(TAG,"showPathInListtle");
				if ((path = mLocationPro.getNaviPath())!=null) {
					LogUtils.d(TAG,"path :have");
					RouteOverLay routeOverLay = new RouteOverLay(mAmap, path, RouteNaviActivity.this);
					routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_startpoi_little));
					routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_waypoi_little));
					routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(RouteNaviActivity.this.getResources(), R.drawable.icon_endpoi_little));
					routeOverLay.setTrafficLine(true);
					routeOverLay.addToMap();
					routeOverLay.zoomToSpan(30);
				}

			}
		},500);
	}


	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		updateScale();
	}

	/**
	 * 开启/关闭地图路况
	 */
	private void changeTriffical(){
		if (mNaviAmap!=null) {
			isTraff = !isTraff;
			mNaviAmap.setTrafficEnabled(isTraff);
			if (isTraff){
				mIvLkIcon.setImageResource(R.drawable.icon_lukuang_01);
			}else {
				mIvLkIcon.setImageResource(R.drawable.icon_lukuang_02);
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
			if (mLocationPro.reCalueInNavi()) {
				if (mProgDialog != null) {
					mProgDialog.show();
				}
				deleyHandler2.sendEmptyMessageDelayed(0,10 * 1000);
			}else {
				Toast.makeText(RouteNaviActivity.this,"规划失败，请稍后重试",Toast.LENGTH_SHORT).show();
			}
		}
	};

	private Handler deleyHandler2 = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (mProgDialog.isShowing()){
				Toast.makeText(RouteNaviActivity.this,"规划失败，请稍后重试",Toast.LENGTH_SHORT).show();
			}
			if (mProgDialog != null) {
				mProgDialog.dismiss();
			}

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


	/**
	 * 更新比例尺
	 */
	private void updateScale(){
		float pixel = mNaviAmap.getScalePerPixel();
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
		}else if (mi >= 1000){
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

	private AMapNaviView mSaveNaviView;
	XpNaviCalueListener xpNaviCalueListener = new XpNaviCalueListener() {
		@Override
		public void onCalculateMultipleRoutesSuccess(int[] ints) {
			LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess");
			mIvShadow.setVisibility(View.VISIBLE);
			mAMapNaviView.setVisibility(View.GONE);
			mAMapNaviView.onPause();

//			mLinView.removeView(mAMapNaviView);
//			mAMapNaviView.onDestroy();
			mSaveNaviView = mAMapNaviView;
			deleyHandler2.removeMessages(0);
			mLinView.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mProgDialog!=null){
						mProgDialog.dismiss();
					}

					if (mSaveNaviView!=null) {
						mSaveNaviView.onPause();
						mLinView.removeView(mSaveNaviView);
						try{
							mSaveNaviView.onDestroy();
						}catch (Exception e){
							e.printStackTrace();
						}
						mSaveNaviView = null;
					}

				}
			},2000);

			if (mAmap!=null){
				mAmap.clear();
			}
//			try{
//				showPathInListtle();
//			}catch (Exception e){
//				e.printStackTrace();
//			}

			mLocationPro.selectRouteId(ints[0]);


			isFirstInit = true;
			initAll();

			boolean isSuccess = mLocationPro.startNavi(Navi_Style);

			LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess?"+isSuccess);
			if (mAMapNaviView!=null) {
				mAMapNaviView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mNaviAmap != null) {
							mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
						}
					}
				}, 400);
			}
		}

		@Override
		public void onCalculateRouteSuccess() {
			LogUtils.d(TAG,"onCalculateRouteSuccess");
			deleyHandler2.removeMessages(0);
			if (mProgDialog!=null){
				mProgDialog.dismiss();
			}
			if (mAmap!=null){
				mAmap.clear();
			}
//			try{
//				showPathInListtle();
//			}catch (Exception e){
//				e.printStackTrace();
//			}
			mLocationPro.startNavi(Navi_Style);
			if (mAMapNaviView!=null) {
				mAMapNaviView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mNaviAmap != null) {
							mNaviAmap.setMapType(AMap.MAP_TYPE_NAVI);
						}
					}
				}, 400);
			}
		}

		@Override
		public void onCalculateRouteFailure() {
			deleyHandler2.removeMessages(0);
			if (mProgDialog!=null){
				mProgDialog.dismiss();
			}
		}
	};

	private XpCarMsgListener mXpCarMsgListener = new XpCarMsgListener(){

		@Override
		public void carTrayLenght(int length) {
			lenghtDis = length;
			showEnageMsg();
		}
	};

	private XpStubGroupListener mStubGroupListener = new XpStubGroupListener() {
		@Override
		public void OnStubData(List<PowerPoint> powerPoints) {
			if(mProgDialog2!=null && mProgDialog2.isShowing()) {
				mProgDialog2.dismiss();
			}
			if (powerPoints!=null && powerPoints.size()>0){
				mStubAdapater.setDate(powerPoints);
				mLvShowStub.setVisibility(View.VISIBLE);
				mLlEnage.setVisibility(View.GONE);
				mLlNoEnage.setVisibility(View.GONE);
				setListViewHeightBasedOnChildren(mLvShowStub);
				((TextView)findViewById(R.id.btn_exit)).setText(R.string.navi_goon);
			}


		}
	};


	/**
	 * 让显示充电桩的ListView高度自适应，且高度不超过6个item的高度
	 * @param listView 需要自适应的listview
     */
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();

		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		if (listAdapter.getCount()<=6){
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
		}else {
			for (int i = 0; i < 6; i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();

		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));

//		((MarginLayoutParams) params).setMargins(10, 10, 10, 10); // 可删除

		listView.setLayoutParams(params);
	}

	private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			PowerPoint powerPoint = mStubAdapater.setIndex(position);
			LatLng latLng = new LatLng(powerPoint.getLat(),powerPoint.getLon());
			AMapLocation location = mLocationPro.getAmapLocation();
			LatLng mLatlng = new LatLng(location.getLatitude(),location.getLongitude());
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(latLng);
			builder.include(mLatlng);
			LatLngBounds latLngBounds = builder.build();
			LatLng rightTop = latLngBounds.northeast;
			LatLng leftBottom = latLngBounds.southwest;
			double latDis = rightTop.latitude - leftBottom.latitude;
			double lonDis = rightTop.longitude - leftBottom.longitude;
			latDis = latDis/5;
			lonDis = lonDis/2f;
			LatLng latLng1 = new LatLng(leftBottom.latitude - latDis,leftBottom.longitude - lonDis);
			LatLngBounds.Builder builder1 = new LatLngBounds.Builder();
			builder1.include(latLng);
			builder1.include(mLatlng);
			builder1.include(latLng1);
			LatLngBounds latLngBounds1 = builder1.build();
			mAMapNaviView.zoomIn();

			mNaviAmap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds1,150));
			AMapNaviViewOptions mapNaviViewOptions = mAMapNaviView.getViewOptions();
			mapNaviViewOptions.setLockMapDelayed(1000 * 1000);
//			mAMapNaviView.setViewOptions(mapNaviViewOptions);
			if (mStubMarker==null){
				MarkerOptions options = new MarkerOptions();
				options.anchor(0.5f,0.75f);
				options.position(latLng);
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_add_way_poi));
				mStubMarker = mNaviAmap.addMarker(options);
			}else {
				mStubMarker.setPosition(latLng);
			}
		}
	};

	private void reLockTime(){
		if (mAMapNaviView.getViewOptions().getLockMapDelayed()!=DEFEA_DELET){
			AMapNaviViewOptions mapNaviViewOptions = mAMapNaviView.getViewOptions();
			mapNaviViewOptions.setLockMapDelayed(1000 * 1000);
		}
	}

	OnClickRightItem mRightItemListener = new OnClickRightItem() {
		@Override
		public void onClickRightItem(int posi) {
			PowerPoint powerPoint = mStubAdapater.getPoP(posi);
			NaviLatLng naviLatLng = new NaviLatLng(powerPoint.getLat(),powerPoint.getLon());
			mLocationPro.stopNavi();
			mLocationPro.tryAddWayPoiCalue(naviLatLng);
			if (mProgDialog!=null) {
				mProgDialog.show();
			}
		}
	};

	XpShouldStubListener mShouldStubListener = new XpShouldStubListener() {
		@Override
		public void onShowStub() {
			if (mProgDialog2!=null&&mLocationPro!=null) {
				mProgDialog2.show();
				mLocationPro.getStubGroups();
			}
		}
	};



}
