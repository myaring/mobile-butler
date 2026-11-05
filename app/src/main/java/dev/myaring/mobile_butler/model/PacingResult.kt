package dev.myaring.mobile_butler.model

import java.util.Locale

data class PacingResult(
    val goalId: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val remainingAmount: Double,
    val dailyTarget: Double,
    val remainingDays: Long,
    val daysOverdue: Long,
    val progressPercent: Double,
    val status: GoalStatus,
    val statusMessage: String
) {
    val dailyTargetFormatted: String
        get() = formatNumber(dailyTarget)

    val remainingAmountFormatted: String
        get() = formatNumber(remainingAmount)

    val currentAmountFormatted: String
        get() = formatNumber(currentAmount)

    val targetAmountFormatted: String
        get() = formatNumber(targetAmount)

    val progressPercentFormatted: String
        get() = String.format(Locale.US, "%.1f%%", progressPercent)

    companion object {
        fun formatNumber(value: Double): String {
            return if (value % 1.0 == 0.0) {
                value.toLong().toString()
            } else {
                String.format(Locale.US, "%.1f", value)
            }
        }
    }
}
