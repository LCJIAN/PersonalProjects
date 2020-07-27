package com.org.firefighting.ui.resource;

import android.content.Intent;
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
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.DataQueryResult;
import com.org.firefighting.data.network.entity.ResourceDataRequest;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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

    private String mResourceId;

    private ResourceEntity mResourceEntity;

    private static final String[] OPTION_NAMES = new String[]{"等于", "不等于", "大于", "大于等于", "小于", "小于等于", "含有", "in", "为空", "不为空"};
    private static final String[] OPTION_OPS = new String[]{"eq", "neq", "gt", "gte", "lt", "lte", "like", "in", "isNull", "isNotNull"};

    private int mCurrentPage = 1;
    private int mTotalPage = 0;

    public static DataQueryFragment newInstance(String taskId) {
        DataQueryFragment fragment = new DataQueryFragment();
        Bundle args = new Bundle();
        args.putString("resource_id", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResourceId = getArguments().getString("resource_id");
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
        ResourceDataRequest.Option option = new ResourceDataRequest.Option();
        View view = LayoutInflater.from(ll_query_options.getContext())
                .inflate(R.layout.query_data_option_item, ll_query_options, false);
        LinearLayout ll_c = view.findViewById(R.id.ll_c);
        AppCompatSpinner sp_field = view.findViewById(R.id.sp_field);
        AppCompatSpinner sp_option = view.findViewById(R.id.sp_option);
        EditText et_keyword = view.findViewById(R.id.et_keyword);
        TextView tv_search = view.findViewById(R.id.tv_search);
        {
            List<ResourceEntity.Field> searchFields = new ArrayList<>();
            for (ResourceEntity.Field f : mResourceEntity.fields) {
                if (f.isSearch == 1) {
                    searchFields.add(f);
                }
            }
            if (searchFields.isEmpty()) {
                ResourceEntity.Field field = new ResourceEntity.Field();
                field.chineseName = "无数据";
                searchFields.add(field);
                ll_c.setVisibility(View.GONE);
            } else {
                ll_c.setVisibility(View.VISIBLE);
            }
            String[] filedNames = new String[searchFields.size()];
            String[] filedShowNames = new String[searchFields.size()];
            for (int i = 0; i < filedNames.length; i++) {
                filedNames[i] = searchFields.get(i).name;
                filedShowNames[i] = TextUtils.isEmpty(searchFields.get(i).chineseName) ? searchFields.get(i).name : searchFields.get(i).chineseName;
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
        mDisposable = RestAPI.getInstance().apiServiceSB2()
                .getResourceDetail(mResourceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            if (responseData.code == -1) {
                                Toast.makeText(App.getInstance(), responseData.message, Toast.LENGTH_SHORT).show();
                            } else {
                                mResourceEntity = responseData.data;
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
        ResourceDataRequest mRequest = new ResourceDataRequest();
        for (int i = 0; i < ll_query_options.getChildCount(); i++) {
            View view = ll_query_options.getChildAt(i);
            ResourceDataRequest.Option option = (ResourceDataRequest.Option) view.getTag();
            if (!TextUtils.isEmpty(option.keyword)) {
                if (mRequest.searchMap == null) {
                    mRequest.searchMap = new HashMap<>();
                }
                mRequest.searchMap.put(option.filedName + "." + option.op, TextUtils.equals("like", option.op) ? "%" + option.keyword + "%" : option.keyword);
            }
        }
        mRequest.search = mRequest.searchMap == null || mRequest.searchMap.isEmpty() ? "{}" : new Gson().toJson(mRequest.searchMap);
        mRequest.pageNumber = mCurrentPage;
        mRequest.pageSize = 10;
        mRequest.searchMap = null;
        mDisposableD = RestAPI.getInstance().apiServiceSB()
                .queryResourceData(
                        SharedPreferencesDataSource.getSignInResponse().user.username,
                        mResourceEntity.tableCode,
                        mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            if (responseData.total == null) {
                                responseData.total = 0;
                            }
                            mTotalPage = responseData.total % 10 == 0 ? responseData.total / 10 : responseData.total / 10 + 1;
                            responseData.columns.add(0, new DataQueryResult.Column());
                            responseData.columns.get(0).name = "序号";
                            responseData.columns.get(0).isDisplay = 1;

                            tv_total_page_cont.setText(getString(R.string.total_page_count, mTotalPage));
                            et_page_number.setText(String.valueOf(mCurrentPage));

                            tl_data.removeAllViews();
                            LayoutInflater inflater = LayoutInflater.from(tl_data.getContext());

                            ArrayList<String> names = new ArrayList<>();
                            ArrayList<String> enNames = new ArrayList<>();
                            {
                                TableRow headerRow = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                for (DataQueryResult.Column c : responseData.columns) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView columnHeader = (TextView) inflater.inflate(R.layout.data_table_cell_item, headerRow, false);
                                        columnHeader.setText(c.name);
                                        headerRow.addView(columnHeader);
                                        names.add(columnHeader.getText().toString());
                                        enNames.add(c.field);
                                    }
                                }
                                tl_data.addView(headerRow);
                            }

                            int j = 0;
                            for (Map<String, String> map : responseData.data) {
                                ArrayList<String> values = new ArrayList<>();
                                TableRow row = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                int i = 0;
                                for (DataQueryResult.Column c : responseData.columns) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView cell = (TextView) inflater.inflate(R.layout.data_table_cell_item, row, false);
                                        if (i == 0) {
                                            cell.setText(String.valueOf(j + 1));
                                        } else {
                                            String value = map.get(c.field);
                                            if (!TextUtils.isEmpty(value) && value.getBytes().length > 1024) {
                                                value = "数据过大，请在PC上浏览";
                                            }
                                            cell.setText(value);
                                        }
                                        row.addView(cell);
                                        values.add(cell.getText().toString());
                                        i++;
                                    }
                                }
                                row.setOnClickListener(v -> startActivity(new Intent(v.getContext(), ResourceRelevanceDataActivity.class)
                                        .putExtra("resource_id", mResourceId)
                                        .putStringArrayListExtra("en_names", enNames)
                                        .putStringArrayListExtra("names", names)
                                        .putStringArrayListExtra("values", values)));
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
