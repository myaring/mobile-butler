package dev.myaring.mobile_butler

import android.content.Context
import dev.myaring.mobile_butler.domain.view.screen.EScreen
import dev.myaring.mobile_butler.domain.view.screen.Screen

class AppContainer(context: Context)
{
    lateinit var screenMap: MutableMap<EScreen, Screen>
}