package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.model.GradientColor;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DrinkReportActivity extends BaseActivity {

    @BindView(R.id.bar_chart)
    BarChart bar_chart;

    @BindView(R.id.iv_sun_completion)
    ImageView iv_sun_completion;
    @BindView(R.id.iv_mon_completion)
    ImageView iv_mon_completion;
    @BindView(R.id.iv_tue_completion)
    ImageView iv_tue_completion;
    @BindView(R.id.iv_wed_completion)
    ImageView iv_wed_completion;
    @BindView(R.id.iv_thu_completion)
    ImageView iv_thu_completion;
    @BindView(R.id.iv_fri_completion)
    ImageView iv_fri_completion;
    @BindView(R.id.iv_sat_completion)
    ImageView iv_sat_completion;

    @BindView(R.id.tv_weekly_average)
    TextView tv_weekly_average;
    @BindView(R.id.tv_monthly_average)
    TextView tv_monthly_average;
    @BindView(R.id.tv_average_completion)
    TextView tv_average_completion;
    @BindView(R.id.tv_drink_frequency)
    TextView tv_drink_frequency;

    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_report);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.ds);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDisposables = new CompositeDisposable();
        setupWeeklyDay();
        setupWeeklyAverage();
        setupMonthlyAverage();
        setupAverageCompletionAndDrinkFrequency();

        YAxis leftAxis = bar_chart.getAxisLeft();
        leftAxis.setLabelCount(8, true);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        setData(30, 100);


        YAxis rightAxis = bar_chart.getAxisRight();
        rightAxis.setEnabled(false);
//        rightAxis.setDrawGridLines(false);
////        rightAxis.setTypeface(tfLight);
//        rightAxis.setLabelCount(8, false);
////        rightAxis.setValueFormatter(custom);
//        rightAxis.setSpaceTop(15f);
//        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = bar_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setTypeface(tfLight);
        xAxis.setDrawGridLines(true);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
//        xAxis.setValueFormatter(xAxisFormatter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mDisposables.dispose();
        super.onDestroy();
    }

    private void setData(int count, float range) {

        float start = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = (int) start; i < start + count; i++) {
            float val = (float) (Math.random() * (range + 1));

            if (Math.random() * 100 < 25) {
                values.add(new BarEntry(i, val, getResources().getDrawable(R.drawable.medal_win)));
            } else {
                values.add(new BarEntry(i, val));
            }
        }

        BarDataSet set1;

        if (bar_chart.getData() != null &&
                bar_chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) bar_chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            bar_chart.getData().notifyDataChanged();
            bar_chart.notifyDataSetChanged();

        } else {
            set1 = new BarDataSet(values, "The year 2017");

            set1.setDrawIcons(false);

//            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            /*int startColor = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor = ContextCompat.getColor(this, android.R.color.holo_blue_bright);
            set1.setGradientColor(startColor, endColor);*/

            int startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark);

            List<GradientColor> gradientColors = new ArrayList<>();
            gradientColors.add(new GradientColor(startColor1, endColor1));
            gradientColors.add(new GradientColor(startColor2, endColor2));
            gradientColors.add(new GradientColor(startColor3, endColor3));
            gradientColors.add(new GradientColor(startColor4, endColor4));
            gradientColors.add(new GradientColor(startColor5, endColor5));

            set1.setGradientColors(gradientColors);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
