package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.EntrySource
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ProgressRecordFlowTest {

    private val fixedClock = Clock.fixed(
        Instant.parse("2026-11-06T10:00:00Z"),
        ZoneId.of("Asia/Seoul")
    )

    private lateinit var goalRepository: TestGoalRepository
    private lateinit var progressRepository: TestProgressRepository
    private lateinit var flow: ProgressRecordFlow

    private val sampleGoal = Goal(
        id = "goal-1",
        title = "코틀린 완벽 가이드",
        type = GoalType.CUMULATIVE,
        targetValue = 100.0,
        unit = "쪽",
        startDate = LocalDate.of(2026, 11, 1),
        dueDate = LocalDate.of(2026, 11, 10),
        status = GoalStatus.ACTIVE,
        priority = 0
    )

    @Before
    fun setUp() {
        goalRepository = TestGoalRepository()
        progressRepository = TestProgressRepository()
        flow = ProgressRecordFlow(
            goalRepository = goalRepository,
            progressRepository = progressRepository,
            clock = fixedClock
        )
        runBlocking {
            goalRepository.saveGoal(sampleGoal)
        }
    }

    @Test
    fun saveRecord_savesEntryAndFormatsMessage() = runBlocking {
        val result = flow.saveRecord(
            goal = sampleGoal,
            amount = 20.0,
            existingEntries = emptyList()
        )

        assertEquals("오늘 20쪽을 모두 기록했습니다", result.message)
        assertFalse(result.isCompleted)
        assertEquals(20.0, result.entry.amount, 0.001)
        assertEquals(1, progressRepository.entries.size)
        assertEquals("goal-1", progressRepository.entries.first().goalId)
    }

    @Test
    fun saveRecord_reachingTarget_completesGoal() = runBlocking {
        val existing = listOf(
            ProgressEntry(
                id = "p-1",
                goalId = sampleGoal.id,
                recordedAt = Instant.parse("2026-11-05T10:00:00Z"),
                recordedOffset = ZoneOffset.ofHours(9),
                amount = 80.0,
                source = EntrySource.MANUAL
            )
        )

        val result = flow.saveRecord(
            goal = sampleGoal,
            amount = 20.0,
            existingEntries = existing
        )

        assertTrue(result.isCompleted)
        assertEquals(GoalStatus.COMPLETED, result.updatedGoal.status)
        assertEquals(EndReason.ACHIEVED, result.updatedGoal.endReason)

        val storedGoal = goalRepository.getGoal(sampleGoal.id)
        assertEquals(GoalStatus.COMPLETED, storedGoal?.status)
        assertEquals(EndReason.ACHIEVED, storedGoal?.endReason)
    }

    @Test(expected = IllegalArgumentException::class)
    fun saveRecord_rejectsNonPositiveAmount() = runBlocking {
        flow.saveRecord(goal = sampleGoal, amount = 0.0)
        Unit
    }

    @Test
    fun undoRecord_restoresCompletedGoalToActive() = runBlocking {
        val completedGoal = sampleGoal.copy(
            status = GoalStatus.COMPLETED,
            endReason = EndReason.ACHIEVED,
            endedAt = LocalDate.of(2026, 11, 6)
        )
        goalRepository.saveGoal(completedGoal)

        val entry = ProgressEntry(
            id = "entry-undo",
            goalId = completedGoal.id,
            recordedAt = Instant.parse("2026-11-06T10:00:00Z"),
            recordedOffset = ZoneOffset.ofHours(9),
            amount = 20.0,
            source = EntrySource.MANUAL
        )
        progressRepository.addEntry(entry)

        val restored = flow.undoRecord(
            goal = completedGoal,
            entryId = entry.id,
            remainingEntriesAfterDeletion = listOf(
                ProgressEntry(
                    id = "entry-prev",
                    goalId = completedGoal.id,
                    recordedAt = Instant.parse("2026-11-05T10:00:00Z"),
                    recordedOffset = ZoneOffset.ofHours(9),
                    amount = 80.0,
                    source = EntrySource.MANUAL
                )
            )
        )

        assertEquals(GoalStatus.ACTIVE, restored.status)
        assertNull(restored.endedAt)
        assertNull(restored.endReason)
        assertEquals(0, progressRepository.entries.size)

        val inRepo = goalRepository.getGoal(completedGoal.id)
        assertEquals(GoalStatus.ACTIVE, inRepo?.status)
        assertNull(inRepo?.endedAt)
        assertNull(inRepo?.endReason)
    }

    private class TestGoalRepository : GoalRepository {
        private val goals = mutableMapOf<String, Goal>()
        private val flow = MutableStateFlow<Map<String, Goal>>(emptyMap())

        override fun observeGoals(): Flow<List<Goal>> =
            flow.map { it.values.sortedWith(compareBy({ it.priority }, { it.id })) }

        override fun observeGoal(id: String): Flow<Goal?> =
            flow.map { it[id] }

        override suspend fun getGoal(id: String): Goal? = goals[id]

        override suspend fun saveGoal(goal: Goal) {
            goals[goal.id] = goal
            flow.value = goals.toMap()
        }

        override suspend fun updateDueDate(id: String, dueDate: LocalDate) {
            goals[id]?.let {
                goals[id] = it.copy(dueDate = dueDate, overdueNoticeShown = false)
                flow.value = goals.toMap()
            }
        }

        override suspend fun updatePriorities(priorities: Map<String, Int>) {
            priorities.forEach { (id, prio) ->
                goals[id]?.let { goals[id] = it.copy(priority = prio) }
            }
            flow.value = goals.toMap()
        }

        override suspend fun completeGoal(id: String, endReason: EndReason, endedAt: LocalDate) {
            goals[id]?.let {
                goals[id] = it.copy(status = GoalStatus.COMPLETED, endReason = endReason, endedAt = endedAt)
                flow.value = goals.toMap()
            }
        }

        override suspend fun reopenGoal(id: String) {
            goals[id]?.let {
                goals[id] = it.copy(status = GoalStatus.ACTIVE, endReason = null, endedAt = null)
                flow.value = goals.toMap()
            }
        }

        override suspend fun setOverdueNoticeShown(id: String, shown: Boolean) {
            goals[id]?.let {
                goals[id] = it.copy(overdueNoticeShown = shown)
                flow.value = goals.toMap()
            }
        }
    }

    private class TestProgressRepository : ProgressRepository {
        val entries = mutableListOf<ProgressEntry>()
        private val flow = MutableStateFlow<List<ProgressEntry>>(emptyList())

        override fun observeEntries(goalId: String): Flow<List<ProgressEntry>> =
            flow.map { list -> list.filter { it.goalId == goalId }.sortedWith(compareBy({ it.recordedAt }, { it.id })) }

        override suspend fun getEntry(id: String): ProgressEntry? =
            entries.find { it.id == id }

        override suspend fun getEntriesBetween(goalId: String, from: Instant, until: Instant): List<ProgressEntry> =
            entries.filter { it.goalId == goalId && !it.recordedAt.isBefore(from) && it.recordedAt.isBefore(until) }

        override suspend fun addEntry(entry: ProgressEntry) {
            entries.add(entry)
            flow.value = entries.toList()
        }

        override suspend fun updateEntry(entry: ProgressEntry) {
            val idx = entries.indexOfFirst { it.id == entry.id }
            if (idx >= 0) {
                entries[idx] = entry
                flow.value = entries.toList()
            }
        }

        override suspend fun deleteEntry(id: String) {
            entries.removeAll { it.id == id }
            flow.value = entries.toList()
        }
    }
}
