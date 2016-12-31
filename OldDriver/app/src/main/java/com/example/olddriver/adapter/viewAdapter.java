package com.example.olddriver.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class viewAdapter extends PagerAdapter {
    public List<View> list_view;
    private List<String> list_Title;                              //tab名的列表
    private int[] tabImg;
    private Context context;

    public viewAdapter(Context context,List<View> list_view,List<String> list_Title,int[] tabImg) {
        this.list_view = list_view;
        this.list_Title = list_Title;
        this.tabImg = tabImg;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list_view.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ((ViewPager) container).addView(list_view.get(position), 0);
        return list_view.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView(list_view.get(position));
    }

    /**
     * 此方法是给tablayout中的tab赋值的，就是显示名称,并且给其添加icon的图标
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {

        return  list_Title.get(position % list_Title.size());


    }

}
