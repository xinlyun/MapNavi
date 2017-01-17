package com.xiaopeng.xmapnavi.view.appwidget.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.gc.materialdesign.widgets.ProgressDialog;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ICollectDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpHisDateListner;
import com.xiaopeng.xmapnavi.presenter.callback.XpNaviCalueListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.activity.BaseFuncActivityInteface;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.HistItemAdapater;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.SearchCollectAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.ShowResultAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.TipItemAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.fragment.second.ShowSearchPoiFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/17.
 */

public class AddCollectFragment extends Fragment implements View.OnClickListener,TextWatcher{
    private static final String TAG = "SearchCollectActivity";

    private FrameLayout mFrameLayout0,mFrameLayout1;
    private ProgressDialog mProgDialog;
    private EditText mEtSearch;
    private ListView mLvShowCollect,mLvShowResult;
//    private SearchCollectAdapter mSearchAdpater;
    private DateHelper mDateHelper;
    private String mCity;
    private TipItemAdapter mTipAdpater;
    private ShowResultAdapter mShowResultAdapter;

//    private List<CollectItem> mCollectItems;
    private List<HisItem> mHisItems;
    private List<Tip> mTips;
    private ILocationProvider mLocationProvider;

    private MapView mTmapView;

    private View rootView;
    private int requestCode = -1;
    public static final int WAY_POI_CODE = 2;
    public static final int ADD_POI_CODE = 5;
    private View findViewById(int id){
        return rootView.findViewById(id);
    }

    public void setRequestCode(int code){
        this.requestCode = code;
    }

    public void setMapView(MapView mapView){
        this.mTmapView = mapView;
    }

    public String searchStr;

    private BaseFuncActivityInteface mActivity;

    private HistItemAdapater mHistAdapter;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mActivity = (BaseFuncActivityInteface) getActivity();
//        setContentView(R.layout.activity_search_collect);
//        mLocationProvider = LocationProvider.getInstence(getActivity());
//        initView();
//        init();
//        mCity = mLocationProvider.getAmapLocation().getCity();
////        mTmapView.onCreate(savedInstanceState);
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BaseFuncActivityInteface) getActivity();
        mLocationProvider = LocationProvider.getInstence(getActivity());
        mLocationProvider    .stopNavi();
        mCity = mLocationProvider.getAmapLocation().getCity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_search_collect,container,false);
        initView();
        init();
        mEtSearch.requestFocus();
        return rootView;
    }

    private void initView(){
        mFrameLayout0           = (FrameLayout) findViewById(R.id.first_framelayout);
        mFrameLayout1           = (FrameLayout) findViewById(R.id.second_framelayout);
        mEtSearch               = (EditText) findViewById(R.id.prepare_edittext);
        mLvShowCollect          = (ListView) findViewById(R.id.lv_show_tip_his);
        mLvShowResult           = (ListView) findViewById(R.id.lv_show_result);

//        findViewById(R.id.btn_sure).setOnClickListener(this);
        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.pre_beginnavi).setOnClickListener(this);
        findViewById(R.id.first_framelayout).setOnClickListener(this);
        findViewById(R.id.outside_ll).setOnClickListener(this);
        mLvShowResult           .setOnItemClickListener(mItemClickListner2);
        mEtSearch.addTextChangedListener(this);

        mProgDialog = new ProgressDialog(getActivity(),"正在搜索相关信息....");
//        mProgDialog.setTitle("正在搜索数据");
//        mProgDialog.setMessage("正在搜索相关信息....");
        mProgDialog.setCancelable(true);


        //----init listener ---//
        mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        switch (requestCode){
            case -1:
                mEtSearch.setHint(R.string.please_input_poi);
                break;
            case 0:
                //fill down
            case 3:
                mEtSearch.setHint(R.string.please_input_company);
                break;

            case 1:
                //fill down
            case 4:
                mEtSearch.setHint(R.string.please_input_home);
                break;

            case 2:
                mEtSearch.setHint(R.string.please_input_way_poi);
                break;

            case 5:
                mEtSearch.setHint(R.string.add_collect_poi);
                break;
        }
    }
    private void init(){
        mDateHelper             = new DateHelper();
        mDateHelper             .setHisDateListner(mHistoryListener);

//        mSearchAdpater = new SearchCollectAdapter(getActivity(),R.layout.layout_history_item);

        mHistAdapter = new HistItemAdapater(getActivity(),R.layout.layout_fix_list_item,mLocationProvider.getAmapLocation());

        mTipAdpater     = new TipItemAdapter(getActivity(),R.layout.layout_dialog_show_collect);
        mShowResultAdapter  = new ShowResultAdapter(getActivity(),R.layout.layout_dialog_show_collect);
        mLvShowResult.setAdapter(mShowResultAdapter);
    }

    private XpHisDateListner mHistoryListener = new XpHisDateListner() {
        @Override
        public void onHistoryDate(List<HisItem> hisItems) {
            mHisItems = hisItems;
            mHistAdapter.setDate(hisItems);
//            mHistAdapter.setOnClickRightItem(this);
            mLvShowCollect.setAdapter(mHistAdapter);
            mLvShowCollect.setOnItemClickListener(mItemHisClickListner);
//            isHistoryList = true;
        }
    };




