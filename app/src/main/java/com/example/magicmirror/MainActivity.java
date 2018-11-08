package com.example.magicmirror;
import com.example.magicmirror.R;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magicmirror.Protocol.CRC8;
import com.example.magicmirror.UDPSocket.UDPMultiClient;
import com.example.magicmirror.UDPSocket.UDPMultiServer;
import com.example.magicmirror.UDPSocket.UDPServer;
import com.example.magicmirror.bean.PhotoMessage;
import com.example.magicmirror.bean.ResultInfo;
import com.example.magicmirror.bean.WifiAdmin;
import com.example.magicmirror.internet.GitHubService;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.DatagramPacket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.magicmirror.R.id.bond;
import static java.lang.Thread.sleep;


public  class MainActivity extends AppCompatActivity {
    public final static String url="http://panhe-tech.cn/index.php/Home/Android/";
    private long exitTime = 0;
    private Context mContext;
    private GitHubService service;
    public LoginActivity loginActivity;
    //private Broadcaster broadcast;
    //private WifiConfigReceiver wifiReceiver;
    private String TAG = "MainActivity";

    //UI控件
    private Toolbar toolbar;
    private TextView ssidText;
    private TextView passwordText;
    private TextView ip;
//  private EditText information;
    private Button sendButton;
    private Button bond;
    private Button wifiConfig;
//    private TextView recevie;
    private Button bt_refresh;
    private Button bt_history;
    private ImageView get_photo;
    public static int flag = 0;

    //结果信息
    public static int[] showTag = new int[7];
    public static int tr_gender;
    public static int tr_age;


    //UDP通信
    private UDPServer udpServer;
    private UDPMultiClient udpClient = null;
    private UDPMultiServer udpMultiServer;

    private Handler postHandler;
    private Runnable PostRunnable;
    private Thread configThread;
    private Handler bondHandler;
    private Runnable bondRunnable;
    private boolean isDoingBind;
    private boolean isDoingConfigWifi;
    private static Communicator comm;
    private Button updateButton;

    //private TextView mOffTextView;
    //private Handler mOffHandler;
    //private Timer mOffTime;
    //private Dialog mDialog;
    //private AlertDialog.Builder updateBuilder;

    public static final String action = "jason.broadcast.action";

    //信息解析测试变量
    WifiInformation wifiInfo;
//    UserInformation userInfo;
    private ArrayList<Integer> cache;
    int running = 0;
    int SocketEnable = 0;
    int interval =200;

//    private int flag = 0;
//    final Handler serverHandler = new Handler() {
//        public void handleMessage(Message msg){
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
//            String time = simpleDateFormat.format(new java.util.Date());
//            String str = msg.getData().getString("info");
//            information.append(time+"||"+str+"\n");
//        }
//    };

    // 创建一个复杂更新进度的Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://该用户名不存在
                    AlertDialog.Builder builder0=new AlertDialog.Builder(MainActivity.this).setTitle("图片信息获取失败").setMessage("该用户未上传图片");
                    builder0.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder0.create().show();
                    break;
                case 1:
                    AlertDialog.Builder builder2=new AlertDialog.Builder(MainActivity.this).setTitle("访问失败").setMessage("请检查网络，服务器访问失败");
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

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static Communicator getCommunicator()
    {
        return comm;
    }

    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Log.e(TAG,"======onCreate=====!!!");
        init();

        verifyStoragePermissions(this);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    void initUpdateDialog(){
        /*mOffTextView = new TextView(this);
        mOffTextView.setText("镜子固件升级握手中...");
        mDialog = new AlertDialog.Builder(this)
                .setTitle("固件升级")
                //.setCancelable(false)
                .setView(mOffTextView) ////

                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mOffTime.cancel();
                    }
                })
                .create();

        Log.e(TAG,"========MainActivity mDialog.show======");
        mDialog.show();
        Log.e(TAG,"========MainActivity after mDialog.show======");
        dialog.setCanceledOnTouchOutside(false);
        final Button btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(btn != null) {
            btn.setEnabled(false);
        }*/

        //////倒计时
        /*mOffTime = new Timer(true);
        TimerTask tt = new TimerTask() {
            int countTime = 10;
            public void run() {
                if (countTime > 0) {
                    countTime--;
                }
                Message msg = new Message();
                msg.what = countTime;
                mOffHandler.sendMessage(msg);
            }
        };
        mOffTime.schedule(tt, 1000, 1000);*/
    }

    private void init() {
        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);//工具栏
        setSupportActionBar(toolbar);

