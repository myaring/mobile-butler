package dev.myaring.mobile_butler.domain.view.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// 화면 구성 시 프로그래밍 실수 방지를 위해 추상 클래스 및 추상 메소드를 도입합니다.
// 새로운 화면 구성 시 추상 메소드를 반드시 구현하세요.
abstract class Screen(val eScreen: EScreen)
{
    @Composable
    abstract fun drawScreen(currentActivity: ComponentActivity, modifier: Modifier) : Unit
}

// Main Activity에서 한 번만 호출됩니다.
fun initAllScreens() : MutableMap<EScreen, Screen>
{
    val screenMap = mutableMapOf<EScreen, Screen>()

    // TODO: 이 곳에 화면 구현체를 명시합니다.
    val screens = listOf(
        MainMenuScreen(),
        SubMenuScreen(),
    )

    for (screen in screens)
    {
        screenMap[screen.eScreen] = screen
    }

    return screenMap
}