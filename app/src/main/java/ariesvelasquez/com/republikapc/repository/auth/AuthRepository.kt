package ariesvelasquez.com.republikapc.repository.auth

import androidx.lifecycle.MutableLiveData
import ariesvelasquez.com.republikapc.model.user.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import timber.log.Timber
import java.util.*

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val userRef: CollectionReference
) {

    fun firebaseSignInWithGoogle(googleAuthCredential: AuthCredential): MutableLiveData<User> {
        val authenticatedUserMutableLiveData = MutableLiveData<User>()

        firebaseAuth.signInWithCredential(googleAuthCredential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                // Check if the user is new
                val isNewUser = authTask.result?.additionalUserInfo?.isNewUser ?: false
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    authenticatedUserMutableLiveData.postValue(
                        User().apply {
                            uid = firebaseUser.uid
                            name = firebaseUser.displayName
                            email = firebaseUser.email
                            photoUrl = Objects.requireNonNull(firebaseUser.photoUrl.toString())
                            isNew = isNewUser
                        }
                    )
                }
            } else {
                Timber.e(authTask.exception?.message!!)
            }
        }

        return authenticatedUserMutableLiveData
    }

    fun createUserInFirestoreIfNotExisting(authenticatedUser: User): MutableLiveData<User> {
        val newCreatedUser = MutableLiveData<User>()

        val uidRef = userRef.document(authenticatedUser.uid)
        uidRef.get().addOnCompleteListener { uidTask ->
            if (uidTask.isSuccessful) {
                val document = uidTask.result
                if (!document?.exists()!!) {
                    uidRef.set(authenticatedUser).addOnCompleteListener { userCreationTask ->
                        if (userCreationTask.isSuccessful) {
                            authenticatedUser.isCreated = true
                            newCreatedUser.postValue(authenticatedUser)
                        } else {
                            Timber.e(userCreationTask.exception?.message!!)
                        }
                    }
                } else {
                    newCreatedUser.setValue(authenticatedUser)
                }
            } else {
                Timber.e(uidTask.exception?.message!!)
            }
        }

        return newCreatedUser
    }
}