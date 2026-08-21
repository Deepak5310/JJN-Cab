package com.deecode.myapp.data.repository

import com.deecode.myapp.core.dispatcher.DispatcherProvider
import com.deecode.myapp.core.result.Resource
import com.deecode.myapp.data.model.VehicleDto
import com.deecode.myapp.domain.model.Vehicle
import com.deecode.myapp.domain.repository.VehicleRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: DispatcherProvider
) : VehicleRepository {

    private val vehiclesCollection = firestore.collection("vehicles")

    override fun observeDriverVehicle(driverId: String): Flow<Resource<Vehicle?>> = callbackFlow {
        val listener = vehiclesCollection.document(driverId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val vehicle = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(VehicleDto::class.java)?.toDomain()
            } else {
                null
            }
            trySend(Resource.Success(vehicle) as Resource<Vehicle?>)
        }
        awaitClose { listener.remove() }
    }.catch {
        emit(Resource.Error(it.localizedMessage ?: "Failed to observe driver vehicle", it))
    }.flowOn(dispatchers.io)

    override suspend fun getDriverVehicle(driverId: String): Resource<Vehicle?> = withContext(dispatchers.io) {
        try {
            val snapshot = vehiclesCollection.document(driverId).get().await()
            val vehicle = if (snapshot.exists()) {
                snapshot.toObject(VehicleDto::class.java)?.toDomain()
            } else {
                null
            }
            Resource.Success(vehicle)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to get driver vehicle", e)
        }
    }

    override suspend fun saveVehicle(vehicle: Vehicle): Resource<Unit> = withContext(dispatchers.io) {
        try {
            val data = VehicleDto.fromDomain(vehicle)
            vehiclesCollection.document(vehicle.driverId).set(data, SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save vehicle details", e)
        }
    }
}
