package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.GoalListItem
import dev.myaring.mobile_butler.ui.theme.MbOnPrimary
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbPrimary
import dev.myaring.mobile_butler.ui.theme.MbSpacing

/**
 * [C-5] 목표 탭 목록 화면.
 *
 * 완료 조건:
 * - "진행 중 N/보관함" 탭 행과 GoalListItem 목록이 보인다.
 * - 행을 누르면 목표 상세로 이동한다. 이 탭에서는 기록하지 않는다.
 * - 우측 하단 "목표 추가" 버튼이 목표 만들기를 연다.
 */
@Composable
fun GoalListTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeGoals = uiState.activeGoals

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (activeGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MbSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "진행 중인 목표가 없습니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MbOnSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = MbSpacing.md,
                    vertical = MbSpacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.sm)
            ) {
                items(activeGoals, key = { it.id }) { goal ->
                    val typeLabel = when (goal.type) {
                        GoalType.CUMULATIVE -> "누적"
                        GoalType.TARGET_VALUE -> "목표치"
                        GoalType.RECURRING -> "하루"
                        GoalType.FREQUENCY -> "빈도"
                        GoalType.CHECKLIST -> "체크리스트"
                    }

                    val summary = buildSummary(goal)

                    GoalListItem(
                        title = goal.title,
                        typeLabel = typeLabel,
                        summary = summary,
                        progress = 0f,
                        showProgress = true,
                        onClick = { viewModel.selectGoal(goal.id) }
                    )
                }
            }
        }

        // 우측 하단 "목표 추가" 플로팅 버튼
        ExtendedFloatingActionButton(
            onClick = viewModel::onChangeTabToCreation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MbSpacing.lg),
            containerColor = MbPrimary,
            contentColor = MbOnPrimary,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            },
            text = {
                Text("목표 추가")
            }
        )
    }
}

private fun buildSummary(goal: Goal): String {
    val target = if (goal.targetValue % 1.0 == 0.0) {
        goal.targetValue.toLong().toString()
    } else {
        goal.targetValue.toString()
    }

    val dueText = goal.dueDate?.let { "마감일: $it" } ?: "기한 없음"
    return "목표: $target${goal.unit} | $dueText"
}
