package com.org.firefighting.ui.service;

import android.os.Bundle;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.lcjian.lib.text.Spans;
import com.lcjian.lib.util.common.DimenUtils;
import com.org.firefighting.R;
import com.org.firefighting.RxBus;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ApplyStatusFragment extends BaseDialogFragment {

    @BindView(R.id.tv_info)
    TextView tv_info;
    @BindView(R.id.iv_bg)
    ImageView iv_bg;
    @BindView(R.id.btn_apply)
    AppCompatButton btn_apply;
    @BindView(R.id.btn_close)
    ImageButton btn_close;

    private Unbinder mUnBinder;

    private ServiceEntity mServiceEntity;

    public static ApplyStatusFragment newInstance(ServiceEntity entity) {
        ApplyStatusFragment fragment = new ApplyStatusFragment();
        Bundle args = new Bundle();
        args.putSerializable("service_entity", entity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mServiceEntity = (ServiceEntity) getArguments().getSerializable("service_entity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply_status, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (1 == mServiceEntity.applyStatus) {
            iv_bg.setImageResource(R.drawable.apply_dialog_bg);
            tv_info.setText(new Spans().append("已申请，请等待审核", new ForegroundColorSpan(0xff383f52)));
            btn_apply.setVisibility(View.GONE);
        } else if (2 == mServiceEntity.applyStatus) {
            iv_bg.setImageResource(R.drawable.apply_dialog_bg);
            tv_info.setText(new Spans()
                    .append("审核已通过", new ForegroundColorSpan(0xff383f52))
                    .append("\n\n")
                    .append("申请时间：" + mServiceEntity.applyTime,
                            new ForegroundColorSpan(0xff383f52),
                            new AbsoluteSizeSpan(DimenUtils.spToPixels(12, view.getContext()))));
            btn_apply.setVisibility(View.GONE);
        } else if (3 == mServiceEntity.applyStatus) {
            iv_bg.setImageResource(R.drawable.apply_dialog_bg_2);
            tv_info.setText(new Spans()
                    .append("审核不通过", new ForegroundColorSpan(0xffa60303))
                    .append("\n")
                    .append(mServiceEntity.auditRemarks,
                            new ForegroundColorSpan(0xff383f52),
                            new AbsoluteSizeSpan(DimenUtils.spToPixels(10, view.getContext())))
                    .append("\n")
                    .append("申请时间：" + mServiceEntity.applyTime,
                            new ForegroundColorSpan(0xff383f52),
                            new AbsoluteSizeSpan(DimenUtils.spToPixels(12, view.getContext()))));
            btn_apply.setVisibility(View.VISIBLE);
            btn_apply.setText("重新申请");
        } else {
            iv_bg.setImageResource(R.drawable.apply_dialog_bg_2);
            tv_info.setText(new Spans().append("暂无使用权限，请申请后使用\n\n你可以", new ForegroundColorSpan(0xff383f52)));
            btn_apply.setVisibility(View.VISIBLE);
            btn_apply.setText("立即申请");
        }

        btn_close.setOnClickListener(v -> dismiss());
        btn_apply.setOnClickListener(v -> {
            dismiss();
            RxBus.getInstance().send(new ServiceDetailActivity.ShowApplyDialogEvent());
        });
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
