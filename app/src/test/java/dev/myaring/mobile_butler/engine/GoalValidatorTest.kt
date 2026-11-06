package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalValidatorTest {

    private val today = LocalDate.of(2026, 11, 5)

    @Test
    fun validateGoalInput_validData_returnsSuccess() {
        val result = GoalValidator.validateGoalInput(
            title = "책 읽기",
            targetAmountText = "300",
            deadline = today.plusDays(10),
            today = today
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateGoalInput_blankTitle_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "   ",
            targetAmountText = "300",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표 이름을 입력해주세요.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_titleTooLong_returnsError() {
        val longTitle = "a".repeat(101)
        val result = GoalValidator.validateGoalInput(
            title = longTitle,
            targetAmountText = "300",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표 이름은 100자 이하로 입력해주세요.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_blankTargetAmount_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표치를 입력해주세요.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_nonNumericTargetAmount_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "abc",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표치는 올바른 숫자여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_zeroTargetAmount_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "0",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표치는 0보다 큰 숫자여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_negativeTargetAmount_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "-10",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표치는 0보다 큰 숫자여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_excessiveTargetAmount_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "1000000001",
            deadline = today.plusDays(10),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("목표치는 10억 이하로 입력해주세요.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_nullDeadline_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "100",
            deadline = null,
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("마감일을 선택해주세요.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_pastDeadline_returnsError() {
        val result = GoalValidator.validateGoalInput(
            title = "운동하기",
            targetAmountText = "100",
            deadline = today.minusDays(1),
            today = today
        )
        assertFalse(result.isSuccess)
        assertEquals("마감일은 오늘 이후 날짜여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateGoalInput_todayDeadline_returnsSuccess() {
        val result = GoalValidator.validateGoalInput(
            title = "오늘 할 일",
            targetAmountText = "10",
            deadline = today,
            today = today
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateProgressLogInput_validNumber_returnsSuccess() {
        val result = GoalValidator.validateProgressLogInput("15.5")
        assertTrue(result.isSuccess)
    }

    @Test
    fun validateProgressLogInput_blankInput_returnsError() {
        val result = GoalValidator.validateProgressLogInput("   ")
        assertFalse(result.isSuccess)
        assertEquals("기록할 분량을 입력해주세요.", result.errorMessage)
    }

    @Test
    fun validateProgressLogInput_nonNumericInput_returnsError() {
        val result = GoalValidator.validateProgressLogInput("twenty")
        assertFalse(result.isSuccess)
        assertEquals("기록할 분량은 올바른 숫자여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateProgressLogInput_zeroInput_returnsError() {
        val result = GoalValidator.validateProgressLogInput("0")
        assertFalse(result.isSuccess)
        assertEquals("기록할 분량은 0보다 큰 숫자여야 합니다.", result.errorMessage)
    }

    @Test
    fun validateProgressLogInput_negativeInput_returnsError() {
        val result = GoalValidator.validateProgressLogInput("-5")
        assertFalse(result.isSuccess)
        assertEquals("기록할 분량은 0보다 큰 숫자여야 합니다.", result.errorMessage)
    }
}
