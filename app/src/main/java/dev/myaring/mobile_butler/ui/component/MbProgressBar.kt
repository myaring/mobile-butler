package dev.myaring.mobile_butler.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.myaring.mobile_butler.ui.theme.MbPrimary
import dev.myaring.mobile_butler.ui.theme.MbSurfaceVariant

/**
 * [B-3] 진행 상황을 직관적으로 보여주는 공용 프로그레스 바.
 *
 * @param progress 0.0f..1.0f 사이의 진행률 (범위 초과 시 자동 클램프)
 * @param contentDescription 스크린 리더(접근성)를 위한 설명 텍스트
 * @param modifier 레이아웃 제어용 Modifier
 * @param trackColor 미완료 트랙 배경 색상
 * @param fillColor 진행률 채움 색상
 * @param height 프로그레스 바 높이 (기본 8.dp)
 */
@Composable
fun MbProgressBar(
    progress: Float,
    contentDescription: String,
    modifier: Modifier = Modifier,
    trackColor: Color = MbSurfaceVariant,
    fillColor: Color = MbPrimary,
    height: Dp = 8.dp,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(height / 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
            .semantics {
                this.contentDescription = contentDescription
                this.progressBarRangeInfo = ProgressBarRangeInfo(clampedProgress, 0f..1f)
            }
    ) {
        if (clampedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .fillMaxHeight()
                    .clip(shape)
                    .background(fillColor)
            )
        }
    }
}
