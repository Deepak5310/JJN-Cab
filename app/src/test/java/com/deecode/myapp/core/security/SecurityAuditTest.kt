package com.deecode.myapp.core.security

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.data.model.UserDto
import com.deecode.myapp.domain.model.BookingStateMachine
import com.deecode.myapp.domain.model.BookingStatus
import com.deecode.myapp.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAuditTest {

    @Test
    fun `prevents client self-escalation to ADMIN role`() {
        val allowedClientRoles = setOf(UserRole.CUSTOMER, UserRole.DRIVER)
        assertFalse(allowedClientRoles.contains(UserRole.ADMIN))

        val regularUser = User(
            uid = "user_123",
            name = "John Doe",
            email = "john@example.com",
            role = UserRole.CUSTOMER
        )

        val updatedMap = UserDto.updateProfileMap("John Doe Updated", "+919876543210")
        assertNull(updatedMap["role"])
        assertNull(updatedMap["uid"])
        assertNull(updatedMap["email"])
        assertNull(updatedMap["isActive"])
        assertNull(updatedMap["isDriverVerified"])
    }

    @Test
    fun `verifies server-authoritative Admin updates and protection`() {
        val statusMap = UserDto.updateStatusMap(isActive = false, adminUid = "admin_super_1")
        assertEquals(false, statusMap["isActive"])
        assertEquals("admin_super_1", statusMap["statusChangedBy"])

        val verificationMap = UserDto.updateDriverVerificationMap(isVerified = true, adminUid = "admin_super_1")
        assertEquals(true, verificationMap["isDriverVerified"])
        assertEquals("admin_super_1", verificationMap["statusChangedBy"])

        val roleMap = UserDto.updateRoleMap(UserRole.DRIVER, adminUid = "admin_super_1")
        assertEquals("DRIVER", roleMap["role"])
        assertEquals("admin_super_1", roleMap["statusChangedBy"])
    }

    @Test
    fun `verifies booking tamper protection and terminal immutability`() {
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.COMPLETED))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED_BY_CUSTOMER))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED_BY_DRIVER))

        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.CUSTOMER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.DRIVER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.ADMIN))

        assertFalse(BookingStateMachine.canCancel(BookingStatus.CANCELLED, UserRole.CUSTOMER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.CANCELLED, UserRole.DRIVER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.CANCELLED, UserRole.ADMIN))
    }

    @Test
    fun `verifies driver assignment authorization`() {
        assertTrue(BookingStateMachine.canDriverAccept(BookingStatus.REQUESTED, null))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.REQUESTED, "existing_driver_id"))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.COMPLETED, null))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.CANCELLED, null))
    }
}
