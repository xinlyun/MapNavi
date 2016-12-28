package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiaopeng.xmapnavi.R;


/**
 * Created by linzx on 2016/8/3.
 */
public class AskDialogActivity extends Activity implements View.OnClickListener,View.OnTouchListener{
    private TextView mTitleTv,mLeftBtn,mRightBtn;
    private RelativeLayout mExitTouch;
    private Bundle bundle;
    private ImageButton mImgBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ask_dialog_activity);
        initView();
    }

    private void initView(){
        mTitleTv        = (TextView) findViewById(R.id.title_dialog);
        mLeftBtn        = (TextView) findViewById(R.id.left_btn);
        mRightBtn       = (TextView) findViewById(R.id.right_btn);
        mExitTouch      = (RelativeLayout) findViewById(R.id.dialog_bg_touch);
        mImgBtn         = (ImageButton) findViewById(R.id.touch_exit);

        mLeftBtn        .setOnClickListener(this);
        mRightBtn       .setOnClickListener(this);
//        mExitTouch      .setOnClickListener(this);
        mTitleTv        .setOnClickListener(this);

        mExitTouch      .setOnTouchListener(this);
        mImgBtn         .setOnTouchListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent!=null){
            readIntent(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if(intent!=null){
            readIntent(intent);

        }
    }

    private void readIntent(Intent intent){
        Bundle bundle = intent.getBundleExtra("myown");
        this.bundle = new Bundle(bundle);
        String name = bundle.getString("EXTRA_DNAME");
        mTitleTv    .setText(name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.left_btn:
                beginNavi();
                break;
            case R.id.title_dialog:

                break;
            default:
                finish();
                break;

        }
    }

    private void beginNavi(){
        String newToast = "EXTRA_SLAT:"+bundle.getDouble("EXTRA_SLAT")+"\nEXTRA_SLON:"+bundle.getDouble("EXTRA_SLON")+"\nEXTRA_DLAT:"+bundle.getDouble("EXTRA_DLAT")
                +"\nEXTRA_DLON:"+bundle.getDouble("EXTRA_DLON")+"\nEXTRA_DNAME:"+bundle.getString("EXTRA_DNAME");
//        Toast.makeText(this, newToast, Toast.LENGTH_LONG).show();
        try {
            deletNavi.sendEmptyMessageDelayed(0, 1000);
            Intent gaodeIntent = new Intent();
            gaodeIntent.setClassName("com.autonavi.amapauto", "com.autonavi.auto.remote.fill.UsbFillActivity");
            gaodeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(gaodeIntent);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            Intent xpNaviIntent = new Intent();
            xpNaviIntent.setClassName("com.xiaopeng.xmapnavi", "com.xiaopeng.xmapnavi.view.appwidget.activity.MainActivity");
            xpNaviIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(xpNaviIntent);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    Handler deletNavi = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Intent intent1 = new Intent();
            intent1.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
            intent1.putExtra("KEY_TYPE", 10007);
            intent1.putExtra("EXTRA_SLAT", bundle.getDouble("EXTRA_SLAT"));
            intent1.putExtra("EXTRA_SLON", bundle.getDouble("EXTRA_SLON"));
            intent1.putExtra("EXTRA_SNAME", "当前位置");
            intent1.putExtra("EXTRA_DLAT",bundle.getDouble("EXTRA_DLAT"));
            intent1.putExtra("EXTRA_DLON",bundle.getDouble("EXTRA_DLON"));
            intent1.putExtra("EXTRA_DNAME",bundle.getString("EXTRA_DNAME"));
            intent1.putExtra("EXTRA_DEV", 0);
            intent1.putExtra("EXTRA_M", 4);
//            PendingIntent pIntent = PendingIntent.getBroadcast()
            sendBroadcast(intent1);

            finish();
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                mImgBtn.setBackgroundResource(R.drawable.dialog_quxiao_btn_1);
                break;
            case MotionEvent.ACTION_UP:
                mImgBtn.setBackgroundResource(R.drawable.dialog_quxiao_btn);
                finish();
                break;
        }
        return true;
    }
}
