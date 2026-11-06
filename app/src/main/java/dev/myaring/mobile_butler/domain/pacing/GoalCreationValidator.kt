package dev.myaring.mobile_butler.domain.pacing

import java.time.LocalDate

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}

object GoalCreationValidator {
    fun validateTitle(title: String): ValidationResult {
        if (title.isBlank()) {
            return ValidationResult.Invalid("목표 이름을 입력해 주십시오.")
        }
        if (title.length > 50) {
            return ValidationResult.Invalid("목표 이름은 50자 이하여야 합니다.")
        }
        return ValidationResult.Valid
    }

    fun validateUnit(unit: String): ValidationResult {
        if (unit.isBlank()) {
            return ValidationResult.Invalid("단위를 입력해 주십시오.")
        }
        return ValidationResult.Valid
    }

    fun validateTargetAmount(rawInput: String): ValidationResult {
        if (rawInput.isBlank()) {
            return ValidationResult.Invalid("전체 분량을 입력해 주십시오.")
        }
        if (rawInput.contains(".")) {
            return ValidationResult.Invalid("전체 분량은 정수만 입력할 수 있습니다.")
        }
        val value = rawInput.toLongOrNull()
            ?: return ValidationResult.Invalid("유효한 정수 숫자를 입력해 주십시오.")
        if (value <= 0L) {
            return ValidationResult.Invalid("전체 분량은 0보다 커야 합니다.")
        }
        return ValidationResult.Valid
    }

    fun validateDates(startDate: LocalDate, dueDate: LocalDate?, today: LocalDate): ValidationResult {
        if (dueDate == null) {
            return ValidationResult.Valid
        }
        if (dueDate.isBefore(startDate)) {
            return ValidationResult.Invalid("마감일은 시작일보다 이전일 수 없습니다.")
        }
        return ValidationResult.Valid
    }
}
