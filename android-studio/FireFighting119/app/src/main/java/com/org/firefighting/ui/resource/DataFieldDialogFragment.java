package com.org.firefighting.ui.resource;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DataFieldDialogFragment extends BaseDialogFragment {

    @BindView(R.id.btn_close)
    ImageButton btn_close;

    private Unbinder mUnBinder;

    private ResourceEntity mResourceEntity;

    public static DataFieldDialogFragment newInstance(ResourceEntity entity) {
        DataFieldDialogFragment fragment = new DataFieldDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("resource_entity", entity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResourceEntity = (ResourceEntity) getArguments().getSerializable("resource_entity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_field_dialog, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_close.setOnClickListener(v -> dismiss());

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, DataFieldFragment.newInstance(mResourceEntity), "DataFieldFragment")
                .commit();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
