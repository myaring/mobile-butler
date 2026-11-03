package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MobilebutlerTheme

enum class MbButtonStyle { Filled, Outlined, TextOnly }

@Composable
fun MbButton (
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: MbButtonStyle = MbButtonStyle.Filled,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val buttonModifier = modifier.heightIn(min = 48.dp)
    val content: @Composable RowScope.() -> Unit = {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text)
    }
    when (style) {
        MbButtonStyle.Filled -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            content = content,
        )
        MbButtonStyle.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            content = content,
        )

        MbButtonStyle.TextOnly -> TextButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
            content = content,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MbButtonPreview() {
    MobilebutlerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding((16.dp)),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MbButton("기록하기", onClick = {}, style = MbButtonStyle.Filled)
                MbButton("기록하기", onClick = {}, style = MbButtonStyle.Outlined)
                MbButton("기록하기", onClick = {}, style = MbButtonStyle.TextOnly)
                MbButton("아이콘 있음", onClick = {}, icon = Icons.Default.Add)
                MbButton("비활성", onClick = {}, enabled = false)
                MbButton("비활성 테두리", onClick = {}, style = MbButtonStyle.Outlined, enabled = false)
            }
        }
    }
}