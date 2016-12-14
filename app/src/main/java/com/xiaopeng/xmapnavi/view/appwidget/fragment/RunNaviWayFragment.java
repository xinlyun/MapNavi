package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.AoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.utils.Utils;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RadarNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RouteNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.NaviPathAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.LikeChangeDialog;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.MRouteOverLay;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.NaviChanDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/15.
 */
public class RunNaviWayFragment extends Fragment implements View.OnClickListener

        ,AMap.OnMapTouchListener,RouteSearch.OnRouteSearchListener
        ,XpNaviCalueListener ,NaviChanDialog.OnChioceNaviStyleListner
        ,AMap.OnCameraChangeListener
        , AMap.OnMapLongClickListener
{
    private static final String TAG = "RunNaviWayFragment";
    private MapView mAmapView;
    private AMap mAMap;
    private LatLonPoint fromPoint,toPoint;
    private NaviPathAdapter adapter;
    private List<NaviLatLng> startPoi = new ArrayList<>(),endPoi=new ArrayList<>(),wayPois  = new ArrayList<>();
//    private RouteOverLay mRouteOverLay;
    private boolean isTouch = false;
    //    private List<RouteOverLay> saveOverLay = new ArrayList<>();
    private LikeChangeDialog mSelectLikeDialog;
    private BaseFuncActivityInteface mActivity;
    CameraUpdate watchUpdate;
    private List<MRouteOverLay> mRouteOverLays = new ArrayList<>();
    private HashMap<Integer,MRouteOverLay> mRouteHash = new HashMap<>();
    private boolean isStart = false;
    public void setMapView(MapView mapView){
        mAmapView = mapView;
        mAMap = mAmapView.getMap();

        mAMap.setMapType(AMap.MAP_TYPE_NAVI);
    }

    public void setPosiFromTo(LatLonPoint fromPoint, LatLonPoint toPoint){
        LogUtils.d(TAG,"setPosiFromTo:"+"from:"+fromPoint+"\nto:"+toPoint);
        this.fromPoint = fromPoint;
        this.toPoint = toPoint;
        startPoi.clear();
        startPoi.add(new NaviLatLng(fromPoint.getLatitude(),fromPoint.getLongitude()));
        endPoi.clear();
        endPoi.add(new NaviLatLng(toPoint.getLatitude(),toPoint.getLongitude()));
    }

    private ILocationProvider mLocaionPro;
    private View rootView;

    //    private List<Marker> markers = new ArrayList<>();
    private List<LatLng> markers = new ArrayList<>();
    private List<Integer> numSave = new ArrayList<>();
    private Marker mMarker;


    /**
     * 保存当前算好的路线
     */


    /**
     * 当前用户选中的路线，在下个页面进行导航
     */
    private int routeIndex = 0;
    /**路线的权值，重合路线情况下，权值高的路线会覆盖权值低的路线**/
    private int zindex = 2;
    /**
     * 路线计算成功标志位
     */
    private boolean calculateSuccess = false;


    private GridView mGvShowNaviPaths;
    private TextView mTvShowMsg;
    private int[] ints;
    private LinearLayout mBtnStartNavi;
    MarkerOptions mOptions;
    long timeRe ;
    Bitmap bi;
    //    private List<RouteOverLay> deletLay = new ArrayList<>();
    private AMapNaviPath pathx;
    private NaviChanDialog mNaviChioceDialog;
    private ProgressDialog mProgDialog;
    private boolean isTricall = true;
    private ImageView mIvLKicon;
    private Polyline mPoline0,mPoline1,mPoline2;
    private Marker markerFromPoi,markerEndPoi,markerWayPoi;
    private TextView mTxBilici;
    private GeocodeSearch geocodeSearch,geocodeSearch2;
    private MarkerOptions wayPoiOptions;
    private Marker mWayPoiMarker;
    private TextView mTxMarkTitle,mTxMarkTitle2;
    private View mMarkInfoView,mMarkInfoView2;
    MarkerOptions options2;
    private boolean isFirst = true;

    private HashMap<Integer,RouteOverLay> routeMap = new HashMap<>();
    private List<RouteOverLay> routeOverLays = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        mActivity = (BaseFuncActivityInteface) getActivity();
        super.onCreate(savedInstanceState);
        mLocaionPro = LocationProvider.getInstence(getActivity());
        mOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_way_poi));
        bi = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.title_back_00);
        isFirst = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calue_navi,container,false);
        initView();
        return rootView;
    }

    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    @Override
    public void onStart() {
        super.onStart();
        isStart = true;
        try {
            isFirst = true;
            geocodeSearch = new GeocodeSearch(getActivity());
            geocodeSearch.setOnGeocodeSearchListener(mGeocodeListener);

            geocodeSearch2 = new GeocodeSearch(getActivity());
            geocodeSearch2.setOnGeocodeSearchListener(mGeocodeListener2);

            mAMap.clear();
            mAMap.setOnCameraChangeListener(this);
            MarkerOptions options = new MarkerOptions();
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_from_poi));
            options.anchor(0.5f, 1f);
            options.position(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()));
            markerFromPoi = mAMap.addMarker(options);
            markerFromPoi.setClickable(false);

            MarkerOptions options1 = new MarkerOptions();
            options1.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end_poi));
            options1.anchor(0.5f, 1f);
            options1.position(new LatLng(toPoint.getLatitude(), toPoint.getLongitude()));
            markerEndPoi = mAMap.addMarker(options1);
            markerEndPoi.setClickable(false);

            options2 = new MarkerOptions();
            options2.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_way_poi));
            options2.anchor(0.5f, 1f);


            mAMap.setMapType(AMap.MAP_TYPE_NAVI);
            mAMap.setOnMapLongClickListener(this);
            mAMap.setOnMarkerClickListener(markerClickListener);

            mLocaionPro.addNaviCalueListner(this);
            isTricall = mAMap.isTrafficEnabled();
            if (mIvLKicon != null) {
                if (isTricall) {
                    mIvLKicon.setImageResource(R.drawable.icon_lukuang_01);
                } else {
                    mIvLKicon.setImageResource(R.drawable.icon_lukuang_02);
                }
            }
