package com.lcjian.osc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.osc.R;
import com.lcjian.osc.ui.base.BaseActivity;
import com.lcjian.osc.util.DateUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DatePickerActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.calendar_view)
    CalendarView calendar_view;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private long mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);
        ButterKnife.bind(this);

        tv_title.setText(getString(R.string.pick_date));
        btn_nav_back.setVisibility(View.VISIBLE);
        btn_nav_back.setOnClickListener(v -> onBackPressed());
        btn_confirm.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("date", mDate);
            setResult(RESULT_OK, intent);
            finish();
        });
        calendar_view.setOnDateChangeListener((view, year, month, dayOfMonth) ->
                mDate = DateUtils.convertStrToDate(year + ":" + (month + 1) + ":" + dayOfMonth, "yyyy:M:dd").getTime());
        calendar_view.setMaxDate(System.currentTimeMillis());
    }
}