        //broadcast = new Broadcaster(this);
        //broadcast.open(34569, 34568);
        //broadcast.start();
        //wifiReceiver = new WifiConfigReceiver(this);
        //wifiReceiver.open(34567, 34566);
        //wifiReceiver.start();
        comm = new Communicator(this);
        comm.open(34569, 34568);
        comm.start();
        isDoingBind = false;
        isDoingConfigWifi = false;

        //初始化WIFI管理类
        final WifiAdmin wifiAdmin = new WifiAdmin(this);
        wifiAdmin.openWifi();
        String ssidstring = wifiAdmin.getSSID();

        //UI控件初始化
        ssidText = (TextView) findViewById(R.id.ssid);
        passwordText = (TextView) findViewById(R.id.password);
        ip = (TextView) findViewById(R.id.ip);
        sendButton = (Button) findViewById(R.id.send);
        sendButton.setBackgroundColor(Color.parseColor("#ffc0cb"));
        sendButton.setText("Wifi配网");
        sendButton.setEnabled(true);
//        wifiConfig = (Button)findViewById(R.id.wificonfig);
//        wifiConfig.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                wifiReceiver.open(34567, 34566);
//                wifiReceiver.start();
//                wifiConfig.setText("clicked");
//            }
//        });

        updateButton = (Button)findViewById(R.id.update);
        updateButton.setBackgroundColor(Color.parseColor("#ffc0cb"));
        updateButton.setText("固件升级");
        updateButton.setEnabled(true);
        updateButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                startActivity(intent);
            }
        });

        bond = (Button)findViewById(R.id.bond);
        bond.setBackgroundColor(Color.parseColor("#ffc0cb"));
        bond.setText("设备绑定");
        bond.setEnabled(true);
        bond.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                if(bond.getText()=="绑定成功"){
                    Toast.makeText(MainActivity.this,"设备已经绑定成功",Toast.LENGTH_LONG).show();
                    return;
                }else /*if(sendButton.getText()=="配网成功")*/{
                    //broadcast.setBroadCast(true);
                    if(!comm.startBind())
                    {
                        Toast.makeText(MainActivity.this,"App与镜子设备连接不成功，请检查网络后再试！",Toast.LENGTH_LONG).show();
                        return;
                    }

                    isDoingBind = true;
                    bond.setBackgroundColor(Color.parseColor("#C0C0C0"));
                    bond.setEnabled(false);
                    bond.setText("正在绑定...");
                    Log.e(TAG,"update button clicked!!!!!!");
                }

                bondRunnable=new Runnable() {
                    @Override
                    public void run() {
                        if (bond.getText() != "绑定成功")
                        {
                            android.support.v7.app.AlertDialog.Builder builder2 = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage("绑定超时，需要继续绑定请再次点击\"绑定设备\"按扭进行绑定！");
                            builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //broadcast.setBroadCast(false);//add by yanghaishan
                                    System.out.println("关闭广播");
                                    bond.setBackgroundColor(Color.parseColor("#ffc0cb"));
                                    bond.setText("设备绑定");
                                    bond.setEnabled(true);
                                    dialog.cancel();
                                }
                            });
                            builder2.setCancelable(false);
                            builder2.create();
                            builder2.show();
                            isDoingBind = false;
                        }
                    }
                };
                bondHandler = new Handler();
                bondHandler.postDelayed(bondRunnable, 60000);
            }
        });

