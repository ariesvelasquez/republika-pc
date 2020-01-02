package ariesvelasquez.com.republikapc.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.FeedItemsAdapter
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.onQuerySubmit
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.search_toolbar.*
import timber.log.Timber


class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@SearchActivity)
                    .getSearchRepository()
                @Suppress("UNCHECKED_CAST")
                return SearchViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Focus to search edit text
        initSearchView()
        initSwipeToRefresh()
        initAdapter()
        initOnClicks()
    }

    private fun initSwipeToRefresh() {
        viewModel.refreshState.observe(this, Observer {
            refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
        refreshSwipe.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun initAdapter() {
        val adapter = FeedItemsAdapter(
            FeedItemsAdapter.FEED_VIEW_TYPE,
            { viewModel.retry() }) { v, pos, feedItem ->

            Timber.e("Clicked Feed Item From Search " + feedItem.name)
        }
        searchList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        searchList.adapter = adapter
        viewModel.items.observe(this, Observer<PagedList<FeedItem>> {
            Timber.e("Search ViewModel Observer: new items added size: %s", it.size)
            adapter.submitList(it)
        })
        viewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
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
            Timber.e("asdfadsf $it")
            if (it.isNotEmpty()) {
                if (viewModel.searchItems(it)) {
                    searchList.scrollToPosition(0)
                    (searchList.adapter as? FeedItemsAdapter)?.submitList(null)
                }
            }
        }
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
    }
}
