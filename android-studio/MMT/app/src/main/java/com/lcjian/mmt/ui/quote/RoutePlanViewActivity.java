package com.lcjian.mmt.ui.quote;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.TransRequest;
import com.lcjian.mmt.ui.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RoutePlanViewActivity extends BaseActivity implements OnGetRoutePlanResultListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.map_view)
    MapView map_view;

    private TransRequest mTransRequest;

    private BaiduMap mBMap;
    private RoutePlanSearch mSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan_view);
        ButterKnife.bind(this);
        mTransRequest = (TransRequest) getIntent().getSerializableExtra("trans_request");

        tv_title.setText("路线");
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        mBMap = map_view.getMap();
        mBMap.getUiSettings().setCompassEnabled(false);
        mBMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        if (mTransRequest.product.mmtStores.latitude != null
                && mTransRequest.product.mmtStores.longitude != null
                && mTransRequest.inquiry.store.latitude != null
                && mTransRequest.inquiry.store.longitude != null) {
            LatLng start = new LatLng(mTransRequest.product.mmtStores.latitude, mTransRequest.product.mmtStores.longitude);
            LatLng end = new LatLng(mTransRequest.inquiry.store.latitude, mTransRequest.inquiry.store.longitude);

            mSearch.drivingSearch(new DrivingRoutePlanOption()
                    .from(PlanNode.withLocation(start))
                    .to(PlanNode.withLocation(end)));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map_view.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map_view.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mSearch != null) {
            mSearch.destroy();
        }
        map_view.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(App.getInstance(), "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            for (DrivingRouteLine drivingRouteLine : result.getRouteLines()) {
                DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBMap);
                mBMap.setOnMarkerClickListener(overlay);
                overlay.setData(drivingRouteLine);
                overlay.addToMap();
                overlay.zoomToSpan();
            }
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }
}
