package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.DriverDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DefaultDriverRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DriverRemoteDataSource {

    private val driversCollection = firestore.collection("drivers")

    override suspend fun setAvailability(driverId: String, isOnline: Boolean): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("User must be authenticated to update driver availability.")

        if (currentUid != driverId) {
            return Resource.Error("Unauthorized. Driver ID does not match authenticated user.")
        }

        return try {
            val updateData = mapOf(
                "driverId" to driverId,
                "isOnline" to isOnline,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            driversCollection.document(driverId).set(updateData, SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update driver availability", e)
        }
    }

    override fun observeAvailability(driverId: String): Flow<DriverDto?> = callbackFlow {
        val listener = driversCollection.document(driverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val isOnline = snapshot.getBoolean("isOnline")
                        ?: snapshot.getBoolean("online")
                        ?: (snapshot.get("isOnline") as? Boolean)
                        ?: false
                    val updatedAt = snapshot.getTimestamp("updatedAt")
                    trySend(DriverDto(driverId = snapshot.id, isOnline = isOnline, updatedAt = updatedAt))
                } else {
                    trySend(DriverDto(driverId = driverId, isOnline = false, updatedAt = null))
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getAvailability(driverId: String): Resource<DriverDto> {
        return try {
            val doc = driversCollection.document(driverId).get().await()
            if (doc.exists()) {
                val isOnline = doc.getBoolean("isOnline")
                    ?: doc.getBoolean("online")
                    ?: (doc.get("isOnline") as? Boolean)
                    ?: false
                val updatedAt = doc.getTimestamp("updatedAt")
                Resource.Success(DriverDto(driverId = doc.id, isOnline = isOnline, updatedAt = updatedAt))
            } else {
                Resource.Success(DriverDto(driverId = driverId, isOnline = false))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get driver availability", e)
        }
    }
}
