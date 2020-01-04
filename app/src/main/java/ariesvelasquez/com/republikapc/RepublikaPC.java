package ariesvelasquez.com.republikapc;

import android.app.Application;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class RepublikaPC extends Application {

    private static GlobalFlags globalFlags;

    @Override
    public void onCreate() {
        super.onCreate();

        globalFlags = new GlobalFlags();

        // Setup Timber Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashReportingTree());
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

    private class CrashReportingTree extends Timber.Tree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, String tag, String message, Throwable throwable) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            Throwable t = throwable != null
                    ? throwable
                    : new Exception(message);

            // Crashlytics
            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);
            Crashlytics.logException(t);
        }
    }

    public static GlobalFlags getGlobalFlags() {
        return globalFlags;
    }
}
