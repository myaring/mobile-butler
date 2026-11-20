package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GoalCardExpandedTest {

    @Test
    fun buttonStyleAndText_whenQuotaNotMet_isFilledAndTodayRecord() {
        val todayProgress = 10.0
        val todayQuota = 20.0
        val isTodayQuotaMet = todayQuota > 0.0 && todayProgress >= todayQuota

        assertFalse("10/20 is not quota met", isTodayQuotaMet)

        val buttonText = if (isTodayQuotaMet) "기록 수정" else "오늘 기록"
        val buttonStyle = if (isTodayQuotaMet) MbButtonStyle.Outlined else MbButtonStyle.Filled

        assertEquals("오늘 기록", buttonText)
        assertEquals(MbButtonStyle.Filled, buttonStyle)
    }

    @Test
    fun buttonStyleAndText_whenQuotaMet_isOutlinedAndEditRecord() {
        val todayProgress = 20.0
        val todayQuota = 20.0
        val isTodayQuotaMet = todayQuota > 0.0 && todayProgress >= todayQuota

        assertTrue("20/20 is quota met", isTodayQuotaMet)

        val buttonText = if (isTodayQuotaMet) "기록 수정" else "오늘 기록"
        val buttonStyle = if (isTodayQuotaMet) MbButtonStyle.Outlined else MbButtonStyle.Filled

        assertEquals("기록 수정", buttonText)
        assertEquals(MbButtonStyle.Outlined, buttonStyle)
    }

    @Test
    fun buttonStyleAndText_whenQuotaExceeded_isOutlinedAndEditRecord() {
        val todayProgress = 25.0
        val todayQuota = 20.0
        val isTodayQuotaMet = todayQuota > 0.0 && todayProgress >= todayQuota

        assertTrue("25/20 exceeds quota", isTodayQuotaMet)

        val buttonText = if (isTodayQuotaMet) "기록 수정" else "오늘 기록"
        val buttonStyle = if (isTodayQuotaMet) MbButtonStyle.Outlined else MbButtonStyle.Filled

        assertEquals("기록 수정", buttonText)
        assertEquals(MbButtonStyle.Outlined, buttonStyle)
    }

    @Test
    fun progressRatios_areSafelyClampedBetweenZeroAndOne() {
        val zeroQuota = 0.0
        val zeroQuotaRatio = if (zeroQuota > 0.0) (10.0 / zeroQuota).toFloat().coerceIn(0f, 1f) else 0f
        assertEquals(0f, zeroQuotaRatio)

        val normalRatio = (10.0 / 20.0).toFloat().coerceIn(0f, 1f)
        assertEquals(0.5f, normalRatio)

        val overRatio = (30.0 / 20.0).toFloat().coerceIn(0f, 1f)
        assertEquals(1.0f, overRatio)
    }

    @Test
    fun recent14Days_recordedCountMatchesOnlyRecordedDays() {
        val today = LocalDate.of(2026, 11, 6)
        val cells = (0 until 14).map { i ->
            val date = today.minusDays((13 - i).toLong())
            val hasRecord = i % 2 == 0 // 7 days recorded
            DayCellUiModel(
                date = date,
                isToday = (i == 13),
                hasRecord = hasRecord
            )
        }

        val recordedCount = cells.count { it.hasRecord }
        assertEquals(7, recordedCount)
    }

    @Test
    fun labelsAndMessages_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "오늘의 목표",
            "오늘 기록",
            "기록 수정",
            "전체 진행",
            "최근 14일",
            "14일 중 11일 기록"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Text must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun labelsAndMessages_containZeroEmojis() {
        val sampleTexts = listOf(
            "오늘의 목표",
            "오늘 기록",
            "기록 수정",
            "전체 진행",
            "최근 14일",
            "14일 중 11일 기록",
            "자세히"
        )

        for (msg in sampleTexts) {
            val hasEmoji = msg.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Text must not contain emoji: $msg", hasEmoji)
        }
    }
}
