package ariesvelasquez.com.republikapc.ui.generic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.BR

typealias ItemClickCallback<T> = (item: T, pos: Int) -> Unit

class FirestorePagingDataAdapter<T : Any> (
    @LayoutRes private val itemLayout: Int,
    diffCallback: DiffUtil.ItemCallback<T>,
) : PagingDataAdapter<T, FirestorePagingDataAdapter<T>.FirestorePagingViewHolder<T>>(diffCallback) {

    private lateinit var callbackListener: ItemClickCallback<T>

    override fun onBindViewHolder(holder: FirestorePagingViewHolder<T>, position: Int) {
        val item = getItem(position) ?: return
        holder.bindProduct(item, position)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FirestorePagingViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, itemLayout, parent, false)
        return FirestorePagingViewHolder(
            callbackListener,
            binding
        )
    }

    fun setOnClickCallback(callbackListener: ItemClickCallback<T>) {
        this.callbackListener = callbackListener
    }

    inner class FirestorePagingViewHolder<T : Any>(
        private val callbackListener: ItemClickCallback<T>,
        private val dataBinding: ViewDataBinding
    ) : RecyclerView.ViewHolder(dataBinding.root) {

        fun bindProduct(item: T, position: Int) {
            dataBinding.root.setOnClickListener {
                callbackListener.invoke(item, position)
            }
            dataBinding.setVariable(BR.item, item)
            dataBinding.setVariable(BR.pos, position)
            dataBinding.executePendingBindings()
        }
    }
}