//    private XpCollectListener mXpCollectListener = new XpCollectListener() {
//        @Override
//        public void onCollectCallBack(List<CollectItem> collectItems) {
//            mCollectItems = collectItems;
//            mSearchAdpater.setData(collectItems);
//            mLvShowCollect.setAdapter(mSearchAdpater);
//            mLvShowCollect.setOnItemClickListener(mItemClickListner);
//        }
//    };

    @Override
    public void onStart() {
        super.onStart();
        mLocationProvider.addSearchListner(mSearchListener);
        mLocationProvider.addNaviCalueListner(mNaviCalueListener);
        mDateHelper.getHisItem(10);
        showInputT();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLocationProvider.removeSearchListner(mSearchListener);
        mLocationProvider.removeNaviCalueListner(mNaviCalueListener);
        showHide();
    }

    private XpSearchListner mSearchListener = new XpSearchListner() {
        @Override
        public void searchSucceful() {
            mProgDialog.dismiss();
            mEtSearch.setText("");
            PoiResult mPoiResult = mLocationProvider.getPoiResult();
            if (mPoiResult != null && mPoiResult.getQuery() != null
                    && mPoiResult.getPois() != null && mPoiResult.getPois().size() > 0) {// 搜索poi的结果
                ShowSearchPoiFragment fragment = new ShowSearchPoiFragment();
                fragment.setRequestCode(requestCode);
                fragment.setSearchStr(searchStr);
//            if (requestCode != WAY_POI_CODE) {
//                mActivity.startFragment(fragment);
//            }else {
                mActivity.startFragmentReplace(fragment);
//            }

//            mFrameLayout0.setVisibility(View.GONE);
//            mFrameLayout1.setVisibility(View.VISIBLE);
//            List<PoiItem> items = mLocationProvider.getPoiResult().getPois();
//            mShowResultAdapter.setDate(items);
            }else {
                Toast.makeText(getActivity(),"未搜索到结果",Toast.LENGTH_SHORT).show();
            }


        }
    };

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure:
                saveCollect();
                break;

            case R.id.btn_exit:
                showHide();
                mActivity.exitFragment();
                break;

            case R.id.btn_return:
                mFrameLayout1.setVisibility(View.GONE);
                mFrameLayout0.setVisibility(View.VISIBLE);
                break;

            case R.id.pre_beginnavi:
                showHide();
                String str = mEtSearch.getText().toString();
                readyToSearch(str);
                break;

            case R.id.outside_ll:
                mActivity.exitFragment();
                break;

            default:
                break;


        }
    }

    private void saveCollect(){
        PoiItem item = mShowResultAdapter.getChoiceItem();
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("name",item.toString());
        bundle.putString("desc",item.getSnippet());
        bundle.putDouble("poiLat",item.getLatLonPoint().getLatitude());
        bundle.putDouble("poiLon",item.getLatLonPoint().getLongitude());
        intent.putExtras(bundle);

        mDateHelper.saveWhereIten(requestCode,item.toString(),item.getSnippet(),item.getLatLonPoint().getLatitude(),item.getLatLonPoint().getLongitude());
        mActivity.exitFragment();
//        setResult(RESULT_OK,intent);
//        finish();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (charSequence.length()>=2){
            String str = charSequence.toString();
            InputtipsQuery inputQuery = new InputtipsQuery(str, mCity);
            inputQuery.setCityLimit(true);
            Inputtips inputTips = new Inputtips(getActivity(), inputQuery);
            inputTips.setInputtipsListener(inputtipsListener);
            inputTips.requestInputtipsAsyn();
        } else {
            mDateHelper.getCollectItems();
        }
    }

    private Inputtips.InputtipsListener inputtipsListener = new Inputtips.InputtipsListener(){
        @Override
        public void onGetInputtips(List<Tip> list, int i) {
            mTips = list;
            mTipAdpater.setDate(list);
            mLvShowCollect.setAdapter(mTipAdpater);
            mLvShowCollect.setOnItemClickListener(mItemClickListner1);
        }
    };

    private AdapterView.OnItemClickListener mItemHisClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                if (position<mHisItems.size()) {
                    HisItem hisItem = mHisItems.get(position);
                    if (hisItem.type == DateHelper.TYPE_NAME) {
                        mEtSearch.setText(hisItem.msg);
                        readyToSearch(hisItem.msg);
                        showHide();
                    } else if (hisItem.type == DateHelper.TYPE_POSI) {
                        mDateHelper.saveCollect(hisItem.posiName, hisItem.posiArt, hisItem.posiLat, hisItem.posiLon);
                        mActivity.exitFragment();
                        mActivity.showCollectDialog();
                    } else if (hisItem.type == DateHelper.TYPE_WAY) {

                    }
                }else {
                    mDateHelper.clearDate();
                    mDateHelper.getHisItem(10);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };





    private AdapterView.OnItemClickListener mItemClickListner1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Tip tip = mTips.get(position);
            String name = tip.getName();
            mEtSearch.setText(name);
            readyToSearch(name);
            showHide();
        }
    };

    private AdapterView.OnItemClickListener mItemClickListner2 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mShowResultAdapter.chioceOne(position);
            saveCollect();
        }
    };

    private void readyToSearch(String str){
        searchStr = str;
        if (str.length() < 1){
            Toast.makeText(getActivity(),R.string.please_sure_text,Toast.LENGTH_SHORT).show();
        }else {
            mEtSearch.setText(str);
            mProgDialog.show();
            mLocationProvider.trySearchPosi(str);

        }
    }

    private void showInputT(){
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEtSearch, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    private void showHide(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        //隐藏软键盘
        imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);
        //显示软键盘
//        imm.showSoftInputFromInputMethod(mEtSearch.getWindowToken(), 0);
    }
}
