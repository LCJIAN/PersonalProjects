package com.org.firefighting.ui.common;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.App;
import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ApplyFragment extends BaseDialogFragment {

    @BindView(R.id.et_apply_name)
    EditText et_apply_name;
    @BindView(R.id.et_apply_reason)
    EditText et_apply_reason;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;

    private Unbinder mUnBinder;

    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_cancel.setOnClickListener(v -> dismiss());
        tv_confirm.setOnClickListener(v -> {
            if (TextUtils.isEmpty(et_apply_name.getEditableText())) {
                Toast.makeText(App.getInstance(), R.string.apply_resource_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(et_apply_reason.getEditableText())) {
                Toast.makeText(App.getInstance(), R.string.apply_resource_reason_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            dismiss();
            if (mListener != null) {
                mListener.onConfirm(et_apply_name.getEditableText().toString(),
                        et_apply_reason.getEditableText().toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }

    public ApplyFragment setListener(Listener l) {
        this.mListener = l;
        return this;
    }

    public interface Listener {
        void onConfirm(String name, String reason);
    }
}
