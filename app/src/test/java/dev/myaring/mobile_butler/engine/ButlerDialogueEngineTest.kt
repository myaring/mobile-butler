package dev.myaring.mobile_butler.engine

import dev.myaring.mobile_butler.model.ButlerEvent
import dev.myaring.mobile_butler.model.ButlerPersona
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ButlerDialogueEngineTest {

    @Test
    fun getDialogue_allPersonasAndEvents_returnNonEmptyDialogue() {
        for (persona in ButlerPersona.entries) {
            for (event in ButlerEvent.entries) {
                val dialogue = ButlerDialogueEngine.getDialogue(persona, event, 0)
                assertTrue("Dialogue for $persona / $event must not be blank", dialogue.isNotBlank())
            }
        }
    }

    @Test
    fun getDialogue_differentPersonas_returnDistinctDialoguesForSameEvent() {
        val event = ButlerEvent.HOME_GREETING
        val cynical = ButlerDialogueEngine.getDialogue(ButlerPersona.CYNICAL, event, 0)
        val calm = ButlerDialogueEngine.getDialogue(ButlerPersona.CALM, event, 0)
        val anxious = ButlerDialogueEngine.getDialogue(ButlerPersona.ANXIOUS, event, 0)

        assertNotEquals(cynical, calm)
        assertNotEquals(cynical, anxious)
        assertNotEquals(calm, anxious)
    }

    @Test
    fun getDialogue_negativeOrLargeIndex_handlesIndexSafely() {
        val dialogueLarge = ButlerDialogueEngine.getDialogue(ButlerPersona.CALM, ButlerEvent.HOME_GREETING, 999)
        val dialogueNeg = ButlerDialogueEngine.getDialogue(ButlerPersona.CALM, ButlerEvent.HOME_GREETING, -5)

        assertTrue(dialogueLarge.isNotBlank())
        assertTrue(dialogueNeg.isNotBlank())
    }

    @Test
    fun dialogues_containNoEmojis() {
        fun containsEmoji(text: String): Boolean {
            var i = 0
            while (i < text.length) {
                val codePoint = text.codePointAt(i)
                val isEmoji = (codePoint in 0x1F600..0x1F64F) ||
                    (codePoint in 0x1F300..0x1F5FF) ||
                    (codePoint in 0x1F680..0x1F6FF) ||
                    (codePoint in 0x1F700..0x1FAFF) ||
                    (codePoint in 0x2600..0x26FF) ||
                    (codePoint in 0x2700..0x27BF)
                if (isEmoji) return true
                i += Character.charCount(codePoint)
            }
            return false
        }

        for (persona in ButlerPersona.entries) {
            for (event in ButlerEvent.entries) {
                val allLines = ButlerDialogueEngine.getAllDialogues(persona, event)
                for (line in allLines) {
                    assertFalse(
                        "Dialogue line contains emoji: '$line'",
                        containsEmoji(line)
                    )
                }
            }
        }
    }
}
