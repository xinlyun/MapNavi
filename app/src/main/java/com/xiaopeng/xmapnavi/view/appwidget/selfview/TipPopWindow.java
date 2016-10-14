package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.presenter.callback.TipItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by linzx on 2016/10/13.
 */
public class TipPopWindow implements AdapterView.OnItemClickListener{
    private static final String TAG = "TipPopWindow";
    private static int WINDOW_WIDTH = 650;
    private static int WINDOW_HEIGHT = 550;

    private Context mContext;
    private View mView,mWindowView;
    private ListView mLvShowTip;
    private SimpleAdapter mAdapter;
    private List<HashMap<String, String>> listString;
    private PopupWindow mPopWindow;
    private TipItemClickListener mItemClickListner;

    public TipPopWindow(View view){
        mContext = view.getContext();
        mView    = view;
        mWindowView = LayoutInflater.from(mContext).inflate(R.layout.window_shwo_tip,null);
        mLvShowTip  = (ListView) mWindowView.findViewById(R.id.lv_show_tip);
        listString = new ArrayList<HashMap<String, String>>();
        mAdapter = new SimpleAdapter(mContext, listString, R.layout.item_layout,
                new String[] {"name","address"}, new int[] {R.id.poi_field_id, R.id.poi_value_id});
        mLvShowTip  .setAdapter(mAdapter);

        mPopWindow = new PopupWindow(mWindowView,WINDOW_WIDTH,WINDOW_HEIGHT);
        mLvShowTip  .setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mItemClickListner != null){
            mItemClickListner.onClickItem(i,listString.get(i));
        }
    }

    public void show(){
        if (mView != null && mPopWindow != null){
            mPopWindow  .showAsDropDown(mView,0,20);
        }
    }

    public void dismiss(){
        if (mPopWindow != null){
            mPopWindow.dismiss();
        }
    }

    public void setData(List<HashMap<String, String>> data){
        if (listString != null){
            listString.clear();
            listString.addAll(data);
            mAdapter.notifyDataSetChanged();
        }
    }
    public void setOnTipItemClickListener(TipItemClickListener itemClickListener){
        mItemClickListner = itemClickListener;
    }
}