//            data.setValueTypeface(tfLight);
            data.setBarWidth(0.9f);

            bar_chart.setData(data);
        }
    }

    private void setupWeeklyDay() {
        mDisposables.add(Flowable
                .interval(0, 1, TimeUnit.HOURS)
                .map(aLong -> {
                    Date today = DateUtils.today();
                    Date startDate = DateUtils.firstDayWeekly(today);

                    List<Date> dates = new ArrayList<>();
                    while (DateUtils.isBefore(startDate, today)) {
                        dates.add(startDate);
                        startDate = DateUtils.addDays(startDate, 1);
                    }
                    return dates;
                })
                .map(dates -> {
                    Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                    List<Double> percents = new ArrayList<>();
                    for (Date date : dates) {
                        List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(date, DateUtils.addDays(date, 1));
                        double total = 0d;
                        for (Record record : records) {
                            total += record.intake;
                        }
                        percents.add(total / setting.intakeGoal);
                    }
                    return percents;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(doubles -> {
                    if (doubles.size() > 0) {
                        iv_sun_completion.setImageResource(doubles.get(0) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_sun_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 1) {
                        iv_mon_completion.setImageResource(doubles.get(1) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_mon_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 2) {
                        iv_tue_completion.setImageResource(doubles.get(2) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_tue_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 3) {
                        iv_wed_completion.setImageResource(doubles.get(3) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_wed_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 4) {
                        iv_thu_completion.setImageResource(doubles.get(4) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_thu_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 5) {
                        iv_fri_completion.setImageResource(doubles.get(5) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_fri_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                    if (doubles.size() > 6) {
                        iv_sat_completion.setImageResource(doubles.get(6) > 0.8 ? R.drawable.medal_win : R.drawable.medal_win_not);
                    } else {
                        iv_sat_completion.setImageResource(R.drawable.ic_week_day_report_bg);
                    }
                }));
    }

    private void setupWeeklyAverage() {
        mDisposables.add(Flowable.combineLatest(
                mAppDatabase.unitDao()
                        .getCurrentUnitAsync()
                        .map(units -> units.get(0)),
                Flowable
                        .interval(0, 1, TimeUnit.HOURS)
                        .map(aLong -> {
                            Date today = DateUtils.today();
                            Date startDate = DateUtils.firstDayWeekly(today);
                            Date endDate = DateUtils.addDays(DateUtils.lastDayWeekly(today), 1);
                            List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(startDate, endDate);

                            if (records.isEmpty()) {
                                return 0d;
                            }
                            double total = 0d;
                            for (Record record : records) {
                                total += record.intake;
                            }
                            return total / (Integer.parseInt(DateUtils.convertDateToStr(DateUtils.addDays(endDate, -1), "d"))
                                    - Integer.parseInt(DateUtils.convertDateToStr(startDate, "d")));
                        }),
                (unit, aDouble) -> aDouble + " " + unit.name.split(",")[1])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aString -> tv_weekly_average.setText(getString(R.string.id, aString)),
                        throwable -> {
                        }));
    }

    private void setupMonthlyAverage() {
        mDisposables.add(Flowable.combineLatest(
                mAppDatabase.unitDao()
                        .getCurrentUnitAsync()
                        .map(units -> units.get(0)),
                Flowable
                        .interval(0, 1, TimeUnit.HOURS)
                        .map(aLong -> {
                            Date today = DateUtils.today();
                            Date startDate = DateUtils.firstDayMonthly(today);
                            Date endDate = DateUtils.addDays(DateUtils.lastDayMonthly(today), 1);
                            List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(startDate, endDate);

                            if (records.isEmpty()) {
                                return 0d;
                            }
                            double total = 0d;
                            for (Record record : records) {
                                total += record.intake;
                            }
                            return total / (Integer.parseInt(DateUtils.convertDateToStr(DateUtils.addDays(endDate, -1), "d"))
                                    - Integer.parseInt(DateUtils.convertDateToStr(startDate, "d")));
                        }),
                (unit, aDouble) -> aDouble + " " + unit.name.split(",")[1])
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aString -> tv_monthly_average.setText(getString(R.string.eo, aString)),
                        throwable -> {
                        }));
    }

    private void setupAverageCompletionAndDrinkFrequency() {
        mDisposables.add(mAppDatabase.recordDao()
                .getFirstAsync()
                .map(records -> {
                    List<Date> dates = new ArrayList<>();
                    if (records.isEmpty()) {
                        return dates;
                    }

                    Date today = DateUtils.today();
                    Date min = DateUtils.convertStrToDate(DateUtils.convertDateToStr(records.get(0).timeAdded));
                    while (!DateUtils.isAfter(min, today)) {
                        dates.add(min);
                        min = DateUtils.addDays(min, 1);
                    }
                    return dates;
                })
                .map(dates -> {
                    double percentTotal = 0d;
                    int times = 0;
                    if (dates.isEmpty()) {
                        return Pair.create(percentTotal, 0d);
                    }
                    Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                    for (Date date : dates) {
                        List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(date, DateUtils.addDays(date, 1));
                        double total = 0d;
                        for (Record record : records) {
                            total += record.intake;
                        }
                        percentTotal += total / setting.intakeGoal;
                        times += records.size();
                    }
                    return Pair.create(percentTotal / dates.size(), ((double) times) / dates.size());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                            int a = (int) Math.round(pair.second);
                            tv_average_completion.setText(getString(R.string.ac, (int) (pair.first * 100)));
                            tv_drink_frequency.setText(getResources().getQuantityString(R.plurals.a, a, a));
                        },
                        throwable -> {
                        }));
    }
}