package com.deecode.myapp.feature.customer

import com.deecode.myapp.core.model.UserRole
import com.deecode.myapp.domain.model.User
import com.deecode.myapp.feature.customer.profile.CustomerProfileUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerProfileTest {

    private val phoneRegex = Regex("^[+]?[0-9]{10,13}$")

    @Test
    fun `validates phone number formats correctly`() {
        assertTrue(phoneRegex.matches("+919876543210"))
        assertTrue(phoneRegex.matches("9876543210"))
        assertTrue(phoneRegex.matches("01123456789"))

        assertFalse(phoneRegex.matches("12345"))
        assertFalse(phoneRegex.matches("abc1234567"))
        assertFalse(phoneRegex.matches("+91 98765 43210")) // Spaces not cleaned
    }

    @Test
    fun `extracts initials properly from user names`() {
        val twoWordsState = CustomerProfileUiState(
            user = User(uid = "u1", name = "Deepak Joshi", email = "deepak@test.com", phone = "+919876543210", role = UserRole.CUSTOMER)
        )
        assertEquals("DJ", twoWordsState.initials)

        val singleWordState = CustomerProfileUiState(
            user = User(uid = "u2", name = "Aarav", email = "aarav@test.com", phone = "+919876543210", role = UserRole.CUSTOMER)
        )
        assertEquals("A", singleWordState.initials)

        val emptyNameState = CustomerProfileUiState(
            user = User(uid = "u3", name = "", email = "test@test.com", phone = "+919876543210", role = UserRole.CUSTOMER)
        )
        assertEquals("U", emptyNameState.initials)
    }

    @Test
    fun `validates CustomerProfileUiState form validity`() {
        val validState = CustomerProfileUiState(
            editName = "Vikram Malhotra",
            editPhone = "+919876543210"
        )
        assertTrue(validState.isValid)

        val blankNameState = CustomerProfileUiState(
            editName = " ",
            editPhone = "+919876543210"
        )
        assertFalse(blankNameState.isValid)

        val shortNameState = CustomerProfileUiState(
            editName = "A",
            editPhone = "+919876543210"
        )
        assertFalse(shortNameState.isValid)
    }
}
