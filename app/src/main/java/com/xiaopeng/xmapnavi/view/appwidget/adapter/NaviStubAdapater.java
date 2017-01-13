package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.xiaopeng.amaplib.util.AMapUtil;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2017/1/10.
 */

public class NaviStubAdapater extends ArrayAdapter {
    private List<PowerPoint> mDate = new ArrayList<>();
    private int index = -1;
    private AMapLocation aMapLocation;
    private OnClickRightItem mOnClickRightItem;
    public NaviStubAdapater(Context context, int resource, AMapLocation Location) {
        super(context, resource);
        aMapLocation = Location;
    }

    @Override
    public int getCount() {
        return mDate.size();
    }

    public void setDate(List<PowerPoint> date){
        index = -1;
        mDate.clear();
        mDate.addAll(date);
        notifyDataSetChanged();
    }

    public PowerPoint setIndex(int index){
        this.index = index;
        notifyDataSetChanged();
        if (mDate.size()>index){
            return mDate.get(index);
        }else return null;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ItemHolder itemHolder = new ItemHolder();
        PowerPoint powerPoint = mDate.get(position);
        if (convertView!=null && convertView.getTag()!=null){
            view = convertView;
            itemHolder = (ItemHolder) view.getTag();
        }else {
            //begin view
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_powerstub, null);
            itemHolder.tvName   = (TextView) view.findViewById(R.id.tv_stub_name);
            itemHolder.tvDis    = (TextView) view.findViewById(R.id.tv_stub_dis);
            itemHolder.tvNum    = (TextView) view.findViewById(R.id.tv_stub_msg);
            itemHolder.rlAddWayPoi= (RelativeLayout) view.findViewById(R.id.btn_begin_add_way);
            //end view
            view.setTag(itemHolder);
        }
        itemHolder.tvName.setText(powerPoint.getName());
        float dis = AMapUtils.calculateLineDistance(new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude()),new LatLng(powerPoint.getLat(),powerPoint.getLon()));
        String disStr =  AMapUtil.getFriendlyLength((int) dis);
        itemHolder.tvDis.setText(disStr);
        String stubMsg = "空闲:快"+powerPoint.acIdleCnt+"/慢"+powerPoint.dcIdleCnt;
        itemHolder.tvNum.setText(stubMsg);
        if (index == position){
            itemHolder.rlAddWayPoi.setVisibility(View.VISIBLE);
        }else {
            itemHolder.rlAddWayPoi.setVisibility(View.GONE);
        }
        itemHolder.rlAddWayPoi.setOnClickListener(new lvButtonListener(position));


        return view;
    }

    class ItemHolder{
        TextView tvName;
        TextView tvDis;

        RelativeLayout rlAddWayPoi;
        TextView tvNum;
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
            if (mOnClickRightItem != null) {
                int posi1 = position;
                mOnClickRightItem.onClickRightItem(posi1);
            }
        }
    }

    public PowerPoint getPoP(int posi){
        if (posi < mDate.size()){
            return mDate.get(posi);
        }else {
            return null;
        }
    }
}
