package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem

class FeedsItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

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

    fun bind(item: FeedItem?) {
        this.item = item
        title.text = item?.title ?: "loading"
        sellerName.text = item?.seller
        price.text = item?.price
        page.text = item?.page?.toString()
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
        fun create(parent: ViewGroup): FeedsItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.feeds_recyclerview_item, parent, false)
            return FeedsItemViewHolder(view)
        }
    }

    fun updateScore(item: FeedItem?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}