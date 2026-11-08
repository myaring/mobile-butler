package dev.myaring.mobile_butler

import android.app.Application
import java.time.Clock

class MobileButlerApplication : Application()
{
    lateinit var container: AppContainer
        private set

    override fun onCreate()
    {
        super.onCreate()
        container = AppContainer(
            context = this,
            clock = Clock.systemDefaultZone(),
        )
    }
}
