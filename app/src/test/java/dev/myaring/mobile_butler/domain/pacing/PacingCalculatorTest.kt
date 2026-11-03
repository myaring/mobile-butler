package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class PacingCalculatorTest {

    @Test
    fun threeHundredPagesInThirtyDaysMeansTenPerDay() {
        val start = TODAY.minusDays(29)

        val clock = Clock.fixed(
            start.atStartOfDay(SEOUL).toInstant(),
            SEOUL,
        )

        val result = PacingCalculator.calculate(
            goal(startDate = start),
            emptyList(),
            clock,
        ) as PacingResult.Daily

        assertEquals(10.0, result.quota, 0.0)
        assertEquals(30L, result.daysRemaining)
    }

    @Test
    fun roundUpAndNeverExceedRemaining() {
        val result = PacingCalculator.calculateOn(
            goal(
                target = 11.0,
                startDate = TODAY.minusDays(2),
            ),
            emptyList(),
            TODAY.minusDays(2),
        ) as PacingResult.Daily

        assertEquals(4.0, result.quota, 0.0)

        val lastDay = PacingCalculator.calculateOn(
            goal(target = 11.0),
            listOf(entry(TODAY.minusDays(1), 10.0)),
            TODAY,
        ) as PacingResult.Daily

        assertEquals(1.0, lastDay.quota, 0.0)
    }

    @Test
    fun todayEntriesDoNotChangeTheQuota() {
        val target = goal(
            target = 100.0,
            dueDate = TODAY.plusDays(9),
        )

        val before = listOf(
            entry(TODAY.minusDays(1), 20.0),
        )

        val initial = PacingCalculator.calculate(
            target,
            before,
            CLOCK,
        )

        val after = PacingCalculator.calculate(
            target,
            before + entry(TODAY, 70.0),
            CLOCK,
        )

        assertEquals(initial, after)
    }

    @Test
    fun changingPreviousDayEntryRecalculatesTodaysQuota() {
        val target = goal(
            target = 100.0,
            dueDate = TODAY.plusDays(3),
        )

        val before = PacingCalculator.calculate(
            target,
            listOf(entry(TODAY.minusDays(1), 10.0)),
            CLOCK,
        ) as PacingResult.Daily

        val updated = PacingCalculator.calculate(
            target,
            listOf(entry(TODAY.minusDays(1), 3.0)),
            CLOCK,
        ) as PacingResult.Daily

        assertEquals(23.0, before.quota, 0.0)
        assertEquals(25.0, updated.quota, 0.0)
    }

    @Test
    fun overdueNeverDividesByZero() {
        val target = goal(
            dueDate = TODAY.minusDays(1),
        )

        assertEquals(
            PacingResult.Overdue,
            PacingCalculator.calculate(
                target,
                emptyList(),
                CLOCK,
            ),
        )
    }

    @Test
    fun alreadyAchievedReturnsZeroEvenPastDeadline() {
        val target = goal(
            target = 10.0,
            dueDate = TODAY.minusDays(1),
        )

        val result = PacingCalculator.calculate(
            target,
            listOf(entry(TODAY.minusDays(2), 15.0)),
            CLOCK,
        ) as PacingResult.Daily

        assertEquals(0.0, result.quota, 0.0)
    }

    @Test
    fun startValueIsIncludedInPreviousTotal() {
        val target = goal(
            target = 100.0,
            start = 80.0,
            dueDate = TODAY.plusDays(3),
        )

        val result = PacingCalculator.calculate(
            target,
            emptyList(),
            CLOCK,
        ) as PacingResult.Daily

        assertEquals(5.0, result.quota, 0.0)
    }

    @Test
    fun notStartedAndNoDeadlineAreExplicit() {
        assertEquals(
            PacingResult.NotStarted,
            PacingCalculator.calculate(
                goal(
                    startDate = TODAY.plusDays(1),
                    dueDate = TODAY.plusDays(2),
                ),
                emptyList(),
                CLOCK,
            ),
        )

        assertEquals(
            PacingResult.NoDeadline,
            PacingCalculator.calculate(
                goal(dueDate = null),
                emptyList(),
                CLOCK,
            ),
        )
    }

    @Test
    fun completedDoesNotReturnDailyQuota() {
        val completed = goal().copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.MANUAL,
            endedAt = TODAY,
        )

        assertEquals(
            PacingResult.Completed,
            PacingCalculator.calculate(
                completed,
                emptyList(),
                CLOCK,
            ),
        )
    }
}

internal val TODAY: LocalDate = LocalDate.of(2026, 11, 2)

internal val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")

internal val CLOCK: Clock = Clock.fixed(
    TODAY.atStartOfDay(SEOUL).toInstant(),
    SEOUL,
)

internal fun goal(
    target: Double = 300.0,
    start: Double = 0.0,
    startDate: LocalDate = TODAY.minusDays(29),
    dueDate: LocalDate? = TODAY,
): Goal = Goal(
    id = "g1",
    title = "Reading",
    unit = "pages",
    targetValue = target,
    startValue = start,
    startDate = startDate,
    dueDate = dueDate,
    priority = 0,
)

internal fun entry(
    date: LocalDate = TODAY,
    amount: Double = 5.0,
    id: String = "entry-${date}-${amount}",
    offset: ZoneOffset = ZoneOffset.ofHours(9),
    time: LocalTime = LocalTime.NOON,
): ProgressEntry = ProgressEntry(
    id = id,
    goalId = "g1",
    recordedAt = date.atTime(time).toInstant(offset),
    recordedOffset = offset,
    amount = amount,
)