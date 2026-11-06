package dev.myaring.mobile_butler.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel
import dev.myaring.mobile_butler.domain.viewmodel.TodayScreenViewModel
import dev.myaring.mobile_butler.ui.component.EmptyGoalView
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

public val todayScreenImpl = Screen(
    eScreen = EScreen.TODAY,
    screenDrawerDelegateeOrNull = ::TodayScreenRouter,
)

@Composable
private fun TodayScreenRouter(onNotifyTopBar: (ITopBarViewModel) -> Unit, modifier: Modifier) {
    val viewModel: TodayScreenViewModel = viewModel()

    onNotifyTopBar(viewModel)
    TodayScreenContent(viewModel, modifier)
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun TodayScreenRouterPreview() {
    val viewModel: TodayScreenViewModel = TodayScreenViewModel()

    MobilebutlerTheme {
        TodayScreenContent(
            viewModel = viewModel,
            modifier = Modifier
        )
    }
}

@Composable
private fun TodayScreenContent(
    viewModel: TodayScreenViewModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MbSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyGoalView(
            onAddGoalClick = {},
            message = "오늘 등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오."
        )
    }
}
