package com.lcjian.mmt.ui.car;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.ProductType;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.lcjian.mmt.ui.base.SlimAdapter;
import com.lcjian.mmt.util.DimenUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ProductTypesActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_nav_right)
    TextView tv_nav_right;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.rv_type_1)
    RecyclerView rv_type_1;
    @BindView(R.id.rv_type_2)
    RecyclerView rv_type_2;

    private Disposable mDisposable;

    private SlimAdapter mAdapter;
    private ProductType mFirstChecked;

    private SlimAdapter mAdapter2;

    private List<ProductType> mChecked;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_types);
        ButterKnife.bind(this);
        tv_title.setText(R.string.product_type);
        tv_nav_right.setText(R.string.confirm);
        tv_nav_right.setVisibility(View.VISIBLE);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_nav_right.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra("data", new ArrayList<>(mChecked)));
            finish();
        });

        mChecked = new ArrayList<>();
        if (getIntent().getSerializableExtra("data") != null) {
            mChecked.addAll((ArrayList<ProductType>) getIntent().getSerializableExtra("data"));
        }
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<ProductType>() {

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<ProductType> viewHolder) {
                viewHolder.clicked(R.id.tv_product_type_name, v -> {
                    if (mFirstChecked != null) {
                        mAdapter.notifyItemChanged(mAdapter.getData().indexOf(mFirstChecked));
                    }
                    mFirstChecked = viewHolder.itemData;
                    mAdapter.notifyItemChanged(mAdapter.getData().indexOf(mFirstChecked));

                    setupSecond(mFirstChecked.id);
                });
            }

            @Override
            public int onGetLayoutResource() {
                return R.layout.product_type_first_item;
            }

            @Override
            public void onBind(ProductType data, SlimAdapter.SlimViewHolder<ProductType> viewHolder) {
                Context context = viewHolder.itemView.getContext();
                viewHolder
                        .text(R.id.tv_product_type_name, data.name)
                        .textColor(R.id.tv_product_type_name, ContextCompat.getColor(context, mFirstChecked == data ? R.color.blue : R.color.colorTextLightGray))
                        .background(R.id.tv_product_type_name, new ColorDrawable(ContextCompat.getColor(context, mFirstChecked == data ? R.color.colorWindowBackground : android.R.color.white)));
            }
        });
        rv_type_1.setHasFixedSize(true);
        rv_type_1.setLayoutManager(new LinearLayoutManager(this));
        rv_type_1.setAdapter(mAdapter);

        mAdapter2 = SlimAdapter.create()
                .register(new SlimAdapter.SlimInjector<ProductType>() {

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.product_type_second_item;
                    }

                    @Override
                    public void onBind(ProductType data, SlimAdapter.SlimViewHolder<ProductType> viewHolder) {
                        viewHolder.text(R.id.tv_product_type_name, data.name);
                    }
                })
                .register(new SlimAdapter.SlimInjector<ProductTypes>() {

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.product_type_third_item;
                    }

                    @Override
                    public void onBind(ProductTypes data, SlimAdapter.SlimViewHolder<ProductTypes> viewHolder) {
                        Context context = viewHolder.itemView.getContext();
                        viewHolder.removeAllViews(R.id.fl_product_type);
                        int p = (int) DimenUtils.dipToPixels(8, context);
                        for (ProductType pt : data.list) {
                            AppCompatCheckedTextView textView = new AppCompatCheckedTextView(context);
                            textView.setText(pt.name);
                            textView.setBackgroundResource(R.drawable.shape_check_bg);
                            textView.setTextColor(ContextCompat.getColorStateList(context, R.color.selector_check_text_color));
                            textView.setPadding(p, p, p, p);

                            textView.setOnClickListener(v -> {
                                boolean contains = false;
                                for (ProductType t : mChecked) {
                                    if (TextUtils.equals(t.id, pt.id)) {
                                        contains = true;
                                        mChecked.remove(t);
                                        break;
                                    }
                                }
                                if (!contains) {
                                    mChecked.add(pt);
                                }
                                mAdapter2.notifyItemChanged(mAdapter2.getData().indexOf(data));
                            });

                            boolean contains = false;
                            for (ProductType t : mChecked) {
                                if (TextUtils.equals(t.id, pt.id)) {
                                    contains = true;
                                    break;
                                }
                            }
                            textView.setChecked(contains);

                            FlexboxLayout.LayoutParams layoutParams =
                                    new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.setMargins(p, p / 2, p, p / 2);
                            viewHolder.addView(R.id.fl_product_type, textView, layoutParams);
                        }
                    }
                });
        rv_type_2.setHasFixedSize(true);
        rv_type_2.setLayoutManager(new LinearLayoutManager(this));
        rv_type_2.setAdapter(mAdapter2);
        setupFirst();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    private void setupFirst() {
        showProgress();
        mDisposable = mRestAPI.cloudService().getProductTypes(null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(productTypes -> {
                            hideProgress();
                            mAdapter.updateData(productTypes);
                        },
                        throwable -> hideProgress());
    }

    private void setupSecond(String id) {
        showProgress();
        Single<List<ProductType>> single = mRestAPI.cloudService().getProductTypes(id).cache();
        mDisposable = Single.zip(
                single,
                single.flatMap(productTypes -> Observable
                        .fromIterable(productTypes)
                        .flatMap(productType -> mRestAPI.cloudService().getProductTypes(productType.id).toObservable())
                        .toList()),
                (productTypes, lists) -> {
                    List<Object> objects = new ArrayList<>();
                    int i = 0;
                    for (ProductType pt : productTypes) {
                        objects.add(pt);
                        objects.add(new ProductTypes(lists.get(i)));
                        i++;
                    }
                    return objects;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(objects -> {
                            hideProgress();
                            mAdapter2.updateData(objects);
                        },
                        throwable -> hideProgress());
    }

    private static class ProductTypes {
        private List<ProductType> list;

        private ProductTypes(List<ProductType> list) {
            this.list = list;
        }
    }

}
