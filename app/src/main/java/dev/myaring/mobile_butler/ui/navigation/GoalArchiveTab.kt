package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.model.EndReason
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.theme.MbOnBackground
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbRecordDone
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant

/**
 * [C-9] 목표 보관함 탭 화면 컴포넌트.
 *
 * 완료 조건:
 * - 보관함은 COMPLETED 목표의 목록이다.
 * - endReason이 MANUAL 인 목표에는 조기 종료 표시를 붙인다.
 * - 목록(메인)에는 읽은/달성한 양을 보여주고, 원래 목표 대비 %는 상세 화면에서만 보여준다.
 * - 실패나 못 한 날을 알리는 문구는 쓰지 않는다.
 */
@Composable
fun GoalArchiveTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val archivedGoals = uiState.archivedGoals

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (archivedGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(MbSpacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "보관된 목표가 없습니다.",
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
                items(archivedGoals, key = { it.id }) { goal ->
                    val entries = uiState.entries[goal.id] ?: emptyList()
                    val totalRecorded = entries.sumOf { it.amount }

                    ArchiveGoalCard(
                        goal = goal,
                        recordedAmount = totalRecorded,
                        onClick = { viewModel.selectGoal(goal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveGoalCard(
    goal: Goal,
    recordedAmount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(MbRadius.md),
        colors = CardDefaults.cardColors(containerColor = MbSurfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MbSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MbSpacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MbSpacing.sm)
                ) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MbOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // 종료 사유 라벨: MANUAL -> 조기 종료, ACHIEVED -> 목표 달성, DEADLINE -> 기한 종료
                    val (label, badgeColor) = when (goal.endReason) {
                        EndReason.MANUAL -> "조기 종료" to MbSurfaceVariant
                        EndReason.ACHIEVED -> "목표 달성" to MbRecordDone
                        EndReason.DEADLINE -> "기한 종료" to MbSurfaceVariant
                        null -> "종료" to MbSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(MbRadius.xs),
                        color = badgeColor
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MbOnBackground,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // 읽은/달성한 양만 표시 (원래 목표 대비 %는 상세 화면에서만 표시, 실패 문구 배제)
                val amountText = if (recordedAmount % 1.0 == 0.0) {
                    recordedAmount.toLong().toString()
                } else {
                    recordedAmount.toString()
                }

                val endedDateText = goal.endedAt?.let { "끝낸 날: $it" } ?: ""
                Text(
                    text = "${amountText}${goal.unit} 완료 · $endedDateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MbOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = MbSpacing.xs))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "상세 보기",
                tint = MbOnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
