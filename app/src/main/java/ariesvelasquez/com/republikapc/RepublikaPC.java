package ariesvelasquez.com.republikapc;

import android.app.Application;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import timber.log.Timber;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class RepublikaPC extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Timber Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

//        try {
//            ProviderInstaller.installIfNeeded(getApplicationContext());
//        } catch (GooglePlayServicesRepairableException e) {
//            e.printStackTrace();
//        } catch (GooglePlayServicesNotAvailableException e) {
//            e.printStackTrace();
//        }


        // Fix for HTTP FAILED: javax.net.ssl.SSLHandshakeException: Handshake failed
//        try {
//            ProviderInstaller.installIfNeeded(getApplicationContext());
//            SSLContext sslContext;
//            sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(null, null, null);
//            sslContext.createSSLEngine();
//        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException
//                | NoSuchAlgorithmException | KeyManagementException e) {
//            e.printStackTrace();
//        }
    }
}
