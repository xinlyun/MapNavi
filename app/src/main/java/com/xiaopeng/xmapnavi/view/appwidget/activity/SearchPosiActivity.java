package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
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
import com.xiaopeng.amaplib.util.ToastUtil;
import com.xiaopeng.amaplib.util.Utils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HistoryPosi;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpLocationListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistoryAndNaviAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.SwipeBackLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/13.
 */
public class SearchPosiActivity extends Activity implements XpLocationListener
        ,View.OnClickListener,XpSearchListner,
        AMap.OnMapClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener
{

    private static final String TAG = "SearchPosiActivity";
    private static final String ACTION_SEARCH = "ACTION_SEARCH";
    private static final String ACTION_MSG = "ACTION_MSG";
    private int[] markers = {com.xiaopeng.amaplib.R.drawable.poi_marker_1,
            com.xiaopeng.amaplib.R.drawable.poi_marker_2,
            com.xiaopeng.amaplib.R.drawable.poi_marker_3,
            com.xiaopeng.amaplib.R.drawable.poi_marker_4,
            com.xiaopeng.amaplib.R.drawable.poi_marker_5,
            com.xiaopeng.amaplib.R.drawable.poi_marker_6,
            com.xiaopeng.amaplib.R.drawable.poi_marker_7,
            com.xiaopeng.amaplib.R.drawable.poi_marker_8,
            com.xiaopeng.amaplib.R.drawable.poi_marker_9,
            com.xiaopeng.amaplib.R.drawable.poi_marker_10
    };

    private String mSearchName;
    private int ACTION ;
    private static final int REQ_HAVE_RESULT = 1;
    private ILocationProvider mLocationPro;
    private PoiResult mPoiResult;
    private HistoryAndNaviAdapter mAdapter;

    private Button titleBtn,mBeginNavi;
    private ListView mHistoryLv;
    private static final int SET_NOWPOSI=1,SET_POSI=2,SET_HOME=3,SET_COMPANY=4;
    private ImageView btn_return;
    private TextView titleTextView;
    private EditText mEtvReq;
    private LinearLayout editBar;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private Bundle bundle;
    private LatLonPoint mPosi = null,mHomePosi = null,mCompanyPosi = null;

    private Intent intent;
    private List<NaviLatLng> startPoint, endPoint, wayPoint;
    //    private AMapNavi mapNavi;
    private SharedPreferences.Editor editor;
    private Dialog mCleanDialog,mCalueDialog;
    private ProgressDialog mProgDialog;
    private boolean causePri = false;
    private ImageView btnListener;
    int style;
    protected int activityCloseEnterAnimation;

    protected int activityCloseExitAnimation;


    private TextureMapView mapview;
    private AMap mAMap;
    private LatLonPoint lp ;// 116.472995,39.993743
    private Marker locationMarker; // 选择的点
    private Marker detailMarker;
    private Marker mlastMarker;
    private myPoiOverlay poiOverlay;// poi图层
    private PoiResult poiResult; // poi返回的结果
    private List<PoiItem> poiItems;// poi数据
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_posi);
//        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
        mLocationPro    = LocationProvider.getInstence(this);

        mapview   = (TextureMapView) findViewById(R.id.tmv_search_show);
        mapview   .onCreate(savedInstanceState);
        mAMap       = mapview.getMap();
        lp          = Utils.getLatLonFromLocation(mLocationPro.getAmapLocation());
        initView();
        initListView(this);
        initMap();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview   .onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        readIntent(getIntent());
        mLocationPro    .addSearchListner(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationPro.removeLocationListener(this);
        mLocationPro    .removeSearchListner(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readIntent(intent);
    }

    private void initView(){

        startPoint = new ArrayList<NaviLatLng>();
        endPoint = new ArrayList<NaviLatLng>();
        wayPoint = new ArrayList<NaviLatLng>();
        mHistoryLv      = (ListView) findViewById(R.id.prepare_listview);
        btn_return      = (ImageView) findViewById(R.id.title_return);
        titleTextView   = (TextView) findViewById(R.id.title_title);
        titleTextView.setText("搜索");
        mEtvReq = (EditText) findViewById(R.id.prepare_edittext);
        titleBtn        = (Button) findViewById(R.id.title_button);
        mBeginNavi      = (Button) findViewById(R.id.pre_beginnavi);
        editBar         = (LinearLayout) findViewById(R.id.prepare_edit_bar);
        btnListener     = (ImageView) findViewById(R.id.prepare_ttslistener);
        mProgDialog = new ProgressDialog(this);
        mProgDialog.setTitle("多样化路径计算");
        mProgDialog.setMessage("正在计算路径......");
        mProgDialog.setCancelable(true);
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
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
                            .fromBitmap(BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.point4)))
                    .position(new LatLng(lp.getLatitude(), lp.getLongitude())));
            locationMarker.showInfoWindow();

        }
        setup();
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lp.getLatitude(), lp.getLongitude()), 14));
    }
    private void setup() {
//        mPoiDetail = (RelativeLayout) findViewById(com.xiaopeng.amaplib.R.id.poi_detail);
//        mPoiDetail.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
////
//
//            }
//        });
//        mPoiName = (TextView) findViewById(com.xiaopeng.amaplib.R.id.poi_name);
//        mPoiAddress = (TextView) findViewById(com.xiaopeng.amaplib.R.id.poi_address);
//        mSearchText = (EditText)findViewById(com.xiaopeng.amaplib.R.id.input_edittext);
    }
    private void readIntent(Intent intent){
        if (intent == null)return;
        ACTION = intent.getIntExtra(ACTION_SEARCH,-1);
        switch ( ACTION ){
            case -1:
                mLocationPro.addLocationListener(this);
                break;

            case REQ_HAVE_RESULT:
                mSearchName = intent.getStringExtra(ACTION_MSG);
                showResult();
                break;

            default:
                break;


        }
    }

    private void showResult(){
        mPoiResult = mLocationPro.getPoiResult();
        if (mPoiResult != null && mPoiResult.getQuery() != null
                && mPoiResult.getPois() != null && mPoiResult.getPois().size() > 0) {// 搜索poi的结果
//            if (poiResult.getQuery().equals(startSearchQuery)) {

            List<PoiItem> poiItems = mPoiResult.getPois();// 取得poiitem数据
            Log.d(TAG,"seach successed ,size:"+mPoiResult.getPois().size());
            mAdapter.clear();

            if (poiItems.size()==0){
                initListView(SearchPosiActivity.this);
            }else {
                mEtvReq.setText(mSearchName);

                mAdapter.setNewOne(poiItems);

                Log.d(TAG,"new Adapter");
                mAdapter.notifyDataSetChanged();
                mHistoryLv.setOnItemClickListener(onItemClickListener);
                mBeginNavi.setTextColor(getResources().getColor(R.color.white));
                mBeginNavi.setBackgroundResource(R.drawable.prepare_seach_btn_true);
                mBeginNavi.setOnClickListener(this);

            }

        }

        mapview.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    onPoiSearched(mLocationPro.getPoiResult(), 1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },1000);

    }

    @Override
    protected void onDestroy() {
        mapview.onDestroy();
        super.onDestroy();

    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            findViewById(R.id.ll_search_layout).setVisibility(View.GONE);
        }
    };

    private void initListView(Context context){
        List<HistoryPosi> historyPosis= new ArrayList<>();
        historyPosis.add(new HistoryPosi("                       清除历史搜索",0f,0f));
        mAdapter = new HistoryAndNaviAdapter(context,R.layout.layout_fix_list_item,historyPosis);
        mAdapter.setLocalPosi(new LatLng(mLocationPro.getAmapLocation().getLatitude(), mLocationPro.getAmapLocation().getLongitude()));
//        ArrayAdapter adapter = new ArrayAdapter(context,R.layout.mysimple_listitem,strings);
        mHistoryLv.setAdapter(mAdapter);
        initHistoryListener();
    }

    private void initHistoryListener(){

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
        return false;
    }
    // 将之前被点击的marker置为原来的状态
    private void resetlastmarker() {
        int index = poiOverlay.getPoiIndex(mlastMarker);
        if (index < 10) {
            mlastMarker.setIcon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(
                            getResources(),
                            markers[index])));
        }else {
            mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.marker_other_highlight)));
        }
        mlastMarker = null;

    }



    private void onPoiSearched(PoiResult result, int rcode) {
        Log.d(TAG,"onPoiSearched");
        if (rcode == 1000)
        {
            Log.d(TAG,"onPoiSearched1");
            if (result != null && result.getQuery() != null)
            {// 搜索poi的结果
                Log.d(TAG,"onPoiSearched2");
//                if (result.getQuery().equals(query)) {// 是否是同一条
                    Log.d(TAG,"onPoiSearched3");
                    poiResult = result;
                    poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    if (poiItems != null && poiItems.size() > 0) {
                        Log.d(TAG,"onPoiSearched4");
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
                        poiOverlay = new myPoiOverlay(mAMap, poiItems);
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();

                        mAMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(BitmapFactory.decodeResource(
                                                getResources(), com.xiaopeng.amaplib.R.drawable.point4)))
                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));

                        mAMap.addCircle(new CircleOptions()
                                .center(new LatLng(lp.getLatitude(),
                                        lp.getLongitude())).radius(5000)
                                .strokeColor(Color.BLUE)
                                .fillColor(Color.argb(50, 1, 1, 1))
                                .strokeWidth(2));

                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
//                        showSuggestCity(suggestionCities);
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
    /**
     * 自定义PoiOverlay
     *
     */

    private class myPoiOverlay {
        private AMap mamap;
        private List<PoiItem> mPois;
        private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
        public myPoiOverlay(AMap amap , List<PoiItem> pois) {
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
                LatLngBounds bounds = getLatLngBounds();
                mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }

        private LatLngBounds getLatLngBounds() {
            LatLngBounds.Builder b = LatLngBounds.builder();
            for (int i = 0; i < mPois.size(); i++) {
                b.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
                        mPois.get(i).getLatLonPoint().getLongitude()));
            }
            return b.build();
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

}
