package com.lcjian.drinkwater.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.data.db.entity.Record;
import com.lcjian.drinkwater.data.db.entity.Unit;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;
import com.lcjian.drinkwater.util.DateUtils;
import com.lcjian.drinkwater.util.Spans;
import com.lcjian.drinkwater.util.StringUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ModifyRecordFragment extends BaseDialogFragment {

    @BindView(R.id.tv_change_record_time)
    TextView tv_change_record_time;
    @BindView(R.id.iv_cup)
    ImageView iv_cup;
    @BindView(R.id.tv_percent_25)
    TextView tv_percent_25;
    @BindView(R.id.tv_percent_25_number)
    TextView tv_percent_25_number;
    @BindView(R.id.tv_percent_50)
    TextView tv_percent_50;
    @BindView(R.id.tv_percent_50_number)
    TextView tv_percent_50_number;
    @BindView(R.id.tv_percent_75)
    TextView tv_percent_75;
    @BindView(R.id.tv_percent_75_number)
    TextView tv_percent_75_number;
    @BindView(R.id.tv_percent_100)
    TextView tv_percent_100;
    @BindView(R.id.tv_percent_100_number)
    TextView tv_percent_100_number;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_ok)
    Button btn_ok;

    private Unbinder unbinder;
    private Disposable mDisposable;

    private Record mRecord;

    private Long mRecordId;

    public static ModifyRecordFragment newInstance(Long recordId) {
        ModifyRecordFragment fragment = new ModifyRecordFragment();
        Bundle args = new Bundle();
        args.putLong("record_id", recordId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRecordId = getArguments().getLong("record_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_record, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_cancel.setOnClickListener(v -> dismiss());
        btn_ok.setOnClickListener(v -> {
            mAppDatabase.recordDao().update(mRecord);
            dismiss();
        });
        tv_percent_25.setOnClickListener(v -> switchIntake(1));
        tv_percent_50.setOnClickListener(v -> switchIntake(2));
        tv_percent_75.setOnClickListener(v -> switchIntake(3));
        tv_percent_100.setOnClickListener(v -> switchIntake(4));
        tv_change_record_time.setOnClickListener(v -> {
            Date date = mRecord.timeAdded;
            TimePickerDialog dpd = TimePickerDialog.newInstance(
                    (vv, hourOfDay, minute, second) -> {
                        mRecord.timeAdded = DateUtils.convertStrToDate(
                                DateUtils.convertDateToStr(mRecord.timeAdded) + " " +
                                        hourOfDay + ":" + minute, "yyyy-MM-dd H:m");
                        mRecord.timeModified = mRecord.timeAdded;

                        tv_change_record_time.setText(new Spans()
                                .append(getString(R.string.e1))
                                .append(":")
                                .append(DateUtils.convertDateToStr(mRecord.timeAdded, "HH:mm a"),
                                        new ForegroundColorSpan(ContextCompat.getColor(tv_change_record_time.getContext(), R.color.colorAccent))));
                    },
                    Integer.parseInt(DateUtils.convertDateToStr(date, "H")),
                    Integer.parseInt(DateUtils.convertDateToStr(date, "m")),
                    false
            );
            dpd.show(requireFragmentManager(), "TimePickerDialog");
        });

        mDisposable = Flowable
                .combineLatest(
                        mAppDatabase.recordDao().getAllAsyncById(mRecordId).map(records -> records.get(0)),
                        mAppDatabase.unitDao().getCurrentUnitAsync().map(units -> units.get(0)),
                        Pair::create)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Record record = pair.first;
                    Unit unit = pair.second;

                    mRecord = record;
                    switch (StringUtils.formatDecimalToInt(record.cupCapacity)) {
                        case 100:
                            iv_cup.setImageResource(R.drawable.ic_cup_100_ml);
                            break;
                        case 200:
                            iv_cup.setImageResource(R.drawable.ic_cup_200_ml);
                            break;
                        case 300:
                            iv_cup.setImageResource(R.drawable.ic_cup_300_ml);
                            break;
                        case 400:
                            iv_cup.setImageResource(R.drawable.ic_cup_400_ml);
                            break;
                        case 500:
                            iv_cup.setImageResource(R.drawable.ic_cup_100_ml);
                            break;
                        default:
                            iv_cup.setImageResource(R.drawable.ic_cup_custom_ml);
                            break;
                    }
                    tv_change_record_time.setText(new Spans()
                            .append(getString(R.string.e1))
                            .append(":")
                            .append(DateUtils.convertDateToStr(record.timeAdded, "HH:mm a"),
                                    new ForegroundColorSpan(ContextCompat.getColor(tv_change_record_time.getContext(), R.color.colorAccent))));

                    String us = unit.name.split(",")[1];
                    double rate = Double.parseDouble(unit.rate.split(",")[1]);
                    String s1 = record.cupCapacity * rate / 4 + " " + us;
                    String s2 = record.cupCapacity * rate / 2 + " " + us;
                    String s3 = record.cupCapacity * rate / 4 * 3 + " " + us;
                    String s4 = record.cupCapacity * rate + " " + us;
                    tv_percent_25_number.setText(s1);
                    tv_percent_50_number.setText(s2);
                    tv_percent_75_number.setText(s3);
                    tv_percent_100_number.setText(s4);

                    if (Math.abs(record.intake - (record.cupCapacity * rate / 4)) < 0.1) {
                        switchIntake(1);
                    } else if (Math.abs(record.intake - (record.cupCapacity * rate / 2)) < 0.1) {
                        switchIntake(2);
                    } else if (Math.abs(record.intake - (record.cupCapacity * rate / 4 * 3)) < 0.1) {
                        switchIntake(3);
                    } else {
                        switchIntake(4);
                    }
                }, throwable -> {

                });
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void switchIntake(int p) {
        Context context = tv_change_record_time.getContext();
        switch (p) {
            case 1:
                tv_percent_25.setBackgroundResource(R.drawable.shape_blue_circle_bg);
                tv_percent_50.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_75.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_100.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);

                tv_percent_25.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                tv_percent_50.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                tv_percent_25_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
                tv_percent_50_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                mRecord.intake = mRecord.cupCapacity / 4;
                break;
            case 2:
                tv_percent_25.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_50.setBackgroundResource(R.drawable.shape_blue_circle_bg);
                tv_percent_75.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_100.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);

                tv_percent_25.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                tv_percent_75.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                tv_percent_25_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
                tv_percent_75_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                mRecord.intake = mRecord.cupCapacity / 2;
                break;
            case 3:
                tv_percent_25.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_50.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_75.setBackgroundResource(R.drawable.shape_blue_circle_bg);
                tv_percent_100.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);

                tv_percent_25.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                tv_percent_100.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                tv_percent_25_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));
                tv_percent_100_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));

                mRecord.intake = mRecord.cupCapacity / 4 * 3;
                break;
            case 4:
                tv_percent_25.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_50.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_75.setBackgroundResource(R.drawable.shape_white_circle_stroke_bg);
                tv_percent_100.setBackgroundResource(R.drawable.shape_blue_circle_bg);

                tv_percent_25.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100.setTextColor(ContextCompat.getColor(context, android.R.color.white));

                tv_percent_25_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_50_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_75_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextGray));
                tv_percent_100_number.setTextColor(ContextCompat.getColor(context, R.color.colorTextBlack));

                mRecord.intake = mRecord.cupCapacity;
                break;
            default:
                break;
        }

    }

}
