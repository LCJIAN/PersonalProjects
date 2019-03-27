package com.lcjian.mmt.data.network.entity;

public class WeChatPayOrder {
    public String return_code;
    public String return_msg;
    public String appid;
    public String mch_id;
    public String nonce_str;
    public String sign;
    public String result_code;
    public String prepay_id;
    public String trade_type;
    public String timestamp;
    public String package_value = "Sign=WXPay";
}
