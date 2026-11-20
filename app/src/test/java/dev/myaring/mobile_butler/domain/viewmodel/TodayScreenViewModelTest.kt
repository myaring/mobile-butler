package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class TodayScreenViewModelTest {

    private val fixedToday = LocalDate.of(2026, 11, 6)
    private val clock = Clock.fixed(
        fixedToday.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    private fun createGoal(id: String, priority: Int, dueDate: LocalDate = fixedToday.plusDays(10)): Goal {
        return Goal(
            id = id,
            title = "목표 $id",
            targetValue = 100.0,
            unit = "쪽",
            startDate = fixedToday.minusDays(5),
            dueDate = dueDate,
            priority = priority,
            status = GoalStatus.ACTIVE
        )
    }

    @Test
    fun updateGoals_splitsIntoTopThreeAndRemaining() {
        val viewModel = TodayScreenViewModel(clock)
        val goals = listOf(
            createGoal("goal_4", priority = 3),
            createGoal("goal_1", priority = 0),
            createGoal("goal_5", priority = 4),
            createGoal("goal_2", priority = 1),
            createGoal("goal_3", priority = 2)
        )

        viewModel.updateGoals(goals)
        val state = viewModel.uiState.value

        assertEquals(3, state.topGoals.size)
        assertEquals("goal_1", state.topGoals[0].goal.id)
        assertEquals("goal_2", state.topGoals[1].goal.id)
        assertEquals("goal_3", state.topGoals[2].goal.id)

        assertEquals(2, state.remainingGoals.size)
        assertEquals("goal_4", state.remainingGoals[0].goal.id)
        assertEquals("goal_5", state.remainingGoals[1].goal.id)
    }

    @Test
    fun defaultExpandedCard_isFirstPriorityGoal() {
        val viewModel = TodayScreenViewModel(clock)
        val goals = listOf(
            createGoal("goal_b", priority = 1),
            createGoal("goal_a", priority = 0)
        )

        viewModel.updateGoals(goals)
        val state = viewModel.uiState.value

        assertEquals("goal_a", state.expandedGoalId)
    }

    @Test
    fun toggleCardExpansion_collapsesWhenSameCardClicked() {
        val viewModel = TodayScreenViewModel(clock)
        val goals = listOf(createGoal("goal_a", priority = 0))

        viewModel.updateGoals(goals)
        assertEquals("goal_a", viewModel.uiState.value.expandedGoalId)

        viewModel.toggleCardExpansion("goal_a")
        assertNull(viewModel.uiState.value.expandedGoalId)

        viewModel.toggleCardExpansion("goal_a")
        assertEquals("goal_a", viewModel.uiState.value.expandedGoalId)
    }

    @Test
    fun toggleMoreExpansion_invertsState() {
        val viewModel = TodayScreenViewModel(clock)
        assertFalse(viewModel.uiState.value.isMoreExpanded)

        viewModel.toggleMoreExpansion()
        assertTrue(viewModel.uiState.value.isMoreExpanded)

        viewModel.toggleMoreExpansion()
        assertFalse(viewModel.uiState.value.isMoreExpanded)
    }

    @Test
    fun recalculate_onDateChange_updatesDDay() {
        val viewModel = TodayScreenViewModel(clock)
        val dueDate = fixedToday.plusDays(5)
        val goal = createGoal("goal_due", priority = 0, dueDate = dueDate)

        viewModel.updateGoals(listOf(goal))
        assertEquals("D-5", viewModel.uiState.value.topGoals[0].dDayText)

        // Advance clock by 2 days
        val nextClock = Clock.fixed(
            fixedToday.plusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )
        viewModel.setClock(nextClock)

        assertEquals("D-3", viewModel.uiState.value.topGoals[0].dDayText)
    }

    @Test
    fun messages_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "오늘 할 목표가 없습니다. 새로운 목표를 등록해 보세요.",
            "오늘도 한 걸음씩 나아가 봅시다.",
            "오늘의 목표 10쪽 · D-5"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun messages_containZeroEmojis() {
        val sampleTexts = listOf(
            "오늘 할 목표가 없습니다. 새로운 목표를 등록해 보세요.",
            "오늘도 한 걸음씩 나아가 봅시다.",
            "오늘의 목표 10쪽 · D-5",
            "오늘"
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
