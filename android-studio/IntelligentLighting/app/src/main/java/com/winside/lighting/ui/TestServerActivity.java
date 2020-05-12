package com.winside.lighting.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.winside.lighting.App;
import com.winside.lighting.BuildConfig;
import com.winside.lighting.R;
import com.winside.lighting.data.local.SharedPreferencesDataSource;
import com.winside.lighting.data.network.RestAPI;
import com.winside.lighting.mesh.BleClient;
import com.winside.lighting.mesh.WTaskGattConfig;
import com.winside.lighting.ui.base.BaseActivity;
import com.winside.lighting.ui.main.MainActivity;
import com.winside.lighting.ui.user.SignInActivity;
import com.winside.lighting.util.Utils;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TestServerActivity extends BaseActivity {

    @BindView(R.id.tv_navigation_title)
    TextView tv_navigation_title;
    @BindView(R.id.et_server_address)
    EditText et_server_address;
    @BindView(R.id.btn_next)
    Button btn_next;

    private Disposable mDisposable;
    private Disposable mDisposableP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_server);
        ButterKnife.bind(this);

        tv_navigation_title.setText(R.string.config);
        et_server_address.setText(BuildConfig.API_URL);
        btn_next.setOnClickListener(v -> {
            RxPermissions rxPermissions = new RxPermissions(this);
            mDisposableP = rxPermissions
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    .subscribe(granted -> {
                        if (granted) {
//                            test();
                            testUrl();
                        } else {
                            finish();
                        }
                    });
        });

        tryToAutoSignIn();
    }

    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        if (mDisposableP != null) {
            mDisposableP.dispose();
        }
        super.onDestroy();
    }

    private void testUrl() {
        String serverAddress = et_server_address.getEditableText().toString();
        if (TextUtils.isEmpty(serverAddress)) {
            return;
        }
        if (!serverAddress.startsWith("http://") && !serverAddress.startsWith("https://")) {
            serverAddress = "http://" + serverAddress;
        }
        showProgress();
        mDisposable = Single.just(serverAddress)
                .map(s -> Pair.create(s, Utils.testUrlWithTimeOut(s, 10 * 1000)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aPair -> {
                            hideProgress();
                            if (aPair.second != null && aPair.second) {
                                RestAPI.getInstance().resetApiUrl(aPair.first);
                                SharedPreferencesDataSource.setServerAddress(aPair.first);
                                startActivity(new Intent(this, SignInActivity.class));
                                finish();
                            } else {
                                Toast.makeText(App.getInstance(), R.string.can_not_connect_server, Toast.LENGTH_LONG).show();
                            }
                        },
                        throwable -> {
                            hideProgress();
                            Toast.makeText(App.getInstance(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                        });
    }

    private void tryToAutoSignIn() {
        String token = SharedPreferencesDataSource.getToken();
        if (!TextUtils.isEmpty(token)) {
            RestAPI.getInstance().resetApiUrl(SharedPreferencesDataSource.getServerAddress());
            RestAPI.getInstance().refreshToken(token);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void test() {
//        new TaskGattConfig(this,
//                "38:d2:ca:39:58:81".toUpperCase(),
//                new byte[]{13, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeef1"),
//                new byte[]{13, 0x00},
//                new byte[]{6, 0x00, 0x00, 0x00},
//                new byte[]{0x00},
//                new byte[]{18, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeef9"),
//                new byte[]{23, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeefa"),
//                new byte[]{0x58, (byte) 0xE4},
//                null,
//                null)
//                .execute();

//        new TaskGattConfig(this,
//                "38:d2:ca:39:58:4c".toUpperCase(),
//                new byte[]{25, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeef2"),
//                new byte[]{25, 0x00},
//                new byte[]{6, 0x00, 0x00, 0x00},
//                new byte[]{0x00},
//                new byte[]{18, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeef9"),
//                new byte[]{23, 0x00},
//                Utils.hexStringToHexByte("11223344556677889900aabbccddeefa"),
//                new byte[]{0x58, (byte) 0xE4},
//                null,
//                null)
//                .execute();
        new ConnectTest().execute();
    }

//    private static class TaskGattConfig extends WTaskGattConfig {
//
//        public TaskGattConfig(Context context, String mac, byte[] destAddress, byte[] deviceKey, byte[] uniCast, byte[] ivIndex, byte[] ivState, byte[] netKeyIndex, byte[] netKey, byte[] appKeyIndex, byte[] appKey, List<byte[]> groupAddresses, byte[] wifiSSID, byte[] wifiPwd) {
//            super(context, mac, destAddress, deviceKey, uniCast, ivIndex, ivState, netKeyIndex, netKey, appKeyIndex, appKey, groupAddresses, wifiSSID, wifiPwd);
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            Toast.makeText(App.getInstance(), String.valueOf(values[0]), Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            Toast.makeText(App.getInstance(), aBoolean ? "成功" : "失败", Toast.LENGTH_LONG).show();
//        }
//    }

    private class ConnectTest extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            BleClient bleClient = new BleClient(TestServerActivity.this, "38:d2:ca:39:58:00".toUpperCase(),
                    UUID.fromString("000018bb-0000-1000-8000-00805f9b34fb"), null, null, null, null);
            if (!bleClient.connect()) {
                bleClient.disconnect();
                bleClient.release();
                return false;
            }
            if (!bleClient.discoverServices()) {
                bleClient.disconnect();
                bleClient.release();
                return false;
            }
//            if (!bleClient.enableNotification()) {
//                bleClient.disconnect();
//                bleClient.release();
//                return false;
//            }
            publishProgress(10);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean result = bleClient.sendData(Utils.hexStringToHexByte("00112233445566778899aabbccddeeff"));
            bleClient.disconnect();
            bleClient.release();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            hideProgress();
            Toast.makeText(App.getInstance(), aBoolean ? "成功" : "失败", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 10) {
                Toast.makeText(App.getInstance(), "连接失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
