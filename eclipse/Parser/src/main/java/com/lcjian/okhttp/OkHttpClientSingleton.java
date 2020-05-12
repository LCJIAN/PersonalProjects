package com.lcjian.okhttp;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttpClientSingleton {

    private volatile static OkHttpClient singleton;

    private OkHttpClientSingleton() {
    }

    public static OkHttpClient getSingleton() {
        if (singleton == null) {
            synchronized (OkHttpClientSingleton.class) {
                if (singleton == null) {
                    X509TrustManager trustManager = systemDefaultTrustManager();
                    singleton = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
                            .sslSocketFactory(systemDefaultSslSocketFactory(trustManager), trustManager)
                            // .proxy(new Proxy(Proxy.Type.HTTP,new
                            // InetSocketAddress("127.0.0.1" , 8888)))
                            // .cookieJar(new PersistentCookieJar(new
                            // SimpleCookiePersistor(new File("ss"))))
                            .build();
                }
            }
        }
        return singleton;
    }

    private static X509TrustManager systemDefaultTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    private static SSLSocketFactory systemDefaultSslSocketFactory(X509TrustManager trustManager) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }
}