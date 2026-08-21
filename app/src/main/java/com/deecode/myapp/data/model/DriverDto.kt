package com.deecode.myapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DriverDto(
    @DocumentId
    val driverId: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
