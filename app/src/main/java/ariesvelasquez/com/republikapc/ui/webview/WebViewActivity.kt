package ariesvelasquez.com.republikapc.ui.webview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.Const.TIPID_PC_BASE_URL
import ariesvelasquez.com.republikapc.Const.TIPID_PC_RAW_URL
import ariesvelasquez.com.republikapc.R
import kotlinx.android.synthetic.main.activity_web_view.*
import timber.log.Timber

@Keep
class WebViewActivity : AppCompatActivity() {

    companion object {
        const val WEB_VIEW_URL = "webviewurl"
    }

    private lateinit var mUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        mUrl = intent.getStringExtra(WEB_VIEW_URL) ?: Const.TIPID_PC_BASE_URL

        Timber.e("WebviewActivity Url " + mUrl)

        webView.getSettings().setJavaScriptEnabled(true)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view!!.loadUrl(url!!)

                return true
            }
        }

        webView.loadUrl(mUrl)

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
}
