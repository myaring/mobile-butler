package dev.myaring.mobile_butler.data.local

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

object DatabaseFactory {

    @Volatile
    private var instance: ButlerDatabase? = null

    fun get(context: Context): ButlerDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder<ButlerDatabase>(
                context.applicationContext,
                "mobile-butler.db",
            ).setDriver(BundledSQLiteDriver()).build().also { instance = it }
        }
}