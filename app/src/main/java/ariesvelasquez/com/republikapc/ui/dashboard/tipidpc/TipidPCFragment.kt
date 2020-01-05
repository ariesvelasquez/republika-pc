package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import kotlinx.android.synthetic.main.fragment_tipid_pc.view.*
import timber.log.Timber

class TipidPCFragment : DashboardFragment() {

    private var listener: OnTPCFragmentListener? = null

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_tipid_pc, container, false)

        initAdapter()
        initSwipeToRefresh()

        dashboardViewModel.showFeedItems()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (mIsUserLoggedIn and !mIsRigInitialized) {
            dashboardViewModel.showRigs()
        }
    }

    private fun initAdapter() {
        val adapter = FeedItemsAdapter(
            FeedItemsAdapter.FEED_VIEW_TYPE,
            { dashboardViewModel.refreshFeeds() }) { v, pos, feedItem ->
            listener?.onTPCItemClicked(feedItem)
        }


        rootView.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rootView.list.adapter = adapter

        // Init Items
        dashboardViewModel.feedItems.observe( viewLifecycleOwner, Observer<PagedList<FeedItem>> {
            Timber.e("Feeds ViewModel Observer: new items added size: %s", it.size)
            adapter.submitList(it)
        })
        dashboardViewModel.feedsNetworkState.observe( viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString(KEY_SELLER, model.currentSeller())
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.feedsRefreshState.observe( viewLifecycleOwner, Observer {
            rootView.refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
        rootView.refreshSwipe.setOnRefreshListener {
            dashboardViewModel.refreshFeeds()
        }
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn() {

    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTPCFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnTPCFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnTPCFragmentListener {
        fun onTPCItemClicked(feedItem: FeedItem)
    }

    companion object {
        const val KEY_SELLER = "seller_items"
        const val DEFAULT_SELLER = "_feeds"
//        fun intentFor(context: Context, type: RedditPostRepository.Type): Intent {
//            val intent = Intent(context, RedditActivity::class.java)
//            intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
//            return intent
//        }

        @JvmStatic
        fun newInstance() =
            TipidPCFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
