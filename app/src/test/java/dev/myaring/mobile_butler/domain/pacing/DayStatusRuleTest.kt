package dev.myaring.mobile_butler.domain.pacing

import dev.myaring.mobile_butler.domain.model.Direction
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneOffset

class DayStatusRuleTest {

    @Test
    fun sameDayEntriesAreSummedAndPartialIfBelowQuota() {
        val entries = listOf(
            entry(amount = 5.0, id = "a"),
            entry(amount = 10.0, id = "b"),
        )

        val result = DayStatusRule.calculate(
            goal(),
            entries,
            TODAY,
            quota = 23.0,
        )

        assertEquals(15.0, result.achievedAmount, 0.0)
        assertEquals(RecordState.PARTIAL, result.state)
        assertTrue(DayStatusRule.hasRecord(result.state))
    }

    @Test
    fun sumEqualToQuotaIsFull() {
        val result = DayStatusRule.calculate(
            goal(),
            listOf(entry(amount = 10.0)),
            TODAY,
            10.0,
        )

        assertEquals(RecordState.FULL, result.state)
    }

    @Test
    fun deletingOnlyEntryMakesNone() {
        val before = DayStatusRule.calculate(
            goal(),
            listOf(entry()),
            TODAY,
            10.0,
        )

        val after = DayStatusRule.calculate(
            goal(),
            emptyList(),
            TODAY,
            10.0,
        )

        assertEquals(RecordState.PARTIAL, before.state)
        assertEquals(RecordState.NONE, after.state)
        assertFalse(DayStatusRule.hasRecord(after.state))
    }

    @Test
    fun todayPendingChangesToDoneOnRecordAndResetsAtMidnight() {
        assertEquals(
            TodayCellState.TodayPending,
            DayStatusRule.todayCell(emptyList(), CLOCK),
        )

        assertEquals(
            TodayCellState.TodayDone,
            DayStatusRule.todayCell(listOf(entry()), CLOCK),
        )

        val tomorrow = Clock.offset(
            CLOCK,
            Duration.ofDays(1),
        )

        assertEquals(
            TodayCellState.TodayPending,
            DayStatusRule.todayCell(listOf(entry()), tomorrow),
        )

        assertEquals(
            RecordState.NONE,
            DayStatusRule.calculate(
                goal(),
                listOf(entry()),
                TODAY.plusDays(1),
                10.0,
            ).state,
        )
    }

    @Test
    fun downMustNotSilentlyUseUpSemantics() {
        assertThrows(IllegalArgumentException::class.java) {
            DayStatusRule.calculate(
                goal(),
                emptyList(),
                TODAY,
                1.0,
                Direction.DOWN,
            )
        }
    }

    @Test
    fun originalOffsetDeterminesTheDay() {
        val original = entry(
            date = TODAY,
            offset = ZoneOffset.ofHours(9),
            time = LocalTime.of(0, 30),
        )

        val result = DayStatusRule.calculate(
            goal(),
            listOf(original),
            TODAY,
            1.0,
        )

        assertEquals(RecordState.FULL, result.state)
    }
}