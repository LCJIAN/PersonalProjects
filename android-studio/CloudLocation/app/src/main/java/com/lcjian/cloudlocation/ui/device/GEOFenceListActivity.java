package com.lcjian.cloudlocation.ui.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.entity.PageResult;
import com.lcjian.cloudlocation.data.network.entity.GEOFences;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.RecyclerFragment;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class GEOFenceListActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;

    private MonitorInfo.MonitorDevice mMonitorDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_fragment);
        ButterKnife.bind(this);
        mMonitorDevice = (MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device");

        tv_title.setText(getString(R.string.geo_fence));
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.dzwl_tj);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(), GEOFenceEditActivity.class)
                .putExtra("monitor_device", mMonitorDevice)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, GEOFencesFragment.newInstance(mMonitorDevice))
                .commit();
    }

    public static class GEOFencesFragment extends RecyclerFragment<GEOFences.GEOFence> {

        private MonitorInfo.MonitorDevice mMonitorDevice;
        private SlimAdapter mAdapter;
        private CompositeDisposable mDisposables;

        public static GEOFencesFragment newInstance(MonitorInfo.MonitorDevice monitorDevice) {
            GEOFencesFragment fragment = new GEOFencesFragment();
            Bundle args = new Bundle();
            args.putSerializable("monitor_device", monitorDevice);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mMonitorDevice = (MonitorInfo.MonitorDevice) getArguments().getSerializable("monitor_device");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<GEOFences.GEOFence> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<GEOFences.GEOFence>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.geo_fence_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<GEOFences.GEOFence> viewHolder) {
                            viewHolder.clicked(v -> v.getContext().startActivity(new Intent(v.getContext(), GEOFenceEditActivity.class)
                                    .putExtra("monitor_device", mMonitorDevice)
                                    .putExtra("geo_fence", viewHolder.itemData)
                            ));
                            viewHolder.longClicked(v -> {
                                showDeleteDialog(v.getContext(), viewHolder.itemData);
                                return true;
                            });
                        }

                        @Override
                        public void onBind(GEOFences.GEOFence data, SlimAdapter.SlimViewHolder<GEOFences.GEOFence> viewHolder) {
                            viewHolder.image(R.id.iv_fence_type,
                                    TextUtils.equals("0", data.FenceType) ? R.drawable.bjwl_j
                                            : (TextUtils.equals("1", data.FenceType) ? R.drawable.bjwl_gs : R.drawable.bjwl_qt))
                                    .text(R.id.tv_fence_name, data.fenceName)
                                    .text(R.id.tv_fence_lat, viewHolder.itemView.getContext().getString(R.string.lat) + data.lat)
                                    .text(R.id.tv_fence_lng, viewHolder.itemView.getContext().getString(R.string.lng) + data.lng)
                                    .text(R.id.tv_fence_r, viewHolder.itemView.getContext().getString(R.string.radius) + data.radius)
                                    .visibility(R.id.iv_fence_in_remind, TextUtils.equals("1", data.Entry) ? View.VISIBLE : View.GONE)
                                    .visibility(R.id.iv_fence_out_remind, TextUtils.equals("1", data.Exit) ? View.VISIBLE : View.GONE);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<GEOFences.GEOFence>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getGEOFence(Long.parseLong(mMonitorDevice.id), "", "Baidu")
                    .map(geoFences -> {
                        PageResult<GEOFences.GEOFence> pageResult = new PageResult<>();
                        if (geoFences.geofences == null) {
                            geoFences.geofences = new ArrayList<>();
                        }
                        pageResult.elements = geoFences.geofences;
                        pageResult.page_number = 1;
                        pageResult.page_size = pageResult.elements.size();
                        pageResult.total_pages = 1;
                        pageResult.total_elements = pageResult.elements.size();
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<GEOFences.GEOFence> data) {
            mAdapter.updateData(data);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
            recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recycler_view.addItemDecoration(new HorizontalDividerItemDecoration.Builder(view.getContext())
                    .size(1)
                    .build());
            super.onViewCreated(view, savedInstanceState);
            mDisposables = new CompositeDisposable();
        }

        @Override
        public void onDestroyView() {
            mDisposables.dispose();
            super.onDestroyView();
        }

        private void showDeleteDialog(Context context, GEOFences.GEOFence data) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.sure_to_delete)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.dismiss();
                        deleteGEOFence(data);
                    })
                    .create().show();
        }

        @SuppressWarnings("unchecked")
        private void deleteGEOFence(GEOFences.GEOFence data) {
            mDisposables.add(mRestAPI.cloudService()
                    .deleteGEOFence(Long.parseLong(mMonitorDevice.id), Long.parseLong(data.geofenceID))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(state -> {
                        if (TextUtils.equals("0", state.state)) {
                            Toast.makeText(App.getInstance(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
                        } else {
                            List<GEOFences.GEOFence> list = new ArrayList<>((List<GEOFences.GEOFence>) mAdapter.getData());
                            list.remove(data);
                            mAdapter.updateData(list);
                        }
                    }, throwable -> {
                    }));
        }
    }
}
