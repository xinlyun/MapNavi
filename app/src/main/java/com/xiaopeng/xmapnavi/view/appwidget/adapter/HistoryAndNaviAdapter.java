package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HistoryPosi;


import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by xinlyun on 15-11-20.
 */
public class HistoryAndNaviAdapter extends ArrayAdapter {
    List<PoiItem> poiItemList;
    List<HistoryPosi> historyPosiList;
    LatLng localPosi;
    int style ;
    public HistoryAndNaviAdapter(Context context,List<PoiItem> poiItemList,int resource) {
        super(context,resource);
        this.poiItemList = poiItemList;
        style = 1;
    }
    public HistoryAndNaviAdapter(Context context, int resource,List<HistoryPosi> historyPosiList) {
        super(context, resource);
        this.historyPosiList = historyPosiList;
        style = 0;
    }
    public void setNewOne(List<PoiItem> poiItemList){
        this.poiItemList = poiItemList;
        style = 1;
    }

    public void setLocalPosi(LatLng localPosi){
        this.localPosi = localPosi;
    }
    @Override
    public int getCount() {
        if(style==0&&historyPosiList!=null)
            return historyPosiList.size();
        if (style ==1 && poiItemList!=null)
            return poiItemList.size();
        return 0;
    }

    @Override
    public void clear() {
        historyPosiList = null;
        poiItemList = null;

        super.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if(style==0){
            HistoryPosi historyPosi = historyPosiList.get(position);
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_simple_listview_item,null);
            TextView historyText = (TextView) view.findViewById(R.id.text1);
            historyText.setText(historyPosi.getName());
        }else if(style==1){
            PoiItem poiItem = poiItemList.get(position);
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_list_item,null);
            TextView naviName = (TextView) view.findViewById(R.id.pre_list_name);
            TextView naviPosi = (TextView) view.findViewById(R.id.pre_list_posi);
            TextView naviDis  = (TextView) view.findViewById(R.id.pre_list_dis);
            naviName.setText(poiItem.toString());
            naviPosi.setText(poiItem.getCityName() + "  " + poiItem.getSnippet());
            float dis = AMapUtils.calculateLineDistance(localPosi,new LatLng(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude()));
            dis = dis/1000f;
            DecimalFormat df = new DecimalFormat("0.0");

            String result = df.format(dis);
            naviDis.setText(result+"KM");
        }

        return view;
    }
}
