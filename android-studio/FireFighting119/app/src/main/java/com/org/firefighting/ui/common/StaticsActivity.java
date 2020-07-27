package com.org.firefighting.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.App;
import com.org.firefighting.GlideApp;
import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.ResponseData;
import com.org.firefighting.data.network.entity.StaticsInfo;
import com.org.firefighting.ui.base.BaseActivity;
import com.org.firefighting.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class StaticsActivity extends BaseActivity {

    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.rv_statics)
    RecyclerView rv_statics;

    private SlimAdapter mAdapter;

    private Disposable mDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statics);
        ButterKnife.bind(this);

        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText(R.string.statics);

        rv_statics.setHasFixedSize(true);
        rv_statics.setLayoutManager(new GridLayoutManager(this, 2));
        mAdapter = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<StaticsInfo>() {
                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.statics_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<StaticsInfo> viewHolder) {
                        viewHolder.clicked(v -> {
                            Intent intent;
                            if (viewHolder.itemData.isRotate == 1) {
                                intent = new Intent(v.getContext(), WebViewActivityH.class)
                                        .putExtra("url", viewHolder.itemData.webviewlink);
                            } else {
                                intent = new Intent(v.getContext(), WebViewActivity.class)
                                        .putExtra("url", viewHolder.itemData.webviewlink)
                                        .putExtra("title", viewHolder.itemData.name)
                                        .putExtra("swipe_disabled", true);
                            }
                            startActivity(intent);
                        });
                    }

                    @Override
                    public void onBind(StaticsInfo data, SlimAdapter.SlimViewHolder<StaticsInfo> viewHolder) {
                        viewHolder.text(R.id.tv_name, data.name)
                                .with(R.id.iv_image, v -> GlideApp.with(v).load(data.logo_path).into((ImageView) v));
                    }
                });
        rv_statics.setAdapter(mAdapter);
        getData();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    private void getData() {
        showProgress();
        mDisposable = Single.just("http://47.241.26.39/datalist.html")
                .map(aString -> new Gson().<ResponseData<List<StaticsInfo>>>fromJson(
                        Utils.get(aString, null, null),
                        new TypeToken<ResponseData<List<StaticsInfo>>>() {
                        }.getType()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            if (responseData.code == 0) {
                                mAdapter.updateData(responseData.data);
                            } else {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Timber.e(throwable);
                        });
    }

}
