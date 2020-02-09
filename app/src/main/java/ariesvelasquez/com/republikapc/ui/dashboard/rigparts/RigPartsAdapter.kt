package ariesvelasquez.com.republikapc.ui.dashboard.rigparts

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.RigPartViewHolder
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.NetworkStateViewHolder


class RigPartsAdapter(
    private val viewTypeParam: Int = RIG_PART_VIEW_TYPE,
    private val retryCallback: () -> Unit,
    private val onClickCallback: (v: View, position: Int ,item: RigPart) -> Unit
)
    : PagedListAdapter<RigPart, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    private var networkState: NetworkState? = null

    var currentRecycledItem: Int = 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        currentRecycledItem = position

        when (getItemViewType(position)) {
            RIG_PART_VIEW_TYPE -> (holder as RigPartViewHolder).bind(getItem(position)!!, position)
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
//                RIG_ITEM_VIEW_TYPE -> (holder as RigPartViewHolder).bind(getItem(position)!!, position)
                ERROR_VIEW_TYPE -> (holder as NetworkStateViewHolder).bindTo(
                    networkState)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RIG_PART_VIEW_TYPE -> RigPartViewHolder.create(parent, onClickCallback)
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
        const val RIG_PART_VIEW_TYPE = 1

        private val PAYLOAD_SCORE = Any()
        val POST_COMPARATOR = object : DiffUtil.ItemCallback<RigPart>() {
            override fun areContentsTheSame(oldItem: RigPart, newItem: RigPart): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: RigPart, newItem: RigPart): Boolean =
                oldItem.docId == newItem.docId

            override fun getChangePayload(oldItem: RigPart, newItem: RigPart): Any? {
//                return if (sameExceptScore(oldItem, newItem)) {
//                    PAYLOAD_SCORE
//                } else {
                    return null
//                }
            }
        }

        private fun sameExceptScore(oldItem: RigPart, newItem: RigPart): Boolean {
            // DON'T do this copy in a real app, it is just convenient here for the demo :)
            // because reddit randomizes scores, we want to pass it as a payload to minimize
            // UI updates between refreshes
//            return oldItem.copy(score = newItem.score) == newItem
            return false
        }
    }
}