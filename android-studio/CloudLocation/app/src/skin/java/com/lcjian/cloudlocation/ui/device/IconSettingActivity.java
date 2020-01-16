package com.lcjian.cloudlocation.ui.device;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lcjian.cloudlocation.App;
import com.lcjian.cloudlocation.R;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.ui.base.BaseActivity;
import com.lcjian.cloudlocation.ui.base.SlimAdapter;
import com.lcjian.cloudlocation.util.DimenUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IconSettingActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.btn_nav_right)
    ImageButton btn_nav_right;
    @BindView(R.id.rv_icon)
    RecyclerView rv_icon;

    private MonitorInfo.MonitorDevice mMonitorDevice;

    private SlimAdapter mAdapter;
    private Icon mChecked;

    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_setting);
        ButterKnife.bind(this);
        mMonitorDevice = (MonitorInfo.MonitorDevice) getIntent().getSerializableExtra("monitor_device");

        tv_title.setText(getString(R.string.icon));
        btn_nav_right.setVisibility(View.VISIBLE);
        btn_nav_right.setImageResource(R.drawable.bjwl_bc);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_nav_right.setOnClickListener(v -> {
            if (mChecked == null)
                return;
            showProgress();
            mDisposable = mRestAPI.cloudService().updateDeviceIcon(Long.parseLong(mMonitorDevice.id), mChecked.name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(state -> {
                                hideProgress();
                                if (TextUtils.equals("0", state.state)) {
                                    Toast.makeText(App.getInstance(), R.string.save_failed, Toast.LENGTH_SHORT).show();
                                } else {
                                    finish();
                                }
                            },
                            throwable -> hideProgress());
        });

        rv_icon.setHasFixedSize(true);
        rv_icon.setLayoutManager(new GridLayoutManager(this, 4));

        List<Icon> icons = new ArrayList<>();
        for (int i = 1; i <= 83; i++) {
            Icon icon = new Icon();
            icon.name = i + ".jpg";
            icons.add(icon);
            if (TextUtils.equals(icon.name, mMonitorDevice.icon)) {
                mChecked = icon;
            }
        }

        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Icon>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.icon_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Icon> viewHolder) {
                viewHolder.clicked(v -> {
                    mChecked = viewHolder.itemData;
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onBind(Icon data, SlimAdapter.SlimViewHolder<Icon> viewHolder) {
                viewHolder
                        .with(R.id.iv_icon, view -> {
                            Glide.with(view).load("file:///android_asset/icon/" + data.name)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(
                                            (int) DimenUtils.dipToPixels(4, view.getContext()))))
                                    .into((ImageView) view);
                            view.setBackgroundResource(mChecked == data ? R.drawable.shape_icon_bg : 0);
                        })
                        .visibility(R.id.iv_icon_check, mChecked == data ? View.VISIBLE : View.INVISIBLE)
                ;
            }
        });
        rv_icon.setAdapter(mAdapter);
        mAdapter.updateData(icons);
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private static class Icon {
        private String name;
    }

}
