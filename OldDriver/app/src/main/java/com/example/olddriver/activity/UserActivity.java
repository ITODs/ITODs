package com.example.olddriver.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olddriver.R;
import com.example.olddriver.bean.UserBean;
import com.example.olddriver.custom.ChangeAddressPopwindow;
import com.example.olddriver.map.LocationService;
import com.example.olddriver.util.DataStore;
import com.example.olddriver.util.MakeHead;

import java.io.File;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    private PopupWindow mPopupWindow;
    private ChangeAddressPopwindow city_popwindow;
    private ImageView head;
    private UserBean user;
    private EditText et_true_name;
    private TextView tv_tel;
    private TextView tv_sex;
    private TextView tv_city;
    private Bitmap head_show;
    private MakeHead makehead = null;
    private String sex;
    private String city = "";
    private Button btn_submit;
    public static final int POP_UP_WINDOW_SEX = 1;
    public static final int POP_UP_WINDOW_CITY = 2;
    public static final int POP_UP_WINDOW_HEAD = 3;
    private View.OnClickListener sexListener= null;
    private View.OnClickListener headListener= null;
    private View.OnClickListener cityListener= null;
    private TextView pop_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.way_toolbar);
        toolbar.setTitle("我的信息");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        init();
    }

    public void init() {
        et_true_name = (EditText) findViewById(R.id.edittext_information_name);
        tv_tel = (TextView) findViewById(R.id.edittext_information_tel);
        head = (ImageView) findViewById(R.id.head);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        tv_sex = (TextView) findViewById(R.id.textview_information_sex);
        tv_city= (TextView) findViewById(R.id.textview_information_city);
        String ObjectID = BmobUser.getCurrentUser().getObjectId();

        BmobQuery<UserBean> bmobQuery = new BmobQuery<UserBean>();
        bmobQuery.getObject(ObjectID, new QueryListener<UserBean>() {
            @Override
            public void done(UserBean object, BmobException e) {
                if (e == null) {
                    user = object;
                    head_show = MakeHead.getDiskBitmap(DataStore.USER_PATH + File.separator + object.getUsername()
                            + File.separator + BmobUser.getCurrentUser().getUsername() + ".png");
                    tv_tel.setText(object.getUsername());
                    if (object.getSex().trim().equals(""))
                        tv_sex.setHint("请选择性别");
                    else
                        tv_sex.setHint(object.getSex());

                    if (object.getTrue_name().trim().equals(""))
                        et_true_name.setHint("请填写昵称");
                    else
                        et_true_name.setHint(object.getTrue_name().toString());
                    if (object.getCity_name().trim().equals(""))
                        tv_city.setHint("请选择城市");
                    else
                        tv_city.setHint(object.getCity_name().trim());
                    if (head_show == null) {
                        head.setImageResource(R.mipmap.logo_new);
                    } else {
                        head.setImageBitmap(head_show);
                    }
                } else {
                }
            }
        });
        head.setOnClickListener(this);
        tv_sex.setOnClickListener(this);
        tv_city.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
        sexListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.male:
                        sex = "男司机";
                        break;
                    case R.id.female:
                        sex = "女司机";
                        break;
                    default:
                        break;
                }
                tv_sex.setText(sex);
                mPopupWindow.dismiss();
            }
        };
        headListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.choose_photo:
                        makehead.ChoosePic(UserActivity.this,MakeHead.CHOOSE_PICTURE);
                        break;
                    case R.id.take_photo:
                        makehead.ChoosePic(UserActivity.this,MakeHead.TAKE_PICTURE);
                        break;
                    default:
                        break;
                }
                mPopupWindow.dismiss();
            }
        };
        cityListener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.check_city:
                        city = LocationService.lastLocation.getCity();
                        tv_city.setText(city);
                        break;
                    case R.id.more_city:
                        city_popwindow = new ChangeAddressPopwindow(UserActivity.this);
                        city_popwindow.setAddress("广东", "佛山", "南海区");
                        city_popwindow.setTouchable(true);
                        city_popwindow.setOutsideTouchable(true);
                        city_popwindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
                        //设置焦点
                        city_popwindow.getContentView().setFocusableInTouchMode(true);
                        city_popwindow.getContentView().setFocusable(true);

                        //设置键盘监听
                        city_popwindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (city_popwindow != null && city_popwindow.isShowing()) {
                                        city_popwindow.dismiss();
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });
                        city_popwindow.setAddresskListener(new ChangeAddressPopwindow.OnAddressCListener() {

                            @Override
                            public void onClick(String province, String city2, String area) {
                                // TODO Auto-generated method stub
                                city = province + " " + city2 + " " + area;
                                tv_city.setText(city);

                            }
                        });
                        city_popwindow.setAnimationStyle(R.style.anim_menu_bottombar);
                        city_popwindow.showAtLocation(findViewById(R.id.activity_user), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                        break;
                    default:
                        break;
                }
                mPopupWindow.dismiss();
            }
        };
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        makehead.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.head:
                makehead = new MakeHead(head);
                setPopupWindow(POP_UP_WINDOW_HEAD);
                mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupWindow.showAtLocation(findViewById(R.id.activity_user), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                break;
            case R.id.textview_information_sex:
                setPopupWindow(POP_UP_WINDOW_SEX);
                mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupWindow.showAtLocation(findViewById(R.id.activity_user), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                break;
            case R.id.textview_information_city:
                setPopupWindow(POP_UP_WINDOW_CITY);
                mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupWindow.showAtLocation(findViewById(R.id.activity_user), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                break;
            case R.id.btn_submit:
                String true_name = et_true_name.getText().toString().trim();
                String sex = tv_sex.getText().toString();
                UserBean change_user = new UserBean();
                if (sex.equals("")&&true_name.equals("")&&city.equals("")){
                    finish();
                    return;
                }
                if(!sex.equals(""))
                    change_user.setSex(sex);
                if(!true_name.equals(""))
                    change_user.setTrue_name(true_name);
                if(!city.equals(""))
                    change_user.setCity_name(city);
                change_user.update(user.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null){
                            Toast.makeText(UserActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(UserActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                finish();
                break;
            default:
                break;
        }
    }
    private void setPopupWindow(int type) {
        View popupView = null;
        switch (type) {
            case POP_UP_WINDOW_HEAD:
                popupView = getLayoutInflater().inflate(R.layout.popupwindow_head, null);
                popupView.findViewById(R.id.choose_photo).setOnClickListener(headListener);
                popupView.findViewById(R.id.take_photo).setOnClickListener(headListener);
                break;
            case POP_UP_WINDOW_SEX:
                popupView = getLayoutInflater().inflate(R.layout.popupwindow_sex, null);
                popupView.findViewById(R.id.male).setOnClickListener(sexListener);
                popupView.findViewById(R.id.female).setOnClickListener(sexListener);
                break;
            case POP_UP_WINDOW_CITY:
                popupView = getLayoutInflater().inflate(R.layout.popupwindow_city, null);
                popupView.findViewById(R.id.check_city).setOnClickListener(cityListener);
                popupView.findViewById(R.id.more_city).setOnClickListener(cityListener);
                pop_city= (TextView) popupView.findViewById(R.id.located_city);
                pop_city.setText(LocationService.lastLocation.getCity());
                break;
            default:
                break;
        }
        mPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, true);
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mPopupWindow.getContentView().setFocusableInTouchMode(true);
        mPopupWindow.getContentView().setFocusable(true);

        //设置键盘监听
        mPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
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
