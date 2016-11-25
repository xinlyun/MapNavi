package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.PoiItem;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/18.
 */

public class ShowResultAdapter extends ArrayAdapter {
    private List<PoiItem> results  = new ArrayList<>();
    private int mPosi;
    private Context mContext;
    public ShowResultAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
    }

    public void setDate(List<PoiItem> poiItems){
        mPosi = -1;
        results.clear();
        results.addAll(poiItems);
        notifyDataSetChanged();
    }

    public void chioceOne(int posi){
        mPosi = posi;
        notifyDataSetChanged();
    }

    public int getChoicePosi(){
        return mPosi;
    }
    public PoiItem getChoiceItem(){
        if (mPosi!=-1){
            return results.get(mPosi);
        }
        return null;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ShowResultAdapter.ViewHolder itemHolder = new ShowResultAdapter.ViewHolder();
        PoiItem item = results.get(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_show_resulte_item, null);
            itemHolder.mTxName = (TextView) view.findViewById(R.id.pre_list_name);
            itemHolder.mTxPoi = (TextView) view.findViewById(R.id.pre_list_posi);
            view.setTag(itemHolder);
        } else {
            view = convertView;
            itemHolder = (ShowResultAdapter.ViewHolder) view.getTag();
        }
        if (mPosi ==position){
            itemHolder.mTxName.setTextColor(mContext.getResources().getColor(R.color.naviblue));
            itemHolder.mTxPoi.setTextColor(mContext.getResources().getColor(R.color.naviblue));
        }else {
            itemHolder.mTxName.setTextColor(mContext.getResources().getColor(R.color.black));
            itemHolder.mTxPoi.setTextColor(mContext.getResources().getColor(R.color.gray_btn_bg_pressed_color));
        }
        itemHolder.mTxName.setText(item.toString());
        itemHolder.mTxPoi.setText(item.getCityName() + "  " + item.getSnippet());
        return view;
    }

    class ViewHolder {
        TextView mTxName;
        TextView mTxPoi;
    }
}
