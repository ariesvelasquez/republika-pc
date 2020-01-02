package ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.NetworkStateViewHolder

class SavedItemsAdapter(
    private val viewTypeParam: Int = SAVED_ITEMS_VIEW_TYPE,
    private val retryCallback: () -> Unit,
    private val onClickCallback: (v: View, item: Saved) -> Unit
) : PagedListAdapter<Saved, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            SAVED_ITEMS_VIEW_TYPE -> (holder as SavedItemViewHolder).bind(getItem(position))
            ERROR_VIEW_TYPE -> (holder as NetworkStateViewHolder).bindTo(
                networkState
            )
        }
    }

//    override fun onBindViewHolder(
//        holder: RecyclerView.ViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//        if (payloads.isNotEmpty()) {
//            val item = getItem(position)
//            (holder as RigItemViewHolder).updateScore(item)
//        } else {
//            onBindViewHolder(holder, position)
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SAVED_ITEMS_VIEW_TYPE -> SavedItemViewHolder.create(parent, onClickCallback)
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

    companion object {

        const val ERROR_VIEW_TYPE = 0
        const val SAVED_ITEMS_VIEW_TYPE = 1

        private val PAYLOAD_SCORE = Any()

        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Saved>() {
            override fun areContentsTheSame(old: Saved, aNew: Saved): Boolean =
                old == aNew

            override fun areItemsTheSame(old: Saved, aNew: Saved): Boolean =
                old.name == aNew.name

            override fun getChangePayload(old: Saved, aNew: Saved): Any? {
                return if (sameExceptScore(old, aNew)) {
                    PAYLOAD_SCORE
                } else {
                    null
                }
            }
        }

        private fun sameExceptScore(old: Saved, aNew: Saved): Boolean {
            // DON'T do this copy in a real app, it is just convenient here for the demo :)
            // because reddit randomizes scores, we want to pass it as a payload to minimize
            // UI updates between refreshes
//            return old.copy(score = aNew.score) == aNew
            return false
        }
    }
}