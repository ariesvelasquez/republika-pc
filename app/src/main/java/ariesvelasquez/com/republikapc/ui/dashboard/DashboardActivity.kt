package ariesvelasquez.com.republikapc.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.ui.dashboard.tipidpc.TipidPCFragment
import ariesvelasquez.com.republikapc.ui.search.SearchActivity
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.main_toolbar.*
import timber.log.Timber

class DashboardActivity : AppCompatActivity() {

    //Google Login Request Code
    private val RC_SIGN_IN = 7
    //Google Sign In Client
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    //Firebase Auth
    private lateinit var mAuth: FirebaseAuth

    private lateinit var viewPager: ViewPager
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initFirebaseAuth()
        initOnClicks()

        textViewToolbarTitle.setOnClickListener { launchActivity<SearchActivity>() }


//        // Always cast your custom Toolbar here, and set it as the ActionBar.
//        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(tb);
//
//        // Get the ActionBar here to configure the way it behaves.
//        final ActionBar ab = getSupportActionBar();
//        //ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
//        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
//        ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)

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

    private fun initOnClicks() {
        searchButton.setOnClickListener { launchActivity<SearchActivity> {} }
        hamburgerButton.setOnClickListener { signIn() }
    }

    private fun initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.e("Login", "Google sign in failed", e)
                // ...
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Timber.d("Login", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("Login", "signInWithCredential:success")
                    val user = mAuth.currentUser
//                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w("Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Auth Failed", Toast.LENGTH_LONG).show()
//                    updateUI(null)
                }
            }
    }

    private fun initTitle() {
        toolbar.post { toolbar.title = bottomNavigationView.menu.getItem(0).title }
    }

    // Bottom Navigation Setup
    private lateinit var bottomNavigationView: BottomNavigationView
    private val mOnNavigationItemClickedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->

        //toolbar.title = menuItem.title

        when (menuItem.itemId) {
            R.id.navigation_tipid_pc -> viewPager.currentItem = 0
            R.id.navigation_rigs -> viewPager.currentItem = 1
            R.id.navigation_settings -> viewPager.currentItem = 2
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
