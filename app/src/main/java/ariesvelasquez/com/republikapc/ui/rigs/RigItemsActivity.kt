package ariesvelasquez.com.republikapc.ui.rigs

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.BaseActivity
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.FeedItemsAdapter
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.Tools
import ariesvelasquez.com.republikapc.utils.extensions.action
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_rig_items.*
import kotlinx.android.synthetic.main.rig_items_total_bottom_sheet.*
import kotlinx.android.synthetic.main.search_toolbar.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class RigItemsActivity : BaseActivity() {

    private lateinit var rigItemsAdapter: FeedItemsAdapter

    private lateinit var rigItemReference: Rig

    private var rigItemsTotal = 0

    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@RigItemsActivity)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rig_items)

        // Get Intent Data
        rigItemReference =
            Gson().fromJson(intent.getStringExtra(RIG_ITEM_REFERENCE), Rig::class.java)

        initOnClicks()

        setupRigList()

        initRigItemList()
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }

    }

    private fun setupRigList() {
        rigItemsAdapter = FeedItemsAdapter(
            FeedItemsAdapter.RIG_ITEM_VIEW_TYPE,
            { dashboardViewModel.refreshRigItems() }
            ) { v, pos, item ->


            // OnItemClick: Validate if the viewer is also the owner of the rig
            var isOwner = false
            isOwner = mFirebaseUser?.uid.equals(rigItemReference.ownerId)

            showItemDetails(isOwner, item)
        }
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        rigItemList.layoutManager = linearLayoutManager
        rigItemList.adapter = rigItemsAdapter

        refreshSwipe.setOnRefreshListener {
            dashboardViewModel.refreshRigItems()
        }
    }

    private fun showItemDetails(owner: Boolean, item: FeedItem) {

        if (!owner) return

        linearLayoutTotalBottomSheet.snack(item.name) {
            action(
                getString(R.string.delete),
                ContextCompat.getColor(context, R.color.colorBlue)) {
                Timber.e("CLicked " + item.name)
                deleteRigItem(item)
            }
        }
    }

    private fun deleteRigItem(item: FeedItem) {
        dashboardViewModel.deleteRigItem(rigItemReference.id, item.docId)
    }

    private fun initRigItemList() {
        dashboardViewModel.getRigItems(rigItemReference.id)

        dashboardViewModel.rigItems.observe(this, Observer<PagedList<FeedItem>> {
            rigItemsAdapter.submitList(it)
        })
        dashboardViewModel.rigItemsNetworkState.observe(this, Observer {
            rigItemsAdapter.setNetworkState(it)
            setTotalNetworkState(it)
        })
        dashboardViewModel.deleteRigItemNetworkState.observe(this, Observer {
            setItemDeleteNetworkState(it)
        })
        dashboardViewModel.rigItemsRefreshState.observe(this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
    }

    private fun setItemDeleteNetworkState(it: NetworkState?) {
        when (it) {
            NetworkState.LOADING -> {
                progressBarLoader.progress = 10
            }
            NetworkState.LOADED -> {
                progressBarLoader.progress = 100
                linearLayoutTotalBottomSheet.snack("Item deleted") {}
                Handler().postDelayed({
                    progressBarLoader.progress = 0
                }, 1000)

                RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
            }
            NetworkState.LOADING -> {}
            else -> {
                // Show Error Prompt
                Toast.makeText(this, it?.msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTotalNetworkState(it: NetworkState?) {

        when (it) {
            NetworkState.LOADING -> {
                textViewTotal.text = getString(R.string.updating)
            }
            NetworkState.LOADED -> {
                val numberFormattedTotal =
                    Tools().numberFormatter?.format(rigItemsAdapter.getItemTotal().toInt())
                textViewTotal.text = "$numberFormattedTotal.00"
            }
        }
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn(user: FirebaseUser) {

    }

    companion object {
        const val RIG_ITEM_REFERENCE = "rig_item_reference"
    }
}
