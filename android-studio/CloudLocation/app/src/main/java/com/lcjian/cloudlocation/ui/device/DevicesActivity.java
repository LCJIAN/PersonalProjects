package com.lcjian.cloudlocation.ui.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.entity.PageResult;
import com.lcjian.cloudlocation.data.network.entity.Devices;
import com.lcjian.cloudlocation.data.network.entity.SignInInfo;
import com.lcjian.cloudlocation.data.network.entity.SubAccounts;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.BaseFragment;
import com.lcjian.cloudlocation.ui.base.RecyclerFragment;
import com.lcjian.cloudlocation.ui.base.SimpleFragmentPagerAdapter;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.lcjian.cloudlocation.ui.user.UserSubAccountsActivity;
import com.lcjian.cloudlocation.util.Spans;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DevicesActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.cl_choose_account)
    ConstraintLayout cl_choose_account;
    @BindView(R.id.et_search_keyword)
    EditText et_search_keyword;

    private SubAccounts.SubAccount mSubAccount;
    private Long mUserId;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.device_list));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        cl_choose_account.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), UserSubAccountsActivity.class), 1000));
        et_search_keyword.setHint(new Spans()
                .append("*", new ImageSpan(this, R.drawable.ic_search))
                .append(getString(R.string.search_device_hint)));
        et_search_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mRxBus.send(s);
            }
        });

        setupContent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                mSubAccount = (SubAccounts.SubAccount) data.getSerializableExtra("sub_account");
                setupContent();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void setupContent() {
        SignInInfo signInInfo = getSignInInfo();
        Long id;
        Integer typeId;
        if (signInInfo.userInfo == null) {
            cl_choose_account.setVisibility(View.GONE);
            id = Long.parseLong(signInInfo.deviceInfo.deviceID);
            typeId = 1;
        } else {
            cl_choose_account.setVisibility(View.VISIBLE);
            if (mSubAccount == null) {
                tv_user_name.setText(signInInfo.userInfo.userName);
            } else {
                tv_user_name.setText(mSubAccount.userName);
            }
            typeId = 0;
            Long userId = Long.parseLong(mSubAccount == null ? signInInfo.userInfo.userID : mSubAccount.userID);
            if (userId.equals(mUserId)) {
                return;
            }
            mUserId = userId;
            id = mUserId;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        showProgress();
        mDisposable = mRestAPI.cloudService().getDevices(id,
                typeId, true, 1, 1000)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(devices -> {
                            hideProgress();
                            List<Devices.Device> online = new ArrayList<>();
                            List<Devices.Device> offline = new ArrayList<>();
                            if (devices.arr == null) {
                                devices.arr = new ArrayList<>();
                            }
                            for (Devices.Device device : devices.arr) {
                                String s = device.status.split("-")[0];
                                if (TextUtils.equals("3", s)) {
                                    offline.add(device);
                                } else if (TextUtils.equals("1", s)
                                        || TextUtils.equals("2", s)) {
                                    online.add(device);
                                }
                            }
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fl_fragment_container,
                                            DevicesContentFragment.newInstance(devices.arr, online, offline), "DevicesContentFragment")
                                    .commit();
                            et_search_keyword.postDelayed(() -> mRxBus.send(et_search_keyword.getEditableText()), 100);
                        },
                        throwable -> hideProgress());
    }

    public static class DevicesContentFragment extends BaseFragment {

        @BindView(R.id.tab_device)
        TabLayout tab_device;
        @BindView(R.id.vp_device)
        ViewPager vp_device;
        Unbinder unbinder;

        private List<Devices.Device> all;
        private List<Devices.Device> online;
        private List<Devices.Device> offline;

        public static DevicesContentFragment newInstance(List<Devices.Device> all,
                                                         List<Devices.Device> online,
                                                         List<Devices.Device> offline) {
            DevicesContentFragment fragment = new DevicesContentFragment();
            Bundle args = new Bundle();
            args.putSerializable("all", new ArrayList<>(all));
            args.putSerializable("online", new ArrayList<>(online));
            args.putSerializable("offline", new ArrayList<>(offline));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                all = (ArrayList<Devices.Device>) getArguments().getSerializable("all");
                online = (ArrayList<Devices.Device>) getArguments().getSerializable("online");
                offline = (ArrayList<Devices.Device>) getArguments().getSerializable("offline");
            }
        }


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_devices_content, container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            vp_device.setOffscreenPageLimit(3);
            tab_device.setupWithViewPager(vp_device);

            vp_device.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                    .addFragment(DevicesFragment.newInstance(all), "全部" + "(" + all.size() + ")")
                    .addFragment(DevicesFragment.newInstance(online), "在线" + "(" + online.size() + ")")
                    .addFragment(DevicesFragment.newInstance(offline), "离线" + "(" + offline.size() + ")"));
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }


    public static class DevicesFragment extends RecyclerFragment<Devices.Device> {

        private List<Devices.Device> mList;
        private SlimAdapter mAdapter;

        public static DevicesFragment newInstance(List<Devices.Device> list) {
            DevicesFragment fragment = new DevicesFragment();
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
                mList = (ArrayList<Devices.Device>) getArguments().getSerializable("data");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Devices.Device> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Devices.Device>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.device_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Devices.Device> viewHolder) {
                            viewHolder.clicked(v -> {
                                Activity activity = (Activity) v.getContext();
                                assert activity != null;

                                Intent intent = new Intent();
                                intent.putExtra("device_id", viewHolder.itemData.id);
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            });
                        }

                        @Override
                        public void onBind(Devices.Device data, SlimAdapter.SlimViewHolder<Devices.Device> viewHolder) {
                            String[] ar = data.status.split("-");

                            String strStatus;
                            switch (data.status.split("-")[0]) {
                                case "0":
                                    strStatus = "未启用";
                                    break;
                                case "1":
                                    strStatus = "运动";
                                    break;
                                case "2":
                                    strStatus = "静止";
                                    break;
                                case "3":
                                    strStatus = "离线";
                                    break;
                                case "4":
                                    strStatus = "欠费";
                                    break;
                                default:
                                    strStatus = "未启用";
                                    break;
                            }
                            viewHolder.image(R.id.iv_device, TextUtils.equals("1", ar[0]) || TextUtils.equals("2", ar[0])
                                    ? R.drawable.ic_device_active : R.drawable.ic_device_inactive)
                                    .text(R.id.tv_device_name_label, data.name + "【" + strStatus + "】")
                                    .text(R.id.tv_device_status, viewHolder.itemView.getContext().getString(R.string.device_no_c) + data.sn);
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Devices.Device>> onCreatePageObservable(int currentPage) {
            return Observable.combineLatest(Single.just(mList).toObservable(),
                    mRxBus.asFlowable().toObservable().filter(o -> o instanceof Editable).debounce(500, TimeUnit.MILLISECONDS),
                    (devices, o) -> {
                        if (TextUtils.isEmpty(o.toString())) {
                            return devices;
                        }
                        List<Devices.Device> result = new ArrayList<>();
                        for (Devices.Device device : devices) {
                            if (device.name.contains(o.toString())) {
                                result.add(device);
                            }
                        }
                        return result;
                    })
                    .map(devices -> {
                        PageResult<Devices.Device> pageResult = new PageResult<>();
                        pageResult.elements = devices;
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
        public void notifyDataChanged(List<Devices.Device> data) {
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
