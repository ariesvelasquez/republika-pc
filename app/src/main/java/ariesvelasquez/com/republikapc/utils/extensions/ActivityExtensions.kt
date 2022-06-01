package ariesvelasquez.com.republikapc.utils.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.ui.webview.WebViewActivity

/**
 * Kotlin Extensions for simpler, easier and funw way
 * of launching of Activities
 */

@SuppressLint("ObsoleteSdkInt")
inline fun <reified T : Any> Activity.launchActivity (
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {})
{
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
    {
        startActivityForResult(intent, requestCode, options)
    } else {
        startActivityForResult(intent, requestCode)
    }
}

@SuppressLint("ObsoleteSdkInt")
inline fun <reified T : Any> Context.launchActivity (
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {})
{
    val intent = newIntent<T>(this)
    intent.init()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
    {
        startActivity(intent, options)
    } else {
        startActivity(intent)
    }
}

fun Context.launchToSellerWebActivity(url: String) {
    this.launchActivity<WebViewActivity> {
        putExtra(WebViewActivity.WEB_VIEW_URL, url)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)