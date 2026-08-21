package com.deecode.myapp.domain.model

import com.deecode.myapp.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingStateMachineTest {

    @Test
    fun `canonicalizes status aliases to authoritative BookingStatus`() {
        assertEquals(BookingStatus.ACCEPTED, BookingStateMachine.canonicalize("ASSIGNED"))
        assertEquals(BookingStatus.ACCEPTED, BookingStateMachine.canonicalize("accepted"))
        assertEquals(BookingStatus.DRIVER_ARRIVING, BookingStateMachine.canonicalize("ARRIVING"))
        assertEquals(BookingStatus.DRIVER_ARRIVING, BookingStateMachine.canonicalize("driver_arriving"))
        assertEquals(BookingStatus.IN_PROGRESS, BookingStateMachine.canonicalize("STARTED"))
        assertEquals(BookingStatus.IN_PROGRESS, BookingStateMachine.canonicalize("in_progress"))
        assertEquals(BookingStatus.COMPLETED, BookingStateMachine.canonicalize("COMPLETED"))
        assertEquals(BookingStatus.CANCELLED, BookingStateMachine.canonicalize("CANCELLED"))
        assertEquals(BookingStatus.REQUESTED, BookingStateMachine.canonicalize(null))
        assertEquals(BookingStatus.REQUESTED, BookingStateMachine.canonicalize("UNKNOWN_STATUS"))
    }

    @Test
    fun `verifies active and terminal state identification`() {
        assertTrue(BookingStateMachine.isActive(BookingStatus.REQUESTED))
        assertTrue(BookingStateMachine.isActive(BookingStatus.ACCEPTED))
        assertTrue(BookingStateMachine.isActive(BookingStatus.DRIVER_ARRIVING))
        assertTrue(BookingStateMachine.isActive(BookingStatus.IN_PROGRESS))

        assertFalse(BookingStateMachine.isActive(BookingStatus.COMPLETED))
        assertFalse(BookingStateMachine.isActive(BookingStatus.CANCELLED))

        assertTrue(BookingStateMachine.isTerminal(BookingStatus.COMPLETED))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED_BY_CUSTOMER))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.CANCELLED_BY_DRIVER))
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.NO_DRIVERS_AVAILABLE))

        assertFalse(BookingStateMachine.isTerminal(BookingStatus.REQUESTED))
        assertFalse(BookingStateMachine.isTerminal(BookingStatus.ACCEPTED))
        assertFalse(BookingStateMachine.isTerminal(BookingStatus.IN_PROGRESS))
    }

    @Test
    fun `verifies driver acceptance rules and race guards`() {
        // Can accept open REQUESTED ride
        assertTrue(BookingStateMachine.canDriverAccept(BookingStatus.REQUESTED, null))
        assertTrue(BookingStateMachine.canDriverAccept(BookingStatus.SEARCHING_DRIVER, null))

        // Cannot accept if already assigned to another driver
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.REQUESTED, "other_driver_123"))

        // Cannot accept if not in REQUESTED or SEARCHING_DRIVER status
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.ACCEPTED, null))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.IN_PROGRESS, null))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.COMPLETED, null))
        assertFalse(BookingStateMachine.canDriverAccept(BookingStatus.CANCELLED, null))
    }

    @Test
    fun `verifies valid forward driver transitions`() {
        assertTrue(BookingStateMachine.canDriverTransition(BookingStatus.ACCEPTED, BookingStatus.DRIVER_ARRIVING))
        assertTrue(BookingStateMachine.canDriverTransition(BookingStatus.DRIVER_ARRIVING, BookingStatus.IN_PROGRESS))
        assertTrue(BookingStateMachine.canDriverTransition(BookingStatus.IN_PROGRESS, BookingStatus.COMPLETED))
        assertTrue(BookingStateMachine.canDriverTransition(BookingStatus.ACCEPTED, BookingStatus.CANCELLED_BY_DRIVER))
    }

    @Test
    fun `rejects invalid and backward driver transitions`() {
        assertFalse(BookingStateMachine.canDriverTransition(BookingStatus.REQUESTED, BookingStatus.IN_PROGRESS))
        assertFalse(BookingStateMachine.canDriverTransition(BookingStatus.DRIVER_ARRIVING, BookingStatus.ACCEPTED))
        assertFalse(BookingStateMachine.canDriverTransition(BookingStatus.IN_PROGRESS, BookingStatus.DRIVER_ARRIVING))
        assertFalse(BookingStateMachine.canDriverTransition(BookingStatus.COMPLETED, BookingStatus.IN_PROGRESS))
        assertFalse(BookingStateMachine.canDriverTransition(BookingStatus.CANCELLED, BookingStatus.ACCEPTED))
    }

    @Test
    fun `verifies completion rules and terminal immutability`() {
        assertTrue(BookingStateMachine.canComplete(BookingStatus.IN_PROGRESS))
        assertFalse(BookingStateMachine.canComplete(BookingStatus.REQUESTED))
        assertFalse(BookingStateMachine.canComplete(BookingStatus.ACCEPTED))
        assertFalse(BookingStateMachine.canComplete(BookingStatus.DRIVER_ARRIVING))
        assertFalse(BookingStateMachine.canComplete(BookingStatus.COMPLETED))
        assertFalse(BookingStateMachine.canComplete(BookingStatus.CANCELLED))
    }

    @Test
    fun `verifies role-based cancellation permissions`() {
        // Customer cancellations
        assertTrue(BookingStateMachine.canCancel(BookingStatus.REQUESTED, UserRole.CUSTOMER))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.ACCEPTED, UserRole.CUSTOMER))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.DRIVER_ARRIVING, UserRole.CUSTOMER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.IN_PROGRESS, UserRole.CUSTOMER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.CUSTOMER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.CANCELLED, UserRole.CUSTOMER))

        // Driver cancellations
        assertTrue(BookingStateMachine.canCancel(BookingStatus.ACCEPTED, UserRole.DRIVER))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.DRIVER_ARRIVING, UserRole.DRIVER))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.IN_PROGRESS, UserRole.DRIVER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.REQUESTED, UserRole.DRIVER))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.DRIVER))

        // Admin cancellations
        assertTrue(BookingStateMachine.canCancel(BookingStatus.REQUESTED, UserRole.ADMIN))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.ACCEPTED, UserRole.ADMIN))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.DRIVER_ARRIVING, UserRole.ADMIN))
        assertTrue(BookingStateMachine.canCancel(BookingStatus.IN_PROGRESS, UserRole.ADMIN))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.COMPLETED, UserRole.ADMIN))
        assertFalse(BookingStateMachine.canCancel(BookingStatus.CANCELLED, UserRole.ADMIN))
    }
}
