package dev.myaring.mobile_butler.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.myaring.mobile_butler.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY priority ASC, id ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY priority ASC, id ASC")
    fun observeActive(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    //UPDATE 함수가 반환하는 Int는 영향을 받은 행의 수입니다.

    @Query("UPDATE goals SET priority = :priority WHERE id = :id")
    suspend fun setPriority(id: String, priority: Int): Int

    @Query("UPDATE goals SET dueDate = :dueDate, overdueNoticeShown = 0 WHERE id = :id AND status = 'ACTIVE'")
    suspend fun setDueDate(
        id: String,
        dueDate: String,
    ): Int

    @Query("UPDATE goals SET status = 'COMPLETED', endReason = :reason, endedAt = :endedAt WHERE id = :id AND status = 'ACTIVE'")
    suspend fun complete(
        id: String,
        reason: String,
        endedAt: String,
    ): Int

    @Query("UPDATE goals SET status = 'ACTIVE', endReason = NULL, endedAt = NULL WHERE id = :id AND status = 'COMPLETED'")
    suspend fun reopen(id: String): Int

    @Query("UPDATE goals SET overdueNoticeShown = :shown WHERE id = :id AND status = 'ACTIVE'")
    suspend fun setNoticeShown(
        id: String,
        shown: Boolean,
    ): Int
}