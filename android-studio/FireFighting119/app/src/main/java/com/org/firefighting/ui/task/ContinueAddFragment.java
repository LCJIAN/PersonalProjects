package com.org.firefighting.ui.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.data.local.SharedPreferencesDataSource;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ContinueAddFragment extends BaseDialogFragment {

    @BindView(R.id.checkbox)
    CheckBox checkbox;
    @BindView(R.id.tv_not_continue)
    TextView tv_not_continue;
    @BindView(R.id.tv_continue)
    TextView tv_continue;

    private Unbinder mUnBinder;

    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_continue_add, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_not_continue.setOnClickListener(v -> {
            dismiss();
            SharedPreferencesDataSource.putContinueAdd(false);
            SharedPreferencesDataSource.putContinueAddRemember(checkbox.isChecked());
            if (mListener != null) {
                mListener.onConfirm();
            }
        });
        tv_continue.setOnClickListener(v -> {
            dismiss();
            SharedPreferencesDataSource.putContinueAdd(true);
            SharedPreferencesDataSource.putContinueAddRemember(checkbox.isChecked());
            if (mListener != null) {
                mListener.onConfirm();
            }
        });
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    public ContinueAddFragment setListener(Listener l) {
        this.mListener = l;
        return this;
    }

    public interface Listener {
        void onConfirm();
    }
}
