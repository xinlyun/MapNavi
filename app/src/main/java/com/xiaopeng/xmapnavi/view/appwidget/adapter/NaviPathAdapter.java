package com.xiaopeng.xmapnavi.view.appwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.navi.model.AMapNaviPath;
import com.xiaopeng.xmapnavi.R;

import java.util.HashMap;
import java.util.zip.Inflater;

/**
 * Created by linzx on 2016/10/17.
 */
public class NaviPathAdapter extends ArrayAdapter{

    private HashMap<Integer,AMapNaviPath> viewHashMap;
    private String[] typeStrs ;
    private int[] ints;

    private int index = 0;
    private Context mContext;
    public NaviPathAdapter(Context context, int resource) {
        super(context, resource);
        viewHashMap = new HashMap<>();
        mContext = context;

    }

    public void clickOne(int id){

    }

    public void setDate(HashMap<Integer,AMapNaviPath> hashMap,int[] ints){
        this.ints = ints;
        if (viewHashMap == null){
            viewHashMap = new HashMap<>();
        }
        viewHashMap.clear();
        viewHashMap.putAll(hashMap);
        typeStrs = new String[hashMap.size()];
        for (int i = 0 ;i<hashMap.size();i++){
            typeStrs[i] = "方案"+(i+1);
        }
        try {
            int wasInt = getMustWayLength(hashMap, ints);
            typeStrs[wasInt] = "路程最短";
            wasInt = getMustFastWay(hashMap, ints);
            typeStrs[wasInt] = "用时最短";
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getMustWayLength(HashMap<Integer,AMapNaviPath> hashMap,int[] ints){
        int k = 0;
        int pathLenght = hashMap.get(ints[0]).getAllLength();
        for (int i = 1 ;i<ints.length;i++){
            if ( pathLenght > hashMap.get(ints[i]).getAllLength()){
                pathLenght = hashMap.get(ints[i]).getAllLength();
                k = i;
            }
        }
        return k;
    }
    private int getMustFastWay(HashMap<Integer,AMapNaviPath> hashMap,int[] ints){
        int k = 0;
        int time = hashMap.get(ints[0]).getAllTime();
        for (int i = 1 ;i<hashMap.size();i++){
            if ( time > hashMap.get(ints[i]).getAllLength()){
                time = hashMap.get(ints[i]).getAllLength();
                k = i;
            }
        }
        return k;
    }


    @Override
    public int getCount() {
        return viewHashMap.size();
    }
    @Override
    public void clear() {
        viewHashMap.clear();
        super.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ItemHolder itemHolder = new ItemHolder();
        AMapNaviPath path = viewHashMap.get(ints[position]);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_gradview_item,null);
            itemHolder.tvShowType = (TextView) view .findViewById(R.id.tv_type_show);
            itemHolder.tvShowMsg = (TextView) view.findViewById(R.id.tv_msg_show);
            itemHolder.tvShowDis = (TextView) view.findViewById(R.id.tv_type_show_2);
            view.setTag(itemHolder);
        }else {
            view = convertView;
            itemHolder = (ItemHolder) view.getTag();
        }

        if (index == position){
            view.setBackgroundColor(getContext().getResources().getColor(R.color.white));
        }else {
            view.setBackground(null);
        }
        itemHolder.tvShowType.setText(typeStrs[position]);

        String hourStr,minStr,secStr;
        int hour = path.getAllTime()/3600;
        if (hour == 0) {
            hourStr ="";
        }else {
            hourStr = ""+hour+"小时";
        }
        int min = path.getAllTime()/60 % 60;
        if (min ==  0){
            minStr = "";
        }else {
            minStr  = ""+min+"分";
        }
        secStr = ""+path.getAllTime()/60+"秒";
        String timeString = hourStr+minStr;

//        +secStr;

        String lengthKm,lengthM;
        int Km = path.getAllLength()/1000;
        if (Km == 0){
            lengthKm = "";
        }else {
            lengthKm = ""+Km+"公里";
        }
        lengthM = ""+path.getAllLength()%1000+"米";
        String lengthStr = lengthKm+lengthM;

        String allMsg = timeString+"\n"+lengthStr;
        itemHolder.tvShowMsg.setText(timeString);
        itemHolder.tvShowDis.setText(lengthStr);
        if (index == position){
            itemHolder.tvShowDis.setTextColor(mContext.getResources().getColor(R.color.blue));
            itemHolder.tvShowType.setTextColor(mContext.getResources().getColor(R.color.blue));
            itemHolder.tvShowMsg.setTextColor(mContext.getResources().getColor(R.color.blue));
        }else {
            itemHolder.tvShowDis.setTextColor(mContext.getResources().getColor(R.color.gray_btn_bg_pressed_color));
            itemHolder.tvShowType.setTextColor(mContext.getResources().getColor(R.color.gray_btn_bg_pressed_color));
            itemHolder.tvShowMsg.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        return view;
    }

    public void setIndex(int index){
        this.index = index;
        this.notifyDataSetChanged();
    }

    class ItemHolder{
        TextView tvShowType,tvShowMsg,tvShowDis;
    }
}
