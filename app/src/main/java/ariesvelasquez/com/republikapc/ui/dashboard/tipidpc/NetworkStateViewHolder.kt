package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ariesvelasquez.com.republikapc.Const.FAILED_TO_CONNECT_ERROR
import ariesvelasquez.com.republikapc.Const.FIREBASE_ERROR_UNAUTHORIZED
import ariesvelasquez.com.republikapc.Const.NO_CONNECTION_ERROR
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.repository.Status.*
import kotlin.math.sign

class NetworkStateViewHolder(private var view: View,
                             private val retryCallback: () -> Unit)
    : RecyclerView.ViewHolder(view) {
    private val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    private val retry = view.findViewById<Button>(R.id.retry_button)
    private val errorMsg = view.findViewById<TextView>(R.id.error_msg)
    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }
    fun bindTo(networkState: NetworkState?) {

        val userSignedIn = (networkState?.msg != null && !networkState.msg.contains(FIREBASE_ERROR_UNAUTHORIZED))
        val noInternetConnection = (networkState?.msg != null && networkState.msg.contains(NO_CONNECTION_ERROR))
        val failedToConnectException = (networkState?.msg != null && networkState.msg.contains(FAILED_TO_CONNECT_ERROR))

        progressBar.visibility = toVisibility(networkState?.status == RUNNING)
        errorMsg.visibility = toVisibility(networkState?.msg != null)

        retry.visibility = toVisibility(networkState?.status == FAILED && userSignedIn)

        // Override Message
        val errorMessage = when {
            !userSignedIn -> { view.context?.getString(R.string.sign_in_to_unlock_feature) }
            failedToConnectException ||
            noInternetConnection -> { view.context?.getString(R.string.no_internet_connection_error) }
            else -> { networkState?.msg }
        }
        errorMsg.text = errorMessage
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.network_state_item, parent, false)
            return NetworkStateViewHolder(view, retryCallback)
        }

        fun toVisibility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}