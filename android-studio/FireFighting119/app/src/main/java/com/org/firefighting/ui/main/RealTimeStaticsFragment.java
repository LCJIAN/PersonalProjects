package com.org.firefighting.ui.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.nekocode.badge.BadgeDrawable;
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
            holder.tv_info = view.findViewById(R.id.tv_info);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull StaticsViewHolder holder, int position) {
            Map<String, String> map = mData.get(position);

            String label = map.get(mArr[2]);
            int badgeColor = 0xff248af9;
            if (TextUtils.equals(label, "出动")) {
                badgeColor = 0xfff28901;
            } else if (TextUtils.equals(label, "接警")) {
                badgeColor = 0xffc31a16;
            } else if (TextUtils.equals(label, "返队")) {
                badgeColor = 0xff119336;
            }
            SpannableString badge = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(badgeColor)
                    .typeFace(Typeface.DEFAULT)
                    .textSize(DimenUtils.spToPixels(10, holder.itemView.getContext()))
                    .textColor(0xffffffff)
                    .text1(" " + label + " ")
                    .build()
                    .toSpannable();

            holder.tv_title.setText(map.get(mArr[1]));
            holder.tv_info.setText(new Spans()
                    .append(map.get(mArr[0]), new ForegroundColorSpan(0xff067ccc))
                    .append(" - ")
                    .append(map.get(mArr[4]), new ForegroundColorSpan(0xff383f52))
                    .append("   ")
                    .append(badge));

            holder.tv_time.setText(map.get(mArr[3]));
        }

        @Override
        public int getItemCount() {
            return mData == null || mData.isEmpty() ? 0 : mData.size();
        }

        static class StaticsViewHolder extends RecyclerView.ViewHolder {

            TextView tv_title;
            TextView tv_time;
            TextView tv_info;

            public StaticsViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}