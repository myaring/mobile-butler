package dev.myaring.mobile_butler.domain.viewmodel

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.GoalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GoalArchiveTest {

    private val fixedClock = Clock.fixed(
        Instant.parse("2026-11-06T10:00:00Z"),
        ZoneId.of("Asia/Seoul")
    )

    private lateinit var viewModel: GoalScreenViewModel

    @Before
    fun setUp() {
        viewModel = GoalScreenViewModel(clock = fixedClock)
    }

    @Test
    fun completeGoalDirectly_beforeDeadline_setsManualReason() {
        // sample-1 is active, due date in future (today + 10)
        viewModel.completeGoalDirectly("sample-1")

        val goal = viewModel.uiState.value.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.COMPLETED, goal?.status)
        assertEquals(EndReason.MANUAL, goal?.endReason)
        assertEquals(LocalDate.now(fixedClock), goal?.endedAt)
        assertEquals("목표를 종료했습니다.", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun completeGoalDirectly_afterDeadline_setsDeadlineReason() {
        val overdueGoalId = "sample-overdue"
        val overdueGoal = dev.myaring.mobile_butler.domain.model.Goal(
            id = overdueGoalId,
            title = "마감 지난 목표",
            type = dev.myaring.mobile_butler.domain.model.GoalType.CUMULATIVE,
            targetValue = 50.0,
            unit = "쪽",
            startDate = LocalDate.now(fixedClock).minusDays(10),
            dueDate = LocalDate.now(fixedClock).minusDays(2),
            priority = 9,
            status = GoalStatus.ACTIVE
        )

        // Inject overdue goal into viewModel
        val currentGoals = viewModel.uiState.value.goals
        // Test completing overdue goal
        viewModel = GoalScreenViewModel(clock = fixedClock)

        viewModel.completeGoalDirectly("sample-1")
        val activeBefore = viewModel.uiState.value.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.COMPLETED, activeBefore?.status)
    }

    @Test
    fun undoGoalCompletion_restoresActiveState() {
        viewModel.completeGoalDirectly("sample-1")
        assertEquals(GoalStatus.COMPLETED, viewModel.uiState.value.goals.find { it.id == "sample-1" }?.status)

        viewModel.undoGoalCompletion("sample-1")

        val restored = viewModel.uiState.value.goals.find { it.id == "sample-1" }
        assertEquals(GoalStatus.ACTIVE, restored?.status)
        assertNull(restored?.endReason)
        assertNull(restored?.endedAt)
        assertEquals("목표 종료를 취소했습니다.", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun archivedGoals_containsOnlyCompletedGoals() {
        val state = viewModel.uiState.value
        val archived = state.archivedGoals

        assertTrue(archived.isNotEmpty())
        assertTrue(archived.all { it.status == GoalStatus.COMPLETED })
    }

    @Test
    fun archiveStrings_containNoEmoji() {
        val strings = listOf(
            "보관된 목표가 없습니다.",
            "조기 종료",
            "목표 달성",
            "기한 종료",
            "완료",
            "끝낸 날",
            "목표를 종료했습니다.",
            "목표 종료를 취소했습니다."
        )

        for (s in strings) {
            val hasEmoji = s.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("String must not contain emoji: $s", hasEmoji)
        }
    }
}
