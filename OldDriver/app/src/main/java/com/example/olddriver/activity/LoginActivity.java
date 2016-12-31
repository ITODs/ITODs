package com.example.olddriver.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olddriver.R;
import com.example.olddriver.adapter.fragmentAdapter;
import com.example.olddriver.adapter.viewAdapter;
import com.example.olddriver.bean.UserBean;
import com.example.olddriver.custom.SmoothCheckBox;
import com.example.olddriver.util.App;
import com.example.olddriver.util.DataStore;
import com.example.olddriver.util.FavouriteManager;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager mViewPager;
    //声明标题栏
    private TabLayout tab_title;
    //tab名称列表
    private List<String> list_title;
    private List<View> listViews;
    private View loginView;
    private View smsView;

    //定义以view为切换的adapter
    private viewAdapter vAdapter;
    //定义以fragment为切换的adapter
    private fragmentAdapter fAdapter;
    private Button btn_toRegister;

    private Button login;
    private Button smsLogin;
    private Button get_sms;
    private TextView forgot_password;
    private LinearLayout textview_tourist;
    private EditText login_smsTel;
    private EditText login_sms;
    private EditText login_tel;
    private EditText login_password;
    private SmoothCheckBox login_remember_password;
    private CountDownTimer timeCount;
    private String tel;
    /**
     * ATTENTION: This was auto-generated travel_to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (BmobUser.getCurrentUser() != null) {
            initDataAndStartActivity();
        }

        timeCount = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long l) {
                get_sms.setTextColor(Color.parseColor("#D3D3D3"));
                get_sms.setClickable(false);
                get_sms.setText("剩余" + l / 1000 + "S");
            }

            @Override
            public void onFinish() {
                get_sms.setText("获取验证码");
                get_sms.setClickable(true);
                get_sms.setTextColor(Color.parseColor("#6495ED"));
            }
        };
        mViewPager = (ViewPager) findViewById(R.id.viewpage);
        tab_title = (TabLayout) findViewById(R.id.tab_title);


        LayoutInflater mInflater = getLayoutInflater();
        loginView = mInflater.inflate(R.layout.layout_login, null);
        smsView = mInflater.inflate(R.layout.layout_sms, null);


        textview_tourist = (LinearLayout) findViewById(R.id.textview_tourist);

        btn_toRegister = (Button) loginView.findViewById(R.id.btn_to_register);
        forgot_password = (TextView) loginView.findViewById(R.id.forgot_password);
        login = (Button) loginView.findViewById(R.id.login_Button);
        login_tel = (EditText) loginView.findViewById(R.id.login_tel);
        login_password = (EditText) loginView.findViewById(R.id.login_password);
        login_remember_password = (SmoothCheckBox) loginView.findViewById(R.id.login_remember_password);

        smsLogin = (Button) smsView.findViewById(R.id.login_SmsButton);
        login_smsTel = (EditText) smsView.findViewById(R.id.login_smsTel);
        login_sms = (EditText) smsView.findViewById(R.id.login_sms);
        get_sms = (Button) smsView.findViewById(R.id.get_sms);

        textview_tourist.setOnClickListener(this);
        btn_toRegister.setOnClickListener(this);
        forgot_password.setOnClickListener(this);
        login.setOnClickListener(this);
        smsLogin.setOnClickListener(this);
        get_sms.setOnClickListener(this);


        //初始化数据
        initDatas();
        // ATTENTION: This was auto-generated travel_to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initDatas() {
        SharedPreferences sharedPreferences = App.getSettingsSharedPreferences();
        login_tel.setText(sharedPreferences.getString("login_tel", ""));
        if (sharedPreferences.getString("login_remember_password", "0").equals("1")) {
            login_remember_password.setChecked(true);
            login_password.setText(sharedPreferences.getString("login_password", ""));
        }

        listViews = new ArrayList<>();

        listViews.add(loginView);
        listViews.add(smsView);

        list_title = new ArrayList<>();
        list_title.add("老司机账号登录");
        list_title.add("动态密码登录");

        //设置TabLayout的模式,这里主要是用来显示tab展示的情况的
        //TabLayout.MODE_FIXED          各tab平分整个工具栏,如果不设置，则默认就是这个值
        tab_title.setTabMode(TabLayout.MODE_FIXED);

        //为TabLayout添加tab名称
        tab_title.addTab(tab_title.newTab().setText(list_title.get(0)));
        tab_title.addTab(tab_title.newTab().setText(list_title.get(1)));

        vAdapter = new viewAdapter(this, listViews, list_title, null);
        mViewPager.setAdapter(vAdapter);

        //将tabLayout与viewpager连起来
        tab_title.setupWithViewPager(mViewPager);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.textview_tourist:
                final UserBean user = new UserBean();
                user.setType(UserBean.TYPE_TRAVELER);
                user.setUsername(((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getDeviceId());
                user.setTrue_name("游客");
                user.setPassword(((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).getDeviceId());
                user.login(new SaveListener<UserBean>() {
                    @Override
                    public void done(UserBean userBean, BmobException e) {
                        if (e == null) {
                            Intent tourist = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(tourist);
                        } else {
                            user.signUp(new SaveListener<UserBean>() {
                                @Override
                                public void done(UserBean userBean, BmobException e) {
                                    if (e == null){
                                        user.login(new SaveListener<UserBean>() {
                                            @Override
                                            public void done(UserBean userBean, BmobException e) {
                                                if (e == null){
                                                    Intent tourist = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(tourist);
                                                }else{
                                                    Log.i("tourist","登录失败"+e.toString());
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
                break;


            case R.id.login_Button:
                final String users = login_tel.getText().toString().trim();
                final String passwords = login_password.getText().toString().trim();
                if (users.equals("") || passwords.equals("")) {
                    Toast.makeText(this, "手机号或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobUser.loginByAccount(users, passwords, new LogInListener<UserBean>() {
                    @Override
                    public void done(UserBean userBean, BmobException e) {
                        if (e == null) {
                            if(BmobUser.getCurrentUser()!=null)
                                Log.i("bmobuser","notNull");

                            initDataAndStartActivity();
                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                            SharedPreferences.Editor editor = App.getSettingsSharedPreferences().edit();

                            editor.putString("login_tel", users);
                            if (login_remember_password.isChecked()) {
                                editor.putString("login_password", passwords);
                                editor.putString("login_remember_password", "1");
                            }
                            editor.commit();
                        } else {
                            Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.get_sms:
                tel = login_smsTel.getText().toString().trim();
                if (tel.equals("")) {
                    Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobSMS.requestSMSCode(tel, "登录验证", new QueryListener<Integer>() {

                    @Override
                    public void done(Integer smsId, BmobException e) {
                        if (e == null) {//验证码发送成功
                            Toast.makeText(LoginActivity.this, "已发送短信到您的手机", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                timeCount.start();
                break;
            case R.id.login_SmsButton:
                tel = login_smsTel.getText().toString().trim();
                String sms = login_sms.getText().toString().trim();
                if (sms.equals("") || tel.equals("")) {
                    Toast.makeText(this, "手机号或验证码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobUser.loginBySMSCode(tel, sms, new LogInListener<UserBean>() {

                    @Override
                    public void done(UserBean user, BmobException e) {
                        if (user != null) {
                            initDataAndStartActivity();
                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.btn_to_register:
                Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register);
                break;
            case R.id.forgot_password:
                Intent password = new Intent(LoginActivity.this, PasswordActivity.class);
                startActivity(password);
                break;
            default:
                break;
        }
    }

    private void initDataAndStartActivity() {
        try {
            Class.forName("com.example.olddriver.util.HistorySearch");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        FavouriteManager.init();
        if (BmobUser.getCurrentUser() != null) {
            String ObjectID = BmobUser.getCurrentUser().getObjectId();
            BmobQuery<UserBean> bmobQuery = new BmobQuery<UserBean>();
            bmobQuery.getObject(ObjectID, new QueryListener<UserBean>() {
                @Override
                public void done(UserBean object, BmobException e) {
                    if (e == null) {
                        String head_path = DataStore.USER_PATH + File.separator + BmobUser.getCurrentUser().getUsername() + File.separator
                                + BmobUser.getCurrentUser().getUsername() + ".png";
                        File file = new File(head_path);
                        if (!file.exists() && object.getHead_path() != null) {
                            BmobFile bmobfile = new BmobFile(object.getUsername() + ".png", "", object.getHead_path());
                            final File savefile = new File(DataStore.USER_PATH + File.separator + BmobUser.getCurrentUser().getUsername() + File.separator
                                    + BmobUser.getCurrentUser().getUsername() + ".png");
                            bmobfile.download(savefile, new DownloadFileListener() {
                                @Override
                                public void done(String savePath, BmobException e) {
                                }

                                @Override
                                public void onProgress(Integer integer, long l) {
                                }
                            });
                        }
                    }
                }
            });
        }
        Intent login = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(login);
        this.finish();
    }

    /**
     * ATTENTION: This was auto-generated travel_to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Login Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated travel_to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated travel_to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
