package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Goal

object ButlerMessages {
    const val NO_GOAL: String = "오늘 등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오."
    const val ALL_TODAY_COMPLETED: String = "오늘 할당된 분량을 모두 완료하셨습니다. 편안한 휴식을 취하십시오."
    const val IN_PROGRESS_NONE: String = "오늘도 차근차근 목표를 향해 나아가 봅시다."
    const val IN_PROGRESS_PARTIAL: String = "오늘 분량을 순조롭게 진행하고 계십니다."
    const val DUE_TODAY: String = "오늘이 마감일입니다. 마지막까지 힘내시기 바랍니다."
    const val OVERDUE: String = "마감 기한이 지났습니다. 무리하지 마시고 일정을 점검해 보십시오."
    const val GOAL_ACHIEVED: String = "목표 분량을 모두 달성하셨습니다. 축하드립니다."

    fun getMessage(
        goal: Goal?,
        evaluation: GoalEvaluation?,
        recordState: RecordState = RecordState.NONE,
    ): String {
        if (goal == null || evaluation == null) {
            return NO_GOAL
        }

        return when (evaluation) {
            is GoalEvaluation.Completed -> GOAL_ACHIEVED
            is GoalEvaluation.Active -> {
                when {
                    evaluation.achieved -> GOAL_ACHIEVED
                    evaluation.overdue -> OVERDUE
                    evaluation.phase == GoalPhase.DUE_TODAY -> DUE_TODAY
                    recordState == RecordState.FULL -> ALL_TODAY_COMPLETED
                    recordState == RecordState.PARTIAL -> IN_PROGRESS_PARTIAL
                    else -> IN_PROGRESS_NONE
                }
            }
        }
    }
}
