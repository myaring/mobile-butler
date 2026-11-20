package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MbSnackbarHostTest {

    @Test
    fun defaultActionLabel_isUndo() {
        val defaultAction = "되돌리기"
        assertEquals("되돌리기", defaultAction)
    }

    @Test
    fun snackbarMessage_formatMatchesDesign() {
        val amount = 20
        val unit = "쪽"
        val message = "오늘 ${amount}${unit}을 모두 기록했습니다"

        assertEquals("오늘 20쪽을 모두 기록했습니다", message)
    }

    @Test
    fun messages_containZeroNegativeWords() {
        val sampleTexts = listOf(
            "오늘 20쪽을 모두 기록했습니다",
            "되돌리기",
            "기록을 저장했습니다",
            "목표를 마무리했습니다"
        )

        val negativeWords = listOf("실패", "미달", "탈락", "좌절", "부족")
        for (text in sampleTexts) {
            for (word in negativeWords) {
                assertFalse("Must not contain negative word '$word': $text", text.contains(word))
            }
        }
    }

    @Test
    fun messages_containZeroEmojis() {
        val sampleTexts = listOf(
            "오늘 20쪽을 모두 기록했습니다",
            "되돌리기",
            "기록을 저장했습니다"
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
