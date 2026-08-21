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
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @get:PropertyName("isDriverVerified") @set:PropertyName("isDriverVerified") var isDriverVerified: Boolean = false,
    @get:PropertyName("ratingAverage") @set:PropertyName("ratingAverage") var ratingAverage: Double = 5.0,
    @get:PropertyName("ratingCount") @set:PropertyName("ratingCount") var ratingCount: Int = 0,
    @get:PropertyName("totalRatingSum") @set:PropertyName("totalRatingSum") var totalRatingSum: Double = 0.0,
    @get:PropertyName("statusChangedAt") @set:PropertyName("statusChangedAt") var statusChangedAt: Timestamp? = null,
    @get:PropertyName("statusChangedBy") @set:PropertyName("statusChangedBy") var statusChangedBy: String? = null,
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
            isActive = isActive,
            isDriverVerified = isDriverVerified,
            ratingAverage = if (ratingCount > 0) ratingAverage else 5.0,
            ratingCount = ratingCount,
            totalRatingSum = totalRatingSum,
            statusChangedAt = statusChangedAt?.toDate()?.time,
            statusChangedBy = statusChangedBy,
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
                "isActive" to user.isActive,
                "isDriverVerified" to user.isDriverVerified,
                "ratingAverage" to user.ratingAverage,
                "ratingCount" to user.ratingCount,
                "totalRatingSum" to user.totalRatingSum,
                "statusChangedAt" to null,
                "statusChangedBy" to null,
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

        fun updateStatusMap(isActive: Boolean, adminUid: String): Map<String, Any> {
            return mapOf(
                "isActive" to isActive,
                "statusChangedAt" to FieldValue.serverTimestamp(),
                "statusChangedBy" to adminUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        fun updateDriverVerificationMap(isVerified: Boolean, adminUid: String): Map<String, Any> {
            return mapOf(
                "isDriverVerified" to isVerified,
                "statusChangedAt" to FieldValue.serverTimestamp(),
                "statusChangedBy" to adminUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }

        fun updateRoleMap(newRole: UserRole, adminUid: String): Map<String, Any> {
            return mapOf(
                "role" to newRole.name,
                "statusChangedAt" to FieldValue.serverTimestamp(),
                "statusChangedBy" to adminUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
    }
}
