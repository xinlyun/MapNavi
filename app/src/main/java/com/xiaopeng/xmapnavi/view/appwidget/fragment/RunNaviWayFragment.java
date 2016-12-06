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
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RadarNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.activity.RouteNaviActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.NaviPathAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.LikeChangeDialog;
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
{
    private static final String TAG = "RunNaviWayFragment";
    private MapView mAmapView;
    private AMap mAMap;
    private LatLonPoint fromPoint,toPoint;
    private NaviPathAdapter adapter;
    private List<NaviLatLng> startPoi = new ArrayList<>(),endPoi=new ArrayList<>(),wayPois  = new ArrayList<>();
    private RouteOverLay mRouteOverLay;
    private boolean isTouch = false;
    private List<RouteOverLay> saveOverLay = new ArrayList<>();
    private LikeChangeDialog mSelectLikeDialog;
    private BaseFuncActivityInteface mActivity;
    public void setMapView(MapView mapView){
        mAmapView = mapView;
        mAMap = mAmapView.getMap();
        mAMap.clear();
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
     * 地图对象
     */
    private Marker mStartMarker;
    private Marker mEndMarker;

    /**
     * 保存当前算好的路线
     */
    private SparseArray<RouteOverLay> routeOverlays = new SparseArray<RouteOverLay>();

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
    private List<RouteOverLay> deletLay = new ArrayList<>();
    private AMapNaviPath pathx;
    private NaviChanDialog mNaviChioceDialog;
    private ProgressDialog mProgDialog;
    private boolean isTricall = true;
    private ImageView mIvLKicon;
    private Polyline mPoline0,mPoline1,mPoline2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.countTimeStart(BugHunter.TIME_TYPE_START,TAG,BugHunter.SWITCH_TYPE_START_COOL);
        mActivity = (BaseFuncActivityInteface) getActivity();
        super.onCreate(savedInstanceState);
        mLocaionPro = LocationProvider.getInstence(getActivity());
        mStartMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_from_poi))));
        mEndMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_end_poi))));
        mOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_way_poi));
        bi = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.title_back_00);

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
        mAMap.setMapType(AMap.MAP_TYPE_NAVI);
        readNaviMsg();
        mLocaionPro.addNaviCalueListner(this);
        mAMap.setTrafficEnabled(isTricall);
        mAMap.setOnPolylineClickListener(polylineClickListener);
        mAMap.showMapText(false);

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
        //--listener--//
        mBtnStartNavi       .setOnClickListener(this);
        findViewById(R.id.btn_start_route_navi).setOnClickListener(this);
        findViewById(R.id.btn_pianhao).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_zoom_plus).setOnClickListener(this);
        findViewById(R.id.btn_zoom_jian).setOnClickListener(this);
        findViewById(R.id.btn_lukuang).setOnClickListener(this);
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
    }





    @Override
    public void onStop() {
        super.onStop();
        mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mAMap.showMapText(true);
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
        routeOverlays.clear();
        mAMap.clear();

        watchAll();

        paths = mLocaionPro.getNaviPaths();
        pathx = mLocaionPro.getNaviPath();
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
        if (paths != null && paths.size() > 1 && ints != null){
            for (int i = 0; i < ints.length; i++) {
                AMapNaviPath path = paths.get(ints[i]);
                if (path != null) {
                    drawRoutes(ints[i], path);
                }
            }
        } else {
            AMapNaviPath path = mLocaionPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
            drawRoutes(-1, path);
        }


        drawPathLine();

    }




    private void cleanLine(){
        try {
            if (routeOverlays.size() > 0) {
                for (int i = 0; i < ints.length; i++) {
//                    routeOverlays.get(ints[i]).removeFromMap();
                    routeOverlays.remove(ints[i]);
                }
//            routeOverlays.clear();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        for (int k = 0;k<saveOverLay.size();k++){
            try {
                deletLay.add(saveOverLay.remove(k));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if (mRouteOverLay!=null){
//            mRouteOverLay.removeFromMap();
            deletLay.add(mRouteOverLay);
            mRouteOverLay = null;
        }
        deleteHandler.sendEmptyMessageDelayed(0,50);
    }

    Handler deleteHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if (deletLay.size()>0){
                        deletLay.remove(0).removeFromMap();
                        deleteHandler.sendEmptyMessageDelayed(0,50);
                    }
                    break;


            }
        }
    };

    private void drawAgain(){

        paths = mLocaionPro.getNaviPaths();
        ints = mLocaionPro.getPathsInts();
        watchAll();

        if (paths != null && paths.size() > 1 && ints != null){
            if (!isTouch) {
                for (int i = 0; i < ints.length; i++) {
                    AMapNaviPath path = paths.get(ints[i]);
                    if (path != null) {
                        drawRoutes(ints[i], path);
                    }
                }
                if (routeIndex >= routeOverlays.size())
                    routeIndex = 0;
                int routeID = routeOverlays.keyAt(routeIndex);
                //突出选择的那条路
                for (int i = 0; i < routeOverlays.size(); i++) {
                    int key = routeOverlays.keyAt(i);
                    routeOverlays.get(key).setTransparency(0.0f);
                    routeOverlays.get(key).setZindex(zindex);
                    if ( i != routeID) {
                        routeOverlays.get(key).setWayPointBitmap(bi);
                    }
                }
                routeOverlays.get(routeID).setTransparency(1f);

                /**
                 * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
                 **/
                routeOverlays.get(routeID).setZindex(zindex++);
                mLocaionPro.selectRouteId(routeID);

                mAmapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            adapter = new NaviPathAdapter(getActivity(), R.layout.layout_gradview_item);
                            adapter.setDate(paths, ints);
                            mGvShowNaviPaths.setAdapter(adapter);
                            mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },400);

                mAmapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);
                            LatLngBounds bounds = LatLngBounds.builder().include(new LatLng(fromPoint.getLatitude(), fromPoint.getLongitude()))
                                    .include(new LatLng(toPoint.getLatitude(), toPoint.getLongitude())).build();

                            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                            mAMap.animateCamera(update);
                            mAMap.setOnMapTouchListener(RunNaviWayFragment.this);

                            changeRoute();

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },800);


            }else {
                AMapNaviPath path = paths.get(ints[0]);
                if (path != null) {
                    mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
                    mRouteOverLay = new RouteOverLay(mAMap, path, getActivity());
                    mRouteOverLay.setWayPointBitmap(bi);
                    mRouteOverLay.setTrafficLine(true);
                    mRouteOverLay.addToMap();
                }
            }
        } else {
            AMapNaviPath path = mLocaionPro.getNaviPath();
            /**
             * 单路径不需要进行路径选择，直接传入－1即可
             */
            drawRoutes(-1, path);
        }

        drawPathLine();
    }



    public void changeRoute() {
        if (!calculateSuccess) {
            Toast.makeText(getActivity(), "请先算路", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 计算出来的路径只有一条
         */

        if (routeOverlays.size() == 1) {
            Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeOverlays.size())
            routeIndex = 0;
        int routeID = routeOverlays.keyAt(routeIndex);
        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.0f);
        }
        routeOverlays.get(routeID).setTransparency(1f);
//        routeOverlays.get(routeID).zoomToSpan();
        /**
         * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
         **/
        routeOverlays.get(routeID).setZindex(zindex++);


        //必须告诉AMapNavi 你最后选择的哪条路
        mLocaionPro.selectRouteId(routeID);


        AMapNaviPath path  = paths.get(ints[routeIndex]);
        int traListhNum = getTrafficLightNum(path);
        int cost = path.getTollCost();
        String msgShow = "花费："+cost+"元   经过"+traListhNum+"个红绿灯";
        mTvShowMsg.setText(msgShow);

        watchAll();
    }

    private int getTrafficLightNum(AMapNaviPath path){
        List<AMapNaviStep> steps =path.getSteps();
        int count = 0;
        for (AMapNaviStep step:steps){
            count = count + step.getTrafficLightNumber();
        }
        return count;
    }

    private void drawRoutes(int routeId, AMapNaviPath path) {
        LogUtils.d(TAG,"drawRoutes id:"+routeId);
        calculateSuccess = true;
        mAMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay routeOverLay = new RouteOverLay(mAMap, path, getActivity());
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_from_poi));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_end_poi));
        routeOverLay.setWayPointBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.icon_way_poi));
        routeOverLay.setTrafficLine(true);

        routeOverLay.addToMap();
        routeOverlays.put(routeId, routeOverLay);
        saveOverLay.add(routeOverLay);



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
                Intent intent = new Intent(getActivity(),RouteNaviActivity.class);
                intent.putExtra("gps", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
            case R.id.btn_start_route_navi:
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

            default:
                break;

        }
    }

    private void watchAll(){
        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.d(TAG,"ready to changeRoute \nfrom:"+fromPoint+"\n toPoint："+toPoint);
                    HashMap<Integer,AMapNaviPath> pathHashMap = mLocaionPro.getNaviPaths();
                    int[] ints = mLocaionPro.getPathsInts();

                    LatLngBounds.Builder builder = LatLngBounds.builder();
                    NaviLatLng startPoi = pathHashMap.get(ints[0]).getStartPoint();
                    NaviLatLng endPoi = pathHashMap.get(ints[0]).getEndPoint();
                    float disF = disMwithLat(startPoi,endPoi);
                    float disK = disMwithLon(startPoi,endPoi);
                    double alllenght = Math.sqrt(disF*disF + disK*disK);
                    double num = disF/alllenght;
                    double scall =Math.toDegrees(Math.atan(disF/disK));
                    LogUtils.d(TAG,"scall:"+scall);
                    for (int i =0 ;i < ints.length;i++){
                        AMapNaviPath path = pathHashMap.get(ints[i]);
                        for(NaviLatLng latLng : path.getCoordList()){
                            builder.include(new LatLng(latLng.getLatitude(),latLng.getLongitude()));
                        }
                    }
//                    CameraUpdate update1 = CameraUpdateFactory.changeBearing((float) scall);
//                    mAMap.animateCamera(update1,0,null);
                    LatLngBounds latLngBounds = builder.build();
                    CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) (30 + num * 305));

                    mAMap.animateCamera(update);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },200);
    }

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
        mAMap.clear();
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
//            LatLng latLng = marker.getPosition();
            LatLng latLng = marker;
            NaviLatLng naviLatLng = new NaviLatLng(latLng.latitude,latLng.longitude);
            wayPois.add(naviLatLng);
        }
        mLocaionPro.calueRunWay(startPoi,wayPois,endPoi);

    }

    private int  isInLine(LatLng latLng){
        //TODO
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
                LogUtils.d(TAG,"dis1+dis2:"+(dis1+dis2));
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
        cleanLine();
        this.ints = ints;
        drawAgain();
        mProgDialog.dismiss();
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
        if (mPoline0!=null){
            mPoline0.remove();
            mPoline0 = null;
        }
        if (mPoline1!=null){
            mPoline1.remove();
            mPoline1 = null;
        }
        if (mPoline2!=null){
            mPoline2.remove();
            mPoline2 = null;
        }
        HashMap<Integer,AMapNaviPath> paths = mLocaionPro.getNaviPaths();
        int[] ints1 = mLocaionPro.getPathsInts();
        AMapNaviPath path0 =  paths.get(ints1[0]);
        mPoline0 = drawPolyLine(path0);
        if (paths.size()>1) {
            AMapNaviPath path1 = paths.get(ints1[1]);
            mPoline1 = drawPolyLine(path1);
        }
        if (paths.size()>2){
            AMapNaviPath path2 = paths.get(ints1[2]);
            mPoline2 = drawPolyLine(path2);
        }
    }

    private Polyline drawPolyLine(AMapNaviPath path){
        List<LatLng> latLngs = naviLatlonToLatLon(path.getCoordList());
        PolylineOptions polylineOptions = new PolylineOptions().addAll(latLngs).setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new)).width(32).zIndex(0);
        return mAMap.addPolyline(polylineOptions);
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
            if (polyline.equals(mPoline0)){
                RunNaviWayFragment.this.routeIndex = 0;
                RunNaviWayFragment.this.adapter.setIndex(0);
                changeRoute();
            }else if (polyline.equals(mPoline1)){
                RunNaviWayFragment.this.routeIndex = 1;
                RunNaviWayFragment.this.adapter.setIndex(1);
                changeRoute();

            }else if(polyline.equals(mPoline2)){
                RunNaviWayFragment.this.routeIndex = 2;
                RunNaviWayFragment.this.adapter.setIndex(2);
                changeRoute();
            }
        }
    };

    private float disMwithLat(NaviLatLng latLng, NaviLatLng other){
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(),latLng.getLongitude()),new LatLng(other.getLatitude(),latLng.getLongitude()));
    }
    private float disMwithLon(NaviLatLng latLng,NaviLatLng other){
        return AMapUtils.calculateLineDistance(new LatLng(latLng.getLatitude(),latLng.getLongitude()),new LatLng(latLng.getLatitude(),other.getLongitude()));
    }
}
