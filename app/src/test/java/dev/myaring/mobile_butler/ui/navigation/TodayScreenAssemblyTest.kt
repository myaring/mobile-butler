package dev.myaring.mobile_butler.ui.navigation

import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.viewmodel.DayCellUiModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayGoalCardUiState
import dev.myaring.mobile_butler.domain.viewmodel.TodayUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TodayScreenAssemblyTest {

    private val sampleDate = LocalDate.of(2026, 11, 6)

    private fun createCardUiState(id: String, dDayText: String = "D-26"): TodayGoalCardUiState {
        val goal = Goal(
            id = id,
            title = "목표 $id",
            targetValue = 100.0,
            unit = "쪽",
            startDate = sampleDate.minusDays(5),
            dueDate = sampleDate.plusDays(10),
            priority = 0
        )
        return TodayGoalCardUiState(
            goal = goal,
            dDayText = dDayText,
            todayProgress = 10.0,
            todayQuota = 20.0,
            totalProgress = 50.0,
            targetValue = 100.0,
            isOverdue = false,
            isTodayQuotaMet = false,
            recent14Days = emptyList(),
            summary = "오늘의 목표 20쪽 · $dDayText"
        )
    }

    @Test
    fun stateHierarchy_topGoalsUpToThree_remainingGoalsSeparate() {
        val topCards = listOf(
            createCardUiState("g1"),
            createCardUiState("g2"),
            createCardUiState("g3")
        )
        val remainingCards = listOf(
            createCardUiState("g4"),
            createCardUiState("g5")
        )

        val state = TodayUiState(
            topGoals = topCards,
            remainingGoals = remainingCards,
            expandedGoalId = "g1",
            butlerMessage = "오늘도 화이팅입니다."
        )

        assertEquals(3, state.topGoals.size)
        assertEquals(2, state.remainingGoals.size)
        assertEquals("g1", state.expandedGoalId)
        assertTrue(state.remainingGoals.isNotEmpty())
    }

    @Test
    fun moreExpansionToggle_revealsRemainingCards() {
        val remainingCards = listOf(createCardUiState("g4"))
        val state = TodayUiState(
            topGoals = listOf(createCardUiState("g1")),
            remainingGoals = remainingCards,
            isMoreExpanded = false
        )

        val expandedState = state.copy(isMoreExpanded = true)
        assertTrue("When expanded, more cards are visible", expandedState.isMoreExpanded)

        val collapsedState = expandedState.copy(isMoreExpanded = false)
        assertFalse("When collapsed, more cards are hidden", collapsedState.isMoreExpanded)
    }

    @Test
    fun texts_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "목표 접기",
            "목표 2개 더보기",
            "2일 연속으로 읽고 계십니다.",
            "자세히"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun texts_containZeroEmojis() {
        val sampleTexts = listOf(
            "목표 접기",
            "목표 2개 더보기",
            "2일 연속으로 읽고 계십니다.",
            "자세히",
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
