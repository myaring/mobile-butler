package dev.myaring.mobile_butler.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ProgressEntryTest {

    private val offset = ZoneOffset.ofHours(9)

    private fun entry(
        amount: Double = 10.0,
    ) = ProgressEntry(
        id = "entry-1",
        goalId = "goal-1",
        recordedAt = Instant.parse("2026-10-20T23:00:00Z"),
        recordedOffset = offset,
        amount = amount,
    )

    @Test
    fun validEntryCanBeCreated() {
        val entry = entry()

        assertEquals(10.0, entry.amount, 0.0)
        assertEquals(EntrySource.MANUAL, entry.source)
    }

    @Test
    fun zeroOrNegativeAmountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            entry(amount = 0.0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            entry(amount = -1.0)
        }
    }

    @Test
    fun nonFiniteAmountIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            entry(amount = Double.NaN)
        }

        assertThrows(IllegalArgumentException::class.java) {
            entry(amount = Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun recordedDateUsesStoredOffset() {
        val entry = entry()

        assertEquals(
            LocalDate.of(2026, 10, 21),
            entry.recordedDate,
        )
    }

    @Test
    fun editingDatePreservesLocalTimeAndOffset() {
        val original = entry()

        val updated = original.withRecordedDate(
            LocalDate.of(2026, 10, 25)
        )

        assertEquals(
            LocalDate.of(2026, 10, 25),
            updated.recordedDate,
        )

        assertEquals(
            original.recordedOffset,
            updated.recordedOffset,
        )

        assertEquals(
            original.recordedAt.atOffset(offset).toLocalTime(),
            updated.recordedAt.atOffset(offset).toLocalTime(),
        )
    }

    @Test
    fun editingDateDoesNotMutateOriginal() {
        val original = entry()
        val updated = original.withRecordedDate(
            LocalDate.of(2026, 10, 25)
        )

        assertEquals(
            LocalDate.of(2026, 10, 21),
            original.recordedDate,
        )

        assertEquals(
            LocalDate.of(2026, 10, 25),
            updated.recordedDate,
        )
    }
}