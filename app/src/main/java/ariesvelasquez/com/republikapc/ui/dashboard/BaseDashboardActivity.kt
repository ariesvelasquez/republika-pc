package ariesvelasquez.com.republikapc.ui.dashboard

import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.model.saved.Saved
import ariesvelasquez.com.republikapc.ui.BaseActivity
import ariesvelasquez.com.republikapc.ui.auth.AuthActivity
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig.AddToRigBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.console.ConsoleBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.createrig.RigCreatorBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.rigdetail.RigDetailBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.saved.SavedActionBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.seller.SellerBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.RepublikaPCFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.followed.FollowedFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.items.PartsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.saved.SavedFragment
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.TipidPCFragment
import ariesvelasquez.com.republikapc.ui.selleritems.SellerItemsActivity
import ariesvelasquez.com.republikapc.ui.webview.WebViewActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson

abstract class BaseDashboardActivity : BaseActivity(),
    TipidPCFragment.OnTPCFragmentListener,
    RigsFragment.OnRigFragmentInteractionListener,
    ConsoleBottomSheetFragment.ConsoleBottomSheetInteractionListener,
    RigCreatorBottomSheetFragment.RigCreatorBottomSheetInteractionListener,
    AddToRigBottomSheetFragment.AddToRigBottomSheetFragmentListener,
    RepublikaPCFragment.OnRepublikaPCInteractionListener,
    PartsFragment.OnPartsFragmentInteractionListener,
    RigDetailBottomSheetFragment.OnRigDetailInteractionListener,
    SavedFragment.OnSavedFragmentInteractionListener,
    SavedActionBottomSheetFragment.OnSavedActionInteractionFragmentListener,
    SellerBottomSheetFragment.SellerBottomSheetFragmentListener,
    FollowedFragment.OnFollowedFragmentInteractionListener {

    // Create Rig Bottom Sheet
    protected lateinit var createRigBottomSheet : RigCreatorBottomSheetFragment
    protected lateinit var rigDetailBottomSheet : RigDetailBottomSheetFragment
    protected lateinit var savedItemBottomSheet : SavedActionBottomSheetFragment

    protected val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@BaseDashboardActivity)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    abstract fun handleAddItemToRigCreationState()
    abstract fun handleSaveItemState()

    override fun onUserLoggedOut() {
        viewModel.setIsUserSignedIn(false)
    }

    override fun onUserLoggedIn(user: FirebaseUser) {
        viewModel.setIsUserSignedIn(true)
        viewModel.setUser(user)

        handleUserState()
    }

    private fun handleUserState() {
        viewModel.observeUser(mFirebaseUser!!.uid)
    }

    override fun onCreateRigInvoked() {
        // Check if Rig Count exceeds the maximum
        if (viewModel.user.value?.rigCount!! >= 2) {
            showSimplePrompt(getString(R.string.reached_max_rig_limit))
            return
        }

        // Launch Create Rig Dialog
        createRigBottomSheet = RigCreatorBottomSheetFragment.newInstance()
        createRigBottomSheet.show(supportFragmentManager, createRigBottomSheet.TAG)
    }

    override fun onTPCItemClicked(feedItem: FeedItem, enabledName: Boolean) {
        val rawFeedItem = Gson().toJson(feedItem)
        val fragment = AddToRigBottomSheetFragment.newInstance(rawFeedItem, enabledName)
        fragment.show(supportFragmentManager, fragment.TAG)
    }

    override fun onTPCSellerClicked(sellerName: String) {
        val sellerBottomSheet = SellerBottomSheetFragment.newInstance(sellerName)
        sellerBottomSheet.show(supportFragmentManager, sellerBottomSheet.TAG)
    }

    override fun onSellerFollowed(feedItem: FeedItem) {

    }

    override fun onRigMenuClicked(rig: Rig) {
        val rawRigRef = Gson().toJson(rig)
        rigDetailBottomSheet = RigDetailBottomSheetFragment.newInstance(rawRigRef)
        rigDetailBottomSheet.show(supportFragmentManager, rigDetailBottomSheet.TAG)
    }

    override fun onNewRigCreated(rigName: String) {
        viewModel.createRig(rigName)
    }

    override fun onCreatePartInvoked() {

    }

    override fun showSignUpBottomSheet() {
        val bottomSheetFragment = ConsoleBottomSheetFragment.newInstance()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.TAG)
    }

    override fun onItemAddedToRig(rigItem: Rig, feedItemReference: FeedItem) {
        viewModel.addItemToRig(rigItem, feedItemReference)
    }

    override fun onItemSave(feedItem: FeedItem) {
        viewModel.save(feedItem)
    }

    override fun onGoToSellerItems(sellerName: String) {
        launchActivity<SellerItemsActivity> {
            putExtra(SellerItemsActivity.SELLER_NAME_REFERENCE, sellerName)
        }
    }

    override fun onSavedItemClicked(saved: Saved) {
        val rawSavedItem = Gson().toJson(saved)
        savedItemBottomSheet = SavedActionBottomSheetFragment.newInstance(rawSavedItem)
        savedItemBottomSheet.show(supportFragmentManager, savedItemBottomSheet.TAG)
    }

    override fun onItemDelete(savedItem: Saved) {
        viewModel.deleteSaved(savedItem.docId)
    }

    override fun onSavedItemAddedToRIg(rigItem: Rig, savedItemReference: Saved) {
        viewModel.addSavedItemToRig(rigItem, savedItemReference)
    }

    override fun onGoToLink(linkId: String) {
        val url = Const.TIPID_PC_VIEW_ITEM + linkId
        launchActivity<WebViewActivity> {
            putExtra(WebViewActivity.WEB_VIEW_URL, url)
        }
    }

    override fun onGoSellerToLink(sellerName: String) {
        val url = Const.TIPID_PC_VIEW_SELLER + sellerName
        launchActivity<WebViewActivity> {
            putExtra(WebViewActivity.WEB_VIEW_URL, url)
        }
    }

    override fun onLoginInvoked() {
        launchActivity<AuthActivity> {}
        finish()
    }

    override fun onLogoutInvoked() {
        mFirebaseAuth.signOut()
        mGoogleClient.signOut()
        Toast.makeText(this, "User has been signed out", Toast.LENGTH_SHORT).show()
    }
}