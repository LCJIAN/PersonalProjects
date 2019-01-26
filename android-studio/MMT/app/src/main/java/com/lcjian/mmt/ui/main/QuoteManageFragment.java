package com.lcjian.mmt.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.entity.PageResult;
import com.lcjian.mmt.data.network.entity.Quote;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.base.RecyclerFragment;
import com.lcjian.mmt.ui.base.SlimAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class QuoteManageFragment extends BaseFragment {


    public class QuotesFragment extends RecyclerFragment<Quote> {


        private SlimAdapter mAdapter;


        @Override
        public RecyclerView.Adapter onCreateAdapter(List<Quote> data) {
            mAdapter = SlimAdapter
                    .create()
                    .register(new SlimAdapter.SlimInjector<Quote>() {

                        @Override
                        public int onGetLayoutResource() {
                            return R.layout.detection_info_item;
                        }

                        @Override
                        public void onInit(SlimAdapter.SlimViewHolder<Quote> viewHolder) {
                            viewHolder.clicked(R.id.tv_image_preview, v -> GalleryFragment.newInstance(viewHolder.itemData.id)
                                    .show(getChildFragmentManager(), "GalleryFragment"));
                        }

                        @Override
                        public void onBind(Quote data, SlimAdapter.SlimViewHolder<Quote> viewHolder) {
                            Context context = viewHolder.itemView.getContext();
                        }
                    })
                    .enableDiff();
            return mAdapter;
        }

        @Override
        public Observable<PageResult<Quote>> onCreatePageObservable(int currentPage) {

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
                        PageResult<Quote> pageResult = new PageResult<>();
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
        public void notifyDataChanged(List<Quote> data) {
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

    }

}
