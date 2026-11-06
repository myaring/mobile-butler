package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalScreenViewModelTest {

    private lateinit var viewModel: GoalScreenViewModel

    @Before
    fun setUp() {
        viewModel = GoalScreenViewModel()
    }

    @Test
    fun initialState_isListTabWithActiveGoals() {
        val state = viewModel.uiState.value
        assertEquals(EGoalTab.LIST, state.selectedTab)
        assertEquals(0, state.subTabOrdinal)
        assertTrue(state.activeGoalsCount > 0)
        assertEquals("목표", viewModel.title.value)
        assertFalse(viewModel.showBackButton.value)
    }

    @Test
    fun selectGoal_transitionsToDetailTab() {
        val targetId = "sample-1"
        viewModel.selectGoal(targetId)

        val state = viewModel.uiState.value
        assertEquals(EGoalTab.DETAIL, state.selectedTab)
        assertEquals(targetId, state.selectedGoalId)
        assertEquals("목표 상세", viewModel.title.value)
        assertTrue(viewModel.showBackButton.value)
    }

    @Test
    fun onChangeTabToCreation_switchesTab() {
        viewModel.onChangeTabToCreation()

        val state = viewModel.uiState.value
        assertEquals(EGoalTab.CREATION, state.selectedTab)
        assertEquals("목표 만들기", viewModel.title.value)
        assertTrue(viewModel.showBackButton.value)
    }

    @Test
    fun onChangeTabToArchive_switchesTab() {
        viewModel.onChangeTabToArchive()

        val state = viewModel.uiState.value
        assertEquals(EGoalTab.ARCHIVE, state.selectedTab)
        assertEquals(1, state.subTabOrdinal)
        assertEquals("목표 보관함", viewModel.title.value)
        assertFalse(viewModel.showBackButton.value)
    }

    @Test
    fun onBackTab_returnsToList() {
        viewModel.onChangeTabToCreation()
        assertTrue(viewModel.showBackButton.value)

        viewModel.onBackTab()
        assertEquals(EGoalTab.LIST, viewModel.uiState.value.selectedTab)
        assertFalse(viewModel.showBackButton.value)
    }

    @Test
    fun activeGoals_filtersOutCompletedGoals() {
        val state = viewModel.uiState.value
        val allActive = state.activeGoals.all { it.status == GoalStatus.ACTIVE }
        assertTrue(allActive)
    }
}
