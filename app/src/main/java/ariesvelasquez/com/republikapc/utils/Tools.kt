package ariesvelasquez.com.republikapc.utils

import org.ocpsoft.prettytime.PrettyTime
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Tools {
    val numberFormatter: NumberFormat? by lazy { NumberFormat.getNumberInstance(Locale.US) }

    fun lastUpdatePrettyTimeFormatter(lastRefreshDate: String?): String {

        if (lastRefreshDate.isNullOrEmpty()) return ""

        val prettyTime = PrettyTime()
        val pattern = "EEE MMM dd HH:mm:ss zzz yyyy"
        val formatter = SimpleDateFormat(pattern ,Locale.ENGLISH)
        val date = formatter.parse(lastRefreshDate)
        val formattedDate = prettyTime.format(date)

        return "List last updated $formattedDate"
    }
}