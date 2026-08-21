package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.domain.model.AuthUser
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRemoteDataSource {

    override val currentUser: AuthUser?
        get() = firebaseAuth.currentUser?.toDomain()

    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Resource<AuthUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toDomain()
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Sign in succeeded but user profile was empty.")
            }
        } catch (e: Exception) {
            Resource.Error(mapAuthException(e), e)
        }
    }

    override suspend fun signUpWithEmailPassword(
        email: String,
        password: String
    ): Resource<AuthUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toDomain()
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Account creation succeeded but user profile was empty.")
            }
        } catch (e: Exception) {
            Resource.Error(mapAuthException(e), e)
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to sign out", e)
        }
    }

    private fun FirebaseUser.toDomain(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            isEmailVerified = isEmailVerified
        )
    }

    private fun mapAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "No account found with this email address."
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Please verify your email and password."
            is FirebaseAuthUserCollisionException -> "An account with this email address already exists."
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please choose a stronger password."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            else -> e.localizedMessage ?: "An unexpected authentication error occurred."
        }
    }
}
