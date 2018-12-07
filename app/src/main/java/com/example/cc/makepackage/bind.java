package com.example.cc.makepackage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class bind extends BaseActivity {

    private TextView title;
    private TextView result;
    private Button bind;
    private String str;
    private String code;

    private ExecutorService mThreadPool;
    private Socket socket;
    private InetAddress serverAddr;
    private static final String serverIp="192.168.200.96";//服务器IP
    private static final int port=19730;//服务器端口
    private PrintWriter out;
    private BufferedReader br;
    private String backString;
    private Handler mMainHandler;
    private Intent intentac;
    private boolean recieveflag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind);
        title=findViewById(R.id.any_type);
        result=findViewById(R.id.code_num);
        bind=findViewById(R.id.bind);
        Intent intent=getIntent();
        intentac=new Intent(bind.this,MainActivity.class);

        str=intent.getStringExtra("type");
        title.setText(str);
        code=intent.getStringExtra("code");
        result.setText(code);
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(bind.this,"绑定失败，请重试",Toast.LENGTH_LONG).show();
                        startActivity(intentac);
                        break;
                    case 1:
                        Toast.makeText(bind.this,"成功绑定一条记录",Toast.LENGTH_LONG).show();
                        startActivity(intentac);
                        break;
                    case 2:
                        Toast.makeText(bind.this,"连接成功",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(bind.this,"连接失败,请检查网络",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;

                }
            }
        };
        bind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
bind.setEnabled(false);
                //应该弹出一个对话框
                try{
                    out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                    out.print(code+":"+str);

                    out.flush();
                }catch (IOException e){
                    e.printStackTrace();
                }
                recieveMsg();
            }
        });
        mThreadPool = Executors.newCachedThreadPool();
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    serverAddr=InetAddress.getByName(serverIp);
                    socket=new Socket(serverAddr,port);
                    br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    if (socket.isConnected()){
                        Message msg = Message.obtain();
                        msg.what = 2;
                        mMainHandler.sendMessage(msg);
                    }
                    else {
                        Message msg = Message.obtain();
                        msg.what = 3;
                        mMainHandler.sendMessage(msg);
                    }

                }catch (IOException E){E.printStackTrace();}
            }
        });

        try{Thread.currentThread().sleep(300);}catch (Exception e){e.printStackTrace();}


        }

private void recieveMsg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (recieveflag){
                            try{
                                if (socket.isConnected()){
                                    if (!socket.isInputShutdown()){
                                        if ((backString=br.readLine())!=null){
                                            if ("ok".equals(backString)){
                                                Message msg = Message.obtain();
                                                msg.what = 1;
                                                mMainHandler.sendMessage(msg);
                                                recieveflag=false;
                                            }else {
                                                Message msg = Message.obtain();
                                                msg.what = 0;
                                                mMainHandler.sendMessage(msg);
                                                recieveflag=false;
                                                /*AlertDialog.Builder builder=new AlertDialog.Builder(bind.this);
                                                builder.setTitle("提示！");
                                                builder.setMessage("绑定记录失败!请重新扫码绑定。");
                                                builder.setCancelable(false);
                                                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        startActivity(intentac);
                                                    }
                                                } );
                                                builder.show();*/

                                            }
                                        }
                                    }
                                }
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                }
            }
        }).start();
}

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mThreadPool.shutdown();
        try{
            if (socket.isConnected())
                socket.close();
                if (socket.getInputStream()!=null)
                socket.getInputStream().close();
            if (socket.getOutputStream()!=null)
                socket.getOutputStream().close();

        }catch (Exception e){e.printStackTrace();}
    }
}
