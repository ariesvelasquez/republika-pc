package ariesvelasquez.com.republikapc.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager.widget.ViewPager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.console.ConsoleBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.RepublikaPCFragment
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.TipidPCFragment
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.main_toolbar.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@ExperimentalCoroutinesApi
@ExperimentalPagingApi
@AndroidEntryPoint
class DashboardActivity : BaseDashboardActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar
    private var mCurrentMenuFragment = R.id.navigation_rigs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initOnClicks()

        // Observe Rig Creation State
        handleRigState()

        handleAddItemToRigCreationState()
        handleSaveItemState()

        handleNukeSignedInUserData()

        textViewToolbarTitle.setOnClickListener { launchActivity<SearchActivity>() }

        viewPager = findViewById(R.id.view_pager)
        val viewPagerFragmentAdapter =
            DashboardFragmentPagerAdapter(
                supportFragmentManager
            )
        viewPager.adapter = viewPagerFragmentAdapter
        viewPager.offscreenPageLimit = viewPagerFragmentAdapter.count - 1

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemClickedListener)

        initTitle()

        bindNavigationDrawer()
    }

    private fun handleNukeSignedInUserData() {
        viewModel.nukeLoggedInUserData().observe( this, Observer {
            when (it) {
                NetworkState.LOADING -> Timber.e("Nuke LOADING")
                NetworkState.LOADED -> Timber.e("Nuke LOADED")
                else -> {
                    Timber.e("Nuke Error")
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (mCurrentMenuFragment == R.id.navigation_rigs && RepublikaPC.globalFlags.shouldRefreshRigs) {
            RepublikaPC.globalFlags.shouldRefreshRigs = false
            viewModel.refreshRigs()
        }

        if (mCurrentMenuFragment == R.id.navigation_rigs && RepublikaPC.globalFlags.shouldRefreshFollowed) {
            RepublikaPC.globalFlags.shouldRefreshFollowed = false
            viewModel.refreshFollowed()
        }

        if (mCurrentMenuFragment == R.id.navigation_rigs && RepublikaPC.globalFlags.shouldRefreshSaved) {
            RepublikaPC.globalFlags.shouldRefreshSaved = false
//            viewModel.refreshSaved()
        }
    }

    private fun handleRigState() {
        viewModel.createRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADED -> {
                    createRigBottomSheet.dismiss()
                    showSnackBar(getString(R.string.rig_created_success))
                }
                NetworkState.LOADING -> {}
                else -> Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.deleteRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    rigDetailBottomSheet.dismiss()
                    finishedLoading()
                    showSnackBar(getString(R.string.rig_deleted))
                }
                else -> Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun handleAddItemToRigCreationState() {
        viewModel.addItemToRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    finishedLoading()
                    showSnackBar(getString(R.string.added_to_rig_success))
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun handleSaveItemState() {
        viewModel.saveItemNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> { startLoading() }
                NetworkState.LOADED -> {
                    finishedLoading()
                    showSnackBar(getString(R.string.item_saved))
                }
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })

        // Saved Item Deleted
//        viewModel.deleteSavedItemNetworkState.observe(this, Observer {
//            when (it) {
//                NetworkState.LOADING -> { startLoading() }
//                NetworkState.LOADED -> {
//                    savedItemBottomSheet.dismiss()
//                    finishedLoading()
//                    showSnackBar(getString(R.string.item_deleted))
//                    viewModel.deleteSavedItemNetworkState.postValue(NetworkState.LOADING)
//                }
//                NetworkState.LOADING -> {}
//                else -> {
//                    // Show Error Prompt
//                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
//                }
//            }
//        })
    }

    private fun startLoading() {
        progressBarLoader.visibility = View.VISIBLE
    }

    private fun finishedLoading() {

        Handler().postDelayed({
            progressBarLoader.visibility = View.GONE
        }, 1000)
    }

    private fun showSnackBar(message: String) {
        val successDialog = AlertDialog.Builder(this)
            .setMessage("New Rig has been created")
            .setOnCancelListener {  }

        navigation.snack(message) {

        }
    }

    private fun initOnClicks() {
        searchButton.setOnClickListener { launchActivity<SearchActivity> {} }
        hamburgerButton.setOnClickListener {

        }
    }

    private fun initTitle() {
        toolbar.post { toolbar.title = bottomNavigationView.menu.getItem(0).title }

        bottomNavigationView
    }

    // Bottom Navigation Setup
    private lateinit var bottomNavigationView: BottomNavigationView
    private val mOnNavigationItemClickedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->

        mCurrentMenuFragment = menuItem.itemId

        when (menuItem.itemId) {
            R.id.navigation_tipid_pc -> viewPager.currentItem = 0
            R.id.navigation_rigs -> {
                viewPager.currentItem = 1
                if (RepublikaPC.globalFlags.shouldRefreshRigs) {
                    RepublikaPC.globalFlags.shouldRefreshRigs = false
                    viewModel.refreshRigs()
                }
            }
//            R.id.navigation_settings -> viewPager.currentItem = 2
            R.id.navigation_settings -> {
                val bottomSheetFragment = ConsoleBottomSheetFragment.newInstance()
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.TAG)
            }
        }
        true
    }

    private class DashboardFragmentPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TipidPCFragment.newInstance()
                else -> RepublikaPCFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    override fun onNavigateToTPCFeeds() {
        bottomNavigationView.selectedItemId = R.id.navigation_tipid_pc
    }

    /**
     * This is for drawer functionality
     */
    private fun bindNavigationDrawer() {

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//        val toggle = ActionBarDrawerToggle(
//            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
//        )
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()

        hamburgerButton.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
        }

        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_vector_cute_knight)

//        val navigationView = findViewById<NavigationView>(R.id.nav_view)
//        navigationView.setNavigationItemSelectedListener { item ->
//            // Handle navigation view item clicks here.
//            val id = item.itemId
//            if (id == R.id.nav_tool) {
//                showToolSnackBar()
//            } else if (id == R.id.nav_share) {
//                showShareSnackBar()
//            } else if (id == R.id.nav_gallery) {
//                showGallerySnackBar()
//            } else if (id == R.id.nav_send) {
//                showSendSnackBar()
//            }
//            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//            drawer.closeDrawer(GravityCompat.START)
//            true
//        }
    }
}
