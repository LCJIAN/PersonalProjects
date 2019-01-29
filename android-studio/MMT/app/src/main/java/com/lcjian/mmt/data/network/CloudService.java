package com.lcjian.mmt.data.network;

import com.lcjian.mmt.data.network.entity.BankCard;
import com.lcjian.mmt.data.network.entity.Car;
import com.lcjian.mmt.data.network.entity.Deposit;
import com.lcjian.mmt.data.network.entity.Driver;
import com.lcjian.mmt.data.network.entity.Quote;
import com.lcjian.mmt.data.network.entity.Record;
import com.lcjian.mmt.data.network.entity.ResponseData;
import com.lcjian.mmt.data.network.entity.ResponsePageData;
import com.lcjian.mmt.data.network.entity.SignInInfo;
import com.lcjian.mmt.data.network.entity.TranOrder;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

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
    Single<ResponsePageData<Quote>> getQuotes(@Field("offset") Integer offset,
                                              @Field("limit") Integer limit,
                                              @Field("status") Integer status);

    /**
     * 物流商运输总订单列表
     */
    @FormUrlEncoded
    @POST("mmt/transorder/transorderbyorder")
    Single<ResponsePageData<TranOrder>> getTranOrders(@Field("offset") Integer offset,
                                                      @Field("limit") Integer limit);

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

    // mock start ---------------------------

    /**
     * 收支记录
     */
    @FormUrlEncoded
    @POST("mmt/carscontacts/driverlist")
    Single<ResponsePageData<Record>> getRecords(@Field("offset") Integer offset,
                                                @Field("limit") Integer limit);

}
