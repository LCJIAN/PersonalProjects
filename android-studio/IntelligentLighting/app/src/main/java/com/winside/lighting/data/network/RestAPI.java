package com.winside.lighting.data.network;

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.winside.lighting.App;
import com.winside.lighting.BuildConfig;
import com.winside.lighting.data.network.entity.Building;
import com.winside.lighting.data.network.entity.Device;
import com.winside.lighting.data.network.entity.Floor;
import com.winside.lighting.data.network.entity.Project;
import com.winside.lighting.data.network.entity.Region;
import com.winside.lighting.data.network.entity.RegionFloorPlanData;
import com.winside.lighting.data.network.entity.ResponseData;
import com.winside.lighting.data.network.entity.SignInInfo;
import com.winside.lighting.util.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.mock.BehaviorDelegate;
import retrofit2.mock.MockRetrofit;
import retrofit2.mock.NetworkBehavior;
import timber.log.Timber;

public class RestAPI {

    private static String API_URL = BuildConfig.API_URL;

    private static final int DISK_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    private static RestAPI instance;

    private Retrofit retrofitSignIn;
    private Retrofit retrofit;

    private SignInService signInService;
    private LightingService lightingService;

    private SignInService signInServiceMock;
    private LightingService lightingServiceMock;

    private String token;

