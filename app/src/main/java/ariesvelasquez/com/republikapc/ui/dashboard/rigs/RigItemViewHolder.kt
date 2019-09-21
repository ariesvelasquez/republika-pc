package ariesvelasquez.com.republikapc.ui.dashboard.rigs

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.RigItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class RigItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private var chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupHighlights)
    private var item : RigItem? = null

    private var context = view.context

    init {
        view.setOnClickListener {
//            item?.itemurl?.let { url ->
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                view.context.startActivity(intent)
//            }
        }
    }

    fun bind(item: RigItem?) {
        this.item = item

        // Add Dummy Chips
        val chip1 = Chip(view.context)
        chip1.text = "Ryzen 5 1600x"
        chip1.chipIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_vector_back)
        chip1.setChipBackgroundColorResource(R.color.text_white)
        chip1.setTextColor(ContextCompat.getColor(context, R.color.text_helper_dark))

        val chip2 = Chip(view.context)
        chip2.text = "GTX 2080ti"
        chip2.chipIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_vector_back)
        chip2.setChipBackgroundColorResource(R.color.text_white)
        chip2.setTextColor(ContextCompat.getColor(context, R.color.text_helper_dark))

        chipGroup.addView(chip1)
        chipGroup.addView(chip2)

//        title.text = item?.title ?: "loading"
//        sellerName.text = item?.seller
//        price.text = item?.price
//        page.text = item?.page?.toString()
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
        fun create(parent: ViewGroup): RigItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.rigs_recyclerview_item, parent, false)
            return RigItemViewHolder(view)
        }
    }

    fun updateScore(item: RigItem?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}