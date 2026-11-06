package dev.myaring.mobile_butler.domain.model

enum class GoalStatus {
    ACTIVE,
    COMPLETED,

    @Deprecated(
        message = "Use COMPLETED with EndReason instead. See #39."
    )
    ARCHIVED,
}