    private static final TypeAdapter<Boolean> BOOLEAN_TYPE_ADAPTER = new TypeAdapter<Boolean>() {

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING: {
                    String s = in.nextString();
                    if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
                        return Boolean.parseBoolean(s);
                    } else {
                        return Integer.parseInt(s) != 0;
                    }
                }
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };

    public static RestAPI getInstance() {
        if (instance == null) {
            synchronized (RestAPI.class) {
                if (instance == null) {
                    instance = new RestAPI();
                }
            }
        }
        return instance;
    }

    private Retrofit getRetrofitSignIn() {
        if (retrofitSignIn == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofitSignIn = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setDateFormat("MMM d, yyyy").create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofitSignIn;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .cache(getCache());

            clientBuilder
                    .authenticator((route, response) -> {
                        Call<ResponseData<SignInInfo>> call = signInService().signIn("", "");
                        ResponseData<SignInInfo> responseData = call.execute().body();
                        if (responseData != null) {
                            token = responseData.data.token;
                            return response.request()
                                    .newBuilder()
                                    .addHeader("Authorization", token)
                                    .build();
                        } else {
                            return null;
                        }
                    })
                    .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("Authorization", token).build()));
            if (BuildConfig.DEBUG) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory
                            .create(new GsonBuilder()
                                    .registerTypeAdapter(Boolean.class, BOOLEAN_TYPE_ADAPTER)
                                    .registerTypeAdapter(boolean.class, BOOLEAN_TYPE_ADAPTER)
                                    .setDateFormat("MMM d, yyyy")
                                    .create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(clientBuilder.build())
                    .build();
        }
        return retrofit;
    }

    private Cache getCache() {
        Cache cache = null;
        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(StorageUtils.getCacheDirectory(App.getInstance()), "http");
            cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        } catch (Exception e) {
            Timber.e(e, "Unable to install disk cache.");
        }
        return cache;
    }

    public void resetApiUrl(String apiUrl) {
        retrofitSignIn = null;
        retrofit = null;
        token = null;
        API_URL = apiUrl;
    }

    public void refreshToken(String token) {
        this.token = token;
    }

    public LightingService lightingServiceMock() {
        if (lightingService == null) {
            lightingService = getRetrofit().create(LightingService.class);
        }
        return lightingService;
    }

    public SignInService signInServiceMock() {
        if (signInService == null) {
            signInService = getRetrofitSignIn().create(SignInService.class);
        }
        return signInService;
    }

    public LightingService lightingService() {
        if (lightingServiceMock == null) {
            NetworkBehavior behavior = NetworkBehavior.create();
            MockRetrofit mockRetrofit = new MockRetrofit.Builder(getRetrofit()).networkBehavior(behavior).build();
            BehaviorDelegate<LightingService> delegate = mockRetrofit.create(LightingService.class);
            lightingServiceMock = new MockLightingService(delegate);
        }
        return lightingServiceMock;
    }

    public SignInService signInService() {
        if (signInServiceMock == null) {
            NetworkBehavior behavior = NetworkBehavior.create();
            MockRetrofit mockRetrofit = new MockRetrofit.Builder(getRetrofitSignIn()).networkBehavior(behavior).build();
            BehaviorDelegate<SignInService> delegate = mockRetrofit.create(SignInService.class);
            signInServiceMock = new MockSignInService(delegate);
        }
        return signInServiceMock;
    }

    static final class MockLightingService implements LightingService {

        private final BehaviorDelegate<LightingService> delegate;

        MockLightingService(BehaviorDelegate<LightingService> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Single<ResponseData<Object>> signOut() {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).signOut();
        }

        @Override
        public Single<ResponseData<Object>> modifyPassword(String oldPassword, String newPassword) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).modifyPassword(oldPassword, newPassword);
        }

        @Override
        public Single<ResponseData<List<Project>>> getProjects() {
            ResponseData<List<Project>> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new ArrayList<>();
            Project project = new Project();
            project.id = 1L;
            project.name = "项目名称";
            project.buildCount = 1;
            project.lightTotal = 1;
            project.lightFixedTotal = 1;
            project.lightOnlineTotal = 1;
            project.sensorTotal = 1;
            project.sensorFixedTotal = 1;
            project.sensorOnlineTotal = 1;
            response.data.add(project);
            return delegate.returningResponse(response).getProjects();
        }

        @Override
        public Single<ResponseData<List<Building>>> getBuildings(Long projectId) {
            ResponseData<List<Building>> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new ArrayList<>();
            Building building = new Building();
            building.id = 1L;
            building.name = "建筑名称";
            response.data.add(building);
            return delegate.returningResponse(response).getBuildings(projectId);
        }

        @Override
        public Single<ResponseData<List<Floor>>> getFloors(Long buildingId) {
            ResponseData<List<Floor>> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new ArrayList<>();
            Floor floor = new Floor();
            floor.id = 1L;
            floor.name = "楼层名称";
            response.data.add(floor);
            return delegate.returningResponse(response).getFloors(buildingId);
        }

        @Override
        public Single<ResponseData<List<Region>>> getRegions(Long floorId) {
            ResponseData<List<Region>> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new ArrayList<>();
            Region region = new Region();
            region.id = 1L;
            region.name = "区域名称";
            response.data.add(region);
            return delegate.returningResponse(response).getRegions(floorId);
        }

        @Override
        public Single<ResponseData<Object>> getFloorData(Long floorId) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).getFloorData(floorId);
        }

        @Override
        public Single<ResponseData<RegionFloorPlanData>> getRegionData(Long regionId) {
            ResponseData<RegionFloorPlanData> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new RegionFloorPlanData();
            response.data.id = 1L;
            response.data.name = "ss";
            response.data.drawings = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1597325872572&di=d780657b987a20900c051f35c1823e09&imgtype=0&src=http%3A%2F%2Fimg1.focus.cn%2Fupload%2Fbj%2F36134%2Fa_361331797.jpg";
            response.data.gatewayCoordinates = new ArrayList<>();
            RegionFloorPlanData.GatewayCoordinate gate = new RegionFloorPlanData.GatewayCoordinate();
            gate.id = 1L;
            gate.name = "ss";
            gate.point = new Double[]{100.0, 100.0};
            gate.deviceId = 1L;
            gate.deviceTypeId = 1;
            gate.deviceStatus = "unconf";
            gate.deviceTypeName = "XXX";
            gate.deviceCoordinates = new ArrayList<>();

            RegionFloorPlanData.DeviceCoordinate dc = new RegionFloorPlanData.DeviceCoordinate();
            dc.deviceId = 1L;
            dc.deviceStatus = "unreg";
            dc.deviceTypeId = 8;
            dc.deviceTypeName = "sss";
            dc.id = 1L;
            dc.point = new Double[]{200.0,200.0};
            gate.deviceCoordinates.add(dc);
            response.data.gatewayCoordinates.add(gate);
            return delegate.returningResponse(response).getRegionData(regionId);
        }

        @Override
        public Single<ResponseData<Object>> bindPosition(Long coordinateId, String deviceSN) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).bindPosition(coordinateId, deviceSN);
        }

        @Override
        public Single<ResponseData<Device>> getDeviceDetail(Long deviceId) {
            ResponseData<Device> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new Device();
            response.data.alias = "1";
            return delegate.returningResponse(response).getDeviceDetail(deviceId);
        }

        @Override
        public Single<ResponseData<Object>> noticeConfigResult(String deviceIds) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).noticeConfigResult(deviceIds);
        }

        @Override
        public Single<ResponseData<Object>> deleteDevice(Long coordinateId) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).deleteDevice(coordinateId);
        }

        @Override
        public Single<ResponseData<List<Device>>> getDevices(Long gwDeviceId) {
            ResponseData<List<Device>> response = new ResponseData<>();
            return delegate.returningResponse(response).getDevices(gwDeviceId);
        }

        @Override
        public Single<ResponseData<Object>> sendToDevice(Long deviceId, String message) {
            ResponseData<Object> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            return delegate.returningResponse(response).sendToDevice(deviceId, message);
        }
    }

    static class MockSignInService implements SignInService {

        private final BehaviorDelegate<SignInService> delegate;

        MockSignInService(BehaviorDelegate<SignInService> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Call<ResponseData<SignInInfo>> signIn(String userName, String password) {
            ResponseData<SignInInfo> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new SignInInfo();
            response.data.token = "ss";
            response.data.userId = 1L;
            return delegate.returningResponse(response).signIn(userName, password);
        }

        @Override
        public Single<ResponseData<SignInInfo>> signInRx(String userName, String password) {
            ResponseData<SignInInfo> response = new ResponseData<>();
            response.code = 1000;
            response.message = "success";
            response.data = new SignInInfo();
            response.data.token = "ss";
            response.data.userId = 1L;
            return delegate.returningResponse(response).signInRx(userName, password);
        }
    }

}
