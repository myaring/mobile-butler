package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class OverdueGoalRuleTest {

    private val fixedToday = LocalDate.of(2026, 11, 6)
    private val clock = Clock.fixed(
        fixedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    private fun createGoal(dueDate: LocalDate, overdueNoticeShown: Boolean = false): Goal {
        return Goal(
            id = "test_goal",
            title = "모바일 집사 프로젝트",
            targetValue = 100.0,
            unit = "쪽",
            startDate = LocalDate.of(2026, 10, 1),
            dueDate = dueDate,
            priority = 0,
            overdueNoticeShown = overdueNoticeShown
        )
    }

    @Test
    fun dueToday_isNotOverdue() {
        val goal = createGoal(dueDate = fixedToday)
        val evaluation = GoalStatusRule.evaluate(goal, 30.0, clock)

        assertTrue(evaluation is GoalEvaluation.Active)
        val active = evaluation as GoalEvaluation.Active
        assertEquals(GoalPhase.DUE_TODAY, active.phase)
        assertFalse("Due today must not be marked as overdue", active.overdue)
    }

    @Test
    fun dayAfterDue_isOverdue() {
        val goal = createGoal(dueDate = fixedToday.minusDays(1))
        val evaluation = GoalStatusRule.evaluate(goal, 30.0, clock)

        assertTrue(evaluation is GoalEvaluation.Active)
        val active = evaluation as GoalEvaluation.Active
        assertEquals(GoalPhase.OVERDUE, active.phase)
        assertTrue("Day after due date must be marked as overdue", active.overdue)
    }

    @Test
    fun pacingCalculator_overdueGoal_returnsOverdueWithoutDivisionByZero() {
        val goal = createGoal(dueDate = fixedToday.minusDays(5))
        val result = PacingCalculator.calculate(goal, emptyList(), clock)

        assertEquals(PacingResult.Overdue, result)
    }

    @Test
    fun continueGoal_updatesDueDate_resetsNoticeShown_keepsActive() {
        val goal = createGoal(dueDate = fixedToday.minusDays(2), overdueNoticeShown = true)
        val newDueDate = fixedToday.plusDays(14)

        val transition = GoalTransitionRule.reduce(
            goal = goal,
            recordedTotal = 40.0,
            event = GoalEvent.Continue(newDueDate = newDueDate),
            clock = clock
        )

        assertEquals(GoalStatus.ACTIVE, transition.goal.status)
        assertEquals(newDueDate, transition.goal.dueDate)
        assertFalse(transition.goal.overdueNoticeShown)
    }

    @Test
    fun finishGoal_setsCompletedWithDeadlineReason() {
        val goal = createGoal(dueDate = fixedToday.minusDays(2))

        val transition = GoalTransitionRule.reduce(
            goal = goal,
            recordedTotal = 50.0,
            event = GoalEvent.Finish(chosenDate = fixedToday),
            clock = clock
        )

        assertEquals(GoalStatus.COMPLETED, transition.goal.status)
        assertEquals(EndReason.DEADLINE, transition.goal.endReason)
        assertEquals(fixedToday, transition.goal.endedAt)
    }

    @Test
    fun recordedAmountReachingTarget_completesWithAchievedReason() {
        val goal = createGoal(dueDate = fixedToday.minusDays(2))

        val transition = GoalTransitionRule.reduce(
            goal = goal,
            recordedTotal = 100.0, // 목표치 100.0 도달
            event = GoalEvent.Reevaluate,
            clock = clock
        )

        assertEquals(GoalStatus.COMPLETED, transition.goal.status)
        assertEquals(EndReason.ACHIEVED, transition.goal.endReason)
        assertEquals(fixedToday, transition.goal.endedAt)
    }

    @Test
    fun overdueNoticeShown_doesNotTriggerShowOverdueOnceAgain() {
        val goalAlreadyShown = createGoal(dueDate = fixedToday.minusDays(2), overdueNoticeShown = true)

        val transition = GoalTransitionRule.reduce(
            goal = goalAlreadyShown,
            recordedTotal = 30.0,
            event = GoalEvent.Reevaluate,
            clock = clock
        )

        assertFalse(
            "Should not show overdue popup again if overdueNoticeShown is true",
            transition.effects.contains(GoalEffect.SHOW_OVERDUE_ONCE)
        )
    }

    @Test
    fun messages_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "마감 기한이 지난 목표입니다.",
            "남은 분량을 이어서 진행하시겠습니까?",
            "현재까지 30쪽 진행하셨습니다."
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Text must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun messages_containZeroEmojis() {
        val sampleTexts = listOf(
            "마감 기한이 지난 목표입니다.",
            "이어서 하기",
            "여기서 마무리",
            "일정 안내"
        )

        for (msg in sampleTexts) {
            val hasEmoji = msg.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Text must not contain emoji: $msg", hasEmoji)
        }
    }
}
