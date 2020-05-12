package com.org.firefighting.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseFragment;
import com.org.firefighting.ui.chat.DepartmentsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ConversationsFragment extends BaseFragment {

    @BindView(R.id.btn_go_contact)
    ImageButton btn_go_contact;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversations, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_go_contact.setOnClickListener(v -> {
            startActivity(new Intent(v.getContext(), DepartmentsActivity.class));
        });
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
