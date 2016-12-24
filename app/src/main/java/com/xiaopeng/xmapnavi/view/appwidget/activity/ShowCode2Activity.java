package com.xiaopeng.xmapnavi.view.appwidget.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.xiaopeng.xmapnavi.R;
import com.xiaopeng.xmapnavi.XpApplication;
import com.xiaopeng.xmapnavi.utils.Utils;

/**
 * Created by linzx on 2016/9/5.
 */
public class ShowCode2Activity extends Activity implements View.OnClickListener{
    private ImageView ticketShow;
    private XpApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
// No Titlebar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_code2);
        ticketShow      = (ImageView) findViewById(R.id.ticket_show2);
        findViewById(R.id.touch_exit).setOnClickListener(this);
        app = XpApplication.sApplication;
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetCode2(app.ticket).start();
    }

    @Override
    public void onClick(View v) {
        finish();
    }

    class GetCode2 extends Thread{
        private String ticketStr ;
        public GetCode2(String msg){
            this.ticketStr = msg;
        }

        @Override
        public void run() {
            super.run();
            try {
                Bitmap bitmap = Utils.Create2DCode(ticketStr);
                Message msg = showTicketImg.obtainMessage();
                msg.what = 0;
                msg.obj = bitmap;
                showTicketImg.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    Handler showTicketImg  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Bitmap bitmap = (Bitmap) msg.obj;
                ticketShow.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };
}
