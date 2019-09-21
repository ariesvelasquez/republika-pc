package ariesvelasquez.com.republikapc.ui.dashboard.rigs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.RigItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import kotlinx.android.synthetic.main.fragment_rigs.view.*
import timber.log.Timber

class RigsFragment : DashboardFragment() {


    private lateinit var rootView: View

    private var listener: OnFragmentInteractionListener? = null

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
        rootView =  inflater.inflate(R.layout.fragment_rigs, container, false)

        initSwipeToRefresh()
        initAdapter()

        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        Timber.e("OnResume ")
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn() {
        // TEST data
        dashboardViewModel.showRigItems()
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.rigRefreshState.observe(this, Observer {
            rootView.swipeRefreshRigs.isRefreshing = it == NetworkState.LOADING
        })
        rootView.swipeRefreshRigs.setOnRefreshListener {
            dashboardViewModel.refreshRigs()
        }
    }

    private fun initAdapter() {
        val adapter = RigItemsAdapter {
            dashboardViewModel.retryFeeds()
        }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        // Snapper
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rootView.rigList)

        rootView.rigList.layoutManager = linearLayoutManager
        rootView.rigList.adapter = adapter
        dashboardViewModel.rigItems.observe(this, Observer<PagedList<RigItem>> {
            Timber.e("Rigs ViewModel Observer: new items added size: %s", it.size)
            adapter.submitList(it)
        })
        dashboardViewModel.rigNetworkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
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
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            RigsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
