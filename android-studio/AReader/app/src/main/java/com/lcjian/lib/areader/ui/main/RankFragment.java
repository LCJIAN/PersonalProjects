package com.lcjian.lib.areader.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.lcjian.lib.areader.R;
import com.lcjian.lib.areader.RxBus;
import com.lcjian.lib.areader.data.entity.RankType;
import com.lcjian.lib.areader.data.entity.ResponseData;
import com.lcjian.lib.areader.data.network.RestAPI;
import com.lcjian.lib.areader.ui.base.BaseFragment;
import com.lcjian.lib.areader.ui.base.SimpleFragmentPagerAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 排行榜
 *
 * @author LCJIAN
 */
public class RankFragment extends BaseFragment {

    @BindView(R.id.tab_gender)
    TabLayout tab_gender;
    @BindView(R.id.rv_rank_category)
    RecyclerView rv_rank_category;
    @BindView(R.id.vp_rank)
    ViewPager vp_rank;

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private SimpleFragmentPagerAdapter mPagerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_rank_category.setHasFixedSize(true);
        rv_rank_category.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mPagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(RankListFragment.newInstance(1), getString(R.string.gender_male))
                .addFragment(RankListFragment.newInstance(2), getString(R.string.gender_female));
        tab_gender.setupWithViewPager(vp_rank);
        tab_gender.setSelectedTabIndicatorColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));

        mDisposable = RestAPI.getInstance().readerService().rankTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResponseData<List<RankType>>>() {
                    @Override
                    public void accept(ResponseData<List<RankType>> listResponseData) {
                        rv_rank_category.setAdapter(new RankCategoryAdapter(listResponseData.data));
                        vp_rank.setAdapter(mPagerAdapter);

                        if (listResponseData.data != null && !listResponseData.data.isEmpty()) {
                            RxBus.getInstance().send(listResponseData.data.get(0));
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
        mDisposable.dispose();
    }

    static class RankCategoryAdapter extends RecyclerView.Adapter<RankCategoryAdapter.RankCategoryViewHolder> {

        private List<RankType> mData;

        private RankType mChecked;

        RankCategoryAdapter(List<RankType> data) {
            this.mData = data;
            this.mChecked = data == null || data.isEmpty() ? null : data.get(0);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @NonNull
        @Override
        public RankCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RankCategoryViewHolder(parent, this);
        }

        @Override
        public void onBindViewHolder(@NonNull RankCategoryViewHolder holder, int position) {
            holder.bindTo(mData.get(position), mChecked);
        }

        static class RankCategoryViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_rank_category_name)
            CheckedTextView tv_rank_category_name;

            RankType rankCategory;
            RankCategoryAdapter mAdapter;

            RankCategoryViewHolder(ViewGroup parent, RankCategoryAdapter adapter) {
                super(LayoutInflater.from(parent.getContext()).inflate(R.layout.rank_category_item, parent, false));
                ButterKnife.bind(this, this.itemView);
                this.mAdapter = adapter;
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAdapter.mChecked = rankCategory;
                        mAdapter.notifyDataSetChanged();
                        RxBus.getInstance().send(rankCategory);
                    }
                });
            }

            void bindTo(RankType rc, RankType checked) {
                this.rankCategory = rc;
                tv_rank_category_name.setText(rankCategory.name);
                tv_rank_category_name.setChecked(checked == rc);
            }
        }
    }
}
