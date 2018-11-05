package com.example.magicmirror;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.magicmirror.bean.Login;
import com.example.magicmirror.internet.GitHubService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends Activity implements View.OnClickListener{
    private GitHubService service;
    private Button bt_login;
    private Button bt_register;
    private EditText et_username;
    private EditText et_password;
    public static String username;//用户名
    public static String mirror_id;//镜子id
    private long exitTime = 0;

    // 创建一个复杂更新进度的Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://该用户名不存在
                    et_username.setText("");
                    et_password.setText("");
                    AlertDialog.Builder builder0=new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败").setMessage("该用户名不存在");
                    builder0.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder0.create().show();
                    break;
                case 1://用户名与密码不匹配
                    et_username.setText("");
                    et_password.setText("");
                    AlertDialog.Builder builder1=new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败").setMessage("用户名与密码不匹配");
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.create().show();
                    break;
                case 2:
                    AlertDialog.Builder builder2=new AlertDialog.Builder(LoginActivity.this).setTitle("登录失败").setMessage("请检查网络，服务器访问失败");
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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);
        bt_login = (Button)findViewById(R.id.bt_login);
        bt_register = (Button)findViewById(R.id.bt_register);
        et_username = (EditText)findViewById(R.id.et_username);
        et_password = (EditText)findViewById(R.id.et_password);
        bt_login.setOnClickListener(this);
        bt_register.setOnClickListener(this);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MainActivity.url).addConverterFactory(GsonConverterFactory.create()).build();
        service = retrofit.create(GitHubService.class);
    }

    public void onClick(final View view){
        switch (view.getId()){
            case R.id.bt_login://登录
                final String usernameInput=et_username.getText().toString().trim();//用户名
                String passwordInput=et_password.getText().toString().trim();//密码
//                异步执行
                service.AndroidLogin(usernameInput,passwordInput).enqueue(new Callback<Login>() {
                    @Override
                    public void onResponse(Call<Login> call, retrofit2.Response<Login> response) {
                        System.out.println("访问成功");
                        Login data=response.body();
                        System.out.println("数据："+data.getStatus());
                        Message msg = new Message();
                        switch (data.getStatus()){
                            case 0://该用户名不存在
                                msg.what = 0;
                                handler.sendMessage(msg);
                                break;
                            case 1://用户名与密码不匹配
                                msg.what = 1;
                                handler.sendMessage(msg);
                                break;
                            case 2://登录成功
                                username=usernameInput;
                                mirror_id=data.getMirror_id();
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }
//                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
//                        startActivity(intent);
                    }
                    @Override
                    public void onFailure(Call<Login> call, Throwable t) {
                        System.out.println("访问失败"+t.getMessage());
                        Message msg1 = new Message();
                        msg1.what = 2;
                        handler.sendMessage(msg1);
                    }
                });


//
//                File file=new File("/storage/emulated/0/DCIM/Camera/photo.jpg");
//                RequestBody photo = RequestBody.create(MediaType.parse("image/jpg"), file);
//                MultipartBody.Part filePart = MultipartBody.Part.createFormData("photo", "photo.jpg", photo);
//
//                System.out.println("图片内容内容"+file.length());
//                System.out.println("图片内容"+photo);
//                MediaType textType = MediaType.parse("text/plain");
//                RequestBody name = RequestBody.create(textType, usernameInput);
//                RequestBody pa = RequestBody.create(textType, "123456");
//                RequestBody time = RequestBody.create(textType, "2017-09-28 11:33");
////                RequestBody pho = RequestBody.create(textType, "123");
//
//               service.PiUpload(name,pa,filePart,time).enqueue(new Callback<String>() {
//                    @Override
//                    public void onResponse(Call<String> call, Response<String> response) {
//                        System.out.println("photo访问成功");
//                        System.out.println("图片返回数据："+response.body());
//                    }
//
//                    @Override
//                    public void onFailure(Call<String> call, Throwable t) {
//                        System.out.println("photo访问失败");
//                        System.out.println("图片返回失败数据："+t.getMessage());
//                    }
//                });
                break;
            case R.id.bt_register://注册
                Intent intent2 = new Intent(LoginActivity.this,RegisterAcitivity.class);
                startActivity(intent2);
                finish();
                break;
        }

    }

    /**
     * 返回键
     * @param keyCode
     * @param event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 连续点击两次返回键退出应用
     */
    private void exit(){
        if((System.currentTimeMillis() - exitTime)>2000){
            Log.e("再按一次退出程序","app");
            Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_LONG).show();
            exitTime = System.currentTimeMillis();
            Log.e("exitTime","app");
        }else {
            finish();
            Log.e("退出","app");
            System.exit(0);
        }
    }

}
