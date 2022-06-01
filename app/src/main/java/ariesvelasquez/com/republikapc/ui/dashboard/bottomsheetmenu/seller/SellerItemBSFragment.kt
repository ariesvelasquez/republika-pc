package ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.seller

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigItemsAdapter
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.selleritems.SellerItemsActivity
import ariesvelasquez.com.republikapc.ui.webview.WebViewActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.Tools
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_seller_item_bs.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class SellerItemBSFragment : BottomSheetDialogFragment() {

    private lateinit var sellerItemReference: FeedItem
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

    private var listener: OnSellerItemBSFragmentListener? = null

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            sellerItemReference = Gson().fromJson(getString(ARG_RAW_SELLER_ITEM), FeedItem::class.java)
//            enabledName = getBoolean(ARG_ENABLED_NAME, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_seller_item_bs, container, false)

        setBasicUIDisplayData()

        handleUserStatus()

        return rootView
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
        rootView.textViewItemName.text = sellerItemReference.name
        // Set Seller Name and Date
        rootView.textViewSellerName.text = sellerItemReference.seller

        // Price
        rootView.textViewPrice.text = Tools.formatPrice(sellerItemReference.price)

        // Link On Click
        rootView.linearLayoutLink.setOnClickListener {
            val url = Const.TIPID_PC_VIEW_ITEM + sellerItemReference.linkId
            context?.launchActivity<WebViewActivity> {
                putExtra(WebViewActivity.WEB_VIEW_URL, url)
            }
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
    interface OnSellerItemBSFragmentListener {
        fun showSignUpBottomSheet()
        fun savedItem(sellerItem: FeedItem)
    }

    fun setListener(callbackListener: OnSellerItemBSFragmentListener) {
        this.listener = callbackListener
    }

    companion object {

        const val TAG = "SellerItemBSFragment"

        private const val ARG_RAW_SELLER_ITEM = "argSellerItem"
        private const val ARG_LIST_POSITION = "listPosition"

        @JvmStatic
        fun newInstance(rawSellerItem: String, listPosition: Int) =
            SellerItemBSFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_RAW_SELLER_ITEM, rawSellerItem)
                    putInt(ARG_LIST_POSITION, listPosition)
                }
            }
    }
}