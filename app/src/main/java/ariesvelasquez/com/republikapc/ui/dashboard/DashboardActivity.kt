package ariesvelasquez.com.republikapc.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.feeds.FeedItem
import ariesvelasquez.com.republikapc.repository.NetworkState
import ariesvelasquez.com.republikapc.ui.BaseActivity
import ariesvelasquez.com.republikapc.ui.auth.AuthActivity
import ariesvelasquez.com.republikapc.ui.create.rig.CreateRigActivity
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.addtorig.AddToRigBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.console.ConsoleBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.bottomsheetmenu.createrig.RigCreatorBottomSheetFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rigs.RigListFragment
import ariesvelasquez.com.republikapc.ui.dashboard.rigs.RigsFragment
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.DashboardViewModel
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.TipidPCFragment
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import ariesvelasquez.com.republikapc.utils.extensions.snack
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.main_toolbar.*
import timber.log.Timber

class DashboardActivity : BaseActivity(),
    TipidPCFragment.OnTPCFragmentListener,
    ConsoleBottomSheetFragment.ConsoleBottomSheetInteractionListener,
    RigCreatorBottomSheetFragment.RigCreatorBottomSheetInteractionListener,
    AddToRigBottomSheetFragment.AddToRigBottomSheetFragmentListener,
    RigListFragment.OnRigListFragmentListener {

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar

    // Create Rig Bottom Sheet
    private lateinit var createRigBottomSheet : RigCreatorBottomSheetFragment

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
        handleRigCreationState()

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

    private fun handleRigCreationState() {
        viewModel.createRigNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADED -> {
                    createRigBottomSheet.dismiss()
                    // Todo Show Success dialog
                    showCreationSuccessDialog()
                }
                NetworkState.LOADING -> {}
                else -> Toast.makeText(this, it.msg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showCreationSuccessDialog() {
        val successDialog = AlertDialog.Builder(this)
            .setMessage("New Rig has been created")
            .setOnCancelListener {  }

        navigation.snack(R.string.rig_created_success) {

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

        when (menuItem.itemId) {
            R.id.navigation_tipid_pc -> viewPager.currentItem = 0
            R.id.navigation_rigs -> viewPager.currentItem = 1
//            R.id.navigation_settings -> viewPager.currentItem = 2
            R.id.navigation_settings -> {
                val bottomSheetFragment = ConsoleBottomSheetFragment.newInstance()
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.TAG)
            }
        }
        true
    }

    private class DashboardFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TipidPCFragment.newInstance()
                1 -> RigsFragment.newInstance()
                else -> SettingsFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }

    override fun onCreateRigInvoked() {
        // Launch Create Rig Dialog
        createRigBottomSheet = RigCreatorBottomSheetFragment.newInstance()
        createRigBottomSheet.show(supportFragmentManager, createRigBottomSheet.TAG)
    }

    override fun onTPCItemClicked(feedItem: FeedItem) {
        val rawFeedItem = Gson().toJson(feedItem)
        val fragment = AddToRigBottomSheetFragment.newInstance(rawFeedItem)
        fragment.show(supportFragmentManager, fragment.TAG)
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