//        mAMap.setTrafficEnabled(isTricall);
            mAMap.setOnPolylineClickListener(polylineClickListener);

            wayPoiOptions = new MarkerOptions();
            wayPoiOptions.anchor(0.5f, 1f);
            wayPoiOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_add_way_poi));

            mAMap.setInfoWindowAdapter(infoWindowAdapter);
            mAmapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        readNaviMsg();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },100);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BugHunter.countTimeEnd(getActivity().getApplication(),BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
    }

    private void initView(){
        mGvShowNaviPaths    = (GridView) findViewById(R.id.gv_show_an);
        mTvShowMsg          = (TextView) findViewById(R.id.tv_show_msg);
        mBtnStartNavi       = (LinearLayout) findViewById(R.id.btn_start_navi);
        mIvLKicon           = (ImageView) findViewById(R.id.iv_lukuang_icon);
        mTxBilici           = (TextView) findViewById(R.id.tx_bilici);
        //--listener--//
        mBtnStartNavi       .setOnClickListener(this);
        findViewById(R.id.btn_start_route_navi).setOnClickListener(this);
        findViewById(R.id.btn_pianhao).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
        findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
        findViewById(R.id.btn_lukuang).setOnClickListener(this);
        findViewById(R.id.btn_add_way_poi).setOnClickListener(this);
        mBtnStartNavi.postDelayed(new Runnable() {
            @Override
            public void run() {
                mNaviChioceDialog = new NaviChanDialog(RunNaviWayFragment.this.getActivity());
                mNaviChioceDialog.setOnChioceNaviStyleListner(RunNaviWayFragment.this);
            }
        },1000);


        mProgDialog = new ProgressDialog(this.getActivity());
        mProgDialog.setTitle("正在搜索数据");
        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(true);


        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        initMarkInfo();
    }





    @Override
    public void onStop() {
        super.onStop();
//
        isStart = false;
        isFirst = true;
        mLocaionPro.removeNaviCalueListner(this);
//        mAMap.setOnMapTouchListener(null);
//        if (mAMap!=null) {
//            mAMap.getUiSettings().setAllGesturesEnabled(false);
//        }


    }

    public void setSucceful(int[] ints){
        this.ints = ints;

    }
    private HashMap<Integer, AMapNaviPath> paths;
    private void readNaviMsg(){

        ints = mLocaionPro.getPathsInts();
//        routeOverlays.clear();

        LogUtils.d(TAG,"watchAll:readNaviMsg");


        paths = mLocaionPro.getNaviPaths();
        pathx = mLocaionPro.getNaviPath();

        watchAll();

        List<NaviLatLng> latLngs = paths.get(ints[0]).getWayPoint();
        if (latLngs!=null && latLngs.size()>0){
            findViewById(R.id.img_div).setVisibility(View.GONE);
            findViewById(R.id.btn_start_route_navi).setVisibility(View.GONE);


            NaviLatLng naviLatLng = latLngs.get(0);

            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(naviLatLng.getLatitude(),naviLatLng.getLongitude()), 200, GeocodeSearch.AMAP);
            geocodeSearch2.getFromLocationAsyn(query);
            mTxMarkTitle2.setText(R.string.loading_hard);

            if (markerWayPoi == null){
                options2.position(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
                markerWayPoi = mAMap.addMarker(options2);
                markerWayPoi.setTitle("title1");
                markerWayPoi.setSnippet("snippet1");

            }else {
                markerWayPoi.setPosition(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
                markerWayPoi.setTitle("title1");
                markerWayPoi.setSnippet("snippet1");

            }
        }else {
            findViewById(R.id.img_div).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_start_route_navi).setVisibility(View.VISIBLE);
        }



        if (ints!=null && paths!=null && paths.size() == ints.length) {
            adapter = new NaviPathAdapter(getActivity(), R.layout.layout_gradview_item);
            adapter.setDate(paths, ints);
            mGvShowNaviPaths.setNumColumns(ints.length);
            mGvShowNaviPaths.setAdapter(adapter);

            mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
        }else if (pathx!=null){
            adapter = new NaviPathAdapter(getActivity(), R.layout.layout_gradview_item);
            HashMap<Integer,AMapNaviPath> pathHashMap = new HashMap<Integer, AMapNaviPath>();
            pathHashMap.put(0,pathx);
            int[] ints1 = new int[]{0};
            adapter.setDate(pathHashMap, ints1);
            mGvShowNaviPaths.setAdapter(adapter);
            mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
        }

        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int traListhNum = getTrafficLightNum(paths.get(ints[0]));
                    int cost = paths.get(ints[0]).getTollCost();
                    String msgShow = "花费约" + cost + "元 ,红绿灯" + traListhNum + "个";
                    mTvShowMsg.setText(msgShow);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },800);

    }








    private void drawAgain(){
        for (RouteOverLay overLay:routeOverLays){
            overLay.removeFromMap();
        }
        for (MRouteOverLay mRouteOverLay:mRouteOverLays){
            mRouteOverLay.removeFromMap();
        }
        routeOverLays.clear();
        routeMap.clear();
        mRouteOverLays.clear();
        mRouteHash.clear();


        paths = mLocaionPro.getNaviPaths();
        ints = mLocaionPro.getPathsInts();
//        LogUtils.d(TAG,"watchAll:drawAgain");
        watchAll();

        List<NaviLatLng> latLngs = paths.get(ints[0]).getWayPoint();
        if (latLngs!=null && latLngs.size()>0){
            NaviLatLng naviLatLng = latLngs.get(0);

            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(naviLatLng.getLatitude(),naviLatLng.getLongitude()), 200, GeocodeSearch.AMAP);
            geocodeSearch2.getFromLocationAsyn(query);
            mTxMarkTitle2.setText(R.string.loading_hard);


            if (markerWayPoi == null){
                options2.position(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
                markerWayPoi = mAMap.addMarker(options2);
                markerWayPoi.setTitle("title1");
                markerWayPoi.setSnippet("snippet1");
            }else {
                markerWayPoi.setPosition(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
                markerWayPoi.setTitle("title1");
                markerWayPoi.setSnippet("snippet1");
            }
            findViewById(R.id.img_div).setVisibility(View.GONE);
            findViewById(R.id.btn_start_route_navi).setVisibility(View.GONE);
        }else {
            findViewById(R.id.img_div).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_start_route_navi).setVisibility(View.VISIBLE);
        }
        drawPathLine();
        if (paths != null  && ints != null){
//            if (!isTouch) {
            int routeID = ints[0];
            routeIndex = 0;
//            AMapNaviPath path = paths.get(routeID);
//            drawRoutes(routeID,path);

            mLocaionPro.selectRouteId(routeID);

            mAmapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        adapter = new NaviPathAdapter(getActivity(), R.layout.layout_gradview_item);
                        adapter.setDate(paths, ints);
                        mGvShowNaviPaths.setAdapter(adapter);
                        mGvShowNaviPaths.setNumColumns(ints.length);
                        mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);

                        for (int i = 0; i < ints.length; i++) {
                            AMapNaviPath path = paths.get(ints[i]);
                            if (path != null) {
                                MRouteOverLay mRouteOverLay = drawMRoutes(path);
                                mRouteOverLays.add(mRouteOverLay);
                                mRouteHash.put(ints[i],mRouteOverLay);
                            }
                        }

                        for (int i = 0; i < ints.length; i++) {
                            AMapNaviPath path = paths.get(ints[i]);
                            if (path != null) {
                                RouteOverLay routeOverLay = drawRoutes(ints[i], path);
                                routeOverLays.add(routeOverLay);
                                routeMap.put(ints[i],routeOverLay);
                            }
                        }
                        changeRoute();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },400);

