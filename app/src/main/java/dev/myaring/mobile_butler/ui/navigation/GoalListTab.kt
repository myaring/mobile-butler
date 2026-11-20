package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.MbButton
import dev.myaring.mobile_butler.ui.component.MbButtonStyle
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

@Composable
fun GoalListTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(MbSpacing.md),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "진행 중인 목표 (${uiState.goals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            MbButton(
                text = "+ 목표 추가",
                onClick = viewModel::onChangeTabToCreation,
                style = MbButtonStyle.Filled
            )
        }

        Spacer(modifier = Modifier.height(MbSpacing.md))

        if (uiState.goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MbSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.sm)
            ) {
                items(uiState.goals) { goal ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MbShapes.medium,
                        color = MbSurfaceContainer
                    ) {
                        Column(modifier = Modifier.padding(MbSpacing.md)) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(MbSpacing.xs))
                            Text(
                                text = "목표치: ${goal.targetValue.toLong()} ${goal.unit} | 마감일: ${goal.dueDate ?: "기한 없음"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
