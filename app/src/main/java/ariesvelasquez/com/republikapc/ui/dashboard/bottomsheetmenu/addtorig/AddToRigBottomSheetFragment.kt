package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigItemsAdapter
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_add_to_rig_bottom_sheet.view.*
import timber.log.Timber


class AddToRigBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "RigCreatorBottomSheetFragment"
    private lateinit var feedItemReference: FeedItem

    private lateinit var adapter: RigItemsAdapter

    val dashboardViewModel: DashboardViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(context!!)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    private lateinit var rootView: View

    private var listener: AddToRigBottomSheetFragmentListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            feedItemReference = Gson().fromJson(getString(ARG_RAW_FEED_ITEM), FeedItem::class.java)
        }

        Timber.e("onCreate " + feedItemReference.title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_to_rig_bottom_sheet, container, false)

        // Setup something...

        setItemUIDetails()

        setupRigList()

        handleAddItemToRigCreationState()

        // Setup Rig Item Observer
        dashboardViewModel.rigs.observe(this, Observer<PagedList<Rig>> {
            adapter.submitList(it)
        })

        return rootView
    }

    private fun setItemUIDetails() {
        // Set Item Name
        rootView.textViewItemName.text = feedItemReference.title
        // Set Seller Name
        rootView.textViewSellerNameAndDate.text = feedItemReference.seller + " • 2 mins ago"
        // Price
        rootView.textViewPrice.text = feedItemReference.price
        // Date
//        rootView
    }

    private fun setupRigList() {
        // List of Rigs
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.recyclerViewRigList.layoutManager = layoutManager

        adapter = RigItemsAdapter(
            RigItemsAdapter.ADD_TO_RIG_VIEW_TYPE,
            { dashboardViewModel.refreshRigs() }) { v, rigItem ->

            when (v.id) {
                R.id.buttonAdd -> {
                    dashboardViewModel.addItemToRig(rigItem, feedItemReference)
                    Timber.e("Adding this item to rig.")
                }
            }

            Timber.e("CLICKKKED " + rigItem.name)
        }

        rootView.recyclerViewRigList.adapter = adapter
    }

    private fun handleAddItemToRigCreationState() {
        dashboardViewModel.addItemToRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> {

                }
                NetworkState.LOADED -> {

                    rootView.coordinatorLayoutRoot.snack(R.string.added_to_rig_success, hasMargin = false) {

                    }

                    dashboardViewModel.addItemToRigNetworkState.postValue(NetworkState.LOADING)
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error
                    val mess = it.msg
                    rootView.snack(mess!!) {

                    }
                }
            }
        })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is AddToRigBottomSheetFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement AddToRigBottomSheetFragment")
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
    interface AddToRigBottomSheetFragmentListener {

    }

    companion object {

        const val TAG = "AddToRigBottomSheetFragment"

        private const val ARG_RAW_FEED_ITEM = "rawFeedItem"

        @JvmStatic
        fun newInstance(rawFeedItem: String) =
            AddToRigBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RAW_FEED_ITEM, rawFeedItem)
                }
            }
    }
}