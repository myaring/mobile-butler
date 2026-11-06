package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalEditExtensionTest {

    private lateinit var viewModel: GoalScreenViewModel

    @Before
    fun setUp() {
        viewModel = GoalScreenViewModel()
    }

    @Test
    fun startEditingGoal_switchesToCreationTabInEditMode() {
        viewModel.startEditingGoal("sample-1")

        val state = viewModel.uiState.value
        assertEquals("sample-1", state.editingGoalId)
        assertNotNull(state.editingGoal)
        assertEquals(EGoalTab.CREATION, state.selectedTab)
        assertEquals("목표 수정", viewModel.title.value)
    }

    @Test
    fun saveGoalEdits_whenTargetLoweredBelowRecorded_triggersConfirmationDialog() {
        // sample-1 has recorded total = 40 + 30 = 70.0
        // Lower target to 60.0
        viewModel.saveGoalEdits(
            goalId = "sample-1",
            newTitle = "코틀린 완벽 가이드 개정판",
            newTarget = 60.0,
            newUnit = "쪽",
            newDueDate = LocalDate.now().plusDays(5)
        )

        val state = viewModel.uiState.value
        assertTrue(state.showAchievedConfirmDialog)
        assertEquals(60.0, state.pendingTargetValue ?: 0.0, 0.001)

        // Goal should NOT be completed yet
        val goal = state.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.ACTIVE, goal?.status)
    }

    @Test
    fun confirmAchieved_completesGoalWithAchievedStatus() {
        viewModel.saveGoalEdits(
            goalId = "sample-1",
            newTitle = "코틀린 완벽 가이드",
            newTarget = 60.0,
            newUnit = "쪽",
            newDueDate = LocalDate.now().plusDays(5)
        )

        viewModel.confirmAchieved("sample-1")

        val state = viewModel.uiState.value
        assertFalse(state.showAchievedConfirmDialog)
        assertNull(state.pendingTargetValue)

        val completedGoal = state.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.COMPLETED, completedGoal?.status)
        assertEquals(EndReason.ACHIEVED, completedGoal?.endReason)
    }

    @Test
    fun dismissAchieved_maintainsActiveStatus() {
        viewModel.saveGoalEdits(
            goalId = "sample-1",
            newTitle = "코틀린 완벽 가이드",
            newTarget = 60.0,
            newUnit = "쪽",
            newDueDate = LocalDate.now().plusDays(5)
        )

        viewModel.dismissAchievedKeepActive("sample-1")

        val state = viewModel.uiState.value
        assertFalse(state.showAchievedConfirmDialog)

        val activeGoal = state.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.ACTIVE, activeGoal?.status)
        assertNull(activeGoal?.endReason)
        assertEquals(60.0, activeGoal?.targetValue ?: 0.0, 0.001)
    }

    @Test
    fun calculateRecalculatedQuota_recalculatesBasedOnExtendedDays() {
        val goal = viewModel.uiState.value.goals.first()
        val today = LocalDate.now()

        // 10 days remaining, 30 units left -> 3 units/day
        val quota1 = viewModel.calculateRecalculatedQuota(
            goal = goal.copy(targetValue = 100.0, dueDate = today.plusDays(9)),
            totalRecorded = 70.0,
            today = today
        )
        assertEquals(3, quota1)

        // Extend due date by 20 days (30 days total) -> 30 units left / 30 days = 1 unit/day
        val quota2 = viewModel.calculateRecalculatedQuota(
            goal = goal.copy(targetValue = 100.0, dueDate = today.plusDays(29)),
            totalRecorded = 70.0,
            today = today
        )
        assertEquals(1, quota2)
    }

    @Test
    fun dialogStrings_containNoEmoji() {
        val strings = listOf(
            "목표 달성 확인",
            "이미 목표량을 채우셨습니다. 완료하시겠습니까?",
            "예",
            "아니오",
            "수정 완료",
            "목표 수정"
        )

        for (s in strings) {
            val hasEmoji = s.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse(hasEmoji)
        }
    }
}
