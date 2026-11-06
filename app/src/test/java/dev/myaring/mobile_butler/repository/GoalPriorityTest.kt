package dev.myaring.mobile_butler.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalPriorityTest {

    private lateinit var repository: InMemoryGoalRepository
    private val today = LocalDate.of(2026, 11, 5)

    @Before
    fun setUp() {
        repository = InMemoryGoalRepository()
    }

    @Test
    fun addGoal_assignsSequentialPriorities() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)
        val g3 = repository.addGoal("목표 3", 300.0, "쪽", today.plusDays(10), today)

        assertEquals(0, g1.priority)
        assertEquals(1, g2.priority)
        assertEquals(2, g3.priority)

        val all = repository.getAllGoals()
        assertEquals(listOf("목표 1", "목표 2", "목표 3"), all.map { it.title })
    }

    @Test
    fun moveGoalUp_middleItem_swapsWithPrevious() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)
        val g3 = repository.addGoal("목표 3", 300.0, "쪽", today.plusDays(10), today)

        val moved = repository.moveGoalUp(g2.id)
        assertTrue(moved)

        val reordered = repository.getAllGoals()
        assertEquals(listOf(g2.id, g1.id, g3.id), reordered.map { it.id })
        assertEquals(0, reordered[0].priority)
        assertEquals(1, reordered[1].priority)
        assertEquals(2, reordered[2].priority)
    }

    @Test
    fun moveGoalUp_firstItem_returnsFalseAndDoesNotChange() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)

        val moved = repository.moveGoalUp(g1.id)
        assertFalse(moved)

        val list = repository.getAllGoals()
        assertEquals(g1.id, list[0].id)
    }

    @Test
    fun moveGoalDown_middleItem_swapsWithNext() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)
        val g3 = repository.addGoal("목표 3", 300.0, "쪽", today.plusDays(10), today)

        val moved = repository.moveGoalDown(g2.id)
        assertTrue(moved)

        val reordered = repository.getAllGoals()
        assertEquals(listOf(g1.id, g3.id, g2.id), reordered.map { it.id })
        assertEquals(0, reordered[0].priority)
        assertEquals(1, reordered[1].priority)
        assertEquals(2, reordered[2].priority)
    }

    @Test
    fun moveGoalDown_lastItem_returnsFalseAndDoesNotChange() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)

        val moved = repository.moveGoalDown(g2.id)
        assertFalse(moved)

        val list = repository.getAllGoals()
        assertEquals(g2.id, list[1].id)
    }

    @Test
    fun singleItem_reorderingDisabled() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)

        assertFalse(repository.moveGoalUp(g1.id))
        assertFalse(repository.moveGoalDown(g1.id))
    }

    @Test
    fun deleteGoal_reindexesRemainingPrioritiesWithoutGaps() {
        val g1 = repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        val g2 = repository.addGoal("목표 2", 200.0, "쪽", today.plusDays(10), today)
        val g3 = repository.addGoal("목표 3", 300.0, "쪽", today.plusDays(10), today)

        repository.deleteGoal(g2.id)

        val remaining = repository.getAllGoals()
        assertEquals(2, remaining.size)
        assertEquals(g1.id, remaining[0].id)
        assertEquals(0, remaining[0].priority)
        assertEquals(g3.id, remaining[1].id)
        assertEquals(1, remaining[1].priority)
    }
}
