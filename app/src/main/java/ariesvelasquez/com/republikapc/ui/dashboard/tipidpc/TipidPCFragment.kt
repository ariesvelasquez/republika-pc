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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest

class TipidPCFragment : DashboardFragment() {

    private var listener: OnTPCFragmentListener? = null

    private lateinit var rootView: View

    // The number of native ads to load and display.
    val NUMBER_OF_ADS = 5

    // The AdLoader used to load ads.
    private var adLoader: AdLoader? = null

    // List of native ads that have been successfully loaded.
    private val mNativeAds = mutableListOf<UnifiedNativeAd>()

    private lateinit var feedAdapter: FeedItemsAdapter

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

        loadNativeAds()

        dashboardViewModel.showFeedItems()

        return rootView
    }

    private fun loadNativeAds() {

        val builder = AdLoader.Builder(context!!, getString(R.string.test_native_advanced_ad_unit))
        adLoader = builder.forUnifiedNativeAd { unifiedNativeAd ->
            // A native ad loaded successfully, check if the ad loader has finished loading
            // and if so, insert the ads into the list.
            mNativeAds.add(unifiedNativeAd)
            Timber.e("builder.forUnifiedNativeAd loading")

            if (!adLoader?.isLoading!!) {
                Timber.e("builder.forUnifiedNativeAd finished loading")

                feedAdapter.adList.add(unifiedNativeAd)
            }
        }.withAdListener (
            object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    // A native ad failed to load, check if the ad loader has finished loading
                    // and if so, insert the ads into the list.
                    Timber.e("The previous native ad failed to load. Attempting to" + " load another.")
                    if (!adLoader!!.isLoading) {

                    }
                }
            }).build()

        adLoader!!.loadAds(AdRequest.Builder().build(), NUMBER_OF_ADS)
    }

    private fun initAdapter() {
        feedAdapter = FeedItemsAdapter(
            FeedItemsAdapter.FEED_VIEW_TYPE,
            { dashboardViewModel.refreshFeeds() }) { v, pos, feedItem ->
            listener?.onTPCItemClicked(feedItem)
        }

        rootView.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rootView.list.adapter = feedAdapter

        // Init Items
        dashboardViewModel.feedItems.observe(viewLifecycleOwner, Observer<PagedList<FeedItem>> {
            Timber.e("Feeds ViewModel Observer: new items added size: %s", it.size)
            feedAdapter.submitList(it)
        })
        dashboardViewModel.feedsNetworkState.observe(viewLifecycleOwner, Observer {
            // Trigger Ad Loader when it loads more item
            when (it) {
                NetworkState.LOADING -> {
                    Timber.e("Feed Loading")
                    loadNativeAds()
                }
            }
            feedAdapter.setNetworkState(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString(KEY_SELLER, model.currentSeller())
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.feedsRefreshState.observe(viewLifecycleOwner, Observer {
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
