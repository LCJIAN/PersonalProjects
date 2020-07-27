package com.org.firefighting.ui.common;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;
import com.org.firefighting.R;

import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ViewPager2 vp_image = findViewById(R.id.vp_image);
        vp_image.setAdapter(new ImageViewerAdapter(this, getIntent().getStringArrayListExtra("uris")));
    }

    private static class ImageViewerAdapter extends FragmentStateAdapter {

        private List<String> mUris;

        private ImageViewerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> uris) {
            super(fragmentActivity);
            this.mUris = uris;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ImageViewerFragment.newInstance(mUris.get(position));
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }
    }

    public static class ImageViewerFragment extends Fragment {

        private BigImageView biv_image;

        private String mUri;

        public static ImageViewerFragment newInstance(String uri) {
            ImageViewerFragment fragment = new ImageViewerFragment();
            Bundle args = new Bundle();
            args.putString("uri", uri);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mUri = getArguments().getString("uri");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_image_viewer, container, false);
            biv_image = view.findViewById(R.id.biv_image);
            return view;
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            biv_image.setImageViewFactory(new GlideImageViewFactory());
            biv_image.showImage(Uri.parse(mUri));
            biv_image.setOnClickListener(v -> getActivity().onBackPressed());
        }

        @Override
        public void onDestroyView() {
            biv_image = null;
            super.onDestroyView();
        }
    }
}
