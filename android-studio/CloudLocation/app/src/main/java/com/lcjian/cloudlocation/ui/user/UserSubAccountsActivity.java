package com.lcjian.cloudlocation.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.entity.PageResult;
import com.lcjian.cloudlocation.data.network.entity.SubAccounts;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.RecyclerFragment;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.lcjian.cloudlocation.util.Spans;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UserSubAccountsActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.et_search_keyword)
    EditText et_search_keyword;

    private String mUserId;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sub_accounts);
        ButterKnife.bind(this);

        mUserId = getIntent().getStringExtra("user_id");

        tv_title.setText(getString(R.string.account_list));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        et_search_keyword.setHint(new Spans()
                .append("*", new ImageSpan(this, R.drawable.ic_search))
                .append(getString(R.string.search_sub_account_hint)));
        et_search_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mRxBus.send(s.toString());
            }
        });

        showProgress();
        String loginName = mUserInfoSp.getString("sign_in_name", "");
        String password = mUserInfoSp.getString("sign_in_name_pwd", "");
        mDisposable = mRestAPI.cloudService()
                .getUserSubAccounts(Long.parseLong(mUserId),
                        loginName, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subAccounts -> {
                            hideProgress();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fl_fragment_container, UserSubAccountsFragment.newInstance(subAccounts.userList))
                                    .commit();
                            et_search_keyword.postDelayed(() -> mRxBus.send(""), 100);
                        },
                        throwable -> hideProgress());
    }

    public void choose(SubAccounts.SubAccount subAccount) {
        Intent intent = new Intent();
        intent.putExtra("sub_account", subAccount);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    public static class UserSubAccountsFragment extends RecyclerFragment<SubAccounts.SubAccount> {

        private List<SubAccounts.SubAccount> mList;
        private SlimAdapter mAdapter;

        public static UserSubAccountsFragment newInstance(List<SubAccounts.SubAccount> list) {
            UserSubAccountsFragment fragment = new UserSubAccountsFragment();
            Bundle args = new Bundle();
            args.putSerializable("data", new ArrayList<>(list));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mList = (ArrayList<SubAccounts.SubAccount>) getArguments().getSerializable("data");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<SubAccounts.SubAccount> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<SubAccounts.SubAccount>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.sub_account_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<SubAccounts.SubAccount> viewHolder) {
                            viewHolder.clicked(v -> ((UserSubAccountsActivity) getActivity()).choose(viewHolder.itemData));
                        }

                        @Override
                        public void onBind(SubAccounts.SubAccount data, SlimAdapter.SlimViewHolder<SubAccounts.SubAccount> viewHolder) {
                            viewHolder.text(R.id.tv_user_name, data.userName);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<SubAccounts.SubAccount>> onCreatePageObservable(int currentPage) {
            return Observable.combineLatest(Single.just(mList).toObservable(),
                    mRxBus.asFlowable().toObservable().filter(o -> o instanceof String).debounce(500, TimeUnit.MILLISECONDS),
                    (subAccounts, o) -> {
                        if (TextUtils.isEmpty(o.toString())) {
                            return subAccounts;
                        }
                        List<SubAccounts.SubAccount> result = new ArrayList<>();
                        for (SubAccounts.SubAccount subAccount : subAccounts) {
                            if (subAccount.userName.contains(o.toString())) {
                                result.add(subAccount);
                            }
                        }
                        return result;
                    })
                    .map(subAccounts -> {
                        PageResult<SubAccounts.SubAccount> pageResult = new PageResult<>();
                        pageResult.elements = subAccounts;
                        pageResult.page_number = 1;
                        pageResult.page_size = pageResult.elements.size();
                        pageResult.total_pages = 1;
                        pageResult.total_elements = pageResult.elements.size();
                        return pageResult;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<SubAccounts.SubAccount> data) {
            TransitionManager.beginDelayedTransition(swipe_refresh_layout);
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setEnabled(false);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(view.getContext())
                    .size(1)
                    .build());
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
