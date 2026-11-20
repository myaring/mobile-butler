package dev.myaring.mobile_butler

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.myaring.mobile_butler.data.local.ButlerDatabase
import dev.myaring.mobile_butler.data.repository.GoalRepositoryImpl
import dev.myaring.mobile_butler.data.repository.ProgressRepositoryImpl
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.ScheduleScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.SettingScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayScreenViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class AppContainerTest {

    private lateinit var database: ButlerDatabase
    private lateinit var goalRepository: GoalRepositoryImpl
    private lateinit var progressRepository: ProgressRepositoryImpl
    private val fixedClock: Clock = Clock.fixed(
        Instant.parse("2026-11-06T10:00:00Z"),
        ZoneId.of("UTC"),
    )

    @Before
    fun setUp() {
        database = Room
            .inMemoryDatabaseBuilder<ButlerDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()

        goalRepository = GoalRepositoryImpl(database)
        progressRepository = ProgressRepositoryImpl(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun createGoal(
        id: String = "goal-1",
        title: String = "Goal Title",
    ) = Goal(
        id = id,
        title = title,
        unit = "pages",
        targetValue = 100.0,
        startValue = 0.0,
        startDate = LocalDate.of(2026, 11, 1),
        dueDate = LocalDate.of(2026, 11, 30),
        priority = 1,
        restDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
    )

    private fun createEntry(
        id: String = "entry-1",
        goalId: String = "goal-1",
        amount: Double = 10.0,
    ) = ProgressEntry(
        id = id,
        goalId = goalId,
        recordedAt = Instant.parse("2026-11-06T09:00:00Z"),
        recordedOffset = ZoneOffset.UTC,
        amount = amount,
    )

    @Test
    fun goalRepositoryImpl_implementsGoalRepository_andOperatesFlow() = runBlocking {
        val repo: GoalRepository = goalRepository
        assertNotNull(repo)

        val initialGoals = goalRepository.observeGoals().first()
        assertTrue(initialGoals.isEmpty())

        val goal = createGoal("test-goal-1", "Test Goal")
        goalRepository.saveGoal(goal)

        val updatedGoals = goalRepository.observeGoals().first()
        assertEquals(1, updatedGoals.size)
        assertEquals("test-goal-1", updatedGoals[0].id)

        val fetched = goalRepository.getGoal("test-goal-1")
        assertNotNull(fetched)
        assertEquals("Test Goal", fetched?.title)
    }

    @Test
    fun progressRepositoryImpl_implementsProgressRepository_andOperatesFlow() = runBlocking {
        val repo: ProgressRepository = progressRepository
        assertNotNull(repo)

        val goal = createGoal("test-goal-2", "Another Goal")
        goalRepository.saveGoal(goal)

        val initialEntries = progressRepository.observeEntries("test-goal-2").first()
        assertTrue(initialEntries.isEmpty())

        val entry = createEntry("entry-1", "test-goal-2", 5.0)
        progressRepository.addEntry(entry)

        val updatedEntries = progressRepository.observeEntries("test-goal-2").first()
        assertEquals(1, updatedEntries.size)
        assertEquals("entry-1", updatedEntries[0].id)

        val fetched = progressRepository.getEntry("entry-1")
        assertNotNull(fetched)
        assertEquals(5.0, fetched?.amount ?: 0.0, 0.001)
    }

    @Test
    fun appContainer_initializesClockAndRepositories() {
        val mockContext: Context = ContextWrapper(null)
        val container = AppContainer(
            context = mockContext,
            clock = fixedClock,
            database = database,
            goalRepository = goalRepository,
            progressRepository = progressRepository,
        )

        assertEquals(fixedClock, container.clock)
        assertEquals(goalRepository, container.goalRepository)
        assertEquals(progressRepository, container.progressRepository)

        val todayVm = container.viewModelFactory.create(
            TodayScreenViewModel::class.java,
            CreationExtras.Empty,
        )
        assertNotNull(todayVm)
        assertEquals(goalRepository, todayVm.goalRepository)
        assertEquals(progressRepository, todayVm.progressRepository)
        assertEquals(fixedClock, todayVm.clock)

        val goalVm = container.viewModelFactory.create(
            GoalScreenViewModel::class.java,
            CreationExtras.Empty,
        )
        assertNotNull(goalVm)

        val scheduleVm = container.viewModelFactory.create(
            ScheduleScreenViewModel::class.java,
            CreationExtras.Empty,
        )
        assertNotNull(scheduleVm)

        val settingVm = container.viewModelFactory.create(
            SettingScreenViewModel::class.java,
            CreationExtras.Empty,
        )
        assertNotNull(settingVm)
    }
}
