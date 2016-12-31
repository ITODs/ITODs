package com.example.olddriver.map;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRoutePlanOption;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.SuggestAddrInfo;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * Created by lzp on 2016/12/11.
 * 跨城公交检索，起点终点用City Name and place name才有打车信息。
 */

public class MassTransitRoutePlan implements OnGetRoutePlanResultListener {
    private com.baidu.mapapi.search.route.RoutePlanSearch mSearch;
    private OnGetRoutePlanListener listener = null;

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {}

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {
        if(massTransitRouteResult.error.equals(SearchResult.ERRORNO.NO_ERROR)){
            if(massTransitRouteResult.getTaxiInfo() != null)
                Log.i("routePlanMass",massTransitRouteResult.getTaxiInfo().getTotalPrice() + "");
            listener.onGetMassTransitRoutePlan(massTransitRouteResult);
            return;

        }else if(massTransitRouteResult.error.equals(SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR)){
            Log.i("routePlanMassErr",massTransitRouteResult.error + "");
            SuggestAddrInfo suggestAddrInfo = massTransitRouteResult.getSuggestAddrInfo();
            Log.i("routePlanMassErr",suggestAddrInfo.getSuggestStartNode() == null ? "null" : "notNull");
            Log.i("routePlanMassErr",suggestAddrInfo.getSuggestEndNode() == null ? "null" : "notNull");


            if(suggestAddrInfo != null
                    && suggestAddrInfo.getSuggestStartNode() != null
                    && suggestAddrInfo.getSuggestEndNode() != null
                    && suggestAddrInfo.getSuggestStartNode().get(0) != null
                    && suggestAddrInfo.getSuggestEndNode().get(0) != null){

                PoiInfo sNode = suggestAddrInfo.getSuggestStartNode().get(0);
                PoiInfo eNode = suggestAddrInfo.getSuggestEndNode().get(0);

                Log.i("routePlanMass", sNode.city + "  " + sNode.address);
                Log.i("routePlanMass", eNode.city + "  " + eNode.address);

                MassTransitRoutePlanOption option = new MassTransitRoutePlanOption();
                option.from(PlanNode.withCityNameAndPlaceName("", sNode.address));
                option.to(PlanNode.withCityNameAndPlaceName("", eNode.address));


                MassTransitRoutePlan.this.search(option);
            }else{
                listener.onGetMassTransitRoutePlan(null);
            }
        }else{
            Log.i("routePlanMassError",massTransitRouteResult.error + "");
            listener.onGetMassTransitRoutePlan(null);
        }
        //MassTransitRoutePlan.this.mSearch.destroy();
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {}

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {}

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {}

    public interface OnGetRoutePlanListener{
        void onGetMassTransitRoutePlan(MassTransitRouteResult result);
    }

    public void setOnGetRoutePlanListener(OnGetRoutePlanListener listener){
        this.listener = listener;
    }


    public void search(MassTransitRoutePlanOption option){
        mSearch = com.baidu.mapapi.search.route.RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);
        /*mSearch.masstransitSearch(new MassTransitRoutePlanOption()
                .travel_from(PlanNode.withCityNameAndPlaceName("北京","天安门"))
                .travel_to(PlanNode.withCityNameAndPlaceName("上海","东方明珠"))
        );*/

        mSearch.masstransitSearch(option);
    }

    public void search(LatLng currentLatLng, final LatLng targetLatLng){
        mSearch = com.baidu.mapapi.search.route.RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        final boolean[] isGetLocation = new boolean[]{false, false};
        final PlanNode[] nodes = new PlanNode[2];
        addressSearch = new AddressSearch();
        addressSearch.setOnGetAddressListener(new AddressSearch.OnGetAddressListener() {
            @Override
            public void onGetAddress(String address) {
                Log.i("routePlan", address + "dizi");
                if(isGetLocation[0] == false){
                    nodes[0] = PlanNode.withCityNameAndPlaceName("", address);
                    isGetLocation[0] = true;

                    addressSearch.getAdress(targetLatLng);
                }else{
                    nodes[1] = PlanNode.withCityNameAndPlaceName("",address);

                    MassTransitRoutePlan.this.search(new MassTransitRoutePlanOption().from(nodes[0]).to(nodes[1]));
                }
            }
        });

        addressSearch.getAdress(currentLatLng);
    }

    private AddressSearch addressSearch = null;

}
