package dev.myaring.mobile_butler.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.myaring.mobile_butler.model.GoalStatus
import dev.myaring.mobile_butler.ui.components.AddEditGoalDialog
import dev.myaring.mobile_butler.ui.components.ButlerHeaderCard
import dev.myaring.mobile_butler.ui.components.ButlerSettingsDialog
import dev.myaring.mobile_butler.ui.components.GoalCard
import dev.myaring.mobile_butler.ui.components.GoalDetailDialog
import dev.myaring.mobile_butler.ui.components.LogProgressDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButlerMainScreen(
    viewModel: ButlerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val overdueCount = uiState.pacingResults.values.count { it.status == GoalStatus.OVERDUE }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "모바일 집사",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddGoalDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "+ 목표 추가",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Butler Speech & Header Card
            item {
                ButlerHeaderCard(
                    persona = uiState.persona,
                    dialogue = uiState.activeDialogue,
                    totalGoalsCount = uiState.goals.size,
                    overdueCount = overdueCount,
                    onOpenSettings = { viewModel.openSettingsDialog() }
                )
            }

            // Section title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "목표 목록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "우선순위 순서대로 표시됩니다",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state
            if (uiState.goals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "등록된 목표가 없습니다.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "새로운 목표를 추가하고 집사와 함께 하루 분량을 정복해보세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { viewModel.openAddGoalDialog() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ 첫 목표 등록하기", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = uiState.goals,
                    key = { _, goal -> goal.id }
                ) { index, goal ->
                    val pacing = uiState.pacingResults[goal.id]
                    if (pacing != null) {
                        GoalCard(
                            goal = goal,
                            pacing = pacing,
                            isFirst = index == 0,
                            isLast = index == uiState.goals.size - 1,
                            canReorder = uiState.goals.size > 1,
                            onMoveUp = { viewModel.reorderUp(goal.id) },
                            onMoveDown = { viewModel.reorderDown(goal.id) },
                            onLogProgress = { viewModel.openLogProgressDialog(goal) },
                            onDetail = { viewModel.openGoalDetailDialog(goal) },
                            onDelete = { viewModel.deleteGoal(goal.id) }
                        )
                    }
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Add Goal Dialog
    if (uiState.isAddGoalDialogOpen) {
        AddEditGoalDialog(
            initialGoal = null,
            today = uiState.today,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.closeAddGoalDialog() },
            onSave = { title, targetText, unit, deadline ->
                viewModel.createGoal(title, targetText, unit, deadline)
            }
        )
    }

    // Edit Goal Dialog
    val editGoal = uiState.selectedGoalForEdit
    if (editGoal != null) {
        AddEditGoalDialog(
            initialGoal = editGoal,
            today = uiState.today,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.closeEditGoalDialog() },
            onSave = { title, targetText, unit, deadline ->
                viewModel.updateGoal(editGoal.id, title, targetText, unit, deadline)
            }
        )
    }

    // Log Progress Dialog
    val logGoal = uiState.selectedGoalForLog
    if (logGoal != null) {
        val pacing = uiState.pacingResults[logGoal.id]
        if (pacing != null) {
            LogProgressDialog(
                goal = logGoal,
                pacing = pacing,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.closeLogProgressDialog() },
                onSave = { amountText, note ->
                    viewModel.logProgress(logGoal.id, amountText, note)
                }
            )
        }
    }

    // Goal Detail Dialog
    val detailGoal = uiState.selectedGoalForDetail
    if (detailGoal != null) {
        val pacing = uiState.pacingResults[detailGoal.id]
        if (pacing != null) {
            GoalDetailDialog(
                goal = detailGoal,
                pacing = pacing,
                logs = viewModel.getLogsForGoal(detailGoal.id),
                onDismiss = { viewModel.closeGoalDetailDialog() },
                onEditGoal = {
                    viewModel.closeGoalDetailDialog()
                    viewModel.openEditGoalDialog(detailGoal)
                },
                onDeleteLog = { logId ->
                    viewModel.deleteLog(logId)
                }
            )
        }
    }

    // Butler Settings Dialog
    if (uiState.isSettingsDialogOpen) {
        ButlerSettingsDialog(
            currentPersona = uiState.persona,
            onDismiss = { viewModel.closeSettingsDialog() },
            onSelectPersona = { newPersona ->
                viewModel.selectPersona(newPersona)
            }
        )
    }
}
