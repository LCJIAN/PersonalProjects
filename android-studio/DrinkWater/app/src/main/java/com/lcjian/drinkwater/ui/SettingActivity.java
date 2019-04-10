package com.lcjian.drinkwater.ui;

import android.os.Bundle;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Setting;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseActivity;
import com.lcjian.drinkwater.ui.setting.GendersFragment;
import com.lcjian.drinkwater.ui.setting.IntakeGoalFragment;
import com.lcjian.drinkwater.ui.setting.ReminderIntervalsFragment;
import com.lcjian.drinkwater.ui.setting.ReminderModesFragment;
import com.lcjian.drinkwater.ui.setting.UnitsFragment;
import com.lcjian.drinkwater.ui.setting.WeightFragment;
import com.lcjian.drinkwater.util.DateUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.rl_unit)
    RelativeLayout rl_unit;
    @BindView(R.id.tv_unit)
    TextView tv_unit;

    @BindView(R.id.rl_intake_goal)
    RelativeLayout rl_intake_goal;
    @BindView(R.id.tv_intake_goal)
    TextView tv_intake_goal;
    @BindView(R.id.tv_unit_for_intake_goal)
    TextView tv_unit_for_intake_goal;

    @BindView(R.id.rl_gender)
    RelativeLayout rl_gender;
    @BindView(R.id.tv_gender)
    TextView tv_gender;

    @BindView(R.id.rl_weight)
    RelativeLayout rl_weight;
    @BindView(R.id.tv_weight)
    TextView tv_weight;
    @BindView(R.id.tv_unit_for_weight)
    TextView tv_unit_for_weight;

    @BindView(R.id.rl_wake_up_time)
    RelativeLayout rl_wake_up_time;
    @BindView(R.id.tv_wake_up_time)
    TextView tv_wake_up_time;

    @BindView(R.id.rl_sleep_time)
    RelativeLayout rl_sleep_time;
    @BindView(R.id.tv_sleep_time)
    TextView tv_sleep_time;

    @BindView(R.id.rl_reminder_interval)
    RelativeLayout rl_reminder_interval;
    @BindView(R.id.tv_reminder_interval)
    TextView tv_reminder_interval;

    @BindView(R.id.rl_reminder_mode)
    RelativeLayout rl_reminder_mode;
    @BindView(R.id.tv_reminder_mode)
    TextView tv_reminder_mode;

    @BindView(R.id.rl_reminder_alert)
    RelativeLayout rl_reminder_alert;
    @BindView(R.id.switch_reminder_alert)
    SwitchCompat switch_reminder_alert;

    @BindView(R.id.rl_further_reminder)
    RelativeLayout rl_further_reminder;
    @BindView(R.id.switch_further_reminder)
    SwitchCompat switch_further_reminder;

    private Disposable mDisposable;

    private Setting mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.gk);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        rl_unit.setOnClickListener(this);
        rl_intake_goal.setOnClickListener(this);
        rl_gender.setOnClickListener(this);
        rl_weight.setOnClickListener(this);
        rl_wake_up_time.setOnClickListener(this);
        rl_sleep_time.setOnClickListener(this);
        rl_reminder_interval.setOnClickListener(this);
        rl_reminder_mode.setOnClickListener(this);
        rl_reminder_alert.setOnClickListener(this);
        rl_further_reminder.setOnClickListener(this);
        switch_reminder_alert.setOnCheckedChangeListener(this);
        switch_further_reminder.setOnCheckedChangeListener(this);

        mDisposable = Flowable.combineLatest(
                mAppDatabase.settingDao().getAllAsync()
                        .map(settings -> settings.get(0)),
                mAppDatabase.unitDao().getCurrentUnitAsync()
                        .map(units -> units.get(0)),
                Pair::create)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Setting setting = pair.first;
                    Unit unit = pair.second;

                    mSetting = setting;

                    tv_intake_goal.setText(String.valueOf(setting.intakeGoal * unit.rate));
                    tv_gender.setText(setting.gender.equals(0) ? R.string.male : R.string.female);
                    tv_weight.setText(String.valueOf(setting.weight * unit.rate));
                    tv_wake_up_time.setText(DateUtils.convertDateToStr(DateUtils.convertStrToDate(setting.wakeUpTime, "HH:mm"), "HH:mm a"));
                    tv_sleep_time.setText(DateUtils.convertDateToStr(DateUtils.convertStrToDate(setting.sleepTime, "HH:mm"), "HH:mm a"));
                    tv_reminder_interval.setText(getString(R.string.ef, String.valueOf(setting.reminderInterval)));
                    tv_reminder_mode.setText(setting.reminderMode.equals(0) ? R.string.hy
                            : ((setting.reminderMode.equals(1) ? R.string.ep : R.string.aa)));
                    switch_further_reminder.setChecked(setting.furtherReminder);
                    switch_reminder_alert.setChecked(setting.reminderAlert);

                    tv_unit.setText(unit.name);
                    tv_unit_for_weight.setText(unit.name.split(",")[0]);
                    tv_unit_for_intake_goal.setText(unit.name.split(",")[1]);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_unit:
                new UnitsFragment().show(getSupportFragmentManager(), "UnitsFragment");
                break;
            case R.id.rl_intake_goal:
                new IntakeGoalFragment().show(getSupportFragmentManager(), "IntakeGoalFragment");
                break;
            case R.id.rl_gender:
                new GendersFragment().show(getSupportFragmentManager(), "GendersFragment");
                break;
            case R.id.rl_weight:
                new WeightFragment().show(getSupportFragmentManager(), "WeightFragment");
                break;
            case R.id.rl_wake_up_time: {
                Date date = DateUtils.convertStrToDate(mSetting.wakeUpTime, "HH:mm");
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        (view, hourOfDay, minute, second) -> {
                            mSetting.wakeUpTime = DateUtils.convertDateToStr(DateUtils.convertStrToDate(hourOfDay + ":" + minute, "H:m"), "HH:mm");
                            mAppDatabase.settingDao().update(mSetting);
                        },
                        Integer.parseInt(DateUtils.convertDateToStr(date, "H")),
                        Integer.parseInt(DateUtils.convertDateToStr(date, "m")),
                        false
                );
                dpd.show(getSupportFragmentManager(), "TimePickerDialog");
            }
            break;
            case R.id.rl_sleep_time: {
                Date date = DateUtils.convertStrToDate(mSetting.sleepTime, "HH:mm");
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        (view, hourOfDay, minute, second) -> {
                            mSetting.sleepTime = DateUtils.convertDateToStr(DateUtils.convertStrToDate(hourOfDay + ":" + minute, "H:m"), "HH:mm");
                            mAppDatabase.settingDao().update(mSetting);
                        },
                        Integer.parseInt(DateUtils.convertDateToStr(date, "H")),
                        Integer.parseInt(DateUtils.convertDateToStr(date, "m")),
                        false
                );
                dpd.show(getSupportFragmentManager(), "TimePickerDialog");
            }
            break;
            case R.id.rl_reminder_interval:
                new ReminderIntervalsFragment().show(getSupportFragmentManager(), "ReminderIntervalsFragment");
                break;
            case R.id.rl_reminder_mode:
                new ReminderModesFragment().show(getSupportFragmentManager(), "ReminderModesFragment");
                break;
            case R.id.rl_further_reminder:
                switch_further_reminder.setChecked(!switch_further_reminder.isChecked());
                break;
            case R.id.rl_reminder_alert:
                switch_reminder_alert.setChecked(!switch_reminder_alert.isChecked());
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_further_reminder:
                mSetting.furtherReminder = isChecked;
                mAppDatabase.settingDao().update(mSetting);
                break;
            case R.id.switch_reminder_alert:
                mSetting.reminderAlert = isChecked;
                mAppDatabase.settingDao().update(mSetting);
                break;
            default:
                break;
        }
    }
}