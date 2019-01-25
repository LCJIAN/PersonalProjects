package com.lcjian.osc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lcjian.osc.R;
import com.lcjian.osc.data.entity.PageResult;
import com.lcjian.osc.data.network.entity.DetectionInfo;
import com.lcjian.osc.data.network.entity.DetectionRequestData;
import com.lcjian.osc.ui.base.RecyclerFragment;
import com.lcjian.osc.ui.base.SlimAdapter;
import com.lcjian.osc.util.DateUtils;
import com.lcjian.osc.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class OverloadFragment extends RecyclerFragment<DetectionInfo.Item> {

    private SlimAdapter mAdapter;
    private Disposable mDisposableInterval;

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
        return mRxBus.asFlowable().filter(o -> o instanceof Long)
                .debounce(1, TimeUnit.SECONDS)
                .switchMap(aLong -> {
                    DetectionRequestData requestData = new DetectionRequestData();
                    requestData.stationId = 1L;
                    requestData.axleNumber = null;
                    requestData.speed = null;
                    requestData.licensePlate = null;
                    requestData.startDate = null;
                    requestData.endDate = null;
                    requestData.startOverRateMass = null;
                    requestData.endOverRateMass = null;
                    requestData.startTotal = null;
                    requestData.endTotal = null;
                    requestData.laneNumber = null;
                    requestData.isOvering = true;
                    requestData.isChecking = null;
                    requestData.directionOff = null;
                    requestData.sorting = "RecordTime DESC";
                    requestData.maxResultCount = Integer.MAX_VALUE;
                    requestData.skipCount = 0;
                    return mRestAPI.cloudService().getPreview(requestData).toFlowable().onBackpressureDrop();
                })
                .map(detectionInfoResponseData -> {
                    DetectionInfo detectionInfo = detectionInfoResponseData.result;
                    PageResult<DetectionInfo.Item> pageResult = new PageResult<>();
                    if (detectionInfo.items == null) {
                        detectionInfo.items = new ArrayList<>();
                    }
                    pageResult.elements = detectionInfo.items;
                    pageResult.page_number = 1;
                    pageResult.page_size = detectionInfo.totalCount;
                    pageResult.total_pages = 1;
                    pageResult.total_elements = detectionInfo.totalCount;
                    return pageResult;
                })
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void notifyDataChanged(List<DetectionInfo.Item> data) {
        mAdapter.updateData(data);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary);
        recycler_view.setLayoutManager(new LinearLayoutManager(view.getContext()));
        super.onViewCreated(view, savedInstanceState);
        onHiddenChanged(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposableInterval != null) {
            mDisposableInterval.dispose();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (mDisposableInterval != null) {
                mDisposableInterval.dispose();
            }
        } else {
            mDisposableInterval = Observable
                    .interval(0, 5, TimeUnit.SECONDS)
                    .subscribe(aLong -> mRxBus.send(aLong));
        }
    }
}
