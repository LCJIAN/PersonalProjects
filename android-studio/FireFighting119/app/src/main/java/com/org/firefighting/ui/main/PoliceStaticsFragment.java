package com.org.firefighting.ui.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PoliceStaticsFragment extends BaseFragment {

    @BindView(R.id.tv_statics_1)
    TextView tv_statics_1;
    @BindView(R.id.tv_statics_2)
    TextView tv_statics_2;
    @BindView(R.id.tv_statics_3)
    TextView tv_statics_3;
    @BindView(R.id.tv_statics_4)
    TextView tv_statics_4;
    @BindView(R.id.tv_statics_5)
    TextView tv_statics_5;
    @BindView(R.id.tv_statics_6)
    TextView tv_statics_6;

    private Unbinder mUnBinder;

    private String[] mArr = new String[]{"出动消防站", "出动车次", "社会救助", "案件总数", "抢险救援", "火灾扑救"};

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_police_statics, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getData();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void getData() {
        mDisposable = Single
                .zip(
                        RestAPI.getInstance().apiServiceSB().getPoliceStaticsDay(DateUtils.convertDateToStr(DateUtils.now())),
                        RestAPI.getInstance().apiServiceSB().getPoliceStaticsYear(DateUtils.convertDateToStr(DateUtils.now())),
                        (listResponseData, listResponseData2) -> Pair.create(listResponseData.data.get(0), listResponseData2.data.get(0)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapMapPair -> {
                            Map<String, Integer> map1 = mapMapPair.first;
                            Map<String, Integer> map2 = mapMapPair.second;
                            assert map1 != null;
                            assert map2 != null;
                            setupStatics(mArr[0], map1.get(mArr[0]), map2.get(mArr[0]), tv_statics_1);
                            setupStatics(mArr[1], map1.get(mArr[1]), map2.get(mArr[1]), tv_statics_2);
                            setupStatics(mArr[2], map1.get(mArr[2]), map2.get(mArr[2]), tv_statics_3);
                            setupStatics(mArr[3], map1.get(mArr[3]), map2.get(mArr[3]), tv_statics_4);
                            setupStatics(mArr[4], map1.get(mArr[4]), map2.get(mArr[4]), tv_statics_5);
                            setupStatics(mArr[5], map1.get(mArr[5]), map2.get(mArr[5]), tv_statics_6);
                        },
                        Timber::e);
    }

    private void setupStatics(String title, Integer day, Integer year, TextView tv_statics) {
        tv_statics.setText(new Spans()
                .append(String.valueOf(day), new AbsoluteSizeSpan(DimenUtils.spToPixels(18, tv_statics.getContext())), new StyleSpan(Typeface.BOLD))
                .append("(今日)", new AbsoluteSizeSpan(DimenUtils.spToPixels(12, tv_statics.getContext()))).append("\n")
                .append(title, new AbsoluteSizeSpan(DimenUtils.spToPixels(14, tv_statics.getContext())), new StyleSpan(Typeface.BOLD)).append("\n")
                .append(String.valueOf(year), new AbsoluteSizeSpan(DimenUtils.spToPixels(16, tv_statics.getContext())))
                .append("(今年)", new AbsoluteSizeSpan(DimenUtils.spToPixels(12, tv_statics.getContext()))));
    }
}
