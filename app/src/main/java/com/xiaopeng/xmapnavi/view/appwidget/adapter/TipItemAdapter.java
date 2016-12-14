package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amap.api.services.help.Tip;
import com.xiaopeng.xmapnavi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/10/17.
 */
public class TipItemAdapter extends ArrayAdapter {
    private List<Tip> mTip;
    public TipItemAdapter(Context context, int resource) {
        super(context, resource);
        mTip = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mTip.size();
    }

    public void setDate(List<Tip> tips){
        mTip.clear();
        mTip.addAll(tips);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ItemHolder itemHolder = new ItemHolder();
        Tip tip = mTip.get(position);
        if (convertView == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tip_list_item,null);
            itemHolder.tvShowName = (TextView) view .findViewById(R.id.pre_list_name);
            itemHolder.tvShowMsg = (TextView) view.findViewById(R.id.pre_list_posi);
            view.setTag(itemHolder);
        }else {
            view = convertView;
            itemHolder = (ItemHolder) view.getTag();
        }

        itemHolder.tvShowName.setText(tip.getName());
        itemHolder.tvShowMsg.setText(tip.getAddress());
        if (tip.getAddress()==null || "".equals(tip.getAddress())){
            itemHolder.tvShowMsg.setVisibility(View.GONE);
        }else {
            itemHolder.tvShowMsg.setVisibility(View.VISIBLE);
        }
        return view;
    }

    class ItemHolder{
        TextView tvShowName,tvShowMsg;
    }
}
