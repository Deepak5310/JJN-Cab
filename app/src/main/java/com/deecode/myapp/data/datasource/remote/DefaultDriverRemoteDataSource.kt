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
                val dto = snapshot?.toObject(DriverDto::class.java)
                trySend(dto)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getAvailability(driverId: String): Resource<DriverDto> {
        return try {
            val doc = driversCollection.document(driverId).get().await()
            val dto = doc.toObject(DriverDto::class.java)
            if (dto != null) {
                Resource.Success(dto)
            } else {
                Resource.Success(DriverDto(driverId = driverId, isOnline = false))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get driver availability", e)
        }
    }
}
