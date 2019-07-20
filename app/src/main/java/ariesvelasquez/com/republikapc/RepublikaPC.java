package ariesvelasquez.com.republikapc;

import android.app.Application;
import timber.log.Timber;

public class RepublikaPC extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Setup Timber Logger
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
