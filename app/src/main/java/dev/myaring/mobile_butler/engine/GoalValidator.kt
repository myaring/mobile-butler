package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.ValidationResult
import java.time.LocalDate

object GoalValidator {

    private const val MAX_AMOUNT = 1_000_000_000.0

    fun validateGoalInput(
        title: String,
        targetAmountText: String,
        deadline: LocalDate?,
        today: LocalDate = LocalDate.now()
    ): ValidationResult {
        if (title.trim().isEmpty()) {
            return ValidationResult.Error("목표 이름을 입력해주세요.")
        }

        if (title.trim().length > 100) {
            return ValidationResult.Error("목표 이름은 100자 이하로 입력해주세요.")
        }

        val trimmedAmount = targetAmountText.trim()
        if (trimmedAmount.isEmpty()) {
            return ValidationResult.Error("목표치를 입력해주세요.")
        }

        val amount = trimmedAmount.toDoubleOrNull()
            ?: return ValidationResult.Error("목표치는 올바른 숫자여야 합니다.")

        if (amount.isNaN() || amount.isInfinite()) {
            return ValidationResult.Error("목표치는 유효한 숫자여야 합니다.")
        }

        if (amount <= 0.0) {
            return ValidationResult.Error("목표치는 0보다 큰 숫자여야 합니다.")
        }

        if (amount > MAX_AMOUNT) {
            return ValidationResult.Error("목표치는 10억 이하로 입력해주세요.")
        }

        if (deadline == null) {
            return ValidationResult.Error("마감일을 선택해주세요.")
        }

        if (deadline.isBefore(today)) {
            return ValidationResult.Error("마감일은 오늘 이후 날짜여야 합니다.")
        }

        return ValidationResult.Success
    }

    fun validateProgressLogInput(
        amountText: String
    ): ValidationResult {
        val trimmed = amountText.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("기록할 분량을 입력해주세요.")
        }

        val amount = trimmed.toDoubleOrNull()
            ?: return ValidationResult.Error("기록할 분량은 올바른 숫자여야 합니다.")

        if (amount.isNaN() || amount.isInfinite()) {
            return ValidationResult.Error("기록할 분량은 유효한 숫자여야 합니다.")
        }

        if (amount <= 0.0) {
            return ValidationResult.Error("기록할 분량은 0보다 큰 숫자여야 합니다.")
        }

        if (amount > MAX_AMOUNT) {
            return ValidationResult.Error("기록할 분량은 10억 이하로 입력해주세요.")
        }

        return ValidationResult.Success
    }
}
