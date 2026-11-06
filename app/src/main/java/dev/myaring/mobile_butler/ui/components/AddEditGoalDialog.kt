package dev.myaring.mobile_butler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.PacingResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditGoalDialog(
    initialGoal: Goal? = null,
    today: LocalDate = LocalDate.now(),
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmountText: String, unit: String, deadline: LocalDate?) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var targetAmountText by remember {
        mutableStateOf(initialGoal?.let { PacingResult.formatNumber(it.targetAmount) } ?: "")
    }
    var unit by remember { mutableStateOf(initialGoal?.unit ?: "쪽") }
    var deadlineDate by remember {
        mutableStateOf(initialGoal?.deadline ?: today.plusDays(30))
    }
    var deadlineInputText by remember {
        mutableStateOf(deadlineDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    val presetUnits = listOf("쪽", "문제", "km", "시간", "회", "개")
    val presetDays = listOf(7, 14, 30, 60, 100)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialGoal == null) "새 목표 등록" else "목표 수정",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("목표 이름") },
                    placeholder = { Text("예: 토익 단어장 완독") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Target Amount
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("목표치") },
                    placeholder = { Text("예: 300") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unit selection
                Text(
                    text = "단위 선택",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetUnits.forEach { preset ->
                        FilterChip(
                            selected = unit == preset,
                            onClick = { unit = preset },
                            label = { Text(preset) }
                        )
                    }
                }

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("직접 입력 단위") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Deadline selection
                Text(
                    text = "마감일 설정",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetDays.forEach { days ->
                        val target = today.plusDays(days.toLong())
                        FilterChip(
                            selected = deadlineDate == target,
                            onClick = {
                                deadlineDate = target
                                deadlineInputText = target.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            },
                            label = { Text("+${days}일") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = deadlineInputText,
                    onValueChange = { input ->
                        deadlineInputText = input
                        try {
                            val parsed = LocalDate.parse(input)
                            deadlineDate = parsed
                        } catch (_: Exception) {
                            // Keep raw input while typing
                        }
                    },
                    label = { Text("마감 날짜 (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalDeadline = try {
                        LocalDate.parse(deadlineInputText)
                    } catch (_: Exception) {
                        deadlineDate
                    }
                    onSave(title, targetAmountText, unit, finalDeadline)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("취소")
            }
        }
    )
}
