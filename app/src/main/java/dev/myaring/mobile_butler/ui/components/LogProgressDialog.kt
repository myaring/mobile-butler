package dev.myaring.mobile_butler.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.model.Goal
import dev.myaring.mobile_butler.model.PacingResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogProgressDialog(
    goal: Goal,
    pacing: PacingResult,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (amountText: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val quickAmounts = listOf(1.0, 5.0, 10.0, 20.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "오늘 분량 기록",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Goal summary box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "진행: ${pacing.currentAmountFormatted} / ${pacing.targetAmountFormatted} ${goal.unit}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "오늘 권장: ${pacing.dailyTargetFormatted} ${goal.unit}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick add chips
                Text(
                    text = "빠른 입력",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.forEach { amount ->
                        val formatted = PacingResult.formatNumber(amount)
                        FilterChip(
                            selected = false,
                            onClick = {
                                val current = amountText.toDoubleOrNull() ?: 0.0
                                amountText = PacingResult.formatNumber(current + amount)
                            },
                            label = { Text("+${formatted}${goal.unit}") }
                        )
                    }

                    if (pacing.dailyTarget > 0) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                amountText = pacing.dailyTargetFormatted
                            },
                            label = { Text("오늘 권장치 (${pacing.dailyTargetFormatted}${goal.unit})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("기록할 분량 (${goal.unit})") },
                    placeholder = { Text("예: 10") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("메모 (선택사항)") },
                    placeholder = { Text("예: 3장 연습문제 풀이 완료") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(amountText, note) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("기록 완료")
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
