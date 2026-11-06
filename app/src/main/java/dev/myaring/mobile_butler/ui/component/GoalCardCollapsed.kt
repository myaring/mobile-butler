package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MbOnSurface
import dev.myaring.mobile_butler.ui.theme.MbOnSurfaceVariant
import dev.myaring.mobile_butler.ui.theme.MbShapes
import dev.myaring.mobile_butler.ui.theme.MbSpacing
import dev.myaring.mobile_butler.ui.theme.MbSurfaceContainer

/**
 * [B-6] GoalCard 접힌 상태 컴포넌트.
 *
 * 완료 조건:
 * - 아이콘과 한 줄 요약
 * - 테두리 버튼 ('기록')
 * - 카드를 누르면 호출되는 콜백 (펼치기)
 *
 * @param title 목표 이름
 * @param summary 한 줄 요약 (예: "오늘의 목표 20쪽 · D-26")
 * @param onCardClick 카드를 탭했을 때 호출되는 콜백 (카드 펼침 트리거)
 * @param onRecordClick 우측 테두리 '기록' 버튼을 눌렀을 때 호출되는 콜백 (기록 시트 직접 오픈)
 * @param icon 좌측 표시 아이콘 (기본값: List 아이콘)
 * @param modifier 레이아웃 제어용 Modifier
 */
@Composable
fun GoalCardCollapsed(
    title: String,
    summary: String,
    onCardClick: () -> Unit,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Filled.List,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        color = MbSurfaceContainer,
        shape = MbShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MbSpacing.lg, vertical = MbSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MbOnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(MbSpacing.md))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MbOnSurface
                    )
                    Spacer(modifier = Modifier.padding(top = MbSpacing.xs))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MbOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(MbSpacing.md))

            MbButton(
                text = "기록",
                onClick = onRecordClick,
                style = MbButtonStyle.Outlined
            )
        }
    }
}
