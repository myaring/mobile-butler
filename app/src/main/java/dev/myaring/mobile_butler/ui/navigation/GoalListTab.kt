package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel

// 이 곳에서 탭의 UI 구조를 선언하세요.
@Composable
public fun GoalListTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier
)
{
    Text("This tab is ${viewModel.currentTab.name}.")

    TextButton(onClick = viewModel::onChangeTabToDetail) {
        Text("목표상세")
    }
    TextButton(onClick = viewModel::onChangeTabToCreation) {
        Text("목표만들기")
    }
}