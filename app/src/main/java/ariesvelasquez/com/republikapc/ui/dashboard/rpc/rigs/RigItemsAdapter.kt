package ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.NetworkStateItemViewHolder

class RigItemsAdapter(
    private val viewTypeParam: Int = SHOW_RIGS_VIEW_TYPE,
    private val retryCallback: () -> Unit,
    private val onClickCallback: (v: View, item: Rig) -> Unit
) : PagedListAdapter<Rig, RecyclerView.ViewHolder>(POST_COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            SHOW_RIGS_VIEW_TYPE -> (holder as RigItemViewHolder).bind(getItem(position))
            ADD_TO_RIG_VIEW_TYPE -> (holder as AddToRigItemViewHolder).bind(getItem(position))
            ERROR_VIEW_TYPE -> (holder as NetworkStateItemViewHolder).bindTo(
                networkState
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            (holder as RigItemViewHolder).updateScore(item)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SHOW_RIGS_VIEW_TYPE -> RigItemViewHolder.create(parent, onClickCallback)
            ADD_TO_RIG_VIEW_TYPE -> AddToRigItemViewHolder.create(parent, onClickCallback)
            ERROR_VIEW_TYPE -> NetworkStateItemViewHolder.create(parent, retryCallback)
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
        const val SHOW_RIGS_VIEW_TYPE = 1
        const val ADD_TO_RIG_VIEW_TYPE = 2

        private val PAYLOAD_SCORE = Any()

        val POST_COMPARATOR = object : DiffUtil.ItemCallback<Rig>() {
            override fun areContentsTheSame(old: Rig, aNew: Rig): Boolean =
                old == aNew

            override fun areItemsTheSame(old: Rig, aNew: Rig): Boolean =
                old.name == aNew.name

            override fun getChangePayload(old: Rig, aNew: Rig): Any? {
                return if (sameExceptScore(old, aNew)) {
                    PAYLOAD_SCORE
                } else {
                    null
                }
            }
        }

        private fun sameExceptScore(old: Rig, aNew: Rig): Boolean {
            // DON'T do this copy in a real app, it is just convenient here for the demo :)
            // because reddit randomizes scores, we want to pass it as a payload to minimize
            // UI updates between refreshes
//            return old.copy(score = aNew.score) == aNew
            return false
        }
    }
}