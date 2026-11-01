package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel

// 이 곳에서 탭의 UI 구조를 선언하세요.
@Composable
public fun GoalDetailTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier
)
{
    Text("This tab is ${viewModel.currentTab.name}.")
}