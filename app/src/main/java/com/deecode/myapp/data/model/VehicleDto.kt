package com.deecode.myapp.data.model

import com.deecode.myapp.domain.model.Vehicle
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

data class VehicleDto(
    @get:PropertyName("driverId") @set:PropertyName("driverId") var driverId: String = "",
    @get:PropertyName("vehicleType") @set:PropertyName("vehicleType") var vehicleType: String = "SEDAN",
    @get:PropertyName("makeModel") @set:PropertyName("makeModel") var makeModel: String = "",
    @get:PropertyName("registrationNumber") @set:PropertyName("registrationNumber") var registrationNumber: String = "",
    @get:PropertyName("color") @set:PropertyName("color") var color: String = "",
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null
) {
    fun toDomain(): Vehicle {
        return Vehicle(
            driverId = driverId,
            vehicleType = vehicleType,
            makeModel = makeModel,
            registrationNumber = registrationNumber,
            color = color,
            updatedAt = updatedAt?.toDate()?.time
        )
    }

    companion object {
        fun fromDomain(vehicle: Vehicle): Map<String, Any?> {
            return mapOf(
                "driverId" to vehicle.driverId,
                "vehicleType" to vehicle.vehicleType,
                "makeModel" to vehicle.makeModel.trim(),
                "registrationNumber" to vehicle.registrationNumber.trim().uppercase(),
                "color" to vehicle.color.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
    }
}
