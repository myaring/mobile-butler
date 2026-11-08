package dev.myaring.mobile_butler

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.myaring.mobile_butler.data.local.ButlerDatabase
import dev.myaring.mobile_butler.data.local.DatabaseFactory
import dev.myaring.mobile_butler.data.repository.GoalRepositoryImpl
import dev.myaring.mobile_butler.data.repository.ProgressRepositoryImpl
import dev.myaring.mobile_butler.domain.repository.GoalRepository
import dev.myaring.mobile_butler.domain.repository.ProgressRepository
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.ScheduleScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.SettingScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayScreenViewModel
import dev.myaring.mobile_butler.ui.navigation.EScreen
import dev.myaring.mobile_butler.ui.navigation.Screen
import dev.myaring.mobile_butler.ui.navigation.goalScreenImpl
import dev.myaring.mobile_butler.ui.navigation.scheduleScreenImpl
import dev.myaring.mobile_butler.ui.navigation.settingScreenImpl
import dev.myaring.mobile_butler.ui.navigation.todayScreenImpl
import java.time.Clock

class AppContainer(
    val context: Context,
    val clock: Clock = Clock.systemDefaultZone(),
    val database: ButlerDatabase = DatabaseFactory.get(context),
    val goalRepository: GoalRepository = GoalRepositoryImpl(database),
    val progressRepository: ProgressRepository = ProgressRepositoryImpl(database),
)
{
    val implScreens: Map<EScreen, Screen> = mapOf(
        EScreen.TODAY to todayScreenImpl,
        EScreen.GOAL to goalScreenImpl,
        EScreen.SCHEDULE to scheduleScreenImpl,
        EScreen.SETTING to settingScreenImpl,
    )

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            TodayScreenViewModel(
                goalRepository = goalRepository,
                progressRepository = progressRepository,
                clock = clock,
            )
        }
        initializer {
            GoalScreenViewModel()
        }
        initializer {
            ScheduleScreenViewModel()
        }
        initializer {
            SettingScreenViewModel()
        }
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as MobileButlerApplication).container
