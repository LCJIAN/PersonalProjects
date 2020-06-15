package com.org.firefighting.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.AdvanceAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.User2;
import com.org.firefighting.ui.base.BaseActivity;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DepartmentUsersActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.srl_department_user)
    SwipeRefreshLayout srl_department_user;
    @BindView(R.id.rv_department_user)
    RecyclerView rv_department_user;

    private TextView tv_total;

    private String mDeptCode;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_users);
        ButterKnife.bind(this);
        mDeptCode = getIntent().getStringExtra("dept_code");

        btn_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText(getIntent().getStringExtra("dept_name"));
        srl_department_user.setColorSchemeResources(R.color.colorPrimary);
        srl_department_user.setOnRefreshListener(this::setupContent);

        rv_department_user.setHasFixedSize(true);
        rv_department_user.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<User2>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.department_user_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<User2> viewHolder) {
                viewHolder.clicked(v -> {

                });
            }

            @Override
            public void onBind(User2 data, SlimAdapter.SlimViewHolder<User2> viewHolder) {
                viewHolder
                        .background(R.id.ll_department_user,
                                viewHolder.getAbsoluteAdapterPosition() == 0 ? R.drawable.shape_card_top :
                                        (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? R.drawable.shape_card_bottom :
                                                R.drawable.shape_card_middle))
                        .visibility(R.id.v_divider, viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? View.INVISIBLE : View.VISIBLE)
                        .with(R.id.iv_user_avatar, view -> GlideApp.with(view).load(data.avatar).placeholder(R.drawable.default_avatar).into((ImageView) view))
                        .text(R.id.tv_user_name, data.realName);
            }
        });

        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View footer = LayoutInflater.from(this).inflate(R.layout.department_users_footer, rv_department_user, false);
        tv_total = footer.findViewById(R.id.tv_total);
        tv_total.setText(getString(R.string.total_user_count, 0));
        advanceAdapter.addFooter(footer);
        rv_department_user.setAdapter(advanceAdapter);

        setupContent();
    }

    private void setupContent() {
        setRefreshing(true);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = RestAPI.getInstance().apiServiceC().getUsersByDepartment(mDeptCode)
                .map(pageResponse -> {
                    Collections.reverse(pageResponse.result);
                    return pageResponse;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pageResponse -> {
                            setRefreshing(false);
                            mAdapter.updateData(pageResponse.result);
                            tv_total.setText(getString(R.string.total_user_count, pageResponse.result.size()));
                        },
                        throwable -> {
                            ThrowableConsumerAdapter.accept(throwable);
                            setRefreshing(false);
                        });
    }

    private void setRefreshing(final boolean refreshing) {
        if (srl_department_user.isEnabled()) {
            srl_department_user.post(() -> {
                if (srl_department_user != null) {
                    srl_department_user.setRefreshing(refreshing);
                }
            });
        }
    }

}