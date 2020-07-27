package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.ThrowableConsumerAdapter;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.data.network.RestAPI;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DataFieldFragment extends BaseFragment {

    @BindView(R.id.tl_dat_field)
    TableLayout tl_dat_field;

    private Unbinder mUnBinder;

    private Disposable mDisposable;

    private String mResourceId;

    public static DataFieldFragment newInstance(String taskId) {
        DataFieldFragment fragment = new DataFieldFragment();
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
        View view = inflater.inflate(R.layout.fragment_resource_data_field, container, false);
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

    private void setupContent() {
        showProgress();
        mDisposable = RestAPI.getInstance().apiServiceSB2()
                .getResourceDetail(mResourceId, SharedPreferencesDataSource.getSignInResponse().user.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseData -> {
                            hideProgress();
                            LayoutInflater inflater = LayoutInflater.from(tl_dat_field.getContext());
                            for (ResourceEntity.Field field : responseData.data.fields) {
                                TableRow row = (TableRow) inflater.inflate(R.layout.data_field_item, tl_dat_field, false);
                                TextView tv_data_field_index = row.findViewById(R.id.tv_data_field_index);
                                TextView tv_english_name = row.findViewById(R.id.tv_english_name);
                                TextView tv_chinese_name = row.findViewById(R.id.tv_chinese_name);
                                TextView tv_data_format = row.findViewById(R.id.tv_data_format);
                                tv_data_field_index.setText(String.valueOf(field.sort));
                                tv_english_name.setText(field.name);
                                tv_chinese_name.setText(field.chineseName);
                                tv_data_format.setText(field.dataType);

                                tl_dat_field.addView(row);
                            }
                        },
                        throwable -> {
                            hideProgress();
                            ThrowableConsumerAdapter.accept(throwable);
                        });
    }
}
