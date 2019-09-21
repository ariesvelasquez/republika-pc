package ariesvelasquez.com.republikapc.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import ariesvelasquez.com.republikapc.model.user.User
import ariesvelasquez.com.republikapc.repository.auth.AuthRepository
import com.google.firebase.auth.AuthCredential

class AuthViewModel (private val authRepository: AuthRepository) : ViewModel() {

    var authenticatedUserLiveData = MutableLiveData<User>()
    var createdUserLiveData = MutableLiveData<User>()

    fun signInWithGoogle(authCredential: AuthCredential) {
        authenticatedUserLiveData = authRepository.firebaseSignInWithGoogle(authCredential)
    }

    fun createUser(authenticatedUser: User) {
        createdUserLiveData = authRepository.createUserInFirestoreIfNotExisting(authenticatedUser)
    }
}