package ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved

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
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import kotlinx.android.synthetic.main.fragment_saved.view.*

class SavedFragment : DashboardFragment() {

    private lateinit var rootView: View
    private lateinit var adapter: SavedItemsAdapter

    private var hasList = false
    private var hasLoadedInitialList = false

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
        initOnClicks()

        return rootView
    }

    private fun initOnClicks() {
        rootView.buttonSignIn.setOnClickListener {
            listener?.showSignUpBottomSheet()
        }

        rootView.buttonSearch.setOnClickListener {
            context!!.launchActivity<SearchActivity> {}
        }

        rootView.buttonGoToFeeds.setOnClickListener {
            listener?.onNavigateToTPCFeeds()
        }
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
//        dashboardViewModel.savedRefreshState.observe( viewLifecycleOwner, Observer {
//            rootView.swipeRefreshSaved.isRefreshing = it == NetworkState.LOADING
//        })
//        rootView.swipeRefreshSaved.setOnRefreshListener {
//            dashboardViewModel.refreshSaved()
//        }
    }

    private fun initAdapter() {
        adapter = SavedItemsAdapter (
            SavedItemsAdapter.SAVED_ITEMS_VIEW_TYPE,
            { dashboardViewModel.refreshSaved() }) { v, pos, item ->

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
            handleListUI(it.snapshot())
//            showHidePlaceholder(show = it.snapshot().isNotEmpty())
        })
        dashboardViewModel.savedNetworkState.observe( viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun handleListUI(it: MutableList<Saved>) {

        if (!mIsUserLoggedIn) {
            rootView.linearLayoutPlaceHolder.visibility = View.GONE
            rootView.savedList.visibility = View.GONE
            rootView.linearLayoutSignIn.visibility = View.VISIBLE
            return
        } else {
            rootView.linearLayoutSignIn.visibility = View.GONE
        }

        if (!hasList && it.isNotEmpty()) {
            // When the list is not empty, and recyclerView is still gone
            this.hasList = true
            showHidePlaceholder(show = false)
        } else if (it.isEmpty() && hasList) {
            // When the list is empty, and recyclerView is still visible
            this.hasList = false
            showHidePlaceholder(show = true)
        } else if (hasList && it.isNotEmpty()) {
            // When the list is not empty, and recyclerView is still visible
            showHidePlaceholder(show = false)
        } else {
            // List is Empty, hasList is false
            showHidePlaceholder(true)
        }
    }

    private fun showHidePlaceholder(show: Boolean) {
        if (show) {
            rootView.savedList.visibility = View.GONE
            rootView.linearLayoutPlaceHolder.visibility = View.VISIBLE
        } else {
            rootView.savedList.visibility = View.VISIBLE
            rootView.linearLayoutPlaceHolder.visibility = View.GONE
        }
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
        fun showSignUpBottomSheet()
        fun onNavigateToTPCFeeds()
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

