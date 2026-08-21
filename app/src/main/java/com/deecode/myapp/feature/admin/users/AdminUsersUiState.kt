package com.deecode.myapp.feature.admin.users

import com.deecode.myapp.core.base.UiEvent
import com.deecode.myapp.core.base.UiState
import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User

enum class UserRoleFilter {
    ALL,
    CUSTOMER,
    DRIVER
}

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val isUnauthorized: Boolean = false,
    val allUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: UserRoleFilter = UserRoleFilter.ALL,
    val selectedUser: User? = null,
    val isUpdating: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) : UiState {
    val filteredUsers: List<User>
        get() {
            return allUsers.filter { user ->
                val matchesFilter = when (selectedFilter) {
                    UserRoleFilter.ALL -> true
                    UserRoleFilter.CUSTOMER -> user.role == UserRole.CUSTOMER
                    UserRoleFilter.DRIVER -> user.role == UserRole.DRIVER
                }

                val query = searchQuery.trim().lowercase()
                val matchesQuery = query.isBlank() ||
                        user.name.lowercase().contains(query) ||
                        user.email.lowercase().contains(query) ||
                        user.phone.lowercase().contains(query) ||
                        user.uid.lowercase().contains(query)

                matchesFilter && matchesQuery
            }
        }
}

sealed interface AdminUsersUiEvent : UiEvent {
    data class SearchQueryChanged(val query: String) : AdminUsersUiEvent
    data class SelectFilter(val filter: UserRoleFilter) : AdminUsersUiEvent
    data class SelectUser(val user: User?) : AdminUsersUiEvent
    data class ToggleUserActive(val targetUid: String, val isActive: Boolean) : AdminUsersUiEvent
    data class ToggleDriverVerification(val targetUid: String, val isVerified: Boolean) : AdminUsersUiEvent
    data class ChangeUserRole(val targetUid: String, val newRole: UserRole) : AdminUsersUiEvent
    data object Refresh : AdminUsersUiEvent
    data object ClearActionMessage : AdminUsersUiEvent
    data object ClearError : AdminUsersUiEvent
}
