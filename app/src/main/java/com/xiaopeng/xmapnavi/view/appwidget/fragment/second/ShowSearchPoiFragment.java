package com.xiaopeng.xmapnavi.view.appwidget.fragment.second;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.navi.model.NaviLatLng;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.xiaopeng.amaplib.util.Utils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HistoryPosi;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.IHistoryDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistoryAndNaviAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.SimplePoiAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.SearchCollectFragment;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.ShowPosiFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linzx on 2016/10/15.
 */
public class ShowSearchPoiFragment extends Fragment implements XpLocationListener
        , View.OnClickListener,XpSearchListner
        , AMap.OnMapClickListener, AMap.OnInfoWindowClickListener
        , AMap.InfoWindowAdapter, AMap.OnMarkerClickListener
        , View.OnTouchListener
        , OnClickRightItem {

    private int[] markers = {
            R.drawable.poi_marker_true_1
            , R.drawable.poi_marker_true_2
            , R.drawable.poi_marker_true_3
            , R.drawable.poi_marker_true_4
            , R.drawable.poi_marker_true_5
            , R.drawable.poi_marker_true_6
            , R.drawable.poi_marker_true_7
            , R.drawable.poi_marker_true_8
            , R.drawable.poi_marker_true_9
            , R.drawable.poi_marker_true_10
    };

    private int[] markers0 = {
            R.drawable.poi_marker_false_1
            , R.drawable.poi_marker_false_2
            , R.drawable.poi_marker_false_3
            , R.drawable.poi_marker_false_4
            , R.drawable.poi_marker_false_5
            , R.drawable.poi_marker_false_6
            , R.drawable.poi_marker_false_7
            , R.drawable.poi_marker_false_8
            , R.drawable.poi_marker_false_9
            , R.drawable.poi_marker_false_10
    };

    private static final String TAG = "ShowPosiFragment";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final String ACTION_MSG = "ACTION_MSG";
    private static int  WINDOW_HEIGHT = 1440,
            LAYOUT_REL_HEIGHT = 600,
            TOUCH_HEIGHT = 800,
            DOWN_HEIGHT = 400,
            TITLE_HEIGHT = 100;


    private MapView mAmapView;

    private String mSearchName;
    private int ACTION ;
    private static final int REQ_HAVE_RESULT = 1;
    private ILocationProvider mLocationPro;
    private PoiResult mPoiResult;
    private SimplePoiAdapter mAdapter;

    private AMap mAMap;
    private LatLonPoint lp ;// 116.472995,39.993743
    private Marker locationMarker; // 选择的点
    private Marker detailMarker;
    private Marker mlastMarker;
    private MyPoiOverlay poiOverlay;// poi图层
    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据

    private Button mBtPull;
    private LinearLayout mLlByPull;
    private float touPx,touPy;

    private ListView mHistoryLv;
    //    private TextView titleTextView;
//    private EditText mEtvReq;
    private ProgressDialog mProgDialog;
    private DateHelper dateHelper;
    private LinearLayout mLlExit;
    private TextView mEdtShowSearch,mTxShowResult;
    private BaseFuncActivityInteface mActivity;
    private int requestCode;
    private String searchStr;
    private boolean isFirst = true;

    private static final int WAY_POI_CODE = 2;

    public void setMapView(MapView mapView){
        mAmapView = mapView;
        mAMap = mAmapView.getMap();
        mAMap.clear();
    }

    public void setRequestCode(int code){
        this.requestCode = code;
    }
    public void setSearchStr(String str){
        searchStr = str;

    }
    private View rootView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        mActivity = (BaseFuncActivityInteface) getActivity();
        setMapView(mActivity.getMapView());
        super.onCreate(savedInstanceState);
        mLocationPro    = LocationProvider.getInstence(this.getActivity());
        lp          = Utils.getLatLonFromLocation(mLocationPro.getAmapLocation());
        dateHelper = new DateHelper();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_show_posi_0,container,false);
        initView();

        return rootView;

    }
    private View findViewById(int id){
        if (rootView != null){
            return rootView.findViewById(id);
        }else return null;
    }


    private void initView(){
        mLlExit         = (LinearLayout) findViewById(R.id.ll_exit);
        mEdtShowSearch  = (TextView) findViewById(R.id.edt_show_search);
        mBtPull         = (Button) findViewById(R.id.btn_pull);
        mLlByPull       = (LinearLayout) findViewById(R.id.ll_search_layout);
        mHistoryLv      = (ListView) findViewById(R.id.prepare_listview);
        mTxShowResult   = (TextView) findViewById(R.id.tx_show_result_msg);
//        titleTextView   = (TextView) findViewById(R.id.title_title);
//        titleTextView.setText("搜索");
//        mEtvReq = (EditText) findViewById(R.id.prepare_edittext);
        mProgDialog = new ProgressDialog(this.getActivity());
        mProgDialog.setTitle("多样化路径计算");
        mProgDialog.setMessage("正在计算路径......");
        mProgDialog.setCancelable(true);
        mEdtShowSearch.setText(searchStr);

        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        mBtPull .setOnTouchListener(this);
        mLlExit         .setOnClickListener(this);
        findViewById(R.id.btn_start_navi).setOnClickListener(this);
    }
    /**
     * 初始化AMap对象
     */
    private void initMap() {
        if (mAMap != null) {
            mAMap.setOnMapClickListener(this);
            mAMap.setOnMarkerClickListener(this);
            mAMap.setOnInfoWindowClickListener(this);
            mAMap.setInfoWindowAdapter(this);
//            TextView searchButton = (TextView) findViewById(com.xiaopeng.amaplib.R.id.btn_search);
//            searchButton.setOnClickListener(this);
            locationMarker = mAMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_seewatch_1)))
                    .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
            locationMarker.showInfoWindow();

        }
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lp.getLatitude(), lp.getLongitude()), 14));
    }

    private void initListView(Context context){
        List<HistoryPosi> historyPosis= new ArrayList<>();
        historyPosis.add(new HistoryPosi("                       清除历史搜索",0f,0f));
        mAdapter    = new SimplePoiAdapter(context,R.layout.layout_fix_list_item,historyPosis);
        mAdapter    . setOnClickRightItem(this);
        mAdapter.setLocalPosi(new LatLng(mLocationPro.getAmapLocation().getLatitude(), mLocationPro.getAmapLocation().getLongitude()));
//        ArrayAdapter adapter = new ArrayAdapter(context,R.layout.mysimple_listitem,strings);
        mHistoryLv.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationPro    .addSearchListner(this);
        mLocationPro    .addNaviCalueListner(mNaviCalueListener);
        initListView(this.getActivity());
        initMap();
        showResult();
        String string = mLocationPro.getSearchStr();
        if (string!=null){
            mEdtShowSearch.setText(string);
        }
        mAMap.getUiSettings().setAllGesturesEnabled(false);
        mAMap.getUiSettings().setZoomGesturesEnabled(true);
        mAMap.getUiSettings().setScrollGesturesEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        BugHunter.countTimeEnd(getActivity().getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);


    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationPro.removeLocationListener(this);
        mLocationPro    .removeSearchListner(this);
        mLocationPro.removeNaviCalueListner(mNaviCalueListener);
    }

    private void showResult(){
        mPoiResult = mLocationPro.getPoiResult();
        if (mPoiResult != null && mPoiResult.getQuery() != null
                && mPoiResult.getPois() != null && mPoiResult.getPois().size() > 0) {// 搜索poi的结果
//            if (poiResult.getQuery().equals(startSearchQuery)) {

            List<PoiItem> poiItems = mPoiResult.getPois();// 取得poiitem数据
            LogUtils.d(TAG,"seach successed ,size:"+mPoiResult.getPois().size());
//            String str = new String("共找到'%s'相关%s个结果",searchStr,""+mPoiResult.getPois().size());
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(getResources().getString(R.string.find_poi_str_0));
            stringBuffer.append(searchStr);
            stringBuffer.append(getResources().getString(R.string.find_poi_str_1));
            stringBuffer.append(mPoiResult.getPois().size());
            stringBuffer.append(getResources().getString(R.string.find_poi_str_2));
            mTxShowResult.setText(stringBuffer);
            mAdapter.clear();

            if (poiItems.size()==0){
                initListView(this.getActivity());
            }else {

                mAdapter.setNewOne(poiItems);

                LogUtils.d(TAG,"new Adapter");
                mAdapter.notifyDataSetChanged();
                mHistoryLv.setOnItemClickListener(onItemClickListener);

            }

        }

        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    onPoiSearched(mLocationPro.getPoiResult(), 1000);
//                    runMarkerChange(poiOverlay.getMarker(0), 0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },100);

        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
//                    onPoiSearched(mLocationPro.getPoiResult(), 1000);
                    runMarkerChange(poiOverlay.getMarker(0), 0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },800);

    }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }

    @Override
    public void onLocationChange(AMapNaviLocation location) {

    }

    @Override
    public void searchSucceful() {
        mPoiResult  = mLocationPro.getPoiResult();
        showResult();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_back:
                //down
            case R.id.title_title:
                getFragmentManager().popBackStack();
                break;

            case R.id.ll_exit:
//                if (requestCode!=WAY_POI_CODE) {
//                    mActivity.exitFragment();
//                }else {
                    SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
                    searchCollectFragment.setMapView(mActivity.getMapView());
                    searchCollectFragment.setRequestCode(requestCode);
                    mActivity.startFragmentReplace(searchCollectFragment);
//                }
                break;

            case R.id.btn_start_navi:
//                startCalueNavi();
                saveThePoi();
                break;
        }
    }


    private void saveThePoi(){
        int index = mAdapter.getIndex();
        if (index!=-1) {
            PoiItem item = poiItems.get(index);
            if (requestCode!=WAY_POI_CODE) {
                if (requestCode != -1) {
                    dateHelper.saveWhereIten(requestCode, item.toString(), item.getCityName(), item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                } else {
                    dateHelper.saveCollect(item.toString(), item.getCityName(), item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                }
                mActivity.exitFragment();
            }else {
                mLocationPro.tryAddWayPoiCalue(new NaviLatLng(item.getLatLonPoint().getLatitude(),item.getLatLonPoint().getLongitude()));
                mActivity.showDialogwithOther();
            }
//            dateHelper.savePoiItem(item);
//            Marker marker = poiOverlay.getMarker(posi);
//            LatLng latLng = marker.getPosition();

        }
    }




    //-----MapListner--//
    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        whetherToShowDetailInfo(false);
        if (mlastMarker != null) {
            resetlastmarker();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int index = poiOverlay.getPoiIndex(marker);
        runMarkerChange(marker,index);
        mHistoryLv.setSelection(index);
        return true;
    }

    private void runMarkerChange(Marker marker,int index){
        if (marker.getObject() != null) {
            whetherToShowDetailInfo(true);
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    // 将之前被点击的marker置为原来的状态
                    resetlastmarker();
                    mlastMarker = marker;
                }
                mAdapter.setIndex(index);

                detailMarker = marker;
                detailMarker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(
                                getResources(),
                                markers0[index])));
                detailMarker.setZIndex(2);

//                setPoiItemDisplayContent(mCurrentPoi);
                if (isFirst) {
//                    CameraUpdate cameraUpdate = CameraUpdateFactory.changeLatLng(new LatLng(mCurrentPoi.getLatLonPoint().getLatitude(), mCurrentPoi.getLatLonPoint().getLongitude()));
//                    mAMap.animateCamera(cameraUpdate);
                    isFirst = false;
                }else {
                    moveToMarker(detailMarker);
//                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
//                            new LatLng(mCurrentPoi.getLatLonPoint().getLatitude(), mCurrentPoi.getLatLonPoint().getLongitude())
////                    新的中心点坐标
//                            ,17, //新的缩放级别
//                            0, //俯仰角0°~45°（垂直与地图时为0）
//                            0  ////偏航角 0~360° (正北方为0)
//                    ));
//                    mAMap.animateCamera(update);
                }

            } catch (Exception e) {
                // TODO: handle exception
            }
        }else {
            whetherToShowDetailInfo(false);
            resetlastmarker();
        }
    }
    private void moveToMarker(Marker marker){
        LogUtils.d(TAG,"moveToMarker start:"+System.currentTimeMillis());
        Projection projection = mAMap.getProjection();
        Point point = projection.toScreenLocation(marker.getPosition());
        LogUtils.d(TAG,"moveToMarker:\nx:"+point.x+"\ny:"+point.y);
        float poiX = point.x;
        float poiY = point.y + 280;
        MarkerOptions options = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.btn_normal))
                .anchor(0.5f,0.5f);
        Marker marker1 = mAMap.addMarker(options);
        marker1.setPositionByPixels((int)poiX, (int)poiY);
        LatLng latLng = marker1.getPosition().clone();
        LogUtils.d(TAG,"moveToMarker:\nlat:"+latLng.latitude+"\nlon:"+latLng.longitude);
        CameraUpdate update = CameraUpdateFactory.changeLatLng(latLng);
        mAMap.moveCamera(update);
        marker1.remove();
        LogUtils.d(TAG,"moveToMarker: end"+System.currentTimeMillis());


    }


    // 将之前被点击的marker置为原来的状态
    private void resetlastmarker() {
        int index = poiOverlay.getPoiIndex(mlastMarker);
        try {
            if (index < 10) {
                mlastMarker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(
                                getActivity().getResources(),
                                markers[index])));
                mlastMarker.setZIndex(1);
            } else {
                mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getActivity().getResources(), com.xiaopeng.amaplib.R.drawable.marker_other_highlight)));
            }
            mlastMarker = null;
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void onPoiSearched(PoiResult result, int rcode) {
        LogUtils.d(TAG,"onPoiSearched");
        if (rcode == 1000)
        {
            LogUtils.d(TAG,"onPoiSearched1");
            if (result != null && result.getQuery() != null)
            {// 搜索poi的结果
                LogUtils.d(TAG,"onPoiSearched2");
//                if (result.getQuery().equals(query)) {// 是否是同一条
                LogUtils.d(TAG,"onPoiSearched3");
                poiResult = result;
                poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                List<SuggestionCity> suggestionCities = poiResult
                        .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                if (poiItems != null && poiItems.size() > 0) {
                    LogUtils.d(TAG,"onPoiSearched4");
                    //清除POI信息显示
                    whetherToShowDetailInfo(false);
                    //并还原点击marker样式
                    if (mlastMarker != null) {
                        resetlastmarker();
                    }
                    //清理之前搜索结果的marker
                    if (poiOverlay !=null) {
                        poiOverlay.removeFromMap();
                    }
                    mAMap.clear();
                    poiOverlay = new MyPoiOverlay(mAMap, poiItems);
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();

                    mAMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(BitmapFactory.decodeResource(
                                            getResources(), R.drawable.icon_seewatch_1)))
                            .position(new LatLng(lp.getLatitude(), lp.getLongitude())));

//                    mAMap.addCircle(new CircleOptions()
//                            .center(new LatLng(lp.getLatitude(),
//                                    lp.getLongitude())).radius(5000)
//                            .strokeColor(Color.BLUE)
//                            .fillColor(Color.argb(50, 1, 1, 1))
//                            .strokeWidth(2));


                } else if (suggestionCities != null
                        && suggestionCities.size() > 0) {
                } else {
//                    }
                }
            } else {
            }
        }
    }


    //-----MapListner--//

    private void whetherToShowDetailInfo(boolean isToShow) {
//        if (isToShow) {
//            mPoiDetail.setVisibility(View.VISIBLE);
//
//        } else {
//            mPoiDetail.setVisibility(View.GONE);
//
//        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                touPx = motionEvent.getRawX();
                touPy = motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                moveLinearLayout(motionEvent);
                touPx = motionEvent.getRawX();
                touPy = motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                resetLinearLayout();
                break;

            default:
                break;
        }


        return true;
    }
    private void moveLinearLayout(MotionEvent motionEvent){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlByPull.getLayoutParams();
        int height = layoutParams.height ;
        float distance = motionEvent.getRawY() - touPy;
        int newHeight  = (int) (height - distance);

        layoutParams.height = newHeight;
        mLlByPull.setLayoutParams(layoutParams);

    }
    private void resetLinearLayout(){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlByPull.getLayoutParams();
        int height = layoutParams.height ;
        if (height > TOUCH_HEIGHT){
            resetLayoutFinish();
        }else if (height<=TOUCH_HEIGHT && height > DOWN_HEIGHT){
            new AnimalHelp(height,LAYOUT_REL_HEIGHT).start();
        }else {
            new AnimalHelp(height,TITLE_HEIGHT).start();
        }

    }
    private void setLinLayoutHeight(int height){
        if (mLlByPull == null)return;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlByPull.getLayoutParams();
        layoutParams.height = height;
        mLlByPull.setLayoutParams(layoutParams);

    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    int height = msg.arg1;
                    setLinLayoutHeight(height);
                    break;

                case 1:

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onClickRightItem(int posi) {
        LogUtils.d(TAG,"onClickRightItem posi:"+posi);
        dateHelper.savePoiItem(poiItems.get(posi));
        Marker marker = poiOverlay.getMarker(posi);
        LatLng latLng = marker.getPosition();
    }
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            LogUtils.d(TAG,"onItemClick posi:"+poiItems);
            if (poiOverlay!=null) {
                ShowSearchPoiFragment.this.runMarkerChange(poiOverlay.getMarker(i), i);
            }
        }
    };

    class AnimalHelp extends Thread{
        int mOldHeight,mPosiHeight;
        AnimalHelp(int oldHeight,int posiHeight){
            mOldHeight = oldHeight;
            mPosiHeight = posiHeight;
        }
        @Override
        public void run() {
            super.run();
            float dif = mPosiHeight - mOldHeight;
            float disP = 10f * (dif/Math.abs(dif));
            while (Math.abs(mPosiHeight - mOldHeight) >10){
                mOldHeight = (int) (mOldHeight + disP);
                Message message = handler.obtainMessage();
                message.what = 0;
                message.arg1 = mOldHeight;
                handler.sendMessage(message);
                try {
                    sleep(8);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message message = handler.obtainMessage();
            message.what = 1;
            message.arg1 = mPosiHeight;

        }
    }

    private void resetLayoutFinish(){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLlByPull.getLayoutParams();
        int height = layoutParams.height ;
        if (height > TOUCH_HEIGHT){
            layoutParams.height = WINDOW_HEIGHT;
        }else if (height<=TOUCH_HEIGHT && height > DOWN_HEIGHT){
            layoutParams.height = LAYOUT_REL_HEIGHT;
        }else {
            layoutParams.height = TITLE_HEIGHT;
        }
        mLlByPull.setLayoutParams(layoutParams);
    }

    /**
     * 自定义PoiOverlay
     *
     */
    private class MyPoiOverlay {
        private AMap mamap;
        private List<PoiItem> mPois;
        private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
        public MyPoiOverlay(AMap amap , List<PoiItem> pois) {
            mamap = amap;
            mPois = pois;
        }

        /**
         * 添加Marker到地图中。
         * @since V2.1.0
         */
        public void addToMap() {
            for (int i = 0; i < mPois.size(); i++) {
                Marker marker = mamap.addMarker(getMarkerOptions(i));
                PoiItem item = mPois.get(i);
                marker.setObject(item);
                mPoiMarks.add(marker);
            }
        }

        /**
         *
         */
        public Marker getMarker(int posi){
            if (mPoiMarks.size()>posi) {
                return mPoiMarks.get(posi);
            }else return null;
        }


        /**
         * 去掉PoiOverlay上所有的Marker。
         *
         * @since V2.1.0
         */
        public void removeFromMap() {
            for (Marker mark : mPoiMarks) {
                mark.remove();
            }
        }

        /**
         * 移动镜头到当前的视角。
         * @since V2.1.0
         */
        public void zoomToSpan() {
            if (mPois != null && mPois.size() > 0) {

                if (mamap == null)
                    return;
                if (mPois.size()>1) {
                    LatLngBounds bounds = getLatLngBounds();
                    mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                }else {
                    PoiItem poiItem = mPois.get(0);
                    CameraUpdate update = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                            new LatLng(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude())
//                    新的中心点坐标
                            ,15, //新的缩放级别
                            0, //俯仰角0°~45°（垂直与地图时为0）
                            0  ////偏航角 0~360° (正北方为0)
                    ));
                    mamap.moveCamera(update);
                }
            }
        }

        private LatLngBounds getLatLngBounds() {
            LatLngBounds.Builder b = LatLngBounds.builder();
            for (int i = 0; i < mPois.size(); i++) {
                b.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
                        mPois.get(i).getLatLonPoint().getLongitude()));
            }
            LatLngBounds bounds = b.build();
            LatLng latLngNot = bounds.northeast;
            LatLng latLngSory = bounds.southwest;
            double dis = latLngNot.latitude - latLngSory.latitude;

            LogUtils.d(TAG,"getLatLngBounds:\nnortheast:"+latLngNot+"\nsouthwest:"+latLngSory+"\ndis:"+dis);

            LatLng newLatlng = new LatLng(latLngSory.latitude - dis,latLngSory.longitude);
            LatLngBounds.Builder c = LatLngBounds.builder();
            for (int i = 0; i < mPois.size(); i++) {
                c.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
                        mPois.get(i).getLatLonPoint().getLongitude()));
            }
            c.include(newLatlng);
            return c.build();
        }

        private MarkerOptions getMarkerOptions(int index) {
            return new MarkerOptions()
                    .position(
                            new LatLng(mPois.get(index).getLatLonPoint()
                                    .getLatitude(), mPois.get(index)
                                    .getLatLonPoint().getLongitude()))
                    .title(getTitle(index)).snippet(getSnippet(index))
                    .icon(getBitmapDescriptor(index));
        }

        protected String getTitle(int index) {
            return mPois.get(index).getTitle();
        }

        protected String getSnippet(int index) {
            return mPois.get(index).getSnippet();
        }

        /**
         * 从marker中得到poi在list的位置。
         *
         * @param marker 一个标记的对象。
         * @return 返回该marker对应的poi在list的位置。
         * @since V2.1.0
         */
        public int getPoiIndex(Marker marker) {
            for (int i = 0; i < mPoiMarks.size(); i++) {
                if (mPoiMarks.get(i).equals(marker)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 返回第index的poi的信息。
         * @param index 第几个poi。
         * @return poi的信息。poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
         * @since V2.1.0
         */
        public PoiItem getPoiItem(int index) {
            if (index < 0 || index >= mPois.size()) {
                return null;
            }
            return mPois.get(index);
        }

        protected BitmapDescriptor getBitmapDescriptor(int arg0) {
            if (arg0 < 10) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), markers[arg0]));
                return icon;
            }else {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
                        BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.marker_other_highlight));
                return icon;
            }
        }
    }


    private XpNaviCalueListener mNaviCalueListener = new XpNaviCalueListener() {
        @Override
        public void onCalculateMultipleRoutesSuccess(int[] ints) {
            mActivity.exitFragment();
        }

        @Override
        public void onCalculateRouteSuccess() {
            mActivity.exitFragment();
        }

        @Override
        public void onCalculateRouteFailure() {

        }
    };




}
