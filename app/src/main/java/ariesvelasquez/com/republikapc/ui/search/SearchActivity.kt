package ariesvelasquez.com.republikapc.ui.search

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.BaseDashboardActivity
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.FeedItemsAdapter
import ariesvelasquez.com.republikapc.utils.extensions.onQuerySubmit
import ariesvelasquez.com.republikapc.utils.extensions.snack
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.search_toolbar.*
import timber.log.Timber


class SearchActivity : BaseDashboardActivity() {

    private lateinit var searchItemsAdapter: FeedItemsAdapter
    private lateinit var searchSellerAdapter: FeedItemsAdapter

    private var showSeller = true

    private var mIsRigInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Focus to search edit text
        initSearchView()

        handleRigState()

        initItemsAdapter()
        handleSearchItemsState()

        initSellersAdapter()
        handleSearchSellersState()

        handleAddItemToRigCreationState()
        handleSaveItemState()

        initOnClicks()
    }


    private fun handleRigState() {
        // init rigs
        viewModel.showRigs()
    }

    private fun handleSearchItemsState() {
        viewModel.searchNetworkState.observe(this, Observer {
            searchItemsAdapter.setNetworkState(it)
        })

        viewModel.searchRefreshNetworkState.observe(this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
    }

    private fun handleSearchSellersState() {
        viewModel.searchedSellersNetworkState.observe(this, Observer {
            searchSellerAdapter.setNetworkState(it)
            if (it == NetworkState.LOADING) {
                linearLayoutSellers.visibility = View.GONE
            } else {
                linearLayoutSellers.visibility = View.VISIBLE
            }
        })

        viewModel.searchedSellersRefreshNetworkState.observe(this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
            if (it == NetworkState.LOADING) {
                linearLayoutSellers.visibility = View.GONE
            } else {
                linearLayoutSellers.visibility = View.VISIBLE
            }
        })
    }

    private fun initItemsAdapter() {
        searchItemsAdapter = FeedItemsAdapter(
            FeedItemsAdapter.FEED_VIEW_TYPE,
            { viewModel.retrySearch() }) { v, pos, feedItem ->

            onTPCItemClicked(feedItem)
        }
        recyclerViewSearch.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerViewSearch.adapter = searchItemsAdapter

        // Listen for Scroll
        recyclerViewSearch.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (searchItemsAdapter.currentRecycledItem > 20) {
                    buttonBackToTop.visibility = View.VISIBLE
                } else {
                    buttonBackToTop.visibility = View.GONE
                }
            }
        })

        viewModel.searchItems.observe(this, Observer<PagedList<FeedItem>> {
            textViewItemsLabel.visibility = View.VISIBLE
            searchItemsAdapter.submitList(it)
        })
    }

    private fun initSellersAdapter() {
        searchSellerAdapter = FeedItemsAdapter(
            FeedItemsAdapter.SELLER_VIEW_TYPE,
            { viewModel.retrySearchedSellers() }) { v, pos, feedItem ->

//            onTPCItemClicked(feedItem)

            onTPCSellerClicked(feedItem.seller)

            Timber.e("Clicked TPC Seller From Search " + feedItem.name)
        }

        recyclerViewSeller.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        recyclerViewSeller.adapter = searchSellerAdapter

        viewModel.searchedSellers.observe(this, Observer<PagedList<FeedItem>> {
            textViewSellerLabel.visibility = View.VISIBLE
            searchSellerAdapter.submitList(it)
        })
    }

    private fun initSearchView() {
        val searchViewEditText = editTextSearch.findViewById<EditText>(
            resources.getIdentifier("android:id/search_src_text", null, null)
        )
        searchViewEditText.setTextColor((ContextCompat.getColor(this, R.color.text_helper_dark)))
        searchViewEditText.setHintTextColor((ContextCompat.getColor(this, R.color.colorLightGray)))

        editTextSearch.requestFocus()
        editTextSearch.onQuerySubmit {
            // Handle Submit change
            if (it.isNotEmpty()) {

                // Trigger Search TPC User API
                if (viewModel.searchSellers(it)) {
                    recyclerViewSearch.scrollToPosition(0)
                    (recyclerViewSeller.adapter as? FeedItemsAdapter)?.submitList(null)
                }

                // Trigger Search TPC For Sale Items API
                if (viewModel.searchItems(it)) {
                    recyclerViewSearch.scrollToPosition(0)
                    (recyclerViewSearch.adapter as? FeedItemsAdapter)?.submitList(null)
                    editTextSearch.clearFocus()
                }
            }
        }
    }

    private fun hideLabels() {
        textViewSellerLabel.visibility = View.GONE
        textViewItemsLabel.visibility = View.GONE
    }

    private fun initOnClicks() {
        refreshSwipe.setOnRefreshListener {
            viewModel.refreshSearch()
            viewModel.refreshSearchedSellers()
        }

        backButton.setOnClickListener { onBackPressed() }

        linearLayoutSellers.setOnClickListener { minimizeMaximizeSellerList() }
        buttonShowHideSellers.setOnClickListener { minimizeMaximizeSellerList() }
        textViewSellerLabel.setOnClickListener { minimizeMaximizeSellerList() }

        buttonBackToTop.setOnClickListener {
            recyclerViewSearch.layoutManager!!.smoothScrollToPosition(recyclerViewSearch, null, 0)
        }
    }

    private fun minimizeMaximizeSellerList() {
        showSeller = !showSeller

        when (showSeller) {
            true -> {
                // Maximize
                buttonShowHideSellers.setImageDrawable(getDrawable(R.drawable.ic_window_minimize))
                recyclerViewSeller.visibility = View.VISIBLE
            }
            else -> {
                // Minimize
                buttonShowHideSellers.setImageDrawable(getDrawable(R.drawable.ic_window_maximize))
                recyclerViewSeller.visibility = View.GONE
            }
        }
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

    private fun startLoading() {
        progressBarLoader.visibility = View.VISIBLE
    }

    private fun finishedLoading() {

        Handler().postDelayed({
            progressBarLoader.visibility = View.GONE
        }, 1000)
    }

    private fun showSnackBar(message: String) {
        val successDialog = AlertDialog.Builder(this)
            .setMessage("New Rig has been created")
            .setOnCancelListener {  }

        recyclerViewSearch.snack(message) {

        }
    }
}
