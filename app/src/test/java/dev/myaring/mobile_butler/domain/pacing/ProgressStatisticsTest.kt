package dev.myaring.mobile_butler.domain.pacing

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressStatisticsTest {

    @Test
    fun overallProgressClampsAtOneHundredPercent() {
        val result = ProgressCalculator.calculate(
            goal(target = 10.0),
            listOf(entry(amount = 12.0)),
        )

        assertEquals(0.0, result.remainingAmount, 0.0)
        assertEquals(100.0, result.completionPercent, 0.0)
    }

    @Test
    fun initialValueIsAccountedForWithoutPretendingItWasRecorded() {
        val result = ProgressCalculator.calculate(
            goal(start = 50.0, target = 150.0),
            listOf(entry(amount = 25.0)),
        )

        assertEquals(25.0, result.recordedAmount, 0.0)
        assertEquals(75.0, result.currentValue, 0.0)
        assertEquals(75.0, result.remainingAmount, 0.0)
        assertEquals(0.25, result.completionFraction, 0.0)
    }

    @Test
    fun streakMayEndYesterdayWithoutPenaltyForPendingToday() {
        val records = listOf(
            entry(TODAY.minusDays(1), id = "1"),
            entry(TODAY.minusDays(2), id = "2"),
        )

        assertEquals(
            2,
            StreakCalculator.currentStreak(records, CLOCK),
        )

        assertEquals(
            3,
            StreakCalculator.currentStreak(
                records + entry(id = "3"),
                CLOCK,
            ),
        )
    }

    @Test
    fun missingYesterdayBreaksTheStreak() {
        val records = listOf(
            entry(TODAY.minusDays(2)),
        )

        assertEquals(
            0,
            StreakCalculator.currentStreak(records, CLOCK),
        )
    }

    @Test
    fun multipleEntriesInOneDayCountOnlyOnce() {
        val records = listOf(
            entry(id = "1"),
            entry(id = "2"),
            entry(TODAY.minusDays(13), id = "3"),
            entry(TODAY.minusDays(14), id = "4"),
        )

        val result = StreakCalculator.recordedDaysInWindow(
            records,
            14,
            CLOCK,
        )

        assertEquals(14, result.windowDays)
        assertEquals(2, result.recordedDays)
    }

    @Test
    fun pendingTodayIsNotIncludedInNumerator() {
        val result = StreakCalculator.recordedDaysInWindow(
            listOf(entry(TODAY.minusDays(1))),
            14,
            CLOCK,
        )

        assertEquals(1, result.recordedDays)
    }
}