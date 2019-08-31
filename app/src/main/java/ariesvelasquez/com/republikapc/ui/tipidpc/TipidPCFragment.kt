package ariesvelasquez.com.republikapc.ui.tipidpc

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
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import kotlinx.android.synthetic.main.fragment_tipid_pc.view.*
import timber.log.Timber

class TipidPCFragment : Fragment() {

    private val feedsViewModel: FeedsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(context!!)
                    .getRepository()
                @Suppress("UNCHECKED_CAST")
                return FeedsViewModel(repo) as T
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

        feedsViewModel.showFeedItems()

        return rootView
    }

    private fun initAdapter() {
        val adapter = FeedItemsAdapter {
            feedsViewModel.retry()
        }
        rootView.list.layoutManager = LinearLayoutManager(context)
        rootView.list.adapter = adapter
        feedsViewModel.items.observe(this, Observer<PagedList<FeedItem>> {
            Timber.e("Feeds ViewModel Observer: new items added size: %s", it.size)
            adapter.submitList(it)
        })
        feedsViewModel.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString(KEY_SELLER, feedsViewModel.currentSeller())
    }

    private fun initSwipeToRefresh() {
//        feedsViewModel.refreshState.observe(this, Observer {
//            swipe_refresh.isRefreshing = it == NetworkState.LOADING
//        })
//        swipe_refresh.setOnRefreshListener {
//            model.refresh()
//        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
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
