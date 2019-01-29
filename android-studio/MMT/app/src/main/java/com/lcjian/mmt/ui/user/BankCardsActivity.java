package com.lcjian.mmt.ui.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcjian.mmt.GlideApp;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.BankCard;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BankCardsActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        ButterKnife.bind(this);

        tv_title.setText("银行卡");
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.ic_add);
        btn_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), AddBankCardActivity.class)));

        if (getSupportFragmentManager().findFragmentByTag("BankCardsFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container, new BankCardsFragment(), "BankCardsFragment").commit();
        }
    }

    public static class BankCardsFragment extends RecyclerFragment<BankCard> {

        private SlimAdapter mAdapter;

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<BankCard> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<BankCard>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.bank_card_item;
                        }

                        @Override
                        public void onBind(BankCard data, SlimAdapter.SlimViewHolder<BankCard> viewHolder) {
                            viewHolder.text(R.id.tv_bank_card_bank, data.openBank)
                                    .text(R.id.tv_bank_card_no, data.cardNo)
                                    .text(R.id.tv_bank_card_type, data.cardType)
                                    .with(R.id.cl_bank_card, view -> view.setBackgroundColor(Color.parseColor(data.remarks)))
                                    .with(R.id.iv_bank_card_bank, view -> GlideApp.with(view).load(data.logoUrl).into((ImageView) view));
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<BankCard>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getBankCards((currentPage - 1) * 20, 20)
                    .map(quoteResponsePageData -> {
                        PageResult<BankCard> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<BankCard> data) {
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
