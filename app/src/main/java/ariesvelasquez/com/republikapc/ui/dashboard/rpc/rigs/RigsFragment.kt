package ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import ariesvelasquez.com.republikapc.ui.rigs.RigItemsActivity
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_rigs.view.*
import timber.log.Timber

@ExperimentalPagingApi
class RigsFragment : DashboardFragment() {

    private lateinit var rootView: View
    private lateinit var adapter: RigItemsAdapter

    private var listener: OnRigFragmentInteractionListener? = null

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

//        initSwipeToRefresh()
//        initAdapter()



        return rootView
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()
        if (mIsUserLoggedIn and !mIsRigInitialized) {
//            dashboardViewModel.showRigs()
//            initRigList()
        }
    }

    override fun onUserLoggedOut() {
//        dashboardViewModel
        dashboardViewModel.cancelRigs()
    }

    override fun onUserLoggedIn() {
        // TEST data
//        dashboardViewModel.showRigs()
    }

    private fun initSwipeToRefresh() {
        dashboardViewModel.rigRefreshState.observe(viewLifecycleOwner, Observer {
            rootView.swipeRefreshRigs.isRefreshing = it == NetworkState.LOADING
        })
        rootView.swipeRefreshRigs.setOnRefreshListener {
            dashboardViewModel.refreshRigs()
        }
    }

    private fun initAdapter() {
        adapter = RigItemsAdapter (
            RigItemsAdapter.SHOW_RIGS_VIEW_TYPE,
            { dashboardViewModel.refreshRigs() }) { v, item ->
            when (v.id) {
                R.id.title -> Timber.e("Title")
                R.id.textViewViewAllParts,
                R.id.imageViewViewAllParts,
                R.id.linearLayoutParts ->  { launchItemsActivity(item) }
                R.id.imageViewOption -> {
                    listener?.onRigMenuClicked(item)
                }
                else -> Timber.e("item")
            }
        }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        // Snapper
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rootView.rigList)

        rootView.rigList.layoutManager = linearLayoutManager
        rootView.rigList.adapter = adapter
        rootView.indefiniteIndicatorRigList.attachToRecyclerView(rootView.rigList)
    }

    private fun launchItemsActivity(item: Rig) {
        val rawRigItem = Gson().toJson(item)

        activity?.launchActivity<RigItemsActivity> {
            putExtra(RigItemsActivity.RIG_ITEM_REFERENCE, rawRigItem)
        }
    }

    private fun initRigList() {
        dashboardViewModel.rigs.observe(viewLifecycleOwner, Observer<PagedList<Rig>> {
            adapter.submitList(it)

            Timber.e("RIGGGGZZZZ Items count " + adapter.itemCount)
        })
        dashboardViewModel.rigNetworkState.observe(viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRigFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnRigFragmentInteractionListener")
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
    interface OnRigFragmentInteractionListener {
        fun onRigMenuClicked(rig: Rig)
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
