package ariesvelasquez.com.republikapc.ui.tipidpc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem

class SellingItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private val thumbnail : ImageView = view.findViewById(R.id.thumbnail)
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
        fun create(parent: ViewGroup): SellingItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.reddit_post_item, parent, false)
            return SellingItemViewHolder(view)
        }
    }

    fun updateScore(item: FeedItem?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}