package com.lcjian.vastplayer.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Recommend;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RecommendFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.rv_recommend)
    RecyclerView rv_recommend;
    @BindView(R.id.srl_recommend)
    SwipeRefreshLayout srl_recommend;
    private Unbinder unbinder;

    private Observable<List<List<Recommend>>> mObservable;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        srl_recommend.setOnRefreshListener(this);
        srl_recommend.setColorSchemeResources(R.color.primary);

        rv_recommend.setHasFixedSize(true);
        rv_recommend.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (mObservable == null) {
            createNewObservable();
        }
        loadRecommend();
    }

    @Override
    public void onRefresh() {
        createNewObservable();
        loadRecommend();
    }

    private void createNewObservable() {
        mObservable = mRestAPI.spunSugarService().recommends()
                .map((Function<List<Recommend>, List<List<Recommend>>>) recommends -> {
                    Map<String, List<Recommend>> groupedRecommends = new LinkedHashMap<>();
                    groupedRecommends.put("banner", new ArrayList<>());
                    Gson gson = new Gson();
                    for (Recommend recommend : recommends) {
                        List<Recommend> groupItem;
                        String key = TextUtils.equals(recommend.title, "banner") ? recommend.title : recommend.title + recommend.type;
                        if (groupedRecommends.containsKey(key)) {
                            groupItem = groupedRecommends.get(key);
                        } else {
                            groupItem = new ArrayList<>();
                            groupedRecommends.put(key, groupItem);
                        }
                        recommend.convertedData = gson.fromJson(recommend.data, Subject.class);
                        recommend.data = null;
                        groupItem.add(recommend);
                    }
                    return new ArrayList<>(groupedRecommends.values());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache();
    }

    private void loadRecommend() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        setRefreshing(true);
        mDisposable = mObservable
                .subscribe(
                        lists -> rv_recommend.setAdapter(new RecommendAdapter(lists)),
                        throwable -> {
                            setRefreshing(false);
                            mObservable = null;
                            Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        },
                        () -> setRefreshing(false));
    }

    private void setRefreshing(final boolean refreshing) {
        srl_recommend.post(() -> {
            if (srl_recommend != null) {
                srl_recommend.setRefreshing(refreshing);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroyView();
        unbinder.unbind();
    }
}
