package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import me.zhanghai.android.fastscroll.PopupTextProvider
import timber.log.Timber
import androidx.annotation.NonNull
import ariesvelasquez.com.republikapc.GlobalFlags
import java.util.*


class FeedItemsAdapter(
    private val viewTypeParam: Int = FEED_VIEW_TYPE,
    private val retryCallback: () -> Unit,
    private val onClickCallback: (v: View, position: Int ,item: FeedItem) -> Unit
)
    : PagedListAdapter<FeedItem, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    private var networkState: NetworkState? = null

    var currentRecycledItem: Int = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        currentRecycledItem = position

        when (getItemViewType(position)) {
            FEED_VIEW_TYPE -> { (holder as FeedsItemViewHolder).bind(getItem(position)!!, position) }
            RIG_ITEM_VIEW_TYPE -> (holder as ItemsOfRigViewHolder).bind(getItem(position)!!, position)
            SELLER_ITEM_VIEW_TYPE -> (holder as TipidPCSellerItemsViewHolder).bind(getItem(position)!!, position)
            SELLER_VIEW_TYPE -> (holder as TipidPCSellerViewHolder).bind(getItem(position)!!, position)
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
            when (getItemViewType(position)) {
//                FEED_VIEW_TYPE -> (holder as FeedsItemViewHolder).bind(getItem(position)!!, position)
//                RIG_ITEM_VIEW_TYPE -> (holder as ItemsOfRigViewHolder).bind(getItem(position)!!, position)
                SELLER_ITEM_VIEW_TYPE -> (holder as TipidPCSellerItemsViewHolder).updatePrice(item)
                ERROR_VIEW_TYPE -> (holder as NetworkStateViewHolder).bindTo(
                    networkState)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FEED_VIEW_TYPE -> FeedsItemViewHolder.create(parent, onClickCallback)
            RIG_ITEM_VIEW_TYPE -> ItemsOfRigViewHolder.create(parent, onClickCallback)
            SELLER_ITEM_VIEW_TYPE -> TipidPCSellerItemsViewHolder.create(parent, onClickCallback)
            SELLER_VIEW_TYPE -> TipidPCSellerViewHolder.create(parent, onClickCallback)
            ERROR_VIEW_TYPE -> NetworkStateViewHolder.create(parent, retryCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            ERROR_VIEW_TYPE
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
            val toBeAdded = it.price
                .replace("PHP", "")
                .replace("HP", "")
                .replace("P", "")
                .toDouble()


            total = total.plus(toBeAdded)
        }
        return total
    }

    companion object {

        const val ERROR_VIEW_TYPE = 0
        const val FEED_VIEW_TYPE = 1
        const val RIG_ITEM_VIEW_TYPE = 2
        const val SELLER_ITEM_VIEW_TYPE = 3
        const val SELLER_VIEW_TYPE = 4

        private val PAYLOAD_SCORE = Any()
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
                oldItem.id == newItem.id && oldItem.isFeed == newItem.isFeed

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