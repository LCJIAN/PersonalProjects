package com.org.firefighting.ui.resource;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;

import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ResourceDataRequest;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DataQueryFragment extends BaseFragment {

    @BindView(R.id.ll_query_options)
    LinearLayout ll_query_options;

    private Unbinder mUnBinder;

    private Disposable mDisposable;
    private Disposable mDisposableD;

    private String mResourceId;

    private ResourceEntity mResourceEntity;

    private ResourceDataRequest mRequest;

    private static final String[] OPTION_NAMES = new String[]{"大于", "小于", "小于等于", "等于", "大于等于", "包括", "排除"};
    private static final String[] OPTION_OPS = new String[]{"gt", "lt", "lte", "eq", "gte", "inc", "ex"};

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
        setupContent();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        mDisposable.dispose();
        super.onDestroyView();
    }

    private void addOptionItem() {
        ResourceDataRequest.Option option = new ResourceDataRequest.Option();
        View view = LayoutInflater.from(ll_query_options.getContext())
                .inflate(R.layout.query_data_option_item, ll_query_options, false);
        AppCompatSpinner sp_field = view.findViewById(R.id.sp_field);
        AppCompatSpinner sp_option = view.findViewById(R.id.sp_option);
        EditText et_keyword = view.findViewById(R.id.et_keyword);
        TextView tv_search = view.findViewById(R.id.tv_search);
        {
            String[] filedNames = new String[mResourceEntity.fields.size()];
            for (int i = 0; i < filedNames.length; i++) {
                filedNames[i] = mResourceEntity.fields.get(i).name;
            }
            ArrayAdapter adapter = new ArrayAdapter<>(sp_option.getContext(),
                    R.layout.spinner_dropdown_item,
                    filedNames);
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
            ArrayAdapter adapter = new ArrayAdapter<>(sp_option.getContext(),
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
                            mResourceEntity = responseData.data;
                            addOptionItem();
                            setupOptions();
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupData() {
        showProgress();

        mRequest = new ResourceDataRequest();
        for (int i = 0; i < ll_query_options.getChildCount(); i++) {
            View view = ll_query_options.getChildAt(i);
            ResourceDataRequest.Option option = (ResourceDataRequest.Option) view.getTag();
            if (!TextUtils.isEmpty(option.keyword)) {
                if (mRequest.search == null) {
                    mRequest.search = new HashMap<>();
                }
                mRequest.search.put(option.filedName + "." + option.op, option.keyword);
            }
        }
        mRequest.pageNumber = 1;
        mRequest.pageSize = 20;
        mDisposableD = RestAPI.getInstance().apiServiceSB3()
                .queryResourceData(
                        SharedPreferencesDataSource.getSignInResponse().user.username,
                        mResourceEntity.tableCode,
                        mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();

                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}
