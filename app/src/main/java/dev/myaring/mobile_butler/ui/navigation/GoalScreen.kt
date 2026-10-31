package dev.myaring.mobile_butler.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

// 의존성 주입 로직에 노출해주는 변수입니다.
public val goalScreenImpl = Screen(
    eScreen = EScreen.GOAL,
    screenDrawerDelegateeOrNull = ::GoalScreenRouter,
)

// Scaffold->AppContainer->composable에 의해 호출됩니다.
// 라우터 함수; 이 곳에서 ViewModel을 얻을 수 있습니다.
// 이 곳에서 TopAppBar의 상태를 갱신해줄 수 있습니다.
@Composable
private fun GoalScreenRouter(onNotifyTopBar: (ITopBarViewModel) -> Unit, modifier: Modifier)
{
    val viewModel: GoalScreenViewModel = viewModel()

    // TopAppBar 상태 갱신
    // Scaffold로부터 라우터 함수까지 매개변수를 통해 전달받았습니다.
    onNotifyTopBar(viewModel)

    GoalScreenContent(viewModel, modifier)
}

// Preview를 위한 라우터 함수입니다.
// 이 곳에서 Preview 창에 보여줄 구조를 선언하세요.
@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun GoalScreenRouterPreview()
{
    // Preview 함수에서 Dummy ViewModel을 강제로 만들어줄 수 있습니다.
    // Preview 함수에서 ViewModelFactory를 사용하는 경우를 고려한 구조입니다.
    val viewModel: GoalScreenViewModel = GoalScreenViewModel()

    MobilebutlerTheme() {
        GoalScreenContent(
            viewModel = viewModel,
            modifier = Modifier)
    }
}

// 이 곳에서 화면의 UI 구조를 선언하세요.
@Composable
private fun GoalScreenContent(
    viewModel: GoalScreenViewModel,
    modifier: Modifier)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MbSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val subTabOrdinal = uiState.subTabOrdinal

        if (subTabOrdinal >= 0)
        {
            PrimaryTabRow(
                selectedTabIndex = subTabOrdinal
            ) {
                Tab(
                    selected = uiState.selectedTab == EGoalTab.LIST,
                    onClick = { viewModel.onChangeTabToList() },
                    text = { Text("진행 중") }
                )

                Tab(
                    selected = uiState.selectedTab == EGoalTab.ARCHIVE,
                    onClick = { viewModel.onChangeTabToArchive() },
                    text = { Text("보관함") }
                )
            }
        }

        Text("This page is ${EScreen.GOAL.name}.")

        // 하나의 화면은 여러 탭으로 구성되어 있을 수 있습니다.
        // 각 탭은 별도의 파일로 분할되어 있습니다.
        val tabDrawer: TabDrawer<GoalScreenViewModel> =
            when (uiState.selectedTab)
            {
                EGoalTab.LIST -> ::GoalListTab
                EGoalTab.DETAIL -> ::GoalDetailTab
                EGoalTab.CREATION -> ::GoalCreationTab
                EGoalTab.ARCHIVE -> ::GoalArchiveTab
                else -> ::GoalListTab
            }

        tabDrawer(viewModel, modifier)
    }
}