package com.lcjian.mmt.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.mmt.App;
import com.lcjian.mmt.R;
import com.lcjian.mmt.data.network.entity.PictureRequestData;
import com.lcjian.mmt.data.network.entity.Pictures;
import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.ui.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class GalleryFragment extends BaseDialogFragment {

    @BindView(R.id.vp_gallery)
    ViewPager vp_gallery;
    @BindView(R.id.btn_pre_image)
    ImageButton btn_pre_image;
    @BindView(R.id.btn_next_image)
    ImageButton btn_next_image;
    @BindView(R.id.btn_close)
    ImageButton btn_close;
    Unbinder unbinder;

    private Long mId;

    private Disposable mDisposable;

    public static GalleryFragment newInstance(Long id) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getLong("id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_pre_image.setOnClickListener(v -> {
            PagerAdapter adapter = vp_gallery.getAdapter();
            if (adapter == null) {
                return;
            }
            int i = vp_gallery.getCurrentItem() - 1;
            if (i < 0) {
                Toast.makeText(App.getInstance(), R.string.no_pre_image, Toast.LENGTH_SHORT).show();
            } else {
                vp_gallery.setCurrentItem(i);
            }
        });
        btn_next_image.setOnClickListener(v -> {
            PagerAdapter adapter = vp_gallery.getAdapter();
            if (adapter == null) {
                return;
            }
            int i = vp_gallery.getCurrentItem() + 1;
            if (i >= adapter.getCount()) {
                Toast.makeText(App.getInstance(), R.string.no_next_image, Toast.LENGTH_SHORT).show();
            } else {
                vp_gallery.setCurrentItem(i);
            }
        });
        btn_close.setOnClickListener(v -> dismiss());

        PictureRequestData pictureRequestData = new PictureRequestData();
        pictureRequestData.id = mId;
        mDisposable = mRestAPI.cloudService().getPictures(pictureRequestData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(picturesResponseData -> vp_gallery.setAdapter(new PictureAdapter(picturesResponseData.result)),
                        throwable -> {
                            if (throwable instanceof HttpException) {
                                ResponseBody errorBody = ((HttpException) throwable).response().errorBody();
                                if (errorBody != null) {
                                    ResponseData<Object> r = new Gson().fromJson(errorBody.charStream(), new TypeToken<ResponseData<Object>>() {
                                    }.getType());
                                    Toast.makeText(App.getInstance(), r.error.details, Toast.LENGTH_SHORT).show();
                                    errorBody.close();
                                }
                            } else {
                                Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposable.dispose();
        unbinder.unbind();
    }

    static class PictureAdapter extends PagerAdapter {

        private Pictures mData;

        private List<View> mRecycledViews;

        PictureAdapter(Pictures data) {
            this.mData = data;
            this.mRecycledViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return mData == null || mData.list == null ? 0 : mData.list.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view;
            if (mRecycledViews.isEmpty()) {
                view = new ImageView(container.getContext());
                ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                view = mRecycledViews.get(0);
                mRecycledViews.remove(0);
            }

            Context context = view.getContext();
            Pictures.Picture picture = mData.list.get(position);
            Glide.with(context)
                    .load(picture.rel)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into((ImageView) view);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
            mRecycledViews.add((View) object);
        }
    }
}
