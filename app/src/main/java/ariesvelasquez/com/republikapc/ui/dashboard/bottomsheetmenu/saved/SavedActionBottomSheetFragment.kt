package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.saved

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigItemsAdapter
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.selleritems.SellerItemsActivity
import ariesvelasquez.com.republikapc.ui.webview.WebViewActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_saved_action_bottom_sheet.view.*


class SavedActionBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "SavedActionBottomSheetFragment"
    private lateinit var savedItemReference: Saved
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

    private var listener: OnSavedActionInteractionFragmentListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            savedItemReference = getSerializable(ARG_SAVED_ITEM) as Saved
//            enabledName = getBoolean(ARG_ENABLED_NAME, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_saved_action_bottom_sheet, container, false)

        setBasicUIDisplayData()
        handleAddItemToRigCreationState()

        handleRigState()
        handleItemDeletedState()
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

    private fun handleItemDeletedState() {
//        dashboardViewModel.deleteSavedItemNetworkState.observe( viewLifecycleOwner, Observer {
//            when (it) {
//                NetworkState.LOADING -> {
//                    // Disable the Button after it was clicked
//                }
//                NetworkState.LOADED -> {
//                    rootView.coordinatorLayoutRoot.snack(R.string.item_deleted, hasMargin = false) {}
//                    dashboardViewModel.deleteSavedItemNetworkState.postValue(NetworkState.LOADING)
//                }
//                else -> {  // Show Error
//                    val mess = it.msg
//                    rootView.snack(mess!!) {}
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
                rootView.imageViewDeleteIcon.setImageResource(R.drawable.ic_delete_outline_disabled)
                rootView.imageViewDeleteText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorGray))

                listener?.onItemDelete(savedItemReference, arguments?.getInt(ARG_LIST_POSITION) ?: -1)
                this.dismiss()
            }
        } else {
            // Disable features only for logged in user.
            rootView.linearLayoutSave.setOnClickListener {
                listener?.showSignUpBottomSheet()
            }
        }
    }

    @OptIn(ExperimentalPagingApi::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @SuppressLint("SetTextI18n")
    private fun setBasicUIDisplayData() {
        // Set Item Name
        rootView.textViewItemName.text = savedItemReference.name
        // Set Seller Name and Date
        rootView.textViewSellerName.text = savedItemReference.seller

        // Date
        rootView.textViewItemDatePosted.text = " • " + savedItemReference.postDate

        // Price
        rootView.textViewPrice.text = savedItemReference.price?.removePrefix("P")?.removePrefix("PHP")

        // Link On Click
        rootView.linearLayoutLink.setOnClickListener {
            val url = Const.TIPID_PC_VIEW_ITEM + savedItemReference.linkId
            context?.launchActivity<WebViewActivity> {
                putExtra(WebViewActivity.WEB_VIEW_URL, url)
            }
        }

        if (savedItemReference.postDate?.isEmpty()!!) {
            rootView.textViewItemDatePosted.visibility = View.GONE
        }

        rootView.constraintSeller.setOnClickListener {
            context?.launchActivity<SellerItemsActivity> {
                putExtra(SellerItemsActivity.SELLER_NAME_REFERENCE, savedItemReference.seller)
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
                    listener?.onSavedItemAddedToRIg(rigItem, savedItemReference)
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
    interface OnSavedActionInteractionFragmentListener {
        fun showSignUpBottomSheet()
        fun onItemDelete(savedItem: Saved, pos: Int)
        fun onSavedItemAddedToRIg(rigItem: Rig, savedItemReference: Saved)
    }

    fun setListener(callbackListener: OnSavedActionInteractionFragmentListener) {
        this.listener = callbackListener
    }

    companion object {

        const val TAG = "SavedActionBottomSheetFragment"

        private const val ARG_SAVED_ITEM = "rawSavedItem"
        private const val ARG_LIST_POSITION = "listPosition"

        @JvmStatic
        fun newInstance(savedItem: Saved, listPosition: Int) =
            SavedActionBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SAVED_ITEM, savedItem)
                    putInt(ARG_LIST_POSITION, listPosition)
                }
            }
    }
}