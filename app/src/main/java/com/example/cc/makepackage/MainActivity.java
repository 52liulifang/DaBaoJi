package com.example.cc.makepackage;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cc.makepackage.util.HttpUtil;
import com.example.cc.makepackage.util.StringUtil;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {
    private  int MODE=1;//MODE真实值来自于Okhttp,访问服务器得到
    //因为不需要在活动中对线程进行控制，因为在StopService时会回调onDestroy，在这个函数里处理就行
    private  int tempMODE=1;
    private  boolean changeMODE=false;
    private  boolean viewmark=true;//区分显示View,真显示主，假显示副
    private String localUser;
    private String serverUser;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private TextView type;
    private TextView totalbags;
    private TextView finishbags;
    private TextView totalfloors;
    private TextView finishfloors;
    private TextView totalnormolnum;
    private TextView finishnormalnum;
    private TextView totalunnormalnum;
    private TextView finishunnormalnum;
    private TextView unnorlength1;
    private TextView unnorlength2;
    private ImageView picimg;
    private Button scan;
    private SwipeRefreshLayout swipeRefresh;

    //need updateview
    private String maintype;
    private int mainbagnum;
    private int mainfloor;
    private int mainnornum;
    private int mainunnorleg1;
    private int mainunnorleg2;
    private String vicetype;
    private int vicebagnum;
    private int vicefloor;
    private int vicenornum;
    private int viceunnorleg1;
    private int viceunnorleg2;
    private int REQUEST_CODE_SCAN = 111;
    private String Path;//图片网络地址

    //20180807，改方案为全部在服务里读取数据
    private int[] arr4=new int[4];
    private int[] arr8=new int[8];

    private Handler mMainHandler;
    private Message msg;
    private Intent intent;

    private boolean isFirstlaunch=false;//is or not first time launch

    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //这里应该有个实例
            MyService.MsgBinder binder=(MyService.MsgBinder)service;
            MyService myService=binder.getService();
            myService.setCallback(new MyService.PassValue() {
                @Override
                public void onDataChange(int[] arr) {
                    if (MODE==1){
                        arr4 =arr;
                        msg=Message.obtain();
                        msg.what = 0;
                        mMainHandler.sendMessage(msg);}

                    else {
                        arr8 =arr;
                        msg=Message.obtain();
                        msg.what = 1;
                        mMainHandler.sendMessage(msg);
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this,"更换包装规则中。。。",Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent=new Intent(MainActivity.this,MyService.class);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }//取消标题栏
        type=(TextView)findViewById(R.id.Type_Name);
        totalbags=(TextView)findViewById(R.id.Total_Num);
        finishbags=(TextView)findViewById(R.id.Finish_Num);
        totalfloors=(TextView)findViewById(R.id.Total_Floor);
        finishfloors=(TextView)findViewById(R.id.Finish_Floor);
        totalnormolnum=(TextView)findViewById(R.id.Normal_Num);
        finishnormalnum=(TextView)findViewById(R.id.Finish_Nor);
        totalunnormalnum=(TextView)findViewById(R.id.unNormal_Num);
        finishunnormalnum=(TextView)findViewById(R.id.Finish_unNor);
        unnorlength1=(TextView)findViewById(R.id.unNormal_Length_1);
        unnorlength2=(TextView)findViewById(R.id.unNormal_Length_2);
        picimg=findViewById(R.id.pic);
        scan=(Button)findViewById(R.id.scan);
        shared = getSharedPreferences("UaP", MODE_PRIVATE);
        editor= shared.edit();
        localUser=shared.getString("username","0000");
        Log.d("本地用户是：",localUser);
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        /*******************Start compare Time********************************/
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date datenow=new Date(System.currentTimeMillis());
        Date date2=null;
        //Date date1=simpleDateFormat.format(datenow);
        Date date1=null;
        try{
            date1=simpleDateFormat.parse(simpleDateFormat.format(datenow));
            Log.d("当前时间是",simpleDateFormat.format(datenow));
            date2=simpleDateFormat.parse("2019-09-13");
            Log.d("转换date2格式后",date2.getTime()+":"+date1.getTime());
        }catch (ParseException e)
        {e.printStackTrace();}
        if (date1.getTime()>date2.getTime()){
            Log.d("date1时间大于date2", "111");
        swipeRefresh.setEnabled(false);
        scan.setEnabled(false);
        }
        else{
            Log.d("date2时间大于date1","222");
        }
            /* Date nowDate=new Date(System.currentTimeMillis()+7*24*60*60*1000);
        Log.d("截止时间是",simpleDateFormat.format(nowDate));*/
        /*******************End compare Time********************************/
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //读取shareperference里的值
                //执行方法
                updateptemp();
                try { Thread.sleep(1000); }catch (Exception e){e.printStackTrace();}
                if (!StringUtil.isServiceRunning(MainActivity.this,"com.example.cc.makepackage.MyService")){
                    //如果只生产一种型号的导轨
                    if (MODE==1){
                        intent.putExtra("mode", 1);//int类,模式一
                        intent.putExtra("maintype",maintype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                        intent.putExtra("maintotalnum",mainnornum);
                        startService(intent);
                        bindService(intent,connection,BIND_AUTO_CREATE);//正常情况传入connection,因为不需要返回实例所以传入NULL
                        Log.d("启动服务","main启动服务");
                    }else {
                        intent.putExtra("mode", 2);//int类,模式二
                        intent.putExtra("maintype",maintype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                        intent.putExtra("vicetype",vicetype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                        intent.putExtra("maintotalnum",mainnornum);
                        intent.putExtra("vicetotalnum",vicenornum);
                        startService(intent);
                        bindService(intent,connection,BIND_AUTO_CREATE);

                    }
                }else {
                    if (changeMODE){
                        stopService(intent);
                        unbindService(connection);
                        //如果只生产一种型号的导轨
                        if (MODE==1){
                            intent.putExtra("mode", 1);//int类,模式一
                            intent.putExtra("maintype",maintype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                            intent.putExtra("maintotalnum",mainnornum);
                            startService(intent);
                            bindService(intent,connection,BIND_AUTO_CREATE);//正常情况传入connection,因为不需要返回实例所以传入NULL

                        }else {
                            intent.putExtra("mode", 2);//int类,模式二
                            intent.putExtra("maintype",maintype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                            intent.putExtra("vicetype",vicetype);//型号应该是在创建页面的时候访问A6得到，存贮在变量里
                            intent.putExtra("maintotalnum",mainnornum);
                            intent.putExtra("vicetotalnum",vicenornum);
                            startService(intent);
                            bindService(intent,connection,BIND_AUTO_CREATE);

                        }
                    }
                }
                if (MODE ==2){
                    if (viewmark){
                        viewmark=false;}

                    else {
                        viewmark=true;
                    }}
                swipeRefresh.setRefreshing(false);
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText(getMac(MainActivity.this));
                //textversion.setText(Build.VERSION.RELEASE);
                //updateptemp();
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                /*ZxingConfig是配置类
                 *可以设置是否显示底部布局，闪光灯，相册，
                 * 是否播放提示音  震动
                 * 设置扫描框颜色等
                 * 也可以不传这个参数
                 * */
                /*ZxingConfig config = new ZxingConfig();
                config.setPlayBeep(true);//是否播放扫描声音 默认为true
                config.setShake(true);//是否震动  默认为true
                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为淡蓝色
                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);*/
                startActivityForResult(intent, REQUEST_CODE_SCAN);
            }
        });

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what) {
                    case 0:

                        finishbags.setText("已完成"+arr4[0]+"包");
                        finishfloors.setText("当前到第"+arr4[1]+"层");
                        finishnormalnum.setText("当前已完成常规"+arr4[2]+"根");
                        finishunnormalnum.setText("当前已完成非标"+arr4[3]+"根");
                        break;
                    case 1:
                        if (!viewmark){
                            finishbags.setText("已完成"+arr8[0]+"包");
                            finishfloors.setText("当前到第"+arr8[1]+"层");
                            finishnormalnum.setText("当前已完成常规"+arr8[2]+"根");
                            finishunnormalnum.setText("当前已完成非标"+arr8[3]+"根");
                        }else {
                            finishbags.setText("已完成"+arr8[4]+"包");
                            finishfloors.setText("当前到第"+arr8[5]+"层");
                            finishnormalnum.setText("当前已完成常规"+arr8[6]+"根");
                            finishunnormalnum.setText("当前已完成非标"+arr8[7]+"根");

                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {

                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Intent intent = new Intent(MainActivity.this, bind.class);
                //把扫到并解析到的信息(既:字符串)带到详情页面
                intent.putExtra("code", content);
                intent.putExtra("type",type.getText().toString());
                startActivity(intent);
                //textView.setText("扫描结果为：" + content);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (StringUtil.isServiceRunning(MainActivity.this,"com.example.cc.makepackage.MyService")){
            stopService(intent);
            unbindService(connection);
            Toast.makeText(MainActivity.this,"停止服务",Toast.LENGTH_SHORT).show();
        }

editor.putString("pic",null);
        editor.commit();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Toast.makeText(MainActivity.this,"暂停",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    //更新临时数据,显示模式，规则等
    private void updateptemp(){
        String requesturl="http://192.168.200.96/plan.json";
        HttpUtil.sendOkHttpRequest(requesturl, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str=response.body().string();
                parseJSONWithJSONObject(str);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (viewmark){
                            Toast.makeText(MainActivity.this,"当前生产模式是"+MODE,Toast.LENGTH_SHORT).show();
                            type.setText("型号"+maintype);
                            totalbags.setText("共需生产"+mainbagnum+"包");
                            totalfloors.setText("共有"+mainfloor+"层");
                            totalnormolnum.setText("共有常规"+mainnornum+"根");
                            totalunnormalnum.setText("共有非标2根");
                            unnorlength1.setText("非标一长"+mainunnorleg1);
                            unnorlength2.setText("非标二长"+mainunnorleg2);
                        }
                        else {
                            type.setText("型号"+vicetype);
                            totalbags.setText("共需生产"+vicebagnum+"包");
                            totalfloors.setText("共有"+vicefloor+"层");
                            totalnormolnum.setText("共有常规"+vicenornum+"根");
                            totalunnormalnum.setText("共有非标2根");
                            unnorlength1.setText("非标一长"+viceunnorleg1);
                            unnorlength2.setText("非标二长"+viceunnorleg2);
                        }
                        //显示图片picimg

                    }
                });
            }
        });
    }
    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONObject jsonObject=new  JSONObject(jsonData);
            MODE=jsonObject.getInt("mode");//重新生产，如果一开始MODE就等于2
            serverUser=jsonObject.getString("user");
            Path=jsonObject.getString("Path");//路径:http://192.168.0.136:8080/CAD/731741.png
            //Path=URLEncoder.encode(Path,"UTF-8");
            //Path="http://192.168.0.136:8080/CAD/731741.png";
            if (localUser.equals(serverUser)){}else{
                Intent intent=new Intent("com.example.cc.makePackage.FORCE_OFFLINE");
                sendBroadcast(intent);
            }
            if (isFirstlaunch){
                //loadPic(Path);
                if (MODE!=tempMODE) {
                    changeMODE=true;
                    loadPic(Path);
                }
                else {
                    changeMODE=false;
                }
            }else {loadPic(Path);
                isFirstlaunch=true;//secondtime check mode
            }
            tempMODE=MODE;

            if (MODE==1){
                maintype=jsonObject.getString("maintype");
                mainbagnum=jsonObject.getInt("mainbagnum");
                mainfloor=jsonObject.getInt("mainfloor");
                mainnornum=jsonObject.getInt("mainnornum");
                mainunnorleg1=jsonObject.getInt("mainunnorleg1");
                mainunnorleg2=jsonObject.getInt("mainunnorleg2");
            }else {
                maintype=jsonObject.getString("maintype");
                mainbagnum=jsonObject.getInt("mainbagnum");
                mainfloor=jsonObject.getInt("mainfloor");
                mainnornum=jsonObject.getInt("mainnornum");
                mainunnorleg1=jsonObject.getInt("mainunnorleg1");
                mainunnorleg2=jsonObject.getInt("mainunnorleg2");
                vicetype=jsonObject.getString("vicetype");
                vicebagnum=jsonObject.getInt("vicebagnum");
                vicefloor=jsonObject.getInt("vicefloor");
                vicenornum=jsonObject.getInt("vicenornum");
                viceunnorleg1=jsonObject.getInt("viceunnorleg1");
                viceunnorleg2=jsonObject.getInt("viceunnorleg2");
            }



        }catch (Exception e){e.printStackTrace();}

    }
    //load pic to combox
    private void loadPic(final String picpath) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(MainActivity.this).load(picpath).into(picimg);
            }
        });
    }
}
