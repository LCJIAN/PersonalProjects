package com.org.firefighting.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.AdvanceAdapter;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.chat.SmackClient;
import com.org.firefighting.App;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.User2;
import com.org.firefighting.ui.base.BaseActivity;

import org.jetbrains.annotations.NotNull;

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
                    if (SharedPreferencesDataSource.getSignInResponse().user.id.equals(viewHolder.itemData.id)) {
                        Toast.makeText(App.getInstance(), "你不能与自已创建会话", Toast.LENGTH_SHORT).show();
                    } else {
                        startActivity(new Intent(v.getContext(), ChatActivity.class)
                                .putExtra("owner_jid", SharedPreferencesDataSource.getSignInResponse().user.id + "@" + SmackClient.DOMAIN)
                                .putExtra("opposite_jid", viewHolder.itemData.id + "@" + SmackClient.DOMAIN)
                                .putExtra("opposite_name", viewHolder.itemData.realName));
                    }
                });
            }

            @Override
            public void onBind(User2 data, SlimAdapter.SlimViewHolder<User2> viewHolder) {
                Context context = viewHolder.itemView.getContext();
                Spans spans = new Spans()
                        .append(data.realName, buildSpanChat(data))
                        .append(TextUtils.isEmpty(data.job.name) ? "  " : "  " + data.job.name,
                                new AbsoluteSizeSpan(DimenUtils.spToPixels(12, context)), new ForegroundColorSpan(0xff999999),
                                buildSpanChat(data));
                if (!TextUtils.isEmpty(data.phone)) {
                    spans
                            .append("  ")
                            .append("*",
                                    new ImageSpan(context, R.drawable.ic_phone, DynamicDrawableSpan.ALIGN_BASELINE),
                                    buildSpanPhone(data))
                            .append(data.phone,
                                    new AbsoluteSizeSpan(DimenUtils.spToPixels(12, context)),
                                    new ForegroundColorSpan(0xff666666),
                                    buildSpanPhone(data));
                }
                TextView tv_user_name = viewHolder.findViewById(R.id.tv_user_name);
                tv_user_name.setText(spans);
                tv_user_name.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolder
                        .background(R.id.ll_department_user,
                                viewHolder.getAbsoluteAdapterPosition() == 0 ? R.drawable.shape_card_top :
                                        (viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? R.drawable.shape_card_bottom :
                                                R.drawable.shape_card_middle))
                        .visibility(R.id.v_divider, viewHolder.getAbsoluteAdapterPosition() == mAdapter.getData().size() - 1 ? View.INVISIBLE : View.VISIBLE)
                        .with(R.id.iv_user_avatar, view -> GlideApp
                                .with(view)
                                .load("http://124.162.30.39:9528/admin-ht/" + data.avatar)
                                .placeholder(R.drawable.default_avatar)
                                .circleCrop()
                                .into((ImageView) view));
            }

            private ClickableSpan buildSpanChat(User2 data) {
                return new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        widget.getContext().startActivity(new Intent(widget.getContext(), ChatActivity.class)
                                .putExtra("owner_jid", SharedPreferencesDataSource.getSignInResponse().user.id + "@" + SmackClient.DOMAIN)
                                .putExtra("opposite_jid", data.id + "@" + SmackClient.DOMAIN)
                                .putExtra("opposite_name", data.realName));
                    }

                    @Override
                    public void updateDrawState(@NotNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                };
            }

            private ClickableSpan buildSpanPhone(User2 data) {
                return new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + data.phone));
                        widget.getContext().startActivity(intent);
                    }

                    @Override
                    public void updateDrawState(@NotNull TextPaint ds) {
                        ds.setUnderlineText(false);
                    }
                };
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
        mDisposable = RestAPI.getInstance().apiServiceSB().getUsersByDepartment(mDeptCode)
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