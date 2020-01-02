package ariesvelasquez.com.republikapc.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ariesvelasquez.com.republikapc.Const.RC_SIGN_IN
import ariesvelasquez.com.republikapc.R
import ariesvelasquez.com.republikapc.model.user.User
import ariesvelasquez.com.republikapc.ui.dashboard.DashboardActivity
import ariesvelasquez.com.republikapc.ui.search.SearchViewModel
import ariesvelasquez.com.republikapc.utils.ServiceLocator
import ariesvelasquez.com.republikapc.utils.extensions.launchActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import timber.log.Timber

class AuthActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@AuthActivity)
                    .getAuthRepository()
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(repo) as T
            }
        }
    }

    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private val googleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Init Sign IN
        signIn()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                if (googleSignInAccount != null) {

                    setUIToLoadingState()

                    getGoogleAuthCredential(googleSignInAccount)
                }
            } catch (e: ApiException) {
                Timber.e("onActivityResulty RC_SIGN_IN exception %s", e.message)
            }

        }
    }

    private fun setUIToLoadingState() {
        textViewSigningUp.text = getString(R.string.creating_user_please_wait)
        progressBarSigningIn.visibility = View.VISIBLE
    }

    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
        signInWithGoogleAuthCredential(googleAuthCredential)
    }

    private fun signInWithGoogleAuthCredential(googleAuthCredential: AuthCredential) {
        authViewModel.signInWithGoogle(googleAuthCredential)
        authViewModel.authenticatedUserLiveData.observe(this, Observer<User> { authenticatedUser ->
            if (authenticatedUser.isNew) {
                createNewUser(authenticatedUser)
            } else {
                goToDashboardActivity(authenticatedUser)
            }
        })
    }

    private fun createNewUser(authenticatedUser: User) {
        authViewModel.createUser(authenticatedUser)
        authViewModel.createdUserLiveData.observe(this, Observer<User>{ user ->
            if (user.isCreated) {
                // handle Set user name setup, etc.
                // Todo : implement user inputs
//                goToDashboardActivity(user)
                goToDashboardActivity(user)
            } else {
//                goToDashboardActivity(user)
            }
        })
    }

    private fun goToDashboardActivity(user: User) {

        Handler().postDelayed({
            launchActivity<DashboardActivity> {}
            finish()
        }, 2000)


    }
}
