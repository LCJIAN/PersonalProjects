package com.lcjian.mmt.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ThrowableConsumerAdapter;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.TransRequest;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SimpleFragmentPagerAdapter;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.ui.quote.CarPickerActivity;
import com.lcjian.mmt.ui.quote.RequestDetailActivity;
import com.lcjian.mmt.ui.quote.RoutePlanViewActivity;
import com.lcjian.mmt.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
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

public class QuoteManageFragment extends BaseFragment {

    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.vp)
    ViewPager vp;

    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vp_trans_request, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vp.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(TransRequestsFragment.newInstance(1), "待报价")
                .addFragment(TransRequestsFragment.newInstance(2), "已报价")
                .addFragment(TransRequestsFragment.newInstance(3), "弃价"));
        vp.setOffscreenPageLimit(3);
        tab.setupWithViewPager(vp);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static class TransRequestsFragment extends RecyclerFragment<TransRequest> {

        private Integer mStatus;
        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        public static TransRequestsFragment newInstance(Integer status) {
            TransRequestsFragment fragment = new TransRequestsFragment();
            Bundle args = new Bundle();
            args.putInt("status", status);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mStatus = getArguments().getInt("status");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<TransRequest> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<TransRequest>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.trans_request_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<TransRequest> viewHolder) {
                            viewHolder
                                    .clicked(R.id.tv_distance_label, v -> v.getContext().startActivity(new Intent(v.getContext(), RoutePlanViewActivity.class)
                                            .putExtra("trans_request", viewHolder.itemData)))
                                    .clicked(R.id.tv_distance, v -> v.getContext().startActivity(new Intent(v.getContext(), RoutePlanViewActivity.class)
                                            .putExtra("trans_request", viewHolder.itemData)))
                                    .clicked(R.id.cl_trans_request, v -> v.getContext().startActivity(
                                            new Intent(v.getContext(), RequestDetailActivity.class).putExtra("trans_request_id", viewHolder.itemData.id)))
                                    .clicked(R.id.tv_quote, v -> validateQuote(v.getContext(), viewHolder.itemData))
                                    .clicked(R.id.tv_quote_not, v -> showQuoteNotDialog(v.getContext(), viewHolder.itemData))
                                    .clicked(R.id.tv_delete, v -> showDeleteDialog(v.getContext(), viewHolder.itemData));
                        }

                        @Override
                        public void onBind(TransRequest data, SlimAdapter.SlimViewHolder<TransRequest> viewHolder) {
                            viewHolder.gone(R.id.tv_quote)
                                    .gone(R.id.tv_quote_not)
                                    .gone(R.id.tv_delete);
                            String s = "";
                            switch (Integer.parseInt(data.status)) {
                                case 1:
                                    s = "报价中";
                                    viewHolder.visible(R.id.tv_quote)
                                            .visible(R.id.tv_quote_not);
                                    break;
                                case 2:
                                    s = "已报价";
                                    break;
                                case 3:
                                    s = "报价失效";
                                    viewHolder.visible(R.id.tv_delete);
                                    break;
                                case 4:
                                    s = "已结束";
                                    break;
                                case 9:
                                    s = "运输中";
                                    break;
                            }
                            viewHolder.text(R.id.tv_product_name, data.product.shortName)
                                    .text(R.id.tv_status, s)
                                    .text(R.id.tv_starting, data.product.mmtStores.address)
                                    .text(R.id.tv_destination, data.inquiry.unloadAddr)
                                    .text(R.id.tv_distance, data.distance + "Km")
                                    .text(R.id.tv_time, data.quoteTime == null ? "" : DateUtils.convertDateToStr(new Date(data.quoteTime)));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<TransRequest>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getTransRequests((currentPage - 1) * 20, 20, mStatus)
                    .map(quoteResponsePageData -> {
                        PageResult<TransRequest> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<TransRequest> data) {
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

        private void validateQuote(Context context, TransRequest data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .validateQuote(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                                if (stringResponseData.code == 1) {
                                    context.startActivity(new Intent(context, CarPickerActivity.class)
                                            .putExtra("trans_request_id", data.id));
                                }
                            },
                            throwable -> {
                                hideProgress();
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        }

        private void showQuoteNotDialog(Context context, TransRequest data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_quote_not)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        quoteNotTransRequest(data);
                    })
                    .create().show();
        }

        private void showDeleteDialog(Context context, TransRequest data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteTransRequest(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void quoteNotTransRequest(TransRequest data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .quoteNotTransRequest(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<TransRequest> list = new ArrayList<>((List<TransRequest>) mAdapter.getData());
                                    list.remove(data);
                                    mAdapter.updateData(list);
                                } else {
                                    Toast.makeText(App.getInstance(), stringResponseData.data, Toast.LENGTH_SHORT).show();
                                }
                            },
                            throwable -> {
                                hideProgress();
                                ThrowableConsumerAdapter.accept(throwable);
                            }));
        }

        @SuppressWarnings("unchecked")
        private void deleteTransRequest(TransRequest data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .deleteTransRequest(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<TransRequest> list = new ArrayList<>((List<TransRequest>) mAdapter.getData());
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
