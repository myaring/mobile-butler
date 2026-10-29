package dev.myaring.mobile_butler.domain.view.screen

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.view.viewmodel.MainMenuViewModel
import dev.myaring.mobile_butler.domain.view.viewmodel.ScreenViewModel
import dev.myaring.mobile_butler.emptyActivity
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

// NOTE: 본 예제 코드는 화면 구성 프로그래밍을 어떻게 수행하는지 보여줍니다.

// Screen 클래스를 상속하여 새로운 화면을 구성할 수 있습니다.
// Enum을 이용해 화면 유형을 결정할 수 있습니다. Enum은 화면 고유 아이디로 사용될 수도 있습니다.
final class MainMenuScreen : Screen(EScreen.MAIN_MENU)
{
    // Main Activity에서 호출해주는 함수입니다.
    // viewModel: viewModel을 얻을 수 있는 함수를 Main Activity로부터 받습니다.
    @Composable
    override fun drawScreen(currentActivity: ComponentActivity, modifier: Modifier)
    {
        val vmMainMenu: MainMenuViewModel by currentActivity.viewModels()
//        val uiState by vmMainMenu.uiState.collectAsStateWithLifecycle()

        // 이하부터는 Jetpack Compose UI 패키지의 권장 사항에 따라
        // 선언형 프로그래밍 방식으로 UI 구조를 정의하세요.

        Scaffold(
            modifier = modifier.fillMaxSize(),
            content = drawScreenContent_Button(currentActivity, modifier)
//            content = drawScreenContent_Greeting(vmMainMenu, modifier)
        )
    }

    // 람다 함수의 가독성 이슈가 발생할 경우 다음과 같이 코드를 분리할 수 있습니다.
    // 외부 접근을 막기 위해 private 키워드가 권장되나, 필수 사항은 아닙니다.
    private fun drawScreenContent_Greeting(viewModel: MainMenuViewModel, modifier: Modifier) : @Composable (PaddingValues) -> Unit
    {
        @Composable
        fun Content(padding: PaddingValues) : Unit
        {
            Text(
                text = "Hello MainMenuScreen!",
                modifier = modifier
            )
        }

        return ::Content
    }

    // 람다 함수의 가독성 이슈가 발생할 경우 다음과 같이 코드를 분리할 수 있습니다.
    // 외부 접근을 막기 위해 private 키워드가 권장되나, 필수 사항은 아닙니다.
    private fun drawScreenContent_Button(currentActivity: ComponentActivity, modifier: Modifier) : @Composable (PaddingValues) -> Unit
    {
        val vmMainMenu: MainMenuViewModel by currentActivity.viewModels()
        val vmScreen: ScreenViewModel by currentActivity.viewModels()

        @Composable
        fun Content(padding: PaddingValues) : Unit
        {
            val uiState by vmMainMenu.uiState.collectAsStateWithLifecycle()

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "현재 ui == Main, uiState == $uiState"
                )

                Button(
                    onClick = {
                        vmMainMenu.onButtonClick()
                        vmScreen.changeScreen(EScreen.SUB_MENU)
                    }
                ) {
                    Text(text = "To Sub Menu")
                }

                Button(
                    onClick = {
                        vmMainMenu.onScoreUp()
                    }
                ) {
                    Text(text = "uiState += 1")
                }
            }
        }

        return ::Content
    }
}

// 앱을 실행하지 않고 화면의 구성을 미리 볼 수 있습니다.
// Design 버튼 혹은 Split 버튼을 눌러 확인하세요.
// 반드시 private 키워드를 명시하여 외부 모듈에서 접근할 수 없도록 합니다.
@Preview
@Composable
private fun drawScreenPreview()
{
    MobilebutlerTheme {
        MainMenuScreen().drawScreen(emptyActivity, Modifier)
    }
}