package com.example.olddriver.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.PlanNode;
import com.example.olddriver.R;
import com.example.olddriver.adapter.HistoryAdapter;
import com.example.olddriver.adapter.SuggestAdapter;
import com.example.olddriver.bean.FavouritePoi;
import com.example.olddriver.bean.LocationList;
import com.example.olddriver.bean.SearchResult;
import com.example.olddriver.map.AddressSearch;
import com.example.olddriver.map.LocationService;
import com.example.olddriver.map.RoutePlanSearch;
import com.example.olddriver.map.SuggestionSearch;
import com.example.olddriver.util.FavouriteManager;
import com.example.olddriver.util.FinalValue;
import com.example.olddriver.util.HistorySearch;
import com.example.olddriver.util.TravelManager;

import java.util.HashMap;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher ,AdapterView.OnItemClickListener{
    private ImageView back;
    private ListView historylist;
    private ListView suggestlist;
    private EditText edit_search;
    private ImageView clear;

    private TextView tvComeBackHome;
    private TextView tvToCompany;
    private TextView tvMore;

    private View historyContent;
    private View suggestContent;
    private LinearLayout.LayoutParams params;
    private boolean isOnHistoryContentShowing = true;

    //自定义BaseAdapter
    private HistoryAdapter historyAdapter;
    private SuggestAdapter suggestAdapter;

    private PopupWindow mLoadingPopupWindow;

    //数据加载任务
    private LoadingTask loadingTask = null;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        back= (ImageView) findViewById(R.id.to_back);
        back.setOnClickListener(this);


        edit_search= (EditText) findViewById(R.id.edit_search);
        edit_search.addTextChangedListener(this);

        clear= (ImageView) findViewById(R.id.to_clear);
        clear.setOnClickListener(this);

        tvComeBackHome = (TextView)findViewById(R.id.search_back_home);
        tvToCompany = (TextView)findViewById(R.id.search_to_company);
        tvMore = (TextView)findViewById(R.id.search_more);



        suggestContent = getLayoutInflater().inflate(R.layout.popupwindow_suggest, null);
        historyContent = findViewById(R.id.history_content);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        historylist= (ListView) findViewById(R.id.located_history);

        HistorySearch.getHistory(new HistorySearch.OnGetHistoryDataListener() {
            @Override
            public void onGetHistoryData(final List<LocationList> dataList) {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        historyAdapter =new HistoryAdapter(dataList,SearchActivity.this);
                        historylist.setAdapter(historyAdapter);
                    }
                });
            }
        });

        //加载收藏夹数据
        FavouriteManager.getFavouriteData(new FavouriteManager.OnGetFavouriteListListener() {
            @Override
            public void onGetFavouriteList(List<FavouritePoi> dataList) {
                SearchActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBtnListener();
                    }
                });
            }
        });

        historylist.setOnItemClickListener(this);

        suggestlist= (ListView)suggestContent.findViewById(R.id.locate_suggest);
        suggestlist.setOnItemClickListener(this);
        //设置滑动listview的时候自动关闭软键盘
        suggestlist.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(getCurrentFocus()
                                        .getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        });


        //初始化加载popupView
        initLoadingPopupWindow();

        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle!= null){
                final FavouritePoi poi = (FavouritePoi) bundle.get("favouritePoi");
                if(poi != null){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchByFavouritePoi(poi);
                        }
                    }, 100);
                }
            }
        }
    }

    //设置常用地点的btn监听
    private void setBtnListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FavouritePoi favouritePoi = null;

                switch (view.getId()){
                    case R.id.search_back_home:
                        favouritePoi = FavouriteManager.getPoiByTag(FavouriteManager.MY_HOME);
                        break;

                    case R.id.search_to_company:
                        favouritePoi = FavouriteManager.getPoiByTag(FavouriteManager.MY_COMPANY);
                        break;

                    case R.id.search_more:
                        break;
                }

                if(favouritePoi == null){
                    Toast.makeText(SearchActivity.this, "找不到数据",Toast.LENGTH_SHORT).show();
                    return;
                }

                searchByFavouritePoi(favouritePoi);
            }
        };

        tvComeBackHome.setOnClickListener(listener);
        tvToCompany.setOnClickListener(listener);
        tvMore.setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.to_back:
                this.setResult(FinalValue.FAILED, null);
                finish();
                break;

            case R.id.to_clear:
                this.edit_search.setText("");
                break;

            default:
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (edit_search.getText().toString().trim().equals("")){
            clear.setVisibility(View.GONE);
            if(isOnHistoryContentShowing == false)
                changeView(true);
        }
        else{
            clear.setVisibility(View.VISIBLE);
            if(isOnHistoryContentShowing == true)
                changeView(false);

            SuggestionSearch suggestionSearch = new SuggestionSearch();
            suggestionSearch.setListener(new SuggestionSearch.OnGetSuggestionResultListener() {
                @Override
                public void onGetSuggestionResult(List<LocationList> dataList) {
                    suggestAdapter = new SuggestAdapter(dataList, SearchActivity.this);
                    suggestlist.setAdapter(suggestAdapter);
                }
            });
            suggestionSearch.search(edit_search.getText().toString().trim());
        }
    }

    private void changeView(boolean isChangeToHistory){
        LinearLayout rootView = (LinearLayout) this.findViewById(R.id.activity_search);
        if(isChangeToHistory){
            rootView.removeView(suggestContent);
            rootView.addView(historyContent,params);
            isOnHistoryContentShowing = true;
        }else{
            rootView.removeView(historyContent);
            rootView.addView(suggestContent,params);
            isOnHistoryContentShowing = false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if(mLoadingPopupWindow.isShowing() == false)
            mLoadingPopupWindow.showAtLocation(findViewById(R.id.activity_search),Gravity.NO_GRAVITY,0,0);

        if(isOnHistoryContentShowing ){
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if(loadingTask != null)
            loadingTask.cancel();

        loadingTask = new LoadingTask();
        loadingTask.execute((LocationList) adapterView.getAdapter().getItem(i));

    }


    private void initLoadingPopupWindow(){
        View popupView = getLayoutInflater().inflate(R.layout.popupwindow_loading_anim, null);

        mLoadingPopupWindow = new PopupWindow(popupView, Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT, true);
        mLoadingPopupWindow.setTouchable(true);
        mLoadingPopupWindow.setOutsideTouchable(true);
        mLoadingPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
        //设置焦点
        mLoadingPopupWindow.getContentView().setFocusableInTouchMode(true);
        mLoadingPopupWindow.getContentView().setFocusable(true);

        mLoadingPopupWindow.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("well","well");
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getRepeatCount() == 0
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mLoadingPopupWindow != null && mLoadingPopupWindow.isShowing()) {
                        mLoadingPopupWindow.dismiss();

                        if(loadingTask != null)
                            loadingTask.cancel();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private class LoadingTask{
        private boolean isCancel = false;

        private void execute(final LocationList targetData){
            final SearchResult searchResult = new SearchResult();

            //12
            final boolean[] isGetResult = new boolean[]{false,false};

            //add history
            HistorySearch.saveHistorySearchData(targetData);

            final Intent intent = new Intent(SearchActivity.this,MainActivity.class);

            searchResult.setTargetLatLng(targetData.getTargetLatLng());
            searchResult.setKey(targetData.getLocation_title());
            searchResult.setAddr(targetData.getLocation_address());

            //启动路径规划
            PlanNode sNode, eNode;
            sNode = PlanNode.withLocation(
                    new LatLng(LocationService.lastLocation.getLatitude(),
                            LocationService.lastLocation.getLongitude()));

            eNode = PlanNode.withLocation(targetData.getTargetLatLng());
            Log.i("location", targetData.getCity() + "  " + targetData.getLocation_title());

            RoutePlanSearch routePlanSearch = new RoutePlanSearch(new DrivingRoutePlanOption().from(sNode).to(eNode));
            routePlanSearch.setListener(new RoutePlanSearch.OnGetRoutePlanResultListener() {
                @Override
                public void OnGetResult(HashMap<DrivingRoutePlanOption.DrivingPolicy, DrivingRouteLine> dataMap) {
                    searchResult.setRouteLineDataMap(dataMap);
                    isGetResult[0] = true;

                    if(isGetResult[0] == true &&
                            isGetResult[1] == true &&
                            isCancel == false){

                        //保存我的行程记录
                        TravelManager.addTravelNode(targetData);

                        SearchResult.lastSearchResult = searchResult;
                        SearchActivity.this.setResult(FinalValue.SUCCESS, intent);
                        SearchActivity.this.finish();
                    }
                }
            });

            Log.i("routePlan", LocationService.lastLocation.getCity() + "  " + LocationService.lastLocation.getAddrStr());
            Log.i("routePlan", targetData.getCity() + "  " + targetData.getLocation_title());

            AddressSearch addressSearch = new AddressSearch();
            addressSearch.setOnGetAddressListener(new AddressSearch.OnGetAddressListener() {
                @Override
                public void onGetAddress(String address) {
                    searchResult.setAddr(address);
                    isGetResult[1] = true;

                    if(isGetResult[0] == true &&
                            isGetResult[1] == true &&
                            isCancel == false){
                        //保存我的行程记录
                        TravelManager.addTravelNode(targetData);

                        SearchResult.lastSearchResult = searchResult;

                        SearchActivity.this.setResult(FinalValue.SUCCESS, intent);
                        SearchActivity.this.finish();
                    }
                }
            });

            routePlanSearch.start();
            addressSearch.getAdress(targetData.getTargetLatLng());
        }

        public void cancel(){
            isCancel = true;
            Log.i("loadingTask","cancel");
        }
    }


    public void searchByFavouritePoi(FavouritePoi favouritePoi){
        if(mLoadingPopupWindow.isShowing() == false)
            mLoadingPopupWindow.showAtLocation(findViewById(R.id.activity_search),Gravity.NO_GRAVITY,0,0);

        if(isOnHistoryContentShowing ){
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus()
                                    .getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if(loadingTask != null)
            loadingTask.cancel();

        //构造查询数据
        LocationList data = new LocationList();
        data.setLocation_title(favouritePoi.getM_title());
        data.setTargetLatLng(new LatLng(favouritePoi.getM_latitude(), favouritePoi.getM_longtitude()));
        data.setLocation_address(favouritePoi.getM_address());
        //data.setCity(favouritePoi.getc);
        loadingTask = new LoadingTask();
        loadingTask.execute(data);
    }
}
