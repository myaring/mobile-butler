package dev.myaring.mobile_butler.data.local

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import dev.myaring.mobile_butler.data.local.dao.GoalDao
import dev.myaring.mobile_butler.data.local.dao.ProgressEntryDao
import dev.myaring.mobile_butler.data.local.entity.GoalEntity
import dev.myaring.mobile_butler.data.local.entity.ProgressEntryEntity

@Database(
    entities = [GoalEntity::class, ProgressEntryEntity::class],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(RoomConverters::class)
abstract class ButlerDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun progressEntryDao(): ProgressEntryDao
}