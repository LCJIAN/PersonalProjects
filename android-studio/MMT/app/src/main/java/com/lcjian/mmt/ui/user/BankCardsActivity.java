package com.lcjian.mmt.ui.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.GlideApp;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.BankCard;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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

        tv_title.setText(getString(R.string.bank_card));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.ic_add);
        btn_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), AddBankCardActivity.class)));

        if (getSupportFragmentManager().findFragmentByTag("BankCardsFragment") == null) {
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fl_fragment_container,
                    BankCardsFragment.newInstance(TextUtils.equals(WithdrawalActivity.class.getSimpleName(), getIntent().getStringExtra("from"))),
                    "BankCardsFragment")
                    .commit();
        }
    }

    private void ss() {

    }

    public static class BankCardsFragment extends RecyclerFragment<BankCard> {

        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;
        private boolean mForResult;

        public static BankCardsFragment newInstance(boolean result) {
            BankCardsFragment fragment = new BankCardsFragment();
            Bundle args = new Bundle();
            args.putBoolean("for_result", result);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mForResult = getArguments().getBoolean("for_result");
            }
        }

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
                        public void onInit(SlimAdapter.SlimViewHolder<BankCard> viewHolder) {
                            viewHolder.clicked(R.id.tv_delete_b, v -> showDeleteDialog(v.getContext(), viewHolder.itemData));
                            if (mForResult) {
                                viewHolder.clicked(R.id.sl_bank_card, v -> {
                                    Intent intent = new Intent();
                                    intent.putExtra("bank_card", viewHolder.itemData);
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.setResult(Activity.RESULT_OK, intent);
                                        activity.finish();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onBind(BankCard data, SlimAdapter.SlimViewHolder<BankCard> viewHolder) {
                            viewHolder.text(R.id.tv_bank_card_bank, data.openBank)
                                    .text(R.id.tv_bank_card_no, data.cardNo)
                                    .text(R.id.tv_bank_card_type, data.cardType)
                                    .with(R.id.cl_bank_card, view -> view.setBackgroundColor(Color.parseColor("#" + data.remarks)))
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
            mDisposables = new CompositeDisposable();
        }

        @Override
        public void onDestroyView() {
            mDisposables.dispose();
            super.onDestroyView();
        }

        private void showDeleteDialog(Context context, BankCard data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteBankCard(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void deleteBankCard(BankCard data) {
            showProgress();
            mDisposables.add(mRestAPI.cloudService()
                    .deleteBankCards(data.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stringResponseData -> {
                                hideProgress();
                                if (stringResponseData.code == 1) {
                                    List<BankCard> list = new ArrayList<>((List<BankCard>) mAdapter.getData());
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
