package dev.myaring.mobile_butler.repository

import dev.myaring.mobile_butler.model.ButlerPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalRepositoryTest {

    private lateinit var repository: InMemoryGoalRepository
    private val today = LocalDate.of(2026, 11, 5)

    @Before
    fun setUp() {
        repository = InMemoryGoalRepository()
    }

    @Test
    fun addGoal_storesGoalAndGeneratesValidId() {
        val goal = repository.addGoal(
            title = "알고리즘 문제 풀기",
            targetAmount = 50.0,
            unit = "문제",
            deadline = today.plusDays(10),
            startDate = today
        )

        assertNotNull(goal.id)
        assertEquals("알고리즘 문제 풀기", goal.title)
        assertEquals(50.0, goal.targetAmount, 0.001)
        assertEquals(0.0, goal.currentAmount, 0.001)
        assertEquals("문제", goal.unit)
        assertFalse(goal.isCompleted)

        val fetched = repository.getGoal(goal.id)
        assertEquals(goal, fetched)
    }

    @Test
    fun updateGoal_modifiesExistingGoalProperties() {
        val goal = repository.addGoal("토익", 100.0, "단어", today.plusDays(10), today)
        val updated = goal.copy(title = "토익 고급 단어", targetAmount = 150.0)

        val result = repository.updateGoal(updated)
        assertTrue(result)

        val fetched = repository.getGoal(goal.id)
        assertEquals("토익 고급 단어", fetched?.title)
        assertEquals(150.0, fetched?.targetAmount ?: 0.0, 0.001)
    }

    @Test
    fun deleteGoal_removesGoalAndAssociatedLogs() {
        val goal = repository.addGoal("달리기", 100.0, "km", today.plusDays(30), today)
        repository.addProgressLog(goal.id, 10.0, "첫날 러닝", today)

        assertEquals(1, repository.getLogsForGoal(goal.id).size)

        val deleted = repository.deleteGoal(goal.id)
        assertTrue(deleted)
        assertNull(repository.getGoal(goal.id))
        assertTrue(repository.getLogsForGoal(goal.id).isEmpty())
    }

    @Test
    fun addProgressLog_accumulatesAmountAndMarksCompletedIfTargetReached() {
        val goal = repository.addGoal("영어 회화", 10.0, "강", today.plusDays(5), today)

        // Log 4 chapters
        val log1 = repository.addProgressLog(goal.id, 4.0, "1~4강 수강", today)
        assertNotNull(log1)
        var currentGoal = repository.getGoal(goal.id)!!
        assertEquals(4.0, currentGoal.currentAmount, 0.001)
        assertFalse(currentGoal.isCompleted)

        // Log 6 chapters (total 10.0 reached)
        val log2 = repository.addProgressLog(goal.id, 6.0, "5~10강 수강", today)
        assertNotNull(log2)
        currentGoal = repository.getGoal(goal.id)!!
        assertEquals(10.0, currentGoal.currentAmount, 0.001)
        assertTrue(currentGoal.isCompleted)
    }

    @Test
    fun deleteProgressLog_recalculatesCurrentAmountAndUnmarksCompletedIfBelowTarget() {
        val goal = repository.addGoal("도서 독서", 100.0, "쪽", today.plusDays(10), today)
        val log1 = repository.addProgressLog(goal.id, 60.0, "1차 독서", today)!!
        val log2 = repository.addProgressLog(goal.id, 45.0, "2차 독서", today)!!

        var currentGoal = repository.getGoal(goal.id)!!
        assertEquals(105.0, currentGoal.currentAmount, 0.001)
        assertTrue(currentGoal.isCompleted)

        // Undo/delete second log (45 pages)
        val deleted = repository.deleteProgressLog(log2.id)
        assertTrue(deleted)

        currentGoal = repository.getGoal(goal.id)!!
        assertEquals(60.0, currentGoal.currentAmount, 0.001)
        assertFalse(currentGoal.isCompleted)
    }

    @Test
    fun personaManagement_storesAndRetrievesSelectedPersona() {
        assertEquals(ButlerPersona.CALM, repository.getButlerPersona())

        repository.setButlerPersona(ButlerPersona.CYNICAL)
        assertEquals(ButlerPersona.CYNICAL, repository.getButlerPersona())

        repository.setButlerPersona(ButlerPersona.ANXIOUS)
        assertEquals(ButlerPersona.ANXIOUS, repository.getButlerPersona())
    }

    @Test
    fun clearAll_resetsStateCompletely() {
        repository.addGoal("목표 1", 100.0, "쪽", today.plusDays(10), today)
        repository.setButlerPersona(ButlerPersona.CYNICAL)

        repository.clearAll()

        assertTrue(repository.getAllGoals().isEmpty())
        assertEquals(ButlerPersona.CALM, repository.getButlerPersona())
    }
}
