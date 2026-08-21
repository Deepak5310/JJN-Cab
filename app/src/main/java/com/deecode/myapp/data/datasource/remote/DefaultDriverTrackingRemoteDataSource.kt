package com.deecode.myapp.data.datasource.remote

import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.DriverLocationDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DefaultDriverTrackingRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : DriverTrackingRemoteDataSource {

    private val trackingCollection = firestore.collection("driver_locations")

    override suspend fun pushDriverLocation(driverLocationDto: DriverLocationDto): Resource<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Resource.Error("Driver must be authenticated to push location.")

        if (currentUid != driverLocationDto.driverId) {
            return Resource.Error("Unauthorized location update. Driver ID does not match session.")
        }

        return try {
            trackingCollection.document(driverLocationDto.bookingId)
                .set(driverLocationDto, SetOptions.merge())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update driver location", e)
        }
    }

    override fun observeDriverLocation(bookingId: String): Flow<DriverLocationDto?> = callbackFlow {
        val listener = trackingCollection.document(bookingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                try {
                    val dto = snapshot?.toObject(DriverLocationDto::class.java)
                    trySend(dto)
                } catch (e: Exception) {
                    trySend(null)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun clearDriverLocation(bookingId: String): Resource<Unit> {
        return try {
            trackingCollection.document(bookingId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to clear driver location", e)
        }
    }
}
