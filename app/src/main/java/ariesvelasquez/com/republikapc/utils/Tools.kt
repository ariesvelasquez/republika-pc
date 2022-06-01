package ariesvelasquez.com.republikapc.utils

import android.view.View
import org.ocpsoft.prettytime.PrettyTime
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Tools {
    val numberFormatter: NumberFormat? by lazy { NumberFormat.getNumberInstance(Locale.US) }

    @JvmStatic
    fun formatPrice(rawPrice: String?) : String {
        if (rawPrice.isNullOrEmpty()) return ""

        val itemPriceClean = rawPrice.
            replace("PHP", "").
            replace("HP", "").
            replace("P", "")

        return numberFormatter?.format(itemPriceClean.toDouble())  + ".00"
    }

    @JvmStatic
    fun lastUpdatePrettyTimeFormatter(lastRefreshDate: String?): String {

        if (lastRefreshDate.isNullOrEmpty()) return ""

        val prettyTime = PrettyTime()
        val pattern = "EEE MMM dd HH:mm:ss zzz yyyy"
        val formatter = SimpleDateFormat(pattern ,Locale.ENGLISH)
        val date = formatter.parse(lastRefreshDate)

        return prettyTime.format(date)
    }

    @JvmStatic
    fun isVisibleGone(isVisible: Boolean): Int {
        return when (isVisible) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }
}