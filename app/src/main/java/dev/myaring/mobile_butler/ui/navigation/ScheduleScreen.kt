package dev.myaring.mobile_butler.ui.navigation

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
import dev.myaring.mobile_butler.domain.viewmodel.ScheduleScreenViewModel
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

public val scheduleScreenImpl = Screen(
    screenDrawerDelegatee = { ScheduleScreen() },
)

@Composable
private fun ScheduleScreen(viewModel: ScheduleScreenViewModel = viewModel())
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

@Preview
@Composable
private fun ScheduleScreenPreview()
{
    MobilebutlerTheme() { ScheduleScreen() }
}