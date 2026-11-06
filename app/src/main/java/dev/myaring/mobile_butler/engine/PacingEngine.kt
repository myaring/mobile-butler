package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalStatus
import dev.myaring.mobile_butler.model.PacingResult
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object PacingEngine {

    fun calculate(goal: Goal, today: LocalDate = LocalDate.now()): PacingResult {
        val safeTarget = goal.targetAmount.coerceAtLeast(0.0)
        val safeCurrent = goal.currentAmount.coerceAtLeast(0.0)
        val remainingAmount = (safeTarget - safeCurrent).coerceAtLeast(0.0)

        val progressPercent = if (safeTarget <= 0.0) {
            100.0
        } else {
            (safeCurrent / safeTarget) * 100.0
        }

        if (goal.isCompleted || safeTarget <= 0.0 || safeCurrent >= safeTarget) {
            return PacingResult(
                goalId = goal.id,
                targetAmount = safeTarget,
                currentAmount = safeCurrent,
                remainingAmount = 0.0,
                dailyTarget = 0.0,
                remainingDays = calculateRemainingDays(today, goal.deadline),
                daysOverdue = 0,
                progressPercent = progressPercent,
                status = GoalStatus.COMPLETED,
                statusMessage = "목표 달성"
            )
        }

        if (today.isAfter(goal.deadline)) {
            val daysOverdue = ChronoUnit.DAYS.between(goal.deadline, today)
            return PacingResult(
                goalId = goal.id,
                targetAmount = safeTarget,
                currentAmount = safeCurrent,
                remainingAmount = remainingAmount,
                dailyTarget = remainingAmount,
                remainingDays = 0,
                daysOverdue = daysOverdue,
                progressPercent = progressPercent,
                status = GoalStatus.OVERDUE,
                statusMessage = "기한 지남 (${daysOverdue}일 초과)"
            )
        }

        if (today.isEqual(goal.deadline)) {
            return PacingResult(
                goalId = goal.id,
                targetAmount = safeTarget,
                currentAmount = safeCurrent,
                remainingAmount = remainingAmount,
                dailyTarget = remainingAmount,
                remainingDays = 1,
                daysOverdue = 0,
                progressPercent = progressPercent,
                status = GoalStatus.D_DAY,
                statusMessage = "오늘 마감 (D-Day)"
            )
        }

        val daysRemainingInclusive = ChronoUnit.DAYS.between(today, goal.deadline) + 1
        val dailyTarget = if (daysRemainingInclusive > 0) {
            remainingAmount / daysRemainingInclusive.toDouble()
        } else {
            remainingAmount
        }

        val dDayCount = (daysRemainingInclusive - 1).coerceAtLeast(0)

        return PacingResult(
            goalId = goal.id,
            targetAmount = safeTarget,
            currentAmount = safeCurrent,
            remainingAmount = remainingAmount,
            dailyTarget = dailyTarget,
            remainingDays = daysRemainingInclusive,
            daysOverdue = 0,
            progressPercent = progressPercent,
            status = GoalStatus.ON_TRACK,
            statusMessage = "D-$dDayCount"
        )
    }

    private fun calculateRemainingDays(today: LocalDate, deadline: LocalDate): Long {
        return if (today.isAfter(deadline)) {
            0
        } else {
            ChronoUnit.DAYS.between(today, deadline) + 1
        }
    }
}
