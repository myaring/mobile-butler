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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.pacing.GoalPriorityRule
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
    val goals = uiState.goals

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
                text = "목표 목록 (${goals.size})",
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

        if (goals.isEmpty()) {
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
                itemsIndexed(goals) { index, goal ->
                    // 위에서 3개(오늘 화면 펼침 구분선 표시)
                    if (index == 0) {
                        Text(
                            text = "오늘 화면에 펼쳐지는 목표 (상위 3개)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = MbSpacing.xs)
                        )
                    } else if (index == GoalPriorityRule.TOP_GOALS_TODAY_LIMIT) {
                        Spacer(modifier = Modifier.height(MbSpacing.xs))
                        Text(
                            text = "나머지 목표",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = MbSpacing.xs)
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MbShapes.medium,
                        color = MbSurfaceContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MbSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = MbSpacing.xs))
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(MbSpacing.xs))
                                Text(
                                    text = "목표치: ${goal.targetValue.toLong()} ${goal.unit} | 마감일: ${goal.dueDate ?: "기한 없음"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // 순서 변경 버튼
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { viewModel.moveGoalUp(index) },
                                    enabled = index > 0
                                ) {
                                    Text("▲")
                                }
                                TextButton(
                                    onClick = { viewModel.moveGoalDown(index) },
                                    enabled = index < goals.size - 1
                                ) {
                                    Text("▼")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
