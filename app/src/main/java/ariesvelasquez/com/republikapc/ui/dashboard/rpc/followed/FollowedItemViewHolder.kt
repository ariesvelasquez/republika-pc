package ariesvelasquez.com.republikapc.ui.dashboard.rpc.followed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.saved.Saved

class FollowedItemViewHolder(val view: View, private val onClickCallback: (v: View, item: Saved) -> Unit) : RecyclerView.ViewHolder(view) {

    private var item : Saved? = null

    private var context = view.context

    init {
        view.setOnClickListener {}
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: Saved?) {
        this.item = item

        // Init Views
        val parentView = view.findViewById<ConstraintLayout>(R.id.constraintLayoutParent)
        val titleView = view.findViewById<TextView>(R.id.name)

        // Set name
        titleView.text = item?.seller

        parentView.setOnClickListener {
            onClickCallback.invoke(it, item!!)
        }
    }

    companion object {
        fun create(parent: ViewGroup, callback: (v: View, item: Saved) -> Unit): FollowedItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_followed, parent, false)
            return FollowedItemViewHolder(view, callback)
        }
    }
}