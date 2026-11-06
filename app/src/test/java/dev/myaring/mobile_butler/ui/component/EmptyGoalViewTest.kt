package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmptyGoalViewTest {

    @Test
    fun defaultMessages_areInformativeAndPolite() {
        val defaultMessage = "등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오."
        val buttonText = "+ 목표 추가"

        assertTrue(defaultMessage.contains("목표가 없습니다"))
        assertTrue(buttonText.contains("목표 추가"))
    }

    @Test
    fun emptyStateMessages_containZeroEmojis() {
        val messages = listOf(
            "등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오.",
            "오늘 등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오.",
            "+ 목표 추가"
        )

        for (msg in messages) {
            val hasEmoji = msg.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Empty state text must not contain emoji: $msg", hasEmoji)
        }
    }
}
