package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.gson.Gson;
import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.DataQueryResult2;
import com.org.firefighting.data.network.entity.ServiceDataRequest;
import com.org.firefighting.data.network.entity.ServiceDataRequestEmpty;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DataQueryFragment extends BaseFragment {

    @BindView(R.id.ll_query_options)
    LinearLayout ll_query_options;
    @BindView(R.id.tl_data)
    TableLayout tl_data;
    @BindView(R.id.tv_total_page_cont)
    TextView tv_total_page_cont;
    @BindView(R.id.tv_pre_page)
    TextView tv_pre_page;
    @BindView(R.id.et_page_number)
    EditText et_page_number;
    @BindView(R.id.tv_next_page)
    TextView tv_next_page;

    private Unbinder mUnBinder;

    private Disposable mDisposable;
    private Disposable mDisposableD;

    private String mInvokeName;
    private List<DataQueryResult2.Column> mColumns;

    private static final String[] OPTION_NAMES = new String[]{"等于", "不等于", "大于", "大于等于", "小于", "小于等于", "含有", "in", "为空", "不为空"};
    private static final String[] OPTION_OPS = new String[]{"eq", "neq", "gt", "gte", "lt", "lte", "like", "in", "isNull", "isNotNull"};

    private int mCurrentPage = 1;
    private int mTotalPage = 0;

    public static DataQueryFragment newInstance(String invokeName) {
        DataQueryFragment fragment = new DataQueryFragment();
        Bundle args = new Bundle();
        args.putString("invoke_name", invokeName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mInvokeName = getArguments().getString("invoke_name");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_query, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        tv_pre_page.setOnClickListener(v -> {
            if (mCurrentPage <= 1) {
                return;
            }
            mCurrentPage = mCurrentPage - 1;
            setupData();
        });
        tv_next_page.setOnClickListener(v -> {
            if (mCurrentPage >= mTotalPage) {
                return;
            }
            mCurrentPage = mCurrentPage + 1;
            setupData();
        });

        tv_total_page_cont.setText(getString(R.string.total_page_count, mTotalPage));

        setupContent();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        super.onDestroyView();
    }

    private void addOptionItem() {
        ServiceDataRequest.Option option = new ServiceDataRequest.Option();
        View view = LayoutInflater.from(ll_query_options.getContext())
                .inflate(R.layout.query_data_option_item, ll_query_options, false);
        LinearLayout ll_c = view.findViewById(R.id.ll_c);
        AppCompatSpinner sp_field = view.findViewById(R.id.sp_field);
        AppCompatSpinner sp_option = view.findViewById(R.id.sp_option);
        EditText et_keyword = view.findViewById(R.id.et_keyword);
        TextView tv_search = view.findViewById(R.id.tv_search);
        {
            List<DataQueryResult2.Column> searchColumns = new ArrayList<>();
            for (DataQueryResult2.Column c : mColumns) {
                if (c.isSearch == 1) {
                    searchColumns.add(c);
                }
            }
            if (searchColumns.isEmpty()) {
                DataQueryResult2.Column column = new DataQueryResult2.Column();
                column.remarks = "无数据";
                searchColumns.add(column);
                ll_c.setVisibility(View.GONE);
            } else {
                ll_c.setVisibility(View.VISIBLE);
            }
            String[] filedNames = new String[searchColumns.size()];
            String[] filedShowNames = new String[searchColumns.size()];
            for (int i = 0; i < filedNames.length; i++) {
                filedNames[i] = searchColumns.get(i).name;
                filedShowNames[i] = TextUtils.isEmpty(searchColumns.get(i).remarks) ? searchColumns.get(i).name : searchColumns.get(i).remarks;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(sp_option.getContext(),
                    R.layout.spinner_dropdown_item,
                    filedShowNames);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sp_field.setAdapter(adapter);
            sp_field.setSelection(0);
            sp_field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    option.filedName = filedNames[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            option.filedName = filedNames[0];
        }

        {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(sp_option.getContext(),
                    R.layout.spinner_dropdown_item,
                    OPTION_NAMES);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sp_option.setAdapter(adapter);
            sp_option.setSelection(0);
            sp_option.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    option.op = OPTION_OPS[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            option.op = OPTION_OPS[0];
        }

        et_keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                option.keyword = s.toString();
            }
        });

        tv_search.setOnClickListener(v -> {
            mCurrentPage = 1;
            setupData();
        });
        view.setTag(option);
        ll_query_options.addView(view);
    }

    private void setupOptions() {
        for (int i = 0; i < ll_query_options.getChildCount(); i++) {
            View view = ll_query_options.getChildAt(i);
            TextView tv_add_or_delete = view.findViewById(R.id.tv_add_or_delete);
            TextView tv_search = view.findViewById(R.id.tv_search);
            if (i == ll_query_options.getChildCount() - 1) {
                tv_add_or_delete.setText("添加");
                tv_search.setVisibility(View.VISIBLE);

                tv_add_or_delete.setOnClickListener(v -> {
                    addOptionItem();
                    setupOptions();
                });
            } else {
                tv_add_or_delete.setText("删除");
                tv_search.setVisibility(View.GONE);
                tv_add_or_delete.setOnClickListener(v -> ll_query_options.removeView(view));
            }
        }
    }

    private void setupContent() {
        showProgress();
        ServiceDataRequestEmpty dataRequest = new ServiceDataRequestEmpty();
        dataRequest.search = "";
        dataRequest.pageNumber = 1;
        dataRequest.pageSize = 1;
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .queryServiceData(SharedPreferencesDataSource.getSignInResponse().user.username, mInvokeName, dataRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            if (TextUtils.equals(responseData.code, "-1")) {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                                RxBus.getInstance().send(new ServiceDataQueryActivity.PermissionEvent(false));
                            } else {
                                RxBus.getInstance().send(new ServiceDataQueryActivity.PermissionEvent(true));
                                mColumns = responseData.field;
                                addOptionItem();
                                setupOptions();

                                mCurrentPage = 1;
                                setupData();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupData() {
        showProgress();
        if (mDisposableD != null) {
            mDisposableD.dispose();
        }
        ServiceDataRequest dataRequest = new ServiceDataRequest();
        Map<String, String> maps = null;
        for (int i = 0; i < ll_query_options.getChildCount(); i++) {
            View view = ll_query_options.getChildAt(i);
            ServiceDataRequest.Option option = (ServiceDataRequest.Option) view.getTag();
            if (!TextUtils.isEmpty(option.keyword)) {
                if (maps == null) {
                    maps = new HashMap<>();
                }
                maps.put(option.filedName + "." + option.op, TextUtils.equals("like", option.op) ? "%" + option.keyword + "%" : option.keyword);
            }
        }
        dataRequest.search = maps == null ? null : Collections.singletonList(new Gson().toJson(maps));
        dataRequest.pageNumber = mCurrentPage;
        dataRequest.pageSize = 10;
        mDisposableD = Single
                .just(dataRequest)
                .flatMap(o -> {
                    if (o.search == null || o.search.isEmpty()) {
                        ServiceDataRequestEmpty dataRequestEmpty = new ServiceDataRequestEmpty();
                        dataRequestEmpty.search = "";
                        dataRequestEmpty.pageNumber = mCurrentPage;
                        dataRequestEmpty.pageSize = 10;
                        return RestAPI.getInstance().apiServiceSB()
                                .queryServiceData(SharedPreferencesDataSource.getSignInResponse().user.username, mInvokeName, dataRequestEmpty);
                    } else {
                        return RestAPI.getInstance().apiServiceSB()
                                .queryServiceData(SharedPreferencesDataSource.getSignInResponse().user.username, mInvokeName, o);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            if (responseData.result.total == null) {
                                responseData.result.total = 0;
                            }
                            mTotalPage = responseData.result.total % 10 == 0 ? responseData.result.total / 10 : responseData.result.total / 10 + 1;
                            responseData.field.add(0, new DataQueryResult2.Column());
                            responseData.field.get(0).remarks = "序号";
                            responseData.field.get(0).isDisplay = 1;

                            tv_total_page_cont.setText(getString(R.string.total_page_count, mTotalPage));
                            et_page_number.setText(String.valueOf(mCurrentPage));

                            tl_data.removeAllViews();
                            LayoutInflater inflater = LayoutInflater.from(tl_data.getContext());

                            {
                                TableRow headerRow = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                for (DataQueryResult2.Column c : responseData.field) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView columnHeader = (TextView) inflater.inflate(R.layout.data_table_cell_item, headerRow, false);
                                        columnHeader.setText(c.remarks);
                                        headerRow.addView(columnHeader);
                                    }
                                }
                                tl_data.addView(headerRow);
                            }

                            int j = 0;
                            for (Map<String, String> map : responseData.result.result) {
                                TableRow row = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                int i = 0;
                                for (DataQueryResult2.Column c : responseData.field) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView cell = (TextView) inflater.inflate(R.layout.data_table_cell_item, row, false);
                                        if (i == 0) {
                                            cell.setText(String.valueOf(j + 1));
                                        } else {
                                            String value = map.get(c.name);
                                            if (!TextUtils.isEmpty(value) && value.getBytes().length > 1024) {
                                                value = "数据过大，请在PC上浏览";
                                            }
                                            cell.setText(value);
                                        }
                                        row.addView(cell);
                                        i++;
                                    }
                                }
                                tl_data.addView(row);
                                j++;
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}
