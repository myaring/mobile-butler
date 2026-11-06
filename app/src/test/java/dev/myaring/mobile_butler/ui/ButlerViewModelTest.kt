package dev.myaring.mobile_butler.ui

import dev.myaring.mobile_butler.model.ButlerEvent
import dev.myaring.mobile_butler.model.ButlerPersona
import dev.myaring.mobile_butler.model.GoalStatus
import dev.myaring.mobile_butler.repository.InMemoryGoalRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ButlerViewModelTest {

    private lateinit var repository: InMemoryGoalRepository
    private lateinit var viewModel: ButlerViewModel
    private val testToday = LocalDate.of(2026, 11, 5)

    @Before
    fun setUp() {
        repository = InMemoryGoalRepository()
        viewModel = ButlerViewModel(
            repository = repository,
            todayProvider = { testToday }
        )
    }

    @Test
    fun initialState_emptyGoals_showsNoGoalsEventAndCalmPersona() {
        val state = viewModel.uiState.value

        assertTrue(state.goals.isEmpty())
        assertEquals(ButlerPersona.CALM, state.persona)
        assertEquals(ButlerEvent.NO_GOALS, state.activeEvent)
        assertTrue(state.activeDialogue.isNotBlank())
        assertNull(state.errorMessage)
    }

    @Test
    fun createGoal_validInputs_createsGoalAndCalculatesPacing() {
        val success = viewModel.createGoal(
            title = "토익 900점",
            targetAmountText = "200",
            unit = "문제",
            deadline = testToday.plusDays(10)
        )

        assertTrue(success)
        val state = viewModel.uiState.value
        assertEquals(1, state.goals.size)
        assertEquals("토익 900점", state.goals[0].title)

        val pacing = state.pacingResults[state.goals[0].id]
        assertNotNull(pacing)
        assertEquals(200.0, pacing?.targetAmount ?: 0.0, 0.001)
        assertEquals(11L, pacing?.remainingDays)
        assertFalse(state.isAddGoalDialogOpen)
    }

    @Test
    fun createGoal_invalidInput_setsErrorMessageAndDoesNotCreate() {
        val success = viewModel.createGoal(
            title = "",
            targetAmountText = "100",
            unit = "쪽",
            deadline = testToday.plusDays(5)
        )

        assertFalse(success)
        val state = viewModel.uiState.value
        assertTrue(state.goals.isEmpty())
        assertEquals("목표 이름을 입력해주세요.", state.errorMessage)
    }

    @Test
    fun logProgress_partial_triggersProgressLoggedEvent() {
        viewModel.createGoal("독서", "100", "쪽", testToday.plusDays(10))
        val goalId = viewModel.uiState.value.goals[0].id

        val success = viewModel.logProgress(goalId, "20", "오늘 분량 완료")
        assertTrue(success)

        val state = viewModel.uiState.value
        val pacing = state.pacingResults[goalId]
        assertEquals(20.0, pacing?.currentAmount ?: 0.0, 0.001)
        assertEquals(80.0, pacing?.remainingAmount ?: 0.0, 0.001)
        assertEquals(ButlerEvent.PROGRESS_LOGGED, state.activeEvent)
        assertTrue(state.activeDialogue.isNotBlank())
    }

    @Test
    fun logProgress_completesGoal_triggersGoalCompletedEvent() {
        viewModel.createGoal("운동", "50", "km", testToday.plusDays(10))
        val goalId = viewModel.uiState.value.goals[0].id

        val success = viewModel.logProgress(goalId, "50", "목표치 전량 달성")
        assertTrue(success)

        val state = viewModel.uiState.value
        val pacing = state.pacingResults[goalId]
        assertEquals(GoalStatus.COMPLETED, pacing?.status)
        assertEquals(ButlerEvent.GOAL_COMPLETED, state.activeEvent)
    }

    @Test
    fun reordering_movesGoalUpAndDown() {
        viewModel.createGoal("목표 1", "10", "개", testToday.plusDays(10))
        viewModel.createGoal("목표 2", "20", "개", testToday.plusDays(10))

        val g2Id = viewModel.uiState.value.goals[1].id

        viewModel.reorderUp(g2Id)
        var state = viewModel.uiState.value
        assertEquals("목표 2", state.goals[0].title)
        assertEquals("목표 1", state.goals[1].title)

        viewModel.reorderDown(g2Id)
        state = viewModel.uiState.value
        assertEquals("목표 1", state.goals[0].title)
        assertEquals("목표 2", state.goals[1].title)
    }

    @Test
    fun deleteGoal_removesGoalFromState() {
        viewModel.createGoal("임시 목표", "10", "개", testToday.plusDays(5))
        val goalId = viewModel.uiState.value.goals[0].id

        viewModel.deleteGoal(goalId)
        val state = viewModel.uiState.value
        assertTrue(state.goals.isEmpty())
        assertEquals(ButlerEvent.NO_GOALS, state.activeEvent)
    }

    @Test
    fun selectPersona_updatesPersonaAndActiveDialogue() {
        viewModel.selectPersona(ButlerPersona.CYNICAL)

        val state = viewModel.uiState.value
        assertEquals(ButlerPersona.CYNICAL, state.persona)
        assertTrue(state.activeDialogue.isNotBlank())
        assertFalse(state.isSettingsDialogOpen)
    }
}
