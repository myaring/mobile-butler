package dev.myaring.mobile_butler.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

data class Goal(
    val id: String,
    val title: String,

    val type: GoalType = GoalType.CUMULATIVE,
    val direction: Direction = Direction.UP,
    val pacingPeriod: PacingPeriod = PacingPeriod.DAY,

    val unit: String,
    val targetValue: Double,
    val startValue: Double = 0.0,

    val startDate: LocalDate,
    val dueDate: LocalDate?,

    val measurementCadence: Cadence? = null,
    val trackingMode: TrackingMode = TrackingMode.MANUAL,
    val healthDataType: HealthDataType? = null,

    val priority: Int,
    val restDays: Set<DayOfWeek> = emptySet(),

    val status: GoalStatus = GoalStatus.ACTIVE,
    val endReason: EndReason? = null,
    val endedAt: LocalDate? = null,
    val overdueNoticeShown: Boolean = false,
) {
    init {
        require(id.isNotBlank()) {
            "Goal id must not be blank."
        }

        require(title.isNotBlank()) {
            "Goal title must not be blank."
        }

        require(targetValue.isFinite()) {
            "Target value must be finite."
        }

        require(startValue.isFinite()) {
            "Start value must be finite."
        }

        require(targetValue > startValue) {
            "Target value must be greater than start value."
        }

        require(dueDate == null || !dueDate.isBefore(startDate)) {
            "Due date must not be earlier than start date."
        }

        when (status) {
            GoalStatus.ACTIVE -> {
                require(endReason == null && endedAt == null) {
                    "Active goals must not have completion data."
                }
            }

            GoalStatus.COMPLETED -> {
                require(endReason != null && endedAt != null) {
                    "Completed goals must have completion data."
                }
            }

            @Suppress("DEPRECATION")
            GoalStatus.ARCHIVED -> {
                // Legacy compatibility only.
            }
        }
    }
}