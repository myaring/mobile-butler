package dev.myaring.mobile_butler.ui.navigation

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GoalDetailTest {

    private lateinit var viewModel: GoalScreenViewModel

    @Before
    fun setUp() {
        viewModel = GoalScreenViewModel()
    }

    @Test
    fun selectedGoal_isInitiallySampleGoal() {
        val selected = viewModel.uiState.value.selectedGoal
        assertNotNull(selected)
        assertEquals("sample-1", selected?.id)
        assertEquals(GoalStatus.ACTIVE, selected?.status)
    }

    @Test
    fun manualCompletion_updatesGoalStatusAndEndReason() {
        val goalId = "sample-1"
        viewModel.completeGoalManually(goalId)

        val updated = viewModel.uiState.value.goals.find { it.id == goalId }
        assertEquals(GoalStatus.COMPLETED, updated?.status)
        assertEquals(EndReason.MANUAL, updated?.endReason)
        assertEquals(LocalDate.now(), updated?.endedAt)
    }

    @Test
    fun extendDueDate_updatesDueDateAndResetsNotice() {
        val goalId = "sample-1"
        val newDue = LocalDate.now().plusDays(20)
        viewModel.extendDueDate(goalId, newDue)

        val updated = viewModel.uiState.value.goals.find { it.id == goalId }
        assertEquals(newDue, updated?.dueDate)
        assertFalse(updated?.overdueNoticeShown ?: true)
    }

    @Test
    fun recordProgress_appendsNewEntryToSelectedGoal() {
        val goalId = "sample-1"
        val initialCount = viewModel.uiState.value.entries[goalId]?.size ?: 0

        viewModel.recordProgress(goalId, 15.0)

        val updatedEntries = viewModel.uiState.value.entries[goalId]
        assertEquals(initialCount + 1, updatedEntries?.size)
        assertEquals(15.0, updatedEntries?.last()?.amount ?: 0.0, 0.001)
    }

    @Test
    fun endReasonLabels_containNoEmoji() {
        val labels = listOf("조기 종료", "목표 달성", "마감 기한 종료", "종료")
        for (label in labels) {
            val hasEmoji = label.codePoints().anyMatch { cp ->
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
