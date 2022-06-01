package ariesvelasquez.com.republikapc.ui

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.utils.Tools
import timber.log.Timber


@BindingAdapter("itemHeight")
fun itemHeight(constraintLayout: ConstraintLayout, isVisible: Boolean) {
    val params: StaggeredGridLayoutManager.LayoutParams = constraintLayout.layoutParams as
            StaggeredGridLayoutManager.LayoutParams

    if (!isVisible) {
        params.height = 0
        params.topMargin = 0
        params.rightMargin = 0
        params.bottomMargin = 0
        constraintLayout.layoutParams = params
    }
}

@BindingAdapter("itemHeight2")
fun itemHeight2(constraintLayout: ConstraintLayout, isVisible: Boolean) {
    val params: RecyclerView.LayoutParams = constraintLayout.layoutParams as
            RecyclerView.LayoutParams

    if (!isVisible) {
        params.height = 0
        params.topMargin = 0
        params.rightMargin = 0
        params.bottomMargin = 0
        constraintLayout.layoutParams = params
    }
}

@BindingAdapter("source")
fun source(textView: TextView, type: String?) {
    val context = textView.context
    textView.text = when (type) {
        Const.SaveType.TPC_ITEM -> context.getString(R.string.label_tipid_pc_dot_com)
        else -> context.getString(R.string.label_tipid_pc_dot_com)
    }
}

@BindingAdapter("refreshing")
fun refreshLayout(swipeRefreshLayout: SwipeRefreshLayout, refreshState: MutableLiveData<Boolean>) {
    Timber.e("Refreshing " + refreshState.value)
    swipeRefreshLayout.isRefreshing = refreshState.value ?: false
}

@BindingAdapter("syncValueCount")
fun syncValueCount(textView: TextView, rawDate: String?) {
    if (rawDate.isNullOrEmpty()) return
    val context = textView.context
    val prettyDate = Tools.lastUpdatePrettyTimeFormatter(rawDate)
    if (prettyDate.contains("moments")) {
        textView.setTextAppearance(context, R.style.HeadLine5)
        textView.setTextColor(ContextCompat.getColor(context, R.color.primary))
        textView.text = prettyDate
    } else {
        val dividedPrettyDate = prettyDate.split(" ")
        textView.setTextAppearance(textView.context, R.style.HeadLine4)
        textView.setTextColor(ContextCompat.getColor(context, R.color.primary))
        textView.text = dividedPrettyDate[0]
    }
}

@BindingAdapter("syncValueSub")
fun syncValueSub(textView: TextView, rawDate: String?) {
    if (rawDate.isNullOrEmpty()) return
    val prettyDate = Tools.lastUpdatePrettyTimeFormatter(rawDate)
    Timber.e("prettyDate $prettyDate" )
    if (prettyDate.contains("moments")) {
        textView.visibility = View.GONE
    } else {
        val dividedPrettyDate: ArrayList<String> = prettyDate.split(" ") as ArrayList<String>
        dividedPrettyDate[0] = ""
        textView.text = dividedPrettyDate.joinToString(" ").removePrefix(" ")
    }
}