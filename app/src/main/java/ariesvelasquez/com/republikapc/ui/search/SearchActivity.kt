package ariesvelasquez.com.republikapc.ui.search

import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.BaseDashboardActivity
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.FeedItemsAdapter
import ariesvelasquez.com.republikapc.utils.extensions.onQuerySubmit
import ariesvelasquez.com.republikapc.utils.extensions.snack
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.search_toolbar.*
import timber.log.Timber


class SearchActivity : BaseDashboardActivity() {

    private lateinit var searchAdapter: FeedItemsAdapter

    private var mIsRigInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Focus to search edit text
        initSearchView()

        handleRigState()

        initAdapter()
        handleSearchState()
        handleRefreshSearchState()

        handleAddItemToRigCreationState()
        handleSaveItemState()

        initOnClicks()
    }

    private fun handleRigState() {
        // init rigs
        viewModel.showRigs()
    }

    private fun handleSearchState() {
        viewModel.searchNetworkState.observe(this, Observer {
            searchAdapter.setNetworkState(it)
        })
    }

    private fun handleRefreshSearchState() {
        viewModel.searchRefreshNetworkState.observe(this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
        refreshSwipe.setOnRefreshListener {
            viewModel.refreshSearch()
        }
    }

    private fun initAdapter() {
        searchAdapter = FeedItemsAdapter(
            FeedItemsAdapter.FEED_VIEW_TYPE,
            { viewModel.retrySearch() }) { v, pos, feedItem ->

            onTPCItemClicked(feedItem)

            Timber.e("Clicked Feed Item From Search " + feedItem.name)
        }
        recyclerViewSearch.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerViewSearch.adapter = searchAdapter
        viewModel.searchItems.observe(this, Observer<PagedList<FeedItem>> {
            searchAdapter.submitList(it)
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
                if (viewModel.searchItems(it)) {
                    recyclerViewSearch.scrollToPosition(0)
                    (recyclerViewSearch.adapter as? FeedItemsAdapter)?.submitList(null)
                    editTextSearch.clearFocus()
                }
            }
        }
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
    }

    override fun handleAddItemToRigCreationState() {
        viewModel.addItemToRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
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
        progressBarLoader.progress = 70
    }

    private fun finishedLoading() {
        progressBarLoader.progress = 100

        Handler().postDelayed({
            progressBarLoader.progress = 0
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
