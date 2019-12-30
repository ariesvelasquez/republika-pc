package ariesvelasquez.com.republikapc.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.RepublikaPC
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.model.rigs.Rig
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.BaseActivity
import ariesvelasquez.com.republikapc.ui.auth.AuthActivity
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig.AddToRigBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.console.ConsoleBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.createrig.RigCreatorBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.rigdetail.RigDetailBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigListFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.RepublikaPCFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.items.PartsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rpc.rigs.RigsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.TipidPCFragment
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.main_toolbar.*

class DashboardActivity : BaseActivity(),
    TipidPCFragment.OnTPCFragmentListener,
    RigsFragment.OnRigFragmentInteractionListener,
    ConsoleBottomSheetFragment.ConsoleBottomSheetInteractionListener,
    RigCreatorBottomSheetFragment.RigCreatorBottomSheetInteractionListener,
    AddToRigBottomSheetFragment.AddToRigBottomSheetFragmentListener,
    RepublikaPCFragment.OnRepublikaPCInteractionListener,
    PartsFragment.OnPartsFragmentInteractionListener,
    RigDetailBottomSheetFragment.OnRigDetailInteractionListener,
    RigListFragment.OnRigListFragmentListener {

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar
    private var mCurrentMenuFragment = R.id.navigation_rigs

    // Create Rig Bottom Sheet
    private lateinit var createRigBottomSheet : RigCreatorBottomSheetFragment
    private lateinit var rigDetailBottomSheet : RigDetailBottomSheetFragment

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@DashboardActivity)
                    .getDashboardRepository()
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initOnClicks()

        // Observe Rig Creation State
        handleRigState()

        // Observe Add Item to Rig Creation State
        handleAddItemToRigCreationState()

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
    }

    override fun onResume() {
        super.onResume()

        if (mCurrentMenuFragment == R.id.navigation_rigs && RepublikaPC.getGlobalFlags().shouldRefreshRigs) {
            RepublikaPC.getGlobalFlags().shouldRefreshRigs = false
            viewModel.refreshRigs()
        }
    }

    private fun handleUserState() {
        viewModel.observeUser(mFirebaseUser!!.uid)
    }

    private fun handleRigState() {
        viewModel.createRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADED -> {
                    createRigBottomSheet.dismiss()
                    // Todo Show Success dialog
                    showSnackBar(getString(R.string.rig_created_success))
                }
                NetworkState.LOADING -> {}
                else -> Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
            }
        })

        viewModel.deleteRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    progressBarLoader.progress = 10
                }
                NetworkState.LOADED -> {
                    rigDetailBottomSheet.dismiss()
                    RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
                    finishedLoading()
                    showSnackBar(getString(R.string.rig_deleted))
                }
                else -> Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleAddItemToRigCreationState() {
        viewModel.addItemToRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> {
                    progressBarLoader.progress = 10
                }
                NetworkState.LOADED -> {
                    RepublikaPC.getGlobalFlags().shouldRefreshRigs = true
                    finishedLoading()
                }
                NetworkState.LOADING -> {}
                else -> {
                    // Show Error Prompt
                    Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun finishedLoading() {
        progressBarLoader.progress = 100

        Handler().postDelayed({
            progressBarLoader.progress = 0
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
    }

    override fun onUserLoggedOut() {
        viewModel.setIsUserSignedIn(false)
    }

    override fun onUserLoggedIn(user: FirebaseUser) {
        viewModel.setIsUserSignedIn(true)
        viewModel.setUser(user)

        handleUserState()
        // Handle UI Changes when logged in
    }

    override fun onAddedToRig(rigId: String) {

    }

    private fun displayRigListFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag(RigListFragment.TAG)
        if (prev != null) {
            fragmentTransaction.remove(prev)
        }
        fragmentTransaction.addToBackStack(null)
        val dialogFragment = RigListFragment.newInstance() //here MyDialog is my custom dialog
        dialogFragment.show(fragmentTransaction, RigListFragment.TAG)
    }

    // Bottom Navigation Setup
    private lateinit var bottomNavigationView: BottomNavigationView
    private val mOnNavigationItemClickedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->

        //toolbar.title = menuItem.title
        mCurrentMenuFragment = menuItem.itemId

        when (menuItem.itemId) {
            R.id.navigation_tipid_pc -> viewPager.currentItem = 0
            R.id.navigation_rigs -> {
                viewPager.currentItem = 1
                if (RepublikaPC.getGlobalFlags().shouldRefreshRigs) {
                    RepublikaPC.getGlobalFlags().shouldRefreshRigs = false
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

    override fun onTPCItemClicked(feedItem: FeedItem) {
        val rawFeedItem = Gson().toJson(feedItem)
        val fragment = AddToRigBottomSheetFragment.newInstance(rawFeedItem)
        fragment.show(supportFragmentManager, fragment.TAG)
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

    override fun onLoginInvoked() {
        launchActivity<AuthActivity> {}
        finish()
    }

    override fun onLogoutInvoked() {
        mFirebaseAuth.signOut()
        mGoogleClient.signOut()
        Toast.makeText(this, "User has been signed out", Toast.LENGTH_SHORT).show()
    }

    /**
     * This is for drawer functionality
     */
//    private fun bindNavigationDrawer() {
//
//        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//        val toggle = ActionBarDrawerToggle(
//            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
//        )
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()
//
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
//    }
}
