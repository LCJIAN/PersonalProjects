package com.org.firefighting.ui.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GuideActivity extends BaseActivity {

    @BindView(R.id.cl_container)
    ConstraintLayout cl_container;
    @BindView(R.id.vp_guide)
    ViewPager vp_guide;
    @BindView(R.id.btn_skip_guide)
    Button btn_skip_guide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);

        vp_guide.setAdapter(new GuideAdapter());
        vp_guide.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                TransitionManager.beginDelayedTransition(cl_container);
                btn_skip_guide.setVisibility(position == 3 ? View.GONE : View.VISIBLE);
            }
        });
        btn_skip_guide.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), SignInActivity.class));
            finish();
        });
    }

    static class GuideAdapter extends PagerAdapter {

        private List<View> mRecycledViews;

        GuideAdapter() {
            this.mRecycledViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return 4;
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
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                view = mRecycledViews.get(0);
                mRecycledViews.remove(0);
            }
            ((ImageView) view).setImageResource(container.getResources().getIdentifier(
                    "guide_" + (position + 1),
                    "drawable",
                    container.getContext().getPackageName()));
            view.setOnClickListener(v -> {
                v.getContext().startActivity(new Intent(v.getContext(), SignInActivity.class));
                ((Activity) v.getContext()).finish();
            });
            view.setEnabled(position == 3);
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
