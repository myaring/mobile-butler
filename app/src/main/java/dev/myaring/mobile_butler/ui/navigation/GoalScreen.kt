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

val goalScreenImpl = Screen(
    eScreen = EScreen.GOAL,
    screenDrawerDelegateeOrNull = ::GoalScreenRouter,
)

@Composable
private fun GoalScreenRouter(onNotifyTopBar: (ITopBarViewModel) -> Unit, modifier: Modifier) {
    val viewModel: GoalScreenViewModel = viewModel()
    onNotifyTopBar(viewModel)
    GoalScreenContent(viewModel, modifier)
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun GoalScreenRouterPreview() {
    val viewModel = GoalScreenViewModel()
    MobilebutlerTheme {
        GoalScreenContent(
            viewModel = viewModel,
            modifier = Modifier
        )
    }
}

@Composable
private fun GoalScreenContent(
    viewModel: GoalScreenViewModel,
    modifier: Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MbSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val subTabOrdinal = uiState.subTabOrdinal

        if (subTabOrdinal >= 0) {
            PrimaryTabRow(
                selectedTabIndex = subTabOrdinal
            ) {
                Tab(
                    selected = uiState.selectedTab == EGoalTab.LIST,
                    onClick = { viewModel.onChangeTabToList() },
                    text = { Text("진행 중 ${uiState.activeGoalsCount}") }
                )

                Tab(
                    selected = uiState.selectedTab == EGoalTab.ARCHIVE,
                    onClick = { viewModel.onChangeTabToArchive() },
                    text = { Text("보관함") }
                )
            }
        }

        val tabDrawer: TabDrawer<GoalScreenViewModel> =
            when (uiState.selectedTab) {
                EGoalTab.LIST -> ::GoalListTab
                EGoalTab.DETAIL -> ::GoalDetailTab
                EGoalTab.CREATION -> ::GoalCreationTab
                EGoalTab.ARCHIVE -> ::GoalArchiveTab
            }

        tabDrawer(viewModel, Modifier)
    }
}
