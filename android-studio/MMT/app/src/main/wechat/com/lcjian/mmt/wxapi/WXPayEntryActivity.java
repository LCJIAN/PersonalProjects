package com.lcjian.mmt.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lcjian.mmt.Constants;
import com.lcjian.mmt.R;
import com.lcjian.mmt.ui.base.BaseActivity;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler {

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.btn_nav_back)
    ImageButton btn_nav_back;
    @BindView(R.id.tv_pay_result)
    TextView tv_pay_result;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_result);
        ButterKnife.bind(this);

        btn_nav_back.setOnClickListener(v -> onBackPressed());
        tv_title.setText(R.string.pay_result);

        api = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                tv_pay_result.setText(R.string.pay_success);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                tv_pay_result.setText(R.string.user_canceled);
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                tv_pay_result.setText(R.string.auth_denied);
                break;
            default:
                tv_pay_result.setText(R.string.pay_failed);
                break;
        }
    }
}