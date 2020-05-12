package com.lcjian.vastplayer.ui.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.db.entity.SearchHistory;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;

import java.util.Date;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener {

    @BindView(R.id.rv_search_auto_data)
    RecyclerView rv_search_auto_data;
    @BindView(R.id.rl_ad_content)
    RelativeLayout rl_ad_content;

    SearchView mSearchView;

    private BannerView banner;

    private SearchAdapter mSearchAdapter;

    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
        mSearchAdapter = new SearchAdapter(mRxBus);
        rv_search_auto_data.setHasFixedSize(true);
        rv_search_auto_data.setLayoutManager(new LinearLayoutManager(this));
        rv_search_auto_data.setAdapter(mSearchAdapter);

        loadAD();
    }

    private void loadAD() {
        banner = new BannerView(this, ADSize.BANNER, Constants.QQ_ID, Constants.GTD_BANNER_ID);
        banner.setRefresh(30);
        banner.setADListener(new AbstractBannerADListener() {
            @Override
            public void onNoAD(AdError adError) {
                banner.loadAD();
            }

            @Override
            public void onADReceiv() {
            }
        });
        rl_ad_content.addView(banner);
        banner.loadAD();
    }


    @Override
    protected void onStart() {
        mDisposables = new CompositeDisposable();
        mDisposables.add(mRxBus.asFlowable()
                .subscribe(
                        event -> {
                            if (event instanceof SearchHistory) {
                                mSearchView.setQuery(((SearchHistory) event).text, true);
                            }
                        }));
        refresh("");
        super.onStart();
    }

    private void refresh(String query) {
        mDisposables.add(Observable
                .just(query)
                .flatMap(q -> {
                    if (TextUtils.isEmpty(q)) {
                        return Observable.just(mAppDatabase.searchHistoryDao().getOrderByTimeSync());
                    } else {
                        return Observable.just(mAppDatabase.searchHistoryDao().getByTextLikeSync(q + "%"));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        searchHistories -> mSearchAdapter.replaceAll(searchHistories),
                        throwable -> Toast.makeText(SearchActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_view);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        Fragment searchResultFragment = getSupportFragmentManager().findFragmentByTag("SearchResultFragment");
        if (searchResultFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(searchResultFragment).commit();
        }
        rv_search_auto_data.setVisibility(View.VISIBLE);
        refresh(query);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        mSearchView.clearFocus();
        mDisposables.add(Observable
                .defer(() -> Observable.just(mAppDatabase.searchHistoryDao().getByTextSync(query)))
                .subscribe(searchHistories -> {
                    SearchHistory searchHistory;
                    if (searchHistories.isEmpty()) {
                        searchHistory = new SearchHistory();
                        searchHistory.text = query;
                    } else {
                        searchHistory = searchHistories.get(0);
                    }
                    searchHistory.updateTime = new Date();
                    mAppDatabase.searchHistoryDao().insert(searchHistory);
                }));
        rv_search_auto_data.setVisibility(View.INVISIBLE);
        getSupportFragmentManager().beginTransaction().add(R.id.fl_search_result_fragment_container,
                SearchResultFragment.newInstance(query), "SearchResultFragment").commit();
        return true;
    }

    @Override
    protected void onStop() {
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        super.onStop();
    }
}
