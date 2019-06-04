package com.lcjian.drinkwater.ui;

import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.util.DateUtils;
import com.lcjian.drinkwater.util.DimenUtils;
import com.lcjian.drinkwater.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DrinkReportActivity extends BaseActivity {

    @BindView(R.id.btn_pre)
    ImageButton btn_pre;
    @BindView(R.id.btn_next)
    ImageButton btn_next;
    @BindView(R.id.tv_month_or_year)
    TextView tv_month_or_year;
    @BindView(R.id.bar_chart)
    BarChart bar_chart;
    @BindView(R.id.tv_monthly)
    TextView tv_monthly;
    @BindView(R.id.tv_yearly)
    TextView tv_yearly;

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

    private Date mDate;
    private Boolean mMonthly;

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

        bar_chart.setTouchEnabled(false);
        bar_chart.getDescription().setEnabled(false);
        bar_chart.getLegend().setEnabled(false);
        bar_chart.getAxisRight().setEnabled(false);

        YAxis leftAxis = bar_chart.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setGridDashedLine(new DashPathEffect(new float[]{DimenUtils.dipToPixels(3, this), DimenUtils.dipToPixels(3, this)}, 0));
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.colorTextLight));
        leftAxis.setValueFormatter(new PercentFormatter());
        leftAxis.setSpaceTop(20);
        leftAxis.setSpaceMin(20);
        leftAxis.setAxisMaximum(120);
        leftAxis.setAxisMinimum(0);

        XAxis xAxis = bar_chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGridColor(ContextCompat.getColor(this, R.color.colorTextLight));
        xAxis.setLabelCount(5, true);
        xAxis.setAxisMinimum(1);

        mDisposables = new CompositeDisposable();
        setupWeeklyDay();
        setupWeeklyAverage();
        setupMonthlyAverage();
        setupAverageCompletionAndDrinkFrequency();
        setBarData();

        mDate = DateUtils.now();
        mMonthly = true;

        bar_chart.post(() -> {
            mRxBus.send(mMonthly);
            mRxBus.send(mDate);
            setupIndicator();
        });

        btn_pre.setOnClickListener(v -> {
            mDate = DateUtils.addMonths(mDate, -1);
            mRxBus.send(mDate);
            setupIndicator();
        });
        btn_next.setOnClickListener(v -> {
            mDate = DateUtils.addMonths(mDate, +1);
            mRxBus.send(mDate);
            setupIndicator();
        });
        tv_monthly.setOnClickListener(v -> {
            mMonthly = true;
            mRxBus.send(true);
            setupIndicator();
        });
        tv_yearly.setOnClickListener(v -> {
            mMonthly = false;
            mRxBus.send(false);
            setupIndicator();
        });
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

    private void setupIndicator() {
        if (mMonthly) {
            tv_monthly.setBackgroundResource(R.drawable.shape_switch_on_bg);
            tv_monthly.setTextColor(ContextCompat.getColor(this, R.color.colorTextBlack));
            tv_yearly.setBackground(null);
            tv_yearly.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            tv_monthly.setBackground(null);
            tv_monthly.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            tv_yearly.setBackgroundResource(R.drawable.shape_switch_on_bg);
            tv_yearly.setTextColor(ContextCompat.getColor(this, R.color.colorTextBlack));
        }
        tv_month_or_year.setText(DateUtils.convertDateToStr(mDate, mMonthly ? "yyyy-MM" : "yyyy"));
    }

    private void setBarData() {
        mDisposables.add(Flowable.combineLatest(
                mRxBus.asFlowable().filter(o -> o instanceof Boolean),
                mRxBus.asFlowable().filter(o -> o instanceof Date),
                (o1, o2) -> {
                    Boolean monthly = (Boolean) o1;
                    Date date = (Date) o2;
                    List<Pair<Date, Date>> dates = new ArrayList<>();
                    if (monthly) {  // monthly
                        Date startDate = DateUtils.firstDayMonthly(date);
                        Date endDate = DateUtils.lastDayMonthly(date);

                        while (DateUtils.isBefore(startDate, endDate)) {
                            dates.add(Pair.create(startDate, DateUtils.addDays(startDate, 1)));
                            startDate = DateUtils.addDays(startDate, 1);
                        }
                    } else {
                        String year = DateUtils.convertDateToStr(date, "yyyy");
                        for (int i = 1; i <= 12; i++) {
                            Date d = DateUtils.convertStrToDate(year + "-" + String.format(Locale.ENGLISH, "%02d", i), "yyyy-MM");
                            dates.add(Pair.create(DateUtils.firstDayMonthly(d), DateUtils.addDays(DateUtils.lastDayMonthly(d), 1)));
                        }
                    }
                    return dates;
                })
                .map(dates -> {
                    Setting setting = mAppDatabase.settingDao().getAllSync().get(0);
                    List<Double> percents = new ArrayList<>();
                    for (Pair<Date, Date> pair : dates) {
                        List<Record> records = mAppDatabase.recordDao().getAllSyncByTime(pair.first, pair.second);
                        double total = 0d;
                        for (Record record : records) {
                            total += record.intake;
                        }
                        percents.add(total * 100 / (setting.intakeGoal * DateUtils.dayDiff(pair.second, pair.first)));
                    }
                    return percents;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(doubles -> {

                            ArrayList<BarEntry> values = new ArrayList<>();
                            int i = 1;
                            for (Double d : doubles) {
                                if (d > 80) {
                                    values.add(new BarEntry((float) i, d.floatValue(), getResources().getDrawable(R.drawable.ic_star)));
                                } else {
                                    values.add(new BarEntry((float) i, d.floatValue()));
                                }
                                i++;
                            }

                            BarDataSet set1;

                            if (bar_chart.getData() != null && bar_chart.getData().getDataSetCount() > 0) {
                                set1 = (BarDataSet) bar_chart.getData().getDataSetByIndex(0);
                                set1.setValues(values);
                                bar_chart.getData().notifyDataChanged();
                                bar_chart.notifyDataSetChanged();
                                bar_chart.animateY(1000);
                            } else {
                                set1 = new BarDataSet(values, null);
                                set1.setColor(ContextCompat.getColor(this, R.color.colorAccent));

                                ArrayList<IBarDataSet> dataSets = new ArrayList<>();
                                dataSets.add(set1);

                                BarData data = new BarData(dataSets);
                                data.setHighlightEnabled(false);
                                data.setDrawValues(false);
                                data.setBarWidth(0.7f);

                                bar_chart.setData(data);
                                bar_chart.animateY(1000);
                            }
                        }
                ));
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
                            return total / DateUtils.dayDiff(endDate, startDate);
                        }),
                (unit, aDouble) -> StringUtils.formatDecimalToString(aDouble) + " " + unit.name.split(",")[1])
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
                            return total / DateUtils.dayDiff(endDate, startDate);
                        }),
                (unit, aDouble) -> StringUtils.formatDecimalToString(aDouble) + " " + unit.name.split(",")[1])
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
                            int a = StringUtils.formatDecimalToInt(pair.second);
                            tv_average_completion.setText(getString(R.string.ac, (int) (pair.first * 100)));
                            tv_drink_frequency.setText(getResources().getQuantityString(R.plurals.a, a, a));
                        },
                        throwable -> {
                        }));
    }
}