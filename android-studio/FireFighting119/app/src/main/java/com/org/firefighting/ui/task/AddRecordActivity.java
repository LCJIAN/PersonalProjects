package com.org.firefighting.ui.task;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.TaskTable;
import com.org.firefighting.ui.base.BaseActivity;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddRecordActivity extends BaseActivity {

    @BindView(R.id.btn_back)
    ImageButton btn_back;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.ll_container)
    LinearLayout ll_container;
    @BindView(R.id.btn_cancel)
    TextView btn_cancel;
    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    private String mTaskId;
    private TaskTable mTaskTable;
    private int mPosition;
    private List<String> mData;

    private Disposable mDisposable;
    private Disposable mDisposableD;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        ButterKnife.bind(this);

        mTaskId = getIntent().getStringExtra("task_id");
        mTaskTable = (TaskTable) getIntent().getSerializableExtra("task_table");
        mPosition = getIntent().getIntExtra("position", -1);

        tv_title.setText(mPosition == -1 ? R.string.add_record : R.string.modify_record);
        btn_back.setOnClickListener(v -> onBackPressed());
        btn_cancel.setOnClickListener(v -> clearInput());
        btn_confirm.setOnClickListener(v -> {

            if (mPosition == -1) { // 添加
                if (SharedPreferencesDataSource.getContinueAddRemember()) { // 记住
                    submitRecord();
                } else { // 没记住
                    new ContinueAddFragment()
                            .setListener(this::submitRecord)
                            .show(getSupportFragmentManager(), "ContinueAddFragment");
                }
            } else { // 修改
                submitRecord();
            }
        });

        if (mPosition == -1) { // 添加
            setupContent();
        } else {  // 修改
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        super.onDestroy();
    }

    private void clearInput() {
        for (int i = 0; i < ll_container.getChildCount(); i++) {
            View child = ll_container.getChildAt(i);
            if (child.findViewById(R.id.et_value) != null) {
                ((EditText) child.findViewById(R.id.et_value)).setText("");
            }
            if (child.findViewById(R.id.tv_value) != null) {
                ((TextView) child.findViewById(R.id.tv_value)).setText("");
            }
            if (child.findViewById(R.id.sp_value) != null) {
                ((AppCompatSpinner) child.findViewById(R.id.sp_value)).setSelection(0);
            }
            TaskTable.RtnTitle t = ((TaskTable.RtnTitle) child.getTag());
            t.value = null;
        }
    }

    private void loadData() {
        showProgress();
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        mDisposableD = RestAPI.getInstance().apiService()
                .getTaskTableRecords(mTaskId, mTaskTable.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listPageResponse -> {
                            hideProgress();
                            mData = listPageResponse.result.get(mPosition);
                            setupContent();
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void submitRecord() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < ll_container.getChildCount(); i++) {
            View child = ll_container.getChildAt(i);
            TaskTable.RtnTitle t = ((TaskTable.RtnTitle) child.getTag());
            if (TextUtils.isEmpty(t.value)) {
                if (t.requestedReal != null && t.requestedReal) {
                    Toast.makeText(App.getInstance(), "有必填项未填入", Toast.LENGTH_SHORT).show();
                    return;
                }
                data.add(t.value);
            } else {
                if (TextUtils.equals("秒表", t.type)) {
                    if (t.value.length() != 6) {
                        Toast.makeText(App.getInstance(), "秒表格式错误", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        data.add(t.value);
                    }
                } else {
                    data.add(t.value);
                }
            }
        }
        showProgress();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = Single
                .defer(() -> {
                    if (mPosition == -1) { // 添加
                        return RestAPI.getInstance().apiService().addTaskTableRecord(mTaskId, mTaskTable.id, data);
                    } else { // 修改
                        return RestAPI.getInstance().apiService().modifyTaskTableRecord(mTaskId, mTaskTable.id, mPosition, data);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(taskTableRecordResponseData -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), taskTableRecordResponseData.message, Toast.LENGTH_SHORT).show();
                            if (taskTableRecordResponseData.code == 0) {
                                if (mPosition == -1) { // 添加
                                    if (SharedPreferencesDataSource.getContinueAdd()) { // 继续
                                        clearInput();
                                    } else {
                                        finish();
                                    }
                                } else { // 修改
                                    finish();
                                }
                            }
                        }
                        , throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupContent() {
        List<List<TaskTable.RtnTitle>> ve = mTaskTable.rtnTitle;
        List<TaskTable.RtnTitle> result = new ArrayList<>();
        final int row = ve.size();
        final int col = ve.get(0).size();
        for (int i = 0; i < col; i++) {
            List<TaskTable.RtnTitle> temp = new ArrayList<>();
            for (int j = row - 1; j >= 0; j--) {
                String name = ve.get(j).get(i).name;
                if (temp.isEmpty()) { // 竖直
                    if (!TextUtils.isEmpty(name)) { // 找到不为空直接添加，为空的话退出本次循环；向上移
                        temp.add(ve.get(j).get(i));
                    }
                } else { // 横向
                    if (!TextUtils.isEmpty(name)) { // 找到不为空直接添加，向上移
                        temp.add(ve.get(j).get(i));
                    } else { // 为空的话向左移
                        for (int k = i; k >= 0; k--) {
                            if (!TextUtils.isEmpty(ve.get(j).get(k).name)) {
                                temp.add(ve.get(j).get(k));
                                break;
                            }
                        }
                    }
                }
            }

            StringBuilder longNameStr = new StringBuilder();
            for (int m = temp.size() - 1; m >= 0; m--) {
                longNameStr.append(temp.get(m).name).append("/");
            }
            temp.get(0).longNameStr = longNameStr.substring(0, longNameStr.length() - 1);
            temp.get(0).requestedReal = ve.get(row - 1).get(i).requested;
            result.add(temp.get(0));
        }

        for (int i = 0; i < result.size(); i++) {
            TaskTable.RtnTitle t = result.get(i);
            if (mData != null) {
                t.value = mData.get(i);
            }
            if (t.options.size() > 0) {
                buildItemPicker(t);
            } else {
                if (TextUtils.equals("文本", t.type)) {
                    buildItemText(t);
                } else if (TextUtils.equals("整数", t.type)) {
                    buildItemInteger(t);
                } else if (TextUtils.equals("小数", t.type)) {
                    buildItemDecimal(t);
                } else if (TextUtils.equals("秒表", t.type)) {
                    buildItemWatch(t);
                } else if (TextUtils.equals("手机号", t.type)) {
                    buildItemPhone(t);
                } else if (TextUtils.equals("身份证号", t.type)) {
                    buildItemIdentity(t);
                } else {
                    buildItemDateTime(t);
                }
            }
        }
    }

    private void setupItemInfo(View view, TaskTable.RtnTitle t) {
        ImageView iv_required = view.findViewById(R.id.iv_required);
        TextView tv_name = view.findViewById(R.id.tv_name);

        iv_required.setVisibility(t.requestedReal != null && t.requestedReal ? View.VISIBLE : View.INVISIBLE);
        tv_name.setText(new Spans(t.longNameStr).append("（" + (t.requestedReal != null && t.requestedReal ? "必填-" : "") + t.type + "）",
                new ForegroundColorSpan(0xffbcbcbc), new AbsoluteSizeSpan(DimenUtils.spToPixels(12, view.getContext()))));
    }

    private void buildItemText(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_text, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：重庆市消防救援总队");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemInteger(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_integer, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：123");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemDecimal(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_decimal, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：123.456");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemWatch(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_watch, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：请填写112233 (记录为 11'22''33 11分22秒33)");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemPhone(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_phone, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：13000000000");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemIdentity(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_identity, ll_container, false);
        setupItemInfo(rootV, t);

        EditText et_value = rootV.findViewById(R.id.et_value);
        et_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "示例：500223000000000000");
        if (t.value != null) {
            et_value.setText(t.value);
        }
        et_value.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = s.toString();
            }
        });
        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemPicker(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_picker, ll_container, false);
        setupItemInfo(rootV, t);

        AppCompatSpinner sp_value = rootV.findViewById(R.id.sp_value);

        String[] options = new String[t.options.size()];
        t.options.toArray(options);
        ArrayAdapter adapter = new ArrayAdapter<>(sp_value.getContext(),
                R.layout.spinner_dropdown_item,
                options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        sp_value.setAdapter(adapter);
        if (t.value != null) {
            sp_value.setSelection(t.options.indexOf(t.value));
        }
        sp_value.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TaskTable.RtnTitle) rootV.getTag()).value = options[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private void buildItemDateTime(TaskTable.RtnTitle t) {
        View rootV = LayoutInflater.from(this).inflate(R.layout.add_record_cell_item_date_time, ll_container, false);
        setupItemInfo(rootV, t);
        TextView tv_value = rootV.findViewById(R.id.tv_value);

        tv_value.setHint(!TextUtils.isEmpty(t.remarks) ? t.remarks : "请选择" + t.type);
        if (t.value != null) {
            tv_value.setText(t.value);
        }
        tv_value.setOnClickListener(clickV -> {
            if (TextUtils.equals(t.type, "日期时间")) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        (view, year, monthOfYear, dayOfMonth) -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tv_value.setText(DateUtils.convertDateToStr(calendar.getTime()));

                            TimePickerDialog tpd = TimePickerDialog.newInstance(
                                    (vvv, hourOfDay, minute, second) -> {
                                        String value = tv_value.getText().toString();
                                        value = value + " " + String.format(Locale.getDefault(), "%02d:%02d:%02d", hourOfDay, minute, second);
                                        tv_value.setText(value);

                                        ((TaskTable.RtnTitle) rootV.getTag()).value = tv_value.getText().toString();
                                    },
                                    true);
                            tpd.setOnCancelListener(dialog -> {
                                tv_value.setText("");
                                ((TaskTable.RtnTitle) rootV.getTag()).value = "";
                            });
                            tpd.show(getSupportFragmentManager(), "TimePickerDialog");
                        }
                );
                dpd.show(getSupportFragmentManager(), "DatePickerDialog");
            } else if (TextUtils.equals(t.type, "日期")) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        (view, year, monthOfYear, dayOfMonth) -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tv_value.setText(DateUtils.convertDateToStr(calendar.getTime()));

                            ((TaskTable.RtnTitle) rootV.getTag()).value = tv_value.getText().toString();
                        }
                );
                dpd.show(getSupportFragmentManager(), "DatePickerDialog");
            } else if (TextUtils.equals(t.type, "年月")) {
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        (view, year, monthOfYear, dayOfMonth) -> {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            tv_value.setText(DateUtils.convertDateToStr(calendar.getTime(), "yyyy-MM"));

                            ((TaskTable.RtnTitle) rootV.getTag()).value = tv_value.getText().toString();
                        }
                );
                dpd.show(getSupportFragmentManager(), "DatePickerDialog");
            } else if (TextUtils.equals(t.type, "时间")) {
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        (view, hourOfDay, minute, second) -> {
                            tv_value.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hourOfDay, minute, second));

                            ((TaskTable.RtnTitle) rootV.getTag()).value = tv_value.getText().toString();
                        },
                        true);
                dpd.show(getSupportFragmentManager(), "TimePickerDialog");
            }
        });

        rootV.setTag(t);
        ll_container.addView(rootV);
    }

    private static class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
