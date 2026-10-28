package dev.myaring.mobile_butler.data.repository

import androidx.room3.withWriteTransaction
import dev.myaring.mobile_butler.data.local.ButlerDatabase
import dev.myaring.mobile_butler.data.mapper.toDomain
import dev.myaring.mobile_butler.data.mapper.toEntity
import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomGoalRepository(
    private val database: ButlerDatabase,
) : GoalRepository {

    private val dao = database.goalDao()

    override fun observeGoals(): Flow<List<Goal>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeGoal(id: String): Flow<Goal?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getGoal(id: String): Goal? =
        dao.getById(id)?.toDomain()

    override suspend fun saveGoal(goal: Goal) {
        @Suppress("DEPRECATION")
        require(goal.status != GoalStatus.ARCHIVED)
        dao.upsert(goal.toEntity())
    }

    override suspend fun updateDueDate(id: String, dueDate: LocalDate) {
        database.withWriteTransaction {
            val goal = requireNotNull(dao.getById(id)) { "Goal not found: $id" }.toDomain()
            check(goal.status == GoalStatus.ACTIVE)
            require(!dueDate.isBefore(goal.startDate))
            check(dao.setDueDate(id, dueDate.toString()) == 1)
        }
    }

    override suspend fun updatePriorities(priorities: Map<String, Int>) {
        database.withWriteTransaction {
            priorities.forEach { (id, priority) ->
                check(dao.setPriority(id, priority) == 1) { "Goal not found: $id" }
            }
        }
    }

    override suspend fun completeGoal(id: String, endReason: EndReason, endedAt: LocalDate) {
        database.withWriteTransaction {
            check(dao.complete(id, endReason.name, endedAt.toString()) == 1) {
                "Goal not found or not ACTIVE: $id"
            }
        }
    }

    override suspend fun reopenGoal(id: String) {
        database.withWriteTransaction {
            check(dao.reopen(id) == 1) { "Goal not found or not COMPLETED: $id" }
        }
    }

    override suspend fun setOverdueNoticeShown(id: String, shown: Boolean) {
        check(dao.setNoticeShown(id, shown) == 1) { "Goal not found or not ACTIVE: $id" }
    }
}