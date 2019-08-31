package ariesvelasquez.com.republikapc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ariesvelasquez.com.republikapc.ui.RigsFragment
import ariesvelasquez.com.republikapc.ui.SettingsFragment
import ariesvelasquez.com.republikapc.ui.tipidpc.TipidPCFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar

    // Bottom Navigation Setup
    private lateinit var bottomNavigationView: BottomNavigationView
    private val mOnNavigationItemClickedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        toolbar.title = menuItem.title

        when (menuItem.itemId) {
            R.id.navigation_tipid_pc -> viewPager.currentItem = 0
            R.id.navigation_rigs -> viewPager.currentItem = 1
            R.id.navigation_settings -> viewPager.currentItem = 2
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        viewPager = findViewById(R.id.view_pager)
        val viewPagerFragmentAdapter = DashboardFragmentPagerAdapter(supportFragmentManager)
        viewPager.adapter = viewPagerFragmentAdapter
        viewPager.offscreenPageLimit = viewPagerFragmentAdapter.count - 1

        bottomNavigationView = findViewById(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemClickedListener)
        initTitle()
    }

    private fun initTitle() {
        toolbar.post { toolbar.title = bottomNavigationView.menu.getItem(0).title }
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
