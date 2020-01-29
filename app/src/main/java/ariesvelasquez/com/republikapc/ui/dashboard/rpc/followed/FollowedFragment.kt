package ariesvelasquez.com.republikapc.ui.dashboard.rpc.followed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved.SavedItemsAdapter
import kotlinx.android.synthetic.main.fragment_followed.view.*
import timber.log.Timber

class FollowedFragment : DashboardFragment() {

    private lateinit var rootView: View
    private lateinit var adapter: SavedItemsAdapter

    private var listener: OnFollowedFragmentInteractionListener? = null

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
        super.onCreateView(inflater, container, savedInstanceState)
        rootView =  inflater.inflate(R.layout.fragment_followed, container, false)

        initSwipeToRefresh()
        initAdapter()

        handleFollowedState()

        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        if (mIsUserLoggedIn and !mIsFollowedInitialized) {
            dashboardViewModel.showFollowed()
        }
    }

    override fun onUserLoggedOut() {
//        dashboardViewModel
        dashboardViewModel.cancelFollowed()
    }

    override fun onUserLoggedIn() {
        // TEST data
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.followedRefreshState.observe( viewLifecycleOwner, Observer {
            rootView.swipeRefreshFollowed.isRefreshing = it == NetworkState.LOADING
        })
        rootView.swipeRefreshFollowed.setOnRefreshListener {
            dashboardViewModel.refreshFollowed()
        }
    }

    private fun initAdapter() {
        adapter = SavedItemsAdapter (
            SavedItemsAdapter.FOLLOWED_ITEMS_VIEW_TYPE,
            { dashboardViewModel.refreshFollowed() }) { v, item ->

            listener?.onTPCSellerClicked(item.seller)
        }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.followedList.layoutManager = linearLayoutManager
        rootView.followedList.adapter = adapter
    }

    private fun handleFollowedState() {
        dashboardViewModel.followed.observe( viewLifecycleOwner, Observer<PagedList<Saved>> {
            adapter.submitList(it)
        })
        dashboardViewModel.followedNetworkState.observe( viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFollowedFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFollowedFragmentInteractionListener")
        }
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
    interface OnFollowedFragmentInteractionListener {
        fun onTPCSellerClicked(sellerName: String)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            FollowedFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

