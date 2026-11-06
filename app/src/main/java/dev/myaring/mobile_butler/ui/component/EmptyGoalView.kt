package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

@Composable
fun EmptyGoalView(
    onAddGoalClick: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "등록된 목표가 없습니다. 새로운 목표를 등록해 보십시오.",
    buttonText: String = "+ 목표 추가",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MbSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(MbSpacing.md))
        MbButton(
            text = buttonText,
            onClick = onAddGoalClick,
            style = MbButtonStyle.Filled
        )
    }
}

@Preview
@Composable
private fun EmptyGoalViewPreview() {
    MobilebutlerTheme {
        EmptyGoalView(
            onAddGoalClick = {}
        )
    }
}
