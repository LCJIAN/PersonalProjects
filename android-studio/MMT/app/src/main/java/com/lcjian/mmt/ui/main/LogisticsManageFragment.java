package com.lcjian.mmt.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.TransOrder;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.ui.logistics.CarOrdersActivity;
import com.lcjian.mmt.ui.logistics.MessagesActivity;
import com.lcjian.mmt.ui.logistics.OrderDetailActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LogisticsManageFragment extends BaseFragment {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logistics_manage, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_title.setText(R.string.action_logistics);
        btn_nav_back.setVisibility(View.GONE);
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.ic_msg);
        btn_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), MessagesActivity.class)));
        if (getChildFragmentManager().findFragmentByTag("TranOrdersFragment") == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_container, new TranOrdersFragment(), "TranOrdersFragment").commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static class TranOrdersFragment extends RecyclerFragment<TransOrder> {

        private SlimAdapter mAdapter;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<TransOrder> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<TransOrder>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.tran_order_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<TransOrder> viewHolder) {
                            viewHolder.clicked(R.id.tv_view_detail, v -> v.getContext().startActivity(
                                    new Intent(v.getContext(), OrderDetailActivity.class).putExtra("trans_order_id", viewHolder.itemData.id)))
                                    .clicked(R.id.tv_view_cars, v -> v.getContext().startActivity(
                                            new Intent(v.getContext(), CarOrdersActivity.class).putExtra("trans_order_id", viewHolder.itemData.id)));
                        }

                        @Override
                        public void onBind(TransOrder data, SlimAdapter.SlimViewHolder<TransOrder> viewHolder) {
                            String status = "";
                            switch (Integer.parseInt(data.tranStatus)) {
                                case 0:
                                    status = "未开始";
                                    break;
                                case 1:
                                    status = "进行中";
                                    break;
                                case 2:
                                    status = "已结束";
                                    break;
                                case 3:
                                    status = "取消中";
                                    break;
                                case 4:
                                    status = "已取消";
                                    break;
                            }
                            viewHolder.text(R.id.tv_order_no, data.tranOrderCode)
                                    .text(R.id.tv_order_status, status)
                                    .text(R.id.tv_order_product_name, data.product.name)
                                    .text(R.id.tv_order_product_spec, data.product.model)
                                    .text(R.id.tv_order_freight, String.valueOf(data.amount / 100) + "元");
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<TransOrder>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getTransOrders((currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<TransOrder> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<TransOrder> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
