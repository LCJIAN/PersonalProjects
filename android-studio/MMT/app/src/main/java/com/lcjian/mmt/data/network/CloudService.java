package com.lcjian.mmt.data.network;

import com.lcjian.mmt.data.network.entity.BankCard;
import com.lcjian.mmt.data.network.entity.Brokerage;
import com.lcjian.mmt.data.network.entity.Car;
import com.lcjian.mmt.data.network.entity.CarOrder;
import com.lcjian.mmt.data.network.entity.CarPrepare;
import com.lcjian.mmt.data.network.entity.Deposit;
import com.lcjian.mmt.data.network.entity.Dict;
import com.lcjian.mmt.data.network.entity.Driver;
import com.lcjian.mmt.data.network.entity.DriverPrepare;
import com.lcjian.mmt.data.network.entity.Invoice;
import com.lcjian.mmt.data.network.entity.Message;
import com.lcjian.mmt.data.network.entity.ProductType;
import com.lcjian.mmt.data.network.entity.QuotePrepare;
import com.lcjian.mmt.data.network.entity.Record;
import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.data.network.entity.ResponsePageData;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.data.network.entity.TransOrder;
import com.lcjian.mmt.data.network.entity.TransRequest;
import com.lcjian.mmt.data.network.entity.WeChatPayOrder;

