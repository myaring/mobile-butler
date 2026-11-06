package dev.myaring.mobile_butler.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalTest {

    private val startDate = LocalDate.of(2026, 10, 28)
    private val dueDate = LocalDate.of(2026, 11, 15)

    private fun goal(
        title: String = "Read a book",
        targetValue: Double = 300.0,
        startValue: Double = 0.0,
        dueDate: LocalDate? = this.dueDate,
        status: GoalStatus = GoalStatus.ACTIVE,
        endReason: EndReason? = null,
        endedAt: LocalDate? = null,
    ): Goal = Goal(
        id = "goal-1",
        title = title,
        unit = "pages",
        targetValue = targetValue,
        startValue = startValue,
        startDate = startDate,
        dueDate = dueDate,
        priority = 0,
        status = status,
        endReason = endReason,
        endedAt = endedAt,
    )

    @Test
    fun validGoalCanBeCreated() {
        val goal = goal()

        assertEquals("goal-1", goal.id)
        assertEquals(GoalStatus.ACTIVE, goal.status)
    }

    @Test
    fun blankTitleIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(title = "   ")
        }
    }

    @Test
    fun invalidTargetValueIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(targetValue = 0.0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            goal(targetValue = -5.0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            goal(targetValue = Double.NaN)
        }

        assertThrows(IllegalArgumentException::class.java) {
            goal(targetValue = Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun startValueMustBeFinite() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(startValue = Double.NaN)
        }
    }

    @Test
    fun dueDateCannotPrecedeStartDate() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(dueDate = LocalDate.of(2026, 10, 27))
        }
    }

    @Test
    fun nullableDueDateIsAllowed() {
        assertNull(goal(dueDate = null).dueDate)
    }

    @Test
    fun activeGoalCannotHaveCompletionData() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(
                endReason = EndReason.MANUAL,
                endedAt = startDate,
            )
        }
    }

    @Test
    fun completedGoalRequiresCompletionData() {
        assertThrows(IllegalArgumentException::class.java) {
            goal(status = GoalStatus.COMPLETED)
        }
    }

    @Test
    fun completedGoalWithReasonIsValid() {
        val completed = goal(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.ACHIEVED,
            endedAt = dueDate,
        )

        assertEquals(GoalStatus.COMPLETED, completed.status)
        assertEquals(EndReason.ACHIEVED, completed.endReason)
        assertEquals(dueDate, completed.endedAt)
    }

    @Test
    fun seasonOneDefaultsAreCorrect() {
        val goal = goal()

        assertEquals(GoalType.CUMULATIVE, goal.type)
        assertEquals(Direction.UP, goal.direction)
        assertEquals(PacingPeriod.DAY, goal.pacingPeriod)
        assertEquals(TrackingMode.MANUAL, goal.trackingMode)
        assertTrue(goal.restDays.isEmpty())
        assertFalse(goal.overdueNoticeShown)
    }

    @Test
    fun copyAlsoValidatesState() {
        val original = goal()

        assertThrows(IllegalArgumentException::class.java) {
            original.copy(
                status = GoalStatus.COMPLETED,
            )
        }
    }
}