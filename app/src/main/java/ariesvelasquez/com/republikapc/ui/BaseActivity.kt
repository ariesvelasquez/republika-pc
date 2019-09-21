package ariesvelasquez.com.republikapc.ui

import androidx.appcompat.app.AppCompatActivity
import ariesvelasquez.com.republikapc.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    val mFirebaseAuth = FirebaseAuth.getInstance()
    val mGoogleClient by lazy {
        GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build())
    }

    abstract fun onUserLoggedOut()
    abstract fun onUserLoggedIn(user: FirebaseUser)

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            onUserLoggedOut()
        } else {
            onUserLoggedIn(firebaseUser)
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAuth.removeAuthStateListener(this)
    }
}