//            mAmapView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);
//                        LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
//                                .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();
//
//                        mAMap.setOnMapTouchListener(RunNaviWayFragment.this);
//
//                        changeRoute();
//
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            },800);


//            }else {
//                AMapNaviPath path = paths.get(ints[0]);
//                if (path != null) {
//                    mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
//                    mRouteOverLay = new RouteOverLay(mAMap, path, getActivity());
//                    mRouteOverLay.setWayPointBitmap(bi);
//                    mRouteOverLay.setTrafficLine(true);
//                    mRouteOverLay.addToMap();
//                }
//            }
        } else {
            AMapNaviPath path = mLocaionPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
            RouteOverLay routeOverLay = drawRoutes(-1, path);
            ints = new int[]{-1};
            routeMap.put(-1,routeOverLay);
            routeOverLays.add(routeOverLay);
        }


    }



    public void changeRoute() {
        try {
            if (!calculateSuccess) {
                Toast.makeText(getActivity(), "请先算路", Toast.LENGTH_SHORT).show();
                return;
            }
            /**
             * 计算出来的路径只有一条
             */
            int routeID = ints[routeIndex];
            AMapNaviPath path = paths.get(routeID);

//        drawRoutes(routeID,path);
            for (int i = 0; i < ints.length; i++) {
                int id = ints[i];
                RouteOverLay routeOverLay = routeMap.get(id);
                if (id == routeID) {
                    routeOverLay.setTransparency(1.0f);
                } else {
                    routeOverLay.setTransparency(0.0f);
                }
            }
            //必须告诉AMapNavi 你最后选择的哪条路
            mLocaionPro.selectRouteId(routeID);


            int traListhNum = getTrafficLightNum(path);
            int cost = path.getTollCost();
            String msgShow = "花费约" + cost + "元 ,红绿灯" + traListhNum + "个";
            mTvShowMsg.setText(msgShow);
            LogUtils.d(TAG, "watchAll:changeRoute");
            watchAll();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getTrafficLightNum(AMapNaviPath path){
        List<AMapNaviStep> steps =path.getSteps();
        int count = 0;
        for (AMapNaviStep step:steps){
            count = count + step.getTrafficLightNumber();
        }
        return count;
    }

    private RouteOverLay drawRoutes(int routeId, AMapNaviPath path) {
        LogUtils.d(TAG,"drawRoutes id:"+routeId);
        calculateSuccess = true;
//        if (mRouteOverLay!=null){
//            mRouteOverLay.removeFromMap();
//            mRouteOverLay = null;
//        }
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, getActivity());
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));
        routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));

        markerFromPoi.setPosition(new LatLng(path.getStartPoint().getLatitude(),path.getStartPoint().getLongitude()));
        markerEndPoi.setPosition(new LatLng(path.getEndPoint().getLatitude(),path.getEndPoint().getLongitude()));

        routeOverLay.setTrafficLine(true);
        routeOverLay.setZindex(zindex);
        routeOverLay.addToMap();
        routeOverLay.setTransparency(0.0f);
        return routeOverLay;
