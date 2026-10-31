package dev.myaring.mobile_butler.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import dev.myaring.mobile_butler.data.local.entity.ProgressEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressEntryDao {

    @Query("SELECT * FROM progress_entries WHERE goalId = :goalId ORDER BY recordedAt ASC, id ASC")
    fun observeByGoal(goalId: String): Flow<List<ProgressEntryEntity>>

    @Query("SELECT * FROM progress_entries WHERE id = :id")
    suspend fun getById(id: String): ProgressEntryEntity?

    @Query("SELECT * FROM progress_entries WHERE goalId = :goalId AND recordedAt >= :from AND recordedAt < :until ORDER BY recordedAt ASC, id ASC")
    suspend fun getBetween(goalId: String, from: Long, until: Long): List<ProgressEntryEntity>

    @Query("SELECT * FROM progress_entries WHERE goalId = :goalId ORDER BY recordedAt ASC, id ASC")
    suspend fun getAllByGoal(goalId: String): List<ProgressEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: ProgressEntryEntity)

    @Update
    suspend fun update(entry: ProgressEntryEntity): Int

    @Query("DELETE FROM progress_entries WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
