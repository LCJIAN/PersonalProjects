package com.lcjian.osc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lcjian.osc.App;
import com.lcjian.osc.R;
import com.lcjian.osc.data.entity.PageResult;
import com.lcjian.osc.data.network.entity.DetectionInfo;
import com.lcjian.osc.data.network.entity.DetectionRequestData;
import com.lcjian.osc.ui.base.LoadMoreAdapter;
import com.lcjian.osc.ui.base.RecyclerFragment;
import com.lcjian.osc.ui.base.SlimAdapter;
import com.lcjian.osc.util.DateUtils;
import com.lcjian.osc.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class DetectionFragment extends RecyclerFragment<DetectionInfo.Item> {

    private TextView tv_start_date;
    private TextView tv_end_date;
    private EditText et_car_no;
    private EditText et_car_lane;
    private EditText et_wheelbase_count;
    private EditText et_start_weight;
    private EditText et_end_weight;
    private EditText et_start_over_speed;
    private EditText et_end_over_speed;
    private AppCompatSpinner sp_check;
    private AppCompatSpinner sp_station_name;
    private Button btn_query;
    private Button btn_view_video;

    private SlimAdapter mAdapter;

    private Date mStartDate;
    private Date mEndDate;

    private String[] mCheckNames = new String[]{"全部", "是", "否"};
    private Boolean mCheck = null;

    private String[] mStationNames = new String[]{"测试"};
    private Long[] mStationIds = new Long[]{1L};
    private Long mStationId = mStationIds[0];

    @Override
    public void onLoadMoreAdapterCreated(LoadMoreAdapter loadMoreAdapter) {
        View header = LayoutInflater.from(recycler_view.getContext()).inflate(R.layout.detect_form, recycler_view, false);
        tv_start_date = header.findViewById(R.id.tv_start_date);
        tv_end_date = header.findViewById(R.id.tv_end_date);
        et_car_no = header.findViewById(R.id.et_car_no);
        et_car_lane = header.findViewById(R.id.et_car_lane);
        et_wheelbase_count = header.findViewById(R.id.et_wheelbase_count);
        et_start_weight = header.findViewById(R.id.et_start_weight);
        et_end_weight = header.findViewById(R.id.et_end_weight);
        et_start_over_speed = header.findViewById(R.id.et_start_over_speed);
        et_end_over_speed = header.findViewById(R.id.et_end_over_speed);
        sp_check = header.findViewById(R.id.sp_check);
        sp_station_name = header.findViewById(R.id.sp_station_name);
        btn_query = header.findViewById(R.id.btn_query);
        btn_view_video = header.findViewById(R.id.btn_view_video);

        {
            ArrayAdapter adapter = new ArrayAdapter<>(sp_check.getContext(),
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    mCheckNames);
            adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
            sp_check.setAdapter(adapter);
            sp_check.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mCheck = (position == 0 ? null : position == 1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        {
            ArrayAdapter adapter = new ArrayAdapter<>(sp_station_name.getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    mStationNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_station_name.setAdapter(adapter);
            sp_station_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mStationId = mStationIds[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        tv_start_date.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), DatePickerActivity.class), 1000));
        tv_end_date.setOnClickListener(v -> startActivityForResult(new Intent(v.getContext(), DatePickerActivity.class), 1001));
        btn_query.setOnClickListener(v -> refresh());
        loadMoreAdapter.addHeader(header);
    }

    @Override
    public RecyclerView.Adapter onCreateAdapter(List<DetectionInfo.Item> data) {
        mAdapter = SlimAdapter
                .create()
                .register(new SlimAdapter.SlimInjector<DetectionInfo.Item>() {

                    @Override
                    public int onGetLayoutResource() {
                        return R.layout.detection_info_item;
                    }

                    @Override
                    public void onInit(SlimAdapter.SlimViewHolder<DetectionInfo.Item> viewHolder) {
                        viewHolder.clicked(R.id.tv_image_preview, v -> GalleryFragment.newInstance(viewHolder.itemData.id)
                                .show(getChildFragmentManager(), "GalleryFragment"));
                    }

                    @Override
                    public void onBind(DetectionInfo.Item data, SlimAdapter.SlimViewHolder<DetectionInfo.Item> viewHolder) {
                        Context context = viewHolder.itemView.getContext();
                        viewHolder.text(R.id.tv_lane, context.getString(R.string.car_lane_holder, data.laneNumber))
                                .text(R.id.tv_car_no, context.getString(R.string.car_no_holder, data.licensePlate))
                                .text(R.id.tv_total_weight, String.valueOf(data.totalMass))
                                .text(R.id.tv_overload_weight, String.valueOf(data.overMass))
                                .text(R.id.tv_overload_percent, String.valueOf(data.overRateMass))
                                .text(R.id.tv_speed, String.valueOf(data.speed))
                                .text(R.id.tv_wheelbase, String.valueOf(data.axleLength))
                                .text(R.id.tv_wheelbase_count, String.valueOf(data.axleNumber))
                                .text(R.id.tv_direction, 0 == data.directionOff ? context.getString(R.string.direction_reverse) : context.getString(R.string.direction_forward))
                                .text(R.id.tv_check_status, data.isChecking ? context.getString(R.string.yes) : context.getString(R.string.no))
                                .text(R.id.tv_overload_status, data.isOvering ? context.getString(R.string.yes) : context.getString(R.string.no))
                                .text(R.id.tv_detection_time, context.getString(R.string.check_time_holder, DateUtils.convertDateToStr(data.recordTime, DateUtils.YYYY_MM_DD_HH_MM_SS)))
                                .textColor(R.id.tv_check_status, data.isChecking ? ContextCompat.getColor(context, R.color.colorTextGray) : 0xffff0b0b)
                                .textColor(R.id.tv_overload_status, data.isOvering ? 0xffff0b0b : ContextCompat.getColor(context, R.color.colorTextGray));
                    }
                })
                .enableDiff(new SlimAdapter.DiffCallback() {
                    @Override
                    public boolean areItemsTheSame(Object oldItem, Object newItem) {
                        return ((DetectionInfo.Item) oldItem).id.equals(((DetectionInfo.Item) newItem).id);
                    }

                    @Override
                    public boolean areContentsTheSame(Object oldItem, Object newItem) {
                        DetectionInfo.Item o = (DetectionInfo.Item) oldItem;
                        DetectionInfo.Item n = (DetectionInfo.Item) newItem;
                        return ObjectUtils.isEquals(o.laneNumber, n.laneNumber)
                                && ObjectUtils.isEquals(o.licensePlate, n.licensePlate)
                                && ObjectUtils.isEquals(o.totalMass, n.totalMass)
                                && ObjectUtils.isEquals(o.overRateMass, n.overRateMass)
                                && ObjectUtils.isEquals(o.speed, n.speed)
                                && ObjectUtils.isEquals(o.axleLength, n.axleLength)
                                && ObjectUtils.isEquals(o.axleNumber, n.axleNumber)
                                && ObjectUtils.isEquals(o.directionOff, n.directionOff)
                                && ObjectUtils.isEquals(o.isChecking, n.isChecking)
                                && ObjectUtils.isEquals(o.isOvering, n.isOvering)
                                && ObjectUtils.isEquals(o.recordTime, n.recordTime);
                    }
                });
        return mAdapter;
    }

    @Override
    public Observable<PageResult<DetectionInfo.Item>> onCreatePageObservable(int currentPage) {

        DetectionRequestData requestData = new DetectionRequestData();
        requestData.stationId = mStationId;
        requestData.axleNumber = TextUtils.isEmpty(et_wheelbase_count.getEditableText().toString()) ? null : Integer.parseInt(et_wheelbase_count.getEditableText().toString());
        requestData.speed = null;
        requestData.licensePlate = TextUtils.isEmpty(et_car_no.getEditableText().toString()) ? null : et_car_no.getEditableText().toString();
        requestData.startDate = mStartDate;
        requestData.endDate = mEndDate;
        requestData.startOverRateMass = TextUtils.isEmpty(et_start_over_speed.getEditableText().toString()) ? null : Double.parseDouble(et_start_over_speed.getEditableText().toString());
        requestData.endOverRateMass = TextUtils.isEmpty(et_end_over_speed.getEditableText().toString()) ? null : Double.parseDouble(et_end_over_speed.getEditableText().toString());
        requestData.startTotal = TextUtils.isEmpty(et_start_weight.getEditableText().toString()) ? null : Double.parseDouble(et_start_weight.getEditableText().toString());
        requestData.endTotal = TextUtils.isEmpty(et_end_weight.getEditableText().toString()) ? null : Double.parseDouble(et_end_weight.getEditableText().toString());
        requestData.laneNumber = TextUtils.isEmpty(et_car_lane.getEditableText().toString()) ? null : et_car_lane.getEditableText().toString();
        requestData.isOvering = null;
        requestData.isChecking = mCheck;
        requestData.directionOff = null;
        requestData.sorting = "RecordTime DESC";
        requestData.maxResultCount = 20;
        requestData.skipCount = (currentPage - 1) * 20;

        return mRestAPI.cloudService().getPreview(requestData)
                .map(detectionInfoResponseData -> {
                    DetectionInfo detectionInfo = detectionInfoResponseData.result;
                    PageResult<DetectionInfo.Item> pageResult = new PageResult<>();
                    if (detectionInfo.items == null) {
                        detectionInfo.items = new ArrayList<>();
                    }
                    pageResult.elements = detectionInfo.items;
                    pageResult.page_number = currentPage;
                    pageResult.page_size = 20;
                    pageResult.total_pages = detectionInfo.totalCount % 20 == 0 ? detectionInfo.totalCount / 20 : detectionInfo.totalCount / 20 + 1;
                    pageResult.total_elements = detectionInfo.totalCount;
                    return pageResult;
                })
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void notifyDataChanged(List<DetectionInfo.Item> data) {
        if (data == null || data.isEmpty()) {
            Toast.makeText(App.getInstance(), R.string.empty_results, Toast.LENGTH_LONG).show();
        }
        mAdapter.updateData(data);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
        recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            assert data != null;
            if (requestCode == 1000) {
                mStartDate = new Date(data.getLongExtra("date", 0));
                tv_start_date.setText(DateUtils.convertDateToStr(mStartDate));
            } else if (requestCode == 1001) {
                mEndDate = DateUtils.convertStrToDate(
                        DateUtils.convertDateToStr(new Date(data.getLongExtra("date", 0))) + " 23:59:59",
                        DateUtils.YYYY_MM_DD_HH_MM_SS);
                tv_end_date.setText(DateUtils.convertDateToStr(mEndDate));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
