package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.EmptyGoalView
import dev.myaring.mobile_butler.ui.theme.MbSpacing

@Composable
fun GoalListTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MbSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyGoalView(
            onAddGoalClick = viewModel::onChangeTabToCreation,
            message = "등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오."
        )
    }
}
