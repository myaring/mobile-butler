package dev.myaring.mobile_butler.data.repository

import androidx.room3.withWriteTransaction
import dev.myaring.mobile_butler.data.local.ButlerDatabase
import dev.myaring.mobile_butler.data.mapper.toDomain
import dev.myaring.mobile_butler.data.mapper.toEntity
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class RoomProgressRepository(
    private val database: ButlerDatabase,
) : ProgressRepository {

    private val dao = database.progressEntryDao()
    private val goals = database.goalDao()

    override fun observeEntries(goalId: String): Flow<List<ProgressEntry>> =
        dao.observeByGoal(goalId).map { list -> list.map { it.toDomain() } }

    override suspend fun getEntry(id: String): ProgressEntry? =
        dao.getById(id)?.toDomain()

    override suspend fun getEntriesBetween(
        goalId: String,
        from: Instant,
        until: Instant,
    ): List<ProgressEntry> {
        require(from < until)
        return dao.getBetween(goalId, from.toEpochMilli(), until.toEpochMilli())
            .map { it.toDomain() }
    }

    private suspend fun requireWritable(goalId: String) {
        val goal = requireNotNull(goals.getById(goalId)) { "Goal not found: $goalId" }
        check(goal.status == "ACTIVE") { "Completed goal is read-only." }
    }

    override suspend fun addEntry(entry: ProgressEntry) {
        database.withWriteTransaction {
            requireWritable(entry.goalId)
            dao.insert(entry.toEntity())
        }
    }

    override suspend fun updateEntry(entry: ProgressEntry) {
        database.withWriteTransaction {
            val existing = requireNotNull(dao.getById(entry.id))
            require(existing.goalId == entry.goalId)
            requireWritable(existing.goalId)
            check(dao.update(entry.toEntity()) == 1)
        }
    }

    override suspend fun deleteEntry(id: String) {
        database.withWriteTransaction {
            val existing = requireNotNull(dao.getById(id))
            requireWritable(existing.goalId)
            check(dao.deleteById(id) == 1)
        }
    }
}