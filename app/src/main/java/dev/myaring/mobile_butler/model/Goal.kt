package dev.myaring.mobile_butler.model

import java.time.LocalDate

data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val unit: String = "쪽",
    val startDate: LocalDate,
    val deadline: LocalDate,
    val priority: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val progressPercent: Double
        get() = if (targetAmount <= 0.0) {
            100.0
        } else {
            ((currentAmount / targetAmount) * 100.0).coerceAtLeast(0.0)
        }
}
