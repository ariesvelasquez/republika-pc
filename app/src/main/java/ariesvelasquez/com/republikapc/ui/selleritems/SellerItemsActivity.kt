package ariesvelasquez.com.republikapc.ui.selleritems

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.ExperimentalPagingApi
import ariesvelasquez.com.republikapc.Const
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.databinding.ActivitySellerItemsBinding
import ariesvelasquez.com.republikapc.model.LoadState
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.ui.dashboard.BaseDashboardActivity
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.seller.SellerItemBSFragment
import ariesvelasquez.com.republikapc.ui.generic.FirestorePagingDataAdapter
import ariesvelasquez.com.republikapc.ui.selleritems.SellerViewModel.LoadType.CHECK_TPC_FOLLOWED_TASK
import ariesvelasquez.com.republikapc.ui.selleritems.SellerViewModel.LoadType.FOLLOW_UNFOLLOW_TASK
import ariesvelasquez.com.republikapc.ui.webview.WebViewActivity
import ariesvelasquez.com.republikapc.utils.extensions.*
import ariesvelasquez.com.republikapc.utils.observeResult
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber

@ExperimentalPagingApi
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SellerItemsActivity : BaseDashboardActivity() {

    private lateinit var mSellerName: String

    private lateinit var adapter: FirestorePagingDataAdapter<FeedItem>

    private val mViewModel by viewModels<SellerViewModel>()

    private lateinit var binding: ActivitySellerItemsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerItemsBinding.inflate(this.layoutInflater)
        binding.lifecycleOwner = this
        binding.vm = mViewModel
        setContentView(binding.root)

        // Get Intent Data
        mSellerName = intent.getStringExtra(SELLER_NAME_REFERENCE) ?: ""

        // Set Toolbar
        initToolbar()
        initAdapter()
        initViewModel()
        initOnClicks()

        mViewModel.checkIfTPCSellerIsFollowed(mSellerName)
    }

    private fun initAdapter() {
        initSwipeToRefresh()
        adapter = FirestorePagingDataAdapter(
            R.layout.item_recycler_view_seller_item,
            FeedItem.Companion.DiffCallback()
        )

        adapter.setOnClickCallback { item, pos ->
            val rawItem = Gson().toJson(item)
            val sellerItemBSFragment = SellerItemBSFragment.newInstance(rawItem, pos)
//            sellerItemBSFragment.setListener(this)
            sellerItemBSFragment.show(supportFragmentManager, SellerItemBSFragment.TAG)
        }

        adapter.addLoadStateListener {
//            Timber.e("it.sourceRefreshState() ${it.sourceRefreshState()}")
            mViewModel.updateSyncState(it.mediatorRefreshState())
        }

        FastScrollerBuilder(binding.recyclerViewSellerItemList)
            .build()

        binding.recyclerViewSellerItemList.layoutManager = getVerticalLinearLayoutManager
        binding.recyclerViewSellerItemList.adapter = adapter
    }

    private fun initToolbar() {
        binding.toolbarLayout.title = mSellerName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initViewModel() {
        lifecycleScope.launch {
            mViewModel.apply {
                sellerItemsPagedList(mSellerName).collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        mViewModel.apply {
            loadState.observe(this@SellerItemsActivity) {
                when (it) {
                    is LoadState.Error -> {
                        showSimplePrompt(it.e.message ?: "Unknown Error")
                    }
                    is LoadState.Loaded -> {
                        when (it.type) {
                            FOLLOW_UNFOLLOW_TASK -> {
                                checkIfTPCSellerIsFollowed(mSellerName)
                            }
                        }
                    }
                    is LoadState.Loading -> {
                        when (it.type) {
                            CHECK_TPC_FOLLOWED_TASK,
                            FOLLOW_UNFOLLOW_TASK ->
                                followButtonLoadingState(true)
                        }
                    }
                }
            }

            isTPCSellerFollowed.observeResult(this@SellerItemsActivity) {
                binding.viewActions.textViewFollow.text = if (it) "Followed" else "Follow"
                binding.viewActions.imageViewFollow.setImageDrawable(
                    Const.Drawables.getFollowUnfollowIcon(this@SellerItemsActivity, it)
                )
                followButtonLoadingState(false)
            }
        }
    }

    private fun followButtonLoadingState(isLoading: Boolean) {
        binding.viewActions.apply {
            btnFollow.isEnabled = !isLoading
            imageViewFollow.visibleGone(!isLoading)
            progressFollowUnfollow.visibleGone(isLoading)
        }
    }

    private fun initSwipeToRefresh() {
        binding.refreshSwipe.isEnabled = false
    }

    private fun initOnClicks() {
        binding.viewActions.linearLayoutSync.setOnClickListener {
            adapter.refresh()
        }
        binding.viewActions.btnFollow.setOnClickListener {
            mViewModel.followUnfollowTPCSeller(mSellerName)
        }
        binding.viewActions.linearLayoutLink.setOnClickListener {
            val url = Const.TIPID_PC_VIEW_SELLER + mSellerName
            this.launchToSellerWebActivity(url)
        }
    }

    override fun handleAddItemToRigCreationState() {

    }

    override fun handleSaveItemState() {

    }

    companion object {
        const val SELLER_NAME_REFERENCE = "seller_name_reference"
    }

    private fun showSnackBar(message: String) {
        binding.root.snack("Test")
    }
}
