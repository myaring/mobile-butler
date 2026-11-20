package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.EntrySource
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalStatus
import dev.myaring.mobile_butler.domain.model.ProgressEntry
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * [C-3] 기록 저장 및 되돌리기 흐름 결과.
 */
data class RecordResult(
    val entry: ProgressEntry,
    val isCompleted: Boolean,
    val message: String,
    val updatedGoal: Goal,
)

/**
 * [C-3] 기록 저장 흐름 및 되돌리기 처리기.
 *
 * 완료 조건:
 * - "남은 몫 모두 완료" 및 "일부만 기록"이 ProgressRepository에 저장된다.
 * - 저장 직후 카드의 진행 바, 14일 칸(오늘 칸), 집사 한마디가 바뀐다.
 * - 스낵바에 "오늘 20쪽을 모두 기록했습니다" 와 되돌리기가 뜬다.
 * - 되돌리기는 방금 저장한 기록 한 건만 지운다. 그 기록으로 목표가 완료되었다면 ACTIVE로 돌아가고 endedAt, endReason이 지워진다.
 * - 몫을 다 채우면 노란 버튼이 사라진다.
 */
class ProgressRecordFlow(
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * 기록 1건을 저장하고 목표 진행률을 갱신합니다.
     * 목표량에 도달하면 자동으로 COMPLETED(ACHIEVED) 처리됩니다.
     */
    suspend fun saveRecord(
        goal: Goal,
        amount: Double,
        existingEntries: List<ProgressEntry> = emptyList(),
    ): RecordResult {
        require(amount > 0.0) { "Record amount must be positive." }

        val zoneId = clock.zone
        val nowInstant = Instant.now(clock)
        val offset = zoneId.rules.getOffset(nowInstant)

        val entry = ProgressEntry(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            recordedAt = nowInstant,
            recordedOffset = offset,
            amount = amount,
            source = EntrySource.MANUAL
        )

        progressRepository.addEntry(entry)

        val allEntries = existingEntries + entry
        val currentTotal = allEntries.sumOf { it.amount }

        val transition = GoalTransitionRule.reduce(
            goal = goal,
            recordedTotal = currentTotal,
            event = GoalEvent.Reevaluate,
            clock = clock
        )

        val isCompleted = transition.effects.contains(GoalEffect.SHOW_ACHIEVED)
        if (isCompleted) {
            goalRepository.completeGoal(
                id = goal.id,
                endReason = EndReason.ACHIEVED,
                endedAt = LocalDate.now(clock)
            )
        }

        val amountFormatted = if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
        val message = "오늘 ${amountFormatted}${goal.unit}을 모두 기록했습니다"

        return RecordResult(
            entry = entry,
            isCompleted = isCompleted,
            message = message,
            updatedGoal = transition.goal
        )
    }

    /**
     * 방금 저장한 기록 1건을 취소(되돌리기)합니다.
     * 해당 기록으로 완료(COMPLETED)되었던 경우 ACTIVE 상태로 되돌리고 endedAt, endReason을 제거합니다.
     */
    suspend fun undoRecord(
        goal: Goal,
        entryId: String,
        remainingEntriesAfterDeletion: List<ProgressEntry>,
    ): Goal {
        progressRepository.deleteEntry(entryId)

        val newTotal = remainingEntriesAfterDeletion.sumOf { it.amount }

        if (goal.status == GoalStatus.COMPLETED && goal.endReason == EndReason.ACHIEVED) {
            val transition = GoalTransitionRule.reduce(
                goal = goal,
                recordedTotal = newTotal,
                event = GoalEvent.UndoCompletion,
                clock = clock
            )
            goalRepository.reopenGoal(goal.id)
            return transition.goal
        }

        return goal
    }
}
