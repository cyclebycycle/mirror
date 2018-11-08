package com.example.magicmirror;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anton46.stepsview.StepsView;
import com.anton46.stepsview.StepsViewIndicator;
import com.example.magicmirror.bean.PhotoMessage;
import com.example.magicmirror.bean.ResultInfo;
import com.example.magicmirror.internet.GitHubService;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.magicmirror.R.id.get_photo;


public class StepViewActivity extends AppCompatActivity {
    private final static String url="http://panhe-tech.cn/index.php/Home/Android/";
    private GitHubService service;
    private final String[] views = {"油分"};
    public final static String[] label1 = {"0","1","2","3","4","5","6","7"};
    public final static String[] label2 = {"0","1","2"};
    public final static String[] label3 = {"0","1","2","3","4","5"};
    public final static String[] label4 = {"0","1","2","3"};
//    public static int[] showTag = new int[7];
    private TextView gender;
    private TextView age;
    private String w_gender;

    private ImageView get_photo;
//    public static int flag =0;


    // 创建一个复杂更新进度的Handler
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://该用户名不存在
                    AlertDialog.Builder builder0=new AlertDialog.Builder(StepViewActivity.this).setTitle("图片信息获取失败").setMessage("该用户未上传图片");
                    builder0.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder0.create().show();
                    break;
                case 1:
                    AlertDialog.Builder builder2=new AlertDialog.Builder(StepViewActivity.this).setTitle("访问失败").setMessage("请检查网络，服务器访问失败");
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
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_stepview);

        get_photo=(ImageView)findViewById(R.id.get_photo);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(StepViewActivity.url).addConverterFactory(GsonConverterFactory.create()).build();
        service = retrofit.create(GitHubService.class);

        gender = (TextView)findViewById(R.id.tv_gender);
        age = (TextView)findViewById(R.id.tv_age);
        System.out.println("用户性别：" + String.valueOf(MainActivity.tr_gender));
        System.out.println("用户年龄: " + String.valueOf(MainActivity.tr_age));
        for(int i = 0; i<7; i++){
            System.out.println( "第"+i+"个：" +MainActivity.showTag[i]);
        }
        if(MainActivity.tr_gender == 0){
             w_gender = "男";
        }else{
             w_gender = "女";
        }
        gender.setText(w_gender);
        age.setText(String.valueOf(MainActivity.tr_age));

        if(MainActivity.flag == 1){
            MainActivity.flag = 0;
            android.support.v7.app.AlertDialog.Builder builder1=new android.support.v7.app.AlertDialog.Builder(StepViewActivity.this).setTitle("提示信息").setMessage("拍照成功\n" + "请查看结果");
            builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder1.setCancelable(false);
            builder1.create();
            builder1.show();

        }else if(MainActivity.flag == 2){
            MainActivity.flag = 0;
            android.support.v7.app.AlertDialog.Builder builder1=new android.support.v7.app.AlertDialog.Builder(StepViewActivity.this).setTitle("提示信息").setMessage("未检测到结果\n" + "请重新拍摄");
            builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder1.setCancelable(false);
            builder1.create();
            builder1.show();
        }

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
                if (data == null) {
                    System.out.println("hahahhahahah");
                }
                System.out.println("数据：" + data.getStates() + data.getTime());
                Message msg = new Message();
                switch (data.getStates()) {
                    case 0://该用户名不存在
                        msg.what = 0;
                        handler.sendMessage(msg);
                        break;
                    case 1://登录成功
//                        System.out.println("wwwww"+data.getResult());
//                        Gson gson = new Gson();
//                        ResultInfo result = new ResultInfo();
//                        String jason = data.getResult();
//                        result = gson.fromJson(jason,ResultInfo.class);
                        //显示照片
                        String photoUrl=data.getPhotoAdd();
                        getPhoto(photoUrl);
                        System.out.println("图片路径"+photoUrl);


//                        if(data.getResult() != null){
//                            System.out.println("性别" +String.valueOf(result.getGender())+"年龄"+String.valueOf(result.getAge()));
//                            tv_gender.setText(String.valueOf(result.getGender()));
//                            tv_age.setText(String.valueOf(result.getAge()));
//                            System.out.println("肤色" +String.valueOf(result.getColor_level())+"色调"+String.valueOf(result.getHue_level()));
//                            showTag[0] = result.getColor_level();
//                            showTag[1] = result.getHue_level();
//                            showTag[2] = result.getOil_level();
//                            showTag[3] = result.getSmooth_level();
//                            showTag[4] = result.getAcne_level();
//                            showTag[5] = result.getPore_level();
//                            showTag[6] = result.getBlack_level();
//                            for(int i = 0; i<7; i++) {
//                                System.out.println("hhhhh" + showTag[i]);
//                            }

                        break;
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

        ListView mListView = (ListView) findViewById(R.id.list);

        MyAdapter adapter = new MyAdapter(StepViewActivity.this, 0);
        adapter.addAll(views);

        mListView.setAdapter(adapter);

    }


    public void getPhoto(String address){
        Call<ResponseBody> call = service.AndroidGetPhoto(address);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    System.out.println("长度"+response.body().contentLength());
                    System.out.println( "server contacted and has file");
                    boolean writtenToDisk = writeResponseBodyToDisk(response.body());
                    System.out.println("file download was a success? " + writtenToDisk);
                } else {
                    System.out.println("server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println( "error");
            }
        });

    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        System.out.println("初始长度："+body.byteStream());
        try {
            byte[] data = body.bytes();
            if(data!=null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                System.out.println("图片1 "+bitmap+" "+bitmap.getWidth()+" "+bitmap.getHeight());
                if(get_photo != null) {
                    System.out.println("图片不为空");
                    get_photo.setImageBitmap(bitmap);
                    System.out.println("get_photo:" + get_photo.getHeight() + " " + get_photo.getWidth());
                }
                return true;
            }
        } catch (IOException e) {
            System.out.println("转换图片异常"+e.getMessage());
        }
        return false;
    }

    public class MyAdapter extends ArrayAdapter<String> {

        private final String[][] text = {{"0", "1", "2","3","4","5","6","7"},
                {"冷色", "中性", "暖色"},{"干性","偏干","中性","混油","偏油","油性"},
                {"0","1","2","3"},{"没有","轻度","中度","重度"},
                {"细致","粗大","较粗"},{"没有","轻度","中度","重度"}};

        public MyAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
//            holder.mLabel1.setText(getItem(0));
//            holder.gender.setText(String.valueOf(MainActivity.tr_gender));
//            holder.age.setText(String.valueOf(MainActivity.tr_age));
            for(int i = 0; i< 7; i++) {
                holder.stepsViews[i].setTextColor(getContext().getResources().getColor(R.color.main_text_color_dark3));
                holder.stepsViews[i].setSelectTextColor(getContext().getResources().getColor(R.color.start));
                System.out.println("hhhhh" + MainActivity.showTag[i]);
                holder.stepsViews[i].setCompletedPosition(MainActivity.showTag[i])
                        .setLabels(text[i])
                        .setBarColorIndicator(
                                getContext().getResources().getColor(R.color.darkgray))
                        .setProgressColorIndicator(getContext().getResources().getColor(R.color.start))
                        .setNumberTextSelectColor(getContext().getResources().getColor(R.color.white))
                        .setNumberTextDefaultColor(getContext().getResources().getColor(R.color.darkgray))
                        .setCircleSelectColor(getColorWithAlpha(getContext().getResources().getColor(R.color.start), 0.4f))
                        .setCircleDefaultColor(getColorWithAlpha(getContext().getResources().getColor(R.color.darkgray), 0.4f))
                        .drawView();
            }
            return convertView;
        }

        public int getColorWithAlpha(int color, float ratio) {
            int newColor = 0;
            int alpha = Math.round(Color.alpha(color) * ratio);
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            newColor = Color.argb(alpha, r, g, b);
            return newColor;
        }

        class ViewHolder {
//            TextView gender;
//            TextView age;
//            TextView mLabel1;
//            TextView mLabel2;
            StepsView[] stepsViews = new StepsView[7];

            public ViewHolder(View view) {
//                gender = (TextView) view.findViewById(R.id.tv_gender);
//                age = (TextView) view.findViewById(R.id.tv_age);
                stepsViews[0] = (StepsView) view.findViewById(R.id.stepsView1);
                stepsViews[0].mStepsViewIndicator.updateLabels(label1);
                stepsViews[1] = (StepsView) view.findViewById(R.id.stepsView2);
                stepsViews[1].mStepsViewIndicator.updateLabels(label2);
                stepsViews[2] = (StepsView) view.findViewById(R.id.stepsView3);
                stepsViews[2].mStepsViewIndicator.updateLabels(label3);
                stepsViews[3] = (StepsView) view.findViewById(R.id.stepsView4);
                stepsViews[3].mStepsViewIndicator.updateLabels(label4);
                stepsViews[4] = (StepsView) view.findViewById(R.id.stepsView5);
                stepsViews[4].mStepsViewIndicator.updateLabels(label4);
                stepsViews[5] = (StepsView) view.findViewById(R.id.stepsView6);
                stepsViews[5].mStepsViewIndicator.updateLabels(label2);
                stepsViews[6] = (StepsView) view.findViewById(R.id.stepsView7);
                stepsViews[6].mStepsViewIndicator.updateLabels(label4);

            }
        }
    }
    //改写物理按键——返回的逻辑
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            flag = 1;
            //Intent intent=new Intent(StepViewActivity.this,MainActivity.class);
            //startActivity(intent);
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

}