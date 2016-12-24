package com.xiaopeng.xmapnavi.mode;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.xiaopeng.lib.scu.NcmControlBox;
import com.xiaopeng.lib.utils.utils.LogUtils;
import com.xiaopeng.xmapnavi.presenter.IRunBroadInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by xinlyun on 16-5-24.
 */
public class SoloNaviInfo implements IRunBroadInfo,GuideInfoExtraKey {
    private static final String TAG  = "SoloNaviInfo";
    Context context;
    NcmControlBox ncmControlBox;
    HashMap<Integer,String> hashMap;
    int msgId=0;
    public SoloNaviInfo(Context context){
        this.context = context;
        hashMap = new HashMap<>();
    }
    public SoloNaviInfo(Context context, NcmControlBox ncmControlBox){
        this.context = context;
        this.ncmControlBox = ncmControlBox;
        hashMap = new HashMap<>();
    }
    @Override
    public void readIntentInfo(Intent intent) {
        int keyType = intent.getIntExtra("KEY_TYPE", 1000);
        if (keyType == 10001) {
//            LogUtils.d(TAG, "----------------------------------");
//            LogUtils.d(TAG, ROUTE_REMAIN_DIS + intent.getIntExtra(ROUTE_REMAIN_DIS, 0));
//            LogUtils.d(TAG, ROUTE_REMAIN_TIME + intent.getIntExtra(ROUTE_REMAIN_TIME, 0));
//            LogUtils.d(TAG, CUR_ROAD_NAME + intent.getStringExtra(CUR_ROAD_NAME));
//            LogUtils.d(TAG, NEXT_ROAD_NAME + intent.getStringExtra(NEXT_ROAD_NAME));
//            LogUtils.d(TAG, ICON + intent.getIntExtra(ICON, 0));
//            LogUtils.d(TAG, SEG_REMAIN_DIS + intent.getIntExtra(SEG_REMAIN_DIS, 0));
//            LogUtils.d(TAG, CAMERA_TYPE + intent.getIntExtra(CAMERA_TYPE, 0));
//            LogUtils.d(TAG, CAMERA_INDEX + intent.getIntExtra(CAMERA_SPEED, 0));
//            LogUtils.d(TAG, CAMERA_DIST + intent.getIntExtra(CAMERA_DIST, 0));
//            LogUtils.d(TAG, "----------------------------------");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("SegRemainDis",""+intent.getIntExtra(ROUTE_REMAIN_DIS,0));
                jsonObject.put("SegRemaintime",""+intent.getIntExtra(ROUTE_REMAIN_TIME,0));
                jsonObject.put("CurRoadName",intent.getStringExtra(CUR_ROAD_NAME));
                jsonObject.put("NexRoadName",intent.getStringExtra(NEXT_ROAD_NAME));
                int icon = getNewNaviIcon(intent.getIntExtra(ICON,0));
                jsonObject.put("NavIcon",icon);
                jsonObject.put("RoadRemainDis",""+intent.getIntExtra(SEG_REMAIN_DIS,0));
                int cameraType = intent.getIntExtra(CAMERA_TYPE,-1);
                if(cameraType==-1){
                    jsonObject.put("CameraType","");
                }else {
                    jsonObject.put("CameraType",""+cameraType);
                    jsonObject.put("CameraSpeed",""+intent.getIntExtra(CAMERA_SPEED,0));
                    jsonObject.put("CameraDis",""+intent.getIntExtra(CAMERA_DIST,0));
                }
                jsonObject.put("LimitedSpeed",""+intent.getIntExtra(LIMITED_SPEED,0));
                if(icon==9||icon==10){
                    jsonObject.put("RoadAllNum",""+intent.getIntExtra(ROUND_ALL_NUM,0));
                    jsonObject.put("RoadAboutNum",""+intent.getIntExtra(ROUND_ABOUT_NUM,0));
                }
                JSONObject data = new JSONObject();
                data.put("nav",jsonObject);
                data.put("msgtype","1");
                data.put("enable","1");
//                LogUtils.d(TAG,"json:\n"+data.toString());
                Message msg = handler.obtainMessage();
                msg.obj = data.toString();
                handler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(keyType == 10019){
            int extraState = intent.getIntExtra("EXTRA_STATE", 0);
            LogUtils.d("from Navi", " keyType:" + keyType + "  extraState:" + extraState);
            if(extraState==9||extraState==12){
                JSONObject data = new JSONObject();
                try {
                    data.put("enable","0");
                    data.put("msgtype","1");
                    Message msg = handler.obtainMessage();
                    msg.obj = data.toString();
                    handler.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            msgId = (msgId+1)%128;
            hashMap.put(msgId,str);
            ncmControlBox.sendNavMessage(msgId,str.getBytes(),null);
        }
    };

    @Override
    public void OnResult(int rpcNum, int result) {
        LogUtils.d("OnResult","rpcNum:"+rpcNum+"   result:"+result);
        if(result==0){
            hashMap.remove(rpcNum);
        }else {
            String msg = hashMap.get(rpcNum);
            ncmControlBox.sendNavMessage(rpcNum,msg.getBytes(),null);
        }
    }

    private int getNewNaviIcon(int icon){
        if(icon<=9&&icon>=2){
            return icon-1;
        }
        else if(icon==1||icon==10){
            return 8;
        }
        else if(icon==11||icon==12){
            return icon -2;
        }
        else if(icon>=14&&icon<=16 ){
            return icon-3;
        }
        return 8;
    }
}
