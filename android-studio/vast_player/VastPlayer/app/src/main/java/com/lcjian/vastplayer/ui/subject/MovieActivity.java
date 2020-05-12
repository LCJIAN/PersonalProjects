package com.lcjian.vastplayer.ui.subject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.glidepalette.GlidePalette;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lcjian.lib.util.common.DateUtils;
import com.lcjian.vastplayer.Constants;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.Tmdb;
import com.lcjian.vastplayer.data.db.entity.Favourite;
import com.lcjian.vastplayer.data.network.entity.Backdrop;
import com.lcjian.vastplayer.data.network.entity.Country;
import com.lcjian.vastplayer.data.network.entity.Poster;
import com.lcjian.vastplayer.data.network.entity.Subject;
import com.lcjian.vastplayer.data.network.entity.VideoUrl;
import com.lcjian.vastplayer.ui.ShareFragment;
import com.lcjian.vastplayer.ui.base.BaseActivity;
import com.lcjian.vastplayer.ui.player.VideoPlayerActivity;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.umeng.socialize.UMShareAPI;
import com.uwetrottmann.tmdb2.entities.Configuration;
import com.uwetrottmann.tmdb2.entities.Genre;
import com.uwetrottmann.tmdb2.entities.Movie;

import org.apmem.tools.layouts.FlowLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MovieActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbar_layout;
    @BindView(R.id.iv_backdrop)
    ImageView iv_backdrop;
    @BindView(R.id.btn_favourite)
    ShineButton btn_favourite;
    @BindView(R.id.app_bar)
    AppBarLayout app_bar;
    @BindView(R.id.fab_play)
    FloatingActionButton fab_play;
    @BindView(R.id.iv_movie_poster)
    ImageView iv_movie_poster;
    @BindView(R.id.tv_movie_title)
    TextView tv_movie_title;
    @BindView(R.id.rb_movie_vote_average)
    RatingBar rb_movie_vote_average;
    @BindView(R.id.tv_movie_vote_count)
    TextView tv_movie_vote_count;
    @BindView(R.id.tv_movie_meta)
    TextView tv_movie_meta;
    @BindView(R.id.tv_movie_overview)
    ReadMoreTextView tv_movie_overview;
    @BindView(R.id.fl_video_urls)
    FlowLayout fl_video_urls;
    private Subject mSubject;
    private List<VideoUrl> mVideoUrls;
    private CompositeDisposable mDisposables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubject = (Subject) getIntent().getSerializableExtra(Constants.BUNDLE_PARAMETER_SUBJECT);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        setTitle(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btn_favourite.init(this);
        btn_favourite.setOnClickListener(this);
        fab_play.setOnClickListener(this);
        setup(mSubject);

        mDisposables = new CompositeDisposable();
        mDisposables.add(Observable.zip(
                mRestAPI.spunSugarService().subjectDetail(mSubject.id),
                Observable.just(getResources().getConfiguration().locale.getLanguage()),
                Pair::create)
                .map(pair -> {
                    Subject origin = pair.first;
                    String language = pair.second;
                    if (origin.properties == null || TextUtils.isEmpty(origin.properties.get("tmdb_id"))) {
                        return origin;
                    } else {
                        if (!TextUtils.equals("movie", origin.type)) {
                            return origin;
                        } else {
                            String tmdbId = origin.properties.get("tmdb_id");
                            Subject subject = new Subject();
                            Configuration configuration;
                            Tmdb tmdb = new Tmdb(Constants.TMDB_API_KEY);
                            try {
                                Movie movie = tmdb.moviesService().summary(Integer.parseInt(tmdbId),
                                        (language.endsWith("zh") ? "zh" : null), null).execute().body();
                                if (movie != null) {
                                    configuration = tmdb.configurationService().configuration().execute().body();
                                    movie.backdrop_path = configuration.images.secure_base_url + configuration.images.backdrop_sizes.get(1) + movie.backdrop_path;
                                    movie.poster_path = configuration.images.secure_base_url + configuration.images.backdrop_sizes.get(1) + movie.poster_path;

                                    subject.title = movie.title;
                                    subject.backdrops = new ArrayList<>(1);
                                    Backdrop backdrop = new Backdrop();
                                    backdrop.url = movie.backdrop_path;
                                    subject.backdrops.add(backdrop);
                                    subject.posters = new ArrayList<>(1);
                                    Poster poster = new Poster();
                                    poster.url = movie.poster_path;
                                    subject.posters.add(poster);
                                    subject.vote_average = movie.vote_average == null ? 0 : movie.vote_average.floatValue();
                                    subject.vote_count = movie.vote_count;
                                    subject.release_date = movie.release_date;
                                    subject.overview = movie.overview;
                                    subject.genres = new ArrayList<>();
                                    subject.properties = new HashMap<>(1);
                                    subject.properties.put("minutes", movie.runtime == null ? "0" : String.valueOf(movie.runtime));
                                    for (Genre genre : movie.genres) {
                                        com.lcjian.vastplayer.data.network.entity.Genre genreA = new com.lcjian.vastplayer.data.network.entity.Genre();
                                        genreA.name = genre.name;
                                        subject.genres.add(genreA);
                                    }
                                    subject.production_countries = new ArrayList<>();
                                    for (com.uwetrottmann.tmdb2.entities.Country productionCountry : movie.production_countries) {
                                        Country country = new Country();
                                        country.name = productionCountry.name;
                                        subject.production_countries.add(country);
                                    }
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return subject;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onTerminateDetach()
                .subscribe(this::setup,
                        throwable -> Toast.makeText(MovieActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));
        mDisposables.add(mRestAPI.spunSugarService()
                .subjectSources(mSubject.id)
                .zipWith(Observable.just(mSubject.type), (videoUrls, s) -> {
                    for (VideoUrl videoUrl : videoUrls) {
                        videoUrl.type = s;
                    }
                    return videoUrls;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        videoUrls -> {
                            for (final VideoUrl videoUrl : videoUrls) {
                                videoUrl.parentId = mSubject.id;
                                View view = LayoutInflater.from(MovieActivity.this).inflate(R.layout.video_url_item, fl_video_urls, false);
                                ((Button) view.findViewById(R.id.tv_video_url_name)).setText(videoUrl.name);
                                view.setOnClickListener(v -> checkThenPlay(videoUrl));
                                fl_video_urls.addView(view);
                            }
                            mVideoUrls = videoUrls;
                        },
                        throwable -> Toast.makeText(MovieActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));
        mDisposables.add(mAppDatabase.favouriteDao().getByIdAndTypeAsync(mSubject.id, mSubject.type)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        favourites -> btn_favourite.setChecked(favourites != null && !favourites.isEmpty()),
                        throwable -> Toast.makeText(MovieActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show()
                ));
    }

    private void setup(Subject subject) {
        String backdrop_path = subject.backdrops == null || subject.backdrops.isEmpty() ? "" : subject.backdrops.get(0).url;
        String poster_path = subject.posters == null || subject.posters.isEmpty() ? "" : subject.posters.get(0).url;
        Float vote_average = subject.vote_average == null ? 0 : subject.vote_average;
        int vote_count = subject.vote_count == null ? 0 : subject.vote_count;
        Date release_date = subject.release_date;
        Integer runtime = subject.properties == null ? 0 : Integer.valueOf(subject.properties.get("minutes"));
        String overview = subject.overview;
        String genres = null;
        if (subject.genres != null && !subject.genres.isEmpty()) {
            List<String> strGenres = new ArrayList<>();
            for (com.lcjian.vastplayer.data.network.entity.Genre genre : subject.genres) {
                strGenres.add(genre.name);
            }
            genres = TextUtils.join(",", strGenres);
        }
        String production_countries = null;
        if (subject.production_countries != null && !subject.production_countries.isEmpty()) {
            List<String> strProductionCountries = new ArrayList<>();
            for (Country country : subject.production_countries) {
                strProductionCountries.add(country.name);
            }
            production_countries = TextUtils.join(",", strProductionCountries);
        }
        tv_movie_title.setText(mSubject.title);
        Glide.with(MovieActivity.this)
                .load(backdrop_path)
                .listener(GlidePalette.with(backdrop_path).intoCallBack(palette -> {
                    if (palette != null) {
                        int mutedColor = palette.getMutedColor(ContextCompat.getColor(MovieActivity.this, R.color.primary));
                        toolbar_layout.setContentScrimColor(mutedColor);
                        toolbar_layout.setStatusBarScrimColor(ColorUtils.compositeColors(0x33000000, mutedColor));
                        int[] colors = new int[]{palette.getVibrantColor(ContextCompat.getColor(MovieActivity.this, R.color.accent)),
                                palette.getLightMutedColor(ContextCompat.getColor(MovieActivity.this, R.color.accent))};
                        int[][] states = new int[2][];
                        states[0] = new int[]{};
                        states[1] = new int[]{android.R.attr.state_pressed};
                        fab_play.setBackgroundTintList(new ColorStateList(states, colors));
                    }
                }))
                .apply(RequestOptions.centerCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_backdrop);
        Glide.with(MovieActivity.this)
                .load(poster_path)
                .apply(RequestOptions.centerCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(iv_movie_poster);
        rb_movie_vote_average.setRating(vote_average / 2);
        tv_movie_vote_count.setText(getString(R.string.vote_count, vote_count));

        StringBuilder metaDataStrBuilder = new StringBuilder(DateUtils.convertDateToStr(release_date, "yyyy"));
        if (runtime != null && runtime != 0) {
            metaDataStrBuilder.append(" • ");
            metaDataStrBuilder.append(getString(R.string.minutes, runtime));
        }
        if (!TextUtils.isEmpty(genres)) {
            metaDataStrBuilder.append(" • ");
            metaDataStrBuilder.append(genres);
        }
        if (!TextUtils.isEmpty(production_countries)) {
            metaDataStrBuilder.append(" • ");
            metaDataStrBuilder.append(production_countries);
        }
        tv_movie_meta.setText(metaDataStrBuilder);
        if (!TextUtils.isEmpty(overview) && overview.length() > tv_movie_overview.getText().length()) {
            tv_movie_overview.setText(overview);
        }
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
    protected void onDestroy() {
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        UMShareAPI.get(this).release();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_favourite: {
                mDisposables.add(mAppDatabase.favouriteDao()
                        .getByIdAndTypeAsync(mSubject.id, mSubject.type)
                        .firstOrError()
                        .subscribe(favourites -> {
                            if (favourites.isEmpty()) {
                                Favourite favourite = new Favourite();
                                favourite.subjectId = mSubject.id;
                                favourite.subjectType = mSubject.type;
                                favourite.createTime = new Date();
                                mAppDatabase.favouriteDao().insert(favourite);
                            } else {
                                mAppDatabase.favouriteDao().delete(favourites.get(0));
                            }
                        }));
            }
            break;
            case R.id.fab_play: {
                if (mVideoUrls != null) {
                    checkThenPlay(mVideoUrls.get(0));
                }
            }
            break;
            default:
                break;
        }
    }

    private void checkThenPlay(VideoUrl videoUrl) {
        if (mUserInfoSp.getBoolean("shared", false)) {
            startActivity(new Intent(MovieActivity.this, VideoPlayerActivity.class)
                    .putExtra("video_url", videoUrl)
                    .putExtra("title", mSubject.title + " " + videoUrl.name));
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.need_to_share_title)
                    .setMessage(R.string.need_to_share_message)
                    .setNegativeButton(R.string.share_next_time, (dialogInterface, i) -> dialogInterface.dismiss())
                    .setPositiveButton(R.string.share_now, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        new ShareFragment().show(getSupportFragmentManager(), "ShareFragment");
                    })
                    .create()
                    .show();
        }
    }
}
