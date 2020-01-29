package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.seller

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.GlobalFlags
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigItemsAdapter
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_seller_bottom_sheet.view.*


class SellerBottomSheetFragment : BottomSheetDialogFragment() {

    val TAG = "SellerBottomSheetFragment"
    private lateinit var sellerNameReference: String

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

    private var listener: SellerBottomSheetFragmentListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            sellerNameReference = getString(ARG_SELLER_NAME, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_seller_bottom_sheet, container, false)

        handleUserStatus()
        setBasicUIDisplayData()

        handleItemSaveState()

        return rootView
    }

    private fun handleItemSaveState() {
        dashboardViewModel.saveItemNetworkState.observe( viewLifecycleOwner, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    // Disable the Button after it was clicked
                }
                NetworkState.LOADED -> {
                    rootView.coordinatorLayoutRoot.snack(R.string.item_saved, hasMargin = false) {}
                    dashboardViewModel.saveItemNetworkState.postValue(NetworkState.LOADING)
                }
                else -> {
                    // Show Error
                    val mess = it.msg
                    rootView.snack(mess!!) {

                    }
                }
            }
        })
    }

    private fun handleUserStatus() {
        // Check for User Status
        dashboardViewModel.isUserSignedIn.observe( viewLifecycleOwner, Observer { isUserLoggedIn ->
            setFollowButtonUIState(isUserLoggedIn)
        })
    }

    private fun setFollowButtonUIState(isUserLoggedIn: Boolean) {
        if (isUserLoggedIn) {

            rootView.textViewSignedOutNote.visibility = View.GONE
            rootView.buttonFollow.isEnabled = false
            rootView.buttonFollow.text = getString(R.string.updating)

            // Check if user already followed the user
            dashboardViewModel.checkIfUserFollowedSeller(sellerNameReference)
            observeUserFollowedSellerState()

            handleFollowUnfollowButtonState()
        } else {
            rootView.textViewSignedOutNote.visibility = View.VISIBLE

            rootView.buttonFollow.isEnabled = true
            rootView.buttonFollow.text = getString(R.string.sign_in_to_follow)
            rootView.buttonFollow.setOnClickListener {
                listener?.showSignUpBottomSheet()
            }
        }
    }

    private fun handleFollowUnfollowButtonState() {
        dashboardViewModel.followUnfollowSellerNetworkState.observe( viewLifecycleOwner, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    rootView.buttonFollow.text = getString(R.string.updating)
                    rootView.buttonFollow.isEnabled = false
                }
                NetworkState.LOADED -> {
                    dashboardViewModel.checkIfUserFollowedSeller(sellerNameReference)
                }
                else -> {
                    // Error Occured
                    val mess = it.msg
                    rootView.snack(mess!!) {}
                }
            }
        })
    }

    private fun observeUserFollowedSellerState() {
        dashboardViewModel.checkUserFollowedState.observe(viewLifecycleOwner, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    rootView.buttonFollow.text = getString(R.string.loading)
                    rootView.buttonFollow.isEnabled = false
                }
                NetworkState.LOADED -> {
                    setFollowUnfollowButtonFunctionality(null)
                }
                else -> {
                    // Error Occured
                    val mess = it.msg
                    rootView.snack(mess!!) {}
                }
            }
        })
    }

    private fun setFollowUnfollowButtonFunctionality(isFollowing: Boolean?) {

        val following = isFollowing ?: dashboardViewModel.isUserFollowedSeller.value!!

        // After Loading Check if User Already followed the Seller
        if (following) {
            rootView.buttonFollow.text = getString(R.string.unfollow)
            rootView.buttonFollow.isEnabled = true

            rootView.buttonFollow.setOnClickListener {
                dashboardViewModel.unfollowSeller(sellerNameReference)
            }
        } else {
            rootView.buttonFollow.text = getString(R.string.follow)
            rootView.buttonFollow.isEnabled = true

            rootView.buttonFollow.setOnClickListener {
                dashboardViewModel.followSeller(sellerNameReference)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBasicUIDisplayData() {
        // Set Seller Name
        rootView.textViewSellerName.text = sellerNameReference

        // Link On Click
        rootView.linearLayoutLink.setOnClickListener {
            listener?.onGoSellerToLink(sellerNameReference)
        }

        // Items Click
        rootView.linearLayoutItem.setOnClickListener {
            listener?.onGoToSellerItems(sellerNameReference)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SellerBottomSheetFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement SellerBottomSheetFragmentListener")
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
    interface SellerBottomSheetFragmentListener {
        fun showSignUpBottomSheet()
        fun onGoSellerToLink(linkId: String)
        fun onGoToSellerItems(sellerName: String)
        fun onSellerFollowed(feedItem: FeedItem)
    }

    companion object {

        const val TAG = "SellerBottomSheetFragment"

        private const val ARG_SELLER_NAME = "sellerName"

        @JvmStatic
        fun newInstance(sellerName: String) =
            SellerBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SELLER_NAME, sellerName)
                }
            }
    }
}