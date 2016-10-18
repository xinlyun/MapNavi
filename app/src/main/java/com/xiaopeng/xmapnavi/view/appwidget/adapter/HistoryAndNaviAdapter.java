package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HistoryPosi;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;


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
    private int index = -1;
    private OnClickRightItem mOnClickRightItem;

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
        this.index = -1;
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

    private int touchPoi = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if(style==0){
            HistoryPosi historyPosi = historyPosiList.get(position);
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_simple_listview_item,null);
            TextView historyText = (TextView) view.findViewById(R.id.text1);
            historyText.setText(historyPosi.getName());
        }else if(style==1){
            ItemHolder itemHolder = new ItemHolder();
            PoiItem poiItem = poiItemList.get(position);
            touchPoi = position;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_list_item,null);
                itemHolder.naviBtn = (LinearLayout) view .findViewById(R.id.touch_to_navi);
                itemHolder.naviBtn.setTag(position);

                itemHolder.naviName = (TextView) view.findViewById(R.id.pre_list_name);
                itemHolder.naviPosi = (TextView) view.findViewById(R.id.pre_list_posi);
                itemHolder.naviDis  = (TextView) view.findViewById(R.id.pre_list_dis);
                view.setTag(itemHolder);
            }else {
                view = convertView;
                itemHolder = (ItemHolder) view.getTag();
            }
            itemHolder.naviName.setText(poiItem.toString());
            itemHolder.naviPosi.setText(poiItem.getCityName() + "  " + poiItem.getSnippet());
            itemHolder.naviBtn.setOnClickListener(new lvButtonListener(position));
            if (position == index){
//                android:background="@color/trans_white_lrc"
                view.findViewById(R.id.back_ll).setBackgroundColor(getContext().getResources().getColor(R.color.gray_lite));
            }else {
                view.findViewById(R.id.back_ll).setBackground(null);
            }
            float dis = AMapUtils.calculateLineDistance(localPosi,new LatLng(poiItem.getLatLonPoint().getLatitude(),poiItem.getLatLonPoint().getLongitude()));
            dis = dis/1000f;
            DecimalFormat df = new DecimalFormat("0.0");
            String result = df.format(dis);
            itemHolder.naviDis.setText(result+"KM");
        }

        return view;
    }

    class ItemHolder{
        TextView naviName,naviPosi,naviDis;
        LinearLayout naviBtn;
//        ImageView imageView;
    }





    public void setOnClickRightItem(OnClickRightItem rightItem){
        mOnClickRightItem = rightItem;
    }

    class lvButtonListener implements View.OnClickListener {
        private int position ;

        lvButtonListener( int pos) {
            position = pos;
        }

        @Override
        public void onClick( View v) {
            Log.d("PlayListDetailAdapter","Onclick:"+(int)v.getTag());
            if (mOnClickRightItem != null) {
                int posi1 = position;
                mOnClickRightItem.onClickRightItem(posi1);
            }
        }
    }

    public void setIndex(int index){
        this.index = index;
        this.notifyDataSetChanged();
        
    }

}
