package com.winside.lighting.ui.device;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class InputWifiInfoFragment extends BaseDialogFragment {

    @BindView(R.id.et_wifi_ssid)
    EditText et_wifi_ssid;
    @BindView(R.id.et_wifi_pwd)
    EditText et_wifi_pwd;
    @BindView(R.id.btn_test)
    Button btn_test;

    private Unbinder unbinder;

    private OnSelectedListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_wifi_info, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btn_test.setOnClickListener(v -> {
            dismiss();
            if (mListener != null
                    && !TextUtils.isEmpty(et_wifi_ssid.getEditableText().toString())
                    && !TextUtils.isEmpty(et_wifi_pwd.getEditableText().toString())) {
                mListener.onSelected(et_wifi_ssid.getEditableText().toString(), et_wifi_pwd.getEditableText().toString());
            }
        });
    }

    InputWifiInfoFragment setListener(OnSelectedListener listener) {
        this.mListener = listener;
        return this;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnSelectedListener {

        void onSelected(String wifiSSID, String wifiPwd);
    }
}


