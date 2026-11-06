package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalPriorityRuleTest {

    private fun createGoal(id: String, title: String, priority: Int): Goal {
        return Goal(
            id = id,
            title = title,
            targetValue = 100.0,
            unit = "쪽",
            startDate = LocalDate.of(2026, 11, 1),
            dueDate = LocalDate.of(2026, 11, 30),
            priority = priority
        )
    }

    @Test
    fun normalize_assignsSequentialPriorities() {
        val list = listOf(
            createGoal("1", "A", 10),
            createGoal("2", "B", 50),
            createGoal("3", "C", 2)
        )

        val normalized = GoalPriorityRule.normalize(list)
        assertEquals(0, normalized[0].priority)
        assertEquals(1, normalized[1].priority)
        assertEquals(2, normalized[2].priority)
    }

    @Test
    fun reorder_singleGoal_returnsSameList() {
        val list = listOf(createGoal("1", "A", 0))
        val reordered = GoalPriorityRule.reorder(list, 0, 0)
        assertEquals(1, reordered.size)
        assertEquals("1", reordered[0].id)
    }

    @Test
    fun reorder_invalidIndices_returnsNormalized() {
        val list = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1)
        )
        val reordered = GoalPriorityRule.reorder(list, -1, 5)
        assertEquals("1", reordered[0].id)
        assertEquals("2", reordered[1].id)
    }

    @Test
    fun moveUp_firstItem_doesNotMove() {
        val list = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1)
        )
        val result = GoalPriorityRule.moveUp(list, 0)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun moveUp_secondItem_movesToFirst() {
        val list = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1)
        )
        val result = GoalPriorityRule.moveUp(list, 1)
        assertEquals("2", result[0].id)
        assertEquals("1", result[1].id)
        assertEquals(0, result[0].priority)
        assertEquals(1, result[1].priority)
    }

    @Test
    fun moveDown_lastItem_doesNotMove() {
        val list = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1)
        )
        val result = GoalPriorityRule.moveDown(list, 1)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun moveDown_firstItem_movesToSecond() {
        val list = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1),
            createGoal("3", "C", 2)
        )
        val result = GoalPriorityRule.moveDown(list, 0)
        assertEquals("2", result[0].id)
        assertEquals("1", result[1].id)
        assertEquals("3", result[2].id)
        assertEquals(0, result[0].priority)
        assertEquals(1, result[1].priority)
        assertEquals(2, result[2].priority)
    }

    @Test
    fun isTopTodayGoal_withinLimit_returnsTrue() {
        assertTrue(GoalPriorityRule.isTopTodayGoal(0))
        assertTrue(GoalPriorityRule.isTopTodayGoal(1))
        assertTrue(GoalPriorityRule.isTopTodayGoal(2))
    }

    @Test
    fun isTopTodayGoal_beyondLimit_returnsFalse() {
        assertFalse(GoalPriorityRule.isTopTodayGoal(3))
        assertFalse(GoalPriorityRule.isTopTodayGoal(4))
        assertFalse(GoalPriorityRule.isTopTodayGoal(-1))
    }

    @Test
    fun viewModel_reorder_updatesState() {
        val viewModel = GoalScreenViewModel()
        val initialGoals = listOf(
            createGoal("1", "A", 0),
            createGoal("2", "B", 1),
            createGoal("3", "C", 2)
        )
        viewModel.setGoals(initialGoals)

        viewModel.moveGoalDown(0)
        val goalsAfter = viewModel.uiState.value.goals
        assertEquals("2", goalsAfter[0].id)
        assertEquals("1", goalsAfter[1].id)
        assertEquals("3", goalsAfter[2].id)
    }
}
