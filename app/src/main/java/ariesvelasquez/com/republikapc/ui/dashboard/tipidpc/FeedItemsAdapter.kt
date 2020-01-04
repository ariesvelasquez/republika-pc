package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.ads.UnifiedNativeAdViewHolder
import com.google.android.gms.ads.formats.UnifiedNativeAd

class FeedItemsAdapter(
    private val viewTypeParam: Int = FEED_VIEW_TYPE,
    private val retryCallback: () -> Unit,
    private val onClickCallback: (v: View, position: Int ,item: FeedItem) -> Unit
)
    : PagedListAdapter<FeedItem, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    var adList = mutableListOf<UnifiedNativeAd>()

    private var networkState: NetworkState? = null

//    private var varitems = mutableListOf<FeedItem>()
    private var rigItemsTotal = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            FEED_VIEW_TYPE -> (holder as FeedsItemViewHolder).bind(getItem(position)!!, position)
            RIG_ITEM_VIEW_TYPE -> (holder as ItemsOfRigViewHolder).bind(getItem(position)!!, position)
            UNIFIED_NATIVE_AD_VIEW_TYPE -> {
                val ad = adList.first()
                (holder as UnifiedNativeAdViewHolder).bind(ad)
                if (adList.isNotEmpty()) { adList.removeAt(0) }
            }
            ERROR_VIEW_TYPE -> (holder as NetworkStateViewHolder).bindTo(
                networkState)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as FeedsItemViewHolder).updateScore(item)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FEED_VIEW_TYPE -> FeedsItemViewHolder.create(parent, onClickCallback)
            RIG_ITEM_VIEW_TYPE -> ItemsOfRigViewHolder.create(parent, onClickCallback)
            ERROR_VIEW_TYPE -> NetworkStateViewHolder.create(parent, retryCallback)
            UNIFIED_NATIVE_AD_VIEW_TYPE -> UnifiedNativeAdViewHolder.create(parent)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            ERROR_VIEW_TYPE
        } else if ((getItem(position)!!.isAd)) {
            UNIFIED_NATIVE_AD_VIEW_TYPE
        } else {
            viewTypeParam
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    fun getItemTotal() : Double {
        var total = 0.00
        currentList?.forEach {
            val toBeAdded = it.price.removePrefix("P").toDouble()
            total = total.plus(toBeAdded)
        }
        return total
    }

    companion object {

        const val ERROR_VIEW_TYPE = 0
        const val UNIFIED_NATIVE_AD_VIEW_TYPE = 1
        const val FEED_VIEW_TYPE = 2
        const val RIG_ITEM_VIEW_TYPE = 3

        private val PAYLOAD_SCORE = Any()
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
                oldItem.id == newItem.id

            override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any? {
                return if (sameExceptScore(oldItem, newItem)) {
                    PAYLOAD_SCORE
                } else {
                    null
                }
            }
        }

        private fun sameExceptScore(oldItem: FeedItem, newItem: FeedItem): Boolean {
            // DON'T do this copy in a real app, it is just convenient here for the demo :)
            // because reddit randomizes scores, we want to pass it as a payload to minimize
            // UI updates between refreshes
//            return oldItem.copy(score = newItem.score) == newItem
            return false
        }
    }
}