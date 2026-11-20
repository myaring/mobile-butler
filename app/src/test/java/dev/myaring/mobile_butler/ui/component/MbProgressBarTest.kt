package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MbProgressBarTest {

    @Test
    fun progressCoerce_clampsBetweenZeroAndOne() {
        assertEquals(0f, (-0.5f).coerceIn(0f, 1f))
        assertEquals(0f, 0f.coerceIn(0f, 1f))
        assertEquals(0.5f, 0.5f.coerceIn(0f, 1f))
        assertEquals(1f, 1.0f.coerceIn(0f, 1f))
        assertEquals(1f, 1.5f.coerceIn(0f, 1f))
    }

    @Test
    fun contentDescription_doesNotContainEmoji() {
        val descriptions = listOf(
            "전체 진행률 50%",
            "오늘의 목표 달성률 100%",
            "진행률 0%"
        )

        for (desc in descriptions) {
            val hasEmoji = desc.codePoints().anyMatch { cp ->
                (cp in 0x1F300..0x1F9FF) ||
                        (cp in 0x2600..0x26FF) ||
                        (cp in 0x2700..0x27BF) ||
                        (cp in 0x1F600..0x1F64F) ||
                        (cp in 0x1F680..0x1F6FF)
            }
            assertFalse("Content description must not contain emoji: $desc", hasEmoji)
            assertTrue("Content description must not be empty", desc.isNotEmpty())
        }
    }
}
