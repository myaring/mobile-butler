package dev.myaring.mobile_butler.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.myaring.mobile_butler.domain.viewmodel.ITopBarViewModel
import dev.myaring.mobile_butler.domain.viewmodel.ScheduleScreenViewModel
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

public val scheduleScreenImpl = Screen(
    eScreen = EScreen.SCHEDULE,
    screenDrawerDelegateeOrNull = null,
)

@Composable
private fun ScheduleScreenRouter(onNotifyTopBar: (ITopBarViewModel) -> Unit, modifier: Modifier)
{
    val viewModel: ScheduleScreenViewModel = viewModel()

    onNotifyTopBar(viewModel)
    ScheduleScreenContent(viewModel, modifier)
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
private fun ScheduleScreenRouterPreview()
{
    val viewModel: ScheduleScreenViewModel = ScheduleScreenViewModel()

    MobilebutlerTheme() {
        ScheduleScreenContent(
            viewModel = viewModel,
            modifier = Modifier)
    }
}

@Composable
private fun ScheduleScreenContent(
    viewModel: ScheduleScreenViewModel,
    modifier: Modifier)
{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MbSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("This page is ${EScreen.SCHEDULE.name}.")
    }
}