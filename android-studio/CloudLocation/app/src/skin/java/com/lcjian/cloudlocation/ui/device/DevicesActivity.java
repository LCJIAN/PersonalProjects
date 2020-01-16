package com.lcjian.cloudlocation.ui.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.Global;
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
import com.lcjian.cloudlocation.util.DimenUtils;
import com.lcjian.cloudlocation.util.Spans;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.ViewPager;
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
    private String mUserId;
    private String mUserName;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.device_list));
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        cl_choose_account.setOnClickListener(v -> startActivityForResult(
                new Intent(v.getContext(), UserSubAccountsActivity.class).putExtra("user_id", mUserId),
                1000));
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
        String id;
        String loginName = null;
        String password = null;
        int typeId;
        if (signInInfo.userInfo == null) {
            cl_choose_account.setVisibility(View.GONE);
            id = signInInfo.deviceInfo.deviceID;
            typeId = 1;
        } else {
            cl_choose_account.setVisibility(View.VISIBLE);
            if (mSubAccount == null) {
                if (TextUtils.isEmpty(Global.CURRENT_USER_NAME)) {
                    mUserName = signInInfo.userInfo.userName;
                } else {
                    mUserName = (Global.CURRENT_USER_NAME);
                }
            } else {
                mUserName = (mSubAccount.userName);
            }
            tv_user_name.setText(mUserName);

            typeId = 0;
            loginName = mUserInfoSp.getString("sign_in_name", "");
            password = mUserInfoSp.getString("sign_in_name_pwd", "");

            String userId = mSubAccount != null ? mSubAccount.userID
                    : (TextUtils.isEmpty(Global.CURRENT_USER_ID) ? signInInfo.userInfo.userID : Global.CURRENT_USER_ID);
            if (TextUtils.equals(userId, mUserId)) {
                return;
            }
            mUserId = userId;
            id = mUserId;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        showProgress();
        mDisposable = mRestAPI.cloudService().getDevices(Long.parseLong(id), loginName, password,
                typeId, true, 1, 5000)
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
                                            DevicesContentFragment.newInstance(devices.arr, online, offline, mUserId, mUserName),
                                            "DevicesContentFragment")
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
        private String mCurrentUserId;
        private String mCurrentUserName;

        public static DevicesContentFragment newInstance(List<Devices.Device> all,
                                                         List<Devices.Device> online,
                                                         List<Devices.Device> offline,
                                                         String currentUserId,
                                                         String currentUserName) {
            DevicesContentFragment fragment = new DevicesContentFragment();
            Bundle args = new Bundle();
            args.putString("current_user_id", currentUserId);
            args.putString("current_user_name", currentUserName);
            fragment.setArguments(args);
            fragment.setData(all, online, offline);
            return fragment;
        }

        private void setData(List<Devices.Device> all,
                             List<Devices.Device> online,
                             List<Devices.Device> offline) {
            this.all = all;
            this.online = online;
            this.offline = offline;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mCurrentUserId = getArguments().getString("current_user_id");
                mCurrentUserName = getArguments().getString("current_user_name");
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
                    .addFragment(DevicesFragment.newInstance(all, mCurrentUserId, mCurrentUserName), getString(R.string.all_device) + "(" + all.size() + ")")
                    .addFragment(DevicesFragment.newInstance(online, mCurrentUserId, mCurrentUserName), getString(R.string.online_device) + "(" + online.size() + ")")
                    .addFragment(DevicesFragment.newInstance(offline, mCurrentUserId, mCurrentUserName), getString(R.string.offline_device) + "(" + offline.size() + ")"));
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
        private String mCurrentUserId;
        private String mCurrentUserName;

        public static DevicesFragment newInstance(List<Devices.Device> list, String currentUserId, String currentUserName) {
            DevicesFragment fragment = new DevicesFragment();
            Bundle args = new Bundle();
            args.putString("current_user_id", currentUserId);
            args.putString("current_user_name", currentUserName);
            fragment.setData(list);
            fragment.setArguments(args);
            return fragment;
        }

        private void setData(List<Devices.Device> list) {
            this.mList = list;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mCurrentUserId = getArguments().getString("current_user_id");
                mCurrentUserName = getArguments().getString("current_user_name");
            }
        }

        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Devices.Device> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Devices.Device>() {

                        private int p;
                        private int p2;

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.device_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Devices.Device> viewHolder) {
                            if (p == 0) {
                                p = (int) DimenUtils.dipToPixels(2, viewHolder.itemView.getContext());
                                p2 = (int) DimenUtils.dipToPixels(4, viewHolder.itemView.getContext());
                            }
                            viewHolder.clicked(v -> {
                                Activity activity = (Activity) v.getContext();
                                assert activity != null;

                                if (TextUtils.equals("0", viewHolder.itemData.status.split("-")[0])) {
                                    Toast.makeText(App.getInstance(), R.string.un_used, Toast.LENGTH_SHORT).show();
                                } else if (TextUtils.equals("4", viewHolder.itemData.status.split("-")[0])) {
                                    Toast.makeText(App.getInstance(), R.string.arrears, Toast.LENGTH_SHORT).show();
                                } else {
                                    Global.CURRENT_USER_ID = mCurrentUserId;
                                    Global.CURRENT_USER_NAME = mCurrentUserName;
                                    Intent intent = new Intent();
                                    intent.putExtra("device_id", viewHolder.itemData.id);
                                    activity.setResult(RESULT_OK, intent);
                                    activity.finish();
                                }
                            });
                        }

                        @Override
                        public void onBind(Devices.Device data, SlimAdapter.SlimViewHolder<Devices.Device> viewHolder) {
                            String[] ar = data.status.split("-");

                            String strStatus;
                            switch (data.status.split("-")[0]) {
                                case "0":
                                    strStatus = getString(R.string.un_used);
                                    break;
                                case "1":
                                    strStatus = getString(R.string.moving);
                                    break;
                                case "2":
                                    strStatus = getString(R.string.status_static);
                                    break;
                                case "3":
                                    strStatus = getString(R.string.offline);
                                    break;
                                case "4":
                                    strStatus = getString(R.string.arrears);
                                    break;
                                default:
                                    strStatus = getString(R.string.un_used);
                            }
                            ImageView iv_device = viewHolder.findViewById(R.id.iv_device);
                            if (TextUtils.equals(data.icon, "Pos_Min.png")) {
                                iv_device.setPadding(0,0,0,0);
                                iv_device.setBackgroundResource(TextUtils.equals("1", ar[0]) ? R.drawable.ic_device_active
                                        : (TextUtils.equals("2", ar[0]) ? R.drawable.ic_device_static
                                        : R.drawable.ic_device_inactive));
                                iv_device.setImageDrawable(null);
                            } else {
                                iv_device.setPadding(p,p,p,p);
                                iv_device.setBackgroundResource(TextUtils.equals("1", ar[0]) ? R.drawable.shape_icon_bg_sport
                                        : (TextUtils.equals("2", ar[0]) ? R.drawable.shape_icon_bg_static
                                        : R.drawable.shape_icon_bg_offline));
                                Glide.with(iv_device).load("file:///android_asset/icon/" + data.icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(p2)))
                                        .into(iv_device);
                            }

                            viewHolder
                                    .text(R.id.tv_device_name_label, data.name + "(" + strStatus + ")")
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
