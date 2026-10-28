package dev.myaring.mobile_butler.domain.repository

import dev.myaring.mobile_butler.domain.model.ProgressEntry
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface ProgressRepository {

    /**
     * Observe all entries belonging to a goal.
     *
     * Ordering:
     * 1. recordedAt ASC
     * 2. id ASC
     */
    fun observeEntries(
        goalId: String,
    ): Flow<List<ProgressEntry>>

    /**
     * Get one entry by id.
     */
    suspend fun getEntry(
        id: String,
    ): ProgressEntry?

    /**
     * Get entries by UTC time interval.
     *
     * Range: [from, until)
     *
     * This is NOT a local-date query.
     */
    suspend fun getEntriesBetween(
        goalId: String,
        from: Instant,
        until: Instant,
    ): List<ProgressEntry>

    /**
     * Add one entry.
     *
     * Must reject writes to COMPLETED goals.
     */
    suspend fun addEntry(
        entry: ProgressEntry,
    )

    /**
     * Update an existing entry.
     *
     * Must reject writes to COMPLETED goals.
     */
    suspend fun updateEntry(
        entry: ProgressEntry,
    )

    /**
     * Delete one entry.
     *
     * Must reject writes to COMPLETED goals.
     */
    suspend fun deleteEntry(
        id: String,
    )
}