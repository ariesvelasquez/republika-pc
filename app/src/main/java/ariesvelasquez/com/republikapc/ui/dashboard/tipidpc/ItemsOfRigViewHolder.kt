package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem

class ItemsOfRigViewHolder(view: View, private val onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit) : RecyclerView.ViewHolder(view) {

    private val mainView: ConstraintLayout = view.findViewById(R.id.constraintLayoutParent)
    private val title: TextView = view.findViewById(R.id.title)
    private val sellerName: TextView = view.findViewById(R.id.textViewSellerName)
    private val page: TextView = view.findViewById(R.id.textViewItemDescription)
    private val price: TextView = view.findViewById(R.id.textViewPrice)
    private var item : FeedItem? = null

    init {
        view.setOnClickListener {

            //            item?.itemurl?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                view.context.startActivity(intent)
//            }
        }
    }

    fun bind(item: FeedItem, position: Int) {
        this.item = item
        title.text = item.title ?: "loading"
        sellerName.text = item.seller
        price.text = item.price
        page.text = item.page.toString()

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
        fun create(parent: ViewGroup, onClickCallback: (v: View, position: Int, item: FeedItem) -> Unit): ItemsOfRigViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_rig_item, parent, false)
            return ItemsOfRigViewHolder(view, onClickCallback)
        }
    }

    fun updateScore(item: FeedItem?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}