package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PacingEngineTest {

    private val baseDate: LocalDate = LocalDate.of(2026, 11, 5)

    @Test
    fun calculate_standardPacing_calculatesCorrectDailyQuota() {
        // 300 pages over 30 days (Nov 5 to Dec 4 inclusive = 30 days)
        val deadline = baseDate.plusDays(29)
        val goal = Goal(
            id = "g1",
            title = "책 읽기",
            targetAmount = 300.0,
            currentAmount = 0.0,
            unit = "쪽",
            startDate = baseDate,
            deadline = deadline
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.ON_TRACK, result.status)
        assertEquals(30L, result.remainingDays)
        assertEquals(300.0, result.remainingAmount, 0.001)
        assertEquals(10.0, result.dailyTarget, 0.001)
        assertEquals(0.0, result.progressPercent, 0.001)
        assertEquals("D-29", result.statusMessage)
    }

    @Test
    fun calculate_partialProgress_recalculatesDailyQuotaAccurately() {
        val deadline = baseDate.plusDays(9) // 10 days total
        val goal = Goal(
            id = "g2",
            title = "코딩 문제 풀이",
            targetAmount = 100.0,
            currentAmount = 40.0, // 60 remaining over 10 days = 6 per day
            unit = "문제",
            startDate = baseDate,
            deadline = deadline
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.ON_TRACK, result.status)
        assertEquals(10L, result.remainingDays)
        assertEquals(60.0, result.remainingAmount, 0.001)
        assertEquals(6.0, result.dailyTarget, 0.001)
        assertEquals(40.0, result.progressPercent, 0.001)
    }

    @Test
    fun calculate_deadlineIsToday_setsDailyTargetToAllRemaining() {
        val goal = Goal(
            id = "g3",
            title = "당일 마감 보고서",
            targetAmount = 50.0,
            currentAmount = 20.0,
            unit = "쪽",
            startDate = baseDate,
            deadline = baseDate
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.D_DAY, result.status)
        assertEquals(1L, result.remainingDays)
        assertEquals(30.0, result.remainingAmount, 0.001)
        assertEquals(30.0, result.dailyTarget, 0.001)
        assertEquals(40.0, result.progressPercent, 0.001)
        assertEquals("오늘 마감 (D-Day)", result.statusMessage)
    }

    @Test
    fun calculate_pastDeadline_setsStatusToOverdue() {
        val pastDeadline = baseDate.minusDays(3)
        val goal = Goal(
            id = "g4",
            title = "기한 지난 과제",
            targetAmount = 100.0,
            currentAmount = 70.0,
            unit = "쪽",
            startDate = baseDate.minusDays(10),
            deadline = pastDeadline
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.OVERDUE, result.status)
        assertEquals(0L, result.remainingDays)
        assertEquals(3L, result.daysOverdue)
        assertEquals(30.0, result.remainingAmount, 0.001)
        assertEquals(30.0, result.dailyTarget, 0.001)
        assertEquals("기한 지남 (3일 초과)", result.statusMessage)
    }

    @Test
    fun calculate_goalCompleted_returnsCompletedStatusWithZeroDailyTarget() {
        val goal = Goal(
            id = "g5",
            title = "완료된 목표",
            targetAmount = 100.0,
            currentAmount = 100.0,
            unit = "쪽",
            startDate = baseDate,
            deadline = baseDate.plusDays(5),
            isCompleted = true
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.COMPLETED, result.status)
        assertEquals(0.0, result.remainingAmount, 0.001)
        assertEquals(0.0, result.dailyTarget, 0.001)
        assertEquals(100.0, result.progressPercent, 0.001)
        assertEquals("목표 달성", result.statusMessage)
    }

    @Test
    fun calculate_progressExceedsTarget_clampedRemainingZeroAndProgressOverHundred() {
        val goal = Goal(
            id = "g6",
            title = "초과 달성",
            targetAmount = 50.0,
            currentAmount = 75.0,
            unit = "km",
            startDate = baseDate,
            deadline = baseDate.plusDays(5)
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.COMPLETED, result.status)
        assertEquals(0.0, result.remainingAmount, 0.001)
        assertEquals(0.0, result.dailyTarget, 0.001)
        assertEquals(150.0, result.progressPercent, 0.001)
    }

    @Test
    fun calculate_leapYearBoundary_accuratelyComputesDays() {
        // 2028 is a leap year (Feb 29 exists)
        val feb27 = LocalDate.of(2028, 2, 27)
        val mar1 = LocalDate.of(2028, 3, 1) // Feb 27, 28, 29, Mar 1 = 4 days
        val goal = Goal(
            id = "leap",
            title = "윤년 테스트",
            targetAmount = 40.0,
            currentAmount = 0.0,
            unit = "쪽",
            startDate = feb27,
            deadline = mar1
        )

        val result = PacingEngine.calculate(goal, today = feb27)

        assertEquals(4L, result.remainingDays)
        assertEquals(10.0, result.dailyTarget, 0.001)
    }

    @Test
    fun calculate_zeroTarget_handlesSafelyWithoutDivisionByZero() {
        val goal = Goal(
            id = "zero",
            title = "0 목표치",
            targetAmount = 0.0,
            currentAmount = 0.0,
            unit = "회",
            startDate = baseDate,
            deadline = baseDate.plusDays(5)
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(GoalStatus.COMPLETED, result.status)
        assertEquals(0.0, result.dailyTarget, 0.001)
        assertEquals(100.0, result.progressPercent, 0.001)
    }

    @Test
    fun calculate_fractionalDailyTarget_formatsCorrectly() {
        val goal = Goal(
            id = "frac",
            title = "분수 분량",
            targetAmount = 10.0,
            currentAmount = 0.0,
            unit = "km",
            startDate = baseDate,
            deadline = baseDate.plusDays(2) // 3 days: 10 / 3 = 3.333...
        )

        val result = PacingEngine.calculate(goal, today = baseDate)

        assertEquals(3.333, result.dailyTarget, 0.01)
        assertEquals("3.3", result.dailyTargetFormatted)
    }
}
