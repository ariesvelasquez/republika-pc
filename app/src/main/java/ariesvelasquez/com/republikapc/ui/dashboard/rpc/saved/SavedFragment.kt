package ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DiffUtil
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.ResultState
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.saved.SavedActionBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.generic.FirestorePagingDataAdapter
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_saved.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SavedFragment : DashboardFragment(),
    SavedActionBottomSheetFragment.OnSavedActionInteractionFragmentListener {

    private lateinit var rootView: View
    private var listener: OnSavedFragmentInteractionListener? = null

    private val viewModel by viewModels<SavedViewModel>()
    private lateinit var adapter : FirestorePagingDataAdapter<Saved>
    private lateinit var savedItemBottomSheet : SavedActionBottomSheetFragment

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
        rootView =  inflater.inflate(R.layout.fragment_saved, container, false)


        initSwipeToRefresh()
        initAdapter()
        initOnClicks()
        handleUIState(false)

        return rootView
    }

    override fun onResume() {
        super.onResume()

        initViewModel()
    }

    private fun initViewModel() {
        lifecycleScope.launch {

            if (mIsUserLoggedIn and !mIsSavedInitialized) {
                mIsSavedInitialized = true
                viewModel.savedPagedList.collectLatest { adapter.submitData(it) }
            }

            viewModel.deleteTask().observe(viewLifecycleOwner) {
                when (it) {
                    is ResultState.Success -> {}
                    is ResultState.Error -> {
                        rootView.snack(
                            it.getErrorIfExists()?.message ?:
                            getString(R.string.delete_failed)
                        )
                    }
                }
            }
        }
    }


    private fun initOnClicks() {
        rootView.buttonSignIn.setOnClickListener {
            listener?.showSignUpBottomSheet()
        }

        rootView.buttonSearch.setOnClickListener {
            requireContext().launchActivity<SearchActivity> {}
        }

        rootView.buttonGoToFeeds.setOnClickListener {
            listener?.onNavigateToTPCFeeds()
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

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private  fun deleteTask(id: String) {
        viewModel.deleteSaved(id)
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn() {

    }

    private fun initSwipeToRefresh() {
        rootView.swipeRefreshSaved.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun initAdapter() {
        adapter = FirestorePagingDataAdapter(
            R.layout.item_recycler_view_saved,
            DiffCallback()
        )

        adapter.setOnClickCallback { item, pos ->
            savedItemBottomSheet = SavedActionBottomSheetFragment.newInstance(item, pos)
            savedItemBottomSheet.setListener(this)
            savedItemBottomSheet.show(childFragmentManager, savedItemBottomSheet.TAG)
        }

        adapter.addLoadStateListener {
            rootView.progressSaved.visibleGone(it.isInitialLoading())
            rootView.swipeRefreshSaved.isRefreshing = it.sourceRefreshState()

            if (it.finishedAppend() || it.finishedPrepend()) {
                handleUIState(adapter.itemCount > 0)
            } else {
                handleUIState(false)
            }
        }

        rootView.savedList.layoutManager = getVerticalStaggeredLayoutManager
        rootView.savedList.adapter = adapter
    }

    private fun handleUIState(hasItem: Boolean = false) {
        rootView.apply {
            linearLayoutSignIn.visibleGone(!mIsUserLoggedIn)
            linearLayoutPlaceHolder.visibleGone(mIsUserLoggedIn and !hasItem)
            savedList.visibleGone(mIsUserLoggedIn and hasItem)
        }
    }

    interface OnSavedFragmentInteractionListener {
        fun onSavedItemClicked(saved: Saved)
        fun showSignUpBottomSheet()
        fun onNavigateToTPCFeeds()
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

    override fun showSignUpBottomSheet() {
        listener?.showSignUpBottomSheet()
    }

    override fun onItemDelete(savedItem: Saved, pos: Int) {
        adapter.snapshot()[pos]?.isVisible = false
        adapter.notifyItemChanged(pos)
        viewModel.deleteSaved(savedItem.docId!!)
    }

    override fun onSavedItemAddedToRIg(rigItem: Rig, savedItemReference: Saved) {

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

