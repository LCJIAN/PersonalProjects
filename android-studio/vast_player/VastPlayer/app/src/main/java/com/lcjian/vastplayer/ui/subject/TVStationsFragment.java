package com.lcjian.vastplayer.ui.subject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.lcjian.lib.recyclerview.AdvanceAdapter;
import com.lcjian.lib.recyclerview.RecyclerViewPositionHelper;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.TvStation;
import com.lcjian.vastplayer.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TVStationsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.srl_tv_station)
    SwipeRefreshLayout srl_tv_station;
    @BindView(R.id.rv_tv_station_group_name)
    RecyclerView rv_tv_station_group_name;
    @BindView(R.id.rv_tv_station)
    RecyclerView rv_tv_station;

    private Unbinder mUnBinder;

    private List<String> mTvStationTypes;
    private List<Object> mTvStationsData;

    private TvStationTypeAdapter mTvStationTypeAdapter;
    private TvStationAdapter mTvStationAdapter;

    private Observable<Pair<List<String>, List<Object>>> mObservable;

    private Disposable mDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTvStationTypes = new ArrayList<>();
        mTvStationsData = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tv_station, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        srl_tv_station.setOnRefreshListener(this);
        srl_tv_station.setColorSchemeResources(R.color.primary);

        mTvStationTypeAdapter = new TvStationTypeAdapter(mTvStationTypes);
        rv_tv_station_group_name.setAdapter(new AdvanceAdapter(mTvStationTypeAdapter)
                .setOnItemClickListener(itemView -> {
                    String typeName = ((TvStationTypeAdapter.TVStationTypeViewHolder) rv_tv_station_group_name.getChildViewHolder(itemView)).typeName;
                    int i = 0;
                    for (Object object : mTvStationsData) {
                        if (object instanceof String
                                && TextUtils.equals(typeName, (String) object)) {
                            rv_tv_station.smoothScrollToPosition(i);
                            break;
                        }
                        i++;
                    }
                }));
        rv_tv_station_group_name.setHasFixedSize(true);
        rv_tv_station_group_name.setLayoutManager(new LinearLayoutManager(getActivity()));

        mTvStationAdapter = new TvStationAdapter(mTvStationsData);
        rv_tv_station.setAdapter(mTvStationAdapter);
        rv_tv_station.setHasFixedSize(true);
        rv_tv_station.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_tv_station.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private String type_name;

            private RecyclerViewPositionHelper mHelper;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mHelper == null) {
                    mHelper = RecyclerViewPositionHelper.createHelper(recyclerView);
                }
                int position = mHelper.findFirstVisibleItemPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                String typeName;
                if (recyclerView.getAdapter().getItemViewType(position) == 0) {
                    typeName = (String) mTvStationsData.get(position);
                } else {
                    typeName = ((TvStation) mTvStationsData.get(position)).type;
                }
                if (!TextUtils.equals(type_name, typeName)) {
                    type_name = typeName;
                    mTvStationTypeAdapter.setCheckedTypeName(type_name);
                    mTvStationTypeAdapter.notifyDataSetChanged();
                }
            }
        });

        if (mObservable == null) {
            createNewObservable();
        }
        loadTVStations();
    }

    @Override
    public void onRefresh() {
        createNewObservable();
        loadTVStations();
    }

    private void createNewObservable() {
        mObservable = mRestAPI.spunSugarService().tvStations(null)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(tvStations -> {
                    final List<String> first = new ArrayList<>();
                    final List<Object> second = new ArrayList<>();
                    for (TvStation tvStation : tvStations) {
                        if (!first.contains(tvStation.type)) {
                            first.add(tvStation.type);
                            second.add(tvStation.type);
                        }
                        second.add(tvStation);
                    }

                    Collections.sort(second, (o1, o2) -> {
                        String s1;
                        String s2;
                        if (o1 instanceof String) {
                            s1 = (String) o1;
                        } else {
                            s1 = ((TvStation) o1).type;
                        }
                        if (o2 instanceof String) {
                            s2 = (String) o2;
                        } else {
                            s2 = ((TvStation) o2).type;
                        }
                        return first.indexOf(s1) - first.indexOf(s2);
                    });
                    return Pair.create(first, second);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private void loadTVStations() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        setRefreshing(true);
        mDisposable = mObservable
                .subscribe(listListPair -> {
                    mTvStationTypes.clear();
                    mTvStationsData.clear();
                    assert listListPair.first != null;
                    assert listListPair.second != null;
                    mTvStationTypes.addAll(listListPair.first);
                    mTvStationsData.addAll(listListPair.second);
                    mTvStationTypeAdapter.notifyDataSetChanged();
                    mTvStationAdapter.notifyDataSetChanged();
                }, throwable -> {
                    setRefreshing(false);
                    mObservable = null;
                    Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }, () -> setRefreshing(false));
    }

    private void setRefreshing(final boolean refreshing) {
        srl_tv_station.post(() -> {
            if (srl_tv_station != null) {
                srl_tv_station.setRefreshing(refreshing);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mUnBinder.unbind();
    }
}
