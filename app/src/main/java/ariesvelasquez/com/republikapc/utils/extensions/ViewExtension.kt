package ariesvelasquez.com.republikapc.utils.extensions

import android.content.Context
import android.content.Intent
import android.view.View

fun View.visibleGone(visible: Boolean) {
    this.visibility = when (visible) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}