package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Duration

class GoalStatusRuleTest {

    @Test
    fun activeGoalBeforeDeadlineIsInProgress() {
        val result = GoalStatusRule.evaluate(
            goal(dueDate = TODAY.plusDays(1)),
            20.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertFalse(result.achieved)
        assertFalse(result.overdue)
        assertEquals(GoalPhase.IN_PROGRESS, result.phase)
    }

    @Test
    fun deadlineDayIsNotOverdue() {
        val result = GoalStatusRule.evaluate(
            goal(dueDate = TODAY),
            0.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertFalse(result.overdue)
        assertEquals(GoalPhase.DUE_TODAY, result.phase)
    }

    @Test
    fun followingDayIsOverdueButStillActive() {
        val tomorrow = Clock.offset(
            CLOCK,
            Duration.ofDays(1),
        )

        val result = GoalStatusRule.evaluate(
            goal(dueDate = TODAY),
            0.0,
            tomorrow,
        ) as GoalEvaluation.Active

        assertTrue(result.overdue)
        assertEquals(GoalPhase.OVERDUE, result.phase)
    }

    @Test
    fun noDeadlineIsInProgress() {
        val result = GoalStatusRule.evaluate(
            goal(dueDate = null),
            0.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertEquals(GoalPhase.IN_PROGRESS, result.phase)
        assertFalse(result.overdue)
    }

    @Test
    fun startingValueIsIncludedInAchievement() {
        val result = GoalStatusRule.evaluate(
            goal(target = 100.0, start = 30.0),
            70.0,
            CLOCK,
        ) as GoalEvaluation.Active

        assertTrue(result.achieved)
    }

    @Test
    fun completedGoalsAreNotReevaluated() {
        val finished = goal().copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.MANUAL,
            endedAt = TODAY,
        )

        assertEquals(
            GoalEvaluation.Completed,
            GoalStatusRule.evaluate(finished, 0.0, CLOCK),
        )
    }

    @Test
    fun invalidAndOverflowTotalsAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GoalStatusRule.evaluate(
                goal(),
                Double.NaN,
                CLOCK,
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            GoalStatusRule.evaluate(
                goal(),
                -1.0,
                CLOCK,
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            GoalStatusRule.evaluate(
                goal(
                    target = Double.MAX_VALUE,
                    start = 1.0e308,
                ),
                1.0e308,
                CLOCK,
            )
        }
    }
}