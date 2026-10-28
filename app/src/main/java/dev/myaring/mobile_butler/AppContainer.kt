package dev.myaring.mobile_butler

import android.content.Context
import dev.myaring.mobile_butler.ui.navigation.EScreen
import dev.myaring.mobile_butler.ui.navigation.Screen
import dev.myaring.mobile_butler.ui.navigation.goalScreenImpl
import dev.myaring.mobile_butler.ui.navigation.scheduleScreenImpl
import dev.myaring.mobile_butler.ui.navigation.settingScreenImpl
import dev.myaring.mobile_butler.ui.navigation.todayScreenImpl

class AppContainer(context: Context)
{
    val implScreens: Map<EScreen, Screen> = mapOf(
        EScreen.TODAY to todayScreenImpl,
        EScreen.GOAL to goalScreenImpl,
        EScreen.SCHEDULE to scheduleScreenImpl,
        EScreen.SETTING to settingScreenImpl,
    )
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MobileButlerApplication).container