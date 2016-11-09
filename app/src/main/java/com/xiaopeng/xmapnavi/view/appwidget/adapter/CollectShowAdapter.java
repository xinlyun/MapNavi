package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/8.
 */
public class CollectShowAdapter extends ArrayAdapter {
    private List<CollectItem> mCollectItems = new ArrayList<>();
    public CollectShowAdapter(Context context, int resource) {
        super(context, resource);
    }

    public void setData(List<CollectItem> collectItems){
        mCollectItems .clear();
        mCollectItems.addAll(collectItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCollectItems.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder itemHolder = new ViewHolder();
        CollectItem collectItem = mCollectItems.get(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_fix_collect, null);
            itemHolder.mTxName = (TextView) view.findViewById(R.id.pre_list_name);
            itemHolder.mTxPoi = (TextView) view.findViewById(R.id.pre_list_posi);
            view.setTag(itemHolder);
        } else {
            view = convertView;
            itemHolder = (ViewHolder) view.getTag();
        }
        itemHolder.mTxName.setText(collectItem.pName);
        itemHolder.mTxPoi.setText(collectItem.pDesc);

        return view;
    }

    class ViewHolder {
        TextView mTxName;
        TextView mTxPoi;
    }
}
