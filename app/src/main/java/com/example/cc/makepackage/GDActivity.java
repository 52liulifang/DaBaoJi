package com.example.cc.makepackage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GDActivity extends BaseActivity {

    private CustomVideoView vv;
    private TextView textView;
    private boolean Flag=false;
    private ArrayList<String> maclist;
    private String[] temp;
    private static String mac;
    private String phoneMac;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private int count=6;
    private final Timer timer=new Timer();
    private TimerTask task;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        shared = getSharedPreferences("UaP", MODE_PRIVATE);
        editor = shared.edit();
        setContentView(R.layout.activity_gd);
        vv=findViewById(R.id.videoview);
        textView=findViewById(R.id.count_time);
        GuideVideo();
        authoriz();
        /*****************定时更新view*********************/
        updataTextView();

    }
    private void updataTextView(){
        task=new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("skip0"+count);
                        count=count-1;
                        if (count==-1){
                            timer.cancel();
                            updataui();
                            return;
                        }
                    }
                });
            }
        };
timer.schedule(task,0,1000);
    }



    private void GuideVideo(){
        //设置加载路径
        vv.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.webwxgetvideo));

        //静音播放
        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //mp.setVolume(0f,0f);
            }
        });
        //播放
        vv.start();

        //循环播放
        /*if (vv.isPlaying()){
        if (vv.getCurrentPosition()==5)
            Toast.makeText(this,"操死刘丽芳潘雯雯李婷婷唐思洁",Toast.LENGTH_LONG).show();}*/
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                vv.start();
            }
        });
    }
    //判断是否第一次启动程序,第一次需要连接WIFI授权
    private void authoriz() {
        boolean isfer = shared.getBoolean("isfer", true);
        if (isfer) {
            Log.d("MAC地址是",getMac(GDActivity.this));
            editor.putString("mac",getMac(GDActivity.this));
            editor.putBoolean("isfer", false);
            editor.commit();
            Toast.makeText(this,"欢迎使用本程序，正在申请授权，稍后请联系作者完成授权",Toast.LENGTH_LONG).show();
            sendMail(getMac(GDActivity.this));
        } else {
            Toast.makeText(this,"欢迎再次使用应用本程序",Toast.LENGTH_LONG).show();
            Log.d("MAC地址是",getMac(GDActivity.this));
            phoneMac=shared.getString("mac","nomac");
            sendRequestWithOkHttp();
        }
    }
    private void sendMail(final String msg) {

        /*****************************************************/
        Log.i("shuxinshuxin", "开始发送邮件");
        // 这个类主要是设置邮件
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                MailSenderInfo mailInfo = new MailSenderInfo();
                mailInfo.setMailServerHost("smtp.163.com");
                mailInfo.setMailServerPort("25");
                mailInfo.setValidate(true);
                mailInfo.setUserName("crackmee@163.com");
                mailInfo.setPassword("51fucklll");// 您的邮箱密码
                mailInfo.setFromAddress("crackmee@163.com");
                mailInfo.setToAddress("704357815@qq.com");
                mailInfo.setSubject("打包机授权Mac码");
                mailInfo.setContent(msg);

                // 这个类主要来发送邮件
                SimpleMailSender sms = new SimpleMailSender();
                boolean isSuccess = sms.sendTextMail(mailInfo);// 发送文体格式
                // sms.sendHtmlMail(mailInfo);//发送html格式
                if (isSuccess) {
                    Log.i("shuxinshuxin", "发送成功");
                } else {
                    Log.i("shuxinshuxin", "发送失败");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GDActivity.this, "当前无法完成授权申请，请检验网络后重试、、", Toast.LENGTH_SHORT).show();
                        }
                    });
                    finish();
                }
            }
        }).start();
    }
    private void sendRequestWithOkHttp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient client=new OkHttpClient();
                    Request request=new Request.Builder()
                            .url("http://blog.sina.com.cn/s/blog_a8b130c70102xf5o.html")
                            .build();
                    Response response=client.newCall(request).execute();
                    String responseData=response.body().string();
                    Pattern p;
                    p=Pattern.compile("([A-F0-9]{2}:){5}[A-F0-9]{2}");
                    Matcher m;
                    m=p.matcher(responseData);
                    maclist=new ArrayList<String>();
                    while (m.find()){
                        maclist.add(m.group());
                    }
                    temp=new String[maclist.size()];
                    int i=0;
                    for(String tempn : maclist){
                        temp[i] = tempn;
                        if (temp[i].equals(phoneMac)){
                            Flag=true;
                        }
                        i++;
                    }
                    //updataui();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void updataui(){
        new Thread(){
            public void run(){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Flag){
                            Toast.makeText(GDActivity.this, "授权成功,正在加载页面。。", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(GDActivity.this,LGActivity.class);
                            startActivity(intent);
                            finish();

                        }else{
                            Toast.makeText(GDActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                            showdialog();
                        }
                    }
                });
            }
        }.start();

    }
    private void showdialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("手机未被授权").setMessage("是否申请授权？已充值未被授权请点击帮助")
                .setPositiveButton("确定", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("MAC地址是：",getMac(GDActivity.this));
                        sendMail(getMac(GDActivity.this));

                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("帮助", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_CALL);//设置活动类型
                        intent.setData(Uri.parse("tel:18721970236"));//设置数据
                        startActivity(intent);
                    }
                });
        Dialog dialog=builder.create();
        dialog.show();
    }
    public static String getMac(Context context) {
        if (mac == null) {
            try {
                NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
                byte[] addrByte = networkInterface.getHardwareAddress();
                StringBuilder sb = new StringBuilder();
                for (byte b : addrByte) {
                    sb.append(String.format("%02X:", b));
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                mac = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                WifiManager wifiM = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                try {
                    WifiInfo wifiI = wifiM.getConnectionInfo();
                    mac = wifiI.getMacAddress();
                    //授权是需要连接WIFI
                } catch (NullPointerException e1) {
                    e1.printStackTrace();
                    mac = "02:00:00:00:00:00";
                }
            }
        }
        return mac;
    }
}
