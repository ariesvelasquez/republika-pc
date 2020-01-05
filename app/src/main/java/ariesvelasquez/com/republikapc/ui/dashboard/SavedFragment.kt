package ariesvelasquez.com.republikapc.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved.SavedItemsAdapter
import kotlinx.android.synthetic.main.fragment_saved.view.*

class SavedFragment : DashboardFragment() {

    private lateinit var rootView: View
    private lateinit var adapter: SavedItemsAdapter

    private var listener: OnSavedFragmentInteractionListener? = null

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
        rootView =  inflater.inflate(R.layout.fragment_saved, container, false)

        initSwipeToRefresh()
        initAdapter()

        initSavedList()

        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        if (mIsUserLoggedIn and !mIsSavedInitialized) {
            dashboardViewModel.showSaved()
        }
    }

    override fun onUserLoggedOut() {
//        dashboardViewModel
        dashboardViewModel.cancelSaved()
    }

    override fun onUserLoggedIn() {
        // TEST data
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.savedRefreshState.observe( viewLifecycleOwner, Observer {
            rootView.swipeRefreshSaved.isRefreshing = it == NetworkState.LOADING
        })
        rootView.swipeRefreshSaved.setOnRefreshListener {
            dashboardViewModel.refreshSaved()
        }
    }

    private fun initAdapter() {
        adapter = SavedItemsAdapter (
            SavedItemsAdapter.SAVED_ITEMS_VIEW_TYPE,
            { dashboardViewModel.refreshSaved() }) { v, item ->

            listener?.onSavedItemClicked(item)
        }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.savedList.layoutManager = linearLayoutManager
        rootView.savedList.adapter = adapter
    }

    private fun initSavedList() {
        dashboardViewModel.saved.observe( viewLifecycleOwner, Observer<PagedList<Saved>> {
            adapter.submitList(it)
        })
        dashboardViewModel.savedNetworkState.observe( viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSavedFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSavedFragmentInteractionListener")
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
    interface OnSavedFragmentInteractionListener {
        fun onSavedItemClicked(saved: Saved)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            SavedFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}

