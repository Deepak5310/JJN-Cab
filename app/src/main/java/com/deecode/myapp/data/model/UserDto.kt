package com.deecode.myapp.data.model

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName

data class UserDto(
    @get:PropertyName("uid") @set:PropertyName("uid") var uid: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("phone") @set:PropertyName("phone") var phone: String = "",
    @get:PropertyName("role") @set:PropertyName("role") var role: String = UserRole.CUSTOMER.name,
    @get:PropertyName("ratingAverage") @set:PropertyName("ratingAverage") var ratingAverage: Double = 5.0,
    @get:PropertyName("ratingCount") @set:PropertyName("ratingCount") var ratingCount: Int = 0,
    @get:PropertyName("totalRatingSum") @set:PropertyName("totalRatingSum") var totalRatingSum: Double = 0.0,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Timestamp? = null,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Timestamp? = null
) {
    fun toDomain(): User {
        val parsedRole = try {
            UserRole.valueOf(role.uppercase())
        } catch (_: Exception) {
            UserRole.CUSTOMER
        }

        return User(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            role = parsedRole,
            ratingAverage = if (ratingCount > 0) ratingAverage else 5.0,
            ratingCount = ratingCount,
            totalRatingSum = totalRatingSum,
            createdAt = createdAt?.toDate()?.time,
            updatedAt = updatedAt?.toDate()?.time
        )
    }

    companion object {
        fun fromDomain(user: User): Map<String, Any?> {
            return mapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "role" to user.role.name,
                "ratingAverage" to user.ratingAverage,
                "ratingCount" to user.ratingCount,
                "totalRatingSum" to user.totalRatingSum,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        fun updateProfileMap(name: String, phone: String): Map<String, Any> {
            return mapOf(
                "name" to name,
                "phone" to phone,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
    }
}
