package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import java.time.Clock
import java.time.LocalDate

data class GoalProgress(
    val recordedAmount: Double,
    val currentValue: Double,
    val remainingAmount: Double,
    val completionFraction: Double,
) {
    val completionPercent: Double
        get() = completionFraction * 100.0
}

object ProgressCalculator {
    fun calculate(
        goal: Goal,
        entries: List<ProgressEntry>,
    ): GoalProgress {
        require(entries.all { it.goalId == goal.id })

        val recordedAmount = entries.sumOf {
            it.amount
        }

        val current = goal.startValue + recordedAmount

        require(current.isFinite())

        val remaining = (
                goal.targetValue - current
                ).coerceAtLeast(0.0)

        val difference = goal.targetValue - goal.startValue

        require(difference.isFinite() && difference > 0.0)

        val fraction = (
                recordedAmount / difference
                ).coerceIn(0.0, 1.0)

        return GoalProgress(
            recordedAmount = recordedAmount,
            currentValue = current,
            remainingAmount = remaining,
            completionFraction = fraction,
        )
    }
}

data class RecordedDaysCount(
    val windowDays: Int,
    val recordedDays: Int,
)

object StreakCalculator {
    fun currentStreak(
        entries: List<ProgressEntry>,
        clock: Clock,
    ): Int {
        val today = LocalDate.now(clock)

        val dates = entries.mapTo(mutableSetOf()) {
            it.recordedDate
        }

        var cursor = if (today in dates) {
            today
        } else {
            today.minusDays(1)
        }

        var count = 0

        while (cursor in dates) {
            count++
            cursor = cursor.minusDays(1)
        }

        return count
    }

    fun recordedDaysInWindow(
        entries: List<ProgressEntry>,
        days: Int,
        clock: Clock,
    ): RecordedDaysCount {
        require(days > 0)

        val today = LocalDate.now(clock)

        val firstDate = today.minusDays(
            days.toLong() - 1L
        )

        val count = entries
            .asSequence()
            .map { it.recordedDate }
            .filter {
                !it.isBefore(firstDate) &&
                        !it.isAfter(today)
            }
            .distinct()
            .count()

        return RecordedDaysCount(
            windowDays = days,
            recordedDays = count,
        )
    }
}