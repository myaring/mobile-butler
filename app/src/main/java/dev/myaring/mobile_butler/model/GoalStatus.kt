package dev.myaring.mobile_butler.model

enum class GoalStatus(val label: String) {
    ON_TRACK("진행 중"),
    D_DAY("오늘 마감"),
    COMPLETED("달성 완료"),
    OVERDUE("기한 지남")
}
