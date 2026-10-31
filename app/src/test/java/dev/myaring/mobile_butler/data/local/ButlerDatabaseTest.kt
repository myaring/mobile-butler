package dev.myaring.mobile_butler.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.myaring.mobile_butler.domain.model.*
import dev.myaring.mobile_butler.data.repository.RoomGoalRepository
import dev.myaring.mobile_butler.data.repository.RoomProgressRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ButlerDatabaseTest {

    private lateinit var database: ButlerDatabase
    private lateinit var goals: RoomGoalRepository
    private lateinit var entries: RoomProgressRepository

    private val startDate = LocalDate.of(2026, 10, 28)
    private val dueDate = LocalDate.of(2026, 11, 15)

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder<ButlerDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        goals = RoomGoalRepository(database)
        entries = RoomProgressRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleGoal(
        id: String = "goal-1",
    ) = Goal(
        id = id,
        title = "독서 300쪽",
        unit = "쪽",
        targetValue = 300.0,
        startValue = 0.0,
        startDate = startDate,
        dueDate = dueDate,
        priority = 0,
        restDays = setOf(
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        ),
    )

    private fun sampleEntry() = ProgressEntry(
        id = "entry-1",
        goalId = "goal-1",
        recordedAt = Instant.parse(
            "2026-10-20T23:00:00Z"
        ),
        recordedOffset = ZoneOffset.ofHours(9),
        amount = 20.0,
    )

    @Test
    fun goalRoundTripPreservesAllFields() = runBlocking {
        val original = sampleGoal()

        goals.saveGoal(original)

        val restored = goals.getGoal(original.id)

        assertEquals(original, restored)
    }

    @Test
    fun entryRoundTripPreservesOffsetAndDate() = runBlocking {
        goals.saveGoal(sampleGoal())

        val original = sampleEntry()
        entries.addEntry(original)

        val restored = entries.getEntry(original.id)

        assertEquals(original, restored)
        assertEquals(
            LocalDate.of(2026, 10, 21),
            restored?.recordedDate,
        )
    }

    @Test
    fun completedGoalPreservesEntries() = runBlocking {
        goals.saveGoal(sampleGoal())
        entries.addEntry(sampleEntry())

        goals.completeGoal(
            id = "goal-1",
            endReason = EndReason.ACHIEVED,
            endedAt = dueDate,
        )

        val completed = goals.getGoal("goal-1")

        assertEquals(
            GoalStatus.COMPLETED,
            completed?.status,
        )
        assertEquals(
            EndReason.ACHIEVED,
            completed?.endReason,
        )
        assertEquals(
            dueDate,
            completed?.endedAt,
        )

        assertNotNull(entries.getEntry("entry-1"))
    }

    @Test
    fun completedGoalRejectsNewEntries() = runBlocking {
        goals.saveGoal(sampleGoal())

        goals.completeGoal(
            "goal-1",
            EndReason.MANUAL,
            startDate,
        )

        try {
            entries.addEntry(sampleEntry())
            fail("Expected completed goal to reject entry")
        } catch (_: IllegalStateException) {
            // Expected
        }
    }

    @Test
    fun priorityChangesArePersisted() = runBlocking {
        goals.saveGoal(sampleGoal("a").copy(priority = 0))
        goals.saveGoal(sampleGoal("b").copy(priority = 1))

        goals.updatePriorities(
            mapOf("a" to 1, "b" to 0)
        )

        assertEquals(1, goals.getGoal("a")?.priority)
        assertEquals(0, goals.getGoal("b")?.priority)
    }

    @Test
    fun reopeningClearsCompletionMetadata() = runBlocking {
        goals.saveGoal(sampleGoal())

        goals.completeGoal(
            "goal-1",
            EndReason.DEADLINE,
            dueDate,
        )

        goals.reopenGoal("goal-1")

        val restored = goals.getGoal("goal-1")

        assertEquals(GoalStatus.ACTIVE, restored?.status)
        assertNull(restored?.endReason)
        assertNull(restored?.endedAt)
    }
}