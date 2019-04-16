package com.lcjian.drinkwater.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.lcjian.drinkwater.R;
import com.lcjian.drinkwater.ui.base.BaseDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class WellDoneFragment extends BaseDialogFragment {

    @BindView(R.id.animation_view)
    LottieAnimationView animation_view;
    @BindView(R.id.btn_got_it)
    Button btn_got_it;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_well_done, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_got_it.setOnClickListener(v -> dismiss());
        animation_view.post(() -> animation_view.playAnimation());
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
