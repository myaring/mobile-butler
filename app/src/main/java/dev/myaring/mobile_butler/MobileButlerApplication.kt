package dev.myaring.mobile_butler

import android.app.Application

class MobileButlerApplication : Application()
{
    lateinit var container: AppContainer
        private set

    override fun onCreate()
    {
        super.onCreate()

        container = AppContainer(this)
    }
}