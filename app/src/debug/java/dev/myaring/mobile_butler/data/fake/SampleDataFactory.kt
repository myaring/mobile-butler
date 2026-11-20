package dev.myaring.mobile_butler.data.fake

import dev.myaring.mobile_butler.domain.model.Direction
import dev.myaring.mobile_butler.domain.model.EntrySource
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.model.PacingPeriod
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.model.TrackingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

/**
 * [A-7] 디버그용 샘플 데이터 팩토리.
 *
 * 샘플 목표 목록 (누적형):
 * 1. 먀의 투쟁 (734쪽, 마감 11/15, 기록 일부)
 * 2. 독서 (전공 서적 독서, 300쪽, 마감 11/20)
 * 3. 공부 (알고리즘 문제 풀이, 100문제, 마감 11/30)
 * 4. 걸음 수 (누적 10만보 걷기, 100000걸음, 마감 11/15, 직접 입력)
 */
object SampleDataFactory {

    const val GOAL_ID_MYA = "goal-mya-struggle"
    const val GOAL_ID_READING = "goal-reading"
    const val GOAL_ID_STUDY = "goal-study"
    const val GOAL_ID_STEPS = "goal-steps"

    fun createSampleGoals(): List<Goal> = listOf(
        Goal(
            id = GOAL_ID_MYA,
            title = "먀의 투쟁",
            type = GoalType.CUMULATIVE,
            direction = Direction.UP,
            pacingPeriod = PacingPeriod.DAY,
            unit = "쪽",
            targetValue = 734.0,
            startValue = 0.0,
            startDate = LocalDate.of(2026, 10, 20),
            dueDate = LocalDate.of(2026, 11, 15),
            trackingMode = TrackingMode.MANUAL,
            priority = 0,
            status = GoalStatus.ACTIVE
        ),
        Goal(
            id = GOAL_ID_READING,
            title = "전공 서적 독서",
            type = GoalType.CUMULATIVE,
            direction = Direction.UP,
            pacingPeriod = PacingPeriod.DAY,
            unit = "쪽",
            targetValue = 300.0,
            startValue = 0.0,
            startDate = LocalDate.of(2026, 10, 25),
            dueDate = LocalDate.of(2026, 11, 20),
            trackingMode = TrackingMode.MANUAL,
            priority = 1,
            status = GoalStatus.ACTIVE
        ),
        Goal(
            id = GOAL_ID_STUDY,
            title = "알고리즘 문제 풀이",
            type = GoalType.CUMULATIVE,
            direction = Direction.UP,
            pacingPeriod = PacingPeriod.DAY,
            unit = "문제",
            targetValue = 100.0,
            startValue = 0.0,
            startDate = LocalDate.of(2026, 10, 28),
            dueDate = LocalDate.of(2026, 11, 30),
            trackingMode = TrackingMode.MANUAL,
            priority = 2,
            status = GoalStatus.ACTIVE
        ),
        Goal(
            id = GOAL_ID_STEPS,
            title = "누적 10만보 걷기",
            type = GoalType.CUMULATIVE,
            direction = Direction.UP,
            pacingPeriod = PacingPeriod.DAY,
            unit = "걸음",
            targetValue = 100000.0,
            startValue = 0.0,
            startDate = LocalDate.of(2026, 11, 1),
            dueDate = LocalDate.of(2026, 11, 15),
            trackingMode = TrackingMode.MANUAL,
            priority = 3,
            status = GoalStatus.ACTIVE
        )
    )

    fun createSampleEntries(): List<ProgressEntry> {
        val offset = ZoneOffset.ofHours(9)
        return listOf(
            // 먀의 투쟁 일부 기록 (총 210쪽 기록됨)
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_MYA,
                recordedAt = Instant.parse("2026-10-25T10:00:00Z"),
                recordedOffset = offset,
                amount = 50.0,
                source = EntrySource.MANUAL
            ),
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_MYA,
                recordedAt = Instant.parse("2026-10-28T14:30:00Z"),
                recordedOffset = offset,
                amount = 60.0,
                source = EntrySource.MANUAL
            ),
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_MYA,
                recordedAt = Instant.parse("2026-11-02T09:15:00Z"),
                recordedOffset = offset,
                amount = 45.0,
                source = EntrySource.MANUAL
            ),
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_MYA,
                recordedAt = Instant.parse("2026-11-05T18:00:00Z"),
                recordedOffset = offset,
                amount = 55.0,
                source = EntrySource.MANUAL
            ),
            // 독서 기록 일부
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_READING,
                recordedAt = Instant.parse("2026-11-01T11:00:00Z"),
                recordedOffset = offset,
                amount = 40.0,
                source = EntrySource.MANUAL
            ),
            // 걸음 수 직접 입력 기록 일부
            ProgressEntry(
                id = UUID.randomUUID().toString(),
                goalId = GOAL_ID_STEPS,
                recordedAt = Instant.parse("2026-11-03T20:00:00Z"),
                recordedOffset = offset,
                amount = 12000.0,
                source = EntrySource.MANUAL
            )
        )
    }
}
