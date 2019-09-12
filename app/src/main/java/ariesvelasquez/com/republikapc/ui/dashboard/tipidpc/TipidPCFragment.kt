package ariesvelasquez.com.republikapc.ui.dashboard.tipidpc

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import kotlinx.android.synthetic.main.fragment_tipid_pc.*
import kotlinx.android.synthetic.main.fragment_tipid_pc.view.*
import kotlinx.android.synthetic.main.fragment_tipid_pc.view.refreshSwipe
import timber.log.Timber

class TipidPCFragment : Fragment() {

    private val model: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(context!!)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    private var listener: OnFragmentInteractionListener? = null

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
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_tipid_pc, container, false)

        initAdapter()
        initSwipeToRefresh()

        model.showFeedItems()

        return rootView
    }

    private fun initAdapter() {
        val adapter = FeedItemsAdapter {
            model.retry()
        }
        rootView.list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        rootView.list.adapter = adapter
        model.items.observe(this, Observer<PagedList<FeedItem>> {
            Timber.e("Feeds ViewModel Observer: new items added size: %s", it.size)
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString(KEY_SELLER, model.currentSeller())
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            rootView.refreshSwipe.isRefreshing = it == NetworkState.LOADING
        })
        rootView.refreshSwipe.setOnRefreshListener {
            model.refresh()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
//            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
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