//        information = (EditText) findViewById(R.id.information);
        loginActivity = new LoginActivity();

        if(ssidstring != "<unknown ssid>"){
            ssidText.append(ssidstring.substring(1,ssidstring.length()-1));
            passwordText.setFocusable(true);
            passwordText.requestFocus();
        }
        ip.setText("1");
        //udpMultiServer = new UDPMultiServer(this);
        //dpServer = new UDPServer(this);
        sendButton.setOnClickListener(new View.OnClickListener() {//设置监听，只有获取到焦点后才能进行此过程...
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if(sendButton.getText()=="配网成功") {//add by yanghaishan
                    Toast.makeText(MainActivity.this, "设备已经配网成功", Toast.LENGTH_LONG).show();
                }else if(passwordText.getText().toString().equals("")){
                    android.support.v7.app.AlertDialog.Builder builder1=new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage("请填写wifi密码");
                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder1.setCancelable(false);
                    builder1.create();
                    builder1.show();
                }else {
                    //设置按钮变灰并且不可点击
                    Log.e(TAG,"begin to config wifi network!!!");

                    // 初始化UDP通信的SERVER & CLIENT
                    running = 1; //1 - running;
                    isDoingConfigWifi = true;

                    if (/*running > 0 && */SocketEnable == 0) {
                        SocketEnable = 1;
                        udpClient = new UDPMultiClient();
                    }
                    try {
                        //if (running > 0) {
                        PostRunnable=new Runnable() {
                            @Override
                            public void run() {
                                if (sendButton.getText() != "配网成功")
                                {
                                    android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage("配网超时，需要继续配网请再次点击\"配网\"按钮进行配网！");
                                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            running = 0;//add by yanghaishan
                                            //wifiReceiver.setBroadCast(false);//add by yanghaishan
                                            configThread.interrupt();
                                            System.out.println("关闭广播");
                                            sendButton.setBackgroundColor(Color.parseColor("#ffc0cb"));
                                            sendButton.setText("Wifi配网");
                                            sendButton.setEnabled(true);
                                            dialog.cancel();
                                        }
                                    });
                                    builder1.setCancelable(false);
                                    builder1.create();
                                    builder1.show();
                                    isDoingConfigWifi = false;
                                }
                            }
                        };
                        postHandler = new Handler();

                        sendButton.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        sendButton.setEnabled(false);
                        sendButton.setText("正在配网...");

                        // cache = new ArrayList();
                        String password = passwordText.getText().toString();//"sw4wifionly";
                        Log.e(TAG,"password = "+password + ": length =" + password.length());
                        String ssid = ssidText.getText().toString();//"TES_TPLINK_TL_WDR5600#3232";//"林小竣";//"TES#07_NETGEAR";
                        String key = "1234567812345678";
                        //通过IP栏获取是否加密
                        String mark = ip.getText().toString();
                        String markmsg = "不需要加密";
                        if (mark.equals("1")) {
                            markmsg = "需要加密";
                        }

//                        Message message1 = new Message();
//                        Bundle bd = new Bundle();
//                        message1 = serverHandler.obtainMessage();
//                        message1.setData(bd);
//                        bd.putString("info", password.length() + ssid.length() + 6 + "个原始数据&&" + markmsg);
//                        serverHandler.sendMessage(message1);
//
                        wifiInfo = new WifiInformation(wifiAdmin.getWifiManager(), password, ssid, key, mark);
//                        Log.e("info", "info is Refresh");
//                        Message message2 = new Message();
//                        Bundle bd1 = new Bundle();
//                        message2 = serverHandler.obtainMessage();
//                        message2.setData(bd1);
//                        bd1.putString("info", wifiInfo.informationSize + "个加密数据");
//                        serverHandler.sendMessage(message2);

//                        userInfo = new UserInformation(wifiAdmin.getWifiManager(),LoginActivity.mirror_id, LoginActivity.username, key, mark);
//                        Log.e("userInfo", "userInfo is Refresh");
//                        Message message3 = new Message();
//                        Bundle bd2 = new Bundle();
//                        message3 = serverHandler.obtainMessage();
//                        message3.setData(bd2);
//                        bd1.putString("userinfo", userInfo.informationSize + "个加密数据");
//                        serverHandler.sendMessage(message3);
//
//
                            //wifiReceiver.setBroadCast(true);
                        //}
                        configThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG,"started config wifi thread!!!");
                                while (running == 1) {
                                    final int LeadCode = 4;
                                    int count = 1;
                                    byte[] SendData = new byte[wifiInfo.DataPackageSum * 3];
                                    //Log.e(TAG,"info.DataPackageSum = " + wifiInfo.DataPackageSum + "");
                                    //Log.e(TAG,"info.length" + wifiInfo.info.length + "");
                                    for (int i = 0, j = 0; i < wifiInfo.info.length; j = j + 3) {
                                        if (i < LeadCode) {
                                            SendData[j] = (byte) count;
                                            SendData[j + 1] = (byte) (wifiInfo.info[i] & 0xff);
                                            byte[] crc = new byte[1];
                                            crc[0] = (byte) (wifiInfo.info[i] & 0xff);
                                            SendData[j + 2] = CRC8.CRC8(crc);
                                            i++;
                                        } else {
                                            SendData[j] = (byte) count;
                                            SendData[j + 1] = (byte) (wifiInfo.info[i] & 0xff);
                                            SendData[j + 2] = (byte) (wifiInfo.info[i + 1] & 0xff);
                                            i = i + 2;
                                        }
                                        count++;
                                    }
                                /* for(int i =0; i < SendData.length; i++){
                                    Log.e("SendData","[" + i + "]" +SendData[i] + "");
                                }*/
                                    String[] UdpIpData = new String[3];
                                    String[] IP = new String[wifiInfo.DataPackageSum];
                                    for (int j = 0; j < (SendData.length / 3); j++) {
                                        for (int i = j * 3; i < j * 3 + 3; i = i + 3) {
                                            UdpIpData[0] = String.valueOf(SendData[i] & 0xff);
                                            UdpIpData[1] = String.valueOf(SendData[i + 1] & 0xff);
                                            UdpIpData[2] = String.valueOf(SendData[i + 2] & 0xff);
                                        }
                                        IP[j] = "239." + UdpIpData[0] + "." + UdpIpData[1] + "." + UdpIpData[2];
                                    }
                                    while (running == 1) {
                                        long current = System.currentTimeMillis();//当前时间
                                        byte[] Data = new byte[1];
                                        for (int i = 0; i < 3; i++) {
                                            for (int j = 0; j < LeadCode; j++) {
                                                udpClient.send(Data, IP[j]);
                                                //Log.e(TAG,"UdpIp[" + j + "]" + IP[j]);
                                            }
                                        }
                                        for (int i = LeadCode; i < IP.length; i++) {
                                            udpClient.send(Data, IP[i]);
                                            if ((i % 5) == 0) {
                                                udpClient.send(Data, IP[0]);
                                                //Log.e(TAG,"UdpIp[" + i + "]" + IP[0]);
                                            }
                                            //Log.e(TAG,"UdpIp[" + i + "]" + IP[i]);
                                        }
                                        long now = System.currentTimeMillis();//获取系统当前时间
                                        //Log.e(TAG,"mark" + (now - current) + "毫秒");
                                   /* Message message = new Message();
                                    Bundle bd = new Bundle();
                                    message = serverHandler.obtainMessage();
                                    message.setData(bd);
                                    bd.putString("info", (now - current) + "毫秒");
                                    serverHandler.sendMessage(message);*/
                                    }
                                    //break;
                                }
                            }
                        });
                        configThread.start();
                        postHandler.postDelayed(PostRunnable, 240000);

                        /*new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (sendButton.getText() != "配网成功")
                                {
                                    android.support.v7.app.AlertDialog.Builder builder1 = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage("请确认是否重新配网");
                                    builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            sendButton.setBackgroundColor(Color.parseColor("#3399CC"));
                                            sendButton.setText("正在配网...");
                                            sendButton.setEnabled(true);
                                            thread.interrupt();
                                            System.out.println("关闭广播");
                                            dialog.cancel();
                                        }
                                    });
                                    builder1.setCancelable(false);
                                    builder1.create();
                                    builder1.show();

                                }
                            }
                        }, 240000);*/
                        /*yanghaishan modify end*/

                        //information.append("发送数据成功.\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        //Server端服务开始运行
        /*Thread serverThread = new Thread(new Runnable() {
            public void run() {
                while(!udpServer.getIsStop()){
                    Log.e(TAG,"begin to receive data from device!!!");
                    DatagramPacket datagramPacket = udpServer.receive();
                    Log.e(TAG,"received data:" + datagramPacket.getData()[0]+"!!!");
                    if (datagramPacket.getData()[0] ==wifiInfo.randomNum){
                        running = 0;
                        //message message = new Message();
                        //Bundle bd = new Bundle();
                        //message = serverHandler.obtainMessage();
                        //message.setData(bd);
                        Log.e(TAG,"设备连接成功!!!!!!!!!!!!!!!!!!!!!!!");
                        //serverHandler.sendMessage(message);
                        sendButton.setBackgroundColor(Color.parseColor("#C0C0C0"));
                        sendButton.setText("配网成功");
                        configThread.interrupt();//add by yanghaishan
                        Toast.makeText(MainActivity.this,"设备已经配网成功",Toast.LENGTH_LONG).show();
                        sendButton.setEnabled(false);
                        return ;
                    }
                }
            }
        });
        serverThread.start();*/

