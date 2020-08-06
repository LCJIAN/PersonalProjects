package com.org.firefighting.ui.resource;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lcjian.lib.recyclerview.SlimAdapter;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.Dir;
import com.org.firefighting.data.network.entity.DirRoot;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TabPopFragment extends BaseFragment {

    @BindView(R.id.fl_tab_pop)
    FrameLayout fl_tab_pop;
    @BindView(R.id.rv_tab)
    RecyclerView rv_tab;

    private Unbinder mUnBinder;

    private SlimAdapter mAdapter;

    private Listener mListener;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_pop, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fl_tab_pop.setOnClickListener(v -> onBackPressed());

        rv_tab.setHasFixedSize(false);
        rv_tab.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<DirRoot>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.tab_pop_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<DirRoot> viewHolder) {
                        TextView tv_tab_name = viewHolder.findViewById(R.id.tv_tab_name);
                        tv_tab_name.setTypeface(Typeface.DEFAULT_BOLD);
                        tv_tab_name.setPadding((int) DimenUtils.dipToPixels(16, viewHolder.itemView.getContext()), 0, 0, 0);
                        viewHolder.clicked(v -> {
                            if (mListener != null) {
                                mListener.onTabClicked(viewHolder.itemData);
                            }
                            onBackPressed();
                        });
                    }

                    @Override
                    public void onBind(DirRoot data, SlimAdapter.SlimViewHolder<DirRoot> viewHolder) {
                        viewHolder.text(R.id.tv_tab_name, data.label);
                    }
                })
                .register(new SlimAdapter.SlimInjector<Dir>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.tab_pop_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<Dir> viewHolder) {
                        viewHolder.clicked(v -> {
                            if (mListener != null) {
                                mListener.onTabClicked(viewHolder.itemData);
                            }
                            onBackPressed();
                        });
                    }

                    @Override
                    public void onBind(Dir data, SlimAdapter.SlimViewHolder<Dir> viewHolder) {
                        TextView tv_tab_name = viewHolder.findViewById(R.id.tv_tab_name);
                        if (data.bold) {
                            tv_tab_name.setTypeface(Typeface.DEFAULT_BOLD);
                        } else {
                            tv_tab_name.setTypeface(Typeface.DEFAULT);
                        }
                        tv_tab_name.setPadding((int) DimenUtils.dipToPixels(data.first ? 32 : 48, viewHolder.itemView.getContext()),
                                0, 0, 0);
                        tv_tab_name.setText(data.showName);
                    }
                });
        rv_tab.setAdapter(mAdapter);

        getData();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void getData() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB().getResourceDirs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            hideProgress();
                            List<Object> data = new ArrayList<>();
                            for (DirRoot dr : listResponseData.data) {
                                dr.label = dr.label.replace("资源目录", "");
                                data.add(dr);
                                if (dr.children != null && !dr.children.isEmpty()) {
                                    for (Dir dir : dr.children) {
                                        dir.first = true;
                                        data.add(dir);
                                        if (dir.children != null && !dir.children.isEmpty()) {
                                            dir.bold = true;
                                            data.addAll(dir.children);
                                        }
                                    }
                                }
                            }
                            mAdapter.updateData(data);
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    public TabPopFragment setListener(Listener l) {
        this.mListener = l;
        return this;
    }

    public interface Listener {

        void onTabClicked(DirRoot dirRoot);

        void onTabClicked(Dir dir);
    }

    private void onBackPressed() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
