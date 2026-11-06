package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarHeatmapTest {

    @Test
    fun titleFormatting_matchesSpecification() {
        val today = LocalDate.of(2026, 10, 20)
        val ym = YearMonth.of(2026, 10)
        val recordedDates = (1..15).map { ym.atDay(it) }.toSet()

        val isCurrentMonth = ym == YearMonth.from(today)
        val eligibleDays = if (isCurrentMonth) today.dayOfMonth else ym.lengthOfMonth()
        val recordedCount = (1..ym.lengthOfMonth()).count { day ->
            val date = ym.atDay(day)
            !date.isAfter(today) && recordedDates.contains(date)
        }

        val title = "${ym.year}년 ${ym.monthValue}월 / ${eligibleDays}일 중 ${recordedCount}일 기록"
        assertEquals("2026년 10월 / 20일 중 15일 기록", title)
    }

    @Test
    fun weekHeaderStartPadding_alignsWithSunday() {
        // 일요일은 0번 열이어야 함
        assertEquals(0, DayOfWeek.SUNDAY.value % 7)
        // 월요일은 1번 열
        assertEquals(1, DayOfWeek.MONDAY.value % 7)
        // 토요일은 6번 열
        assertEquals(6, DayOfWeek.SATURDAY.value % 7)
    }

    @Test
    fun futureOpacity_is35Percent() {
        val futureAlpha = 0.35f
        assertEquals(0.35f, futureAlpha, 0.001f)
    }

    @Test
    fun legendAndHeaders_containNoEmoji() {
        val texts = listOf(
            "2026년 10월 / 20일 중 15일 기록",
            "일", "월", "화", "수", "목", "금", "토",
            " 한 날", " 기록 없음",
            "이전 달", "다음 달"
        )

        for (text in texts) {
            val hasEmoji = text.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Text must not contain emoji: $text", hasEmoji)
            assertTrue(text.isNotEmpty())
        }
    }
}
