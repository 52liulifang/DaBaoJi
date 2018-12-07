package com.example.cc.makepackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cc.makepackage.util.HttpUtil;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LGActivity extends BaseActivity {

    private EditText etUserName;
    private EditText etUserPassword;
    private Button btnLogin;
    private ImageView unameClear;
    private ImageView pwdClear;
    private CheckBox checkBox;
    private String serverUrl="http://192.168.200.96/plan.json";
    private String currentUser;
    private String password;
    private String path;
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    private boolean isRememberUserNameAndPassWord=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lg);
        etUserName = findViewById(R.id.et_userName);
        etUserPassword = findViewById(R.id.et_password);
        unameClear = findViewById(R.id.iv_unameClear);
        pwdClear =  findViewById(R.id.iv_pwdClear);
        btnLogin=findViewById(R.id.btn_login);
        checkBox=findViewById(R.id.cb_checkbox);
        shared = getSharedPreferences("UaP", MODE_PRIVATE);
        isRememberUserNameAndPassWord=shared.getBoolean("rememberUaP",false);
        if (isRememberUserNameAndPassWord){
            checkBox.setChecked(true);
            etUserName.setText(shared.getString("username","0000"));
            etUserPassword.setText(shared.getString("password","0000"));
        }
        editor = shared.edit();
        etUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    unameClear.setVisibility(View.VISIBLE);
                    EditTextClearTools.addClearListener(etUserName,unameClear);

                }else{
                    unameClear.setVisibility(View.INVISIBLE);
                }
            }
        });
        etUserPassword.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus){
                    pwdClear.setVisibility(View.VISIBLE);
                    EditTextClearTools.addClearListener(etUserPassword,pwdClear);
                }else{
                    pwdClear.setVisibility(View.INVISIBLE);

                }
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isRememberUserNameAndPassWord=true;}
                    else{
                    isRememberUserNameAndPassWord=false;
                }

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUerandPassWord();
                btnLogin.setEnabled(false);
                try{
                    Thread.sleep(1500);
                }catch (Exception e){e.printStackTrace();}
                etUserName.getText().toString();
                if (etUserName.getText().toString().equals(currentUser)&&etUserPassword.getText().toString().equals(password)) {
                    editor.putString("username",currentUser);
                    editor.putString("password",password);
                    if (isRememberUserNameAndPassWord){
                    editor.putBoolean("rememberUaP",true);}else{
                        editor.putBoolean("rememberUaP",false);
                    }
                    editor.commit();
                    Intent intent = new Intent(LGActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(LGActivity.this, "账户或密码错误，请重新尝试。。", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void getUerandPassWord(){
        HttpUtil.sendOkHttpRequest(serverUrl, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str=response.body().string();
                parseJSONWithJSONObject(str);
            }
        });
    }
    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONObject jsonObject=new  JSONObject(jsonData);
            currentUser=jsonObject.getString("user");
            password=jsonObject.getString("secrect");
            path=jsonObject.getString("Path");
            Log.d("路径是：",path);

        }catch (Exception e){e.printStackTrace();}

    }
}
