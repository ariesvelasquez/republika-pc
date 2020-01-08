package ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.utils.Tools

class SavedItemViewHolder(val view: View, private val onClickCallback: (v: View, item: Saved) -> Unit) : RecyclerView.ViewHolder(view) {

    private var item : Saved? = null

    private var context = view.context

    private var tools = Tools().numberFormatter

    init {
        view.setOnClickListener {}
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: Saved?) {
        this.item = item

        // Init Views
        val parentView = view.findViewById<ConstraintLayout>(R.id.constraintLayoutParent)
        val titleView = view.findViewById<TextView>(R.id.name)
        val sellerNameView = view.findViewById<TextView>(R.id.textViewSellerName)
        val priceView = view.findViewById<TextView>(R.id.textViewPrice)

        // Set name
        titleView.text = item?.name
        sellerNameView.text = item?.seller
        val itemPriceClean = item?.price?.removePrefix("HP")!!.removePrefix("P")

        priceView.text = tools?.format(itemPriceClean.toDouble()) + ".00"

        parentView.setOnClickListener {
            onClickCallback.invoke(it, item)
        }
    }

    companion object {
        fun create(parent: ViewGroup, callback: (v: View, item: Saved) -> Unit): SavedItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_saved, parent, false)
            return SavedItemViewHolder(view, callback)
        }
    }
}