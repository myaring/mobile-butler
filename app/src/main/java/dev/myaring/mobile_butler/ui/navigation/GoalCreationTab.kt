package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.myaring.mobile_butler.domain.pacing.GoalCreationValidator
import dev.myaring.mobile_butler.domain.pacing.ValidationResult
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.MbButton
import dev.myaring.mobile_butler.ui.component.MbButtonStyle
import dev.myaring.mobile_butler.ui.theme.MbBackground
import dev.myaring.mobile_butler.ui.theme.MbOnBackground
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant
import java.time.LocalDate

/**
 * [C-4] & [C-12] 목표 만들기 및 수정 탭 화면 컴포넌트.
 *
 * 완료 조건:
 * - 목표 만들기 화면을 수정 모드로 재사용
 * - 마감일 연장 시 하루 분량이 다시 계산된다.
 * - 목표량을 낮춰서 기록 합계가 목표량 이상이 되면 바로 완료하지 않고
 *   "이미 목표량을 채우셨습니다. 완료하시겠습니까?" 라고 한 번 묻는다. (예 > ACHIEVED, 아니오 > ACTIVE 유지)
 */
@Composable
fun GoalCreationTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editingGoal = uiState.editingGoal
    val isEditMode = editingGoal != null

    var title by remember(editingGoal) { mutableStateOf(editingGoal?.title ?: "") }
    var unit by remember(editingGoal) { mutableStateOf(editingGoal?.unit ?: "쪽") }
    var targetValueRaw by remember(editingGoal) {
        mutableStateOf(editingGoal?.targetValue?.toLong()?.toString() ?: "100")
    }

    val today = LocalDate.now()
    var dueDate by remember(editingGoal) {
        mutableStateOf(editingGoal?.dueDate ?: today.plusDays(10))
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MbSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(MbSpacing.md)
    ) {
        Text(
            text = if (isEditMode) "목표 수정" else "새 목표 만들기",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MbOnBackground
        )

        Text(
            text = if (isEditMode) "목표 분량이나 마감일을 수정합니다." else "달성하고자 하는 누적형 목표를 등록하십시오.",
            style = MaterialTheme.typography.bodySmall,
            color = MbOnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MbSpacing.xs))

        // 목표 이름 입력
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = { Text("목표 이름") },
            placeholder = { Text("예: 코틀린 완벽 가이드") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MbOnSurface,
                unfocusedTextColor = MbOnSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MbSurfaceVariant
            )
        )

        // 전체 목표 분량
        OutlinedTextField(
            value = targetValueRaw,
            onValueChange = {
                targetValueRaw = it
                errorMessage = null
            },
            label = { Text("전체 목표 분량") },
            placeholder = { Text("예: 100") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MbOnSurface,
                unfocusedTextColor = MbOnSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MbSurfaceVariant
            )
        )

        // 단위 입력
        OutlinedTextField(
            value = unit,
            onValueChange = {
                unit = it
                errorMessage = null
            },
            label = { Text("단위") },
            placeholder = { Text("예: 쪽, 문제, km") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MbOnSurface,
                unfocusedTextColor = MbOnSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MbSurfaceVariant
            )
        )

        // 마감일 연장 및 설정
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "마감일",
                    style = MaterialTheme.typography.labelMedium,
                    color = MbOnSurfaceVariant
                )
                Text(
                    text = dueDate.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MbOnBackground
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(MbSpacing.xs)) {
                TextButton(onClick = { dueDate = dueDate.plusDays(7) }) {
                    Text("+7일 연장")
                }
                TextButton(onClick = { dueDate = dueDate.plusDays(30) }) {
                    Text("+30일 연장")
                }
            }
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(MbSpacing.lg))

        // 저장 버튼
        MbButton(
            text = if (isEditMode) "수정 완료" else "목표 만들기",
            onClick = {
                val titleValidation = GoalCreationValidator.validateTitle(title)
                if (titleValidation is ValidationResult.Invalid) {
                    errorMessage = titleValidation.reason
                    return@MbButton
                }

                val unitValidation = GoalCreationValidator.validateUnit(unit)
                if (unitValidation is ValidationResult.Invalid) {
                    errorMessage = unitValidation.reason
                    return@MbButton
                }

                val amountValidation = GoalCreationValidator.validateTargetAmount(targetValueRaw)
                if (amountValidation is ValidationResult.Invalid) {
                    errorMessage = amountValidation.reason
                    return@MbButton
                }

                val targetValue = targetValueRaw.toDouble()

                if (editingGoal != null) {
                    viewModel.saveGoalEdits(
                        goalId = editingGoal.id,
                        newTitle = title,
                        newTarget = targetValue,
                        newUnit = unit,
                        newDueDate = dueDate
                    )
                } else {
                    viewModel.onChangeTabToList()
                }
            },
            style = MbButtonStyle.Filled,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // [C-12] 목표량을 낮춰서 이미 채운 경우의 확인 팝업
    if (uiState.showAchievedConfirmDialog && editingGoal != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAchievedKeepActive(editingGoal.id) },
            title = { Text("목표 달성 확인") },
            text = { Text("이미 목표량을 채우셨습니다. 완료하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmAchieved(editingGoal.id) }) {
                    Text("예")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissAchievedKeepActive(editingGoal.id) }) {
                    Text("아니오")
                }
            }
        )
    }
}
