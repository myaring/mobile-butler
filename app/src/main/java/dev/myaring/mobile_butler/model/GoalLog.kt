package dev.myaring.mobile_butler.model

import java.time.LocalDate

data class GoalLog(
    val id: String,
    val goalId: String,
    val date: LocalDate,
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
