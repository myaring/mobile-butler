package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Direction
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import java.time.Clock
import java.time.LocalDate

enum class RecordState {
    FULL,
    PARTIAL,
    NONE,
}

enum class TodayCellState {
    TodayPending,
    TodayDone,
}

data class DailyRecord(
    val date: LocalDate,
    val goalId: String,
    val achievedAmount: Double,
    val quota: Double,
    val state: RecordState,
)

object DayStatusRule {
    fun calculate(
        goal: Goal,
        entries: List<ProgressEntry>,
        date: LocalDate,
        quota: Double,
        direction: Direction = goal.direction,
    ): DailyRecord {
        require(direction == Direction.UP) {
            "Only UP goals are supported in Season 1."
        }

        require(entries.all { it.goalId == goal.id })
        require(quota.isFinite() && quota >= 0.0)

        val dayEntries = entries.filter {
            it.recordedDate == date
        }

        val dailyTotal = dayEntries.sumOf {
            it.amount
        }

        val state = when {
            dayEntries.isEmpty() -> RecordState.NONE
            dailyTotal >= quota -> RecordState.FULL
            else -> RecordState.PARTIAL
        }

        return DailyRecord(
            date = date,
            goalId = goal.id,
            achievedAmount = dailyTotal,
            quota = quota,
            state = state,
        )
    }

    fun todayCell(
        entries: List<ProgressEntry>,
        clock: Clock,
    ): TodayCellState {
        val today = LocalDate.now(clock)

        return if (entries.any { it.recordedDate == today }) {
            TodayCellState.TodayDone
        } else {
            TodayCellState.TodayPending
        }
    }

    fun hasRecord(
        state: RecordState,
    ): Boolean = state != RecordState.NONE
}