package com.org.firefighting.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ApplyFragment extends BaseDialogFragment {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_des)
    TextView tv_des;
    @BindView(R.id.et_apply_reason)
    EditText et_apply_reason;
    @BindView(R.id.btn_close)
    ImageButton btn_close;
    @BindView(R.id.btn_submit)
    AppCompatButton btn_submit;

    private Unbinder mUnBinder;

    private Listener mListener;

    private boolean mService;

    private String mDes;

    public static ApplyFragment newInstance(String des) {
        ApplyFragment fragment = new ApplyFragment();
        Bundle args = new Bundle();
        args.putString("des", des);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDes = getArguments().getString("des");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_title.setText(mService ? R.string.service_application : R.string.resource_application);
        tv_des.setText(mDes);
        btn_close.setOnClickListener(v -> dismiss());
        btn_submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(et_apply_reason.getEditableText())) {
                Toast.makeText(App.getInstance(), R.string.apply_resource_reason_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            dismiss();
            if (mListener != null) {
                mListener.onConfirm(et_apply_reason.getEditableText().toString(),
                        et_apply_reason.getEditableText().toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    public ApplyFragment setService(boolean service) {
        this.mService = service;
        return this;
    }

    public ApplyFragment setListener(Listener l) {
        this.mListener = l;
        return this;
    }

    public interface Listener {
        void onConfirm(String name, String reason);
    }
}
