package dev.myaring.mobile_butler.ui.navigation

import dev.myaring.mobile_butler.domain.viewmodel.SettingScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScheduleAndSettingTest {

    @Test
    fun scheduleScreenImpl_hasNullDrawerDelegatee_soItTriggersDialog() {
        assertEquals(EScreen.SCHEDULE, scheduleScreenImpl.eScreen)
        assertNull(
            "Schedule screen drawer delegatee must be null in Season 1 to trigger popup",
            scheduleScreenImpl.screenDrawerDelegateeOrNull
        )
    }

    @Test
    fun settingScreenViewModel_titleIsSettings() {
        val viewModel = SettingScreenViewModel()
        assertEquals("설정", viewModel.title.value)
        assertFalse(viewModel.showBackButton.value)
    }

    @Test
    fun settingScreenImpl_hasNonNullDrawerDelegatee() {
        assertEquals(EScreen.SETTING, settingScreenImpl.eScreen)
        assertNotNull(settingScreenImpl.screenDrawerDelegateeOrNull)
    }
}
