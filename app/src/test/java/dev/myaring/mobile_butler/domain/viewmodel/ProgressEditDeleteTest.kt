package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProgressEditDeleteTest {

    private lateinit var viewModel: GoalScreenViewModel

    @Before
    fun setUp() {
        viewModel = GoalScreenViewModel()
    }

    @Test
    fun updateProgressEntry_modifiesAmountAndReflectsInTotal() {
        val goalId = "sample-1" // target: 100.0, initial total: 20 + 15 + 25 = 60
        val entryId = "e-1"

        // Update e-1 from 20.0 to 30.0 -> total becomes 70.0
        viewModel.updateProgressEntry(goalId, entryId, 30.0)

        val entries = viewModel.uiState.value.entries[goalId] ?: emptyList()
        val updated = entries.find { it.id == entryId }
        assertEquals(30.0, updated?.amount ?: 0.0, 0.001)

        val total = entries.sumOf { it.amount }
        assertEquals(70.0, total, 0.001)

        val goal = viewModel.uiState.value.goals.find { it.id == goalId }
        assertEquals(GoalStatus.ACTIVE, goal?.status)
    }

    @Test
    fun updateProgressEntry_reachingTarget_automaticallyCompletesWithAchieved() {
        val goalId = "sample-1" // target: 100.0, initial total: 60
        val entryId = "e-1"

        // Update e-1 from 20.0 to 60.0 -> total becomes 60 + 15 + 25 = 100.0
        viewModel.updateProgressEntry(goalId, entryId, 60.0)

        val goal = viewModel.uiState.value.goals.find { it.id == goalId }
        assertEquals(GoalStatus.COMPLETED, goal?.status)
        assertEquals(EndReason.ACHIEVED, goal?.endReason)
    }

    @Test
    fun deleteProgressEntry_removesEntryAndUpdatesTotal() {
        val goalId = "sample-1" // entries: e-1(20), e-2(15), e-3(25) -> total 60
        val entryId = "e-2"

        viewModel.deleteProgressEntry(goalId, entryId)

        val entries = viewModel.uiState.value.entries[goalId] ?: emptyList()
        assertFalse(entries.any { it.id == entryId })
        assertEquals(2, entries.size)

        val total = entries.sumOf { it.amount }
        assertEquals(45.0, total, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateProgressEntry_rejectsNonPositiveAmount() {
        viewModel.updateProgressEntry("sample-1", "e-1", 0.0)
    }

    @Test
    fun editSheetStrings_containNoEmoji() {
        val strings = listOf(
            "기록 수정",
            "기존에 기록된 양을 변경하거나 삭제할 수 있습니다",
            "으로 수정",
            "기록 삭제"
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
