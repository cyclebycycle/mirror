package com.example.magicmirror;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends Activity {
    private Button start;
    private TextView infoText;
    private boolean firstUpdateTime;
    private String TAG = "UpdateActivity";
    private CharSequence subInfo;

    //OTA update status
    private final int OTA_BEGIN = 0;
    private final int OTA_READY = 1;
    private final int OTA_RECEIVED = 2;
    private final int OTA_UPDATING = 3;
    private final int OTA_SUCCESS = 4;
    private final int OTA_FAIL = 5;

    private Handler mTimeHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        infoText = (TextView)findViewById(R.id.updateInfo);
        /*infoText2 = (TextView)findViewById(R.id.updateInfo2);
        infoText3 = (TextView)findViewById(R.id.updateInfo3);
        infoText4 = (TextView)findViewById(R.id.updateInfo4);
        infoText5 = (TextView)findViewById(R.id.updateInfo5);
        infoText6 = (TextView)findViewById(R.id.updateInfo6);*/

        /*mEditText = (EditText)findViewById(R.id.updateInfo);
        mEditText.setEnabled(false);
        mEditText.setFocusable(false);
        mEditText.setKeyListener(null);//重点*/

        IntentFilter filter = new IntentFilter(MainActivity.action);
        registerReceiver(broadcastReceiver,filter);

        /*mTimeHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                int status = msg.arg1;
                int time = msg.what;
                Log.d(TAG,"444444444");
                if(status == OTA_READY)
                {
                    //infoText.append("\n镜子开始接收固件...");
                    Log.d(TAG,"setText(\"\\n镜子开始接收固件...\")");
                    infoText.setText("\n镜子开始接收固件...");
                }
                else if(status == OTA_RECEIVED)
                {
                    //infoText.append("\n镜子接收固件成功！");
                    Log.d(TAG,"setText(\"\\n镜子开始接收固件...\")");
                    infoText.setText("\n镜子接收固件成功！");
                }
                else if(status == OTA_UPDATING)
                {
                    infoTextTimeUpdate(time);
                }
                else if(status == OTA_FAIL)
                {
                    //Toast.makeText(MainActivity.this,"镜子固件升级失败，请确认固件后重新升级！",Toast.LENGTH_LONG).show();
                    //infoTextTimeUpdate(0);
                    Log.d(TAG,"11111111");
                    infoText.setText("\n镜子固件升级失败，请确认固件后重新升级！");
                    infoText.setText("\n镜子即将重启系统...");

                    start.setBackgroundColor(Color.parseColor("#ffc0cb"));
                    start.setEnabled(true);
                }
                else if(status == OTA_SUCCESS)
                {
                    //Toast.makeText(MainActivity.this,"镜子固件升级成功！！！",Toast.LENGTH_LONG).show();
                    //infoTextTimeUpdate(0);
                    Log.d(TAG,"22222222");
                    infoText.setText("\n镜子固件升级成功！！！");
                    infoText.setText("\n镜子即将重启系统...");

                    start.setBackgroundColor(Color.parseColor("#ffc0cb"));
                    start.setEnabled(true);
                }
            }
        };*/

        start = (Button)findViewById(R.id.start);
        start.setBackgroundColor(Color.parseColor("#ffc0cb"));
        start.setEnabled(true);
        start.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                start.setEnabled(false);
                start.setBackgroundColor(Color.parseColor("#C0C0C0"));

                infoText.setText("升级握手中...");
                firstUpdateTime = true;

                if(!MainActivity.getCommunicator().startUpdate("/sdcard/full_img.fex"))
                {
                    infoText.append("\nApp与镜子设备连接不成功，请检查网络后再试！");
                    start.setBackgroundColor(Color.parseColor("#ffc0cb"));
                    start.setEnabled(true);
                    return;
                }

            }
        });
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            int status = intent.getExtras().getInt("status");
            int time = intent.getExtras().getInt("time");
            Log.d(TAG,"onReceive:status="+status+",time="+time);
            //Message msg1 = new Message();
            //msg1.arg1 = status;
            //msg1.what = time;
            //mTimeHandler.sendMessage(msg1);
            //Log.d(TAG,"444444444");
            if(status == OTA_READY)
            {
                //infoText.append("\n镜子开始接收固件...");
                //Log.d("UpdateActivity","22222222");
                infoText.append("\n镜子开始接收固件...");
            }
            else if(status == OTA_RECEIVED)
            {
                //infoText.append("\n镜子接收固件成功！");
                //Log.d("UpdateActivity","33333333");
                infoText.append("\n镜子接收固件成功！");
            }
            else if(status == OTA_UPDATING)
            {
                infoTextTimeUpdate(time);
            }
            else if(status == OTA_FAIL)
            {
                //Toast.makeText(MainActivity.this,"镜子固件升级失败，请确认固件后重新升级！",Toast.LENGTH_LONG).show();
                //infoTextTimeUpdate(0);
                //Log.d("UpdateActivity","44444444");
                infoText.append("\n镜子固件升级失败，请确认固件和wifi网络后重新升级！！！");
                infoText.append("\n镜子即将重启系统...");

                start.setBackgroundColor(Color.parseColor("#ffc0cb"));
                start.setEnabled(true);
            }
            else if(status == OTA_SUCCESS)
            {
                //Toast.makeText(MainActivity.this,"镜子固件升级成功！！！",Toast.LENGTH_LONG).show();
                infoTextTimeUpdate(0);
                //Log.d("UpdateActivity","55555555");
                infoText.append("\n镜子固件升级成功！");
                infoText.append("\n镜子即将重启系统...");

                start.setBackgroundColor(Color.parseColor("#ffc0cb"));
                start.setEnabled(true);
            }
        }
    };

    public void sendMessage(int status, int time)
    {
        Log.d("UpdateActivity","11111111");
        Message msg1 = new Message();
        msg1.arg1 = status;
        msg1.what = time;
        mTimeHandler.sendMessage(msg1);
    }

    public void appendInfoText(int status, int time)
    {
        /*if(status == OTA_READY)
        {
            infoText.append("\n镜子开始接收固件...");
        }
        else if(status == OTA_RECEIVED)
        {
            infoText.append("\n镜子接收固件成功！");
        }
        else if(status == OTA_UPDATING)
        {
            infoTextTimeUpdate(time);
        }
        else if(status == OTA_FAIL)
        {
            //Toast.makeText(MainActivity.this,"镜子固件升级失败，请确认固件后重新升级！",Toast.LENGTH_LONG).show();
            infoTextTimeUpdate(0);
            infoText.append("\n镜子固件升级失败，请确认固件后重新升级！");
            infoText.append("\n镜子即将重启系统...");

            start.setBackgroundColor(Color.parseColor("#ffc0cb"));
            start.setEnabled(true);
        }
        else if(status == OTA_SUCCESS)
        {
            //Toast.makeText(MainActivity.this,"镜子固件升级成功！！！",Toast.LENGTH_LONG).show();
            infoTextTimeUpdate(0);
            infoText.append("\n镜子固件升级成功！！！");
            infoText.append("\n镜子即将重启系统...");

            start.setBackgroundColor(Color.parseColor("#ffc0cb"));
            start.setEnabled(true);
        }*/

    }

    public void infoTextTimeUpdate(int time)
    {
        if(firstUpdateTime)
        {
            infoText.append("\n镜子正在固件升级中...");
            subInfo = infoText.getText();
            //Log.d("UpdateActivity","66666666");
            //infoText.append("\n"+time);
            firstUpdateTime = false;
        }
        else
        {
            infoText.setText(subInfo);
            infoText.append("\n"+time);
        }

    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(start.isEnabled()) {
                finish();
                Log.e(TAG,"UpdateActivity finish,back!");
                return super.onKeyDown(keyCode, event);
            }
        }
        return true;
    }*/

    @Override
    public void onBackPressed() {
        if(start.isEnabled()) {
            finish();
            Log.e(TAG,"UpdateActivity finish,back!");
            //return super.onKeyDown(keyCode, event);
        }
        Log.e(TAG,"onBackPressed!");
    }

}
