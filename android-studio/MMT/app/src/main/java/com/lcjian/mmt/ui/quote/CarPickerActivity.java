package com.lcjian.mmt.ui.quote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Car;
import com.lcjian.mmt.data.network.entity.QuotePrepare;
import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CarPickerActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        tv_title.setText("选择车辆");
        tv_nav_right.setText(R.string.confirm);
        tv_nav_right.setVisibility(View.VISIBLE);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_nav_right.setOnClickListener(v -> goNext());

        if (getSupportFragmentManager().findFragmentByTag("CarsFragment") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_container,
                            CarsFragment.newInstance(getIntent().getStringExtra("trans_request_id")),
                            "CarsFragment")
                    .commit();
        }
    }

    private void goNext() {
        CarsFragment carsFragment = (CarsFragment) getSupportFragmentManager().findFragmentByTag("CarsFragment");
        if (carsFragment != null) {
            QuotePrepare quotePrepare = carsFragment.prepareQuote();
            if (quotePrepare != null) {
                startActivity(new Intent(this, QuoteFormActivity.class)
                        .putExtra("quote_prepare", quotePrepare));
            }
        }
    }

    public static class CarsFragment extends RecyclerFragment<Car> {

        private SlimAdapter mAdapter;

        private String mTransRequestId;
        private List<Car> mSelected;
        private QuotePrepare mQuotePrepare;
        private Disposable mDisposable;

        public static CarsFragment newInstance(String transRequestId) {
            CarsFragment fragment = new CarsFragment();
            Bundle args = new Bundle();
            args.putString("trans_request_id", transRequestId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mTransRequestId = getArguments().getString("trans_request_id");
            }
            mSelected = new ArrayList<>();
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Car> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Car>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.car_picker_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Car> viewHolder) {
                            viewHolder.clicked(R.id.tv_car_no, v -> {
                                if (mSelected.contains(viewHolder.itemData)) {
                                    mSelected.remove(viewHolder.itemData);
                                } else {
                                    mSelected.add(viewHolder.itemData);
                                }
                                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                            });
                        }

                        @Override
                        public void onBind(Car data, SlimAdapter.SlimViewHolder<Car> viewHolder) {
                            viewHolder.text(R.id.tv_car_no, data.carCode)
                                    .background(R.id.tv_car_no, mSelected.contains(data) ? R.drawable.shape_selected_bg : R.drawable.shape_un_selected_bg);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Car>> onCreatePageObservable(int currentPage) {
            Single<ResponseData<QuotePrepare>> single = mRestAPI.cloudService().getQuotePrepareInfo(mTransRequestId).cache();
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            mDisposable = single.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((quotePrepareResponseData, throwable) -> {
                        if (quotePrepareResponseData != null) {
                            mQuotePrepare = quotePrepareResponseData.data;
                        }
                    });
            return single
                    .map(quotePrepareResponseData -> {
                        PageResult<Car> pageResult = new PageResult<>();
                        if (quotePrepareResponseData.data.cars == null) {
                            quotePrepareResponseData.data.cars = new ArrayList<>();
                        }
                        pageResult.elements = quotePrepareResponseData.data.cars;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = quotePrepareResponseData.data.cars.size();
                        pageResult.total_pages = 1;
                        pageResult.total_elements = quotePrepareResponseData.data.cars.size();
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Car> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new GridLayoutManager(view.getContext(), 4));
            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public void onDestroyView() {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            super.onDestroyView();
        }

        public QuotePrepare prepareQuote() {
            if (mQuotePrepare != null) {
                mQuotePrepare.cars = mSelected;
            }
            return mQuotePrepare;
        }
    }
}

