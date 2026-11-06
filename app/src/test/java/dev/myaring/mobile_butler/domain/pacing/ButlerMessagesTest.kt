package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ButlerMessagesTest {

    private val testGoal = Goal(
        id = "goal_test",
        title = "독서 100쪽",
        targetValue = 100.0,
        unit = "쪽",
        startDate = LocalDate.of(2026, 11, 1),
        dueDate = LocalDate.of(2026, 11, 30),
        priority = 0
    )

    @Test
    fun nullGoal_returnsNoGoalMessage() {
        val message = ButlerMessages.getMessage(null, null)
        assertEquals(ButlerMessages.NO_GOAL, message)
    }

    @Test
    fun completedEvaluation_returnsGoalAchievedMessage() {
        val evaluation = GoalEvaluation.Completed
        val message = ButlerMessages.getMessage(testGoal, evaluation)
        assertEquals(ButlerMessages.GOAL_ACHIEVED, message)
    }

    @Test
    fun activeAchieved_returnsGoalAchievedMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = true,
            overdue = false,
            phase = GoalPhase.IN_PROGRESS
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation)
        assertEquals(ButlerMessages.GOAL_ACHIEVED, message)
    }

    @Test
    fun activeOverdue_returnsOverdueMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = false,
            overdue = true,
            phase = GoalPhase.OVERDUE
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation)
        assertEquals(ButlerMessages.OVERDUE, message)
    }

    @Test
    fun activeDueToday_returnsDueTodayMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = false,
            overdue = false,
            phase = GoalPhase.DUE_TODAY
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation)
        assertEquals(ButlerMessages.DUE_TODAY, message)
    }

    @Test
    fun activeInProgress_fullRecord_returnsAllTodayCompletedMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = false,
            overdue = false,
            phase = GoalPhase.IN_PROGRESS
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation, RecordState.FULL)
        assertEquals(ButlerMessages.ALL_TODAY_COMPLETED, message)
    }

    @Test
    fun activeInProgress_partialRecord_returnsInProgressPartialMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = false,
            overdue = false,
            phase = GoalPhase.IN_PROGRESS
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation, RecordState.PARTIAL)
        assertEquals(ButlerMessages.IN_PROGRESS_PARTIAL, message)
    }

    @Test
    fun activeInProgress_noneRecord_returnsInProgressNoneMessage() {
        val evaluation = GoalEvaluation.Active(
            achieved = false,
            overdue = false,
            phase = GoalPhase.IN_PROGRESS
        )
        val message = ButlerMessages.getMessage(testGoal, evaluation, RecordState.NONE)
        assertEquals(ButlerMessages.IN_PROGRESS_NONE, message)
    }

    @Test
    fun allMessages_usePoliteKoreanVerbEnding() {
        val allMessages = listOf(
            ButlerMessages.NO_GOAL,
            ButlerMessages.ALL_TODAY_COMPLETED,
            ButlerMessages.IN_PROGRESS_NONE,
            ButlerMessages.IN_PROGRESS_PARTIAL,
            ButlerMessages.DUE_TODAY,
            ButlerMessages.OVERDUE,
            ButlerMessages.GOAL_ACHIEVED
        )

        for (msg in allMessages) {
            val endsWithPoliteForm = msg.endsWith("니다.") ||
                    msg.endsWith("십시오.") ||
                    msg.endsWith("바랍니다.") ||
                    msg.endsWith("봅시다.")
            assertTrue("Message should end with polite formal ending: $msg", endsWithPoliteForm)
        }
    }

    @Test
    fun allMessages_containZeroEmojis() {
        val allMessages = listOf(
            ButlerMessages.NO_GOAL,
            ButlerMessages.ALL_TODAY_COMPLETED,
            ButlerMessages.IN_PROGRESS_NONE,
            ButlerMessages.IN_PROGRESS_PARTIAL,
            ButlerMessages.DUE_TODAY,
            ButlerMessages.OVERDUE,
            ButlerMessages.GOAL_ACHIEVED
        )

        for (msg in allMessages) {
            val hasEmoji = msg.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Message must not contain emoji: $msg", hasEmoji)
        }
    }
}
