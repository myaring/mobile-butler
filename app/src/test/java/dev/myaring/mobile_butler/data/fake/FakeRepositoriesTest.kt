package dev.myaring.mobile_butler.data.fake

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.TrackingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FakeRepositoriesTest {

    private lateinit var goalRepository: FakeGoalRepository
    private lateinit var progressRepository: FakeProgressRepository

    @Before
    fun setUp() {
        val sampleGoals = SampleDataFactory.createSampleGoals()
        val sampleEntries = SampleDataFactory.createSampleEntries()
        goalRepository = FakeGoalRepository(sampleGoals)
        progressRepository = FakeProgressRepository(sampleEntries)
    }

    @Test
    fun sampleDataFactory_containsRequiredCumulativeGoals() {
        val goals = SampleDataFactory.createSampleGoals()
        assertTrue("All sample goals must be CUMULATIVE", goals.all { it.type == GoalType.CUMULATIVE })

        val myaGoal = goals.find { it.id == SampleDataFactory.GOAL_ID_MYA }
        assertNotNull(myaGoal)
        assertEquals("먀의 투쟁", myaGoal?.title)
        assertEquals(734.0, myaGoal?.targetValue ?: 0.0, 0.001)
        assertEquals("쪽", myaGoal?.unit)
        assertEquals(LocalDate.of(2026, 11, 15), myaGoal?.dueDate)

        val stepsGoal = goals.find { it.id == SampleDataFactory.GOAL_ID_STEPS }
        assertNotNull(stepsGoal)
        assertEquals(TrackingMode.MANUAL, stepsGoal?.trackingMode)
        assertEquals("걸음", stepsGoal?.unit)
    }

    @Test
    fun fakeGoalRepository_observeGoals_ordersByPriority() = runBlocking {
        val list = goalRepository.observeGoals().first()
        assertTrue(list.size >= 4)
        for (i in 0 until list.size - 1) {
            assertTrue(list[i].priority <= list[i + 1].priority)
        }
    }

    @Test
    fun fakeGoalRepository_completeAndReopen() = runBlocking {
        val goalId = SampleDataFactory.GOAL_ID_MYA
        val today = LocalDate.of(2026, 11, 6)

        goalRepository.completeGoal(goalId, EndReason.ACHIEVED, today)
        val completed = goalRepository.getGoal(goalId)
        assertEquals(GoalStatus.COMPLETED, completed?.status)
        assertEquals(EndReason.ACHIEVED, completed?.endReason)
        assertEquals(today, completed?.endedAt)

        goalRepository.reopenGoal(goalId)
        val reopened = goalRepository.getGoal(goalId)
        assertEquals(GoalStatus.ACTIVE, reopened?.status)
        assertNull(reopened?.endReason)
        assertNull(reopened?.endedAt)
    }

    @Test
    fun fakeProgressRepository_observeAndFilterByGoal() = runBlocking {
        val myaEntries = progressRepository.observeEntries(SampleDataFactory.GOAL_ID_MYA).first()
        assertTrue(myaEntries.isNotEmpty())
        assertTrue(myaEntries.all { it.goalId == SampleDataFactory.GOAL_ID_MYA })

        val sum = myaEntries.sumOf { it.amount }
        assertEquals(210.0, sum, 0.001)
    }

    @Test
    fun fakeProgressRepository_getEntriesBetween() = runBlocking {
        val from = Instant.parse("2026-10-20T00:00:00Z")
        val until = Instant.parse("2026-10-30T00:00:00Z")

        val between = progressRepository.getEntriesBetween(SampleDataFactory.GOAL_ID_MYA, from, until)
        assertEquals(2, between.size)
    }

    @Test
    fun sampleDataStrings_containNoEmoji() {
        val titles = SampleDataFactory.createSampleGoals().map { it.title }
        for (t in titles) {
            val hasEmoji = t.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse(hasEmoji)
        }
    }
}
