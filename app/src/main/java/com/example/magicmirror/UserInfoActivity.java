package com.example.magicmirror;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;


import com.example.magicmirror.adapter.PhotoInfoAdapter;
import com.example.magicmirror.bean.Photo;
import com.example.magicmirror.bean.PhotoResult;
import com.example.magicmirror.internet.GitHubService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInfoActivity extends AppCompatActivity {
    private String url="http://118.31.46.177/index.php/Home/AndroidShop/";
    private GitHubService service;
    private ListView lv_photos_info;
    private PhotoInfoAdapter photoInfoAdapter;
    private TextView tv_client;

    // 创建一个复杂更新进度的Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://该用户名不存在
                    photoInfoAdapter.clear();
                    AlertDialog.Builder builder0=new AlertDialog.Builder(UserInfoActivity.this).setTitle("登录失败").setMessage("该用户名不存在");
                    builder0.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder0.create().show();
                    break;
                case 1://表示该店没有客户
                    photoInfoAdapter.clear();
                    AlertDialog.Builder builder1=new AlertDialog.Builder(UserInfoActivity.this).setTitle("该客户未拍照").setMessage("该店没有客户信息");
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.create().show();
                    break;
                case 2:
                    AlertDialog.Builder builder2=new AlertDialog.Builder(UserInfoActivity.this).setTitle("登录失败").setMessage("请检查网络，服务器访问失败");
                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder2.create().show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_info);

        tv_client=(TextView)findViewById(R.id.tv_client);
        lv_photos_info=(ListView)findViewById(R.id.lv_photos_info);
        photoInfoAdapter=new PhotoInfoAdapter(this);
        lv_photos_info.setAdapter(photoInfoAdapter);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
        service = retrofit.create(GitHubService.class);
    }


    @Override
    protected void onResume() {
        super.onResume();
        String username =getIntent().getExtras().get("用户名").toString();
        System.out.println("用户名："+username);
        tv_client.setText(username);
        //                异步执行
        service.AndroidShopClientPhotoGet(username).enqueue(new Callback<PhotoResult>() {
            @Override
            public void onResponse(Call<PhotoResult> call, retrofit2.Response<PhotoResult> response) {
                System.out.println("访问成功");
                PhotoResult data=response.body();
                System.out.println(data+"数据："+data.getStatus());
                Message msg = new Message();
                switch (data.getStatus()){
                    case 0://该用户名不存在
                        msg.what = 0;
                        handler.sendMessage(msg);
                        break;
                    case 1://表示该店没有客户
                        msg.what = 1;
                        handler.sendMessage(msg);
                        break;
                    case 2://表示返回成功
                        photoInfoAdapter.clear();
                        ArrayList<Photo> photos=data.getData();
                        System.out.println("大小："+photos.size());
                        for(int i=0;i<photos.size();i++){
                            System.out.println("时间："+photos.get(i).getTime()+" "+photos.get(i).getResult());
                            photoInfoAdapter.addphoto(photos.get(i));
                            photoInfoAdapter.notifyDataSetInvalidated();
                        }
                        break;
                }
            }
            @Override
            public void onFailure(Call<PhotoResult> call, Throwable t) {
                System.out.println("访问失败"+t.getMessage());
                Message msg1 = new Message();
                msg1.what = 2;
                handler.sendMessage(msg1);
            }
        });
    }

    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent=new Intent(UserInfoActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }
}
