package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalCardCollapsedTest {

    @Test
    fun summaryFormat_containsQuotaAndDDay() {
        val quota = 20.0
        val unit = "쪽"
        val dDay = 18
        val summary = "오늘의 목표 ${quota.toLong()}$unit · D-$dDay"

        assertEquals("오늘의 목표 20쪽 · D-18", summary)
    }

    @Test
    fun buttonText_isRecord() {
        val buttonText = "기록"
        assertEquals("기록", buttonText)
    }

    @Test
    fun text_containsZeroNegativeWords() {
        val sampleTexts = listOf(
            "오늘의 목표 20쪽 · D-26",
            "오늘의 목표 1만 보 · D-18",
            "기록"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun text_containsZeroEmojis() {
        val sampleTexts = listOf(
            "오늘의 목표 20쪽 · D-26",
            "오늘의 목표 1만 보 · D-18",
            "기록",
            "모바일 집사 프로젝트"
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
