package com.lcjian.vastplayer.ui.download;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DownloadAddTaskFragment extends BaseDialogFragment implements View.OnClickListener {

    @BindView(R.id.til_download_url)
    TextInputLayout til_download_url;
    @BindView(R.id.et_download_url)
    EditText et_download_url;
    @BindView(R.id.btn_cancel)
    Button btn_cancel;
    @BindView(R.id.btn_ok)
    Button btn_ok;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downlad_add_task, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        til_download_url.setHint(getResources().getString(R.string.download_url));
        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        getDialog().setTitle(R.string.add_task);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel: {
                dismiss();
            }
            break;
            case R.id.btn_ok: {
                String url = et_download_url.getEditableText().toString();
                if (!TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"))) {
                    mRxBus.send(url);
                    dismiss();
                }
            }
            break;
            default:
                break;
        }
    }
}
