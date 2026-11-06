package dev.myaring.mobile_butler.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

data class ProgressEntry(
    val id: String,
    val goalId: String,
    val recordedAt: Instant,
    val recordedOffset: ZoneOffset,
    val amount: Double,
    val source: EntrySource = EntrySource.MANUAL,
) {
    init {
        require(id.isNotBlank()) {
            "Entry id must not be blank."
        }

        require(goalId.isNotBlank()) {
            "Goal id must not be blank."
        }

        require(amount.isFinite() && amount > 0.0) {
            "Entry amount must be positive and finite."
        }
    }

    val recordedDate: LocalDate
        get() = recordedAt
            .atOffset(recordedOffset)
            .toLocalDate()

    fun withRecordedDate(date: LocalDate): ProgressEntry {
        val localTime = recordedAt
            .atOffset(recordedOffset)
            .toLocalTime()

        val newInstant = LocalDateTime
            .of(date, localTime)
            .toInstant(recordedOffset)

        return copy(recordedAt = newInstant)
    }
}