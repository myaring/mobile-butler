package dev.myaring.mobile_butler.repository

import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.GoalLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class GoalSerializerTest {

    @Test
    fun serializeAndDeserializeGoal_preservesAllFieldsAndSpecialCharacters() {
        val original = Goal(
            id = "test-uuid-1234",
            title = "코틀린 & 컴포즈 완전 정복 (1권, 2권)",
            targetAmount = 450.5,
            currentAmount = 120.0,
            unit = "페이지/쪽",
            startDate = LocalDate.of(2026, 11, 1),
            deadline = LocalDate.of(2026, 12, 1),
            priority = 2,
            isCompleted = false,
            createdAt = 1730000000000L
        )

        val serialized = GoalSerializer.serializeGoal(original)
        assertNotNull(serialized)

        val restored = GoalSerializer.deserializeGoal(serialized)
        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.title, restored?.title)
        assertEquals(original.targetAmount, restored?.targetAmount ?: 0.0, 0.001)
        assertEquals(original.currentAmount, restored?.currentAmount ?: 0.0, 0.001)
        assertEquals(original.unit, restored?.unit)
        assertEquals(original.startDate, restored?.startDate)
        assertEquals(original.deadline, restored?.deadline)
        assertEquals(original.priority, restored?.priority)
        assertEquals(original.isCompleted, restored?.isCompleted)
        assertEquals(original.createdAt, restored?.createdAt)
    }

    @Test
    fun serializeAndDeserializeLog_preservesAllFields() {
        val original = GoalLog(
            id = "log-uuid-999",
            goalId = "test-uuid-1234",
            date = LocalDate.of(2026, 11, 5),
            amount = 25.5,
            note = "퇴근 후 3챕터 완독, 핵심 메모 작성",
            timestamp = 1730800000000L
        )

        val serialized = GoalSerializer.serializeLog(original)
        val restored = GoalSerializer.deserializeLog(serialized)

        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.goalId, restored?.goalId)
        assertEquals(original.date, restored?.date)
        assertEquals(original.amount, restored?.amount ?: 0.0, 0.001)
        assertEquals(original.note, restored?.note)
        assertEquals(original.timestamp, restored?.timestamp)
    }

    @Test
    fun deserializeGoal_corruptString_returnsNullSafely() {
        assertNull(GoalSerializer.deserializeGoal(""))
        assertNull(GoalSerializer.deserializeGoal("short,corrupt"))
        assertNull(GoalSerializer.deserializeGoal("1,2,not_a_number,4,5,6,7,8,9,10"))
    }

    @Test
    fun deserializeLog_corruptString_returnsNullSafely() {
        assertNull(GoalSerializer.deserializeLog(""))
        assertNull(GoalSerializer.deserializeLog("short"))
        assertNull(GoalSerializer.deserializeLog("1,2,not_a_date,not_a_number,5,6"))
    }
}
