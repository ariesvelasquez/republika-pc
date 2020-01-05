package ariesvelasquez.com.republikapc.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ariesvelasquez.com.republikapc.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber

abstract class BaseActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    protected var mIsUserLoggedIn: Boolean = false

    protected var mFirebaseUser = FirebaseAuth.getInstance().currentUser
    protected val mFirebaseAuth = FirebaseAuth.getInstance()
    protected val mGoogleClient by lazy {
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
            mIsUserLoggedIn = false
            onUserLoggedOut()
        } else {
            mIsUserLoggedIn = true
            onUserLoggedIn(firebaseUser)
            mFirebaseUser = firebaseUser
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

    protected fun showSimplePrompt(message: String) {
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage(message)
            // if the dialog is cancelable
            .setCancelable(true)
            // positive button text and action
//            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
//
//            })
            // negative button text and action
            .setNegativeButton("Okeeh") { dialog, id ->
                dialog.cancel()
            }

        // create dialog box
        val alert = dialogBuilder.create()
        // set name for alert dialog box
        alert.setTitle(" ")
        // show alert dialog
        alert.show()
    }
}