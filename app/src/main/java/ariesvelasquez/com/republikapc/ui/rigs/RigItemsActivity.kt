package ariesvelasquez.com.republikapc.ui.rigs

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.rigparts.RigPart
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.BaseActivity
import ariesvelasquez.com.republikapc.ui.dashboard.rigparts.RigPartsAdapter
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.Tools
import ariesvelasquez.com.republikapc.utils.extensions.action
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_rig_items.*
import kotlinx.android.synthetic.main.activity_rig_items.toolbar
import kotlinx.android.synthetic.main.rig_items_total_bottom_sheet.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class RigItemsActivity : BaseActivity() {

    private lateinit var rigItemsAdapter: RigPartsAdapter

    private lateinit var rigItemReference: Rig

    private var rigItemsTotal = 0
    private var hasList = false

    private val dashboardViewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@RigItemsActivity)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rig_items)

        // Get Intent Data
        rigItemReference =
            Gson().fromJson(intent.getStringExtra(RIG_ITEM_REFERENCE), Rig::class.java)

        // Set Title
        toolbar.textViewToolbarTitle.text = rigItemReference.name

        initOnClicks()

        setupRigList()

        initRigItemList()
    }

    private fun initOnClicks() {
        backButton.setOnClickListener { onBackPressed() }
        buttonSearch.setOnClickListener { launchActivity<SearchActivity> {  } }
    }

    private fun setupRigList() {
        rigItemsAdapter = RigPartsAdapter(
            RigPartsAdapter.RIG_PART_VIEW_TYPE,
            { dashboardViewModel.refreshRigParts() }
            ) { v, pos, item ->

            // OnItemClick: Validate if the viewer is also the owner of the rig
            var isOwner = false
            isOwner = mFirebaseUser?.uid.equals(rigItemReference.ownerId)

            showItemDetails(isOwner, item)
        }
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        rigPartList.layoutManager = linearLayoutManager
        rigPartList.adapter = rigItemsAdapter

//        refreshSwipe.setOnRefreshListener {
//            dashboardViewModel.refreshRigParts()
//        }
    }

    private fun showItemDetails(owner: Boolean, item: RigPart) {

        if (!owner) return

        linearLayoutTotalBottomSheet.snack(item.name) {
            action(
                getString(R.string.delete),
                ContextCompat.getColor(context, R.color.colorBlue)) {
                deleteRigItem(item)
            }
        }
    }

    private fun deleteRigItem(item: RigPart) {
        dashboardViewModel.removeRigPart(rigItemReference.id, item.docId)
    }

    private fun initRigItemList() {
        dashboardViewModel.getRigParts(rigItemReference.id)

        dashboardViewModel.rigParts.observe(this, Observer<PagedList<RigPart>> {
            rigItemsAdapter.submitList(it)
            handleListUI(it.snapshot())
        })
        dashboardViewModel.rigItemsNetworkState.observe(this, Observer {
            rigItemsAdapter.setNetworkState(it)
            setTotalNetworkState(it)
        })
        dashboardViewModel.deleteRigItemNetworkState.observe(this, Observer {
            setItemDeleteNetworkState(it)
            setTotalNetworkState(it)
        })
        dashboardViewModel.rigItemsRefreshState.observe(this, Observer {
//            refreshSwipe.isRefreshing = it == NetworkState.LOADING
            setTotalNetworkState(it)
        })
    }

    private fun setItemDeleteNetworkState(it: NetworkState?) {
        when (it) {
            NetworkState.LOADING -> { startLoading() }
            NetworkState.LOADED -> {
                linearLayoutTotalBottomSheet.snack("Item deleted") {}
                finishedLoading()
            }
            NetworkState.LOADING -> {}
            else -> {
                // Show Error Prompt
                Toast.makeText(this, it?.msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleListUI(it: MutableList<RigPart>) {

        if (!mIsUserLoggedIn) {
            linearLayoutPlaceHolder.visibility = View.GONE
            rigPartList.visibility = View.GONE
            linearLayoutSignIn.visibility = View.VISIBLE
            return
        } else {
            linearLayoutSignIn.visibility = View.GONE
        }

        if (!hasList && it.isNotEmpty()) {
            // When the list is not empty, and recyclerView is still gone
            this.hasList = true
            showHidePlaceholder(show = false)
        } else if (it.isEmpty() && hasList) {
            // When the list is empty, and recyclerView is still visible
            this.hasList = false
            showHidePlaceholder(show = true)
        } else if (hasList && it.isNotEmpty()) {
            // When the list is not empty, and recyclerView is still visible
            showHidePlaceholder(show = false)
        } else {
            // List is Empty, hasList is false
            showHidePlaceholder(true)
        }
    }

    private fun showHidePlaceholder(show: Boolean) {
        if (show) {
            rigPartList.visibility = View.GONE
            linearLayoutPlaceHolder.visibility = View.VISIBLE
        } else {
            rigPartList.visibility = View.VISIBLE
            linearLayoutPlaceHolder.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTotalNetworkState(it: NetworkState?) {

        when (it) {
            NetworkState.LOADING -> {
                textViewTotal.text = getString(R.string.updating)
            }
            NetworkState.LOADED -> {
                val numberFormattedTotal =
                    Tools().numberFormatter?.format(rigItemsAdapter.getItemTotal().toInt())
                textViewTotal.text = "$numberFormattedTotal.00"
            }
        }
    }

    private fun startLoading() {
        progressBarLoader.visibility = View.VISIBLE
    }

    private fun finishedLoading() {
        Handler().postDelayed({
            progressBarLoader.visibility = View.INVISIBLE
        }, 500)
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn(user: FirebaseUser) {

    }

    companion object {
        const val RIG_ITEM_REFERENCE = "rig_item_reference"
    }
}
