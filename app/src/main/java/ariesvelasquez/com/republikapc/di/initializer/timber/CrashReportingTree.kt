package ariesvelasquez.com.republikapc.di.initializer.timber

import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.lang.Exception

class CrashReportingTree : Timber.Tree() {

    companion object {
        const val CRASHLYTICS_KEY_PRIORITY = "priority"
        const val CRASHLYTICS_KEY_TAG = "tag"
        const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        // Crashlytics
        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
        crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag ?: "Empty Key")
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)
    }
}

