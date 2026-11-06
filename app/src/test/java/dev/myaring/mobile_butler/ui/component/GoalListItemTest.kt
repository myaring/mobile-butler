package dev.myaring.mobile_butler.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalListItemTest {

    @Test
    fun goalListItem_semantics_containsNoEmoji() {
        val title = "코틀린 완벽 가이드"
        val typeLabel = "누적"
        val summary = "오늘 20쪽 / 전체 100쪽 중 60쪽"
        val combined = "$title, $typeLabel, $summary"

        val hasEmoji = combined.codePoints().anyMatch { cp ->
            (cp in 0x1F300..0x1F9FF) ||
                    (cp in 0x2600..0x26FF) ||
                    (cp in 0x2700..0x27BF) ||
                    (cp in 0x1F600..0x1F64F) ||
                    (cp in 0x1F680..0x1F6FF)
        }

        assertFalse("Semantics must not contain emoji", hasEmoji)
        assertEquals("코틀린 완벽 가이드, 누적, 오늘 20쪽 / 전체 100쪽 중 60쪽", combined)
    }

    @Test
    fun goalListItem_progressClamping() {
        val underProgress = -0.2f
        val validProgress = 0.75f
        val overProgress = 1.4f

        assertEquals(0f, underProgress.coerceIn(0f, 1f))
        assertEquals(0.75f, validProgress.coerceIn(0f, 1f))
        assertEquals(1f, overProgress.coerceIn(0f, 1f))
    }

    @Test
    fun goalListItem_percentFormatting() {
        val progress = 0.65f
        val percent = (progress.coerceIn(0f, 1f) * 100).toInt()
        assertEquals(65, percent)
        assertEquals("65%", "$percent%")
    }
}