import java.util.List;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface CloudService {

    /**
     * 获取短信验证码
     */
    @FormUrlEncoded
    @POST("mmt/user/getsmscode")
    Single<ResponseData<String>> sendVerificationCode(@Field("mobile") String phone);

    /**
     * 校验短信验证码有效
     */
    @FormUrlEncoded
    @POST("mmt/user/checksmscode")
    Single<ResponseData<String>> checkVerificationCode(@Field("mobile") String phone,
                                                       @Field("smscode") String verificationCode);

    /**
     * 用户注册
     */
    @FormUrlEncoded
    @POST("mmt/user/mobileregsubmit")
    Single<ResponseData<String>> signUp(@Field("mobile") String phone,
                                        @Field("smscode") String verificationCode,
                                        @Field("password") String password);

    /**
     * 登陆
     */
    @FormUrlEncoded
    @POST("mmt/login/mobile")
    Single<ResponseData<SignInInfo>> signIn(@Field("username") String phone,
                                            @Field("password") String password);

    /**
     * 登陆
     */
    @FormUrlEncoded
    @POST("mmt/login/mobile")
    Single<ResponseData<SignInInfo>> signIn(@Field("token") String token);

    /**
     * 用户忘记密码
     */
    @FormUrlEncoded
    @POST("mmt/user/forgetpassword")
    Single<ResponseData<String>> setPassword(@Field("mobile") String phone,
                                             @Field("smscode") String verificationCode,
                                             @Field("password") String password);

    /**
     * 查询余额账户、保证金账户(app)
     */
    @POST("mmt/deposit/deposit")
    Single<Deposit> getDeposit();

    /**
     * 物流商获取待报价的报价信息
     */
    @FormUrlEncoded
    @POST("mmt/offer/offerlist")
    Single<ResponsePageData<TransRequest>> getTransRequests(@Field("offset") Integer offset,
                                                            @Field("limit") Integer limit,
                                                            @Field("status") Integer status);

    /**
     * 物流商放弃报价
     */
    @POST("mmt/offer/refuseQuoted/{id}")
    Single<ResponseData<String>> quoteNotTransRequest(@Path("id") String requestId);

    /**
     * 删除
     */
    @POST("mmt/offer/delete/{id}")
    Single<ResponseData<String>> deleteTransRequest(@Path("id") String requestId);

    /**
     * 查看
     */
    @POST("mmt/offer/view/{id}")
    Single<ResponseData<TransRequest>> getTransRequest(@Path("id") String requestId);

    /**
     * 物流参与报时获取车辆的信息
     */
    @FormUrlEncoded
    @POST("mmt/offer/involvedquoted")
    Single<ResponseData<QuotePrepare>> getQuotePrepareInfo(@Field("id") String requestId);

    /**
     * 物流商报价前判断商户合法性
     */
    @FormUrlEncoded
    @POST("mmt/offer/valideQuoted")
    Single<ResponseData<String>> validateQuote(@Field("id") String requestId);

    /**
     * 物流商参与报价-保存
     */
    @POST("mmt/offer/saveQuoted")
    Single<ResponseData<String>> submitQuote(@Body QuotePrepare quotePrepare);

    /**
     * 物流商运输总订单列表
     */
    @FormUrlEncoded
    @POST("mmt/transorder/transorderbyorder")
    Single<ResponsePageData<TransOrder>> getTransOrders(@Field("offset") Integer offset,
                                                        @Field("limit") Integer limit);

    /**
     * 查看总订单详情
     */
    @POST("mmt/transorder/transorderview/{id}")
    Single<ResponseData<TransOrder>> getTransOrderDetail(@Path("id") String id);

    /**
     * 分车运单
     */
    @POST("mmt/transorder/transorderbylogistics/{id}")
    Single<ResponsePageData<CarOrder>> getCarOrders(@Path("id") String id);

    /**
     * 查看分车运单详情
     */
    @POST("mmt/transorder/carorderview/{id}")
    Single<ResponseData<CarOrder>> getCarOrderDetail(@Path("id") String id);

    /**
     * 车辆管理列表
     *
     * @param search 车牌号
     */
    @FormUrlEncoded
    @POST("mmt/cars/list")
    Single<ResponsePageData<Car>> getCars(@Field("offset") Integer offset,
                                          @Field("limit") Integer limit,
                                          @Field("search") String search);

    /**
     * 车辆管理列表
     *
     * @param search 司机名称或手机号
     */
    @FormUrlEncoded
    @POST("mmt/carscontacts/driverlist")
    Single<ResponsePageData<Driver>> getDrivers(@Field("offset") Integer offset,
                                                @Field("limit") Integer limit,
                                                @Field("search") String search);

    /**
     * 银行卡列表
     */
    @FormUrlEncoded
    @POST("mmt/deposit/cardlist")
    Single<ResponsePageData<BankCard>> getBankCards(@Field("offset") Integer offset,
                                                    @Field("limit") Integer limit);

    /**
     * 添加银行卡
     */
    @FormUrlEncoded
    @POST("mmt/deposit/addcard")
    Single<ResponseData<BankCard>> addBankCard(@Field("cardNo") String cardNo,
                                               @Field("ownerName") String ownerName);

    /**
     * 删除银行卡
     */
    @POST("mmt/deposit/deletecard/{ids}")
    Single<ResponseData<String>> deleteBankCards(@Path("ids") String ids);

    /**
     * 账户余额提现记录
     */
    @FormUrlEncoded
    @POST("mmt/deposit/withdrawList")
    Single<ResponsePageData<Record>> getWithdrawRecords(@Field("offset") Integer offset,
                                                        @Field("limit") Integer limit);

    /**
     * 账户余额充值记录
     */
    @FormUrlEncoded
    @POST("mmt/deposit/rechargeList")
    Single<ResponsePageData<Record>> getRechargeRecords(@Field("offset") Integer offset,
                                                        @Field("limit") Integer limit);

    /**
     * 账户余额收支记录
     */
    @FormUrlEncoded
    @POST("mmt/deposit/balanceRecordList")
    Single<ResponsePageData<Record>> getInOutRecords(@Field("offset") Integer offset,
                                                     @Field("limit") Integer limit);

    /**
     * 保证金充值列表记录
     */
    @FormUrlEncoded
    @POST("mmt/deposit/payList")
    Single<ResponsePageData<Record>> getBondRechargeRecords(@Field("offset") Integer offset,
                                                            @Field("limit") Integer limit);

    /**
     * 添加给平台的建议和意见
     */
    @FormUrlEncoded
    @POST("mmt/orderComplain/createComplain")
    Single<ResponseData<Object>> addFeedback(@Field("compReason") String content);

    /**
     * 缴纳保证金（充值）
     */
    @FormUrlEncoded
    @POST("mmt/deposit/payDeposit")
    Single<ResponseData<String>> rechargeBond(@Field("amount") Integer amount);

    /**
     * 账户余额提现到银行卡
     */
    @FormUrlEncoded
    @POST("mmt/pay/confirmDrawings")
    Single<ResponseData<String>> withdraw(@Field("cardId") String bankCardId,
                                          @Field("amount") Integer amount);

    /**
     * 微信支付
     */
    @FormUrlEncoded
    @POST("mmt/wiki/wxpay")
    Single<ResponseData<WeChatPayOrder>> createWeChatPayOrder(@Field("amount") Double amount,
                                                              @Field("description") String description,
                                                              @Field("spbillIp") String spbillIp,
                                                              @Field("type") String type);

    /**
     * 销售方、物流方查看佣金发票列表
     */
    @FormUrlEncoded
    @POST("mmt/invoice/salerbrokeinvoicelist")
    Single<ResponsePageData<Invoice>> getCommissionInvoices(@Field("status") Integer status,
                                                            @Field("offset") Integer offset,
                                                            @Field("limit") Integer limit);

    /**
     * 消息列表
     *
     * @param state 状态 1：未读。2：已读，默认空
     */
    @FormUrlEncoded
    @POST("mmt/msg/list")
    Single<ResponsePageData<Message>> getMessages(@Field("state") Integer state,
                                                  @Field("offset") Integer offset,
                                                  @Field("limit") Integer limit);

    /**
     * 查看通知消息内容
     */
    @POST("mmt/msg/view/{id}")
    Single<ResponseData<Message>> viewMessage(@Path("id") String id);

    /**
     * 删除通知消息
     */
    @FormUrlEncoded
    @POST("mmt/msg/delete")
    Single<ResponseData<String>> deleteMessages(@Field("ids") String ids);

    /**
     * 佣金扣除记录列表
     */
    @FormUrlEncoded
    @POST("mmt/brokerage/recordlist")
    Single<ResponsePageData<Brokerage>> getBrokerage(@Field("offset") Integer offset,
                                                     @Field("limit") Integer limit);

    /**
     * 图片上传
     */
    @Multipart
    @POST("api/upload")
    Single<ResponseData<String>> uploadImage(@Part MultipartBody.Part image);

    /**
     * 证件上传
     */
    @Multipart
    @POST("api/uploadCer")
    Single<ResponseData<String>> uploadCer(@Part MultipartBody.Part image);

    /**
     * 上传过磅单
     */
    @FormUrlEncoded
    @POST("mmt/transpound/uploadTransPoundApp/{transOrderId}")
    Single<ResponseData<String>> uploadTransPound(@Path("transOrderId") String transOrderId,
                                                  @Field("transOrderId") String transOrderId2,
                                                  @Field("time") Long time,
                                                  @Field("type") Integer type,
                                                  @Field("netWeight") Integer netWeight,
                                                  @Field("totalWeight") Integer totalWeight,
                                                  @Field("url") String url);

    /**
     * 司机添加
     */
    @POST("mmt/carscontacts/drivercreate")
    Single<ResponseData<String>> addDriver(@Body DriverPrepare driverPrepare);

    /**
     * 司机删除
     */
    @POST("mmt/carscontacts/driverdelete/{id}")
    Single<ResponseData<String>> deleteDriver(@Path("id") String id);

    /**
     * 删除车辆
     */
    @FormUrlEncoded
    @POST("mmt/cars/delete")
    Single<ResponseData<String>> deleteCars(@Field("ids") String ids);

    /**
     * 获取相关字典
     */
    @FormUrlEncoded
    @POST("mmt/index/api/dict")
    Single<List<Dict>> dict(@Field("dicttype") String dictType);

    /**
     * 车辆添加时获取司机
     */
    @POST("mmt/carscontacts/getdrivers")
    Single<ResponseData<List<Driver>>> getDrivers();

    /**
     * 车辆添加
     */
    @POST("mmt/cars/create")
    Single<ResponseData<String>> addCar(@Body CarPrepare carPrepare);

    /**
     * 获取商品的分类信息
     */
    @FormUrlEncoded
    @POST("mmt/index/producttypes")
    Single<List<ProductType>> getProductTypes(@Field("id") String id);

    /**
     * 出车前修改司机
     */
    @FormUrlEncoded
    @POST("mmt/transorder/changeDriver/{transOrderId}")
    Single<ResponseData<String>> changeCarOrderDriver(@Path("transOrderId") String transOrderId1,
                                                      @Field("transOrderId") String transOrderId2,
                                                      @Field("driverId") String driverId);

    /**
     * 出车前修改到达时间
     */
    @FormUrlEncoded
    @POST("mmt/transorder/updateArrivalTime/{transOrderId}")
    Single<ResponseData<String>> changeCarOrderArrivalTime(@Path("transOrderId") String transOrderId1,
                                                           @Field("transOrderId") String transOrderId2,
                                                           @Field("arrivalTime") Long arrivalTime,
                                                           @Field("unloadTime") Long unloadTime);

    /**
     * 出车
     */
    @FormUrlEncoded
    @POST("mmt/transorder/updateStatus/{transOrderId}")
    Single<ResponseData<String>> setOutCarOrder(@Path("transOrderId") String transOrderId1,
                                                @Field("transOrderId") String transOrderId2,
                                                @Field("status") Integer status);

    /**
     * 车辆定位
     */
    @FormUrlEncoded
    @POST("mmt/index/api/location/{id}")
    Single<ResponseData<String>> uploadLocation(@Path("id") String userId,
                                                @Field("y") Double longitude,
                                                @Field("x") Double latitude);

}
