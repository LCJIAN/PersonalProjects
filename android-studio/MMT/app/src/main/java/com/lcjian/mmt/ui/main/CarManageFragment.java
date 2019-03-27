package com.lcjian.mmt.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Car;
import com.lcjian.mmt.data.network.entity.Driver;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SimpleFragmentPagerAdapter;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.ui.car.AddCarActivity;
import com.lcjian.mmt.ui.car.AddDriverActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CarManageFragment extends BaseFragment {

    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.vp)
    ViewPager vp;
    @BindView(R.id.btn_add)
    ImageButton btn_add;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vp_car, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vp.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(new CarsFragment(), "车辆管理")
                .addFragment(new DriversFragment(), "驾驶员管理"));
        vp.setOffscreenPageLimit(2);
        tab.setupWithViewPager(vp);

        btn_add.setOnClickListener(v -> {
            if (vp.getCurrentItem() == 0) {
                v.getContext().startActivity(new Intent(v.getContext(), AddCarActivity.class));
            } else {
                v.getContext().startActivity(new Intent(v.getContext(), AddDriverActivity.class));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static class CarsFragment extends RecyclerFragment<Car> {

        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Car> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Car>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.car_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Car> viewHolder) {
                            viewHolder.clicked(R.id.tv_delete_c, v -> showDeleteDialog(v.getContext(), viewHolder.itemData));
                        }

                        @Override
                        public void onBind(Car data, SlimAdapter.SlimViewHolder<Car> viewHolder) {
                            String status = "";
                            switch (Integer.parseInt(data.checkStatus)) {
                                case 0:
                                    status = "审核中";
                                    break;
                                case 1:
                                    status = "已认证";
                                    break;
                                case 2:
                                    status = "未通过";
                                    break;
                                default:
                                    break;
                            }
                            StringBuilder goodsTypeS = new StringBuilder();
                            switch (Integer.parseInt(TextUtils.substring(data.goodsType, 0, 1))) {
                                case 1:
                                    goodsTypeS.append("固体");
                                    break;
                                case 2:
                                    goodsTypeS.append("液体");
                                    break;
                                case 3:
                                    goodsTypeS.append("气体");
                                    break;
                                default:
                                    break;
                            }
                            switch (Integer.parseInt(TextUtils.substring(data.goodsType, 1, 2))) {
                                case 1:
                                    goodsTypeS.append(",危化品");
                                    break;
                                case 2:
                                    goodsTypeS.append(",普通货物");
                                    break;
                                case 3:
                                    goodsTypeS.append(",生鲜类货物");
                                    break;
                                default:
                                    break;
                            }

                            viewHolder.text(R.id.tv_car_no, data.carCode)
                                    .text(R.id.tv_car_owner, data.owner == null ? "暂无" : data.owner.name)
                                    .text(R.id.tv_driver, data.driver1 == null ? "暂无" : data.driver1.realname)
                                    .text(R.id.tv_contact_phone, data.driver1 == null ? "暂无" : data.driver1.mobile)
                                    .text(R.id.tv_available, goodsTypeS.toString())
                                    .text(R.id.tv_available_weight, String.valueOf(data.loadWeight / 1000) + "吨")
                                    .text(R.id.tv_car_status_label, status);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Car>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getCars((currentPage - 1) * 20, 20, null)
                    .map(quoteResponsePageData -> {
                        PageResult<Car> pageResult = new PageResult<>();
                        if (quoteResponsePageData.elements == null) {
                            quoteResponsePageData.elements = new ArrayList<>();
                        }
                        pageResult.elements = quoteResponsePageData.elements;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = quoteResponsePageData.total_elements % 20 == 0
                                ? quoteResponsePageData.total_elements / 20
                                : quoteResponsePageData.total_elements / 20 + 1;
                        pageResult.total_elements = quoteResponsePageData.total_elements;
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
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
            mDisposables = new CompositeDisposable();
        }

        @Override
        public void onDestroyView() {
            mDisposables.dispose();
            super.onDestroyView();
        }

        private void showDeleteDialog(Context context, Car data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteCar(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void deleteCar(Car data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .deleteCars(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<Car> list = new ArrayList<>((List<Car>) mAdapter.getData());
                                    list.remove(data);
                                    mAdapter.updateData(list);
                                } else {
                                    Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }
    }

    public static class DriversFragment extends RecyclerFragment<Driver> {

        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Driver> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Driver>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.driver_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Driver> viewHolder) {
                            viewHolder.clicked(R.id.tv_delete_d, v -> showDeleteDialog(v.getContext(), viewHolder.itemData));
                        }

                        @Override
                        public void onBind(Driver data, SlimAdapter.SlimViewHolder<Driver> viewHolder) {
                            viewHolder.text(R.id.tv_driver_name, data.realname)
                                    .text(R.id.tv_driver_phone, data.mobile)
                                    .text(R.id.tv_driver_car_no, data.remarks);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Driver>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getDrivers((currentPage - 1) * 20, 20, null)
                    .map(quoteResponsePageData -> {
                        PageResult<Driver> pageResult = new PageResult<>();
                        if (quoteResponsePageData.elements == null) {
                            quoteResponsePageData.elements = new ArrayList<>();
                        }
                        pageResult.elements = quoteResponsePageData.elements;
                        pageResult.page_number = currentPage;
                        pageResult.page_size = 20;
                        pageResult.total_pages = quoteResponsePageData.total_elements % 20 == 0
                                ? quoteResponsePageData.total_elements / 20
                                : quoteResponsePageData.total_elements / 20 + 1;
                        pageResult.total_elements = quoteResponsePageData.total_elements;
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Driver> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
            mDisposables = new CompositeDisposable();
        }

        @Override
        public void onDestroyView() {
            mDisposables.dispose();
            super.onDestroyView();
        }

        private void showDeleteDialog(Context context, Driver data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteDriver(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void deleteDriver(Driver data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .deleteDriver(data.userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<Driver> list = new ArrayList<>((List<Driver>) mAdapter.getData());
                                    list.remove(data);
                                    mAdapter.updateData(list);
                                } else {
                                    Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }

    }
}
