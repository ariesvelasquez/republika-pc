package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.utils.Tools
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*

class TipidPCSellerItemsViewHolder(view: View, private val onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit) : RecyclerView.ViewHolder(view) {

    private var tools = Tools.numberFormatter

    private val mainView: ConstraintLayout = view.findViewById(R.id.constraintLayoutParent)
    private val name: TextView = view.findViewById(R.id.name)
    private val price: TextView = view.findViewById(R.id.textViewPrice)
    private val textViewLabelSpacer: TextView = view.findViewById(R.id.textViewLabelSpacer)
    private val constraintLayoutInfo: ConstraintLayout = view.findViewById(R.id.constraintLayoutInfo)
    private var item : FeedItem? = null

    init {
        view.setOnClickListener {

            //            item?.itemurl?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                view.context.startActivity(intent)
//////            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: FeedItem, position: Int) {
//        this.item = item
        name.text = item.name
        price.text = tools?.format(item.price?.removePrefix("PHP")?.toDouble()) + ".00"

        if (position == 0) {
            val prettifiedDate = Tools.lastUpdatePrettyTimeFormatter(item.lastRefresh)
            if (prettifiedDate.isNotEmpty()) {
                textViewLabelSpacer.visibility = View.VISIBLE
                constraintLayoutInfo.visibility = View.VISIBLE
            }
        } else {
            constraintLayoutInfo.visibility = View.GONE
            textViewLabelSpacer.visibility = View.GONE
        }

        mainView.setOnClickListener {
            onClickCallback.invoke(it, position, item)
        }
    }

    companion object {
        fun create(parent: ViewGroup, onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit): TipidPCSellerItemsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_seller_item, parent, false)
            return TipidPCSellerItemsViewHolder(view, onClickCallback)
        }
    }

    fun updatePrice(item: FeedItem?) {

//        score.text = "${item?.score ?: 0}"
    }
}