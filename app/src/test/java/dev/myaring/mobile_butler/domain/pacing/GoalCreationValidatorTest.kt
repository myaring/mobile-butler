package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.navigation.EGoalTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalCreationValidatorTest {

    private val today = LocalDate.of(2026, 11, 6)

    @Test
    fun validateTitle_blankTitle_returnsInvalid() {
        val result = GoalCreationValidator.validateTitle("   ")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateTitle_tooLongTitle_returnsInvalid() {
        val longTitle = "a".repeat(51)
        val result = GoalCreationValidator.validateTitle(longTitle)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateTitle_validTitle_returnsValid() {
        val result = GoalCreationValidator.validateTitle("토익 900점 완성")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun validateUnit_blankUnit_returnsInvalid() {
        val result = GoalCreationValidator.validateUnit("")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateUnit_validUnit_returnsValid() {
        val result = GoalCreationValidator.validateUnit("쪽")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun validateTargetAmount_decimal_returnsInvalid() {
        val result = GoalCreationValidator.validateTargetAmount("100.5")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("전체 분량은 정수만 입력할 수 있습니다.", (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun validateTargetAmount_zeroOrNegative_returnsInvalid() {
        val zeroResult = GoalCreationValidator.validateTargetAmount("0")
        assertTrue(zeroResult is ValidationResult.Invalid)

        val negativeResult = GoalCreationValidator.validateTargetAmount("-50")
        assertTrue(negativeResult is ValidationResult.Invalid)
    }

    @Test
    fun validateTargetAmount_nonNumber_returnsInvalid() {
        val result = GoalCreationValidator.validateTargetAmount("abc")
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateTargetAmount_validInteger_returnsValid() {
        val result = GoalCreationValidator.validateTargetAmount("300")
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun validateDates_dueDateBeforeStartDate_returnsInvalid() {
        val startDate = LocalDate.of(2026, 11, 10)
        val dueDate = LocalDate.of(2026, 11, 5)
        val result = GoalCreationValidator.validateDates(startDate, dueDate, today)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateDates_dueDateBeforeToday_returnsInvalid() {
        val startDate = LocalDate.of(2026, 11, 1)
        val pastDueDate = LocalDate.of(2026, 11, 5) // today is 11-06
        val result = GoalCreationValidator.validateDates(startDate, pastDueDate, today)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateDates_validFutureDueDate_returnsValid() {
        val startDate = today
        val dueDate = today.plusDays(30)
        val result = GoalCreationValidator.validateDates(startDate, dueDate, today)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun viewModel_createGoal_addsGoalAndNavigatesToListTab() {
        val viewModel = GoalScreenViewModel()
        viewModel.onChangeTabToCreation()
        assertEquals(EGoalTab.CREATION, viewModel.currentTab)

        val success = viewModel.createGoal(
            title = "책 300쪽 읽기",
            unit = "쪽",
            targetValue = 300.0,
            startDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(30)
        )

        assertTrue(success)
        assertEquals(EGoalTab.LIST, viewModel.currentTab)
        assertEquals(1, viewModel.uiState.value.goals.size)
        assertEquals("책 300쪽 읽기", viewModel.uiState.value.goals[0].title)
        assertEquals(300.0, viewModel.uiState.value.goals[0].targetValue, 0.001)
    }

    @Test
    fun allValidationMessages_containZeroEmojis() {
        val reasons = listOf(
            (GoalCreationValidator.validateTitle("") as ValidationResult.Invalid).reason,
            (GoalCreationValidator.validateUnit("") as ValidationResult.Invalid).reason,
            (GoalCreationValidator.validateTargetAmount("10.5") as ValidationResult.Invalid).reason,
            (GoalCreationValidator.validateTargetAmount("0") as ValidationResult.Invalid).reason,
            (GoalCreationValidator.validateDates(today, today.minusDays(1), today) as ValidationResult.Invalid).reason
        )

        for (reason in reasons) {
            val hasEmoji = reason.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Reason must not contain emoji: $reason", hasEmoji)
        }
    }
}
