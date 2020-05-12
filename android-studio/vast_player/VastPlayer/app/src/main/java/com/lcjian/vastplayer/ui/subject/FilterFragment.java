package com.lcjian.vastplayer.ui.subject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;

import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.lcjian.lib.content.SimpleFragmentPagerAdapter;
import com.lcjian.lib.widget.MyViewPager;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.data.network.entity.Country;
import com.lcjian.vastplayer.data.network.entity.Genre;
import com.lcjian.vastplayer.ui.base.BaseBottomSheetDialogFragment;
import com.lcjian.vastplayer.ui.base.BaseFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.apmem.tools.layouts.FlowLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FilterFragment extends BaseBottomSheetDialogFragment implements View.OnClickListener {

    @BindView(R.id.tab_filter)
    SmartTabLayout tab_filter;
    @BindView(R.id.vp_filter)
    MyViewPager vp_filter;
    @BindView(R.id.btn_cancel)
    ImageButton btn_cancel;
    @BindView(R.id.btn_confirm)
    ImageButton btn_confirm;

    private Unbinder mUnBinder;

    private FilterData mFilterData;

    private String mFilterType;

    public static FilterFragment newInstance(FilterData filterData, String filterType) {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        args.putSerializable("filter_data", filterData);
        args.putString("filter_type", filterType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFilterData = new FilterData((FilterData) getArguments().getSerializable("filter_data"));
            mFilterType = getArguments().getString("filter_type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vp_filter.setAdapter(new SimpleFragmentPagerAdapter(getChildFragmentManager())
                .addFragment(SortFragment.newInstance(mFilterData), getString(R.string.sort))
                .addFragment(GenreFragment.newInstance(mFilterData, mFilterType), getString(R.string.genre))
                .addFragment(YearRatingFragment.newInstance(mFilterData), getString(R.string.year_rating))
                .addFragment(CountryFragment.newInstance(mFilterData, mFilterType), getString(R.string.country))
        );
        tab_filter.setViewPager(vp_filter);
        btn_cancel.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel: {
                dismiss();
            }
            break;
            case R.id.btn_confirm: {
                SimpleFragmentPagerAdapter adapter = (SimpleFragmentPagerAdapter) vp_filter.getAdapter();
                FilterFragment.SortFragment sortFragment = (FilterFragment.SortFragment) adapter.getItem(0);
                FilterFragment.GenreFragment genreFragment = (FilterFragment.GenreFragment) adapter.getItem(1);
                FilterFragment.YearRatingFragment yearRatingFragment = (FilterFragment.YearRatingFragment) adapter.getItem(2);
                FilterFragment.CountryFragment countryFragment = (FilterFragment.CountryFragment) adapter.getItem(3);
                mRxBus.send(new FilterData(genreFragment.getFilterData() == null ? mFilterData.genre : genreFragment.getFilterData().genre,
                        countryFragment.getFilterData() == null ? mFilterData.country : countryFragment.getFilterData().country,
                        yearRatingFragment.getFilterData() == null ? mFilterData.startReleaseDate : yearRatingFragment.getFilterData().startReleaseDate,
                        yearRatingFragment.getFilterData() == null ? mFilterData.endReleaseDate : yearRatingFragment.getFilterData().endReleaseDate,
                        yearRatingFragment.getFilterData() == null ? mFilterData.startVoteAverage : yearRatingFragment.getFilterData().startVoteAverage,
                        yearRatingFragment.getFilterData() == null ? mFilterData.endVoteAverage : yearRatingFragment.getFilterData().endVoteAverage,
                        sortFragment.getFilterData() == null ? mFilterData.sortType : sortFragment.getFilterData().sortType,
                        sortFragment.getFilterData() == null ? mFilterData.sortDirection : sortFragment.getFilterData().sortDirection)
                );
                dismiss();
            }
            break;
            default:
                break;
        }
    }

    public static class FilterData implements Serializable {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;
        public Genre genre;
        public Country country;
        public String startReleaseDate;
        public String endReleaseDate;
        public Float startVoteAverage;
        public Float endVoteAverage;
        public String sortType;
        public String sortDirection;

        FilterData() {
        }

        private FilterData(FilterData filterData) {
            this.genre = filterData.genre;
            this.country = filterData.country;
            this.startReleaseDate = filterData.startReleaseDate;
            this.endReleaseDate = filterData.endReleaseDate;
            this.startVoteAverage = filterData.startVoteAverage;
            this.endVoteAverage = filterData.endVoteAverage;
            this.sortType = filterData.sortType;
            this.sortDirection = filterData.sortDirection;
        }

        private FilterData(Genre genre, Country country, String startReleaseDate, String endReleaseDate,
                           Float startVoteAverage, Float endVoteAverage, String sortType, String sortDirection) {
            this.genre = genre;
            this.country = country;
            this.startReleaseDate = startReleaseDate;
            this.endReleaseDate = endReleaseDate;
            this.startVoteAverage = startVoteAverage;
            this.endVoteAverage = endVoteAverage;
            this.sortType = sortType;
            this.sortDirection = sortDirection;
        }
    }

    public static class GenreFragment extends BaseFragment {

        private FlowLayout fl_genres;

        private List<Genre> mGenres;

        private FilterData mFilterData;

        private String mFilterType;

        private Disposable mDisposable;

        private View mView;

        public static GenreFragment newInstance(FilterData filterData, String filterType) {
            GenreFragment fragment = new GenreFragment();
            Bundle args = new Bundle();
            args.putSerializable("filter_data", filterData);
            args.putString("filter_type", filterType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mFilterData = (FilterData) getArguments().getSerializable("filter_data");
                mFilterType = getArguments().getString("filter_type");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if (mView == null) {
                mView = inflater.inflate(R.layout.fragment_genres, container, false);
                fl_genres = mView.findViewById(R.id.fl_genres);
            }
            return mView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            if (mDisposable == null) {
                mDisposable = Observable.just(mFilterType)
                        .flatMap(type -> mRestAPI.spunSugarService().subjectGenres(type))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                genres -> {
                                    mGenres = genres;
                                    Genre all = new Genre();
                                    all.name = getString(R.string.all);
                                    mGenres.add(0, all);
                                    for (Genre genre : mGenres) {
                                        AppCompatRadioButton radioButton = new AppCompatRadioButton(getActivity());
                                        radioButton.setText(genre.name);
                                        radioButton.setOnClickListener(v -> {
                                            for (int i = 0; i < fl_genres.getChildCount(); i++) {
                                                if (fl_genres.getChildAt(i) == v) {
                                                    mFilterData.genre = mGenres.get(i);
                                                    ((AppCompatRadioButton) fl_genres.getChildAt(i)).setChecked(true);
                                                } else {
                                                    ((AppCompatRadioButton) fl_genres.getChildAt(i)).setChecked(false);
                                                }
                                            }
                                        });
                                        if ((mFilterData.genre == null && TextUtils.equals(getString(R.string.all), genre.name))
                                                || (mFilterData.genre != null && TextUtils.equals(mFilterData.genre.name, genre.name))) {
                                            radioButton.setChecked(true);
                                        }
                                        fl_genres.addView(radioButton);
                                    }

                                },
                                throwable -> Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        }

        @Override
        public void onDestroyView() {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            super.onDestroyView();
        }

        public FilterData getFilterData() {
            return mFilterData;
        }
    }

    public static class CountryFragment extends BaseFragment {

        private FlowLayout fl_countries;

        private List<Country> mCountries;

        private FilterData mFilterData;

        private String mFilterType;

        private Disposable mDisposable;

        private View mView;

        public static CountryFragment newInstance(FilterData filterData, String filterType) {
            CountryFragment fragment = new CountryFragment();
            Bundle args = new Bundle();
            args.putSerializable("filter_data", filterData);
            args.putString("filter_type", filterType);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mFilterData = (FilterData) getArguments().getSerializable("filter_data");
                mFilterType = getArguments().getString("filter_type");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if (mView == null) {
                mView = inflater.inflate(R.layout.fragment_countries, container, false);
                fl_countries = mView.findViewById(R.id.fl_countries);
            }
            return mView;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            if (mDisposable == null) {
                mDisposable = Observable.just(mFilterType)
                        .flatMap(type -> mRestAPI.spunSugarService().subjectCountries(type))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                countries -> {
                                    mCountries = countries;
                                    Country all = new Country();
                                    all.name = getString(R.string.all);
                                    mCountries.add(0, all);
                                    for (Country country : mCountries) {
                                        AppCompatRadioButton radioButton = new AppCompatRadioButton(getActivity());
                                        radioButton.setText(country.name);
                                        radioButton.setOnClickListener(v -> {
                                            for (int i = 0; i < fl_countries.getChildCount(); i++) {
                                                if (fl_countries.getChildAt(i) == v) {
                                                    mFilterData.country = mCountries.get(i);
                                                    ((AppCompatRadioButton) fl_countries.getChildAt(i)).setChecked(true);
                                                } else {
                                                    ((AppCompatRadioButton) fl_countries.getChildAt(i)).setChecked(false);
                                                }
                                            }
                                        });
                                        if ((mFilterData.country == null && TextUtils.equals(getString(R.string.all), country.name))
                                                || (mFilterData.country != null && TextUtils.equals(mFilterData.country.name, country.name))) {
                                            radioButton.setChecked(true);
                                        }
                                        fl_countries.addView(radioButton);
                                    }
                                },
                                throwable -> Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_SHORT).show()
                        );
            }
        }

        @Override
        public void onDestroyView() {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
            super.onDestroyView();
        }

        public FilterData getFilterData() {
            return mFilterData;
        }
    }

    public static class YearRatingFragment extends BaseFragment {

        private TextView tv_start_release_date;
        private CrystalRangeSeekbar range_seek_bar_year;
        private TextView tv_end_release_date;
        private TextView tv_start_vote_average;
        private CrystalRangeSeekbar range_seek_bar_vote_average;
        private TextView tv_end_vote_average;

        private FilterData mFilterData;

        public static YearRatingFragment newInstance(FilterData filterData) {
            YearRatingFragment fragment = new YearRatingFragment();
            Bundle args = new Bundle();
            args.putSerializable("filter_data", filterData);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mFilterData = (FilterData) getArguments().getSerializable("filter_data");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_year_rating, container, false);
            tv_start_release_date = view.findViewById(R.id.tv_start_release_date);
            range_seek_bar_year = view.findViewById(R.id.range_seek_bar_year);
            tv_end_release_date = view.findViewById(R.id.tv_end_release_date);
            tv_start_vote_average = view.findViewById(R.id.tv_start_vote_average);
            range_seek_bar_vote_average = view.findViewById(R.id.range_seek_bar_vote_average);
            tv_end_vote_average = view.findViewById(R.id.tv_end_vote_average);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            range_seek_bar_year.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
                tv_start_release_date.setText(String.valueOf(minValue));
                tv_end_release_date.setText(String.valueOf(maxValue));
                mFilterData.startReleaseDate = String.valueOf(minValue);
                mFilterData.endReleaseDate = String.valueOf(maxValue);
            });
            range_seek_bar_vote_average.setOnRangeSeekbarChangeListener((minValue, maxValue) -> {
                tv_start_vote_average.setText(String.valueOf(minValue));
                tv_end_vote_average.setText(String.valueOf(maxValue));
                mFilterData.startVoteAverage = minValue.floatValue();
                mFilterData.endVoteAverage = maxValue.floatValue();
            });
            if (!TextUtils.isEmpty(mFilterData.startReleaseDate) && !TextUtils.isEmpty(mFilterData.endReleaseDate)) {
            }
        }

        public FilterData getFilterData() {
            return mFilterData;
        }
    }

    public static class SortFragment extends BaseFragment {

        private FlowLayout fl_sort_types;
        private RadioGroup rg_sort_direction;

        private List<String> mSortTypes;

        private FilterData mFilterData;

        public static SortFragment newInstance(FilterData filterData) {
            SortFragment fragment = new SortFragment();
            Bundle args = new Bundle();
            args.putSerializable("filter_data", filterData);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mFilterData = (FilterData) getArguments().getSerializable("filter_data");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_sort, container, false);
            fl_sort_types = view.findViewById(R.id.fl_sort_types);
            rg_sort_direction = view.findViewById(R.id.rg_sort_direction);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            mSortTypes = new ArrayList<>();
            mSortTypes.add("release_date");
            mSortTypes.add("popularity");
            mSortTypes.add("vote_average");
            mSortTypes.add("create_time");
            for (String item : mSortTypes) {
                AppCompatRadioButton radioButton = new AppCompatRadioButton(getActivity());
                radioButton.setText(getSortTypeTitle(item));
                radioButton.setOnClickListener(v -> {
                    for (int i = 0; i < fl_sort_types.getChildCount(); i++) {
                        if (fl_sort_types.getChildAt(i) == v) {
                            mFilterData.sortType = mSortTypes.get(i);
                            ((AppCompatRadioButton) fl_sort_types.getChildAt(i)).setChecked(true);
                        } else {
                            ((AppCompatRadioButton) fl_sort_types.getChildAt(i)).setChecked(false);
                        }
                    }
                });
                if ((mFilterData.sortType == null && TextUtils.equals("create_time", item))
                        || TextUtils.equals(mFilterData.sortType, item)) {
                    radioButton.setChecked(true);
                }
                fl_sort_types.addView(radioButton);
            }
            rg_sort_direction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.rb_sort_desc:
                            mFilterData.sortDirection = "DESC";
                            break;
                        case R.id.rb_sort_asc:
                            mFilterData.sortDirection = "ASC";
                            break;
                        default:
                            break;
                    }
                }
            });
            rg_sort_direction.check(TextUtils.equals(mFilterData.sortDirection, "ASC") ?
                    R.id.rb_sort_asc : R.id.rb_sort_desc);
        }

        private String getSortTypeTitle(String sortType) {
            String title;
            switch (sortType) {
                case "release_date":
                    title = getString(R.string.release_date);
                    break;
                case "popularity":
                    title = getString(R.string.popularity);
                    break;
                case "vote_average":
                    title = getString(R.string.vote_average);
                    break;
                case "create_time":
                    title = getString(R.string.create_time);
                    break;
                default:
                    title = getString(R.string.create_time);
                    break;
            }
            return title;
        }

        public FilterData getFilterData() {
            return mFilterData;
        }
    }
}