//        recevie=(TextView)findViewById(R.id.recevie);
        bt_refresh = (Button) findViewById(R.id.bt_refresh);
//        bt_history = (Button)findViewById(R.id.bt_history);
        get_photo=(ImageView)findViewById(R.id.get_photo);
//        recevie.setMovementMethod(ScrollingMovementMethod.getInstance());
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MainActivity.url).addConverterFactory(GsonConverterFactory.create()).build();
        service = retrofit.create(GitHubService.class);
        //展示当前拍照结果
        bt_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*yanghaishan add,20180724*/
                if(isDoingConfigWifi || isDoingBind)
                {
                    android.support.v7.app.AlertDialog.Builder builder3 = new android.support.v7.app.AlertDialog.Builder(MainActivity.this).setTitle("提示信息").setMessage("正在进行配网或绑定设备，请稍后再查看拍照结果！");
                    builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder3.setCancelable(false);
                    builder3.create();
                    builder3.show();
                    return;
                }
                /*add end*/

                System.out.println("用户名：" + LoginActivity.username);
                System.out.println("镜子id: " + LoginActivity.mirror_id);
                String username = LoginActivity.username;
                String mirror_id = LoginActivity.mirror_id;

                //异步执行
                service.AndroidPhoto(username, mirror_id).enqueue(new Callback<PhotoMessage>() {
                    @Override
                    public void onResponse(Call<PhotoMessage> call, retrofit2.Response<PhotoMessage> response) {
                        System.out.println("访问成功");
                        PhotoMessage data = response.body();
                        System.out.println("wwwww"+data.getResult());
                        Gson gson = new Gson();
                        ResultInfo result = new ResultInfo();
                        String jason = data.getResult();
                        result = gson.fromJson(jason,ResultInfo.class);
                        Intent intent = new Intent(MainActivity.this, StepViewActivity.class);
                        startActivity(intent);
                        //finish();
                        if(data.getResult() != null) {
                            System.out.println("性别" + String.valueOf(result.getGender()) + "年龄" + String.valueOf(result.getAge()));
                            System.out.println("肤色" + String.valueOf(result.getColor_level()) + "色调" + String.valueOf(result.getHue_level()));
                            tr_gender = result.getGender();
                            tr_age = result.getAge();
                            showTag[0] = result.getColor_level();
                            showTag[1] = result.getHue_level();
                            showTag[2] = result.getOil_level();
                            showTag[3] = result.getSmooth_level();
                            showTag[4] = result.getAcne_level();
                            showTag[5] = result.getPore_level();
                            showTag[6] = result.getBlack_level();
                            flag =1;
//                            Toast.makeText(MainActivity.this, "请查看检测结果", Toast.LENGTH_LONG).show();
                        }else {
                            flag = 2;
//                            Toast.makeText(MainActivity.this, "未检测到结果，请重新拍摄...", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<PhotoMessage> call, Throwable t) {
                        System.out.println("访问失败" + t.getMessage());
                        Message msg1 = new Message();
                        msg1.what = 1;
                        handler.sendMessage(msg1);
                    }
                });

            }
        });

