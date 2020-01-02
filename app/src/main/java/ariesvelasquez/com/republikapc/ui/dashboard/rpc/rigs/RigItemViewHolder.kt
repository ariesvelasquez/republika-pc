package ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs

import android.annotation.SuppressLint
import android.media.Image
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.Rig
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RigItemViewHolder(val view: View, private val onClickCallback: (v: View, item: Rig) -> Unit) : RecyclerView.ViewHolder(view) {

    private var chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupHighlights)
    private var item : Rig? = null

    // Init Views
    private val titleView = view.findViewById<TextView>(R.id.title)
    private val partsView = view.findViewById<LinearLayout>(R.id.linearLayoutParts)
    private val countView = view.findViewById<TextView>(R.id.textViewItemCount)
    private val viewAllView = view.findViewById<TextView>(R.id.textViewViewAllParts)
    private val dateView = view.findViewById<TextView>(R.id.textViewDate)
    private val imageViewAll = view.findViewById<ImageView>(R.id.imageViewViewAllParts)
    private val imageViewOption = view.findViewById<ImageView>(R.id.imageViewOption)

    private var context = view.context
    private val chipWidth: Float by lazy {
        TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 1f,
            context.resources.displayMetrics)
    }

    init {
        view.setOnClickListener {}
    }

    @SuppressLint("SetTextI18n")
    fun bind(item: Rig?) {
        this.item = item

        // Set name
        titleView.text = item?.name
        // Item Count
        countView.text = item?.itemCount.toString()
        // Date
        val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US)
        val creationDate = dateFormat.format(item?.date)
        dateView.text = "${context.getString(R.string.created_at_)} $creationDate"

        // Parts Click
        partsView.setOnClickListener { onClickCallback.invoke(it, item!!) }
        viewAllView.setOnClickListener { onClickCallback.invoke(it, item!!) }
        viewAllView.setOnClickListener { onClickCallback.invoke(it, item!!) }
        imageViewAll.setOnClickListener { onClickCallback.invoke(it, item!!) }
        imageViewOption.setOnClickListener { onClickCallback.invoke(it, item!!) }

        // Add Dummy Chips
        val chip1 = Chip(view.context)
        chip1.text = "XXXXXXXXXXXXX"
//        chip1.chipIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_vector_star)
        chip1.chipStrokeWidth = chipWidth
        chip1.setChipStrokeColorResource(R.color.colorDarkGray)
        chip1.setChipBackgroundColorResource(R.color.colorWhite)
        chip1.setTextAppearance(R.style.RigChipTextStyle)
//        chip1.setTextColor(ContextCompat.getColor(context, R.color.text_helper_dark))

        val chip2 = Chip(view.context)
        chip2.text = "XXXXXXXXX "
//        chip2.chipIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_vector_star)
        chip2.chipStrokeWidth = chipWidth
        chip2.setChipStrokeColorResource(R.color.colorDarkGray)
        chip2.setChipBackgroundColorResource(R.color.colorWhite)
        chip2.setTextAppearance(R.style.RigChipTextStyle)

        chipGroup.removeAllViews()
        chipGroup.addView(chip1)
        chipGroup.addView(chip2)

//        name.text = item?.name ?: "loading"
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
        fun create(parent: ViewGroup, callback: (v: View, item: Rig) -> Unit): RigItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_recycler_view_rig_2, parent, false)
            return RigItemViewHolder(view, callback)
        }
    }

    fun updateScore(item: Rig?) {
        this.item = item
//        score.text = "${item?.score ?: 0}"
    }
}