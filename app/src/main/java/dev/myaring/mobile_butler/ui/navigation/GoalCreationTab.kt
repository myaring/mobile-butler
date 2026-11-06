package dev.myaring.mobile_butler.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.domain.model.Goal
import dev.myaring.mobile_butler.domain.model.GoalType
import dev.myaring.mobile_butler.domain.pacing.GoalCreationValidator
import dev.myaring.mobile_butler.domain.pacing.PacingCalculator
import dev.myaring.mobile_butler.domain.pacing.PacingResult
import dev.myaring.mobile_butler.domain.pacing.ValidationResult
import dev.myaring.mobile_butler.domain.viewmodel.GoalScreenViewModel
import dev.myaring.mobile_butler.ui.component.MbButton
import dev.myaring.mobile_butler.ui.component.MbButtonStyle
import dev.myaring.mobile_butler.ui.theme.MbRadius
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalCreationTab(
    viewModel: GoalScreenViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val today = remember { LocalDate.now() }

    var title by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("쪽") }
    var rawTargetAmount by remember { mutableStateOf("") }
    var daysToAdd by remember { mutableStateOf(30L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dueDate = remember(daysToAdd) { today.plusDays(daysToAdd) }

    // 실시간 페이싱 계산 미리보기
    val pacingPreview by remember(rawTargetAmount, dueDate) {
        derivedStateOf {
            val amount = rawTargetAmount.toLongOrNull()
            if (amount != null && amount > 0L) {
                try {
                    val previewGoal = Goal(
                        id = "preview",
                        title = "미리보기",
                        type = GoalType.CUMULATIVE,
                        unit = unit,
                        targetValue = amount.toDouble(),
                        startDate = today,
                        dueDate = dueDate,
                        priority = 0
                    )
                    when (val result = PacingCalculator.calculateOn(previewGoal, emptyList(), today)) {
                        is PacingResult.Daily -> "하루 권장 분량: 약 ${result.quota.toLong()} $unit (남은 일수: ${result.daysRemaining}일)"
                        is PacingResult.Overdue -> "마감 기한이 지났습니다."
                        is PacingResult.Completed -> "목표가 이미 달성되었습니다."
                        else -> null
                    }
                } catch (_: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(MbSpacing.md),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "새 목표 만들기",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(MbSpacing.md))

        // 1. 목표 종류 선택
        Text(
            text = "목표 종류",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MbSpacing.xs)
        ) {
            MbButton(
                text = "누적형",
                onClick = {},
                style = MbButtonStyle.Filled,
                modifier = Modifier.weight(1f)
            )
            MbButton(
                text = "줄이기(준비 중)",
                onClick = {},
                enabled = false,
                style = MbButtonStyle.Outlined,
                modifier = Modifier.weight(1f)
            )
            MbButton(
                text = "빈도(준비 중)",
                onClick = {},
                enabled = false,
                style = MbButtonStyle.Outlined,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(MbSpacing.md))

        // 2. 목표 이름
        Text(
            text = "목표 이름",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            placeholder = { Text("예: 토익 단어장 완독, 알고리즘 100제") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MbSpacing.md))

        // 3. 단위
        Text(
            text = "단위",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        OutlinedTextField(
            value = unit,
            onValueChange = {
                unit = it
                errorMessage = null
            },
            placeholder = { Text("쪽, 분, 개, 문제, km 등") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MbSpacing.xs))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MbSpacing.xs)
        ) {
            listOf("쪽", "분", "개", "문제", "km", "시간").forEach { preset ->
                TextButton(onClick = { unit = preset }) {
                    Text(preset)
                }
            }
        }

        Spacer(modifier = Modifier.height(MbSpacing.sm))

        // 4. 전체 분량 (정수만 입력 가능)
        Text(
            text = "전체 목표 분량 (정수만 입력)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        OutlinedTextField(
            value = rawTargetAmount,
            onValueChange = { input ->
                if (input.contains(".")) {
                    errorMessage = "전체 분량은 정수만 입력할 수 있습니다."
                } else {
                    rawTargetAmount = input.filter { it.isDigit() }
                    errorMessage = null
                }
            },
            placeholder = { Text("예: 300") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(MbSpacing.md))

        // 5. 기간 (시작일 / 마감일)
        Text(
            text = "마감일 설정 (오늘: $today)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        Text(
            text = "마감일: $dueDate (${daysToAdd}일 후)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(MbSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MbSpacing.xs)
        ) {
            listOf(7L, 14L, 30L, 60L, 100L).forEach { days ->
                MbButton(
                    text = "+${days}일",
                    onClick = {
                        daysToAdd = days
                        errorMessage = null
                    },
                    style = if (daysToAdd == days) MbButtonStyle.Filled else MbButtonStyle.Outlined,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(MbSpacing.md))

        // 6. 하루 분량 미리보기
        if (pacingPreview != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MbSurfaceContainer,
                shape = MbShapes.medium
            ) {
                Text(
                    text = pacingPreview ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(MbSpacing.md)
                )
            }
            Spacer(modifier = Modifier.height(MbSpacing.md))
        }

        // 에러 메시지 안내
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = MbSpacing.sm)
            )
        }

        // 7. 저장 및 취소 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MbSpacing.sm)
        ) {
            MbButton(
                text = "취소",
                onClick = viewModel::onChangeTabToList,
                style = MbButtonStyle.Outlined,
                modifier = Modifier.weight(1f)
            )

            MbButton(
                text = "목표 저장",
                onClick = {
                    val titleRes = GoalCreationValidator.validateTitle(title)
                    if (titleRes is ValidationResult.Invalid) {
                        errorMessage = titleRes.reason
                        return@MbButton
                    }

                    val unitRes = GoalCreationValidator.validateUnit(unit)
                    if (unitRes is ValidationResult.Invalid) {
                        errorMessage = unitRes.reason
                        return@MbButton
                    }

                    val amountRes = GoalCreationValidator.validateTargetAmount(rawTargetAmount)
                    if (amountRes is ValidationResult.Invalid) {
                        errorMessage = amountRes.reason
                        return@MbButton
                    }

                    val dateRes = GoalCreationValidator.validateDates(today, dueDate, today)
                    if (dateRes is ValidationResult.Invalid) {
                        errorMessage = dateRes.reason
                        return@MbButton
                    }

                    val targetValue = rawTargetAmount.toDouble()
                    val success = viewModel.createGoal(
                        title = title,
                        unit = unit,
                        targetValue = targetValue,
                        startDate = today,
                        dueDate = dueDate
                    )

                    if (!success) {
                        errorMessage = "목표를 저장하지 못했습니다. 입력값을 확인해 주십시오."
                    }
                },
                style = MbButtonStyle.Filled,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(MbSpacing.xl))
    }
}
