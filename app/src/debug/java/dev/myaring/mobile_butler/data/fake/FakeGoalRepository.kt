package dev.myaring.mobile_butler.data.fake

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * [A-7] 디버그용 메모리 기반 가짜 GoalRepository 구현체.
 * 릴리스 빌드에는 포함되지 않습니다.
 */
class FakeGoalRepository(
    initialGoals: List<Goal> = emptyList()
) : GoalRepository {

    private val goalsMap = mutableMapOf<String, Goal>().apply {
        initialGoals.forEach { put(it.id, it) }
    }
    private val flow = MutableStateFlow(goalsMap.toMap())

    override fun observeGoals(): Flow<List<Goal>> =
        flow.map { it.values.sortedWith(compareBy({ g -> g.priority }, { g -> g.id })) }

    override fun observeGoal(id: String): Flow<Goal?> =
        flow.map { it[id] }

    override suspend fun getGoal(id: String): Goal? =
        goalsMap[id]

    override suspend fun saveGoal(goal: Goal) {
        goalsMap[goal.id] = goal
        flow.value = goalsMap.toMap()
    }

    override suspend fun updateDueDate(id: String, dueDate: LocalDate) {
        goalsMap[id]?.let { current ->
            goalsMap[id] = current.copy(dueDate = dueDate, overdueNoticeShown = false)
            flow.value = goalsMap.toMap()
        }
    }

    override suspend fun updatePriorities(priorities: Map<String, Int>) {
        priorities.forEach { (id, priority) ->
            goalsMap[id]?.let { current ->
                goalsMap[id] = current.copy(priority = priority)
            }
        }
        flow.value = goalsMap.toMap()
    }

    override suspend fun completeGoal(id: String, endReason: EndReason, endedAt: LocalDate) {
        goalsMap[id]?.let { current ->
            goalsMap[id] = current.copy(
                status = GoalStatus.COMPLETED,
                endReason = endReason,
                endedAt = endedAt
            )
            flow.value = goalsMap.toMap()
        }
    }

    override suspend fun reopenGoal(id: String) {
        goalsMap[id]?.let { current ->
            goalsMap[id] = current.copy(
                status = GoalStatus.ACTIVE,
                endReason = null,
                endedAt = null
            )
            flow.value = goalsMap.toMap()
        }
    }

    override suspend fun setOverdueNoticeShown(id: String, shown: Boolean) {
        goalsMap[id]?.let { current ->
            goalsMap[id] = current.copy(overdueNoticeShown = shown)
            flow.value = goalsMap.toMap()
        }
    }
}
