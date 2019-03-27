package com.lcjian.mmt.ui.logistics;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aitangba.pickdatetime.DatePickDialog;
import com.aitangba.pickdatetime.bean.DateParams;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.CarOrder;
import com.lcjian.mmt.data.network.entity.Driver;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.BaseDialogFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.ui.car.AddDriverActivity;
import com.lcjian.mmt.ui.common.PickerFragment;
import com.lcjian.mmt.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CarOrdersActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        tv_title.setText(R.string.car_order);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        if (getSupportFragmentManager().findFragmentByTag("CarOrdersFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container, CarOrdersFragment.newInstance(getIntent().getStringExtra("trans_order_id")), "CarOrdersFragment").commit();
        }
    }

    public static class CarOrdersFragment extends RecyclerFragment<CarOrder> {

        private String mId;
        private SlimAdapter mAdapter;

        private List<Driver> mDrivers;

        private CompositeDisposable mDisposables;
        private Disposable mDisposable;
        private Disposable mDisposableP;
        private Disposable mDisposableD;

        public static CarOrdersFragment newInstance(String transOrderId) {
            CarOrdersFragment fragment = new CarOrdersFragment();
            Bundle args = new Bundle();
            args.putString("trans_order_id", transOrderId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mId = getArguments().getString("trans_order_id");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<CarOrder> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<CarOrder>() {

                        private final String[] statusArray = new String[]{"空车未出发", "已出车", "待装货", "运输中", "待卸货", "结束", "其他"};

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.car_order_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<CarOrder> viewHolder) {
                            viewHolder
                                    .clicked(R.id.tv_launch, v -> mRxBus.send(new ModifyEvent("launch", viewHolder.itemData)))
                                    .clicked(R.id.tv_set_out, v -> mRxBus.send(new ModifyEvent("set_out", viewHolder.itemData)))
                                    .clicked(R.id.tv_modify, v -> CarOrderModifyOptionFragment.newInstance(viewHolder.itemData)
                                            .show(getChildFragmentManager(), "CarOrderModifyOptionFragment"))
                                    .clicked(R.id.tv_view_detail, v -> v.getContext().startActivity(
                                            new Intent(v.getContext(), CarOrderDetailActivity.class).putExtra("car_order_id", viewHolder.itemData.id)))
                                    .clicked(R.id.tv_upload, v -> v.getContext().startActivity(
                                            new Intent(v.getContext(), UploadPackVoucherActivity.class)
                                                    .putExtra("car_order_id", viewHolder.itemData.id)
                                                    .putExtra("load", Integer.parseInt(viewHolder.itemData.status) == 2)));
                        }

                        @Override
                        public void onBind(CarOrder data, SlimAdapter.SlimViewHolder<CarOrder> viewHolder) {
                            int s = Integer.parseInt(data.status);
                            viewHolder.text(R.id.tv_car_order_no, data.tranOrderCode)
                                    .text(R.id.tv_car_order_status, s <= 5 ? statusArray[s] : statusArray[6])
                                    .text(R.id.tv_car_order_price, "单价：" + data.price / 100 + "元")
                                    .text(R.id.tv_car_order_quantity, "数量：" + data.quantity / 1000 + "吨")
                                    .text(R.id.tv_car_order_amount, "运费：" + data.amount / 100 + "元")
                                    .text(R.id.tv_car_order_car_no, "运输车辆车牌：" + data.cars.carCode)
                                    .text(R.id.tv_car_order_car_driver, "驾驶员：" + (data.cars.driver1 == null ? "暂无" : data.cars.driver1.realname))
                                    .text(R.id.tv_car_order_car_phone, "联系电话：" + (data.cars.driver1 == null ? "暂无" : data.cars.driver1.mobile))
                                    .text(R.id.tv_car_order_customer, "单位：" + (data.cars.driver1 == null ? "暂无" : data.cars.driver1.mobile))
                                    .text(R.id.tv_car_order_unload_time, "预计下货时间：" + DateUtils.convertDateToStr(new Date(data.unloadTime), DateUtils.YYYY_MM_DD_HH_MM_SS));
                            viewHolder
                                    .visibility(R.id.tv_set_out, View.GONE)
                                    .visibility(R.id.tv_modify, View.GONE)
                                    .visibility(R.id.tv_launch, View.GONE)
                                    .visibility(R.id.tv_upload, View.GONE);
                            if (s == 0) {
                                viewHolder.visibility(R.id.tv_set_out, View.VISIBLE) // 出发
                                        .visibility(R.id.tv_modify, View.VISIBLE); // 修改
                            } else if (s == 1) {
                                viewHolder.visibility(R.id.tv_launch, View.VISIBLE)
                                        .text(R.id.tv_launch, R.string.launch_loading); // 发起装货
                            } else if (s == 2) {
                                viewHolder.visibility(R.id.tv_upload, View.VISIBLE); // 上传装货
                            } else if (s == 3) {
                                viewHolder.visibility(R.id.tv_launch, View.VISIBLE)
                                        .text(R.id.tv_launch, R.string.launch_unloading); // 发起卸货
                            } else if (s == 4) {
                                viewHolder.visibility(R.id.tv_upload, View.VISIBLE); // 上传卸货
                            }
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<CarOrder>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getCarOrders(mId)
                    .map(quoteResponsePageData -> {
                        PageResult<CarOrder> pageResult = new PageResult<>();
                        if (quoteResponsePageData.elements == null) {
                            quoteResponsePageData.elements = new ArrayList<>();
                        }
                        pageResult.elements = quoteResponsePageData.elements;
                        pageResult.page_number = 1;
                        pageResult.page_size = quoteResponsePageData.total_elements;
                        pageResult.total_pages = 1;
                        pageResult.total_elements = quoteResponsePageData.total_elements;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<CarOrder> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);

            mDisposables = new CompositeDisposable();
            mDisposable = mRxBus.asFlowable()
                    .filter(o -> o instanceof ModifyEvent)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                                ModifyEvent modifyEvent = (ModifyEvent) o;
                                if (TextUtils.equals("modify_driver", modifyEvent.action)) {
                                    mDisposableP = Observable.fromIterable(mDrivers)
                                            .map(driver -> driver.realname)
                                            .toList()
                                            .subscribe(
                                                    strings -> PickerFragment
                                                            .newInstance(getString(R.string.please_pick_driver), new ArrayList<>(strings))
                                                            .setOnPickListener((item, position) -> modifyDriver(modifyEvent.carOrder, mDrivers.get(position)))
                                                            .setExtraOnClickListener(v1 -> v1.getContext().startActivity(new Intent(v1.getContext(), AddDriverActivity.class)))
                                                            .show(getChildFragmentManager(), "PickerFragment"));
                                } else if (TextUtils.equals("set_out", modifyEvent.action)) {
                                    showSetOutDialog(modifyEvent.carOrder);
                                } else if (TextUtils.equals("launch", modifyEvent.action)) {
                                    showLaunchDialog(modifyEvent.carOrder);
                                } else {
                                    showDatePickDialog(modifyEvent.carOrder);
                                }
                            },
                            throwable -> {
                            });
            getDrivers();
        }

        @Override
        public void onDestroyView() {
            mDisposable.dispose();
            mDisposableD.dispose();
            if (mDisposableP != null) {
                mDisposableP.dispose();
            }
            super.onDestroyView();
        }

        private void getDrivers() {
            showProgress();
            mDisposableD = mRestAPI.cloudService().getDrivers()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            listResponseData -> {
                                hideProgress();
                                mDrivers = listResponseData.data;
                            },
                            throwable -> hideProgress());

        }

        private void modifyDriver(CarOrder carOrder, Driver driver) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .changeCarOrderDriver(carOrder.id, carOrder.id, driver.userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }

        private void modifyArrivalTime(CarOrder carOrder, Long arrivalTime) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .changeCarOrderArrivalTime(carOrder.id, carOrder.id, arrivalTime, null)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }

        private void setOutCarOrder(CarOrder carOrder, int status) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .setOutCarOrder(carOrder.id, carOrder.id, status)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    carOrder.status = String.valueOf(status);
                                    mAdapter.notifyItemChanged(mAdapter.getData().indexOf(carOrder));
                                }
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }

        private void showSetOutDialog(CarOrder carOrder) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.sure_to_set_out)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        setOutCarOrder(carOrder, 1);
                    })
                    .create().show();
        }

        private void showLaunchDialog(CarOrder carOrder) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.sure_to_launch)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        setOutCarOrder(carOrder, 2);
                    })
                    .create().show();
        }

        private void showDatePickDialog(CarOrder carOrder) {
            Calendar todayCal = Calendar.getInstance();
            Calendar startCal = Calendar.getInstance();
            Calendar endCal = Calendar.getInstance();
            endCal.add(Calendar.YEAR, 6);

            new DatePickDialog.Builder()
                    .setTypes(DateParams.TYPE_YEAR,
                            DateParams.TYPE_MONTH,
                            DateParams.TYPE_DAY,
                            DateParams.TYPE_HOUR,
                            DateParams.TYPE_MINUTE)
                    .setCurrentDate(todayCal.getTime())
                    .setStartDate(startCal.getTime())
                    .setEndDate(endCal.getTime())
                    .setTitle("设置预计装货时间")
                    .setOnSureListener(date -> modifyArrivalTime(carOrder, date.getTime()))
                    .show(getActivity());
        }
    }

    public static class CarOrderModifyOptionFragment extends BaseDialogFragment {

        @BindView(R.id.tv_modify_driver)
        TextView tv_modify_driver;
        @BindView(R.id.tv_modify_arrival_time)
        TextView tv_modify_arrival_time;
        @BindView(R.id.tv_cancel)
        TextView tv_cancel;
        Unbinder unbinder;

        private CarOrder mCarOrder;

        public static CarOrderModifyOptionFragment newInstance(CarOrder carOrder) {
            CarOrderModifyOptionFragment fragment = new CarOrderModifyOptionFragment();
            Bundle args = new Bundle();
            args.putSerializable("car_order", carOrder);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mCarOrder = (CarOrder) getArguments().getSerializable("car_order");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_car_order_modify_option, container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            tv_modify_driver.setOnClickListener(v -> {
                mRxBus.send(new ModifyEvent("modify_driver", mCarOrder));
                dismiss();
            });
            tv_modify_arrival_time.setOnClickListener(v -> {
                mRxBus.send(new ModifyEvent("modify_arrival_time", mCarOrder));
                dismiss();
            });
            tv_cancel.setOnClickListener(v -> dismiss());
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }

    private static class ModifyEvent {
        private String action;
        private CarOrder carOrder;

        private ModifyEvent(String action, CarOrder carOrder) {
            this.action = action;
            this.carOrder = carOrder;
        }
    }
}
