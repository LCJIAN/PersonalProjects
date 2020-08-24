package com.org.firefighting.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lcjian.lib.recyclerview.AdvanceAdapter;
import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.DutyInfo;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DutyStaticsFragment extends BaseFragment {

    @BindView(R.id.rv_duty_statics)
    RecyclerView rv_duty_statics;

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private List<DutyInfo.DutyItem> mDuties;
    private DutyAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_duty_statics, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_duty_statics.setHasFixedSize(true);
        rv_duty_statics.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mDuties = new ArrayList<>();
        mAdapter = new DutyAdapter(mDuties);

        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View footer = LayoutInflater.from(view.getContext()).inflate(R.layout.department_users_footer, rv_duty_statics, false);
        TextView tv_total = footer.findViewById(R.id.tv_total);
        tv_total.setText("~数据加载完成，下拉刷新~");
        advanceAdapter.addFooter(footer);

        rv_duty_statics.setAdapter(advanceAdapter);
        getData();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void getData() {
        mDisposable = Single.just("http://124.162.30.39:10001/zbdt/getZb/50000000")
                .map(aString -> new Gson().fromJson(
                        Utils.get(aString, null, null)
                                .replace("var data = ", ""),
                        DutyInfo.class))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dutyInfo -> {
                            mDuties.clear();
                            List<DutyInfo.DutyItem> items = dutyInfo.data;
                            if (items != null) {
                                mDuties.addAll(items);
                            }
                            mAdapter.notifyDataSetChanged();
                        },
                        Timber::e);
    }

    private static class DutyAdapter extends RecyclerView.Adapter<DutyAdapter.DutyViewHolder> {

        private List<DutyInfo.DutyItem> mData;

        public DutyAdapter(List<DutyInfo.DutyItem> data) {
            this.mData = data;
        }

        @NonNull
        @Override
        public DutyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.duty_item, parent, false);
            DutyViewHolder holder = new DutyViewHolder(view);
            holder.text = view.findViewById(R.id.text);
            holder.text1 = view.findViewById(R.id.text1);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull DutyViewHolder holder, int position) {
            DutyInfo.DutyItem item = mData.get(position);
            holder.text.setText(item.name);
            holder.text1.setText(item.type);
        }

        @Override
        public int getItemCount() {
            return mData == null || mData.isEmpty() ? 0 : mData.size();
        }

        static class DutyViewHolder extends RecyclerView.ViewHolder {

            TextView text;
            TextView text1;

            public DutyViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
