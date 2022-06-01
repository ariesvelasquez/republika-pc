package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.utils.Tools

class RigPartViewHolder(view: View, private val onClickCallback: (v: View, position: Int, item: RigPart) -> Unit) : RecyclerView.ViewHolder(view) {

    private var tools = Tools.numberFormatter

    private val mainView: ConstraintLayout = view.findViewById(R.id.constraintLayoutParent)
    private val name: TextView = view.findViewById(R.id.name)
    private val sellerName: TextView = view.findViewById(R.id.textViewSellerName)
    private val price: TextView = view.findViewById(R.id.textViewPrice)
    private var item : RigPart? = null

    init {
        view.setOnClickListener {

        }
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: RigPart, position: Int) {
        this.item = item
        name.text = item.name ?: "loading"
        sellerName.text = item.seller

        val itemPriceClean = item.price.
            replace("PHP", "").
            replace("HP", "").
            replace("P", "")

        price.text = tools?.format(itemPriceClean.toDouble()) + ".00"

        mainView.setOnClickListener {
            onClickCallback.invoke(it, position, item)
        }

//        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
//            post?.author ?: "unknown")
//        score.text = "${post?.score ?: 0}"
//        if (post?.thumbnail?.startsWith("http") == true) {
//            thumbnail.visibility = View.VISIBLE
//            glide.load(post.thumbnail)
//                .centerCrop()
//                .placeholder(R.drawable.ic_insert_photo_black_48dp)
//                .into(thumbnail)
//        } else {
//            thumbnail.visibility = View.GONE
//            glide.clear(thumbnail)
//        }
    }

    companion object {
        fun create(parent: ViewGroup, onClickCallback: (v: View, position: Int, item: RigPart) -> Unit): RigPartViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_rig_part, parent, false)
            return RigPartViewHolder(view, onClickCallback)
        }
    }

    fun updateScore(item: RigPart?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}