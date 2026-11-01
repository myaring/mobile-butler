package dev.myaring.mobile_butler.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["status", "priority"])
    ]
)
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String,
    val direction: String,
    val pacingPeriod: String,
    val unit: String,
    val targetValue: Double,
    val startValue: Double,
    val startDate: LocalDate,
    val dueDate: LocalDate?,
    val measurementCadence: String?,
    val trackingMode: String,
    val healthDataType: String?,
    val priority: Int,
    val restDays: Set<DayOfWeek>,
    val status: String,
    val endReason: String?,
    val endedAt: LocalDate?,
    val overdueNoticeShown: Boolean,
)