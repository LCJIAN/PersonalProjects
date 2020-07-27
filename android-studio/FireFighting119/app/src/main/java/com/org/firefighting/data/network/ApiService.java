package com.org.firefighting.data.network;

import com.org.firefighting.data.network.entity.AskRequest;
import com.org.firefighting.data.network.entity.DataQueryResult;
import com.org.firefighting.data.network.entity.DataQueryResult2;
import com.org.firefighting.data.network.entity.Department;
import com.org.firefighting.data.network.entity.ModifyPwdRequest;
import com.org.firefighting.data.network.entity.News;
import com.org.firefighting.data.network.entity.NewsCategory;
import com.org.firefighting.data.network.entity.PageResponse;
import com.org.firefighting.data.network.entity.RelevanceTable;
import com.org.firefighting.data.network.entity.ResourceDataRequest;
import com.org.firefighting.data.network.entity.ResourceEntity;
import com.org.firefighting.data.network.entity.ResponseData;
import com.org.firefighting.data.network.entity.SearchRequest;
import com.org.firefighting.data.network.entity.SearchResult;
import com.org.firefighting.data.network.entity.ServiceDataRequest;
import com.org.firefighting.data.network.entity.ServiceDataRequestEmpty;
import com.org.firefighting.data.network.entity.ServiceEntity;
import com.org.firefighting.data.network.entity.SignInRequest;
import com.org.firefighting.data.network.entity.SignInResponse;
import com.org.firefighting.data.network.entity.SystemMessage;
import com.org.firefighting.data.network.entity.Task;
import com.org.firefighting.data.network.entity.TaskLog;
import com.org.firefighting.data.network.entity.TaskQuestion;
import com.org.firefighting.data.network.entity.TaskSummary2;
import com.org.firefighting.data.network.entity.TaskTable;
import com.org.firefighting.data.network.entity.User2;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * 用户鉴权接口
     */
    @POST("admin-ht/auth/login")
    Single<SignInResponse> signIn(@Body SignInRequest signInRequest);

    /**
     * 用户鉴权接口
     */
    @POST("admin-ht/auth/login")
    Call<SignInResponse> signInSync(@Body SignInRequest signInRequest);

    /**
     * 修改密码
     */
    @POST("admin-ht/api/users/updatePass")
    Single<ResponseBody> modifyPwd(@Body ModifyPwdRequest modifyPwdRequest);

    /**
     * 获取个人信息填报任务列表
     */
    @GET("ht/api/v2/xxtb/task/my?pager=1000000&page=0")
    Single<PageResponse<Task>> getMyTasks();

    /**
     * 获取某条信息填报任务详情
     */
    @GET("ht/api/v2/xxtb/task/{id}")
    Single<ResponseData<Task>> getTaskDetail(@Path("id") String id);

    /**
     * 获取某条信息填报任务详情
     */
    @GET("ht/api/v2/user/task/{id}/access")
    Single<ResponseData<Object>> checkTaskDetail(@Path("id") String id);

    /**
     * 获取某条信息填报任务对应的表单
     */
    @GET("ht/api/v2/xxtb/task/{id}/tables")
    Single<ResponseData<List<TaskTable>>> getTaskTables(@Path("id") String id);

    /**
     * 获取某条信息填报任务对应的表单的记录
     */
    @GET("ht/api/v2/xxtb/task/{task_id}/table/{table_id}/record")
    Single<PageResponse<List<String>>> getTaskTableRecords(@Path("task_id") String taskId,
                                                           @Path("table_id") String tableId);

    /**
     * 添加表单记录
     */
    @POST("ht/api/v2/xxtb/task/{task_id}/table/{table_id}/record")
    Single<ResponseData<Object>> addTaskTableRecord(@Path("task_id") String taskId,
                                                    @Path("table_id") String tableId,
                                                    @Body List<String> data);

    /**
     * 修改表单记录
     */
    @PUT("ht/api/v2/xxtb/task/{task_id}/table/{table_id}/record/{position}")
    Single<ResponseData<Object>> modifyTaskTableRecord(@Path("task_id") String taskId,
                                                       @Path("table_id") String tableId,
                                                       @Path("position") Integer position,
                                                       @Body List<String> data);

    /**
     * 删除表单记录
     */
    @DELETE("ht/api/v2/xxtb/task/{task_id}/table/{table_id}/record/{position}")
    Single<ResponseData<Object>> deleteTaskTableRecord(@Path("task_id") String taskId,
                                                       @Path("table_id") String tableId,
                                                       @Path("position") Integer position);

    /**
     * 清空表单记录
     */
    @DELETE("ht/api/v2/xxtb/task/{task_id}/table/{table_id}/wipe")
    Single<ResponseData<Object>> clearTaskTableRecords(@Path("task_id") String taskId,
                                                       @Path("table_id") String tableId);

    /**
     * 获取某填报任务的问答列表
     */
    @GET("ht/api/v2/xxtb/task/{id}/question")
    Single<ResponseData<List<TaskQuestion>>> getTaskQuestions(@Path("id") String id);


    /**
     * 获取某填报任务的问答列表
     */
    @POST("ht/api/v2/xxtb/task/{id}/question")
    Single<ResponseData<TaskQuestion>> putTaskQuestion(@Path("id") String id,
                                                       @Body AskRequest askRequest);

    /**
     * 获取某填报任务的日志
     */
    @GET("ht/api/v2/xxtb/task/{id}/my/timeline")
    Single<ResponseData<List<TaskLog>>> getTaskLogs(@Path("id") String id);

    /**
     * 数据上报
     */
    @PUT("ht/api/v2/xxtb/task/{id}/commit")
    Single<ResponseData<Object>> uploadTaskData(@Path("id") String id);

    /**
     * 填报任务数据
     */
    @GET("ht/api/v2/xxtb/user/task/info/{user_id}")
    Single<ResponseData<Map<String, Integer>>> getTaskSummary1(@Path("user_id") Long userId,
                                                               @Query("token") String token);

    /**
     * 服务申请和数据处理
     */
    @GET("admin-ht/api/stat/view")
    Single<TaskSummary2> getTaskSummary2();

    /**
     * 组织架构列明
     */
    @GET("admin-ht/api/dept?code=&page=0&size=100&sort=id,desc")
    Single<PageResponse<Department>> getDepartments();

    /**
     * 组织成员
     */
    @GET("admin-ht/api/users?page=0&size=200&sort=id,desc")
    Single<PageResponse<User2>> getUsersByDepartment(@Query("code") String code);

    /**
     * 组织成员
     */
    @GET("admin-ht/api/users?page=0&size=20000&sort=id,desc")
    Single<PageResponse<User2>> getAllUsers();

    /**
     * 搜素接口
     */
    @POST("admin-ht/api/es/search")
    Single<PageResponse<SearchResult>> search(@Body SearchRequest searchRequest);

    /**
     * 用户个人消息列表
     */
    @GET("admin-ht/api/msg")
    Single<PageResponse<SystemMessage>> getSystemMessages(@Query("receiver") Long receiverUid,
                                                          @Query("content") String content,
                                                          @Query("msgStatus") Integer msgStatus,
                                                          @Query("pageNum") Integer pageNum,
                                                          @Query("pageSize") Integer pageSize);

    /**
     * 资源目录接口
     */
    @GET("interior/data/resource/tables?category=resource")
    Single<ResponseData<PageResponse<ResourceEntity>>> getResources(@Query("dirId") Long dirId,
                                                                    @Query("userId") Long userId,
                                                                    @Query("name") String name,
                                                                    @Query("orderBy") String orderBy,
                                                                    @Query("pageNum") Integer pageNum,
                                                                    @Query("pageSize") Integer pageSize);

    /**
     * 资源目录详情接口
     */
    @GET("interior/data/resource/table/detail/{id}?userType=external&category=resource")
    Single<ResponseData<ResourceEntity>> getResourceDetail(@Path("id") String id,
                                                           @Query("userId") Long userId);

    /**
     * 资源目录数据查询接口
     */
    @POST("admin-ht/api/resource/dapi/{username}/0/{tableCode}")
    Single<DataQueryResult> queryResourceData(@Path("username") String username,
                                              @Path("tableCode") String tableCode,
                                              @Body ResourceDataRequest request);

    /**
     * 资源目录数据查询接口
     */
    @POST("gtone-ht/label/dsgMetadataRelation/findListBypresentTableId")
    Single<ResponseData<List<RelevanceTable>>> getResourceRelevanceData(@Body Map<String, String> request);

    /**
     * 收藏
     */
    @POST("gtone-ht/interior/data/collect/create/{resource_id}")
    Single<ResponseData<Object>> favourite(@Path("resource_id") String resourceId,
                                           @Body Map<String, Object> request);

    /**
     * 取消收藏
     */
    @GET("gtone-ht/interior/data/collect/cancel/{collect_id}")
    Single<ResponseData<Object>> unFavourite(@Path("collect_id") String collectId);

    /**
     * 申请
     */
    @POST("gtone-ht/interior/data/resource/v2/apply")
    Single<ResponseData<Object>> applyResource(@Body Map<String, String> request);

    /**
     * 服务列表接口
     */
    @GET("gtone-ht/interior/data/dservice/dir/tableList?category=service")
    Single<ResponseData<PageResponse<ServiceEntity>>> getServices(@Query("dirId") String dirId,
                                                                  @Query("name") String name,
                                                                  @Query("userId") Long userId,
                                                                  @Query("userType") String userType,
                                                                  @Query("pageNum") Integer pageNum,
                                                                  @Query("pageSize") Integer pageSize);

    /**
     * 服务
     */
    @GET("gtone-ht/interior/data/dservice/dir/detail/{id}?userType=external&category=service")
    Single<ResponseData<ServiceEntity>> getService(@Path("id") String id,
                                                   @Query("userId") Long userId);

    /**
     * 申请
     */
    @POST("gtone-ht/interior/data/service/apply")
    Single<ResponseData<Object>> applyService(@Body Map<String, String> request);

    /**
     * 实时服务数据查询接口
     */
    @POST("admin-ht/api/esb/call/0/{username}/{invokeName}")
    Single<DataQueryResult2> queryServiceData(@Path("username") String username,
                                              @Path("invokeName") String invokeName,
                                              @Body ServiceDataRequest request);

    /**
     * 实时服务数据查询接口
     */
    @POST("admin-ht/api/esb/call/0/{username}/{invokeName}")
    Single<DataQueryResult2> queryServiceData(@Path("username") String username,
                                              @Path("invokeName") String invokeName,
                                              @Body ServiceDataRequestEmpty request);

    /**
     * 获取资迅类型
     */
    @GET("crawler/crawler/news/menu?pageNum=1&pageSize=100")
    Single<PageResponse<NewsCategory>> getNewsCategories();

    /**
     * 资迅列表接口
     */
    @GET("crawler/crawlerPage/news?crawlerStatus=2&depth=2")
    Single<PageResponse<News>> getNews(@Query("seedUrlMd5") String seedUrlMd5,
                                       @Query("pageNum") Integer pageNum,
                                       @Query("pageSize") Integer pageSize);

    /**
     * 年警情数据
     */
    @GET("mhht/mhjy/data/api/v3/home/ndxxtj")
    Single<ResponseData<List<Map<String, Integer>>>> getPoliceStaticsYear(@Query("queryDate") String date);

    /**
     * 日警情数据
     */
    @GET("mhht/mhjy/data/api/v2/home/jrjqxxtj")
    Single<ResponseData<List<Map<String, Integer>>>> getPoliceStaticsDay(@Query("queryDate") String date);

    /**
     * 实时警情数据
     */
    @GET("mhht/mhjy/data/api/v2/home/ssajxx")
    Single<ResponseData<List<Map<String, String>>>> getRealTimePoliceStatics(@Query("queryDate") String date);
}
