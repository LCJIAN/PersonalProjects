package com.lcjian.mmt.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lcjian.mmt.GlideApp;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ThrowableConsumerAdapter;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.ui.base.BaseFragment;
import com.lcjian.mmt.ui.user.BankCardsActivity;
import com.lcjian.mmt.ui.user.BrokerageActivity;
import com.lcjian.mmt.ui.user.FeedbackActivity;
import com.lcjian.mmt.ui.user.InvoiceManageActivity;
import com.lcjian.mmt.ui.user.RechargeActivity;
import com.lcjian.mmt.ui.user.RecordsActivity;
import com.lcjian.mmt.ui.user.SettingsActivity;
import com.lcjian.mmt.ui.user.WithdrawalActivity;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UserCenterFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.iv_user_avatar)
    ImageView iv_user_avatar;
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.btn_go_settings)
    ImageButton btn_go_settings;
    @BindView(R.id.tv_balance)
    TextView tv_balance;
    @BindView(R.id.tv_account_type)
    TextView tv_account_type;
    @BindView(R.id.tv_bond_balance)
    TextView tv_bond_balance;
    @BindView(R.id.tv_my_bankcard)
    TextView tv_my_bankcard;
    @BindView(R.id.tv_recharge_bond)
    TextView tv_recharge_bond;
    @BindView(R.id.tv_withdraw)
    TextView tv_withdraw;
    @BindView(R.id.tv_recharge)
    TextView tv_recharge;
    @BindView(R.id.tv_commission_info)
    TextView tv_commission_info;
    @BindView(R.id.tv_invoice_manage)
    TextView tv_invoice_manage;
    @BindView(R.id.tv_feedback)
    TextView tv_feedback;
    @BindView(R.id.tv_record)
    TextView tv_record;

    Unbinder unbinder;

    private Disposable mDisposable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_center, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tv_record.setOnClickListener(this);
        tv_recharge.setOnClickListener(this);
        tv_recharge_bond.setOnClickListener(this);
        tv_withdraw.setOnClickListener(this);
        tv_feedback.setOnClickListener(this);
        tv_my_bankcard.setOnClickListener(this);
        tv_commission_info.setOnClickListener(this);
        tv_invoice_manage.setOnClickListener(this);
        btn_go_settings.setOnClickListener(this);

        SignInInfo signInInfo = getSignInInfo();
        GlideApp.with(this)
                .load(signInInfo.user.avatar)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(iv_user_avatar);
        tv_user_name.setText(signInInfo.user.realname);
        tv_account_type.setText(TextUtils.equals("1", signInInfo.user.userSort) ? R.string.personal_account : R.string.business_account);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDisposable = mRestAPI.cloudService().getDeposit()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deposit -> {
                            tv_balance.setText(new DecimalFormat("0.00").format(deposit.balance / 100));
                            tv_bond_balance.setText(new DecimalFormat("0.00").format(deposit.bondBalance / 100));
                        },
                        ThrowableConsumerAdapter::accept);
    }

    @Override
    public void onPause() {
        mDisposable.dispose();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_go_settings:
                startActivity(new Intent(v.getContext(), SettingsActivity.class));
                break;
            case R.id.tv_recharge:
                startActivity(new Intent(v.getContext(), RechargeActivity.class));
                break;
            case R.id.tv_recharge_bond:
                startActivity(new Intent(v.getContext(), RechargeActivity.class).putExtra("bond", true));
                break;
            case R.id.tv_record:
                startActivity(new Intent(v.getContext(), RecordsActivity.class));
                break;
            case R.id.tv_withdraw:
                startActivity(new Intent(v.getContext(), WithdrawalActivity.class));
                break;
            case R.id.tv_feedback:
                startActivity(new Intent(v.getContext(), FeedbackActivity.class));
                break;
            case R.id.tv_my_bankcard:
                startActivity(new Intent(v.getContext(), BankCardsActivity.class));
                break;
            case R.id.tv_commission_info:
                startActivity(new Intent(v.getContext(), BrokerageActivity.class));
                break;
            case R.id.tv_invoice_manage:
                startActivity(new Intent(v.getContext(), InvoiceManageActivity.class));
                break;
            default:
                break;
        }
    }
}
