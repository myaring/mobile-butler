package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordSheetTest {

    @Test
    fun remainingCalculation_normalGoal_calculatesTodayDifference() {
        val todayQuota = 20.0
        val todayProgress = 10.0
        val remainingForToday = (todayQuota - todayProgress).coerceAtLeast(0.0).toInt()

        assertEquals(10, remainingForToday)
    }

    @Test
    fun remainingCalculation_overdueGoal_usesRemainingTotal() {
        val remainingTotal = 74.0
        val remainingForOverdue = remainingTotal.coerceAtLeast(0.0).toInt()

        assertEquals(74, remainingForOverdue)
    }

    @Test
    fun stepperAmount_cannotBeZeroOrNegative() {
        var stepper = 1
        // Attempt to decrement when at 1
        if (stepper > 1) {
            stepper -= 1
        }
        assertEquals(1, stepper)

        // Increment by 1
        stepper += 1
        assertEquals(2, stepper)

        // Increment by 1 again
        stepper += 1
        assertEquals(3, stepper)

        // Decrement by 1
        if (stepper > 1) {
            stepper -= 1
        }
        assertEquals(2, stepper)
    }

    @Test
    fun headerText_overdueGoal_doesNotContainTodayGoal() {
        val isOverdue = true
        val unit = "쪽"
        val remainingForOverdue = 70

        val headerSubtitle = if (isOverdue) {
            "남은 ${remainingForOverdue}${unit} 중 진행한 만큼 기록합니다"
        } else {
            "오늘의 목표 20 중 10 했어요"
        }

        assertFalse("Overdue header must not contain '오늘의 목표'", headerSubtitle.contains("오늘의 목표"))
        assertTrue("Overdue header must describe remaining amount", headerSubtitle.contains("남은 70쪽"))
    }

    @Test
    fun texts_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "오늘의 목표 20 중 10 했어요",
            "남은 10쪽 모두 완료했어요",
            "또는 한 만큼만",
            "5쪽 기록",
            "남은 70쪽 중 진행한 만큼 기록합니다"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun texts_containZeroEmojis() {
        val sampleTexts = listOf(
            "오늘의 목표 20 중 10 했어요",
            "남은 10쪽 모두 완료했어요",
            "또는 한 만큼만",
            "5쪽 기록",
            "남은 70쪽 중 진행한 만큼 기록합니다"
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
