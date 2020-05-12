package com.winside.lighting.ui.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winside.lighting.R;
import com.winside.lighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SwitchServerFragment extends BaseDialogFragment implements View.OnClickListener {

    @BindView(R.id.et_server_address)
    EditText et_server_address;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;
    private Unbinder unbinder;

    private OnServerAddressChangeListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_switch_server, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public SwitchServerFragment setOnServerAddressChangeListener(OnServerAddressChangeListener l) {
        this.mListener = l;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            String address = et_server_address.getEditableText().toString();
            dismiss();
            if (mListener != null && !TextUtils.isEmpty(address)) {
                mListener.onServerAddressChange(address);
            }
        } else {
            dismiss();
        }
    }

    public interface OnServerAddressChangeListener {
        void onServerAddressChange(String serverAddress);
    }
}
