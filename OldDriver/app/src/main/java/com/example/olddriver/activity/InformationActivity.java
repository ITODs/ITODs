package com.example.olddriver.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.olddriver.R;
import com.example.olddriver.bean.UserBean;
import com.example.olddriver.custom.ChangeAddressPopwindow;
import com.example.olddriver.map.LocationService;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {
    private Button submit_info;
    private PopupWindow mPopupWindow;
    private TextView jump;
    private TextView tv_sex;
    private EditText et_true_name;
    private TextView tv_tel;
    private TextView tv_city;
    private TextView pop_city;
    private ChangeAddressPopwindow city_popwindow;
    public static final int POP_UP_WINDOW_SEX = 1;
    public static final int POP_UP_WINDOW_CITY = 2;
    private View.OnClickListener sexListener= null;
    private View.OnClickListener cityListener= null;
    private String sex;
    private String city = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        submit_info= (Button) findViewById(R.id.btn_submit);
        jump= (TextView) findViewById(R.id.tv_jump);
        et_true_name = (EditText) findViewById(R.id.edittext_information_name);
        tv_tel = (TextView) findViewById(R.id.edittext_information_tel);
        tv_sex = (TextView) findViewById(R.id.edittext_information_sex);
        tv_city = (TextView) findViewById(R.id.edittext_information_city);
        submit_info.setOnClickListener(this);
        jump.setOnClickListener(this);
        tv_sex.setOnClickListener(this);
        tv_city.setOnClickListener(this);

        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_submit:
                Intent submit=new Intent(InformationActivity.this, MainActivity.class);
                String true_name = et_true_name.getText().toString().trim();
                String sex = tv_sex.getText().toString();
                UserBean change_user = new UserBean();
                if(!sex.equals(""))
                    change_user.setSex(sex);
                if(!true_name.equals(""))
                    change_user.setTrue_name(true_name);
                if(!city.equals(""))
                    change_user.setCity_name(city);
                change_user.update(BmobUser.getCurrentUser().getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null){
                            Toast.makeText(InformationActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(InformationActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                startActivity(submit);
                finish();
                break;
            case R.id.tv_jump:
                Intent jump=new Intent(InformationActivity.this, MainActivity.class);
                startActivity(jump);
                break;
            case R.id.edittext_information_sex:
                setPopupWindow(POP_UP_WINDOW_SEX);
                mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupWindow.showAtLocation(findViewById(R.id.activity_information), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                break;
            case R.id.edittext_information_city:
                setPopupWindow(POP_UP_WINDOW_CITY);
                mPopupWindow.setAnimationStyle(R.style.anim_menu_bottombar);
                mPopupWindow.showAtLocation(findViewById(R.id.activity_information), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                break;

            default:
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0) {
            if (mPopupWindow != null && !mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(findViewById(R.id.layout_main), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void init() {

        tv_sex.setHint("请选择");
        et_true_name.setHint("请填写");
        tv_tel.setText("Tel:*******"+ BmobUser.getCurrentUser().getUsername().substring(7,11));


        sexListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.male:
                        sex = "男";
                        break;
                    case R.id.female:
                        sex = "女";
                        break;
                    default:
                        break;
                }
                tv_sex.setText(sex);
                mPopupWindow.dismiss();
            }
        };
        cityListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.more_city:
                        city_popwindow = new ChangeAddressPopwindow(InformationActivity.this);
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
                        city_popwindow.showAtLocation(findViewById(R.id.activity_information), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0,0);
                        break;
                    case R.id.check_city:
                        city = LocationService.lastLocation.getCity();
                        tv_city.setText(city);
                        break;
                    default:
                    break;
                }
                mPopupWindow.dismiss();
            }
        };
    }
    private void setPopupWindow(int type) {
        View popupView = null;
        switch (type) {
            case POP_UP_WINDOW_SEX:
                popupView = getLayoutInflater().inflate(R.layout.popupwindow_sex, null);
                popupView.findViewById(R.id.male).setOnClickListener(sexListener);
                popupView.findViewById(R.id.female).setOnClickListener(sexListener);
                break;
            case POP_UP_WINDOW_CITY:
                popupView = getLayoutInflater().inflate(R.layout.popupwindow_city, null);
                popupView.findViewById(R.id.more_city).setOnClickListener(cityListener);
                popupView.findViewById(R.id.check_city).setOnClickListener(cityListener);
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
}
