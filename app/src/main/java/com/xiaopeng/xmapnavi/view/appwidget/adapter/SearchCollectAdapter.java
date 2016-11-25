package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/18.
 */

public class SearchCollectAdapter extends ArrayAdapter {
    private List<CollectItem> mCollectItems = new ArrayList<>();
    public SearchCollectAdapter(Context context, int resource) {
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
        SearchCollectAdapter.ViewHolder itemHolder = new SearchCollectAdapter.ViewHolder();
        CollectItem collectItem = mCollectItems.get(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_history_item, null);
            itemHolder.mTxName = (TextView) view.findViewById(R.id.pre_list_name);
            itemHolder.mTxPoi = (TextView) view.findViewById(R.id.pre_list_posi);
            view.setTag(itemHolder);
        } else {
            view = convertView;
            itemHolder = (SearchCollectAdapter.ViewHolder) view.getTag();
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