//        //展示历史记录
//        bt_history.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent(MainActivity.this,UserInfoActivity.class);
////                startActivity(intent);
////                finish();
//            }
//        });

    }

    @Override
    protected void onDestroy() {
        Log.e(TAG,"========MainActivity onDestroy======");
        if(udpClient != null) {
            udpClient.close();//add by yanghaishan
            udpClient = null;
        }

        //broadcast.close();
        //wifiReceiver.close();
        comm.close();

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void bindCallback()
    {
        //broadcast.setBroadCast(false);//add by yanghaishan
        bondHandler.removeCallbacks(bondRunnable);//add by yanghaishan
        System.out.print("绑定成功");
        new Thread(){
            public void run(){
                handler.post(runnable1);
            }
        }.start();

    }
    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            bond.setText("绑定成功");
            bond.setEnabled(false);
            bond.setBackgroundColor(Color.parseColor("#C0C0C0"));
            isDoingBind = false;
        }
    };

    public void wifiCallback()
    {
        running = 0;//add by yanghaishan
        //wifiReceiver.setBroadCast(false);
        if(postHandler != null) {
            postHandler.removeCallbacks(PostRunnable);//add by yanghaishan
        }
        System.out.print("wifi配置成功");
        new Thread(){
            public void run(){
                handler.post(runnable2);
            }
        }.start();

    }
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            sendButton.setBackgroundColor(Color.parseColor("#C0C0C0"));
            sendButton.setText("配网成功");
            configThread.interrupt();//add by yanghaishan
            Toast.makeText(MainActivity.this,"设备已经配网成功",Toast.LENGTH_LONG).show();
            sendButton.setEnabled(false);
            isDoingConfigWifi = false;
            //bond.setBackgroundColor(Color.parseColor("#ffc0cb"));
        }
    };

    public void updateCallback(int status,int time)
    {
        Log.e(TAG,"updateCallback:status=" + status + "time=" + time);
        Intent intent = new Intent(action);
        intent.putExtra("status", status);
        intent.putExtra("time", time);
        sendBroadcast(intent);
        //UpdateActivity.sendMessage(status, time);
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
            Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_LONG).show();
            exitTime = System.currentTimeMillis();
        }else {
            finish();
            Log.e(TAG,"app");
            System.exit(0);
        }
    }
}
