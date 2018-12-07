package com.example.cc.makepackage;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.cc.makepackage.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class MyService extends Service {
    public static final int NO1_WAIT_TOSCAN=1;
    public static final int NO2_WAIT_TOSCAN=2;
    private boolean MODE=false;//默认假，单型号生产
    private Socket socket1;
    private Socket socket2;
    private InputStream is1;
    private InputStream is2;
    private InputStreamReader isr;
    private String response1;
    private String response2;
    private OutputStream outputStream1;
    private OutputStream outputStream2;

    private DataInputStream dis1;
    private DataInputStream dis2;
    private InetAddress A6Sever1;
    private InetAddress A6Sever2;

    private static final String adr1="192.168.200.15";
    private static final String adr2="192.168.200.16";
    private static final String testadr="192.168.0.15";//测试PLC地址
    private static final int port=502;
    //private boolean flag;//false是单型号生产，true是双型号生产
    private static final byte[] buf1={(byte)0xA8,(byte)0x18,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x00,
            (byte)0x03,(byte)0x05,(byte)0xDC,(byte)0x00,(byte)0x16};
    private static final byte[] buf2 = {(byte)0xB8,(byte)0x28,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x00,
            (byte)0x03,(byte)0x06,(byte)0xA4,(byte)0x00,(byte)0x16};
    private static final byte[] testbuf = {(byte)0xB8,(byte)0x28,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x00,
            (byte)0x03,(byte)0x02,(byte)0xBD,(byte)0x00,(byte)0x16};
    private int[] arr1;
    private int[] arr2;//之前是24
    private int[] arr4;
    private int[] arr8;

    private PassValue passValue;
    private int mode;
    private String maintype;
    private String vicetype;
    private int maintotalnum;
    private int vicetotalnum;
    private int[] passint;
    private boolean flag=true;//是否是第一次开启服务
    private AlarmManager manager;
    private PendingIntent pi;

    private static boolean testService=false;//测试定时服务中变量值变化情况20180904
    public MyService() {
    }

//HandlerThread thread=new HandlerThread();


    private MsgBinder mBinder = new MsgBinder();//不需要在活动里控制代码，只需回调显示进度(需要的因为有两种生产模式)

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;//不需要在活动里控制代码，只需回调显示进度(需要的因为有两种生产模式)
    }

    class MsgBinder extends Binder {
        //不需要在活动里控制代码，只需回调显示进度(需要的因为有两种生产模式)
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        arr1=new int[44];
        arr2=new int[44];
        arr4=new int[4];
        arr8=new int[8];
        //flag=true;
        Log.d("新建服务","新建后台服务");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //第二次循环后，并不会得到从活动get到的intent值，因为flag再次为true，
        if (flag) {
            flag = false;
            mode = intent.getIntExtra("mode", 3);//获取Activity传递的值
            Log.d("获取狗逼刘丽芳", "fuck");

            if (mode == 1) {
                maintype = intent.getStringExtra("maintype");
                maintotalnum = intent.getIntExtra("maintotalnum", 0);
            } else {
                maintype = intent.getStringExtra("maintype");
                vicetype = intent.getStringExtra("vicetype");
                maintotalnum = intent.getIntExtra("maintotalnum", 0);
                vicetotalnum = intent.getIntExtra("vicetotalnum", 0);
            }
        }
        Log.d("模式是", mode+"");
        Toast.makeText(this,"模式是"+mode,Toast.LENGTH_SHORT).show();
        backint();
        startForeground(1, getNotification("后台打包中...", "请点此进入生产界面查看详细。"));

        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int oneminute = 10 * 1000;//预设每分钟更新一次
        long triggerAtTime = SystemClock.elapsedRealtime() + oneminute;
        Intent i = new Intent(this, MyService.class);
        pi = PendingIntent.getService(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //flag=true;
        manager.cancel(pi);
        Toast.makeText(this,"服务onDestroy",Toast.LENGTH_SHORT).show();

    }


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    /**@param
     * **/
    private Notification getNotification(String title, String context) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setContentText(context);
        builder.setAutoCancel(true);
        //20180816
        builder.build().flags=Notification.FLAG_NO_CLEAR;
        return builder.build();
    }
    public void setCallback(PassValue passValue){
        this.passValue=passValue;
    }

    public  interface PassValue{
        void onDataChange(int[] arr);//差一个调用
    }
    /**********
     * 如果是向PLC请求数据，请求数据和接收数据请参照Mosbua报文结构解析
     *
     * 如果是向A6请求数据，相比于PLC，接收数据缺少事务标识符和协议标识符，共4个字节即8个字符，其他都一样
     * 所以解析接收到的A6数据时，使用str.substring(10+(i-1)*2,2*i+10),即从第10个字符，每两个字符作为一个数组的一个元素
     * 由于A6是双字装置，装置里的存放顺序是③④②①，读取时④是最低位（1个字节）①是最高位（1个字节）
     *
     * *****************/
    public void backint(){

        new Thread(){
            @Override
            public void run() {
                is1=null;
                is2=null;

                outputStream2=null;
                outputStream1=null;
                if (mode==1){
                    try{
                        A6Sever1=InetAddress.getByName(adr1);
                        socket1=new Socket(A6Sever1,port);//A6Sever1
                        //查询
                        outputStream1=socket1.getOutputStream();
                        outputStream1.write(buf1);//buf1
                        outputStream1.flush();
                        is1=socket1.getInputStream();
                        dis1=new DataInputStream(is1);
                        byte[] buf=new byte[49];//之前是29
                        dis1.read(buf);
                        response1= StringUtil.bytesToHexString(buf);
                        Log.d("有",response1);
                        StringUtil.string2int(response1,arr1);
                        if (arr1[1]==1)
                            getNotificationManager().notify(1, getNotification("当前生产包"+maintype, "主轨数量是"+maintotalnum+"还剩下"+"根"));

                        //发送msg
                        arr4[0]=arr1[29];//test地址713
                        arr4[1]=arr1[33];//test地址715
                        arr4[2]=arr1[37];//test地址717
                        arr4[3]=arr1[41];//test地址719
                        Log.d("arr1[41]",arr1[41]+"");
                        if (passValue!=null)
                        passValue.onDataChange(arr4);
                        Log.d("1111","aaa"+String.valueOf(arr1[29])+"aa"+String.valueOf(arr1[33])+"bb"+String.valueOf(arr1[37])+"烂逼逼"+String.valueOf(arr1[41]));

                    }catch (IOException e){
                        e.printStackTrace();
                    }finally {
                        try{
                            if (is1!=null)
                                is1.close();
                            if (outputStream1!=null)
                                outputStream1.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }else {
                    try{
                        A6Sever1=InetAddress.getByName(adr1);
                        A6Sever2=InetAddress.getByName(adr2);


                        socket1=new Socket(A6Sever1,port);

                        socket2=new Socket(A6Sever2,port);

                        //查询
                        outputStream1=socket1.getOutputStream();
                        outputStream2=socket2.getOutputStream();
                        outputStream1.write(buf1);
                        outputStream2.write(buf2);
                        outputStream1.flush();
                        outputStream2.flush();
                        is1=socket1.getInputStream();
                        is2=socket2.getInputStream();
                        dis1=new DataInputStream(is1);
                        dis2=new DataInputStream(is2);
                        byte[] backbuf1=new byte[49];
                        byte[] backbuf2=new byte[49];
                        dis1.read(backbuf1);
                        dis2.read(backbuf2);
                        response1=StringUtil.bytesToHexString(backbuf1);
                        response2=StringUtil.bytesToHexString(backbuf2);
                        StringUtil.string2int(response1,arr1);
                        StringUtil.string2int(response2,arr2);
                        arr8[0]=arr1[29];
                        arr8[1]=arr1[33];
                        arr8[2]=arr1[37];
                        arr8[3]=arr1[41];
                        arr8[4]=arr2[29];
                        arr8[5]=arr2[33];
                        arr8[6]=arr2[37];
                        arr8[7]=arr2[41];
                        if (passValue!=null)
                        passValue.onDataChange(arr8);
                        Log.d("狗比比李玲玲","狗比李玲玲"+String.valueOf(arr1[2])+"的逼逼那么大"+String.valueOf(arr2[2]));
                        if (arr1[1]==1)
                            Toast.makeText(getApplicationContext(),"嗨",Toast.LENGTH_SHORT).show();
                        if (arr2[1]==1)
                            Toast.makeText(getApplicationContext(),"嗨",Toast.LENGTH_SHORT).show();
                        //分别是总数量，剩余数量
                    }catch (IOException e){
                        e.printStackTrace();
                    }finally {
                        try{
                            if (is1!=null)
                                is1.close();
                            if (is2!=null)
                                is2.close();
                            if (outputStream1!=null)
                                outputStream1.close();
                            if (outputStream2!=null)
                                outputStream2.close();
                        }catch (Exception e){e.printStackTrace();}
                    }
                }
            }
        }.start();

    }

}
