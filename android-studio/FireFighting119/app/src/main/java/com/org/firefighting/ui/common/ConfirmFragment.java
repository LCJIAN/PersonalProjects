package com.org.firefighting.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.org.firefighting.R;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ConfirmFragment extends BaseDialogFragment implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_cancel)
    TextView tv_cancel;
    @BindView(R.id.tv_confirm)
    TextView tv_confirm;
    private Unbinder unbinder;

    private Listener mListener;
    private TitleProvider mTitleProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);

        if (mTitleProvider != null) {
            tv_title.setText(mTitleProvider.title());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public ConfirmFragment setListener(Listener l) {
        this.mListener = l;
        return this;
    }

    public ConfirmFragment setTitleProvider(TitleProvider p) {
        this.mTitleProvider = p;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_cancel) {
            dismiss();
            if (mListener != null) {
                mListener.onCancel();
            }
        } else if (v.getId() == R.id.tv_confirm) {
            dismiss();
            if (mListener != null) {
                mListener.onConfirm();
            }
        } else {
            dismiss();
        }
    }

    public interface Listener {

        void onCancel();

        void onConfirm();

    }

    public interface TitleProvider {
        CharSequence title();
    }

    public static class SimpleListener implements Listener {

        public void onCancel() {
        }

        public void onConfirm() {
        }

    }
}
