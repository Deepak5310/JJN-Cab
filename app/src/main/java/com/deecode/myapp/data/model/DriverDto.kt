package com.deecode.myapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class DriverDto(
    @DocumentId
    @get:PropertyName("driverId") @set:PropertyName("driverId") var driverId: String = "",
    @get:PropertyName("isOnline") @set:PropertyName("isOnline") var isOnline: Boolean = false,
    @ServerTimestamp
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null
)
