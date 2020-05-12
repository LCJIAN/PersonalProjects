package com.winside.lighting.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winside.lighting.App;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.data.network.entity.Building;
import com.winside.lighting.data.network.entity.Floor;
import com.winside.lighting.data.network.entity.Region;
import com.winside.lighting.ui.base.BaseFragment;
import com.winside.lighting.ui.base.SlimAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FloorPlanFilterFragment extends BaseFragment {

    @BindView(R.id.rv_building)
    RecyclerView rv_building;
    @BindView(R.id.rv_floor)
    RecyclerView rv_floor;
    @BindView(R.id.rv_region)
    RecyclerView rv_region;

    private Unbinder unbinder;

    private Building mCurrentBuilding;
    private Floor mCurrentFloor;
    private Region mCurrentRegion;

    private SlimAdapter mBuildingAdapter;
    private SlimAdapter mFloorAdapter;
    private SlimAdapter mRegionAdapter;

    private Disposable mBuildingDisposable;
    private Disposable mFloorDisposable;
    private Disposable mRegionDisposable;

    private boolean mFirst = true;

    private OnRegionSelectedListener mOnRegionSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.floor_plan_filter, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rv_building.setHasFixedSize(true);
        rv_building.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv_floor.setHasFixedSize(true);
        rv_floor.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rv_region.setHasFixedSize(true);
        rv_region.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mBuildingAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Building>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.floor_plan_filter_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Building> viewHolder) {
                viewHolder.clicked(v -> {
                    if (!mCurrentBuilding.id.equals(viewHolder.itemData.id)) {
                        mCurrentBuilding = viewHolder.itemData;
                        mCurrentFloor = null;
                        mCurrentRegion = null;
                        mBuildingAdapter.notifyDataSetChanged();
                        mFloorAdapter.updateData(null);
                        mRegionAdapter.updateData(null);
                        setupFloor();
                    }
                });
            }

            @Override
            public void onBind(Building data, SlimAdapter.SlimViewHolder<Building> viewHolder) {
                viewHolder.text(R.id.tv_name, data.name)
                        .checked(R.id.tv_name, mCurrentBuilding.id.equals(data.id));
            }
        });
        rv_building.setAdapter(mBuildingAdapter);

        mFloorAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Floor>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.floor_plan_filter_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Floor> viewHolder) {
                viewHolder.clicked(v -> {
                    if (mCurrentFloor == null || !mCurrentFloor.id.equals(viewHolder.itemData.id)) {
                        mCurrentFloor = viewHolder.itemData;
                        mCurrentRegion = null;
                        mFloorAdapter.notifyDataSetChanged();
                        mRegionAdapter.updateData(null);
                        setupRegion();
                    }
                });
            }

            @Override
            public void onBind(Floor data, SlimAdapter.SlimViewHolder<Floor> viewHolder) {
                viewHolder.text(R.id.tv_name, data.name)
                        .checked(R.id.tv_name, mCurrentFloor != null && mCurrentFloor.id.equals(data.id));
            }
        });
        rv_floor.setAdapter(mFloorAdapter);

        mRegionAdapter = SlimAdapter.create().register(new SlimAdapter.SlimInjector<Region>() {
            @Override
            public int onGetLayoutResource() {
                return R.layout.floor_plan_filter_item;
            }

            @Override
            public void onInit(SlimAdapter.SlimViewHolder<Region> viewHolder) {
                viewHolder.clicked(v -> {
                    if (mCurrentRegion == null || !mCurrentRegion.id.equals(viewHolder.itemData.id)) {
                        mCurrentRegion = viewHolder.itemData;
                        mRegionAdapter.notifyDataSetChanged();

                        if (mOnRegionSelectedListener != null) {
                            mOnRegionSelectedListener.onRegionSelected(mCurrentBuilding, mCurrentFloor, mCurrentRegion);
                        }
                    }
                });
            }

            @Override
            public void onBind(Region data, SlimAdapter.SlimViewHolder<Region> viewHolder) {
                viewHolder.text(R.id.tv_name, data.name)
                        .checked(R.id.tv_name, mCurrentRegion != null && mCurrentRegion.id.equals(data.id));
            }
        });
        rv_region.setAdapter(mRegionAdapter);

        setupBuilding();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mBuildingDisposable != null) {
            mBuildingDisposable.dispose();
        }
        if (mFloorDisposable != null) {
            mFloorDisposable.dispose();
        }
        if (mRegionDisposable != null) {
            mRegionDisposable.dispose();
        }
    }

    private void setupBuilding() {
        if (mBuildingDisposable != null) {
            mBuildingDisposable.dispose();
        }
        mBuildingDisposable = RestAPI.getInstance().lightingService().getBuildings(SharedPreferencesDataSource.getCurrentProjectId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            if (listResponseData.code == 1000) {
                                mCurrentBuilding = listResponseData.data.get(0);
                                mBuildingAdapter.updateData(listResponseData.data);
                                setupFloor();
                            } else {
                                Toast.makeText(App.getInstance(), listResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                        });
    }

    private void setupFloor() {
        if (mFloorDisposable != null) {
            mFloorDisposable.dispose();
        }
        mFloorDisposable = RestAPI.getInstance().lightingService().getFloors(mCurrentBuilding.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            if (listResponseData.code == 1000) {
                                if (mFirst) {
                                    mCurrentFloor = listResponseData.data.get(0);
                                }
                                mFloorAdapter.updateData(listResponseData.data);
                                if (mFirst) {
                                    setupRegion();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), listResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                        });
    }

    private void setupRegion() {
        if (mRegionDisposable != null) {
            mRegionDisposable.dispose();
        }
        mRegionDisposable = RestAPI.getInstance().lightingService().getRegions(mCurrentFloor.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listResponseData -> {
                            if (listResponseData.code == 1000) {
                                if (mFirst) {
                                    mCurrentRegion = listResponseData.data.get(0);
                                    mFirst = false;

                                    if (mOnRegionSelectedListener != null) {
                                        mOnRegionSelectedListener.onRegionSelected(mCurrentBuilding, mCurrentFloor, mCurrentRegion);
                                    }
                                }
                                mRegionAdapter.updateData(listResponseData.data);
                            } else {
                                Toast.makeText(App.getInstance(), listResponseData.message, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                        });
    }

    public FloorPlanFilterFragment setOnRegionSelectedListener(OnRegionSelectedListener listener) {
        this.mOnRegionSelectedListener = listener;
        return this;
    }

    public interface OnRegionSelectedListener {
        void onRegionSelected(Building building, Floor floor, Region region);
    }
}
