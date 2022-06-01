package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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

    val TAG = "AddToRigBottomSheetFragment"
    private lateinit var feedItemReference: FeedItem
    private var enabledName = true

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
            enabledName = getBoolean(ARG_ENABLED_NAME, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_add_to_rig_bottom_sheet, container, false)

        setBasicUIDisplayData()
        handleAddItemToRigCreationState()

        handleRigState()
        handleItemSaveState()
        handleUserStatus()

        setupRigList()

        return rootView
    }

    private fun handleRigState() {
        dashboardViewModel.rigs.observe( viewLifecycleOwner, Observer<PagedList<Rig>> {
            adapter.submitList(it)
        })
        dashboardViewModel.rigNetworkState.observe( viewLifecycleOwner, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun handleItemSaveState() {
//        dashboardViewModel.saveItemNetworkState.observe( viewLifecycleOwner, Observer {
//            when (it) {
//                NetworkState.LOADING -> {
//                    // Disable the Button after it was clicked
//                }
//                NetworkState.LOADED -> {
//                    rootView.coordinatorLayoutRoot.snack(R.string.item_saved, hasMargin = false) {}
//                    dashboardViewModel.saveItemNetworkState.postValue(NetworkState.LOADING)
//                }
//                else -> {
//                    // Show Error
//                    val mess = it.msg
//                    rootView.snack(mess!!) {
//
//                    }
//                }
//            }
//        })
    }

    private fun handleUserStatus() {
        // Check for User Status
        dashboardViewModel.isUserSignedIn.observe( viewLifecycleOwner, Observer { isUserLoggedIn ->
            setOnClickEvents(isUserLoggedIn)
        })
    }

    private fun setOnClickEvents(isUserLoggedIn: Boolean?) {
        if (isUserLoggedIn!!) {
            // Show Rig List, Enable features only for logged in user.
            rootView.linearLayoutSave.setOnClickListener {
                it.isEnabled = false
                rootView.imageViewSaveIcon.setImageResource(R.drawable.ic_save_solid_disabled)
                rootView.imageViewSaveText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGray))

                listener?.onItemSave(feedItemReference)
            }
        } else {
            // Disable features only for logged in user.
            rootView.linearLayoutSave.setOnClickListener {
                listener?.showSignUpBottomSheet()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBasicUIDisplayData() {
        // Set Item Name
        rootView.textViewItemName.text = feedItemReference.name
        // Set Seller Name and Date
        rootView.textViewSellerName.text = feedItemReference.seller
        // Date
        rootView.textViewItemDatePosted.text = " • " + feedItemReference.date
        // Price
        rootView.textViewPrice.text = feedItemReference.price?.removePrefix("PHP")?.removePrefix("P")

        // Link On Click
        rootView.linearLayoutLink.setOnClickListener {
            listener?.onGoToLink(feedItemReference.linkId ?: "")
        }

        // Tpc User Items On Click
        if (feedItemReference.date == null || feedItemReference.date.equals("Invalid date format", true)) {
            rootView.textViewItemDatePosted.visibility = View.GONE
        }

        if (!enabledName) {
            rootView.textViewSellerName.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGray))
        } else {
            rootView.textViewSellerName.setOnClickListener {
                feedItemReference.seller?.let {
                    listener?.onTPCSellerClicked(it)
                }
            }
        }
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
                    listener?.onItemAddedToRig(rigItem, feedItemReference)
                }
            }
        }

        rootView.recyclerViewRigList.adapter = adapter
    }

    private fun handleAddItemToRigCreationState() {
        dashboardViewModel.addItemToRigNetworkState.observe( viewLifecycleOwner, Observer {
            when (it) {
                NetworkState.LOADING -> {

                }
                NetworkState.LOADED -> {
                    rootView.coordinatorLayoutRoot.snack(R.string.added_to_rig_success, hasMargin = false) {}
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
        fun showSignUpBottomSheet()
        fun onGoToLink(linkId: String)
        fun onGoToSellerItems(sellerName: String)
        fun onTPCSellerClicked(sellerName: String)
        fun onItemSave(feedItem: FeedItem)
        fun onItemAddedToRig(rigItem: Rig, feedItemReference: FeedItem)
    }

    companion object {

        const val TAG = "AddToRigBottomSheetFragment"

        private const val ARG_RAW_FEED_ITEM = "rawFeedItem"
        private const val ARG_ENABLED_NAME = "enableName"

        @JvmStatic
        fun newInstance(rawFeedItem: String, enabledName: Boolean = false) =
            AddToRigBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_RAW_FEED_ITEM, rawFeedItem)
                    putBoolean(ARG_ENABLED_NAME, enabledName)
                }
            }
    }
}