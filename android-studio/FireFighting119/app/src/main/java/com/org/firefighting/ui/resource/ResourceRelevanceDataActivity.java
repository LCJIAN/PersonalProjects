package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.lib.recyclerview.SlimAdapter;
import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.DataQueryResult;
import com.org.firefighting.data.network.entity.RelevanceTable;
import com.org.firefighting.data.network.entity.ResourceDataRequest;
import com.org.firefighting.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ResourceRelevanceDataActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;

    @BindView(R.id.tl_data_this)
    TableLayout tl_data_this;
    @BindView(R.id.rv_table_name)
    RecyclerView rv_table_name;

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

    private Disposable mDisposable;
    private Disposable mDisposableR;

    private String mResourceId;
    private ArrayList<String> mEnNames;
    private ArrayList<String> mNames;
    private ArrayList<String> mValues;

    private SlimAdapter mAdapter;
    private RelevanceTable mChecked;

    private int mCurrentPage = 1;
    private int mTotalPage = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_relevance_data);
        ButterKnife.bind(this);
        mResourceId = getIntent().getStringExtra("resource_id");
        mEnNames = getIntent().getStringArrayListExtra("en_names");
        mNames = getIntent().getStringArrayListExtra("names");
        mValues = getIntent().getStringArrayListExtra("values");

        tv_title.setText(R.string.data_detail);
        btn_nav_back.setOnClickListener(v -> onBackPressed());

        LayoutInflater inflater = LayoutInflater.from(tl_data_this.getContext());
        for (int i = 0; i < mNames.size(); i++) {
            TableRow row = (TableRow) inflater.inflate(R.layout.data_table_row_item_2, tl_data_this, false);
            TextView cellName = row.findViewById(R.id.tv_name);
            TextView cellValue = row.findViewById(R.id.tv_value);
            cellName.setText(mNames.get(i));
            cellValue.setText(mValues.get(i));
            if (i % 2 == 0) {
                cellName.setBackgroundColor(0xffeeeef5);
                cellValue.setBackgroundColor(0xffeeeef5);
            } else {
                cellName.setBackgroundColor(0xffffffff);
                cellValue.setBackgroundColor(0xffffffff);
            }
            tl_data_this.addView(row);
        }

        rv_table_name.setHasFixedSize(true);
        rv_table_name.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<RelevanceTable>() {

            @Override
            public int onGetLayoutResource() {
                return R.layout.table_name_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<RelevanceTable> viewHolder) {
                viewHolder.clicked(v -> {
                    if (mChecked == viewHolder.itemData) {
                        return;
                    }
                    mChecked = viewHolder.itemData;
                    mAdapter.notifyDataSetChanged();
                    mCurrentPage = 1;
                    mTotalPage = 0;
                    setupRelevance();
                });
            }

            @Override
            public void onBind(RelevanceTable data, SlimAdapter.SlimViewHolder<RelevanceTable> viewHolder) {
                viewHolder.text(R.id.tv_table_name, data.relevanceTableNameChinese)
                        .background(R.id.tv_table_name, mChecked == data ? R.drawable.shape_tab_checked : R.drawable.shape_tab_normal)
                        .textColor(R.id.tv_table_name, mChecked == data ? 0xffeeeef5 : 0xffbcbcbc);
            }
        });
        rv_table_name.setAdapter(mAdapter);

        tv_pre_page.setOnClickListener(v -> {
            if (mCurrentPage <= 1) {
                return;
            }
            mCurrentPage = mCurrentPage - 1;
            setupRelevance();
        });
        tv_next_page.setOnClickListener(v -> {
            if (mCurrentPage >= mTotalPage) {
                return;
            }
            mCurrentPage = mCurrentPage + 1;
            setupRelevance();
        });

        tv_total_page_cont.setText(getString(R.string.total_page_count, mTotalPage));

        setupContent();
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        if (mDisposableR != null) {
            mDisposableR.dispose();
        }
        super.onDestroy();
    }

    private void setupContent() {
        showProgress();
        Map<String, String> map = new HashMap<>();
        map.put("presentTableId", mResourceId);
        mDisposable = RestAPI.getInstance().apiServiceSB()
                .getResourceRelevanceData(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();

                            if (responseData.data == null || responseData.data.isEmpty()) {
                                return;
                            }
                            mChecked = responseData.data.get(0);
                            mAdapter.updateData(responseData.data);
                            mCurrentPage = 1;
                            mTotalPage = 0;
                            setupRelevance();
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }

    private void setupRelevance() {
        showProgress();
        if (mDisposableR != null) {
            mDisposableR.dispose();
        }
        ResourceDataRequest mRequest = new ResourceDataRequest();

        List<RelevanceTable.RelevanceField> fields =
                new Gson().fromJson(mChecked.relevanceField, new TypeToken<List<RelevanceTable.RelevanceField>>() {
                }.getType());
        mRequest.searchMap = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            ResourceDataRequest.Option option = new ResourceDataRequest.Option();
            option.filedName = fields.get(i).target;
            option.op = "eq";
            option.keyword = mValues.get(mEnNames.indexOf(fields.get(i).source));
            mRequest.searchMap.put(option.filedName + "." + option.op, option.keyword);
        }
        mRequest.search = mRequest.searchMap == null || mRequest.searchMap.isEmpty() ? "" : new Gson().toJson(mRequest.searchMap);
        mRequest.pageNumber = mCurrentPage;
        mRequest.pageSize = 10;
        mRequest.searchMap = null;
        mDisposableR = RestAPI.getInstance().apiServiceSB()
                .queryResourceData(
                        SharedPreferencesDataSource.getSignInResponse().user.username,
                        mChecked.relevanceTableId,
                        mRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            mTotalPage = responseData.total % 10 == 0 ? responseData.total / 10 : responseData.total / 10 + 1;
                            responseData.columns.add(0, new DataQueryResult.Column());
                            responseData.columns.get(0).name = "序号";

                            tv_total_page_cont.setText(getString(R.string.total_page_count, mTotalPage));
                            et_page_number.setText(String.valueOf(mCurrentPage));

                            tl_data.removeAllViews();
                            LayoutInflater inflater = LayoutInflater.from(tl_data.getContext());

                            {
                                TableRow headerRow = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                for (DataQueryResult.Column c : responseData.columns) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView columnHeader = (TextView) inflater.inflate(R.layout.data_table_cell_item, headerRow, false);
                                        columnHeader.setText(c.name);
                                        headerRow.addView(columnHeader);
                                    }
                                }
                                tl_data.addView(headerRow);
                            }

                            int j = 0;
                            for (Map<String, String> map : responseData.data) {
                                TableRow row = (TableRow) inflater.inflate(R.layout.data_table_row_item, tl_data, false);
                                int i = 0;
                                for (DataQueryResult.Column c : responseData.columns) {
                                    if (c.isDisplay != null && c.isDisplay == 1) {
                                        TextView cell = (TextView) inflater.inflate(R.layout.data_table_cell_item, row, false);
                                        if (i == 0) {
                                            cell.setText(String.valueOf(j));
                                        } else {
                                            cell.setText(map.get(c.field));
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
