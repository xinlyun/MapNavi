package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.TextureMapView;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.mode.LocationProvider;
import com.xiaopeng.xmapnavi.presenter.ICollectDateHelper;
import com.xiaopeng.xmapnavi.presenter.ILocationProvider;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpCollectListener;
import com.xiaopeng.xmapnavi.presenter.callback.XpSearchListner;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.SearchCollectAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.ShowResultAdapter;
import com.xiaopeng.xmapnavi.view.appwidget.adapter.TipItemAdapter;

import java.util.List;

/**
 * Created by linzx on 2016/11/17.
 */

public class SearchCollectActivity extends Activity implements View.OnClickListener,TextWatcher{
    private static final String TAG = "SearchCollectActivity";

    private FrameLayout mFrameLayout0,mFrameLayout1;
    private ProgressDialog mProgDialog;
    private EditText mEtSearch;
    private ListView mLvShowCollect,mLvShowResult;
    private SearchCollectAdapter mSearchAdpater;
    private ICollectDateHelper mDateHelper;
    private String mCity;
    private TipItemAdapter mTipAdpater;
    private ShowResultAdapter mShowResultAdapter;

    private List<CollectItem> mCollectItems;
    private List<Tip> mTips;
    private ILocationProvider mLocationProvider;

    private TextureMapView mTmapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_collect);
        mLocationProvider = LocationProvider.getInstence(this);
        initView();
        init();
        mCity = mLocationProvider.getAmapLocation().getCity();
        mTmapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTmapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTmapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mTmapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTmapView.onDestroy();
    }

    private void initView(){
//        mTmapView               = (TextureMapView) findViewById(R.id.base_map);
        mFrameLayout0           = (FrameLayout) findViewById(R.id.first_framelayout);
        mFrameLayout1           = (FrameLayout) findViewById(R.id.second_framelayout);
        mEtSearch               = (EditText) findViewById(R.id.prepare_edittext);
        mLvShowCollect          = (ListView) findViewById(R.id.lv_show_tip_his);
        mLvShowResult           = (ListView) findViewById(R.id.lv_show_result);

//        findViewById(R.id.btn_sure).setOnClickListener(this);
        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        mLvShowResult           .setOnItemClickListener(mItemClickListner2);
        mEtSearch.addTextChangedListener(this);

        mProgDialog = new ProgressDialog(this);
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
    private void init(){
        mDateHelper             = new DateHelper();
        mDateHelper             .setOnCollectListener(mXpCollectListener);
        mSearchAdpater = new SearchCollectAdapter(this,R.layout.layout_history_item);
        mTipAdpater     = new TipItemAdapter(this,R.layout.layout_dialog_show_collect);
        mShowResultAdapter  = new ShowResultAdapter(this,R.layout.layout_dialog_show_collect);
        mLvShowResult.setAdapter(mShowResultAdapter);
    }

    private XpCollectListener mXpCollectListener = new XpCollectListener() {
        @Override
        public void onCollectCallBack(List<CollectItem> collectItems) {
            mCollectItems = collectItems;
            mSearchAdpater.setData(collectItems);
            mLvShowCollect.setAdapter(mSearchAdpater);
            mLvShowCollect.setOnItemClickListener(mItemClickListner);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mLocationProvider.addSearchListner(mSearchListener);
        mDateHelper.getCollectItems();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationProvider.removeSearchListner(mSearchListener);
    }

    private XpSearchListner mSearchListener = new XpSearchListner() {
        @Override
        public void searchSucceful() {
            mProgDialog.dismiss();
            mEtSearch.setText("");
            mFrameLayout0.setVisibility(View.GONE);
            mFrameLayout1.setVisibility(View.VISIBLE);
            List<PoiItem> items = mLocationProvider.getPoiResult().getPois();
            mShowResultAdapter.setDate(items);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure:
                saveCollect();
                break;

            case R.id.btn_exit:
                finish();
                break;

            case R.id.btn_return:
                mFrameLayout1.setVisibility(View.GONE);
                mFrameLayout0.setVisibility(View.VISIBLE);
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
        setResult(RESULT_OK,intent);
        finish();
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
            Inputtips inputTips = new Inputtips(this, inputQuery);
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

    private AdapterView.OnItemClickListener mItemClickListner = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            LogUtils.d(TAG,"mItemClickListner"+"  mCollectItems:"+mCollectItems);
            if (mCollectItems==null || position>=mCollectItems.size())return;
            CollectItem item = mCollectItems.get(position);
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString("name",item.pName);
            bundle.putString("desc",item.pDesc);
            bundle.putDouble("poiLat",item.posLat);
            bundle.putDouble("poiLon",item.posLon);
            intent.putExtras(bundle);
            setResult(RESULT_OK,intent);
            finish();
        }
    };


    private AdapterView.OnItemClickListener mItemClickListner1 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Tip tip = mTips.get(position);
            String name = tip.getName();
            mEtSearch.setText(name);
            readyToSearch(name);
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
        if (str.length() < 3){
            Toast.makeText(this,R.string.please_sure_text,Toast.LENGTH_SHORT).show();
        }else {
            mProgDialog.show();
            mLocationProvider.trySearchPosi(str);
            mEtSearch.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgDialog.dismiss();
                }
            },6 * 1000);
        }
    }
}
