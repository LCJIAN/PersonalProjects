package com.lcjian.cloudlocation.ui.device;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.entity.PageResult;
import com.lcjian.cloudlocation.data.network.entity.Commands;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.RecyclerFragment;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CommandHistoryActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_fragment);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.command_history));
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        showProgress();
        mDisposable = mRestAPI.cloudService().getDeviceDetail(Long.parseLong(getIntent().getStringExtra("device_id")), "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceDetail -> {
                            hideProgress();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fl_fragment_container, CommandHistoryFragment.newInstance(deviceDetail.id, deviceDetail.sn))
                                    .commit();
                        },
                        throwable -> hideProgress());
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    public static class CommandHistoryFragment extends RecyclerFragment<Commands.Command> {

        private String mDeviceId;
        private String mDeviceSn;
        private SlimAdapter mAdapter;

        public static CommandHistoryFragment newInstance(String deviceId, String deviceSn) {
            CommandHistoryFragment fragment = new CommandHistoryFragment();
            Bundle args = new Bundle();
            args.putSerializable("device_id", deviceId);
            args.putSerializable("device_sn", deviceSn);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mDeviceId = getArguments().getString("device_id");
                mDeviceSn = getArguments().getString("device_sn");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Commands.Command> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Commands.Command>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.command_history_item;
                        }

                        @Override
                        public void onBind(Commands.Command data, SlimAdapter.SlimViewHolder<Commands.Command> viewHolder) {
                            viewHolder.text(R.id.tv_command_name, data.commandName)
                                    .text(R.id.tv_command_status, TextUtils.equals(data.isSend, "1") ? "命令发送成功" : "命令发送失败")
                                    .text(R.id.tv_command_info, data.responseText)
                                    .text(R.id.tv_command_send_time, data.sendDate)
                                    .text(R.id.tv_command_response_time, data.responseDate)
                                    .visibility(R.id.tv_command_info, TextUtils.isEmpty(data.responseText) ? View.GONE : View.VISIBLE)
                                    .visibility(R.id.tv_command_response_time, TextUtils.isEmpty(data.responseDate) ? View.GONE : View.VISIBLE);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Commands.Command>> onCreatePageObservable(int currentPage) {
            return mRestAPI.cloudService().getCommandHistory(mDeviceSn, Long.parseLong(mDeviceId),
                    "", currentPage, 20)
                    .map(commands -> {
                        PageResult<Commands.Command> pageResult = new PageResult<>();
                        pageResult.elements = commands.commandArr;
                        pageResult.page_number = Integer.parseInt(commands.nowPage);
                        pageResult.page_size = 20;
                        pageResult.total_pages = pageResult.elements.size() < 20 ? currentPage : Integer.MAX_VALUE;
                        pageResult.total_elements = Integer.parseInt(commands.resSize);
                        return pageResult;
                    })
                    .toObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        @Override
        public void notifyDataChanged(List<Commands.Command> data) {
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
        }
    }
}
