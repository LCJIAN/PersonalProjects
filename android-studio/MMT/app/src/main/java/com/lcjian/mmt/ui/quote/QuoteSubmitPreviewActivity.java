package com.lcjian.mmt.ui.quote;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.baidu.mapapi.utils.DistanceUtil;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.QuotePrepare;
import com.lcjian.mmt.data.network.entity.TransQuoteForm;
import com.lcjian.mmt.ui.base.AdvanceAdapter;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.BaseDialogFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.ui.main.MainActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class QuoteSubmitPreviewActivity extends BaseActivity implements OnGetRoutePlanResultListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.btn_submit_quote)
    Button btn_submit_quote;

    private MapView map_view;
    private SlimAdapter mAdapter;
    private QuotePrepare mQuotePrepare;
    private Disposable mDisposableBus;
    private Disposable mDisposable;

    private BaiduMap mBMap;
    private RoutePlanSearch mSearch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_submit_preview);
        ButterKnife.bind(this);

        mQuotePrepare = (QuotePrepare) getIntent().getSerializableExtra("quote_prepare");
        tv_title.setText(R.string.quote_it);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_submit_quote.setOnClickListener(v -> showTaxRateDialog());

        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).size(1).build());
        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<TransQuoteForm>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.trans_quote_preview_item;
                    }

                    @Override
                    public void onBind(TransQuoteForm data, SlimAdapter.SlimViewHolder<TransQuoteForm> viewHolder) {
                        viewHolder.text(R.id.tv_car_no, data.car.carCode)
                                .text(R.id.tv_trans_num, String.valueOf(data.abletranNum))
                                .text(R.id.tv_t_price, String.valueOf(data.tprice))
                                .text(R.id.tv_ut_price, String.valueOf(data.utprice));
                    }
                });
        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View header = LayoutInflater.from(this).inflate(R.layout.trans_route, recycler_view, false);
        TextView tv_starting_place_t = header.findViewById(R.id.tv_starting_place_t);
        TextView tv_destination_t = header.findViewById(R.id.tv_destination_t);
        TextView tv_distance_t = header.findViewById(R.id.tv_distance_t);

        tv_starting_place_t.setText(mQuotePrepare.loadStore.address);
        tv_destination_t.setText(mQuotePrepare.unloadedStore.address);

        map_view = header.findViewById(R.id.map_view);
        mBMap = map_view.getMap();
        mBMap.getUiSettings().setCompassEnabled(false);
        mBMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        if (mQuotePrepare.loadStore.latitude != null
                && mQuotePrepare.loadStore.longitude != null
                && mQuotePrepare.unloadedStore.latitude != null
                && mQuotePrepare.unloadedStore.longitude != null) {
            LatLng start = new LatLng(mQuotePrepare.loadStore.latitude, mQuotePrepare.loadStore.longitude);
            LatLng end = new LatLng(mQuotePrepare.unloadedStore.latitude, mQuotePrepare.unloadedStore.longitude);

            mSearch.drivingSearch(new DrivingRoutePlanOption()
                    .from(PlanNode.withLocation(start))
                    .to(PlanNode.withLocation(end)));

            double distance = DistanceUtil.getDistance(start, end);
            String s = (distance > 1000 ? new DecimalFormat("0.00").format(distance / 1000) + "Km" : distance + "m");
            tv_distance_t.setText(s);
        }

        advanceAdapter.addHeader(header);
        recycler_view.setAdapter(advanceAdapter);
        mAdapter.updateData(mQuotePrepare.carsItem);

        mDisposableBus = mRxBus.asFlowable()
                .filter(o -> o instanceof Double)
                .firstOrError()
                .subscribe(o -> submitQuote((Double) o),
                        throwable -> {
                        });
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
        mDisposableBus.dispose();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mSearch != null) {
            mSearch.destroy();
        }
        map_view.onDestroy();
        super.onDestroy();
    }

    private void submitQuote(Double taxRate) {
        mQuotePrepare.id = mQuotePrepare.transRequest.id;
        mQuotePrepare.transRequest = null;
        mQuotePrepare.taxRate = String.valueOf(taxRate);
        showProgress();
        mDisposable = mRestAPI.cloudService()
                .submitQuote(mQuotePrepare)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stringResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            if (stringResponseData.code == 1) {
                                startActivity(new Intent(QuoteSubmitPreviewActivity.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        });
    }

    private void showTaxRateDialog() {
        new FillTaxRateFragment().show(getSupportFragmentManager(), "FillTaxRateFragment");
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

    public static class FillTaxRateFragment extends BaseDialogFragment {

        @BindView(R.id.et_tax_rate)
        EditText et_tax_rate;
        @BindView(R.id.tv_think_twice)
        TextView tv_think_twice;
        @BindView(R.id.tv_confirm_quote)
        TextView tv_confirm_quote;
        Unbinder unbinder;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_fill_tax_rate, container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tv_think_twice.setOnClickListener(v -> dismiss());
            tv_confirm_quote.setOnClickListener(v -> {
                if (TextUtils.isEmpty(et_tax_rate.getEditableText())) {
                    Toast.makeText(App.getInstance(), R.string.please_fill_tax_rate, Toast.LENGTH_SHORT).show();
                } else {
                    mRxBus.send(Double.parseDouble(et_tax_rate.getEditableText().toString()));
                    dismiss();
                }
            });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }
}
