package com.example.olddriver.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olddriver.R;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText password_tel;
    private EditText password_sms;
    private EditText new_password;
    private Button sms_password;
    private Button loginButton;

    private String tel;
    private String newPassword;
    private CountDownTimer timeCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.way_toolbar);
        toolbar.setTitle("找回密码");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        timeCount = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                sms_password.setTextColor(Color.parseColor("#D3D3D3"));
                sms_password.setClickable(false);
                sms_password.setText("剩余"+l/ 1000 + "S");
            }
            @Override
            public void onFinish() {
                sms_password.setText("获取验证码");
                sms_password.setClickable(true);
                sms_password.setTextColor(Color.parseColor("#6495ED"));
            }
        };
        password_tel = (EditText) findViewById(R.id.password_tel);
        password_sms = (EditText) findViewById(R.id.password_sms);
        new_password = (EditText) findViewById(R.id.new_password);
        sms_password = (Button) findViewById(R.id.sms_password);
        loginButton = (Button) findViewById(R.id.loginButton);

        sms_password.setOnClickListener(this);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sms_password:
                tel = password_tel.getText().toString().trim();
                BmobSMS.requestSMSCode(tel,"登录验证", new QueryListener<Integer>() {
                    @Override
                    public void done(Integer smsId,BmobException e) {
                        if(e==null){//验证码发送成功
                            Toast.makeText(PasswordActivity.this, "已发送短信到您的手机", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                timeCount.start();
                break;
            case R.id.loginButton:
                String sms = password_sms.getText().toString().trim();
                newPassword = new_password.getText().toString().trim();
                BmobUser.resetPasswordBySMSCode(sms, newPassword, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            Intent smspassword = new Intent(PasswordActivity.this, LoginActivity.class);
                            startActivity(smspassword);
                            Toast.makeText(PasswordActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(PasswordActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    //titlewbar 返回键
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
