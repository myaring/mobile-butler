package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [C-8] 기록 수정/삭제 시트 UI.
 *
 * 완료 조건:
 * - 기록 시트와 같은 형태의 수정 시트
 * - 양을 고치거나 삭제할 수 있다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordSheet(
    title: String,
    unit: String,
    initialAmount: Double,
    onUpdate: (Double) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    var stepperAmount by remember(initialAmount) {
        mutableIntStateOf(initialAmount.toInt().coerceAtLeast(1))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MbSurfaceContainer,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MbSpacing.lg, vertical = MbSpacing.md)
        ) {
            Text(
                text = "$title · 기록 수정",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MbOnSurface
            )

            Spacer(modifier = Modifier.height(MbSpacing.xs))

            Text(
                text = "기존에 기록된 양을 변경하거나 삭제할 수 있습니다",
                style = MaterialTheme.typography.bodySmall,
                color = MbOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MbSpacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (stepperAmount > 1) stepperAmount -= 1 },
                    enabled = stepperAmount > 1
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stepperAmount > 1) MbOnSurface else MbOnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = MbSpacing.md))

                Text(
                    text = "$stepperAmount$unit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MbOnSurface
                )

                Spacer(modifier = Modifier.padding(horizontal = MbSpacing.md))

                IconButton(onClick = { stepperAmount += 1 }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "1 $unit 추가",
                        tint = MbOnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(MbSpacing.xl))

            MbButton(
                text = "$stepperAmount$unit 으로 수정",
                onClick = { onUpdate(stepperAmount.toDouble()) },
                style = MbButtonStyle.Filled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MbSpacing.sm))

            MbButton(
                text = "기록 삭제",
                onClick = onDelete,
                style = MbButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MbSpacing.xl))
        }
    }
}
