package ariesvelasquez.com.republikapc.utils

import java.text.NumberFormat
import java.util.*

class Tools {
    val numberFormatter: NumberFormat? by lazy { NumberFormat.getNumberInstance(Locale.US) }
}