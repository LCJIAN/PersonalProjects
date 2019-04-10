package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

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
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class DrinkReportActivity extends BaseActivity {

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
        setupWeeklyAverage();
        setupMonthlyAverage();
        setupAverageCompletionAndDrinkFrequency();
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

    private void setss() {

    }

    private void setupWeeklyAverage() {
        mDisposables.add(Flowable.combineLatest(
                mAppDatabase.unitDao()
                        .getCurrentUnitAsync()
                        .map(units -> units.get(0))
                        .subscribeOn(Schedulers.io()),
                Flowable
                        .interval(0, 1, TimeUnit.HOURS)
                        .flatMap(aLong -> {
                            Date date = DateUtils.today();
                            return mAppDatabase.recordDao()
                                    .getAllAsyncByTime(DateUtils.firstDayWeekly(date), DateUtils.addDays(DateUtils.lastDayWeekly(date), 1))
                                    .subscribeOn(Schedulers.io());
                        })
                        .map(records -> {
                            if (records.isEmpty()) {
                                return 0d;
                            }
                            double total = 0d;
                            for (Record record : records) {
                                total += record.intake;
                            }
                            return total / records.size();
                        }),
                (unit, aDouble) -> aDouble + " " + unit.name.split(",")[1])
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aString -> tv_weekly_average.setText(getString(R.string.id, aString)),
                        throwable -> {
                        }));
    }

    private void setupMonthlyAverage() {
        mDisposables.add(Flowable.combineLatest(
                mAppDatabase.unitDao()
                        .getCurrentUnitAsync()
                        .map(units -> units.get(0))
                        .subscribeOn(Schedulers.io()),
                Flowable
                        .interval(0, 1, TimeUnit.HOURS)
                        .flatMap(aLong -> {
                            Date date = DateUtils.today();
                            return mAppDatabase.recordDao()
                                    .getAllAsyncByTime(DateUtils.firstDayMonthly(date), DateUtils.addDays(DateUtils.lastDayMonthly(date), 1))
                                    .subscribeOn(Schedulers.io());
                        })
                        .map(records -> {
                            if (records.isEmpty()) {
                                return 0d;
                            }
                            double total = 0d;
                            for (Record record : records) {
                                total += record.intake;
                            }
                            return total / records.size();
                        }),
                (unit, aDouble) -> aDouble + " " + unit.name.split(",")[1])
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

                    Date now = DateUtils.today();
                    for (Date min = records.get(0).timeAdded; DateUtils.isAfter(min, now); min = DateUtils.addDays(min, 1)) {
                        dates.add(min);
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