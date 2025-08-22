package com.example.smartsplit.Viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginScreenViewModel() : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FB", "SignIn Success: ${task.result}")

                        // Check and link if password not yet linked
                        linkEmailPasswordIfMissing(email, password)

                        home()
                    } else {
                        Log.d("FB", "SignIn Failed: ${task.exception?.message}")
                    }
                }
        } catch (ex: Exception) {
            Log.e("FB", "SignIn Exception: ${ex.message}")
        }
    }

    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) {
        if (_loading.value == false) {
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Log.d("FB", "Account created for: ${user?.email}")

                        // Log providers to confirm password is linked
                        user?.providerData?.forEach {
                            Log.d("FB", "Linked provider: ${it.providerId}")
                        }

                        val displayName = email.split('@')[0]
                        createUser(displayName)
                        home()
                    } else {
                        Log.e("FB", "Registration Failed: ${task.exception?.message}")
                    }
                    _loading.value = false
                }
        }
    }
    private fun createUser(displayName: String?) {
        val userId = auth.currentUser?.uid ?: return
        val user = MUser(
            userId = userId,
            displayName = displayName ?: "",
            avatarUrl = "",
            quote = "Life is great",
            profession = "Android Developer",
            id = null
        ).toMap()
        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                Log.d("FB", "User document successfully created!")
            }
            .addOnFailureListener { exception ->
                Log.e("FB", "Error creating user document: ${exception.message}")
            }
    }

    fun isUserRegistered(email: String, callback: (Boolean) -> Unit) {
        FirebaseAuth.getInstance()
            .fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                    Log.d("FB", "Fetched sign-in methods for $email: $signInMethods")

                    // Check if password is one of the methods
                    val isRegistered = "password" in signInMethods
                    callback(isRegistered)
                } else {
                    Log.e("FB", "Error fetching sign-in methods", task.exception)
                    callback(false)
                }
            }
    }
    private fun linkEmailPasswordIfMissing(email: String, password: String) {
        val normalizedEmail = email.trim().lowercase()
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(normalizedEmail)
            .addOnSuccessListener { result ->
                val signInMethods = result.signInMethods
                if (signInMethods.isNullOrEmpty() || !signInMethods.contains("password")) {
                    Log.d("FB", "Password not linked for $normalizedEmail, linking now...")

                    val credential = EmailAuthProvider.getCredential(normalizedEmail, password)
                    auth.currentUser?.linkWithCredential(credential)
                        ?.addOnSuccessListener {
                            Log.d("FB", "Successfully linked email/password for $normalizedEmail")
                        }
                        ?.addOnFailureListener { exception ->
                            Log.e("FB", "Failed to link email/password: ${exception.message}")
                        }
                } else {
                    Log.d("FB", "$normalizedEmail already has password provider linked.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FB", "Failed to fetch sign-in methods: ${exception.message}")
            }
    }
}

data class MUser(val id: String?,
                 val userId: String,
                 val displayName: String,
                 val avatarUrl: String,
                 val quote: String,
                 val profession: String){
    fun toMap(): MutableMap<String, Any> {
        return mutableMapOf("user_id" to this.userId,
            "display_name" to this.displayName,
            "quote" to this.quote,
            "profession" to this.profession,
            "avatar_url" to this.avatarUrl)
    }

}
