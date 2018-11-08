package com.example.magicmirror;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;




import static com.example.magicmirror.R.id.scanning_settings;
import com.example.magicmirror.zxing.android.CaptureActivity;
import com.tbruyelle.rxpermissions.RxPermissions;

import com.example.magicmirror.bean.user;
import com.example.magicmirror.internet.GitHubService;
import com.example.magicmirror.zxing.android.CaptureActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.magicmirror.R.id.scanning_settings;

public class RegisterAcitivity extends AppCompatActivity implements View.OnClickListener{



    //扫描
    private RxPermissions rxPermissions;
    private static final int SCANNING_CODE = 1;
    private String content = "";
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private Button scanning_settings;
    private Toolbar toolbar;


    private EditText et_mirrorid;
    private EditText et_re_username;
    private EditText et_re_password;
    private Button bt_upload;
    private GitHubService service;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    et_mirrorid.setText("");
                    et_re_username.setText("");
                    et_re_password.setText("");
                    AlertDialog.Builder builder0 = new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("该镜子ID已存在");
                    builder0.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder0.create().show();
                    break;
                case 1:
                    et_mirrorid.setText("");
                    et_re_username.setText("");
                    et_re_password.setText("");
                    AlertDialog.Builder builder1=new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("用户名不能为空");
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder1.create().show();
                    break;
                case 2:
                    et_mirrorid.setText("");
                    et_re_username.setText("");
                    et_re_password.setText("");
                    AlertDialog.Builder builder2=new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("密码不能为空");
                    builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder2.create().show();
                    break;
                case 3:
                    et_mirrorid.setText("");
                    et_re_username.setText("");
                    et_re_password.setText("");
                    AlertDialog.Builder builder3=new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("镜子ID不能为空");
                    builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder3.create().show();
                    break;
                case 4:
                    AlertDialog.Builder builder4 = new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册成功").setMessage("用户添加成功");
                    builder4.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Intent intent=new Intent(RegisterAcitivity.this,LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder4.create().show();
                    break;
                case 5:
                    AlertDialog.Builder builder5 = new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("该镜子不属于商店");
                    builder5.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder5.create().show();
                    break;
                case 6:
                    AlertDialog.Builder builder6 = new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("用户名已存在");
                    builder6.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder6.create().show();
                    break;
                case 7:
                    AlertDialog.Builder builder7 = new AlertDialog.Builder(RegisterAcitivity.this).setTitle("注册失败").setMessage("请检查网络，服务器访问失败");
                    builder7.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder7.create().show();
                    break;

            }
        }
    };

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_register,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.scanning_settings:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //如果已经授权就直接跳转到二维码扫面界面
                                Intent intent = new Intent(RegisterAcitivity.this, CaptureActivity.class);
                                startActivityForResult(intent, SCANNING_CODE);
                                Toast.showToast(this, "扫一扫");
                            } else { // Oups permission denied
                                Toast.showToast(this, "相机权限被拒绝，无法扫描二维码");
                                return;
                            }
                        });
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxPermissions = new RxPermissions(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        //toolbar
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        et_mirrorid = (EditText)findViewById(R.id.et_mirrorid);
        et_re_username = (EditText)findViewById(R.id.et_re_username);
        et_re_password = (EditText)findViewById(R.id.et_re_password);
        bt_upload = (Button)findViewById(R.id.bt_upload);
        bt_upload.setOnClickListener(this);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MainActivity.url).addConverterFactory(GsonConverterFactory.create()).build();
        service = retrofit.create(GitHubService.class);
    }

    public void onClick(final View view) {
            String mirror_id = et_mirrorid.getText().toString().trim();//镜子ID
            String username = et_re_username.getText().toString().trim();//用户名
            String password = et_re_password.getText().toString().trim();//密码
            System.out.println("mirror_id:" + mirror_id + " username:" + username + " password:" + password);
            //异步执行
            service.AndroidAddNewUser(mirror_id, username, password).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    System.out.println("访问成功");
                    String data = response.body();
                    System.out.println("数据1：" + response);
//                String str=data.getMirror_id()+" "+data.getPassword()+" "+data.getUsername();
                    System.out.println(response + "数据：" + data);
                    Message msg = new Message();
                    switch (data) {
                        case "0":
                            msg.what = 0;
                            handler.sendMessage(msg);//该mirror_id已存在
                            break;
                        case "1":
                            msg.what = 1;
                            handler.sendMessage(msg);//用户名不能为空
                            break;
                        case "2":
                            msg.what = 2;
                            handler.sendMessage(msg);//密码不能为空
                            break;
                        case "3":
                            msg.what = 3;
                            handler.sendMessage(msg);//镜子id不能为空
                            break;
                        case "4":
                            msg.what = 4;
                            handler.sendMessage(msg);//用户添加成功
                            break;
                        case "5":
                            msg.what = 5;
                            handler.sendMessage(msg);//该镜子不属于商店
                            break;
                        case "6":
                            msg.what = 6;
                            handler.sendMessage(msg);//用户名已存在
                            break;


                    }
                }


                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    System.out.println("访问失败" + t.getMessage());
                    Message msg = new Message();
                    msg.what = 7;
                    handler.sendMessage(msg);
                }
            });

    }

    public void customView(){
        System.out.println("设备ID"+content);
        if (TextUtils.isEmpty(content)){
            Toast.showToast(RegisterAcitivity.this,"请先扫描设备ID");
        } else {
            et_mirrorid.setText(content);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == SCANNING_CODE && resultCode == RESULT_OK) {
            if (data != null) {

                content = data.getStringExtra(DECODED_CONTENT_KEY);
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                System.out.println("扫描结果"+content);
                if (!TextUtils.isEmpty(content)){
                    customView();

                }
//                tv_scanning_result.("扫描结果： " + content);
            }
        }
    }
    //显示
    public static class Toast {
        private static android.widget.Toast toast;  //???
        public static void showToast(Context context, String content){
            if(toast==null){
                toast= android.widget.Toast.makeText(context,content, android.widget.Toast.LENGTH_SHORT);
            }else{
                toast.setText(content);
            }
            toast.show();
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
            finish();
            Intent intent=new Intent(RegisterAcitivity.this,LoginActivity.class);
            startActivity(intent);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