//        mRouteOverLay = routeOverLay;

    }

    private MRouteOverLay drawMRoutes(AMapNaviPath path) {
        calculateSuccess = true;
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        MRouteOverLay routeOverLay = new MRouteOverLay(mAMap, path, getActivity());
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));
        routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.nothing_poi));

        markerFromPoi.setPosition(new LatLng(path.getStartPoint().getLatitude(),path.getStartPoint().getLongitude()));
        markerEndPoi.setPosition(new LatLng(path.getEndPoint().getLatitude(),path.getEndPoint().getLongitude()));

        routeOverLay.setTrafficLine(true);
        routeOverLay.setZindex(1);
        routeOverLay.addToMap();
        routeOverLay.setTransparency(1.0f);
        return routeOverLay;
//        mRouteOverLay = routeOverLay;

    }


    private AdapterView.OnItemClickListener mClickPathItemListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            RunNaviWayFragment.this.routeIndex = i;
            RunNaviWayFragment.this.adapter.setIndex(i);
            changeRoute();
        }
    };


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_navi:
                isFirst = true;
                watchUpdate = null;
                Intent intent = new Intent(getActivity(),RouteNaviActivity.class);
                intent.putExtra("gps", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case R.id.btn_start_route_navi:
                mAMap.clear();
                isFirst = true;
                watchUpdate = null;
                RadarNaviFragment radarNaviFragment = new RadarNaviFragment();
                radarNaviFragment.setMapView(mActivity.getMapView());
                radarNaviFragment.setToPoint(toPoint);
                mActivity.startFragment(radarNaviFragment);
                break;

            case R.id.btn_pianhao:
                if (mSelectLikeDialog==null){
                    mSelectLikeDialog = new LikeChangeDialog(getActivity());
                    mSelectLikeDialog.setOnSelectLikeStyle(likeStyle);
                }
                mSelectLikeDialog.show();
                break;

            case  R.id.btn_exit:
                mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
                mAMap.clear();
                mActivity.exitFragment();

                break;

            case R.id.btn_zoom_plus:
                if(mAMap!=null){
                    mAMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                break;

            case R.id.btn_zoom_jian:
                if(mAMap!=null){
                    mAMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
                break;

            case R.id.btn_lukuang:
                isTricall = !isTricall;
                if (mAMap!=null){
                    mAMap.setTrafficEnabled(isTricall);

                }
                if (mIvLKicon!=null) {
                    if (isTricall) {
                        mIvLKicon.setImageResource(R.drawable.icon_lukuang_01);
                    }else {
                        mIvLKicon.setImageResource(R.drawable.icon_lukuang_02);
                    }
                }

                break;

            case R.id.btn_begin_add_way:
                addWayPoi();
                break;

            case R.id.btn_add_way_poi:
                SearchCollectFragment searchCollectFragment = new SearchCollectFragment();
                searchCollectFragment.setRequestCode(2);
                searchCollectFragment.setMapView(mAmapView);
                mActivity.startFragment(searchCollectFragment);
                break;

            case R.id.btn_begin_add_way_2:
                if (markerWayPoi !=null){
                    markerWayPoi.remove();
                    markerWayPoi = null;
                }
                mActivity.showDialogwithOther();
                mLocaionPro.reCalue();
                break;

            default:
                break;

        }
    }

    private void watchAll(){
        try {
            LogUtils.d(TAG, "watchAll");
            if (mAMap != null && watchUpdate != null) {

                LogUtils.d(TAG, "watchAll0");
                mAMap.animateCamera(watchUpdate, mCancelableCallBack);
                mAmapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isFirst && isStart) {
                            isFirst = false;
                            LogUtils.d(TAG, "staie first time to show Paths");
                            mActivity.forShowDeleyDialog();
                            if (paths == null) {
                                paths = mLocaionPro.getNaviPaths();
                            }
                            if (ints == null) {
                                ints = mLocaionPro.getPathsInts();
                            }
                            drawPathLine();

                            if (paths != null && paths.size() > 1 && ints != null) {

                                for (int i = 0; i < ints.length; i++) {
                                    AMapNaviPath path = paths.get(ints[i]);
                                    if (path != null) {
                                        MRouteOverLay mRouteOverLay = drawMRoutes(path);
                                        mRouteOverLays.add(mRouteOverLay);
                                        mRouteHash.put(ints[i],mRouteOverLay);
                                    }
                                }

                                for (int i = 0; i < ints.length; i++) {
                                    AMapNaviPath path = paths.get(ints[i]);
                                    if (path != null) {
                                        RouteOverLay routeOverLay = drawRoutes(ints[i], path);
                                        routeOverLays.add(routeOverLay);
                                        routeMap.put(ints[i], routeOverLay);
                                    }
                                }
                                changeRoute();
                            } else {
                                AMapNaviPath path = mLocaionPro.getNaviPath();
                                /**
                                 * 单路径不需要进行路径选择，直接传入－1即可
                                 */
                                drawRoutes(-1, path);
                            }
                            mActivity.dismissDeleyDialog();

                        }
                    }
                }, 1000);

            } else {
                LogUtils.d(TAG, "watchAll1");
                new WatchSee().start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mWayPoiMarker!=null){
            mWayPoiMarker.remove();
            mWayPoiMarker = null;
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        updateScale();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(mWayPoiMarker==null){
            wayPoiOptions.position(latLng);
            mWayPoiMarker = mAMap.addMarker(wayPoiOptions);
            mWayPoiMarker.setTitle("title");
            mWayPoiMarker.setSnippet("snippet");
        }else {
            mWayPoiMarker.setPosition(latLng);
        }
        mWayPoiMarker.showInfoWindow();
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latLng.latitude,latLng.longitude), 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        mTxMarkTitle.setText(R.string.loading_hard);
    }

    class WatchSee extends Thread{
        @Override
        public void run() {
            super.run();
            try {
                LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);
                HashMap<Integer,AMapNaviPath> pathHashMap = mLocaionPro.getNaviPaths();
                int[] ints = mLocaionPro.getPathsInts();

                LatLngBounds.Builder builder = LatLngBounds.builder();
                AMapNaviPath pathx = pathHashMap.get(ints[0]);
                if (pathx.getAllLength() < 100 * 1000) {
                    NaviLatLng startPoi = pathHashMap.get(ints[0]).getStartPoint();
                    NaviLatLng endPoi = pathHashMap.get(ints[0]).getEndPoint();
                    float disF = disMwithLat(startPoi, endPoi);
                    float disK = disMwithLon(startPoi, endPoi);
                    double alllenght = Math.sqrt(disF * disF + disK * disK);
                    double num = disF / alllenght;
                    double scall = Math.toDegrees(Math.atan(disF / disK));
                    LogUtils.d(TAG, "scall:" + scall);
                    for (int i = 0; i < ints.length; i++) {
                        AMapNaviPath path = pathHashMap.get(ints[i]);
                        LatLngBounds bounds = path.getBoundsForPath();
                        builder.include(bounds.northeast);
                        builder.include(bounds.southwest);
                    }
//                    CameraUpdate update1 = CameraUpdateFactory.changeBearing((float) scall);
//                    mAMap.animateCamera(update1,0,null);
                    LatLngBounds latLngBounds = builder.build();
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (100 + num * 235));
                    Message message = handlTheWatch.obtainMessage();
                    message.what = 0;
                    message.obj = update;
                    handlTheWatch.sendMessage(message);
                }else {
                    NaviLatLng startPoi = pathx.getStartPoint();
                    NaviLatLng endPoi = pathx.getEndPoint();
                    float disF = disMwithLat(startPoi, endPoi);
                    float disK = disMwithLon(startPoi, endPoi);
                    double alllenght = Math.sqrt(disF * disF + disK * disK);
                    double num = disF / alllenght;

                    builder.include(new LatLng(startPoi.getLatitude(),startPoi.getLongitude()));
                    builder.include(new LatLng(endPoi.getLatitude(),endPoi.getLongitude()));
                    LatLngBounds latLngBounds = builder.build();
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (100 + num * 235) );
                    Message message = handlTheWatch.obtainMessage();
                    message.what = 0;
                    message.obj = update;
                    handlTheWatch.sendMessage(message);
                }




            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    Handler handlTheWatch = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtils.d(TAG,"handlTheWatch");
            CameraUpdate update = (CameraUpdate) msg.obj;
            watchUpdate = update;
            mAMap.animateCamera(update,mCancelableCallBack);
            mAmapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isFirst&& isStart){
                        isFirst = false;
                        LogUtils.d(TAG,"staie first time to show Paths");
                        mActivity.forShowDeleyDialog();
                        if (paths==null){
                            paths = mLocaionPro.getNaviPaths();
                        }
                        if (ints == null){
                            ints = mLocaionPro.getPathsInts();
                        }

                        drawPathLine();

                        if (paths != null && paths.size() > 1 && ints != null){
                            for (int i = 0; i < ints.length; i++) {
                                AMapNaviPath path = paths.get(ints[i]);
                                if (path != null) {
                                    MRouteOverLay mRouteOverLay = drawMRoutes(path);
                                    mRouteOverLays.add(mRouteOverLay);
                                    mRouteHash.put(ints[i],mRouteOverLay);
                                }
                            }

                            for (int i = 0; i < ints.length; i++) {
                                AMapNaviPath path = paths.get(ints[i]);
                                if (path != null) {
                                    RouteOverLay routeOverLay = drawRoutes(ints[i], path);

                                    routeOverLays.add(routeOverLay);
                                    routeMap.put(ints[i],routeOverLay);
                                }
                            }
                            changeRoute();
                        } else {
                            AMapNaviPath path = mLocaionPro.getNaviPath();
                            /**
                             * 单路径不需要进行路径选择，直接传入－1即可
                             */
                            drawRoutes(-1, path);
                        }
                        mActivity.dismissDeleyDialog();

                    }
                }
            },1000);
        }
    };

    private AMap.CancelableCallback mCancelableCallBack = new AMap.CancelableCallback() {
        @Override
        public void onFinish() {

//            mAmapView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
                    LogUtils.d(TAG,"onFinish COMPLETE:isFirst:"+isFirst);
                    if (isFirst&& isStart){
                        isFirst = false;
                        mActivity.forShowDeleyDialog();
                        if (paths==null){
                            paths = mLocaionPro.getNaviPaths();
                        }
                        if (ints == null){
                            ints = mLocaionPro.getPathsInts();
                        }
                        drawPathLine();

                        if (paths != null && paths.size() > 1 && ints != null){
                            for (int i = 0; i < ints.length; i++) {
                                AMapNaviPath path = paths.get(ints[i]);
                                if (path != null) {
                                    MRouteOverLay mRouteOverLay = drawMRoutes(path);
                                    mRouteOverLays.add(mRouteOverLay);
                                    mRouteHash.put(ints[i],mRouteOverLay);
                                }
                            }
                            for (int i = 0; i < ints.length; i++) {
                                AMapNaviPath path = paths.get(ints[i]);
                                if (path != null) {
                                    RouteOverLay routeOverLay = drawRoutes(ints[i], path);
                                    routeOverLays.add(routeOverLay);
                                    routeMap.put(ints[i],routeOverLay);
                                }

                            }
                            changeRoute();
                        } else {
                            AMapNaviPath path = mLocaionPro.getNaviPath();
                            /**
                             * 单路径不需要进行路径选择，直接传入－1即可
                             */
                            drawRoutes(-1, path);
                        }
                        mActivity.dismissDeleyDialog();

                    }
//                }
//            },300);

        }

        @Override
        public void onCancel() {
            LogUtils.d(TAG,"onCancel");
//            watchAll();
        }
    };

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {


    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    private float posiX,posiY;
    private int nowNum;
    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (mAMap!=null) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mMarker = mAMap.addMarker(mOptions);
                    mMarker.setPositionByPixels((int)motionEvent.getX(), (int) motionEvent.getY());
                    mMarker.setDraggable(true);
                    mMarker.setVisible(false);
//                    markers.add(mMarker);
                    int posi = isTouchMarker(mMarker.getPosition());
                    int num ;
                    if ( (num = isInLine(mMarker.getPosition()))!=-1 && markers.size() < 3){
                        int newNum = addNumG(num);
                        if (posi == -1){
                            nowNum = newNum;
                            markers.add(newNum,mMarker.getPosition());
                            numSave.add(newNum,num);
                        }else {
                            nowNum = posi;
                            markers.add(posi,mMarker.getPosition());
                            numSave.add(posi,num);
                        }
                        mMarker.setVisible(true);
                        isTouch = true;
//                        if (mAMap!=null) {
//                            mAMap.getUiSettings().setAllGesturesEnabled(false);
//                        }
                    }else {
                        mMarker.remove();
                        mMarker = null;
                    }
                    posiX = motionEvent.getX();
                    posiY = motionEvent.getY();

                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mMarker!=null){

                        mMarker.setPositionByPixels((int)motionEvent.getX(), (int) motionEvent.getY());
                        float disX = motionEvent.getX() - posiX;
                        float disY = motionEvent.getY() - posiY;
                        if ((disX*disX + disY*disY)>200 && System.currentTimeMillis() - timeRe >200) {
                            markers.remove(nowNum);
                            markers.add(nowNum,mMarker.getPosition());
//                            markers.get(nowNum).latitude = mMarker.getPosition().latitude;
//                            markers.get(nowNum).longitude = mMarker.getPosition().longitude;
                            reCanLine();
                            timeRe = System.currentTimeMillis();
                            posiX = motionEvent.getX();
                            posiY = motionEvent.getY();
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mMarker!=null){
                        mMarker.remove();
//                        mMarker.setDraggable(false);
                        mMarker = null;
                    }

//                    if (mAMap!=null) {
//                        mAMap.getUiSettings().setAllGesturesEnabled(true);
//                    }

                    isTouch = false;
                    break;
            }
        }
    }

    private void reCanLine(){
        LogUtils.d(TAG,"reCanLine");
        wayPois.clear();
        for (LatLng marker:markers){
            LatLng latLng = marker;
            NaviLatLng naviLatLng = new NaviLatLng(latLng.latitude,latLng.longitude);
            wayPois.add(naviLatLng);
        }
        mLocaionPro.calueRunWay(startPoi,wayPois,endPoi);

    }

    private int  isInLine(LatLng latLng){
        LogUtils.d(TAG,"isInLine:"+latLng);
        AMapNaviPath path = paths.get(ints[routeIndex]);
        List<NaviLatLng> latLngs = path.getCoordList();
        for (int i = 0;i<latLngs.size();i++){
            NaviLatLng latLng1 = latLngs.get(i);
            double dis1 = Math.abs(latLng.latitude - latLng1.getLatitude());
            double dis2 = Math.abs(latLng.longitude - latLng1.getLongitude());
            if ((dis1+dis2)<0.002){
                return i;
            }else {
//                LogUtils.d(TAG,"dis1+dis2:"+(dis1+dis2));
            }
        }
        return -1;
    }

    private int  isTouchMarker(LatLng latLng){
        int k = -1;
        for (int i = 0 ;i<markers.size();i++){
//            Marker marker = markers.get(i);
//            LatLng latLng1 = marker.getPosition();
            LatLng latLng1 = markers.get(i);
            double dis1 = Math.abs(latLng.latitude - latLng1.latitude);
            double dis2 = Math.abs(latLng.longitude - latLng1.longitude);
            if ((dis1+dis2) < 0.008){
                k = i;
            }
        }
        if (k != -1) {
            markers.remove(k);
            numSave.remove(k);
        }
        return k;
    }

    private int addNumG(int newPo){
        int k = 0;
        for (int i=0;i<numSave.size();i++){
            if (numSave.get(i) < newPo){
                k = i+1;
            }else break;
        }
        return k;

    }




    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        LogUtils.d(TAG,"onCalculateMultipleRoutesSuccess");
        watchUpdate = null;
        this.ints = ints;
        mActivity.forShowDeleyDialog();
        drawAgain();
        mProgDialog.dismiss();
        mActivity.dismissDeleyDialog();
    }

    @Override
    public void onCalculateRouteSuccess() {

    }

    @Override
    public void onCalculateRouteFailure() {
        mProgDialog.dismiss();
    }


    @Override
    public void onChioceNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed) {
        mLocaionPro.setNaviStyle(congestion,avHighSpeed,avCost,highSpeed);
        reCanLine();
        mProgDialog.show();

    }

    private LikeChangeDialog.OnSelectLikeStyle likeStyle = new LikeChangeDialog.OnSelectLikeStyle() {

        @Override
        public void changeLikeStyle(boolean congestion, boolean avCost, boolean highSpeed, boolean avHighSpeed) {
            mLocaionPro.setNaviStyle(congestion,avHighSpeed,avCost,highSpeed);
            mLocaionPro.reCalue();
        }
    };


    private void drawPathLine(){
        LogUtils.d(TAG,"drawPathLine");
//        if (mPoline0!=null){
//            mPoline0.remove();
//            mPoline0 = null;
//        }
//        if (mPoline1!=null){
//            mPoline1.remove();
//            mPoline1 = null;
//        }
//        if (mPoline2!=null){
//            mPoline2.remove();
//            mPoline2 = null;
//        }
//        HashMap<Integer,AMapNaviPath> paths = mLocaionPro.getNaviPaths();
//        int[] ints1 = mLocaionPro.getPathsInts();
//        AMapNaviPath path0 =  paths.get(ints1[0]);
//        mPoline0 = drawPolyLine(path0);
//        mPoline0.setZIndex(0);
//        if (paths.size()>1) {
//            AMapNaviPath path1 = paths.get(ints1[1]);
//            mPoline1 = drawPolyLine(path1);
//            mPoline1.setZIndex(0);
//        }
//        if (paths.size()>2){
//            AMapNaviPath path2 = paths.get(ints1[2]);
//            mPoline2 = drawPolyLine(path2);
//            mPoline2.setZIndex(0);
//        }
    }

    private Polyline drawPolyLine(AMapNaviPath path){
        LogUtils.d(TAG,"drawPolyLine:begin:"+System.currentTimeMillis());
        List<LatLng> latLngs = naviLatlonToLatLon(path.getCoordList());
        LogUtils.d(TAG,"drawPolyLine:getall Point:"+System.currentTimeMillis());
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new)).width(32).zIndex(0);
        LogUtils.d(TAG,"drawPolyLine:build options:"+System.currentTimeMillis());
        Polyline polyline = mAMap.addPolyline(polylineOptions);
        LogUtils.d(TAG,"drawPolyLine:finish:"+System.currentTimeMillis());
        return polyline;
    }
    private List<LatLng> naviLatlonToLatLon(List<NaviLatLng> naviLatLngs){
        List<LatLng> latLngs = new ArrayList<>();
        for (NaviLatLng naviLatLng:naviLatLngs){
            latLngs.add(new LatLng(naviLatLng.getLatitude(),naviLatLng.getLongitude()));
        }
        return latLngs;
    }

    private AMap.OnPolylineClickListener polylineClickListener = new AMap.OnPolylineClickListener() {
        @Override
        public void onPolylineClick(Polyline polyline) {
            LogUtils.d(TAG,"\n touch one:"+polyline+"\n first:"+mPoline0+"\n first1:"+mPoline1+"\n first2:"+mPoline2);
//            if (polyline.equals(mPoline0)){
//                RunNaviWayFragment.this.routeIndex = 0;
//                RunNaviWayFragment.this.adapter.setIndex(0);
//                changeRoute();
//            }else if (polyline.equals(mPoline1)){
//                RunNaviWayFragment.this.routeIndex = 1;
//                RunNaviWayFragment.this.adapter.setIndex(1);
//                changeRoute();
//
//            }else if(polyline.equals(mPoline2)){
//                RunNaviWayFragment.this.routeIndex = 2;
//                RunNaviWayFragment.this.adapter.setIndex(2);
//                changeRoute();
//            }
            for (int i= 0;i<ints.length;i++){
                MRouteOverLay mRouteOverLay = mRouteOverLays.get(i);
                if (mRouteOverLay.isPolyLineInIt(polyline)){
                    routeIndex = i;
                    changeRoute();
                }
            }
        }
    };

    private float disMwithLat(NaviLatLng latLng, NaviLatLng other){
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(),latLng.getLongitude()),new LatLng(other.getLatitude(),latLng.getLongitude()));
    }
    private float disMwithLon(NaviLatLng latLng,NaviLatLng other){
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(),latLng.getLongitude()),new LatLng(latLng.getLatitude(),other.getLongitude()));
    }

    private void updateScale(){
        float pixel = mAMap.getScalePerPixel();
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
        }else if (mi > 1000){
            mi = mi/1000;
            String str = String.format(getString(R.string.num_kilemile),""+mi);
            mTxBilici.setText(str);
        }
    }

    private GeocodeSearch.OnGeocodeSearchListener mGeocodeListener = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
            String poiName;
            if (address.getAois()!=null && address.getAois().size()>0) {
                AoiItem aoiItem = address.getAois().get(0);

                poiName = aoiItem.getAoiName();

                mTxMarkTitle.setText(poiName);

            }else {

                poiName = getUsefulInfo(address);
                if (poiName.length()<3){
                    poiName = getString(R.string.unknow_road);
                }
                mTxMarkTitle.setText(poiName);
            }

        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    };


    private GeocodeSearch.OnGeocodeSearchListener mGeocodeListener2 = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
            String poiName;
            if (address.getAois()!=null && address.getAois().size()>0) {
                AoiItem aoiItem = address.getAois().get(0);

                poiName = aoiItem.getAoiName();

                mTxMarkTitle2.setText(poiName);

            }else {

                poiName = getUsefulInfo(address);
                if (poiName.length()<3){
                    poiName = getString(R.string.unknow_road);
                }
                mTxMarkTitle2.setText(poiName);
            }

        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

        }
    };

    private String getUsefulInfo(RegeocodeAddress address){
        String msg ;
        if(address.getBuilding().length()>0){
            msg = address.getBuilding()+getString(R.string.negiht);
        }else if(address.getFormatAddress().length()>0) {
            msg = Utils.getAddressSimple(address.getFormatAddress());
        }
        else if(address.getNeighborhood().length()>0) {
            msg = Utils.getAddressSimple(address.getNeighborhood());
        }else if(address.getRoads().size()>0){
            msg = Utils.getAddressSimple(address.getRoads().get(0).getName());

        }else {
            msg = getString(R.string.unknow_road);
        }
        return msg;
    }

    private void addWayPoi(){
        //TODO
        LatLng latLng  = mWayPoiMarker.getPosition();
        mWayPoiMarker.hideInfoWindow();
        mWayPoiMarker.remove();
        mWayPoiMarker = null;
        boolean isTure = mLocaionPro.tryAddWayPoiCalue(new NaviLatLng(latLng.latitude,latLng.longitude));
        if (isTure){
            mActivity.showDialogwithOther();
        }else {
            Toast.makeText(getActivity(),"无法计算",Toast.LENGTH_SHORT).show();
        }

    }

    private void initMarkInfo(){
        mMarkInfoView       = getActivity().getLayoutInflater().inflate(R.layout.layout_tip_show_1,null);
        mTxMarkTitle        = (TextView) mMarkInfoView.findViewById(R.id.tx_tip_show);
        mTxMarkTitle        .setOnClickListener(this);
        mMarkInfoView       .findViewById(R.id.btn_begin_add_way).setOnClickListener(this);
        mMarkInfoView.findViewById(R.id.btn_little_begin_navi).setOnClickListener(this);

        mMarkInfoView2      = getActivity().getLayoutInflater().inflate(R.layout.layout_tip_show_2,null);
        mMarkInfoView2      .findViewById(R.id.btn_begin_add_way_2).setOnClickListener(this);
        mTxMarkTitle2       = (TextView) mMarkInfoView2.findViewById(R.id.tx_tip_show);

    }


    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            LogUtils.d(TAG,"getInfoWindow:"+marker+"\n");
            if (marker.equals(mWayPoiMarker)) {
                return mMarkInfoView;
            }else if(marker.equals(markerWayPoi)){
                return mMarkInfoView2;
            }
            return mMarkInfoView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            LogUtils.d(TAG,"getInfoContents:"+marker);
            if (marker==mWayPoiMarker) {
                return mMarkInfoView;
            }else if(marker == markerWayPoi){
                return mMarkInfoView2;
            }
            return mMarkInfoView;
        }
    };

    private AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

            LogUtils.d(TAG,"marker0:"+marker+"\nwayPoi:"+markerWayPoi);
//            markerWayPoi.showInfoWindow();


            if (marker.equals(markerWayPoi)) {
                markerWayPoi.showInfoWindow();
                return true;
            }

            return true;
        }
    };

}
