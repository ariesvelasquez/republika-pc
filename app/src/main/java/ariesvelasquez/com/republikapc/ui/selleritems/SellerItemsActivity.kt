package ariesvelasquez.com.republikapc.ui.selleritems

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.BaseDashboardActivity
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.FeedItemsAdapter
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_seller_items.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.toolbar
import kotlinx.android.synthetic.main.toolbar.view.*
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber

class SellerItemsActivity : BaseDashboardActivity() {

    private lateinit var sellerItemsAdapter: FeedItemsAdapter

    private lateinit var mSellerName: String
    private var mListSize = 0
    private var mItemNetworkState = NetworkState.LOADING

    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@SellerItemsActivity)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_items)

        // Get Intent Data
        mSellerName = intent.getStringExtra(SELLER_NAME_REFERENCE)

        // Set Title
        toolbar.textViewToolbarTitle.text = mSellerName
        toolbar.textViewToolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.colorDarkGray))

        initOnClicks()

        handleRigState()

        setupSellerItemList()

        handleAddItemToRigCreationState()
        handleSaveItemState()

        dashboardViewModel.showSellerItems(mSellerName)
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
    }

    private fun setupSellerItemList() {
        sellerItemsAdapter = FeedItemsAdapter(
            FeedItemsAdapter.SELLER_ITEM_VIEW_TYPE,
            { dashboardViewModel.refreshSellerItems() }
        ) { v, pos, feedItem ->

            Timber.e("clicked Seller Item link_id " + feedItem.linkId)

            onTPCItemClicked(feedItem, false)
        }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        FastScrollerBuilder(recyclerViewSellerItemList)
            .build()

        recyclerViewSellerItemList.layoutManager = linearLayoutManager
        recyclerViewSellerItemList.adapter = sellerItemsAdapter

        refreshSwipe.setOnRefreshListener {
            dashboardViewModel.refreshSellerItems()
        }

        // Init Items
        dashboardViewModel.sellerItems.observe( this, Observer<PagedList<FeedItem>> {
            sellerItemsAdapter.submitList(it)
        })

        dashboardViewModel.sellerItemsRefreshState.observe( this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })

        dashboardViewModel.sellerItemsNetworkState.observe( this, Observer {
            sellerItemsAdapter.setNetworkState(it)
        })
    }

    private fun handleRigState() {
        // init rigs
        viewModel.showRigs()
    }

    private fun startLoading() {
        progressBarLoader.visibility = View.VISIBLE
    }

    private fun finishedLoading() {
        Handler().postDelayed({
            progressBarLoader.visibility = View.GONE
        }, 1000)
    }

    override fun handleAddItemToRigCreationState() {
        viewModel.addItemToRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    finishedLoading()
                    showSnackBar(getString(R.string.added_to_rig_success))
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun handleSaveItemState() {
        viewModel.saveItemNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    finishedLoading()
                    showSnackBar(getString(R.string.item_saved))
                    viewModel.saveItemNetworkState.postValue(NetworkState.LOADING)
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })

        // Saved Item Deleted
        viewModel.deleteSavedItemNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    savedItemBottomSheet.dismiss()
                    finishedLoading()
                    showSnackBar(getString(R.string.item_deleted))
                    viewModel.deleteSavedItemNetworkState.postValue(NetworkState.LOADING)
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    companion object {
        const val SELLER_NAME_REFERENCE = "seller_name_reference"
    }

    private fun showSnackBar(message: String) {
        val successDialog = AlertDialog.Builder(this)
            .setMessage("New Rig has been created")
            .setOnCancelListener {  }

        recyclerViewSellerItemList.snack(message) {

        }
    }
}
