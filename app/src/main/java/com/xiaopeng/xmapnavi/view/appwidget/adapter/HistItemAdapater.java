package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import com.xiaopeng.lib.utils.utils.LogUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class HistItemAdapater extends ArrayAdapter {
    private List<HisItem> hisItems = new ArrayList<>();
    private AMapLocation mLocation;

    private OnClickRightItem mOnClickRightItem;

    public HistItemAdapater(Context context, int resource, AMapLocation Location) {
        super(context, resource);
        mLocation = Location;

    }

    @Override
    public int getCount() {
        if (hisItems.size()!=0) {
            return hisItems.size() + 1;
        }else {
            return 0;
        }
    }

    public void setDate(List<HisItem> hisItems){
        this.hisItems.clear();
        if (hisItems != null) {
            this.hisItems.addAll(hisItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position < hisItems.size()) {
            View view = null;
            ItemHolder itemHolder = new ItemHolder();
            HisItem hisItem = hisItems.get(position);
            if (convertView!=null && convertView.getTag()!=null){
                view = convertView;
                itemHolder = (ItemHolder) view.getTag();
            }else {
                //begin view
                if (hisItem.type == DateHelper.TYPE_POSI) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_list_item, null);
                    itemHolder.tvShowName = (TextView) view.findViewById(R.id.pre_list_name);
                    itemHolder.tvShowMsg = (TextView) view.findViewById(R.id.pre_list_posi);
//                    itemHolder.tvDis = (TextView) view.findViewById(R.id.pre_list_dis);
                    itemHolder.llTouchNavi = (LinearLayout) view.findViewById(R.id.touch_to_navi);
                } else {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_list_item_1, null);
                    itemHolder.tvShowMsg = (TextView) view.findViewById(R.id.pre_list_name);
                }
                //end view
                view.setTag(itemHolder);
            }

            if (hisItem.type == DateHelper.TYPE_POSI) {
                itemHolder.tvShowName.setText(hisItem.posiName);
                itemHolder.tvShowMsg.setText(hisItem.posiArt);
                float dis = AMapUtils.calculateLineDistance(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())
                        , new LatLng(hisItem.posiLat, hisItem.posiLon));
                dis = dis / 1000f;
                DecimalFormat df = new DecimalFormat("0.0");
                String result = df.format(dis);
//                itemHolder.tvDis.setText(result + "KM");
                itemHolder.llTouchNavi.setOnClickListener(new lvButtonListener(position));

            } else {
                itemHolder.tvShowMsg.setText(hisItem.msg);
            }


            return view;
        }else {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_clear_his, null);
            return view;
        }
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
    class ItemHolder{
        TextView tvShowName;
        TextView tvShowMsg;
//        TextView tvDis;
        LinearLayout llTouchNavi;
    }
}
