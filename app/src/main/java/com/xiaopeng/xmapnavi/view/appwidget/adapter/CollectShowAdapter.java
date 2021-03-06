package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.bean.CollectItem;
import com.xiaopeng.xmapnavi.bean.HisItem;
import com.xiaopeng.xmapnavi.mode.DateHelper;
import com.xiaopeng.xmapnavi.presenter.callback.OnClickRightItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linzx on 2016/11/8.
 */
public class CollectShowAdapter extends ArrayAdapter {
    private List<CollectItem> mCollectItems = new ArrayList<>();
    private OnClickRightItem mOnClickRightItem;
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
            itemHolder.mImageView = (ImageView) view.findViewById(R.id.touch_to_navi);
            view.setTag(itemHolder);
        } else {
            view = convertView;
            itemHolder = (ViewHolder) view.getTag();
        }
        itemHolder.mTxName.setText(collectItem.pName);
        itemHolder.mTxPoi.setText(collectItem.pDesc);
        if (collectItem.pDesc==null || "".equals(collectItem.pDesc)){
            itemHolder.mTxPoi.setVisibility(View.GONE);
        }else {
            itemHolder.mTxPoi.setVisibility(View.VISIBLE);
        }
        itemHolder.mImageView.setOnClickListener(new lvButtonListener(position));
        return view;
    }

    class ViewHolder {
        TextView mTxName;
        TextView mTxPoi;
        ImageView mImageView;
    }

    public void setRightLisener(OnClickRightItem lisener){
        this.mOnClickRightItem = lisener;
    }

    class lvButtonListener implements View.OnClickListener {
        private int position ;

        lvButtonListener(int pos) {
            position = pos;
        }

        @Override
        public void onClick(View v) {
            if (mOnClickRightItem != null) {
                int posi1 = position;
                mOnClickRightItem.onClickRightItem(posi1);
            }
        }
    }
}
