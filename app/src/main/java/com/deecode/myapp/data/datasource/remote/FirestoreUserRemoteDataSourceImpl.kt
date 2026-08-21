package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.UserDto
import com.deecode.myapp.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreUserRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRemoteDataSource {

    private val usersCollection = firestore.collection("users")

    override suspend fun createUserProfile(user: User): Resource<User> {
        return try {
            val userMap = UserDto.fromDomain(user)
            usersCollection.document(user.uid).set(userMap).await()
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to create user profile", e)
        }
    }

    override suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                val dto = snapshot.toObject(UserDto::class.java)
                if (dto != null) {
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Error("Failed to parse user profile data.")
                }
            } else {
                Resource.Error("User profile does not exist.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to retrieve user profile", e)
        }
    }

    override fun observeUserProfile(uid: String): Flow<Resource<User>> = callbackFlow {
        val listenerRegistration = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.localizedMessage ?: "Error observing profile", error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val dto = snapshot.toObject(UserDto::class.java)
                    if (dto != null) {
                        trySend(Resource.Success(dto.toDomain()))
                    } else {
                        trySend(Resource.Error("Failed to parse user profile data."))
                    }
                } else {
                    trySend(Resource.Error("User profile does not exist."))
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun updateProfile(
        uid: String,
        name: String,
        phone: String
    ): Resource<Unit> {
        return try {
            val updateMap = UserDto.updateProfileMap(name.trim(), phone.trim())
            usersCollection.document(uid).update(updateMap).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile", e)
        }
    }
}
