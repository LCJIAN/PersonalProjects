package com.org.firefighting.ui.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.Triple;
import com.lcjian.lib.util.common.DimenUtils;
import com.lcjian.lib.util.common.SoftKeyboardUtils;
import com.org.chat.SmackClient;
import com.org.firefighting.App;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Department;
import com.org.firefighting.data.network.entity.User2;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.ChatActivity;
import com.org.firefighting.ui.chat.DepartmentUsersActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DepartmentsFragment extends BaseFragment {

    @BindView(R.id.srl_department)
    SwipeRefreshLayout srl_department;
    @BindView(R.id.rv_department)
    RecyclerView rv_department;

    @BindView(R.id.et_keyword)
    EditText et_keyword;
    @BindView(R.id.btn_search)
    ImageButton btn_search;

    private Unbinder mUnBinder;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_departments, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_search.setOnClickListener(v -> setupContent());

        et_keyword.setOnEditorActionListener((v, actionId, event) -> {
            if (!TextUtils.isEmpty(et_keyword.getEditableText())) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setupContent();
                }
            }
            return false;
        });

        srl_department.setColorSchemeResources(R.color.colorPrimary);
        srl_department.setOnRefreshListener(this::setupContent);

        rv_department.setHasFixedSize(true);
        rv_department.setLayoutManager(new LinearLayoutManager(rv_department.getContext()));
        rv_department.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(0, (int) DimenUtils.dipToPixels(5, parent.getContext()), 0, 0);
            }
        });

        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<Department>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.department_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<Department> viewHolder) {
                        viewHolder.clicked(v -> startActivity(new Intent(v.getContext(), DepartmentUsersActivity.class)
                                .putExtra("dept_code", viewHolder.itemData.code)
                                .putExtra("dept_name", viewHolder.itemData.name))
                        );
                    }

                    @Override
                    public void onBind(Department data, SlimAdapter.SlimViewHolder<Department> viewHolder) {
                        viewHolder.text(R.id.tv_department_name, data.name);
                    }
                })
                .register(new SlimAdapter.SlimInjector<User2>() {
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
                                .append(
                                        (TextUtils.isEmpty(data.dept.name) ? "  " : "  " + data.dept.name) +
                                                (TextUtils.isEmpty(data.job.name) ? "" : "-" + data.job.name),
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
                                .background(R.id.ll_department_user, R.drawable.shape_card)
                                .visibility(R.id.v_divider, View.INVISIBLE)
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
        rv_department.setAdapter(mAdapter);

        setupContent();
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
        super.onDestroyView();
    }

    private void setupContent() {
        setRefreshing(true);
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (TextUtils.isEmpty(et_keyword.getEditableText())) {
            mDisposable = RestAPI.getInstance().apiServiceSB().getDepartments()
                    .flatMapObservable(departments -> Observable.fromIterable(departments.result))
                    .flatMap(department -> {
                        Collections.reverse(department.children);
                        return Observable.fromIterable(department.children);
                    })
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                                setRefreshing(false);
                                mAdapter.updateData(list);
                            },
                            throwable -> {
                                setRefreshing(false);
                                ThrowableConsumerAdapter.accept(throwable);
                            });
        } else {
//            mDisposable = Single
//                    .just(et_keyword.getEditableText().toString())
//                    .zipWith(RestAPI.getInstance().apiServiceSB().getDepartments()
//                                    .flatMapObservable(departments -> Observable.fromIterable(departments.result))
//                                    .flatMap(department -> Observable.fromIterable(department.children))
//                                    .publish(departmentObservable -> departmentObservable
//                                            .zipWith(departmentObservable
//                                                            .flatMap(department ->
//                                                                    RestAPI.getInstance().apiServiceSB()
//                                                                            .getUsersByDepartment(department.code)
//                                                                            .map(user2PageResponse -> user2PageResponse.result).toObservable()),
//                                                    Pair::create))
//                                    .toList(),
//                            Pair::create)
//                    .map(stringListPair -> {
//                        assert stringListPair.first != null;
//                        assert stringListPair.second != null;
//                        String keyword = stringListPair.first;
//                        List<Pair<Department, List<User2>>> list = stringListPair.second;
//
//                        List<Object> result = new ArrayList<>();
//                        for (Pair<Department, List<User2>> p : list) {
//                            Department d = p.first;
//                            List<User2> l = p.second;
//                            for (User2 u : l) {
//                                if (u.realName.contains(keyword)) {
//                                    result.add(u);
//                                }
//                            }
//                            if (d.name.contains(keyword)) {
//                                result.add(d);
//                            }
//                        }
//                        return result;
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(list -> {
//                                setRefreshing(false);
//                                mAdapter.updateData(list);
//                            },
//                            throwable -> {
//                                setRefreshing(false);
//                                Timber.e(throwable);
//                            });
            SoftKeyboardUtils.hideSoftInput(et_keyword.getContext());
            mDisposable = Single
                    .zip(Single.just(et_keyword.getEditableText().toString()),
                            RestAPI.getInstance().apiServiceSB().getDepartments()
                                    .flatMapObservable(departments -> Observable.fromIterable(departments.result))
                                    .flatMap(department -> {
                                        Collections.reverse(department.children);
                                        return Observable.fromIterable(department.children);
                                    })
                                    .toList(),
                            RestAPI.getInstance().apiServiceSB().getAllUsers().map(pageResponse -> pageResponse.result),
                            Triple::create)
                    .map(triple -> {
                        assert triple.first != null;
                        assert triple.second != null;
                        assert triple.third != null;
                        String keyword = triple.first;
                        List<Department> departments = triple.second;
                        List<User2> users = triple.third;

                        List<Object> result = new ArrayList<>();
                        for (Department d : departments) {
                            if (d.name.contains(keyword)) {
                                result.add(d);
                            }
                        }
                        for (User2 u : users) {
                            if (u.realName.contains(keyword)) {
                                result.add(u);
                            }
                        }
                        return result;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                                setRefreshing(false);
                                mAdapter.updateData(list);
                            },
                            throwable -> {
                                setRefreshing(false);
                                Timber.e(throwable);
                            });
        }
    }

    private void setRefreshing(final boolean refreshing) {
        if (srl_department.isEnabled()) {
            srl_department.post(() -> {
                if (srl_department != null) {
                    srl_department.setRefreshing(refreshing);
                }
            });
        }
    }

}
