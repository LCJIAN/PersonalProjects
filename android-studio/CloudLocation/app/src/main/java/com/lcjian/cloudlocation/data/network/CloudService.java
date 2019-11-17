package com.lcjian.cloudlocation.data.network;

import com.lcjian.cloudlocation.data.network.entity.Address;
import com.lcjian.cloudlocation.data.network.entity.Commands;
import com.lcjian.cloudlocation.data.network.entity.DeviceDetail;
import com.lcjian.cloudlocation.data.network.entity.Devices;
import com.lcjian.cloudlocation.data.network.entity.GEOFences;
import com.lcjian.cloudlocation.data.network.entity.Messages;
import com.lcjian.cloudlocation.data.network.entity.MonitorInfo;
import com.lcjian.cloudlocation.data.network.entity.Route;
import com.lcjian.cloudlocation.data.network.entity.SignInInfo;
import com.lcjian.cloudlocation.data.network.entity.State;
import com.lcjian.cloudlocation.data.network.entity.SubAccounts;
import com.lcjian.cloudlocation.data.network.entity.UserProfile;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CloudService {

    /**
     * 登陆
     *
     * @param name      用户名或(IMEI号/车牌号
     * @param pass      用户名密码或设备密码
     * @param loginType 值为0: 用户名登陆,1:IMEI号或车牌号登陆
     */
    @GET("Login")
    Single<SignInInfo> signIn(@Query("Name") String name,
                              @Query("Pass") String pass,
                              @Query("LoginType") Integer loginType);

    /**
     * 获取用户下设备列表
     *
     * @param id 根据typeId不同而表示不同.(typeId=0:是UserID,1:是DeviceID);
     *           如果typeId=1,通过DeviceID查询这个方法,参数pageNumber和参数pageSize无实际意义,可随便传个数字
     */
    @GET("GetDeviceList")
    Single<Devices> getDevices(@Query("ID") Long id,
                               @Query("LoginName") String loginName,
                               @Query("Password") String password,
                               @Query("TypeID") Integer typeId,
                               @Query("IsAll") Boolean isAll,
                               @Query("PageNo") Integer pageNumber,
                               @Query("PageCount") Integer pageSize);

    /**
     * 多车监控
     */
    @GET("GetMonitorByUserID")
    Single<MonitorInfo> monitorDevices(@Query("UserID") Long userId,
                                       @Query("MapType") String mapType,
                                       @Query("LoginName") String loginName,
                                       @Query("Password") String password);

    /**
     * 实时跟踪
     */
    @GET("GetTracking")
    Single<MonitorInfo.MonitorDevice> getTrack(@Query("DeviceID") Long deviceId,
                                               @Query("Model") String model,
                                               @Query("MapType") String mapType);

    /**
     * 获取地址信息
     */
    @GET("GetAddressByLatlng")
    Single<Address> getAddressByLatLng(@Query("Lat") String Lat,
                                       @Query("Lng") String lng,
                                       @Query("MapType") String mapType);

    /**
     * 历史轨迹
     *
     * @param showLBS 0不需要,1需要
     */
    @GET("GetDevicesHistory")
    Single<Route> getDeviceRoute(@Query("DeviceID") Long deviceId,
                                 @Query("StartTime") String startTime,
                                 @Query("EndTime") String endTime,
                                 @Query("ShowLBS") int showLBS,
                                 @Query("SelectCount") int count,
                                 @Query("MapType") String mapType);

    /**
     * 获取当前用户和子用户信息列表
     */
    @GET("GetUserDevices")
    Single<SubAccounts> getUserSubAccounts(@Query("UserID") Long userId,
                                           @Query("LoginName") String loginName,
                                           @Query("Password") String password);

    /**
     * 获取用户信息
     */
    @GET("GetUserInfo")
    Single<UserProfile> getUserProfile(@Query("UserID") Long userId);

    /**
     * ID登录密码修改
     */
    @GET("UpdateDevicePass")
    Single<State> updateDevicePwd(@Query("DeviceID") Long deviceId,
                                  @Query("OldPass") String oldPwd,
                                  @Query("NewPass") String newPwd);

    /**
     * 用户登录密码修改
     */
    @GET("UpdateUserPass")
    Single<State> updateUserPwd(@Query("UserID") Long userId,
                                @Query("OldPass") String oldPwd,
                                @Query("NewPass") String newPwd);

    /**
     * 修改用户信息
     */
    @GET("UpdateUserInfo")
    Single<State> updateUserProfile(@Query("UserID") Long id,
                                    @Query("userName") String name,
                                    @Query("contact") String contact,
                                    @Query("cellPhone") String phone,
                                    @Query("primaryEmail") String email,
                                    @Query("address1") String address);

    /**
     * 修改设备信息
     */
    @GET("UpdateDevice")
    Single<State> updateDevice(@Query("DeviceID") Long deviceId,
                               @Query("DeviceName") String name,
                               @Query("CarNum") String carNO,
                               @Query("PhoneNumbe") String carPhone,
                               @Query("CarUserName") String carUserName,
                               @Query("CellPhone") String cellPhone);

    /**
     * 获取单个设备详细信息
     */
    @GET("GetDeviceDetail")
    Single<DeviceDetail> getDeviceDetail(@Query("DeviceID") Long deviceId);

    /**
     * 下发记录列表
     *
     * @param sn 设备IMEI号
     */
    @GET("GetCommandList")
    Single<Commands> getCommandHistory(@Query("SN") String sn,
                                       @Query("DeviceID") Long deviceId,
                                       @Query("PageNo") Integer pageNumber,
                                       @Query("PageCount") Integer pageSize);

    /**
     * 获取电子栅栏
     */
    @GET("GetGeofence")
    Single<GEOFences> getGEOFence(@Query("DeviceID") Long deviceId,
                                  @Query("MapType") String mapType);

    /**
     * 新增或修改电子栅栏
     */
    @GET("SaveGeofence")
    Single<State> saveGEOFence(@Query("UserID") Long userId,
                               @Query("DeviceID") Long deviceId,
                               @Query("GeofenceID") Long id,
                               @Query("GeofenceName") String name,
                               @Query("GeoFenceType") Integer type,
                               @Query("Lat") Double lat,
                               @Query("Lng") Double lng,
                               @Query("Radius") Integer radius,
                               @Query("Entry") Integer entry,
                               @Query("Exit") Integer exit,
                               @Query("Remark") String remark,
                               @Query("MapType") String mapType);

    /**
     * 删除电子栅栏
     */
    @GET("DelGeofence")
    Single<State> deleteGEOFence(@Query("DeviceID") Long deviceId,
                                 @Query("GeofenceID") Long id);

    /**
     * 获取用户或单个设备的报警列表
     *
     * @param id 根据typeId不同而表示不同.(typeId=0:是UserID,1:是DeviceID);
     */
    @GET("GetWarnList")
    Single<Messages> getMessages(@Query("ID") Long id,
                                 @Query("TypeID") Integer typeId,
                                 @Query("PageNo") Integer pageNumber,
                                 @Query("PageCount") Integer pageSize);

    /**
     * 删除消息
     */
    @GET("ClearExceptionMessageByID")
    Single<State> deleteMessage(@Query("ID") Long id,
                                @Query("TypeID") Integer typeId,
                                @Query("ExceptionID") String messageId);
}
