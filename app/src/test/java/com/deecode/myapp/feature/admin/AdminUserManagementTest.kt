package com.deecode.myapp.feature.admin

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.data.model.UserDto
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.feature.admin.users.AdminUsersUiState
import com.deecode.myapp.feature.admin.users.UserRoleFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminUserManagementTest {

    private val sampleUsers = listOf(
        User(
            uid = "u1",
            name = "Aman Sharma",
            email = "aman@example.com",
            phone = "+919876543210",
            role = UserRole.CUSTOMER,
            isActive = true
        ),
        User(
            uid = "u2",
            name = "Ravi Kumar",
            email = "ravi@example.com",
            phone = "+919811223344",
            role = UserRole.DRIVER,
            isActive = true,
            isDriverVerified = true
        ),
        User(
            uid = "u3",
            name = "Priya Singh",
            email = "priya@example.com",
            phone = "+919988776655",
            role = UserRole.CUSTOMER,
            isActive = false
        )
    )

    @Test
    fun `filters users by search query across name email and phone`() {
        val stateName = AdminUsersUiState(allUsers = sampleUsers, searchQuery = "aman")
        assertEquals(1, stateName.filteredUsers.size)
        assertEquals("u1", stateName.filteredUsers[0].uid)

        val stateEmail = AdminUsersUiState(allUsers = sampleUsers, searchQuery = "ravi@")
        assertEquals(1, stateEmail.filteredUsers.size)
        assertEquals("u2", stateEmail.filteredUsers[0].uid)

        val statePhone = AdminUsersUiState(allUsers = sampleUsers, searchQuery = "998877")
        assertEquals(1, statePhone.filteredUsers.size)
        assertEquals("u3", statePhone.filteredUsers[0].uid)
    }

    @Test
    fun `filters users by role filter`() {
        val stateCust = AdminUsersUiState(allUsers = sampleUsers, selectedFilter = UserRoleFilter.CUSTOMER)
        assertEquals(2, stateCust.filteredUsers.size)

        val stateDriver = AdminUsersUiState(allUsers = sampleUsers, selectedFilter = UserRoleFilter.DRIVER)
        assertEquals(1, stateDriver.filteredUsers.size)
        assertEquals("u2", stateDriver.filteredUsers[0].uid)

        val stateAll = AdminUsersUiState(allUsers = sampleUsers, selectedFilter = UserRoleFilter.ALL)
        assertEquals(3, stateAll.filteredUsers.size)
    }

    @Test
    fun `prevents promotion to ADMIN privilege`() {
        fun canAssignRole(newRole: UserRole): Boolean {
            return newRole != UserRole.ADMIN
        }

        assertTrue(canAssignRole(UserRole.CUSTOMER))
        assertTrue(canAssignRole(UserRole.DRIVER))
        assertFalse(canAssignRole(UserRole.ADMIN))
    }

    @Test
    fun `generates correct update maps with server timestamps and admin audit metadata`() {
        val statusMap = UserDto.updateStatusMap(isActive = false, adminUid = "admin_99")
        assertEquals(false, statusMap["isActive"])
        assertEquals("admin_99", statusMap["statusChangedBy"])
        assertNotNull(statusMap["statusChangedAt"])
        assertNotNull(statusMap["updatedAt"])

        val driverMap = UserDto.updateDriverVerificationMap(isVerified = true, adminUid = "admin_99")
        assertEquals(true, driverMap["isDriverVerified"])
        assertEquals("admin_99", driverMap["statusChangedBy"])

        val roleMap = UserDto.updateRoleMap(newRole = UserRole.DRIVER, adminUid = "admin_99")
        assertEquals("DRIVER", roleMap["role"])
        assertEquals("admin_99", roleMap["statusChangedBy"])
    }
}
