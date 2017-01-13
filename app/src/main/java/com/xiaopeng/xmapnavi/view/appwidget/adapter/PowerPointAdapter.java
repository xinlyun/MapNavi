package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.xiaopeng.amaplib.util.AMapUtil;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.PowerPoint;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/12/28.
 */

public class PowerPointAdapter extends ArrayAdapter {
    private Context mContext;
    private List<PowerPoint> dataPowers = new ArrayList<>();
    private LatLng mPoi;
    private OnClickRightItem mOnClickRightItem;
    private boolean[] isLoves;
    public void setOnClickRightItem(OnClickRightItem rightItem){
        mOnClickRightItem = rightItem;
    }
    private DateHelper mDateHelper;
    public PowerPointAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mDateHelper = new DateHelper();
    }

    public void setPoi(LatLng latLng){
        mPoi = latLng;
    }

    @Override
    public int getCount() {
        return dataPowers.size();
    }

    public void setData(List<PowerPoint> powerPoints){
        dataPowers.clear();
        if (powerPoints!=null) {
            dataPowers.addAll(powerPoints);
            isLoves = new boolean[powerPoints.size()];
            for (int i = 0; i < powerPoints.size(); i++) {
                PowerPoint powerPoint = powerPoints.get(i);
                List<PowerPoint> powerPoints1 = mDateHelper.getPowerPointById(powerPoint.getPoiId());
                if (powerPoints1 != null && powerPoints1.size() > 0) {
                    isLoves[i] = true;
                } else {
                    isLoves[i] = false;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ItemHolder itemHolder = new ItemHolder();
        PowerPoint powerPoint = dataPowers.get(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_power_msg, null);
            itemHolder.tvAddress    = (TextView) view.findViewById(R.id.tv_address);
            itemHolder.tvName       = (TextView) view.findViewById(R.id.tv_name);
            itemHolder.tvFast       = (TextView) view.findViewById(R.id.tv_fast);
            itemHolder.tvSlow       = (TextView) view.findViewById(R.id.tv_slow);
            itemHolder.tvPowerPy    = (TextView) view.findViewById(R.id.tv_power_py);
            itemHolder.tvServerPy   = (TextView) view.findViewById(R.id.tv_server_py);
            itemHolder.tvShowDis    = (TextView) view.findViewById(R.id.tv_distance);
            itemHolder.ivLove       = (ImageView) view.findViewById(R.id.img_love);
            itemHolder.tvFastTotal  = (TextView) view.findViewById(R.id.tv_kuai_total);
            itemHolder.tvSlowTotal  = (TextView) view.findViewById(R.id.tv_man_total);
            itemHolder.tvServerTime = (TextView) view.findViewById(R.id.tv_server_time);
            view.setTag(itemHolder);
        }else {
            view = convertView;
            itemHolder = (ItemHolder) view.getTag();
        }

        itemHolder.tvAddress.setText(powerPoint.getAddress());
        itemHolder.tvName.setText(powerPoint.getName());
        itemHolder.tvFast.setText(""+powerPoint.getAcIdleCnt());
        itemHolder.tvSlow.setText(""+powerPoint.getDcIdleCnt());
        itemHolder.tvFastTotal.setText(""+powerPoint.getAcCnt());
        itemHolder.tvSlowTotal.setText(""+powerPoint.getDcCnt());
        itemHolder.tvPowerPy.setText(""+powerPoint.getElectricFee()+"元/度");
        itemHolder.tvServerPy.setText(""+powerPoint.getServiceFee()+"元/度");
        itemHolder.tvServerTime.setText(powerPoint.getTimeDesc());
        if (mPoi!=null) {
            LatLng thisPoi = new LatLng(powerPoint.getLat(),powerPoint.getLon());
            float dis = AMapUtils.calculateLineDistance(mPoi,thisPoi);
            if (dis>1000){
                dis = dis/1000f;
                DecimalFormat df = new DecimalFormat("0.0");
                String result = df.format(dis);
                itemHolder.tvShowDis.setText(result+"公里");
            }else {
                itemHolder.tvShowDis.setText(dis+"米");
            }
        }
        try {
            if (isLoves[position]) {
                itemHolder.ivLove.setImageResource(R.drawable.icon_collect_1);
            } else {
                itemHolder.ivLove.setImageResource(R.drawable.icon_collect_2);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        itemHolder.ivLove.setOnClickListener(new lvButtonListener(position));
//        LogUtils.d("PowerPointAdapater","disance:"+powerPoint.getDistance());

        return view;
    }


    class ItemHolder{
        TextView tvName,tvAddress,tvShowDis,tvPowerPy,tvServerPy,tvFast,tvSlow,tvFastTotal,tvSlowTotal,tvServerTime;
        ImageView ivLove;
    }


    class lvButtonListener implements View.OnClickListener {
        private int position ;

        lvButtonListener( int pos) {
            position = pos;
        }

        @Override
        public void onClick( View v) {
            try{
                isLoves[position] = !isLoves[position];
                notifyDataSetChanged();
                PowerPoint powerPoint = dataPowers.get(position);
                if (isLoves[position]){
                    powerPoint.save();
                }else {
                    mDateHelper.deletPowerPointById(powerPoint.getPoiId());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            if (mOnClickRightItem != null) {
                int posi1 = position;
                mOnClickRightItem.onClickRightItem(posi1);
            }
        }
    }
}
