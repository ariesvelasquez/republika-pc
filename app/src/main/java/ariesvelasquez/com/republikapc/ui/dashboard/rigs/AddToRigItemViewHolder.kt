package ariesvelasquez.com.republikapc.ui.dashboard.rigs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.Rig
class AddToRigItemViewHolder(val view: View, private val onClickCallback: (v: View, item: Rig) -> Unit) : RecyclerView.ViewHolder(view) {

    private var item : Rig? = null

    private var context = view.context

    init {
        view.setOnClickListener {}
    }

    fun bind(item: Rig?) {
        this.item = item

        // Init Views
        val titleView = view.findViewById<TextView>(R.id.title)
        val buttonAdd = view.findViewById<ImageView>(R.id.buttonAdd)

        // Set title
        titleView.text = item?.name

        // Parts Click
        buttonAdd.setOnClickListener { onClickCallback.invoke(it, item!!) }
    }

    companion object {
        fun create(parent: ViewGroup, callback: (v: View, item: Rig) -> Unit): AddToRigItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_add_to_rig, parent, false)
            return AddToRigItemViewHolder(view, callback)
        }
    }
}