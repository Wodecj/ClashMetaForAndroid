// SPDX-License-Identifier: GPL-3.0-or-later
package com.github.kr328.clash.update

import com.github.kr328.clash.design.util.ProfileOrder
import org.junit.Assert.*
import org.junit.Test

class ProfileOrderTest {
    data class Entry(val id: String, val active: Boolean = false)

    @Test fun draggingMovesTheChosenProfileWithoutChangingActivation() {
        val entries = listOf(Entry("a"), Entry("b", true), Entry("c"))
        val reordered = ProfileOrder.moved(entries, 1, 0)
        assertEquals(listOf("b", "a", "c"), reordered.map { it.id })
        assertSame(entries[1], reordered[0])
        assertTrue(reordered[0].active)
        assertEquals(listOf("a", "b", "c"), entries.map { it.id })
    }

    @Test fun savedOrderSurvivesARefetchWithNewObjects() {
        val refreshed = listOf(Entry("a"), Entry("b"), Entry("c"))
        assertEquals(listOf("c", "a", "b"),
            ProfileOrder.sorted(refreshed, "c|a|b") { it.id }.map { it.id })
    }

    @Test fun deletedProfilesAreIgnoredAndNewOnesAreAppended() {
        val refreshed = listOf(Entry("d"), Entry("a"), Entry("c"), Entry("e"))
        assertEquals(listOf("c", "a", "d", "e"),
            ProfileOrder.sorted(refreshed, "c|deleted|a") { it.id }.map { it.id })
    }

    @Test fun emptyOrDuplicateSavedOrderIsSafe() {
        val values = listOf("a", "b", "c")
        assertEquals(values, ProfileOrder.sorted(values, "") { it })
        assertEquals(listOf("c", "a", "b"), ProfileOrder.sorted(values, "c||c|a") { it })
    }

    @Test fun invalidMovesAndEmptyListsDoNotLoseEntries() {
        val values = listOf("a", "b")
        assertEquals(values, ProfileOrder.moved(values, -1, 0))
        assertEquals(values, ProfileOrder.moved(values, 0, 2))
        assertEquals(emptyList<String>(), ProfileOrder.moved(emptyList<String>(), 0, 1))
    }
}
