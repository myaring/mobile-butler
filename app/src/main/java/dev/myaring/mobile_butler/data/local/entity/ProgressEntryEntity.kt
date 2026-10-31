package dev.myaring.mobile_butler.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant
import java.time.ZoneOffset

@Entity(
    tableName = "progress_entries",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["goalId", "recordedAt"])
    ]
)
data class ProgressEntryEntity(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val recordedAt: Instant,
    val recordedOffset: ZoneOffset,
    val amount: Double,
    val source: String,
)