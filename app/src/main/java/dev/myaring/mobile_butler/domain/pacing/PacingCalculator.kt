package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Direction
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.PacingPeriod
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

sealed interface PacingResult {
    data class Daily(
        val quota: Double,
        val remainingAmount: Double,
        val daysRemaining: Long,
    ) : PacingResult

    data object NotStarted : PacingResult
    data object NoDeadline : PacingResult
    data object Overdue : PacingResult
    data object Completed : PacingResult
}

object PacingCalculator {
    fun calculate(
        goal: Goal,
        entries: List<ProgressEntry>,
        clock: Clock
    ): PacingResult =
        calculateOn(goal, entries, LocalDate.now(clock))

    fun calculateOn(
        goal: Goal,
        entries: List<ProgressEntry>,
        date: LocalDate
    ): PacingResult {
        require(goal.type == GoalType.CUMULATIVE)
        require(goal.direction == Direction.UP)
        require(goal.pacingPeriod == PacingPeriod.DAY)
        require(entries.all { it.goalId == goal.id })

        if (goal.status == GoalStatus.COMPLETED) {
            return PacingResult.Completed
        }

        @Suppress("DEPRECATION")
        require(goal.status != GoalStatus.ARCHIVED)

        if (date.isBefore(goal.startDate)) {
            return PacingResult.NotStarted
        }

        val dueDate = goal.dueDate ?: return PacingResult.NoDeadline

        val daysRemaining =
            ChronoUnit.DAYS.between(date, dueDate) + 1L

        val previousTotal = entries
            .asSequence()
            .filter { it.recordedDate.isBefore(date) }
            .sumOf { it.amount } + goal.startValue

        require(previousTotal.isFinite())

        val difference = goal.targetValue - previousTotal
        require(difference.isFinite())

        val remaining = difference.coerceAtLeast(0.0)

        if (remaining == 0.0) {
            return PacingResult.Daily(
                quota = 0.0,
                remainingAmount = 0.0,
                daysRemaining = daysRemaining.coerceAtLeast(0L),
            )
        }

        if (daysRemaining <= 0L) {
            return PacingResult.Overdue
        }

        val quota = ceil(remaining / daysRemaining.toDouble())
            .coerceAtMost(remaining)
            .coerceAtLeast(0.0)

        return PacingResult.Daily(
            quota = quota,
            remainingAmount = remaining,
            daysRemaining = daysRemaining,
        )
    }
}