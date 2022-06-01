package ariesvelasquez.com.republikapc.ui.dashboard.rpc.followed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.seller.SellerBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.generic.FirestorePagingDataAdapter
import ariesvelasquez.com.republikapc.ui.selleritems.SellerItemsActivity
import ariesvelasquez.com.republikapc.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_followed.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FollowedFragment : DashboardFragment(),
    SellerBottomSheetFragment.SellerBottomSheetFragmentListener {

    private lateinit var rootView: View
    private lateinit var adapter: FirestorePagingDataAdapter<Saved>

    private var listener: OnFollowedFragmentInteractionListener? = null

    private val viewModel by viewModels<FollowedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_followed, container, false)

        initSwipeToRefresh()
        initAdapter()
        handleUIState(false)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        initViewModel()
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            if (mIsUserLoggedIn and !mIsFollowedInitialized) {
                mIsFollowedInitialized = true
                viewModel.followedPagedList.collectLatest { adapter.submitData(it) }
            }
            viewModel.unfollowTask().observe(viewLifecycleOwner) {
                when (it) {
                    is ResultState.Success -> {}
                    is ResultState.Error -> {
                        rootView.snack(
                            it.getErrorIfExists()?.message ?: getString(R.string.unfollow_failed)
                        )
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onUserLoggedOut() {
//        dashboardViewModel
//        dashboardViewModel.cancelFollowed()
    }

    override fun onUserLoggedIn() {
        // TEST data
    }

    private fun initSwipeToRefresh() {
        rootView.swipeRefreshFollowed.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun initAdapter() {
        adapter = FirestorePagingDataAdapter(
            R.layout.item_recycler_view_followed,
            DiffCallback()
        )

        adapter.setOnClickCallback { item, pos ->
            item.seller?.let {
                val sellerBottomSheet = SellerBottomSheetFragment.newInstance(it, pos)
                sellerBottomSheet.setListener(this)
                sellerBottomSheet.show(childFragmentManager, sellerBottomSheet.TAG)
            }
        }

        adapter.addLoadStateListener {
            rootView.swipeRefreshFollowed.isRefreshing = it.sourceRefreshState()
            if (it.finishedAppend() || it.finishedPrepend()) {
                handleUIState(adapter.itemCount > 0)
            } else {
                handleUIState(false)
            }
        }

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.followedList.layoutManager = linearLayoutManager
        rootView.followedList.adapter = adapter
    }

    private fun handleUIState(hasItem: Boolean = false) {
        rootView.apply {
            followedList.visibleGone(mIsUserLoggedIn and hasItem)
        }
    }

    override fun showSignUpBottomSheet() {

    }

    override fun onGoSellerToLink(linkId: String) {
        val url = Const.TIPID_PC_VIEW_SELLER + linkId
        requireActivity().launchToSellerWebActivity(url)
    }

    override fun onGoToSellerItems(sellerName: String) {
        requireActivity().launchActivity<SellerItemsActivity> {
            putExtra(SellerItemsActivity.SELLER_NAME_REFERENCE, sellerName)
        }
    }

    override fun onSellerUnfollowed(pos: Int) {
        adapter.snapshot()[pos]?.isVisible = false
        adapter.notifyItemChanged(pos)
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

    inner class DiffCallback : DiffUtil.ItemCallback<Saved>() {
        override fun areItemsTheSame(oldItem: Saved, newItem: Saved): Boolean {
            return oldItem.docId == newItem.docId
        }

        override fun areContentsTheSame(oldItem: Saved, newItem: Saved): Boolean {
            return oldItem == newItem
        }
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

