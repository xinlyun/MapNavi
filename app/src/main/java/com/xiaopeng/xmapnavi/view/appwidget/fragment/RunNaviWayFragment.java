package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.xiaopeng.lib.bughunter.BugHunter;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.INaviViewProvide;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.view.appwidget.activity.ShowPosiActivity;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.NaviPathAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.selfview.NaviChanDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RunnableFuture;

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
    public void setMapView(MapView mapView){
        mAmapView = mapView;
        mAMap = mAmapView.getMap();
        mAMap.clear();

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
    private int zindex = 1;
    /**
     * 路线计算成功标志位
     */
    private boolean calculateSuccess = false;


    private GridView mGvShowNaviPaths;
    private TextView mTvShowMsg;
    private int[] ints;
    private Button mBtnStartNavi;
    MarkerOptions mOptions;
    long timeRe ;
    Bitmap bi;
    private List<RouteOverLay> deletLay = new ArrayList<>();

    private NaviChanDialog mNaviChioceDialog;
    private ProgressDialog mProgDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BugHunter.statisticsStart(BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
        super.onCreate(savedInstanceState);
        mLocaionPro = LocationProvider.getInstence(getActivity());
        mStartMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.start))));
        mEndMarker = mAMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), com.xiaopeng.amaplib.R.drawable.end))));
        mOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(com.xiaopeng.amaplib.R.drawable.way));
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
        readNaviMsg();

        mLocaionPro.addNaviCalueListner(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BugHunter.statisticsEnd(getActivity().getApplication(),BugHunter.CUSTOM_STATISTICS_TYPE_START_ACTIVITY,TAG);
    }

    private void initView(){
        mGvShowNaviPaths    = (GridView) findViewById(R.id.gv_show_an);
        mTvShowMsg          = (TextView) findViewById(R.id.tv_show_msg);
        mBtnStartNavi       = (Button) findViewById(R.id.btn_start_navi);

        //--listener--//
        mBtnStartNavi       .setOnClickListener(this);
        findViewById(R.id.btn_start_route_navi).setOnClickListener(this);
        findViewById(R.id.tv_right).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
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
        mLocaionPro.removeNaviCalueListner(this);
        mAMap.setOnMapTouchListener(null);
        if (mAMap!=null) {
            mAMap.getUiSettings().setAllGesturesEnabled(false);
        }
    }

    public void setSucceful(int[] ints){
        this.ints = ints;
    }
    private HashMap<Integer, AMapNaviPath> paths;
    private void readNaviMsg(){
        routeOverlays.clear();
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

//                    if (mAMap!=null) {
//                            mAMap.getUiSettings().setAllGesturesEnabled(false);
//                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },800);

        paths = mLocaionPro.getNaviPaths();

        mAmapView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                HashMap<Integer, AMapNaviPath> paths = mLocaionPro.getNaviPaths();
//                INaviViewProvide naviViewProvide = new NaviViewProvide(RunNaviWayFragment.this.getActivity());
//                HashMap<Integer,View> viewHashMap = naviViewProvide.createViewByPath(paths);
//                addViewWithViews(viewHashMap);
                adapter = new NaviPathAdapter(getActivity(),R.layout.layout_gradview_item);
                adapter.setDate(paths,ints);
                mGvShowNaviPaths.setAdapter(adapter);
                mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
            }
        },400);


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
                    routeOverlays.get(key).setTransparency(0.23f);
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
//                HashMap<Integer, AMapNaviPath> paths = mLocaionPro.getNaviPaths();
//                INaviViewProvide naviViewProvide = new NaviViewProvide(RunNaviWayFragment.this.getActivity());
//                HashMap<Integer,View> viewHashMap = naviViewProvide.createViewByPath(paths);
//                addViewWithViews(viewHashMap);
                        adapter = new NaviPathAdapter(getActivity(),R.layout.layout_gradview_item);
                        adapter.setDate(paths,ints);
                        mGvShowNaviPaths.setAdapter(adapter);
                        mGvShowNaviPaths.setOnItemClickListener(mClickPathItemListner);
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

//                    if (mAMap!=null) {
//                            mAMap.getUiSettings().setAllGesturesEnabled(false);
//                    }
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
//            chooseRouteSuccess = true;
            Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPath()).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPath()).getAllTime() + "s", Toast.LENGTH_SHORT).show();
            return;
        }

        if (routeIndex >= routeOverlays.size())
            routeIndex = 0;
        int routeID = routeOverlays.keyAt(routeIndex);
        //突出选择的那条路
        for (int i = 0; i < routeOverlays.size(); i++) {
            int key = routeOverlays.keyAt(i);
            routeOverlays.get(key).setTransparency(0.4f);
        }
        routeOverlays.get(routeID).setTransparency(1f);
        /**
         * 把用户选择的那条路的权值弄高，使路线高亮显示的同时，重合路段不会变的透明
         **/
        routeOverlays.get(routeID).setZindex(zindex++);


        //必须告诉AMapNavi 你最后选择的哪条路
//        mAMapNavi.selectRouteId(routeID);
        mLocaionPro.selectRouteId(routeID);

//        Toast.makeText(getActivity(), "导航距离:" + (mLocaionPro.getNaviPaths()).get(routeID).getAllLength() + "m" + "\n" + "导航时间:" + (mLocaionPro.getNaviPaths()).get(routeID).getAllTime() + "s", Toast.LENGTH_SHORT).show();
//        routeIndex++;

//        chooseRouteSuccess = true;

        AMapNaviPath path  = paths.get(ints[routeIndex]);
        int traListhNum = getTrafficLightNum(path);
        int cost = path.getTollCost();
        String msgShow = "花费："+cost+"元   经过"+traListhNum+"个红绿灯";
        mTvShowMsg.setText(msgShow);
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
                ((ShowPosiActivity)getActivity()).requestStartNavi();
                break;
            case R.id.btn_start_route_navi:
                ((ShowPosiActivity)getActivity()).requestRouteNavi();
                break;

            case R.id.tv_right:
                mNaviChioceDialog.show();
                break;

            case  R.id.btn_exit:
                getActivity().finish();
                break;
            default:
                break;

        }
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

    private float posiX,posiY;
    private int nowNum;
    @Override
    public void onTouch(MotionEvent motionEvent) {
        LogUtils.d(TAG,"onTouch:");
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
                        if (mAMap!=null) {
                            mAMap.getUiSettings().setAllGesturesEnabled(false);
                        }
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

                    if (mAMap!=null) {
                        mAMap.getUiSettings().setAllGesturesEnabled(true);
                    }

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
    public void onChioceNaviStyle(boolean congestion, boolean avHighSpeed, boolean avCost, boolean highSpeed) {
        mLocaionPro.setNaviStyle(congestion,avHighSpeed,avCost,highSpeed);
        reCanLine();
        mProgDialog.show();
        mTvShowMsg.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgDialog.dismiss();
            }
        },6 * 1000);
    }
}
