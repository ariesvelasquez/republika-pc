package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.utils.Tools

class TipidPCSellerViewHolder(view: View, private val onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit) : RecyclerView.ViewHolder(view) {

    private val name: TextView = view.findViewById(R.id.name)
    private val icon: ImageView = view.findViewById(R.id.imageViewTipidPCIcon)
    private val mainView: LinearLayout = view.findViewById(R.id.linearLayoutMainView)

    private val context = view.context

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

        if (item.isEmptyItem) {
            name.text = context.getString(R.string.no_sellers_found)
            name.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
            icon.visibility = View.GONE
            mainView.isEnabled = false
            return
        }

        name.text = item.seller
        icon.visibility = View.VISIBLE
        mainView.isEnabled = true
        mainView.setOnClickListener { onClickCallback.invoke(it, position, item)}
    }

    companion object {
        fun create(parent: ViewGroup, onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit): TipidPCSellerViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_seller, parent, false)
            return TipidPCSellerViewHolder(view, onClickCallback)
        }
    }
}