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

import com.lcjian.lib.recyclerview.AdvanceAdapter;
import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DateUtils;
import com.org.firefighting.R;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class RealTimeStaticsFragment extends BaseFragment {

    @BindView(R.id.rv_real_time_statics)
    RecyclerView rv_real_time_statics;

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private List<Map<String, String>> mStatics;
    private StaticsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_time_statics, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_real_time_statics.setHasFixedSize(true);
        rv_real_time_statics.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mStatics = new ArrayList<>();
        mAdapter = new StaticsAdapter(mStatics);

        AdvanceAdapter advanceAdapter = new AdvanceAdapter(mAdapter);
        View footer = LayoutInflater.from(view.getContext()).inflate(R.layout.department_users_footer, rv_real_time_statics, false);
        TextView tv_total = footer.findViewById(R.id.tv_total);
        tv_total.setText("~数据加载完成，下拉刷新~");
        advanceAdapter.addFooter(footer);

        rv_real_time_statics.setAdapter(advanceAdapter);
        getData();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void getData() {
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .getRealTimePoliceStatics(DateUtils.convertDateToStr(DateUtils.now()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            mStatics.clear();
                            mStatics.addAll(responseData.data);
                            mAdapter.notifyDataSetChanged();
                        },
                        Timber::e);
    }

    private static class StaticsAdapter extends RecyclerView.Adapter<StaticsAdapter.StaticsViewHolder> {

        private static final String[] mArr = new String[]{"案件类型", "案发地址", "状态", "接警时间", "主管机构"};

        private List<Map<String, String>> mData;

        public StaticsAdapter(List<Map<String, String>> data) {
            this.mData = data;
        }

        @NonNull
        @Override
        public StaticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.real_time_statics_item, parent, false);
            StaticsViewHolder holder = new StaticsViewHolder(view);
            holder.tv_title = view.findViewById(R.id.tv_title);
            holder.tv_time = view.findViewById(R.id.tv_time);
            holder.tv_status = view.findViewById(R.id.tv_status);
            holder.tv_content = view.findViewById(R.id.tv_content);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull StaticsViewHolder holder, int position) {
            Map<String, String> map = mData.get(position % mData.size());
            holder.tv_title.setText(new Spans().append(map.get(mArr[0])).append(" - ").append(map.get(mArr[4])));
            holder.tv_time.setText(map.get(mArr[3]));
            holder.tv_status.setText(map.get(mArr[2]));
            holder.tv_content.setText(map.get(mArr[1]));
        }

        @Override
        public int getItemCount() {
            return mData == null || mData.isEmpty() ? 0 : mData.size();
        }

        static class StaticsViewHolder extends RecyclerView.ViewHolder {

            TextView tv_title;
            TextView tv_time;
            TextView tv_status;
            TextView tv_content;

            public StaticsViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}