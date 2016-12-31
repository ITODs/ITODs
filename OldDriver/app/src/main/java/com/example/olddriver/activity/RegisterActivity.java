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
import com.example.olddriver.bean.UserBean;
import com.example.olddriver.custom.SmoothCheckBox;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private Button register;
    private EditText login_tel;
    private EditText login_sms;
    private EditText login_password;
    private SmoothCheckBox login_enter;
    private Button sms_get;
    private String tel;
    private CountDownTimer timeCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.way_toolbar);
        toolbar.setTitle("个人注册");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        timeCount = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                sms_get.setTextColor(Color.parseColor("#D3D3D3"));
                sms_get.setClickable(false);
                sms_get.setText("剩余"+l/ 1000 + "S");
            }
            @Override
            public void onFinish() {
                sms_get.setText("获取验证码");
                sms_get.setClickable(true);
                sms_get.setTextColor(Color.parseColor("#6495ED"));
            }
        };

        register = (Button) findViewById(R.id.btn_to_register);
        sms_get = (Button) findViewById(R.id.sms_get);
        login_tel = (EditText)findViewById(R.id.login_tel);
        login_sms = (EditText)findViewById(R.id.login_sms);
        login_password = (EditText)findViewById(R.id.login_password);
        login_enter = (SmoothCheckBox) findViewById(R.id.login_enter);

        register.setOnClickListener(this);
        sms_get.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sms_get:
                tel = login_tel.getText().toString().trim();
                if (tel.equals("")){
                    Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobQuery<BmobUser> query = new BmobQuery<BmobUser>();
                query.addWhereEqualTo("username", tel);
                query.findObjects(new FindListener<BmobUser>() {
                    @Override
                    public void done(List<BmobUser> object, BmobException e) {
                        if(e==null){
                            if (object.size()==0){
                                BmobSMS.requestSMSCode(tel,"登录验证", new QueryListener<Integer>() {
                                @Override
                                public void done(Integer smsId,BmobException e) {
                                    if(e==null){//验证码发送成功
                                        Toast.makeText(RegisterActivity.this, "已发送短信到您的手机", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            }else{
                                Toast.makeText(RegisterActivity.this, "该用户已注册", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                timeCount.start();
                break;
            case R.id.btn_to_register:
                tel = login_tel.getText().toString().trim();
                String sms = login_sms.getText().toString().trim();
                final String password = login_password.getText().toString().trim();
                if(tel.equals("")){
                    Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sms.equals("")){
                    Toast.makeText(this, "验证码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.equals("")){
                    Toast.makeText(this, "请输入密码，方便以后登录哦", Toast.LENGTH_SHORT).show();
                    return;
                }
                UserBean user = new UserBean();
                user.setMobilePhoneNumber(tel);
                user.setPassword(password);
                user.setSex("");
                user.setTrue_name("");
                user.signOrLogin(sms, new SaveListener<UserBean>() {
                    @Override
                    public void done(UserBean user,BmobException e) {
                        BmobUser.loginByAccount(tel, password, new LogInListener<UserBean>() {
                            @Override
                            public void done(UserBean userBean, BmobException e) {
                                if(e == null){
                                    Intent register =null;
                                    if(login_enter.isChecked())
                                        register=new Intent(RegisterActivity.this, MainActivity.class);
                                    else
                                        register=new Intent(RegisterActivity.this, InformationActivity.class);
                                    startActivity(register);
                                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
